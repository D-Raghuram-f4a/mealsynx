package com.mealsynx.service;

import com.mealsynx.dao.*;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Canteen;
import com.mealsynx.model.Customer;
import com.mealsynx.model.FoodItem;
import com.mealsynx.model.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderService {
    public static final String[] CAMPUS_DELIVERY_POINTS = {
        "Floor 1 Delivery Point",
        "Library",
        "Lab 1",
        "Lab 2 (AI & Data Science Block)",
        "Main Auditorium Foyer",
        "Hostel Block A Reception",
        "Student Activity Center"
    };

    private final OrderDAO orderDAO;
    private final MenuDAO menuDAO;
    private final CanteenDAO canteenDAO;
    private final CustomerDAO customerDAO;
    private final EmployeeDAO employeeDAO;
    private final InventoryDAO inventoryDAO;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public OrderService(OrderDAO orderDAO, MenuDAO menuDAO, CanteenDAO canteenDAO,
                        CustomerDAO customerDAO, EmployeeDAO employeeDAO,
                        InventoryDAO inventoryDAO, PaymentService paymentService,
                        NotificationService notificationService) {
        this.orderDAO = orderDAO;
        this.menuDAO = menuDAO;
        this.canteenDAO = canteenDAO;
        this.customerDAO = customerDAO;
        this.employeeDAO = employeeDAO;
        this.inventoryDAO = inventoryDAO;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        recalculateAllCanteenActiveCounts();
    }

    public final void recalculateAllCanteenActiveCounts() {
        for (Canteen c : canteenDAO.findAll()) {
            int active = orderDAO.countActiveOrdersForCanteen(c.getCanteenId());
            c.setActiveOrderCount(active);
        }
    }

    public synchronized Order placeStudentOrder(Customer student, Order order) {
        paymentService.processPayment(student, order);
        order.setStatus(Order.OrderStatus.PLACED);
        orderDAO.save(order);

        Canteen canteen = canteenDAO.findById(order.getCanteenId()).orElse(null);
        if (canteen != null) {
            canteen.addSalesRevenue(order.getTotalAmount());
            canteen.setActiveOrderCount(orderDAO.countActiveOrdersForCanteen(canteen.getCanteenId()));
            canteenDAO.save(canteen);
        }

        for (Order.OrderLineItem li : order.getItems()) {
            student.recordFoodOrdered(li.getFoodId(), li.getFoodName(), li.getQuantity());
            menuDAO.findById(li.getFoodId()).ifPresent(food -> {
                food.incrementOrderCount(li.getQuantity());
                menuDAO.save(food);
                deductIngredientsForFood(food, li.getQuantity());
            });
        }
        customerDAO.save(student);

        String canteenName = canteen != null ? canteen.getName() : order.getCanteenId();
        notificationService.notifyStudent(student,
                String.format("Order %s (₹%.2f) placed at %s [%s | %s]. Status: Order Placed.",
                        order.getOrderId(), order.getTotalAmount(), canteenName,
                        order.getOrderType(), order.getDeliveryMode() == Order.DeliveryMode.CAMPUS_DELIVERY
                                ? ("Delivery to " + order.getDeliveryPoint()) : "Pick-up"));

        notificationService.notifyWorkerForCanteen(employeeDAO, order.getCanteenId(),
                String.format("New Order %s from %s: %s (%s | %s)",
                        order.getOrderId(), student.getUsername(), order.getItemsSummary(),
                        order.getOrderType(), order.getDeliveryPoint()));
        PersistentDataStore.getInstance().saveToDisk();
        return order;
    }

    public synchronized Order createLiveCounterOfflineOrder(String canteenId, String walkInCustomerName,
                                                            List<Order.OrderLineItem> lineItems, String paymentMode) {
        String orderId = "POS-" + (2001 + orderDAO.findAll().size());
        String custLabel = (walkInCustomerName == null || walkInCustomerName.isBlank())
                ? "Walk-In Student (Live Counter)" : walkInCustomerName.trim();
        Order posOrder = new Order(orderId, "WALK-IN", custLabel, canteenId,
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Live Counter Billing",
                "Immediate", paymentMode, true, LocalDateTime.now());

        for (Order.OrderLineItem li : lineItems) {
            posOrder.addItem(li.getFoodId(), li.getFoodName(), li.getQuantity(), li.getUnitPrice(), li.getSpecialRequirement());
            menuDAO.findById(li.getFoodId()).ifPresent(food -> {
                food.incrementOrderCount(li.getQuantity());
                menuDAO.save(food);
                deductIngredientsForFood(food, li.getQuantity());
            });
        }
        posOrder.setStatus(Order.OrderStatus.COLLECTED);
        orderDAO.save(posOrder);
        paymentService.processPayment(null, posOrder);

        canteenDAO.findById(canteenId).ifPresent(c -> {
            c.addSalesRevenue(posOrder.getTotalAmount());
            canteenDAO.save(c);
        });
        PersistentDataStore.getInstance().saveToDisk();
        return posOrder;
    }

    public synchronized Order reorderPastOrder(Customer student, Order pastOrder,
                                               Order.OrderType orderType, Order.DeliveryMode deliveryMode,
                                               String deliveryPoint, String scheduledTime, String paymentMethod) {
        String newOrderId = nextOrderId();
        Order newOrder = new Order(newOrderId, student.getUserId(), student.getUsername(),
                pastOrder.getCanteenId(), orderType, deliveryMode, deliveryPoint,
                scheduledTime, paymentMethod, false, LocalDateTime.now());
        for (Order.OrderLineItem li : pastOrder.getItems()) {
            double currentPrice = menuDAO.findById(li.getFoodId()).map(FoodItem::getPrice).orElse(li.getUnitPrice());
            newOrder.addItem(li.getFoodId(), li.getFoodName(), li.getQuantity(), currentPrice, li.getSpecialRequirement());
        }
        return placeStudentOrder(student, newOrder);
    }

    public synchronized boolean advanceOrderStatusByPlusButton(String orderId) {
        Optional<Order> ordOpt = orderDAO.findById(orderId);
        if (ordOpt.isEmpty()) return false;
        Order order = ordOpt.get();
        Order.OrderStatus nextStatus = order.getNextForwardStatus();
        if (nextStatus == null) {
            return false;
        }
        updateOrderStatusByWorker(orderId, nextStatus);
        return true;
    }

    public synchronized void updateOrderStatusByWorker(String orderId, Order.OrderStatus newStatus) {
        orderDAO.findById(orderId).ifPresent(order -> {
            order.setStatus(newStatus);
            orderDAO.save(order);

            canteenDAO.findById(order.getCanteenId()).ifPresent(c -> {
                c.setActiveOrderCount(orderDAO.countActiveOrdersForCanteen(c.getCanteenId()));
                canteenDAO.save(c);
            });

            customerDAO.findByUsernameOrIdentifier(order.getCustomerUsername()).ifPresent(student -> {
                String msg = switch (newStatus) {
                    case PREPARING -> "👨‍🍳 Your order " + orderId + " is now PREPARING in the kitchen.";
                    case PREPARED -> "✅ Your order " + orderId + " is PREPARED and ready for Pick-up at the counter!";
                    case OUT_FOR_DELIVERY -> "🛵 Your order " + orderId + " is OUT FOR DELIVERY to " + order.getDeliveryPoint() + "!";
                    case DELIVERED -> "🎉 Your order " + orderId + " has been DELIVERED to " + order.getDeliveryPoint() + "!";
                    case COLLECTED -> "🎉 Your order " + orderId + " has been COLLECTED from the counter!";
                    default -> "Order " + orderId + " status updated to " + Order.formatStatusName(newStatus);
                };
                notificationService.notifyStudent(student, msg);
            });
            PersistentDataStore.getInstance().saveToDisk();
        });
    }

    private void deductIngredientsForFood(FoodItem food, int plates) {
        for (Map.Entry<String, Double> bom : food.getRecipeIngredientsPerPlate().entrySet()) {
            inventoryDAO.findById(bom.getKey()).ifPresent(stock -> {
                double used = bom.getValue() * plates;
                stock.setCurrentStock(stock.getCurrentStock() - used);
                inventoryDAO.save(stock);
                if (stock.isLowStock() || stock.isQualityOrExpiryLow()) {
                    notificationService.notifyWorkerForCanteen(employeeDAO, food.getCanteenId(),
                            "⚠ Warehouse Alert: " + stock.getName() + " is at " +
                            String.format("%.1f %s (%s)", stock.getCurrentStock(), stock.getUnit(), stock.getQualityStatusLabel()));
                }
            });
        }
    }

    public List<Order> getOrdersForStudent(String username) { return orderDAO.findByCustomer(username); }
    public List<Order> getOrdersForCanteen(String canteenId) { return orderDAO.findByCanteen(canteenId); }
    public List<Order> getAllOrders() { return orderDAO.findAll(); }

    public String nextOrderId() {
        int maxNum = 1016;
        for (Order o : orderDAO.findAll()) {
            if (o.getOrderId().startsWith("ORD-")) {
                try {
                    int n = Integer.parseInt(o.getOrderId().substring(4));
                    if (n > maxNum) maxNum = n;
                } catch (Exception ignored) {}
            }
        }
        return "ORD-" + (maxNum + 1);
    }
}

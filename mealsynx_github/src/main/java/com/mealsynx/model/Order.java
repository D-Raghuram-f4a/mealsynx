package com.mealsynx.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum OrderType implements Serializable { BUY_NOW, PRE_ORDER }
    public enum DeliveryMode implements Serializable { PICKUP, CAMPUS_DELIVERY }
    public enum OrderStatus implements Serializable {
        PLACED, PREPARING, PREPARED, OUT_FOR_DELIVERY, DELIVERED, COLLECTED, REJECTED
    }

    public static class OrderLineItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String foodId;
        private final String foodName;
        private final int quantity;
        private final double unitPrice;
        private final String specialRequirement;

        public OrderLineItem(String foodId, String foodName, int quantity, double unitPrice, String specialRequirement) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.specialRequirement = (specialRequirement == null || specialRequirement.isBlank())
                    ? "Standard Prep" : specialRequirement.trim();
        }

        public String getFoodId() { return foodId; }
        public String getFoodName() { return foodName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public String getSpecialRequirement() { return specialRequirement; }
        public double getLineTotal() { return quantity * unitPrice; }
    }

    private final String orderId;
    private final String customerId;
    private final String customerUsername;
    private final String canteenId;
    private final OrderType orderType;
    private final DeliveryMode deliveryMode;
    private final String deliveryPoint;
    private final String scheduledTime;
    private final String paymentMethod;
    private final boolean offlineCounterOrder;
    private final List<OrderLineItem> items = new ArrayList<>();
    private OrderStatus status;
    private final LocalDateTime createdAt;

    public Order(String orderId, String customerId, String customerUsername, String canteenId,
                 OrderType orderType, DeliveryMode deliveryMode, String deliveryPoint,
                 String scheduledTime, String paymentMethod, boolean offlineCounterOrder,
                 LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerUsername = customerUsername;
        this.canteenId = canteenId;
        this.orderType = orderType;
        this.deliveryMode = deliveryMode;
        this.deliveryPoint = (deliveryPoint == null || deliveryPoint.isBlank()) ? "Canteen Pickup Counter" : deliveryPoint;
        this.scheduledTime = (scheduledTime == null || scheduledTime.isBlank()) ? "Immediate" : scheduledTime;
        this.paymentMethod = paymentMethod;
        this.offlineCounterOrder = offlineCounterOrder;
        this.status = offlineCounterOrder ? OrderStatus.COLLECTED : OrderStatus.PLACED;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void addItem(String foodId, String foodName, int quantity, double unitPrice, String specialRequirement) {
        items.add(new OrderLineItem(foodId, foodName, quantity, unitPrice, specialRequirement));
    }

    public OrderStatus getNextForwardStatus() {
        return switch (status) {
            case PLACED -> OrderStatus.PREPARING;
            case PREPARING -> (deliveryMode == DeliveryMode.CAMPUS_DELIVERY)
                    ? OrderStatus.OUT_FOR_DELIVERY
                    : OrderStatus.PREPARED;
            case OUT_FOR_DELIVERY -> OrderStatus.DELIVERED;
            case PREPARED -> OrderStatus.COLLECTED;
            case DELIVERED, COLLECTED, REJECTED -> null;
        };
    }

    public boolean canAdvanceStatus() {
        return getNextForwardStatus() != null;
    }

    public String getDisplayStatusLabel() {
        return switch (status) {
            case PLACED -> "Order Placed";
            case PREPARING -> "Preparing";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case PREPARED -> "Prepared (Ready for Pickup)";
            case DELIVERED -> "Delivered (Completed)";
            case COLLECTED -> "Collected (Completed)";
            case REJECTED -> "Rejected";
        };
    }

    public static String formatStatusName(OrderStatus st) {
        if (st == null) return "Completed";
        return switch (st) {
            case PLACED -> "Order Placed";
            case PREPARING -> "Preparing";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case PREPARED -> "Prepared";
            case DELIVERED -> "Delivered";
            case COLLECTED -> "Collected";
            case REJECTED -> "Rejected";
        };
    }

    public String getPlusButtonLabel() {
        OrderStatus next = getNextForwardStatus();
        if (next == null) {
            return "✓ " + formatStatusName(status) + " (Locked)";
        }
        return "[ + ] Next: " + formatStatusName(next);
    }

    public double getTotalAmount() {
        return items.stream().mapToDouble(OrderLineItem::getLineTotal).sum();
    }

    public String getItemsSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderLineItem li = items.get(i);
            if (i > 0) sb.append(", ");
            sb.append(li.getQuantity()).append("x ").append(li.getFoodName());
            if (!"Standard Prep".equalsIgnoreCase(li.getSpecialRequirement())) {
                sb.append(" [").append(li.getSpecialRequirement()).append("]");
            }
        }
        return sb.toString();
    }

    public boolean isActiveInKitchen() {
        return status == OrderStatus.PLACED || status == OrderStatus.PREPARING
                || status == OrderStatus.PREPARED || status == OrderStatus.OUT_FOR_DELIVERY;
    }

    public String getFormattedTimestamp() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM hh:mm a"));
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerUsername() { return customerUsername; }
    public String getCanteenId() { return canteenId; }
    public OrderType getOrderType() { return orderType; }
    public DeliveryMode getDeliveryMode() { return deliveryMode; }
    public String getDeliveryPoint() { return deliveryPoint; }
    public String getScheduledTime() { return scheduledTime; }
    public String getPaymentMethod() { return paymentMethod; }
    public boolean isOfflineCounterOrder() { return offlineCounterOrder; }
    public List<OrderLineItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

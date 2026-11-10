package com.mealsynx.service;

import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.CustomerDAO;
import com.mealsynx.dao.MenuDAO;
import com.mealsynx.dao.OrderDAO;
import com.mealsynx.dao.PaymentDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Canteen;
import com.mealsynx.model.Customer;
import com.mealsynx.model.FoodItem;
import com.mealsynx.model.Order;
import com.mealsynx.model.WeatherSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserService {
    private final CustomerDAO customerDAO;
    private final MenuDAO menuDAO;
    private final OrderDAO orderDAO;
    private final CanteenDAO canteenDAO;
    private final PaymentDAO paymentDAO;
    private final NotificationService notificationService;

    public UserService(CustomerDAO customerDAO, MenuDAO menuDAO, OrderDAO orderDAO,
                       CanteenDAO canteenDAO, PaymentDAO paymentDAO, NotificationService notificationService) {
        this.customerDAO = customerDAO;
        this.menuDAO = menuDAO;
        this.orderDAO = orderDAO;
        this.canteenDAO = canteenDAO;
        this.paymentDAO = paymentDAO;
        this.notificationService = notificationService;
    }

    public List<Customer> getAllStudents() {
        return customerDAO.findAll();
    }

    public Optional<Customer> findStudent(String usernameOrId) {
        return customerDAO.findByUsernameOrIdentifier(usernameOrId);
    }

    public boolean toggleStudentFavourite(Customer student, String foodId) {
        boolean nowFav = student.toggleFavouriteFood(foodId);
        customerDAO.save(student);
        PersistentDataStore.getInstance().saveToDisk();
        return nowFav;
    }

    public List<String> getSmartFoodRecommendations(Customer student, WeatherSnapshot weather) {
        List<String> recommendations = new ArrayList<>();
        Set<String> favIds = student.getFavouriteFoodIds();
        List<Order> pastOrders = orderDAO.findByCustomer(student.getUsername());

        Set<String> preferredCategories = new LinkedHashSet<>();
        for (String fid : favIds) {
            menuDAO.findById(fid).ifPresent(f -> preferredCategories.add(f.getCategory()));
        }
        for (Order o : pastOrders) {
            for (Order.OrderLineItem li : o.getItems()) {
                menuDAO.findById(li.getFoodId()).ifPresent(f -> preferredCategories.add(f.getCategory()));
            }
        }

        for (FoodItem item : menuDAO.findAll()) {
            if (!item.isAvailable()) continue;
            Canteen c = canteenDAO.findById(item.getCanteenId()).orElse(null);
            String cName = c != null ? c.getName() : item.getCanteenId();
            int approxMins = c != null ? c.calculateApproxPrepTime(item.getBasePrepTimeMinutes()) : item.getBasePrepTimeMinutes();

            boolean matchesCategory = preferredCategories.contains(item.getCategory());
            boolean isFav = favIds.contains(item.getFoodId());
            if ((matchesCategory || isFav) && item.getCustomerRating() >= 4.6) {
                String reason = isFav ? "In your ❤️ Favourites" : "Matches your past " + item.getCategory() + " orders";
                recommendations.add(String.format("💡 %s (₹%.0f | ★%.2f | ~%d mins | %s) — %s",
                        item.getName(), item.getPrice(), item.getCustomerRating(), approxMins, cName, reason));
            }
        }

        if (weather != null) {
            if (weather.isSummerOrHot()) {
                recommendations.add(0, "☀ Live Weather Pick (" + weather.getTemperatureCelsius() + "°C Hot): Fresh Sweet Lime Juice (CAN-1), Mango Lassi (CAN-1) & Cold Belgian Frappe (CAN-3)");
            } else if (weather.isWinterOrRainy()) {
                recommendations.add(0, "🌧 Live Weather Pick (" + weather.getTemperatureCelsius() + "°C Cool/Rainy): Kumbakonam Filter Coffee (CAN-1), Hot Kozhi Soup (CAN-2) & Ginger Chai + Samosas (CAN-3)");
            }
        }

        return recommendations.stream().limit(7).collect(Collectors.toList());
    }

    public List<String> getTrendingFoodsAllCanteens() {
        return menuDAO.findAll().stream()
                .filter(FoodItem::isAvailable)
                .sorted(Comparator.comparingInt(FoodItem::getTotalOrdersCount)
                        .thenComparingDouble(FoodItem::getCustomerRating).reversed())
                .limit(8)
                .map(item -> {
                    Canteen c = canteenDAO.findById(item.getCanteenId()).orElse(null);
                    String cName = c != null ? c.getName() : item.getCanteenId();
                    int prep = c != null ? c.calculateApproxPrepTime(item.getBasePrepTimeMinutes()) : item.getBasePrepTimeMinutes();
                    return String.format("🔥 %s — ₹%.0f | ★%.2f (%d orders) | ~%d mins | %s",
                            item.getName(), item.getPrice(), item.getCustomerRating(),
                            item.getTotalOrdersCount(), prep, cName);
                })
                .collect(Collectors.toList());
    }

    public List<FoodItem> getRecentlyOrderedItems(Customer student) {
        Set<String> seenFoodIds = new LinkedHashSet<>();
        List<FoodItem> recentItems = new ArrayList<>();

        for (Order o : orderDAO.findByCustomer(student.getUsername())) {
            for (Order.OrderLineItem li : o.getItems()) {
                if (seenFoodIds.add(li.getFoodId())) {
                    menuDAO.findById(li.getFoodId()).ifPresent(recentItems::add);
                }
            }
        }
        for (String fid : student.getRecentlyOrderedFoodIds()) {
            if (seenFoodIds.add(fid)) {
                menuDAO.findById(fid).ifPresent(recentItems::add);
            }
        }
        return recentItems;
    }

    public List<FoodItem> getFavouriteItemsList(Customer student) {
        List<FoodItem> favs = new ArrayList<>();
        for (String fid : student.getFavouriteFoodIds()) {
            menuDAO.findById(fid).ifPresent(favs::add);
        }
        return favs;
    }

    public List<FoodItem> getFoodsOrderedByStudentForReview(Customer student) {
        return getRecentlyOrderedItems(student);
    }

    public Optional<Customer> addMoneyToStudentWallet(String studentUsername, double amountToAdd) {
        if (amountToAdd <= 0) return Optional.empty();
        Optional<Customer> studentOpt = customerDAO.findByUsernameOrIdentifier(studentUsername);
        if (studentOpt.isPresent()) {
            Customer student = studentOpt.get();
            double newBal = student.getWalletBalance() + amountToAdd;
            student.setWalletBalance(newBal);
            customerDAO.save(student);

            String txnId = "TOPUP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            paymentDAO.recordTransaction(txnId, student.getUsername(), "ADMIN-TOPUP", amountToAdd, "ADMIN_CASH_DEPOSIT");
            notificationService.notifyStudent(student,
                    String.format("Admin added ₹%.2f to your Food Wallet! New Balance: ₹%.2f", amountToAdd, newBal));
            PersistentDataStore.getInstance().saveToDisk();
            return Optional.of(student);
        }
        return Optional.empty();
    }
}

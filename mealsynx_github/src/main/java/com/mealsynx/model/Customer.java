package com.mealsynx.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Customer extends User {
    private static final long serialVersionUID = 1L;
    private double walletBalance;
    private String favouriteCategory;
    private String typicalOrderingTime;
    private double averageSpending;
    private final Set<String> favouriteFoodIds = new LinkedHashSet<>();
    private final Map<String, Integer> orderFrequencyMap = new LinkedHashMap<>();
    private final List<String> recentlyOrderedFoodIds = new ArrayList<>();
    private final Map<String, String> dayPreferenceMap = new HashMap<>();
    private final Map<String, String> companionDrinkMap = new HashMap<>();

    public Customer(String userId, String username, String fullName, String registerNo, String email,
                    String password, double walletBalance, String favouriteCategory,
                    String typicalOrderingTime, double averageSpending) {
        super(userId, username, fullName, registerNo, email, password, UserRole.STUDENT);
        this.walletBalance = walletBalance;
        this.favouriteCategory = favouriteCategory;
        this.typicalOrderingTime = typicalOrderingTime;
        this.averageSpending = averageSpending;
    }

    @Override
    public String getDashboardTitle() {
        return "MealSynx Student Portal — " + getUsername() + " (" + getFullName() + ")";
    }

    public boolean toggleFavouriteFood(String foodId) {
        if (favouriteFoodIds.contains(foodId)) {
            favouriteFoodIds.remove(foodId);
            return false;
        } else {
            favouriteFoodIds.add(foodId);
            return true;
        }
    }

    public void addFavouriteFood(String foodId) {
        favouriteFoodIds.add(foodId);
    }

    public boolean isFavourite(String foodId) {
        return favouriteFoodIds.contains(foodId);
    }

    public void recordFoodOrdered(String foodId, String foodName, int quantity) {
        orderFrequencyMap.put(foodName, orderFrequencyMap.getOrDefault(foodName, 0) + quantity);
        recentlyOrderedFoodIds.remove(foodId);
        recentlyOrderedFoodIds.add(0, foodId);
    }

    public void setDaySpecialtyPreference(String dayOfWeek, String foodName) {
        dayPreferenceMap.put(dayOfWeek.toUpperCase(), foodName);
    }

    public void setCompanionDrinkPairing(String mainCourse, String drink) {
        companionDrinkMap.put(mainCourse, drink);
    }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }
    public String getFavouriteCategory() { return favouriteCategory; }
    public void setFavouriteCategory(String favouriteCategory) { this.favouriteCategory = favouriteCategory; }
    public String getTypicalOrderingTime() { return typicalOrderingTime; }
    public String getTypicalOrderWindow() { return typicalOrderingTime; }
    public double getAverageSpending() { return averageSpending; }
    public void setAverageSpending(double averageSpending) { this.averageSpending = averageSpending; }
    public Set<String> getFavouriteFoodIds() { return favouriteFoodIds; }
    public Map<String, Integer> getOrderFrequencyMap() { return orderFrequencyMap; }
    public List<String> getRecentlyOrderedFoodIds() { return recentlyOrderedFoodIds; }
    public Map<String, String> getDayPreferenceMap() { return dayPreferenceMap; }
    public Map<String, String> getCompanionDrinkMap() { return companionDrinkMap; }
}

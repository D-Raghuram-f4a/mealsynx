package com.mealsynx.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class FoodItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String foodId;
    private String name;
    private String category;
    private double price;
    private double prepCost;
    private int basePrepTimeMinutes;
    private boolean available;
    private String nutritionalInfo;
    private String canteenId;
    private int historicalDailyDemand;
    private int totalOrdersCount;
    private double wasteRatePercent;
    private int ingredientDifficulty;
    private double customerRating;
    private int ratingCount;
    private int customKitchenPlanTarget;
    private final Map<String, Double> recipeIngredientsPerPlate = new LinkedHashMap<>();

    public FoodItem(String foodId, String name, String category, double price, double prepCost,
                    int basePrepTimeMinutes, boolean available, String nutritionalInfo,
                    String canteenId, int historicalDailyDemand, int totalOrdersCount,
                    double wasteRatePercent, int ingredientDifficulty,
                    double customerRating, int ratingCount) {
        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.prepCost = prepCost;
        this.basePrepTimeMinutes = basePrepTimeMinutes;
        this.available = available;
        this.nutritionalInfo = nutritionalInfo;
        this.canteenId = canteenId;
        this.historicalDailyDemand = historicalDailyDemand;
        this.totalOrdersCount = totalOrdersCount;
        this.wasteRatePercent = wasteRatePercent;
        this.ingredientDifficulty = ingredientDifficulty;
        this.customerRating = customerRating;
        this.ratingCount = Math.max(1, ratingCount);
        this.customKitchenPlanTarget = 0;
    }

    public void addIngredientRequirement(String ingredientId, double qtyPerPlate) {
        recipeIngredientsPerPlate.put(ingredientId, qtyPerPlate);
    }

    public synchronized void recordNewUserRating(int stars) {
        double totalStars = (this.customerRating * this.ratingCount) + stars;
        this.ratingCount++;
        this.customerRating = Math.round((totalStars / this.ratingCount) * 100.0) / 100.0;
    }

    public void incrementOrderCount(int qty) {
        this.totalOrdersCount += qty;
    }

    public double getProfitMarginPercent() {
        return price > 0 ? ((price - prepCost) / price) * 100.0 : 0.0;
    }

    public int calculatePerformanceScore() {
        double demandScore = Math.min(30.0, ((historicalDailyDemand + totalOrdersCount) / 260.0) * 30.0);
        double profitScore = Math.min(25.0, (getProfitMarginPercent() / 58.0) * 25.0);
        double ratingScore = (customerRating / 5.0) * 25.0;
        double revenueScore = Math.min(20.0, (((historicalDailyDemand + totalOrdersCount) * price) / 26000.0) * 20.0);
        double wastePenalty = Math.min(20.0, wasteRatePercent * 1.2);
        double difficultyPenalty = ingredientDifficulty * 2.2;

        int score = (int) Math.round(demandScore + profitScore + ratingScore + revenueScore - wastePenalty - difficultyPenalty + 8);
        return Math.max(35, Math.min(99, score));
    }

    public int getSystemEstimatedKitchenPrep(double weatherMultiplier, double eventMultiplier) {
        double base = historicalDailyDemand * 0.85 + (totalOrdersCount * 1.4);
        double adjusted = base * weatherMultiplier * eventMultiplier;
        if (wasteRatePercent > 10.0) {
            adjusted *= 0.90;
        } else if (customerRating >= 4.6) {
            adjusted *= 1.06;
        }
        return Math.max(25, (int) Math.round(adjusted));
    }

    public String getFoodId() { return foodId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getPrepCost() { return prepCost; }
    public int getBasePrepTimeMinutes() { return basePrepTimeMinutes; }
    public void setBasePrepTimeMinutes(int basePrepTimeMinutes) { this.basePrepTimeMinutes = basePrepTimeMinutes; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getNutritionalInfo() { return nutritionalInfo; }
    public void setNutritionalInfo(String nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }
    public String getCanteenId() { return canteenId; }
    public int getHistoricalDailyDemand() { return historicalDailyDemand; }
    public int getTotalOrdersCount() { return totalOrdersCount; }
    public double getWasteRatePercent() { return wasteRatePercent; }
    public int getIngredientDifficulty() { return ingredientDifficulty; }
    public double getCustomerRating() { return customerRating; }
    public int getRatingCount() { return ratingCount; }
    public int getCustomKitchenPlanTarget() { return customKitchenPlanTarget; }
    public void setCustomKitchenPlanTarget(int customKitchenPlanTarget) { this.customKitchenPlanTarget = customKitchenPlanTarget; }
    public Map<String, Double> getRecipeIngredientsPerPlate() { return recipeIngredientsPerPlate; }
}

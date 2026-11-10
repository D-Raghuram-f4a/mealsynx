package com.mealsynx.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class IngredientStock implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String ingredientId;
    private final String name;
    private final String category;
    private final String canteenId;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private double currentStock;
    private final double safetyStock;
    private double nextDayRequiredQty;
    private final String unit;
    private final double costPerUnit;

    public IngredientStock(String ingredientId, String name, String category, String canteenId,
                           LocalDate purchaseDate, LocalDate expiryDate,
                           double currentStock, double safetyStock, double nextDayRequiredQty,
                           String unit, double costPerUnit) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.category = category;
        this.canteenId = canteenId;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.currentStock = currentStock;
        this.safetyStock = safetyStock;
        this.nextDayRequiredQty = nextDayRequiredQty;
        this.unit = unit;
        this.costPerUnit = costPerUnit;
    }

    public long getDaysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public boolean isLowStock() {
        return currentStock <= safetyStock || currentStock < nextDayRequiredQty * 0.70;
    }

    public boolean isQualityOrExpiryLow() {
        return getDaysUntilExpiry() <= 3;
    }

    public String getQualityStatusLabel() {
        long days = getDaysUntilExpiry();
        if (days < 0) return "CRITICAL: EXPIRED";
        if (days <= 3) return "WARNING: Expiring in " + days + "d (Quality Low)";
        if (isLowStock()) return "LOW STOCK ALERT";
        return "Fresh / Optimal (" + days + "d left)";
    }

    public double getRecommendedPurchaseQty() {
        double needed = (nextDayRequiredQty + safetyStock) - currentStock;
        return Math.max(0.0, Math.round(needed * 10.0) / 10.0);
    }

    public String getIngredientId() { return ingredientId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getCanteenId() { return canteenId; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public double getCurrentStock() { return currentStock; }
    public void setCurrentStock(double currentStock) { this.currentStock = Math.max(0.0, currentStock); }
    public double getSafetyStock() { return safetyStock; }
    public double getNextDayRequiredQty() { return nextDayRequiredQty; }
    public void setNextDayRequiredQty(double nextDayRequiredQty) { this.nextDayRequiredQty = nextDayRequiredQty; }
    public String getUnit() { return unit; }
    public double getCostPerUnit() { return costPerUnit; }
}

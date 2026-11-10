package com.mealsynx.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Canteen implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String canteenId;
    private final String name;
    private final String specialtyType;
    private final String location;
    private final int maxCapacity;
    private int activeOrderCount;
    private double totalSalesRevenue;
    private double ingredientPurchaseExpenses;
    private double workerWagesExpenses;
    private double utilitiesExpenses;
    private final List<String> adminUpdatesFeed = new ArrayList<>();

    public Canteen(String canteenId, String name, String specialtyType, String location,
                   int maxCapacity, int activeOrderCount,
                   double totalSalesRevenue, double ingredientPurchaseExpenses,
                   double workerWagesExpenses, double utilitiesExpenses) {
        this.canteenId = canteenId;
        this.name = name;
        this.specialtyType = specialtyType;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.activeOrderCount = activeOrderCount;
        this.totalSalesRevenue = totalSalesRevenue;
        this.ingredientPurchaseExpenses = ingredientPurchaseExpenses;
        this.workerWagesExpenses = workerWagesExpenses;
        this.utilitiesExpenses = utilitiesExpenses;
    }

    public String getBusyStatus() {
        if (activeOrderCount <= 2) return "Free";
        if (activeOrderCount <= 5) return "Nominal";
        return "Busy";
    }

    public double getPrepTimeMultiplier() {
        String status = getBusyStatus();
        if ("Free".equals(status)) return 1.0;
        if ("Nominal".equals(status)) return 1.30;
        return 1.65;
    }

    public int calculateApproxPrepTime(int basePrepMins) {
        int extraQueueMins = Math.max(0, (activeOrderCount - 1) * 2);
        return (int) Math.round(basePrepMins * getPrepTimeMultiplier()) + extraQueueMins;
    }

    public double getTotalExpenses() {
        return ingredientPurchaseExpenses + workerWagesExpenses + utilitiesExpenses;
    }

    public double getNetProfitOrLoss() {
        return totalSalesRevenue - getTotalExpenses();
    }

    public boolean isRunningInLoss() {
        return getNetProfitOrLoss() < 0;
    }

    public void addSalesRevenue(double amount) { this.totalSalesRevenue += amount; }
    public void addIngredientPurchaseExpense(double amount) { this.ingredientPurchaseExpenses += amount; }
    public void addWorkerWagesExpense(double amount) { this.workerWagesExpenses += amount; }
    public void addUtilitiesExpense(double amount) { this.utilitiesExpenses += amount; }
    public void addAdminUpdate(String alertMessage) { this.adminUpdatesFeed.add(0, alertMessage); }

    public String getCanteenId() { return canteenId; }
    public String getName() { return name; }
    public String getSpecialtyType() { return specialtyType; }
    public String getLocation() { return location; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getActiveOrderCount() { return activeOrderCount; }
    public void setActiveOrderCount(int activeOrderCount) { this.activeOrderCount = Math.max(0, activeOrderCount); }
    public double getTotalSalesRevenue() { return totalSalesRevenue; }
    public double getIngredientPurchaseExpenses() { return ingredientPurchaseExpenses; }
    public double getWorkerWagesExpenses() { return workerWagesExpenses; }
    public double getUtilitiesExpenses() { return utilitiesExpenses; }
    public List<String> getAdminUpdatesFeed() { return adminUpdatesFeed; }
}

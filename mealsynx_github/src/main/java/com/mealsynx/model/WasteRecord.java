package com.mealsynx.model;

import java.io.Serializable;
import java.time.LocalDate;

public class WasteRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum WasteReason implements Serializable {
        OVERPRODUCTION, LOW_DEMAND, SPOILAGE, PREPARATION_ERROR,
        DAMAGED_FOOD, EXPIRED_INGREDIENTS, SERVING_LEFTOVERS, OTHER
    }

    private final String wasteId;
    private final String canteenId;
    private final String foodName;
    private final LocalDate date;
    private final int predictedQty;
    private final int preparedQty;
    private final int soldQty;
    private final int wastedQty;
    private final double wasteWeightKg;
    private final WasteReason reason;

    public WasteRecord(String wasteId, String canteenId, String foodName, LocalDate date,
                       int predictedQty, int preparedQty, int soldQty, int wastedQty,
                       double wasteWeightKg, WasteReason reason) {
        this.wasteId = wasteId;
        this.canteenId = canteenId;
        this.foodName = foodName;
        this.date = date;
        this.predictedQty = predictedQty;
        this.preparedQty = preparedQty;
        this.soldQty = soldQty;
        this.wastedQty = wastedQty;
        this.wasteWeightKg = wasteWeightKg;
        this.reason = reason;
    }

    public String getWasteId() { return wasteId; }
    public String getCanteenId() { return canteenId; }
    public String getFoodName() { return foodName; }
    public LocalDate getDate() { return date; }
    public int getPredictedQty() { return predictedQty; }
    public int getPreparedQty() { return preparedQty; }
    public int getSoldQty() { return soldQty; }
    public int getWastedQty() { return wastedQty; }
    public double getWasteWeightKg() { return wasteWeightKg; }
    public WasteReason getReason() { return reason; }
}

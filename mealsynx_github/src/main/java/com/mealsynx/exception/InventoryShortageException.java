package com.mealsynx.exception;

public class InventoryShortageException extends MealSynxException {
    public InventoryShortageException(String ingredientName, double required, double available, String unit) {
        super("ERR_INV_SHORTAGE", String.format(
            "Inventory shortage for %s: required %.2f %s, available %.2f %s",
            ingredientName, required, unit, available, unit));
    }
}

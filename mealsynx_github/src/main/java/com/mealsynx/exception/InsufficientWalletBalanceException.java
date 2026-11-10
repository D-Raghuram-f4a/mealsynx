package com.mealsynx.exception;

public class InsufficientWalletBalanceException extends MealSynxException {
    private final double currentBalance;
    private final double requiredAmount;

    public InsufficientWalletBalanceException(double currentBalance, double requiredAmount) {
        super("ERR_WALLET_LOW", String.format(
            "Insufficient Food Wallet balance! Available: ₹%.2f | Required: ₹%.2f",
            currentBalance, requiredAmount));
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public double getCurrentBalance() { return currentBalance; }
    public double getRequiredAmount() { return requiredAmount; }
}

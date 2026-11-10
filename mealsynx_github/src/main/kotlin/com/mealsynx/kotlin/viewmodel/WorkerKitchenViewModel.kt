package com.mealsynx.kotlin.viewmodel

class WorkerKitchenViewModel(private val canteenId: String) {

    data class KitchenPrepCard(
        val itemName: String,
        val predictedCount: Int,
        val recommendedPrepCount: Int,
        val activePreOrders: Int,
        val statusBadge: String
    )

    data class LowStockAlertBanner(
        val ingredientName: String,
        val currentKg: Double,
        val tomorrowRequiredKg: Double,
        val recommendedPurchaseKg: Double,
        val isExpiringSoon: Boolean
    )

    fun getTodayKitchenPlan(): List<KitchenPrepCard> = listOf(
        KitchenPrepCard("Biryani", predictedCount = 300, recommendedPrepCount = 285, activePreOrders = 42, statusBadge = "OPTIMIZED (-5% Waste Trim)"),
        KitchenPrepCard("Meals", predictedCount = 250, recommendedPrepCount = 260, activePreOrders = 35, statusBadge = "HIGH SCORE (94)"),
        KitchenPrepCard("Juice", predictedCount = 180, recommendedPrepCount = 200, activePreOrders = 28, statusBadge = "WEATHER BOOST (35°C)")
    )

    fun getWarehouseLowStockAlerts(): List<LowStockAlertBanner> = listOf(
        LowStockAlertBanner("Rice", currentKg = 20.0, tomorrowRequiredKg = 32.0, recommendedPurchaseKg = 17.0, isExpiringSoon = false),
        LowStockAlertBanner("Chicken", currentKg = 38.0, tomorrowRequiredKg = 35.0, recommendedPurchaseKg = 2.0, isExpiringSoon = true)
    )
}

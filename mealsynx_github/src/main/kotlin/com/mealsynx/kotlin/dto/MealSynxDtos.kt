package com.mealsynx.kotlin.dto

data class CustomerProfileDto(
    val userId: String,
    val fullName: String,
    val registerNo: String,
    val walletBalance: Double,
    val favouriteCategory: String,
    val typicalOrderingWindow: String,
    val averageSpending: Double,
    val frequentlyOrdered: List<String>,
    val favourites: List<String>
)

data class FoodCardDto(
    val foodId: String,
    val name: String,
    val category: String,
    val price: Double,
    val prepTimeMins: Int,
    val performanceScore: Int,
    val available: Boolean,
    val canteenId: String,
    val tagBadge: String? = null
)

data class PreOrderRequestDto(
    val customerId: String,
    val canteenId: String,
    val items: Map<String, Int>,
    val collectionTimeWindow: String,
    val deliveryMode: String,
    val campusDropLocation: String?,
    val paymentMethod: String
)

data class WhatIfScenarioRequest(
    val scenarioType: String,
    val additionalStudents: Int = 800,
    val attendancePercent: Int = 85,
    val durationHours: Int = 3,
    val oldPrice: Double = 120.0,
    val newPrice: Double = 140.0
)

sealed class OrderTrackingUiState {
    object Idle : OrderTrackingUiState()
    data class Accepted(val orderId: String, val estimatedMins: Int) : OrderTrackingUiState()
    data class PreparingInKitchen(val orderId: String, val chefNote: String) : OrderTrackingUiState()
    data class OutForCampusDelivery(val orderId: String, val destinationBlock: String) : OrderTrackingUiState()
    data class ReadyOrDelivered(val orderId: String, val completionTimestamp: String) : OrderTrackingUiState()
    data class Error(val errorCode: String, val reason: String) : OrderTrackingUiState()
}

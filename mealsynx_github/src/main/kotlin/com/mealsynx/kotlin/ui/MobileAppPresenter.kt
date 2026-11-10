package com.mealsynx.kotlin.ui

import com.mealsynx.kotlin.dto.CustomerProfileDto
import com.mealsynx.kotlin.dto.FoodCardDto
import com.mealsynx.kotlin.dto.OrderTrackingUiState
import com.mealsynx.kotlin.dto.WhatIfScenarioRequest

class MobileAppPresenter {

    fun renderStudentHomeScreen(profile: CustomerProfileDto, isFriday: Boolean): String {
        val sb = StringBuilder()
        sb.appendLine("┌──────────────────────────────────────────────────────────┐")
        sb.appendLine("│ 📱 MEALSYNX STUDENT APP — Hi, ${profile.fullName.padEnd(26)} │")
        sb.appendLine("│ 💳 Food Wallet: ₹${String.format("%.2f", profile.walletBalance).padEnd(10)} | 🕒 Peak: ${profile.typicalOrderingWindow.padEnd(16)}│")
        sb.appendLine("├──────────────────────────────────────────────────────────┤")
        sb.appendLine("│ ⭐ Order Again         : ${profile.frequentlyOrdered.firstOrNull() ?: "Chicken Biryani"}")
        sb.appendLine("│ ❤️ Your Favourite      : ${profile.favourites.firstOrNull() ?: "Lime Juice"}")
        sb.appendLine("│ 🔥 Popular Today       : Paneer Roll")
        sb.appendLine("│ 💡 Recommended For You : Chicken Noodles")
        if (isFriday) {
            sb.appendLine("│ 🎯 Friday Special      : Chicken Biryani + Lime Juice Combo")
        }
        sb.appendLine("└──────────────────────────────────────────────────────────┘")
        return sb.toString()
    }

    fun renderOrderTrackingBanner(state: OrderTrackingUiState): String = when (state) {
        is OrderTrackingUiState.Idle -> "No active orders."
        is OrderTrackingUiState.Accepted -> "🔔 Order ${state.orderId} accepted! Est. prep: ${state.estimatedMins} mins."
        is OrderTrackingUiState.PreparingInKitchen -> "👨‍🍳 Order ${state.orderId} is being prepared (${state.chefNote})."
        is OrderTrackingUiState.OutForCampusDelivery -> "🛵 Order ${state.orderId} is out for campus delivery to ${state.destinationBlock}."
        is OrderTrackingUiState.ReadyOrDelivered -> "✅ Order ${state.orderId} completed at ${state.completionTimestamp}."
        is OrderTrackingUiState.Error -> "⚠ Order Error [${state.errorCode}]: ${state.reason}"
    }

    fun formatWhatIfSimulationPreview(req: WhatIfScenarioRequest): String {
        return "Scenario [${req.scenarioType}] -> +${req.additionalStudents} students @ ${req.attendancePercent}% attendance (${req.durationHours}h)"
    }
}

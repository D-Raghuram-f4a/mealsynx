package com.mealsynx.kotlin.payment

import java.util.UUID

class QrPaymentManager {

    data class QrPaymentSession(
        val sessionId: String,
        val orderId: String,
        val customerId: String,
        val amountInr: Double,
        val upiQrPayload: String,
        val status: String
    )

    fun createDynamicQrSession(orderId: String, customerId: String, amountInr: Double, canteenId: String): QrPaymentSession {
        val sessionId = "QR-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        val upiString = "upi://pay?pa=mealsynx.${canteenId.lowercase()}@campusbank&pn=MealSynx%20Campus&tr=$orderId&am=${String.format("%.2f", amountInr)}&cu=INR"
        return QrPaymentSession(
            sessionId = sessionId,
            orderId = orderId,
            customerId = customerId,
            amountInr = amountInr,
            upiQrPayload = upiString,
            status = "AWAITING_SCAN"
        )
    }

    fun calculateWalletBalanceAfterOrder(currentBalance: Double, orderAmount: Double): Result<Double> {
        return if (currentBalance >= orderAmount) {
            Result.success(currentBalance - orderAmount)
        } else {
            Result.failure(IllegalStateException("Insufficient Food Wallet balance: ₹$currentBalance available, ₹$orderAmount needed"))
        }
    }
}

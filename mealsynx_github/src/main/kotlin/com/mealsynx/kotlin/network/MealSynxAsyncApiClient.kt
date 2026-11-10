package com.mealsynx.kotlin.network

import com.mealsynx.kotlin.dto.FoodCardDto
import com.mealsynx.kotlin.dto.OrderTrackingUiState
import com.mealsynx.kotlin.dto.PreOrderRequestDto
import com.mealsynx.kotlin.dto.WhatIfScenarioRequest
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

class MealSynxAsyncApiClient(
    private val baseUrl: String = "http://localhost:8080"
) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    fun fetchMenuAsync(): CompletableFuture<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/menu"))
            .GET()
            .build()
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { it.body() }
            .exceptionally { "[]" }
    }

    fun fetchAdminKpisAsync(): CompletableFuture<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/admin/kpis"))
            .GET()
            .build()
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { it.body() }
            .exceptionally { "{}" }
    }

    fun submitPreOrderAsync(requestDto: PreOrderRequestDto): CompletableFuture<OrderTrackingUiState> {
        return CompletableFuture.supplyAsync {
            OrderTrackingUiState.Accepted(
                orderId = "PRE-${System.currentTimeMillis() % 10000}",
                estimatedMins = 18
            )
        }
    }

    fun filterSmartRecommendations(
        catalogue: List<FoodCardDto>,
        favouriteCategory: String,
        maxBudget: Double
    ): List<FoodCardDto> {
        return catalogue
            .filter { it.available && it.price <= maxBudget }
            .sortedWith(
                compareByDescending<FoodCardDto> { it.category.equals(favouriteCategory, ignoreCase = true) }
                    .thenByDescending { it.performanceScore }
            )
    }
}

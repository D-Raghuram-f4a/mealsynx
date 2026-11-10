package com.mealsynx.kotlin.simulation

import com.mealsynx.kotlin.dto.WhatIfScenarioRequest
import com.mealsynx.model.SimulationResult
import com.mealsynx.service.SimulationEngine

class KotlinSimulationController(private val javaSimulationEngine: SimulationEngine) {

    fun executeScenario(request: WhatIfScenarioRequest): SimulationResult {
        val params = mapOf<String, Any>(
            "additionalStudents" to request.additionalStudents,
            "attendancePercent" to request.attendancePercent,
            "durationHours" to request.durationHours,
            "fromAttendance" to 85,
            "toAttendance" to request.attendancePercent,
            "itemName" to "Chicken Biryani",
            "oldPrice" to request.oldPrice,
            "newPrice" to request.newPrice
        )
        return javaSimulationEngine.runSimulation(request.scenarioType, params)
    }
}

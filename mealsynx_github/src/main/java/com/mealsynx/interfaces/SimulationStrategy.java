package com.mealsynx.interfaces;

import com.mealsynx.model.SimulationResult;
import java.util.Map;

public interface SimulationStrategy {
    String getScenarioName();
    SimulationResult simulate(Map<String, Object> parameters);
}

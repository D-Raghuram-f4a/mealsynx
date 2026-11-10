package com.mealsynx.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimulationResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String scenarioTitle;
    private final String summaryDescription;
    private final Map<String, String> categoryDemandDelta = new LinkedHashMap<>();
    private final Map<String, String> ingredientRiskStatus = new LinkedHashMap<>();
    private final Map<String, Integer> preparationAdjustments = new LinkedHashMap<>();
    private final List<String> strategicRecommendations = new ArrayList<>();
    private double projectedRevenueDeltaPercent;
    private double projectedWasteDeltaPercent;

    public SimulationResult(String scenarioTitle, String summaryDescription) {
        this.scenarioTitle = scenarioTitle;
        this.summaryDescription = summaryDescription;
    }

    public void addCategoryDelta(String category, String deltaText) { categoryDemandDelta.put(category, deltaText); }
    public void addIngredientRisk(String ingredient, String riskStatus) { ingredientRiskStatus.put(ingredient, riskStatus); }
    public void addPrepAdjustment(String item, int deltaPortions) { preparationAdjustments.put(item, deltaPortions); }
    public void addRecommendation(String recommendation) { strategicRecommendations.add(recommendation); }

    public String getScenarioTitle() { return scenarioTitle; }
    public String getSummaryDescription() { return summaryDescription; }
    public Map<String, String> getCategoryDemandDelta() { return categoryDemandDelta; }
    public Map<String, String> getIngredientRiskStatus() { return ingredientRiskStatus; }
    public Map<String, Integer> getPreparationAdjustments() { return preparationAdjustments; }
    public List<String> getStrategicRecommendations() { return strategicRecommendations; }
    public double getProjectedRevenueDeltaPercent() { return projectedRevenueDeltaPercent; }
    public void setProjectedRevenueDeltaPercent(double val) { this.projectedRevenueDeltaPercent = val; }
    public double getProjectedWasteDeltaPercent() { return projectedWasteDeltaPercent; }
    public void setProjectedWasteDeltaPercent(double val) { this.projectedWasteDeltaPercent = val; }
}

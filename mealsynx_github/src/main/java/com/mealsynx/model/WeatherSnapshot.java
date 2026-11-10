package com.mealsynx.model;

import java.io.Serializable;

public class WeatherSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String seasonMode;
    private final double temperatureCelsius;
    private final boolean raining;
    private final int humidityPercent;
    private final String conditionLabel;

    public WeatherSnapshot(String seasonMode, double temperatureCelsius, boolean raining,
                           int humidityPercent, String conditionLabel) {
        this.seasonMode = seasonMode;
        this.temperatureCelsius = temperatureCelsius;
        this.raining = raining;
        this.humidityPercent = humidityPercent;
        this.conditionLabel = conditionLabel;
    }

    public boolean isSummerOrHot() {
        return temperatureCelsius >= 32.5 || "SUMMER_HOT".equalsIgnoreCase(seasonMode);
    }

    public boolean isWinterOrRainy() {
        return temperatureCelsius <= 25.0 || raining || "WINTER_COOL".equalsIgnoreCase(seasonMode) || "MONSOON_RAINY".equalsIgnoreCase(seasonMode);
    }

    public double getCategoryDemandMultiplier(String category) {
        String cat = category.toLowerCase();
        if (isSummerOrHot()) {
            if (cat.contains("beverage") || cat.contains("drink") || cat.contains("juice") || cat.contains("cold") || cat.contains("dessert")) {
                return 1.42;
            }
            if (cat.contains("hot") || cat.contains("soup")) {
                return 0.88;
            }
        } else if (isWinterOrRainy()) {
            if (cat.contains("hot") || cat.contains("tea") || cat.contains("coffee") || cat.contains("snack") || cat.contains("grill") || cat.contains("fast food") || cat.contains("starter")) {
                return 1.38;
            }
            if (cat.contains("cold") || cat.contains("juice") || cat.contains("ice")) {
                return 0.72;
            }
        }
        return 1.05;
    }

    public String getWeatherAnalyticsRecommendation() {
        if (isSummerOrHot()) {
            return String.format("☀ LIVE WEATHER ANALYTICS (%.1f°C | Humidity %d%%): Cold Drinks, Fresh Juices, Lassi & Frappes demand surges +42%%. Boost chiller inventory across Canteens 1, 2 & 3.", temperatureCelsius, humidityPercent);
        } else if (isWinterOrRainy()) {
            return String.format("🌧 LIVE WEATHER ANALYTICS (%.1f°C | Humidity %d%%): Hot Filter Coffee, Ginger Masala Chai, Samosas & Hot Grills demand surges +38%%.", temperatureCelsius, humidityPercent);
        }
        return String.format("⛅ LIVE WEATHER ANALYTICS (%.1f°C | Humidity %d%%): Balanced demand across Veg Meals, Biryani, Grills & Beverages.", temperatureCelsius, humidityPercent);
    }

    public String getSeasonMode() { return seasonMode; }
    public double getTemperatureCelsius() { return temperatureCelsius; }
    public boolean isRaining() { return raining; }
    public int getHumidityPercent() { return humidityPercent; }
    public String getConditionLabel() { return conditionLabel; }
}

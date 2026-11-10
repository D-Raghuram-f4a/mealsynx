package com.mealsynx.service;

import com.mealsynx.interfaces.SimulationStrategy;
import com.mealsynx.model.SimulationResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimulationEngine {

    public static class EventSurgeStrategy implements SimulationStrategy {
        @Override
        public String getScenarioName() { return "EVENT_SURGE"; }

        @Override
        public SimulationResult simulate(Map<String, Object> params) {
            String targetCanteen = (String) params.getOrDefault("canteenId", "ALL");
            int extraStudents = ((Number) params.getOrDefault("extraStudents", 750)).intValue();
            int attendancePct = ((Number) params.getOrDefault("attendancePct", 88)).intValue();
            int durationHrs = ((Number) params.getOrDefault("durationHrs", 4)).intValue();

            double factor = (extraStudents / 700.0) * (attendancePct / 85.0);
            int vegSurge = (int) Math.round(22 * factor);
            int nonVegSurge = (int) Math.round(29 * factor);
            int grillsDrinksSurge = (int) Math.round(37 * factor);

            SimulationResult res = new SimulationResult(
                    "Event & Crowd Surge Simulation [" + targetCanteen + "]",
                    String.format("+%d Additional Students | %d%% Campus Attendance | %d hrs Event Window",
                            extraStudents, attendancePct, durationHrs)
            );
            res.addCategoryDelta("Canteen 1 (Pure Veg Meals & Tiffin)", "+" + vegSurge + "% demand surge");
            res.addCategoryDelta("Canteen 2 (Non-Veg Biryani & Starters)", "+" + nonVegSurge + "% demand surge");
            res.addCategoryDelta("Canteen 3 (Fast Food, Grills & Cold Drinks)", "+" + grillsDrinksSurge + "% demand surge");

            res.addIngredientRisk("Seeraga Samba Rice & Sona Masoori", "Medium Risk (Buffer +18 kg)");
            res.addIngredientRisk("Fresh Broiler Chicken & Grill Strips", "⚠ HIGH SHORTAGE RISK (+24 kg needed)");
            res.addIngredientRisk("Citrus Fruit & Cold Frappe Base", "⚠ SHORTAGE RISK (+20 L needed)");

            res.addPrepAdjustment("Executive Veg Meals (CAN-1)", (int) Math.round(110 * factor));
            res.addPrepAdjustment("Ambur Chicken Dum Biryani (CAN-2)", (int) Math.round(145 * factor));
            res.addPrepAdjustment("Smoky BBQ Grill Wrap & Frappe (CAN-3)", (int) Math.round(160 * factor));

            res.addRecommendation("Alert Canteen 2 & Canteen 3 to pre-marinate +25 kg poultry batch before 10:30 AM.");
            res.addRecommendation("Redirect walk-in beverage & snack orders to Canteen 3 (currently Free) to relieve Canteen 2 queue.");
            res.setProjectedRevenueDeltaPercent(Math.round(26.4 * factor * 10.0) / 10.0);
            res.setProjectedWasteDeltaPercent(-12.5);
            return res;
        }
    }

    public static class AttendanceDropStrategy implements SimulationStrategy {
        @Override
        public String getScenarioName() { return "ATTENDANCE_DROP"; }

        @Override
        public SimulationResult simulate(Map<String, Object> params) {
            int fromPct = ((Number) params.getOrDefault("fromAttendance", 85)).intValue();
            int toPct = ((Number) params.getOrDefault("toAttendance", 60)).intValue();
            double dropRatio = Math.max(0.05, (fromPct - toPct) / (double) Math.max(1, fromPct));
            int dropPct = (int) Math.round(dropRatio * 100);

            SimulationResult res = new SimulationResult(
                    "Attendance Drop / Exam / Holiday Simulation",
                    String.format("Campus Attendance drops from %d%% → %d%% (-%d%% footfall)", fromPct, toPct, dropPct)
            );
            res.addCategoryDelta("Overall Campus Meal Demand", "-" + dropPct + "% ↓");
            res.addCategoryDelta("Recommended Kitchen Batch Reduction", "-" + (dropPct + 3) + "% ↓");
            res.addCategoryDelta("Prevented Overproduction Waste", String.format("%.1f kg food saved ↓", dropRatio * 95.0));

            res.addIngredientRisk("Perishable Batter & Paneer (CAN-1)", "Hold 30% in chiller for next day");
            res.addIngredientRisk("Fresh Poultry & Fish (CAN-2)", "Trim morning procurement by " + dropPct + "%");

            res.addPrepAdjustment("Executive Veg Meals (CAN-1)", -(int) Math.round(260 * dropRatio));
            res.addPrepAdjustment("Ambur Chicken Dum Biryani (CAN-2)", -(int) Math.round(310 * dropRatio));
            res.addPrepAdjustment("Fast Food & Grills (CAN-3)", -(int) Math.round(220 * dropRatio));

            res.addRecommendation("Switch Canteen 1, 2 & 3 to cooked-to-order batching after 1:15 PM.");
            res.addRecommendation("Alert all canteen workers to freeze extra batter & grill marinades immediately.");
            res.setProjectedRevenueDeltaPercent(-dropPct);
            res.setProjectedWasteDeltaPercent(-42.0);
            return res;
        }
    }

    public static class PriceElasticityStrategy implements SimulationStrategy {
        @Override
        public String getScenarioName() { return "PRICE_ELASTICITY"; }

        @Override
        public SimulationResult simulate(Map<String, Object> params) {
            String dishName = (String) params.getOrDefault("dishName", "Ambur Chicken Dum Biryani");
            double oldPrice = ((Number) params.getOrDefault("oldPrice", 130.0)).doubleValue();
            double newPrice = ((Number) params.getOrDefault("newPrice", 150.0)).doubleValue();

            double priceChangePct = ((newPrice - oldPrice) / Math.max(1.0, oldPrice)) * 100.0;
            double demandChangePct = -0.58 * priceChangePct;
            int baseQty = 315;
            int newQty = (int) Math.round(baseQty * (1.0 + demandChangePct / 100.0));
            double oldRev = baseQty * oldPrice;
            double newRev = newQty * newPrice;
            double revDeltaPct = ((newRev - oldRev) / oldRev) * 100.0;

            SimulationResult res = new SimulationResult(
                    "Menu Price Elasticity Simulation — " + dishName,
                    String.format("Price changed from ₹%.0f → ₹%.0f (%+.1f%%)", oldPrice, newPrice, priceChangePct)
            );
            res.addCategoryDelta("Projected Daily Plates Sold", String.format("%d → %d plates (%+.1f%%)", baseQty, newQty, demandChangePct));
            res.addCategoryDelta("Projected Daily Dish Revenue", String.format("₹%.0f → ₹%.0f (%+.1f%%)", oldRev, newRev, revDeltaPct));
            res.addCategoryDelta("Unit Profit Margin Impact", priceChangePct >= 0 ? "Margin Increases ↑ (Watch volume dip)" : "Volume Increases ↑ (Student Value Boost)");

            res.addPrepAdjustment(dishName, newQty - baseQty);
            res.addRecommendation(String.format("Adjust %s daily kitchen plan by %+d plates to avoid overproduction waste.", dishName, newQty - baseQty));
            res.addRecommendation("Bundle with a ₹30 beverage combo to preserve student order frequency.");
            res.setProjectedRevenueDeltaPercent(Math.round(revDeltaPct * 10.0) / 10.0);
            res.setProjectedWasteDeltaPercent(-9.0);
            return res;
        }
    }

    private final Map<String, SimulationStrategy> strategies = new LinkedHashMap<>();

    public SimulationEngine() {
        register(new EventSurgeStrategy());
        register(new AttendanceDropStrategy());
        register(new PriceElasticityStrategy());
    }

    private void register(SimulationStrategy s) {
        strategies.put(s.getScenarioName(), s);
    }

    public SimulationResult runSimulation(String key, Map<String, Object> params) {
        SimulationStrategy s = strategies.getOrDefault(key, strategies.get("EVENT_SURGE"));
        return s.simulate(params);
    }
}

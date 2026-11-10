package com.mealsynx.service;

import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.MenuDAO;
import com.mealsynx.dao.OrderDAO;
import com.mealsynx.model.CampusEvent;
import com.mealsynx.model.Canteen;
import com.mealsynx.model.FoodItem;
import com.mealsynx.model.WeatherSnapshot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictionService {
    private final MenuDAO menuDAO;
    private final OrderDAO orderDAO;
    private final CanteenDAO canteenDAO;
    private final EventWeatherService eventWeatherService;

    public PredictionService(MenuDAO menuDAO, OrderDAO orderDAO, CanteenDAO canteenDAO,
                             EventWeatherService eventWeatherService) {
        this.menuDAO = menuDAO;
        this.orderDAO = orderDAO;
        this.canteenDAO = canteenDAO;
        this.eventWeatherService = eventWeatherService;
    }

    public double getActiveEventMultiplier() {
        List<CampusEvent> events = eventWeatherService.getUpcomingEvents();
        if (events.isEmpty()) return 1.0;
        return events.get(0).getDemandMultiplier();
    }

    public Map<String, Integer> predictCanteenDishDemand(String canteenId) {
        Map<String, Integer> predictions = new LinkedHashMap<>();
        WeatherSnapshot weather = eventWeatherService.getCurrentWeather();
        double eventMult = getActiveEventMultiplier();

        for (FoodItem item : menuDAO.findByCanteen(canteenId)) {
            double weatherMult = weather.getCategoryDemandMultiplier(item.getCategory());
            int sysEst = item.getSystemEstimatedKitchenPrep(weatherMult, eventMult);
            int target = item.getCustomKitchenPlanTarget() > 0 ? item.getCustomKitchenPlanTarget() : sysEst;
            predictions.put(item.getName(), target);
        }
        return predictions;
    }

    public int getTotalCampusPredictedOrders() {
        int sum = 0;
        for (Canteen c : canteenDAO.findAll()) {
            for (int val : predictCanteenDishDemand(c.getCanteenId()).values()) {
                sum += val;
            }
        }
        return sum;
    }
}

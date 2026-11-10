package com.mealsynx.service;

import com.mealsynx.dao.FeedbackDAO;
import com.mealsynx.dao.MenuDAO;
import com.mealsynx.dao.WasteDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.FoodItem;
import com.mealsynx.model.WasteRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class WasteService {
    private final WasteDAO wasteDAO;
    private final FeedbackDAO feedbackDAO;
    private final MenuDAO menuDAO;

    public WasteService(WasteDAO wasteDAO, FeedbackDAO feedbackDAO, MenuDAO menuDAO) {
        this.wasteDAO = wasteDAO;
        this.feedbackDAO = feedbackDAO;
        this.menuDAO = menuDAO;
    }

    public FeedbackDAO.FeedbackEntry submitStudentFoodReview(String studentUsername, FoodItem food,
                                                             int stars, String comment) {
        food.recordNewUserRating(stars);
        menuDAO.save(food);

        String lower = comment.toLowerCase();
        String taste = (lower.contains("good") || lower.contains("great") || lower.contains("best") || lower.contains("tasty") || lower.contains("fresh") || stars >= 4)
                ? "Positive" : "Needs Improvement";
        String spice = (lower.contains("too spicy") || lower.contains("very spicy") || lower.contains("burning"))
                ? "Negative (Too Spicy)" : (lower.contains("bland") ? "Negative (Too Mild)" : "Optimal");
        String qty = (lower.contains("low") || lower.contains("small") || lower.contains("less"))
                ? "Negative (Low Portion)" : "Positive";

        FeedbackDAO.FeedbackEntry entry = new FeedbackDAO.FeedbackEntry(
                studentUsername, food.getCanteenId(), food.getFoodId(), food.getName(),
                stars, comment, taste, spice, qty
        );
        feedbackDAO.saveFeedback(entry);
        PersistentDataStore.getInstance().saveToDisk();
        return entry;
    }

    public WasteRecord recordDailyBatchWaste(String canteenId, String foodName,
                                             int predictedQty, int preparedQty, int soldQty,
                                             WasteRecord.WasteReason reason) {
        int wasted = Math.max(0, preparedQty - soldQty);
        double weightKg = Math.round(wasted * 0.38 * 10.0) / 10.0;
        String id = "WST-" + (101 + wasteDAO.findAll().size());
        WasteRecord rec = new WasteRecord(id, canteenId, foodName, LocalDate.now(),
                predictedQty, preparedQty, soldQty, wasted, weightKg, reason);
        wasteDAO.save(rec);
        PersistentDataStore.getInstance().saveToDisk();
        return rec;
    }

    public List<WasteRecord> getAllWasteRecords() { return wasteDAO.findAll(); }
    public double getTotalWasteKg() { return wasteDAO.getTotalWasteKg(); }
    public Map<String, Double> getWasteReasonBreakdown() { return wasteDAO.getReasonPercentageBreakdown(); }
    public List<FeedbackDAO.FeedbackEntry> getAllFeedbacks() { return feedbackDAO.findAll(); }
}

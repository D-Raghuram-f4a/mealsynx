package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    public static class FeedbackEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String customerUsername;
        public final String canteenId;
        public final String foodId;
        public final String foodName;
        public final int rating;
        public final String comment;
        public final String tasteSentiment;
        public final String spicinessSentiment;
        public final String quantitySentiment;
        public final String timestamp;

        public FeedbackEntry(String customerUsername, String canteenId, String foodId, String foodName,
                             int rating, String comment, String tasteSentiment,
                             String spicinessSentiment, String quantitySentiment) {
            this.customerUsername = customerUsername;
            this.canteenId = canteenId;
            this.foodId = foodId;
            this.foodName = foodName;
            this.rating = rating;
            this.comment = comment;
            this.tasteSentiment = tasteSentiment;
            this.spicinessSentiment = spicinessSentiment;
            this.quantitySentiment = quantitySentiment;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM hh:mm a"));
        }
    }

    public void saveFeedback(FeedbackEntry entry) {
        PersistentDataStore.getInstance().data().feedbacks.add(0, entry);
        PersistentDataStore.getInstance().saveToDisk();
    }

    public List<FeedbackEntry> findAll() {
        return new ArrayList<>(PersistentDataStore.getInstance().data().feedbacks);
    }
}

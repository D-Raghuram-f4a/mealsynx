package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.WasteRecord;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WasteDAO {
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    public void save(WasteRecord record) {
        store.data().wasteRecords.add(0, record);
        store.saveToDisk();
    }

    public List<WasteRecord> findAll() {
        return new ArrayList<>(store.data().wasteRecords);
    }

    public double getTotalWasteKg() {
        return Math.round(store.data().wasteRecords.stream().mapToDouble(WasteRecord::getWasteWeightKg).sum() * 10.0) / 10.0;
    }

    public Map<String, Double> getReasonPercentageBreakdown() {
        Map<String, Double> map = new LinkedHashMap<>();
        double totalKg = getTotalWasteKg();
        if (totalKg <= 0) {
            map.put("OVERPRODUCTION", 50.0);
            map.put("LOW_DEMAND", 25.0);
            map.put("SPOILAGE", 15.0);
            map.put("PREPARATION_ERROR", 10.0);
            return map;
        }
        Map<String, Double> sums = new LinkedHashMap<>();
        for (WasteRecord r : store.data().wasteRecords) {
            String key = r.getReason().name();
            sums.put(key, sums.getOrDefault(key, 0.0) + r.getWasteWeightKg());
        }
        for (Map.Entry<String, Double> e : sums.entrySet()) {
            double pct = Math.round((e.getValue() / totalKg) * 1000.0) / 10.0;
            map.put(e.getKey(), pct);
        }
        return map;
    }
}

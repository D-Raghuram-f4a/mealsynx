package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.CampusEvent;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    public void addEvent(CampusEvent event) {
        store.data().events.add(0, event);
        store.saveToDisk();
    }

    public List<CampusEvent> findAll() {
        return new ArrayList<>(store.data().events);
    }
}

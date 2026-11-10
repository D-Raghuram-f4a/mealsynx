package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Canteen;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CanteenDAO {
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    public List<Canteen> findAll() {
        return new ArrayList<>(store.data().canteens.values());
    }

    public Optional<Canteen> findById(String id) {
        return Optional.ofNullable(store.data().canteens.get(id));
    }

    public void save(Canteen canteen) {
        store.data().canteens.put(canteen.getCanteenId(), canteen);
        store.saveToDisk();
    }
}

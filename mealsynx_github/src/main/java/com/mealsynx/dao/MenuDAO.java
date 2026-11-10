package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.interfaces.CrudRepository;
import com.mealsynx.model.FoodItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuDAO implements CrudRepository<FoodItem, String> {
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    @Override
    public FoodItem save(FoodItem entity) {
        store.data().menuItems.put(entity.getFoodId(), entity);
        store.saveToDisk();
        return entity;
    }

    @Override
    public Optional<FoodItem> findById(String id) {
        return Optional.ofNullable(store.data().menuItems.get(id));
    }

    public Optional<FoodItem> findByName(String name) {
        return store.data().menuItems.values().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name) || f.getName().toLowerCase().contains(name.toLowerCase()))
                .findFirst();
    }

    @Override
    public List<FoodItem> findAll() {
        return new ArrayList<>(store.data().menuItems.values());
    }

    public List<FoodItem> findByCanteen(String canteenId) {
        if (canteenId == null || "ALL".equalsIgnoreCase(canteenId)) {
            return findAll();
        }
        return store.data().menuItems.values().stream()
                .filter(f -> f.getCanteenId().equalsIgnoreCase(canteenId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        boolean removed = store.data().menuItems.remove(id) != null;
        if (removed) store.saveToDisk();
        return removed;
    }
}

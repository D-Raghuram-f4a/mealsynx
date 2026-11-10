package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.interfaces.CrudRepository;
import com.mealsynx.model.IngredientStock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryDAO implements CrudRepository<IngredientStock, String> {
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    @Override
    public IngredientStock save(IngredientStock entity) {
        store.data().inventory.put(entity.getIngredientId(), entity);
        store.saveToDisk();
        return entity;
    }

    @Override
    public Optional<IngredientStock> findById(String id) {
        return Optional.ofNullable(store.data().inventory.get(id));
    }

    @Override
    public List<IngredientStock> findAll() {
        return new ArrayList<>(store.data().inventory.values());
    }

    public List<IngredientStock> findByCanteen(String canteenId) {
        return store.data().inventory.values().stream()
                .filter(i -> i.getCanteenId().equalsIgnoreCase(canteenId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        boolean removed = store.data().inventory.remove(id) != null;
        if (removed) store.saveToDisk();
        return removed;
    }
}

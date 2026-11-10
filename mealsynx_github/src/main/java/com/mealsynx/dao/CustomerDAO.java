package com.mealsynx.dao;

import com.mealsynx.db.DatabaseConnectionPool;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.interfaces.CrudRepository;
import com.mealsynx.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO implements CrudRepository<Customer, String> {
    private final PersistentDataStore store = PersistentDataStore.getInstance();
    private final DatabaseConnectionPool dbPool = DatabaseConnectionPool.getInstance();

    @Override
    public Customer save(Customer entity) {
        store.data().customers.put(entity.getUserId(), entity);
        store.saveToDisk();
        dbPool.saveOrUpdateUser(
            entity.getUserId(),
            entity.getUsername(),
            entity.getFullName(),
            entity.getRegisterNo(),
            entity.getEmail(),
            entity.getPassword(),
            "CUSTOMER",
            entity.getWalletBalance(),
            entity.getFavouriteCategory(),
            entity.getTypicalOrderingTime(),
            entity.getAverageSpending(),
            null,
            null
        );
        return entity;
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.data().customers.get(id));
    }

    public Optional<Customer> findByUsernameOrIdentifier(String input) {
        if (input == null) return Optional.empty();
        String trimmed = input.trim();
        String normalized = trimmed.replaceAll("\\s+", "");
        return store.data().customers.values().stream()
                .filter(c -> c.getUsername().equalsIgnoreCase(trimmed)
                        || c.getUsername().replaceAll("\\s+", "").equalsIgnoreCase(normalized)
                        || c.getUserId().equalsIgnoreCase(trimmed)
                        || (c.getRegisterNo() != null && c.getRegisterNo().equalsIgnoreCase(trimmed))
                        || c.getFullName().equalsIgnoreCase(trimmed))
                .findFirst();
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.data().customers.values());
    }

    @Override
    public boolean deleteById(String id) {
        boolean removed = store.data().customers.remove(id) != null;
        if (removed) store.saveToDisk();
        return removed;
    }
}

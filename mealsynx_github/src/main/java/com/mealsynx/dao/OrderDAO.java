package com.mealsynx.dao;

import com.mealsynx.db.DatabaseConnectionPool;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.interfaces.CrudRepository;
import com.mealsynx.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderDAO implements CrudRepository<Order, String> {
    private final PersistentDataStore store = PersistentDataStore.getInstance();
    private final DatabaseConnectionPool dbPool = DatabaseConnectionPool.getInstance();

    @Override
    public synchronized Order save(Order entity) {
        store.data().orders.put(entity.getOrderId(), entity);
        store.saveToDisk();
        dbPool.saveOrUpdateOrder(
            entity.getOrderId(),
            entity.getCustomerId(),
            entity.getCustomerUsername(),
            entity.getCanteenId(),
            entity.getOrderType().name(),
            entity.getDeliveryMode().name(),
            entity.getDeliveryPoint(),
            entity.getScheduledTime(),
            entity.getPaymentMethod(),
            entity.getTotalAmount(),
            entity.getStatus().name()
        );
        return entity;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.data().orders.get(id));
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>(store.data().orders.values());
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return list;
    }

    public List<Order> findByCustomer(String customerIdOrUsername) {
        return findAll().stream()
                .filter(o -> o.getCustomerId().equalsIgnoreCase(customerIdOrUsername)
                        || o.getCustomerUsername().equalsIgnoreCase(customerIdOrUsername))
                .collect(Collectors.toList());
    }

    public List<Order> findByCanteen(String canteenId) {
        return findAll().stream()
                .filter(o -> o.getCanteenId().equalsIgnoreCase(canteenId))
                .collect(Collectors.toList());
    }

    public int countActiveOrdersForCanteen(String canteenId) {
        return (int) store.data().orders.values().stream()
                .filter(o -> o.getCanteenId().equalsIgnoreCase(canteenId) && o.isActiveInKitchen())
                .count();
    }

    @Override
    public boolean deleteById(String id) {
        boolean removed = store.data().orders.remove(id) != null;
        if (removed) store.saveToDisk();
        return removed;
    }
}

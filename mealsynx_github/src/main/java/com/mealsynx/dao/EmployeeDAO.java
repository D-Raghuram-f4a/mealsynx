package com.mealsynx.dao;

import com.mealsynx.db.DatabaseConnectionPool;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Admin;
import com.mealsynx.model.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAO {
    private final PersistentDataStore store = PersistentDataStore.getInstance();
    private final DatabaseConnectionPool dbPool = DatabaseConnectionPool.getInstance();

    public synchronized Worker saveWorker(Worker worker) {
        store.data().workers.removeIf(w -> w.getUsername().equalsIgnoreCase(worker.getUsername()) || w.getUserId().equalsIgnoreCase(worker.getUserId()));
        store.data().workers.add(worker);
        store.saveToDisk();
        dbPool.saveOrUpdateUser(
            worker.getUserId(),
            worker.getUsername(),
            worker.getFullName(),
            worker.getRegisterNo(),
            worker.getEmail(),
            worker.getPassword(),
            "WORKER",
            0.00,
            null,
            null,
            0.00,
            worker.getAssignedCanteenId(),
            worker.getShiftTiming()
        );
        return worker;
    }

    public synchronized Admin saveAdmin(Admin admin) {
        store.data().admins.removeIf(a -> a.getUsername().equalsIgnoreCase(admin.getUsername()) || a.getUserId().equalsIgnoreCase(admin.getUserId()));
        store.data().admins.add(admin);
        store.saveToDisk();
        dbPool.saveOrUpdateUser(
            admin.getUserId(),
            admin.getUsername(),
            admin.getFullName(),
            admin.getRegisterNo(),
            admin.getEmail(),
            admin.getPassword(),
            "ADMIN",
            0.00,
            null,
            null,
            0.00,
            "CAN-1",
            "08:00 AM – 06:00 PM"
        );
        return admin;
    }

    public List<Worker> findAllWorkers() { return new ArrayList<>(store.data().workers); }
    public List<Admin> findAllAdmins() { return new ArrayList<>(store.data().admins); }

    public Optional<Worker> findWorker(String input) {
        if (input == null) return Optional.empty();
        String trimmed = input.trim();
        return store.data().workers.stream()
                .filter(w -> w.getUsername().equalsIgnoreCase(trimmed)
                        || w.getUserId().equalsIgnoreCase(trimmed)
                        || w.getRegisterNo().equalsIgnoreCase(trimmed))
                .findFirst();
    }

    public Optional<Admin> findAdmin(String input) {
        if (input == null) return Optional.empty();
        String trimmed = input.trim();
        return store.data().admins.stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(trimmed)
                        || a.getUserId().equalsIgnoreCase(trimmed)
                        || a.getRegisterNo().equalsIgnoreCase(trimmed))
                .findFirst();
    }
}

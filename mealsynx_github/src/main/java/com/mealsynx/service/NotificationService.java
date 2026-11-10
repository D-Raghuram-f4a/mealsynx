package com.mealsynx.service;

import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Canteen;
import com.mealsynx.model.Customer;
import com.mealsynx.model.Worker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final List<String> broadcastHistory = new ArrayList<>();

    public void notifyStudent(Customer student, String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String formatted = "🔔 [" + ts + "] " + message;
        student.pushNotification(formatted);
        broadcastHistory.add(0, "To Student " + student.getUsername() + ": " + formatted);
        PersistentDataStore.getInstance().saveToDisk();
    }

    public void notifyWorkerForCanteen(EmployeeDAO employeeDAO, String canteenId, String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String formatted = "🔔 [" + ts + "] " + message;
        for (Worker w : employeeDAO.findAllWorkers()) {
            if (w.getAssignedCanteenId().equalsIgnoreCase(canteenId)) {
                w.pushNotification(formatted);
            }
        }
        broadcastHistory.add(0, "To Canteen " + canteenId + " Workers: " + formatted);
        PersistentDataStore.getInstance().saveToDisk();
    }

    public void sendAdminAlertToCanteens(String targetCanteenIdOrAll, String alertCategory,
                                         String message, CanteenDAO canteenDAO, EmployeeDAO employeeDAO) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM hh:mm a"));
        String fullAlert = String.format("📢 [%s | %s] %s", ts, alertCategory.toUpperCase(), message);

        for (Canteen c : canteenDAO.findAll()) {
            if ("ALL".equalsIgnoreCase(targetCanteenIdOrAll) || c.getCanteenId().equalsIgnoreCase(targetCanteenIdOrAll)) {
                c.addAdminUpdate(fullAlert);
                canteenDAO.save(c);
                notifyWorkerForCanteen(employeeDAO, c.getCanteenId(), "ADMIN ALERT (" + alertCategory + "): " + message);
            }
        }
        PersistentDataStore.getInstance().saveToDisk();
    }

    public List<String> getBroadcastHistory() {
        return new ArrayList<>(broadcastHistory);
    }
}

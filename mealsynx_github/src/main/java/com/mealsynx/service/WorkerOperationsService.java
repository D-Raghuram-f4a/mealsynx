package com.mealsynx.service;

import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Admin;
import com.mealsynx.model.Canteen;
import com.mealsynx.model.SimulationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkerOperationsService {
    private final CanteenDAO canteenDAO;
    private final EmployeeDAO employeeDAO;
    private final NotificationService notificationService;
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    public WorkerOperationsService(CanteenDAO canteenDAO, EmployeeDAO employeeDAO,
                                   NotificationService notificationService) {
        this.canteenDAO = canteenDAO;
        this.employeeDAO = employeeDAO;
        this.notificationService = notificationService;
    }

    public Optional<Canteen> getCanteen(String canteenId) {
        return canteenDAO.findById(canteenId);
    }

    public void recordWagesOrUtilitiesExpense(String canteenId, String expenseType, double amount) {
        canteenDAO.findById(canteenId).ifPresent(canteen -> {
            if ("WORKER_WAGES".equalsIgnoreCase(expenseType)) {
                canteen.addWorkerWagesExpense(amount);
            } else {
                canteen.addUtilitiesExpense(amount);
            }
            canteenDAO.save(canteen);
            if (canteen.isRunningInLoss()) {
                notificationService.notifyWorkerForCanteen(employeeDAO, canteenId,
                        String.format("🚨 LOSS WARNING: %s is running in a Net LOSS of ₹%.2f (Revenue: ₹%.2f vs Total Expenses: ₹%.2f)!",
                                canteen.getName(), Math.abs(canteen.getNetProfitOrLoss()),
                                canteen.getTotalSalesRevenue(), canteen.getTotalExpenses()));
            }
            store.saveToDisk();
        });
    }

    public SimulationRequest submitWhatIfSimulationRequest(String workerUsername, String canteenId,
                                                           String scenarioType, String requestDetails) {
        String id = "SIMREQ-" + (101 + store.data().simulationRequests.size());
        SimulationRequest req = new SimulationRequest(id, workerUsername, canteenId, scenarioType, requestDetails);
        store.data().simulationRequests.add(0, req);
        for (Admin a : employeeDAO.findAllAdmins()) {
            a.pushNotification("🔔 [Worker Sim Request " + id + "] " + workerUsername + " (" + canteenId + ") requested " + scenarioType + ": " + requestDetails);
        }
        store.saveToDisk();
        return req;
    }

    public List<SimulationRequest> getAllSimulationRequests() {
        return new ArrayList<>(store.data().simulationRequests);
    }
}

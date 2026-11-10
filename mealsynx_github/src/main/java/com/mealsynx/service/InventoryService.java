package com.mealsynx.service;

import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.dao.InventoryDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.IngredientStock;

import java.time.LocalDate;
import java.util.List;

public class InventoryService {
    private final InventoryDAO inventoryDAO;
    private final CanteenDAO canteenDAO;
    private final EmployeeDAO employeeDAO;
    private final NotificationService notificationService;

    public InventoryService(InventoryDAO inventoryDAO, CanteenDAO canteenDAO,
                            EmployeeDAO employeeDAO, NotificationService notificationService) {
        this.inventoryDAO = inventoryDAO;
        this.canteenDAO = canteenDAO;
        this.employeeDAO = employeeDAO;
        this.notificationService = notificationService;
    }

    public List<IngredientStock> getInventoryForCanteen(String canteenId) {
        return inventoryDAO.findByCanteen(canteenId);
    }

    public List<IngredientStock> getAllInventory() {
        return inventoryDAO.findAll();
    }

    public void recordIngredientPurchase(String ingredientId, double quantityPurchased, int shelfLifeDays) {
        inventoryDAO.findById(ingredientId).ifPresent(stock -> {
            stock.setCurrentStock(stock.getCurrentStock() + quantityPurchased);
            stock.setPurchaseDate(LocalDate.now());
            stock.setExpiryDate(LocalDate.now().plusDays(Math.max(1, shelfLifeDays)));
            inventoryDAO.save(stock);

            double purchaseCost = quantityPurchased * stock.getCostPerUnit();
            canteenDAO.findById(stock.getCanteenId()).ifPresent(canteen -> {
                canteen.addIngredientPurchaseExpense(purchaseCost);
                canteenDAO.save(canteen);
                notificationService.notifyWorkerForCanteen(employeeDAO, canteen.getCanteenId(),
                        String.format("Purchased %.1f %s of %s for ₹%.2f (New Stock: %.1f %s).",
                                quantityPurchased, stock.getUnit(), stock.getName(),
                                purchaseCost, stock.getCurrentStock(), stock.getUnit()));
                if (canteen.isRunningInLoss()) {
                    notificationService.notifyWorkerForCanteen(employeeDAO, canteen.getCanteenId(),
                            String.format("🚨 FINANCIAL LOSS WARNING: %s expenses (₹%.2f) now exceed revenue (₹%.2f)! Net Loss: ₹%.2f",
                                    canteen.getName(), canteen.getTotalExpenses(),
                                    canteen.getTotalSalesRevenue(), Math.abs(canteen.getNetProfitOrLoss())));
                }
            });
            PersistentDataStore.getInstance().saveToDisk();
        });
    }

    public void recordDailyConsumption(String ingredientId, double quantityUsed) {
        inventoryDAO.findById(ingredientId).ifPresent(stock -> {
            stock.setCurrentStock(stock.getCurrentStock() - quantityUsed);
            inventoryDAO.save(stock);
            if (stock.isLowStock() || stock.isQualityOrExpiryLow()) {
                notificationService.notifyWorkerForCanteen(employeeDAO, stock.getCanteenId(),
                        String.format("⚠ LOW STOCK / EXPIRY ALERT: %s is down to %.1f %s (Needed tomorrow: %.1f %s | %s)",
                                stock.getName(), stock.getCurrentStock(), stock.getUnit(),
                                stock.getNextDayRequiredQty(), stock.getUnit(), stock.getQualityStatusLabel()));
            }
            PersistentDataStore.getInstance().saveToDisk();
        });
    }

    public IngredientStock addNewWarehouseIngredient(String canteenId, String name, String category,
                                                     double initialQty, double safetyQty, double nextDayReq,
                                                     String unit, double unitCost, int expiryDays) {
        String id = "ING-" + (400 + inventoryDAO.findAll().size() + 1);
        IngredientStock stock = new IngredientStock(
                id, name, category, canteenId,
                LocalDate.now(), LocalDate.now().plusDays(expiryDays),
                initialQty, safetyQty, nextDayReq, unit, unitCost
        );
        inventoryDAO.save(stock);
        double totalCost = initialQty * unitCost;
        canteenDAO.findById(canteenId).ifPresent(c -> {
            c.addIngredientPurchaseExpense(totalCost);
            canteenDAO.save(c);
        });
        PersistentDataStore.getInstance().saveToDisk();
        return stock;
    }
}

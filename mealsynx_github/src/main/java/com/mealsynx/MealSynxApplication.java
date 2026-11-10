package com.mealsynx;

import com.mealsynx.api.MealSynxRestServer;
import com.mealsynx.bigdata.BigDataPipelineService;
import com.mealsynx.dao.*;
import com.mealsynx.db.DatabaseConfig;
import com.mealsynx.db.DatabaseConnectionPool;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Customer;
import com.mealsynx.service.*;
import com.mealsynx.ui.MealSynxConsoleCLI;
import com.mealsynx.ui.MealSynxDesktopApp;

import javax.swing.*;
import java.awt.*;

public class MealSynxApplication {

    public static void main(String[] args) {
        DatabaseConnectionPool dbPool = DatabaseConnectionPool.getInstance();
        System.out.println("[DB LAYER] Connection Name : " + DatabaseConfig.MYSQL_CONNECTION_NAME);
        System.out.println("[DB LAYER] Database Schema : " + DatabaseConfig.DB_SCHEMA);
        System.out.println("[DB LAYER] Connection Mode : " + (dbPool.isLiveJdbcAvailable()
                ? "LIVE MySQL 8.4 LTS Connected"
                : "Local Fault-Tolerant Engine (Set password in application.properties or DatabaseConfig.java to connect)"));

        PersistentDataStore store = PersistentDataStore.getInstance();

        CanteenDAO canteenDAO = new CanteenDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();
        MenuDAO menuDAO = new MenuDAO();
        OrderDAO orderDAO = new OrderDAO();
        InventoryDAO inventoryDAO = new InventoryDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        EventDAO eventDAO = new EventDAO();
        WasteDAO wasteDAO = new WasteDAO();

        NotificationService notificationService = new NotificationService();
        AuthService authService = new AuthService(customerDAO, employeeDAO);
        SupportService supportService = new SupportService(employeeDAO);
        UserService userService = new UserService(customerDAO, menuDAO, orderDAO, canteenDAO, paymentDAO, notificationService);
        MenuService menuService = new MenuService(menuDAO, canteenDAO);
        PaymentService paymentService = new PaymentService(customerDAO, paymentDAO);
        OrderService orderService = new OrderService(orderDAO, menuDAO, canteenDAO, customerDAO, employeeDAO, inventoryDAO, paymentService, notificationService);
        InventoryService inventoryService = new InventoryService(inventoryDAO, canteenDAO, employeeDAO, notificationService);
        WorkerOperationsService workerService = new WorkerOperationsService(canteenDAO, employeeDAO, notificationService);
        EventWeatherService eventWeatherService = new EventWeatherService(eventDAO);
        PredictionService predictionService = new PredictionService(menuDAO, orderDAO, canteenDAO, eventWeatherService);
        WasteService wasteService = new WasteService(wasteDAO, feedbackDAO, menuDAO);
        SimulationEngine simulationEngine = new SimulationEngine();
        AnalyticsService analyticsService = new AnalyticsService(canteenDAO, orderDAO, menuDAO, wasteDAO, predictionService);
        BigDataPipelineService bigDataPipeline = new BigDataPipelineService();

        if (args.length > 0 && "--verify-only".equalsIgnoreCase(args[0])) {
            Customer r = userService.findStudent("Raghuram").orElseThrow();
            userService.toggleStudentFavourite(r, "FD-108");
            store.saveToDisk();
            store.resetToFactoryDataset();
            return;
        }

        CompletableWeatherSync(eventWeatherService);

        MealSynxRestServer restServer = new MealSynxRestServer(8080, menuService, analyticsService);
        try {
            restServer.start();
        } catch (Exception ignored) {}

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                MealSynxDesktopApp gui = new MealSynxDesktopApp(
                        authService, userService, menuService, orderService, inventoryService,
                        workerService, eventWeatherService, predictionService, wasteService,
                        simulationEngine, analyticsService, notificationService, supportService,
                        canteenDAO, employeeDAO, bigDataPipeline
                );
                gui.setVisible(true);
            });
        }

        MealSynxConsoleCLI cli = new MealSynxConsoleCLI(
                authService, userService, menuService, orderService, inventoryService,
                workerService, eventWeatherService, predictionService, wasteService,
                simulationEngine, analyticsService, notificationService, supportService,
                canteenDAO, employeeDAO, bigDataPipeline
        );
        cli.runInteractiveLoop();
        restServer.stop();
    }

    private static void CompletableWeatherSync(EventWeatherService eventWeatherService) {
        Thread bg = new Thread(() -> {
            eventWeatherService.syncLiveWeatherFromApi();
            eventWeatherService.syncPublicHolidaysAndAcademicCalendar();
        });
        bg.setDaemon(true);
        bg.start();
    }
}

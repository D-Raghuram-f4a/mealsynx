package com.mealsynx.api;

import com.mealsynx.model.Canteen;
import com.mealsynx.model.FoodItem;
import com.mealsynx.service.AnalyticsService;
import com.mealsynx.service.MenuService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class MealSynxRestServer {
    private final int port;
    private final MenuService menuService;
    private final AnalyticsService analyticsService;
    private HttpServer server;

    public MealSynxRestServer(int port, MenuService menuService, AnalyticsService analyticsService) {
        this.port = port;
        this.menuService = menuService;
        this.analyticsService = analyticsService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", ex -> sendJson(ex, 200, "{\"status\":\"UP\",\"platform\":\"MealSynx v6.0\"}"));
        server.createContext("/api/menu", ex -> {
            List<FoodItem> items = menuService.getAllFoodItems();
            String json = "[" + items.stream().map(f -> String.format(
                    "{\"foodId\":\"%s\",\"name\":\"%s\",\"canteen\":\"%s\",\"price\":%.2f,\"rating\":%.2f}",
                    f.getFoodId(), f.getName(), f.getCanteenId(), f.getPrice(), f.getCustomerRating()
            )).collect(Collectors.joining(",")) + "]";
            sendJson(ex, 200, json);
        });
        server.createContext("/api/canteens", ex -> {
            List<Canteen> list = analyticsService.getAllCanteens();
            String json = "[" + list.stream().map(c -> String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"busy\":\"%s\",\"revenue\":%.2f,\"profit\":%.2f}",
                    c.getCanteenId(), c.getName(), c.getSpecialtyType(), c.getBusyStatus(),
                    c.getTotalSalesRevenue(), c.getNetProfitOrLoss()
            )).collect(Collectors.joining(",")) + "]";
            sendJson(ex, 200, json);
        });
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    private void sendJson(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}

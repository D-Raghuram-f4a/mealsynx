package com.mealsynx.db;

import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConnectionPool {
    private static DatabaseConnectionPool instance;
    private final String jdbcUrl;
    private final String username;
    private String password;
    private final BlockingQueue<Connection> pool;
    private boolean liveJdbcAvailable = false;

    private final Set<String> userColumns = new HashSet<>();
    private final Set<String> orderColumns = new HashSet<>();
    private boolean roleEnumSupportsCustomer = true;
    private boolean roleEnumSupportsStudent = false;

    private DatabaseConnectionPool() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
        } catch (Exception ignored) {}

        String configuredUrl = props.getProperty("db.url", DatabaseConfig.JDBC_URL);
        this.jdbcUrl = System.getProperty("mealsynx.db.url", configuredUrl);

        String configuredUser = props.getProperty("db.username", DatabaseConfig.DB_USER);
        this.username = System.getProperty("mealsynx.db.user", configuredUser);

        String cfgPass = DatabaseConfig.DB_PASSWORD;
        if ("ENTER_YOUR_PASSWORD_HERE".equals(cfgPass)) {
            String propPass = props.getProperty("db.password", "ENTER_YOUR_PASSWORD_HERE");
            if (!"ENTER_YOUR_PASSWORD_HERE".equals(propPass)) {
                cfgPass = propPass;
            }
        }
        this.password = System.getProperty("mealsynx.db.pass", cfgPass);
        this.pool = new ArrayBlockingQueue<>(10);
        initializePool();
    }

    public static synchronized DatabaseConnectionPool getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionPool();
        }
        return instance;
    }

    private void initializePool() {
        if ("ENTER_YOUR_PASSWORD_HERE".equals(this.password)) {
            this.liveJdbcAvailable = false;
            System.out.println("[DB LAYER] Notice: MySQL password not set ('ENTER_YOUR_PASSWORD_HERE').");
            System.out.println("[DB LAYER] To connect live MySQL 8.4, set your password in application.properties or DatabaseConfig.java.");
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            bootstrapSchemaIfNeeded(conn);
            pool.offer(conn);
            liveJdbcAvailable = true;
            System.out.println("[DB LAYER] ========================================================");
            System.out.println("[DB LAYER] Connection Name : " + DatabaseConfig.MYSQL_CONNECTION_NAME);
            System.out.println("[DB LAYER] Database Schema : " + DatabaseConfig.DB_SCHEMA);
            System.out.println("[DB LAYER] Connection Mode : LIVE MySQL 8.4 LTS Connected");
            System.out.println("[DB LAYER] ========================================================");
        } catch (Exception ex) {
            liveJdbcAvailable = false;
            System.err.println("[DB LAYER] MySQL Connection Failed: " + ex.getMessage());
            System.err.println("[DB LAYER] Falling back to local fault-tolerant data engine.");
        }
    }

    private void bootstrapSchemaIfNeeded(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.DB_SCHEMA);
            st.executeUpdate("USE " + DatabaseConfig.DB_SCHEMA);

            // 1. Ensure canteens table exists and has essential canteens so foreign keys never fail
            try {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS canteens (" +
                        "canteen_id VARCHAR(20) PRIMARY KEY, name VARCHAR(120) NOT NULL, " +
                        "location VARCHAR(150), max_capacity INT DEFAULT 500, " +
                        "is_active BOOLEAN DEFAULT TRUE)");
                st.executeUpdate("INSERT IGNORE INTO canteens (canteen_id, name) VALUES " +
                        "('CAN-1', 'Canteen 1 — Annapoorna Pure Veg'), " +
                        "('CAN-2', 'Canteen 2 — Spice Route Non-Veg'), " +
                        "('CAN-3', 'Canteen 3 — Campus Grills & Fast Food'), " +
                        "('CAN-A', 'Canteen A (Main Block)'), " +
                        "('CAN-B', 'Canteen B (Library Hub)'), " +
                        "('CAN-C', 'Canteen C (Hostel Zone)')");
            } catch (Exception ignored) {}

            // 2. Ensure users table exists with flexible schema if not already present
            try {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                        "user_id VARCHAR(20) PRIMARY KEY, " +
                        "username VARCHAR(60), " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "register_no VARCHAR(30), " +
                        "email VARCHAR(120) NOT NULL, " +
                        "password_hash VARCHAR(255), " +
                        "password_plain VARCHAR(120), " +
                        "role VARCHAR(30) NOT NULL, " +
                        "wallet_balance DECIMAL(10, 2) DEFAULT 500.00, " +
                        "favourite_category VARCHAR(50), " +
                        "typical_order_window VARCHAR(50), " +
                        "average_spending DECIMAL(10, 2) DEFAULT 120.00, " +
                        "assigned_canteen_id VARCHAR(20), " +
                        "shift_timing VARCHAR(60), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            } catch (Exception ignored) {}

            // 3. Inspect exact columns in users table to guarantee zero SQL syntax/column errors
            try (ResultSet rs = st.executeQuery("SHOW COLUMNS FROM users")) {
                userColumns.clear();
                while (rs.next()) {
                    String col = rs.getString("Field").toLowerCase();
                    userColumns.add(col);
                    if ("role".equalsIgnoreCase(col)) {
                        String type = rs.getString("Type").toUpperCase();
                        roleEnumSupportsStudent = type.contains("STUDENT") || type.contains("VARCHAR");
                        roleEnumSupportsCustomer = type.contains("CUSTOMER") || type.contains("VARCHAR");
                    }
                }
            } catch (Exception ignored) {}

            // 4. Ensure orders table exists
            try {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS orders (" +
                        "order_id VARCHAR(30) PRIMARY KEY, customer_id VARCHAR(20), customer_username VARCHAR(60), " +
                        "canteen_id VARCHAR(20), order_type VARCHAR(20), delivery_mode VARCHAR(30), " +
                        "delivery_point VARCHAR(150), scheduled_time VARCHAR(50), payment_method VARCHAR(40), " +
                        "total_amount DECIMAL(10,2), status VARCHAR(30))");
            } catch (Exception ignored) {}

            // 5. Inspect orders columns
            try (ResultSet rs = st.executeQuery("SHOW COLUMNS FROM orders")) {
                orderColumns.clear();
                while (rs.next()) {
                    orderColumns.add(rs.getString("Field").toLowerCase());
                }
            } catch (Exception ignored) {}

        } catch (Exception ex) {
            System.err.println("[DB LAYER] Schema bootstrap notice: " + ex.getMessage());
        }
    }

    /**
     * Dynamically saves or updates any user (Student, Worker, Admin) into the MySQL users table,
     * adapting automatically to the exact schema present in the database.
     */
    public synchronized boolean saveOrUpdateUser(
            String userId, String username, String fullName, String registerNo,
            String email, String password, String roleType, double walletBalance,
            String favouriteCategory, String typicalOrderWindow, double averageSpending,
            String assignedCanteenId, String shiftTiming) {

        if (!liveJdbcAvailable) return false;

        // Determine mapped role
        String dbRole = "CUSTOMER";
        if ("STUDENT".equalsIgnoreCase(roleType) || "CUSTOMER".equalsIgnoreCase(roleType)) {
            if (roleEnumSupportsStudent && !roleEnumSupportsCustomer) {
                dbRole = "STUDENT";
            } else {
                dbRole = "CUSTOMER";
            }
        } else if ("WORKER".equalsIgnoreCase(roleType)) {
            dbRole = "WORKER";
        } else if ("ADMIN".equalsIgnoreCase(roleType)) {
            dbRole = "ADMIN";
        }

        // Map candidate fields
        Map<String, Object> fields = new LinkedHashMap<>();
        if (userColumns.isEmpty() || userColumns.contains("user_id")) {
            fields.put("user_id", userId);
        }
        if (userColumns.isEmpty() || userColumns.contains("full_name")) {
            fields.put("full_name", (fullName != null && !fullName.isBlank()) ? fullName : (username != null ? username : userId));
        }
        if (userColumns.isEmpty() || userColumns.contains("register_no")) {
            fields.put("register_no", (registerNo != null && !registerNo.isBlank()) ? registerNo : null);
        }
        if (userColumns.isEmpty() || userColumns.contains("email")) {
            fields.put("email", (email != null && !email.isBlank()) ? email : (userId.toLowerCase() + "@campus.edu"));
        }
        if (userColumns.contains("username")) {
            fields.put("username", (username != null && !username.isBlank()) ? username : userId);
        }
        if (userColumns.contains("password_hash")) {
            fields.put("password_hash", (password != null && !password.isBlank()) ? password : (userId + "123*"));
        }
        if (userColumns.contains("password_plain")) {
            fields.put("password_plain", (password != null && !password.isBlank()) ? password : (userId + "123*"));
        }
        if (userColumns.isEmpty() || userColumns.contains("role")) {
            fields.put("role", dbRole);
        }
        if (userColumns.isEmpty() || userColumns.contains("wallet_balance")) {
            fields.put("wallet_balance", walletBalance);
        }
        if (userColumns.contains("favourite_category")) {
            fields.put("favourite_category", favouriteCategory);
        }
        if (userColumns.contains("typical_order_window")) {
            fields.put("typical_order_window", (typicalOrderWindow != null) ? typicalOrderWindow : "12:30 PM – 1:30 PM");
        }
        if (userColumns.contains("average_spending")) {
            fields.put("average_spending", (averageSpending > 0) ? averageSpending : 120.00);
        }
        if (userColumns.contains("assigned_canteen_id")) {
            fields.put("assigned_canteen_id", assignedCanteenId);
        }
        if (userColumns.contains("shift_timing")) {
            fields.put("shift_timing", shiftTiming);
        }

        // Build dynamic query
        StringBuilder colList = new StringBuilder();
        StringBuilder qList = new StringBuilder();
        StringBuilder updateList = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (colList.length() > 0) {
                colList.append(", ");
                qList.append(", ");
                updateList.append(", ");
            }
            colList.append(entry.getKey());
            qList.append("?");
            if (!"user_id".equalsIgnoreCase(entry.getKey())) {
                updateList.append(entry.getKey()).append(" = VALUES(").append(entry.getKey()).append(")");
            } else {
                updateList.append("user_id = user_id");
            }
            paramValues.add(entry.getValue());
        }

        String sql = "INSERT INTO users (" + colList + ") VALUES (" + qList + ") ON DUPLICATE KEY UPDATE " + updateList;

        Connection conn = null;
        try {
            conn = pool.poll();
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(jdbcUrl, username, password);
            }
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < paramValues.size(); i++) {
                    ps.setObject(i + 1, paramValues.get(i));
                }
                ps.executeUpdate();
            }
            conn.commit();
            System.out.println("[DB LAYER] ✅ User updated in MySQL '" + DatabaseConfig.DB_SCHEMA + ".users': ID=" + userId + ", Name=" + fullName + ", Role=" + dbRole);
            return true;
        } catch (SQLException e) {
            System.err.println("[DB LAYER] ❌ Error updating user in MySQL: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                pool.offer(conn);
            }
        }
    }

    /**
     * Dynamically saves or updates an order into the MySQL orders table.
     */
    public synchronized boolean saveOrUpdateOrder(
            String orderId, String customerId, String customerUsername, String canteenId,
            String orderType, String deliveryMode, String deliveryPoint, String scheduledTime,
            String paymentMethod, double totalAmount, String status) {

        if (!liveJdbcAvailable) return false;

        Map<String, Object> fields = new LinkedHashMap<>();
        if (orderColumns.isEmpty() || orderColumns.contains("order_id")) fields.put("order_id", orderId);
        if (orderColumns.isEmpty() || orderColumns.contains("customer_id")) fields.put("customer_id", customerId);
        if (orderColumns.contains("customer_username")) fields.put("customer_username", customerUsername);
        if (orderColumns.isEmpty() || orderColumns.contains("canteen_id")) fields.put("canteen_id", canteenId);
        if (orderColumns.isEmpty() || orderColumns.contains("order_type")) fields.put("order_type", orderType);
        if (orderColumns.isEmpty() || orderColumns.contains("delivery_mode")) fields.put("delivery_mode", deliveryMode);
        if (orderColumns.contains("delivery_point")) fields.put("delivery_point", deliveryPoint);
        else if (orderColumns.contains("delivery_location")) fields.put("delivery_location", deliveryPoint);
        if (orderColumns.contains("scheduled_time")) fields.put("scheduled_time", scheduledTime);
        if (orderColumns.isEmpty() || orderColumns.contains("payment_method")) fields.put("payment_method", paymentMethod);
        if (orderColumns.isEmpty() || orderColumns.contains("total_amount")) fields.put("total_amount", totalAmount);
        if (orderColumns.isEmpty() || orderColumns.contains("status")) fields.put("status", status);

        StringBuilder colList = new StringBuilder();
        StringBuilder qList = new StringBuilder();
        StringBuilder updateList = new StringBuilder();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (colList.length() > 0) {
                colList.append(", ");
                qList.append(", ");
                updateList.append(", ");
            }
            colList.append(entry.getKey());
            qList.append("?");
            if (!"order_id".equalsIgnoreCase(entry.getKey())) {
                updateList.append(entry.getKey()).append(" = VALUES(").append(entry.getKey()).append(")");
            } else {
                updateList.append("order_id = order_id");
            }
            paramValues.add(entry.getValue());
        }

        String sql = "INSERT INTO orders (" + colList + ") VALUES (" + qList + ") ON DUPLICATE KEY UPDATE " + updateList;

        Connection conn = null;
        try {
            conn = pool.poll();
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(jdbcUrl, username, password);
            }
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < paramValues.size(); i++) {
                    ps.setObject(i + 1, paramValues.get(i));
                }
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[DB LAYER] ❌ Error updating order in MySQL: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                pool.offer(conn);
            }
        }
    }

    public boolean executeTransactionalUpdate(String sql, Object... params) {
        if (!liveJdbcAvailable) return false;
        Connection conn = null;
        try {
            conn = pool.poll();
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(jdbcUrl, username, password);
            }
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[DB LAYER] ❌ Transaction SQL failed: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                pool.offer(conn);
            }
        }
    }

    public boolean isLiveJdbcAvailable() { return liveJdbcAvailable; }
}

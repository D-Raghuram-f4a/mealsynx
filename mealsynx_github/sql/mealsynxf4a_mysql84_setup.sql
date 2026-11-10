-- ============================================================================
-- MEALSYNX COMPLETE MYSQL 8.4 LTS DATABASE SETUP
-- Connection Name : mealsynxf4a
-- Database Name   : mealsynx
-- Authors         : D Raghuram (RA2511027040004) & B Likith Adhithya (RA2511027040022)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS mealsynx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mealsynx;

CREATE TABLE IF NOT EXISTS canteens (
    canteen_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    specialty_type VARCHAR(80) NOT NULL DEFAULT 'Multi-Cuisine',
    location VARCHAR(150) NOT NULL,
    max_capacity INT NOT NULL DEFAULT 400,
    active_orders INT NOT NULL DEFAULT 0,
    sales_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    ingredient_expenses DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    worker_wages DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    utilities_expenses DECIMAL(12, 2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB;

INSERT IGNORE INTO canteens (canteen_id, name, specialty_type, location) VALUES
('CAN-1', 'Canteen 1 — Annapoorna Pure Veg', 'Pure Veg South & North Indian', 'Academic Block A — Ground Floor'),
('CAN-2', 'Canteen 2 — Spice Route Non-Veg', 'Dum Biryani & Coastal Non-Veg', 'Central Dining Plaza — Block B'),
('CAN-3', 'Canteen 3 — Campus Grills & Fast Food', 'Charcoal Grills, Burgers & Wok', 'Student Activity Center — Block C'),
('CAN-A', 'Canteen A (Main Block)', 'Academic Dining', 'Academic Block A - Ground Floor'),
('CAN-B', 'Canteen B (Library Hub)', 'Coffee & Snacks', 'Block B - Library Complex'),
('CAN-C', 'Canteen C (Hostel Zone)', 'Hostel Meals', 'North Hostel Arena');

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(60),
    full_name VARCHAR(100) NOT NULL,
    register_no VARCHAR(30) UNIQUE,
    email VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255),
    password_plain VARCHAR(120),
    role VARCHAR(30) NOT NULL,
    wallet_balance DECIMAL(10, 2) DEFAULT 500.00,
    favourite_category VARCHAR(50),
    typical_order_window VARCHAR(50),
    average_spending DECIMAL(10, 2) DEFAULT 120.00,
    assigned_canteen_id VARCHAR(20),
    shift_timing VARCHAR(60),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT IGNORE INTO users (user_id, username, full_name, register_no, email, password_hash, password_plain, role, wallet_balance, favourite_category) VALUES
('USR-001', 'Raghuram', 'D Raghuram', 'RA2511027040004', 'raghuram@campus.edu', 'Raghuram123*', 'Raghuram123*', 'CUSTOMER', 640.00, 'Non-Veg Biryani & Grills'),
('USR-002', 'Likith', 'B Likith Adhithya', 'RA2511027040022', 'likith@campus.edu', 'Likith123*', 'Likith123*', 'CUSTOMER', 720.00, 'Fast Food & Grills'),
('STU-003', 'Student A', 'Aarav Krishnan (Student A)', 'RA2511027040031', 'studenta@campus.edu', 'Student A123*', 'Student A123*', 'CUSTOMER', 480.00, 'Pure Veg South Indian'),
('STU-004', 'Student B', 'Divya Nair (Student B)', 'RA2511027040045', 'studentb@campus.edu', 'Student B123*', 'Student B123*', 'CUSTOMER', 550.00, 'Grills & Beverages'),
('STU-005', 'Student C', 'Karthik Raja (Student C)', 'RA2511027040058', 'studentc@campus.edu', 'Student C123*', 'Student C123*', 'CUSTOMER', 810.00, 'Non-Veg Curries & Biryani'),
('WRK-101', 'Cant1workA', 'Murugan S (Veg Kitchen Lead)', 'EMP-CAN1-01', 'cant1worka@mealsynx.edu', 'Cant1workA123*', 'Cant1workA123*', 'WORKER', 0.00, NULL),
('WRK-102', 'Cant2workA', 'Senthil Pandian (Non-Veg Head Chef)', 'EMP-CAN2-01', 'cant2worka@mealsynx.edu', 'Cant2workA123*', 'Cant2workA123*', 'WORKER', 0.00, NULL),
('WRK-103', 'Cant3workA', 'Ramesh Babu (Grill & Fast Food Master)', 'EMP-CAN3-01', 'cant3worka@mealsynx.edu', 'Cant3workA123*', 'Cant3workA123*', 'WORKER', 0.00, NULL),
('ADM-001', 'admin', 'Campus Food Intelligence Director', 'ADM-CENTRAL-01', 'admin@mealsynx.edu', 'admin123*', 'admin123*', 'ADMIN', 0.00, NULL);

CREATE TABLE IF NOT EXISTS food_items (
    food_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    prep_cost DECIMAL(10, 2) NOT NULL,
    base_prep_mins INT NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    nutritional_info VARCHAR(150) NOT NULL,
    canteen_id VARCHAR(20) NOT NULL,
    historical_daily_demand INT DEFAULT 100,
    total_orders_count INT DEFAULT 25,
    waste_rate_percent DECIMAL(5, 2) DEFAULT 4.50,
    ingredient_difficulty INT DEFAULT 2,
    avg_rating DECIMAL(3, 2) DEFAULT 4.60,
    rating_count INT DEFAULT 15
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(30) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL,
    customer_username VARCHAR(60),
    canteen_id VARCHAR(20) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    delivery_mode VARCHAR(30) NOT NULL,
    delivery_point VARCHAR(150),
    scheduled_time VARCHAR(50),
    payment_method VARCHAR(40) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customer_feedback (
    feedback_id VARCHAR(30) PRIMARY KEY,
    customer_username VARCHAR(60) NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    rating INT NOT NULL,
    comment_text TEXT NOT NULL,
    taste_sentiment VARCHAR(25),
    spiciness_sentiment VARCHAR(25),
    quantity_sentiment VARCHAR(25),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

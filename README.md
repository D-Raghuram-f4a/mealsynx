# MealSynx — Intelligent Campus Food Demand, Waste & Resource Platform

Autonomous multi-tier campus dining & waste platform built on Java 21 LTS & MySQL 8.4 (3NF). Features a Strategy-pattern What-If simulation engine, JDBC connection pooling, async Kotlin DTOs, and closed-loop waste telemetry.

---

## Problem & Campus Dining Dilemma

High-density academic micro-cities serve thousands of students, faculty, and staff daily across narrow 45-minute lunch breaks:
* **The Counter Bottleneck:** Uncoordinated walk-in rushes overload primary dining centers while auxiliary cafeterias sit empty.
* **The Overproduction Dilemma:** Kitchens prepare meals based on guesswork, leading to massive financial losses and food disposal on examination days, bad weather, or holiday periods.
* **Lack of Integration:** Student ordering, warehouse raw-material procurement, kitchen preparation, and POS settlement operate in disconnected silos.

MealSynx resolves this through an autonomous, closed-loop software pipeline connecting demand signals directly to warehouse procurement and kitchen stoves.

---

## Operational Interface Screenshots

All original, uncompressed high-resolution application screenshots and operational dashboard captures from the live runtime environment are available in the project cloud drive:

👉 **Google Drive Screenshot Repository:** https://www.google.com/search?q=https://drive.google.com/drive/folders/1WU5mLa1a5kv9KuL4wuhrsInbJYXuqK\_C%3Fusp%3Ddrive\_link

---

## Core Capabilities & 10 Integrated Modules

1. **Customer & Personalization:** Smart home feed, day-of-week combo suggestions, and automated push alerts.
2. **Menu Management & Food Performance Scoring (FPS):** Multi-factor composite performance algorithm evaluating demand, margin, rating, and waste.
3. **Order & Campus Delivery Lifecycle:** Instant Buy-Now, scheduled advance pre-orders, digital food wallet, dynamic QR UPI payments, and campus delivery tracking.
4. **Kitchen Batch Operations:** Real-time daily kitchen prep plans and automated low-stock warnings.
5. **Warehouse & Recipe BOM Intelligence:** Algorithmic Bill of Materials exploding portion demand into raw ingredient kg (e.g., 300 biryani plates -> 30 kg rice, 35 kg poultry, 6 L cooking oil).
6. **Predictive Demand Engine:** Baseline sales modulated by academic calendar events, weather forecasts, and advance pre-order signals.
7. **Closed-Loop Waste Analytics:** Continuous feedback loop: PREDICT -> PREPARE -> SELL -> MEASURE -> LEARN.
8. **Academic Calendar & Weather Feed:** Automated detection of exam footfall dips, cultural event surges, and heatwave drink spikes.
9. **What-If Scenario Simulation Engine:** Strategy Design Pattern modeling event footfall surges, attendance drops, and menu price elasticity.
10. **Multi-Canteen Load Balancer:** Real-time capacity monitoring and express-pickup routing to relieve overcrowded counters.

---

## System Architecture & Design Patterns

* **Strategy Design Pattern:** Implemented in `SimulationEngine` (`EVENT_SURGE`, `ATTENDANCE_DROP`, `PRICE_CHANGE`) to evaluate operational scenarios without modifying core business services.
* **Generic Repository (DAO) Pattern:** `CrudRepository<T, ID>` decouples service logic from persistence engines.
* **Thread-Safe Connection Pooling:** Custom `DatabaseConnectionPool` utilizing `ArrayBlockingQueue<Connection>` (pool size: 10) with automatic hybrid in-memory fallback.
* **Asynchronous Concurrency:** Java 21 `ExecutorService` (4 worker threads) managing background cooking and delivery status transitions without UI freezing.
* **Polyglot Interoperability:** 70% Java 21 core services, 30% Kotlin immutable data classes (DTOs) for mobile payloads, and Apache Spark 3.5 / Scala historical stream processing.

---

## Database Architecture (MySQL 8.4 LTS)

The database schema is fully normalized to Third Normal Form (3NF) and contains 12 relational tables:
* `canteens`: Canteen dining facilities, capacity limits, and real-time occupancy.
* `users`: Multi-role accounts (Customer, Worker, Admin) and Food Wallet balances.
* `food_items`: Menu catalogue with margin, preparation time, and Food Performance Scores.
* `inventory`: Raw warehouse ingredients, safety stock thresholds, and expiry tracking.
* `food_recipe_bom`: Bill of Materials mapping dishes to raw material units.
* `customer_favourites`: Personal ordering history, favourite days, and smart beverage combos.
* `orders`: Header records with delivery mode, status, and payment method.
* `order_items`: Line-item dish breakdowns and captured unit prices.
* `financial_ledger`: Double-entry transaction auditing and daily net margins.
* `waste_logs`: Attribution logs (Overproduction, Spoilage, Low Demand).
* `customer_feedback`: Aspect sentiment scores (Taste, Spiciness, Quantity).
* `campus_events`: Academic calendar dates, cultural fests, and surge multipliers.

---

## Quick Start & Setup Guide

### Prerequisites
* Java Development Kit (JDK) 21 LTS
* IntelliJ IDEA Ultimate (or Community)
* MySQL Community Server 8.4 LTS
* MySQL Workbench 8.0

### Step 1: Initialize Database in MySQL Workbench 8.0
1. Open MySQL Workbench 8.0 and connect to your local MySQL instance (`localhost:3306`).
2. Open `sql/mealsynx_mysql84_workbench.sql`.
3. Click the lightning bolt icon (⚡ Execute) to create the `mealsynx` database, 12 relational tables, foreign key constraints, and campus seed records.

### Step 2: Open Project in IntelliJ IDEA
1. Open IntelliJ IDEA and select **Open** -> choose the extracted `MealSynx` folder.
2. Maven automatically resolves dependencies (`mysql-connector-j:8.4.0`, `kotlin-stdlib:1.9.24`, `kotlinx-coroutines-core:1.8.1`).
3. Open `src/main/resources/application.properties` and verify your local MySQL root password:
   ```properties
   db.url=jdbc:mysql://localhost:3306/mealsynx?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   db.username=root
   db.password=YOUR_MYSQL_ROOT_PASSWORD
   ```

### Step 3: Run the Application
1. Open `src/main/java/com/mealsynx/MealSynxApplication.java`.
2. Click the green Run (▶) button next to `main()`.
3. The platform will boot up, verify live JDBC connectivity with MySQL 8.4 LTS, and execute all 10 modules sequentially with live simulation telemetry output in the console.

---

## Academic Context & References

* **Course:** Advanced Object Oriented Programming (21CSC206P)
* **Department:** Computer Science & Engineering (Emerging Technologies)
* **Specialization:** Big Data Analytics
* **Institution:** SRM Institute of Science and Technology, Vadapalani Campus, Chennai
* **Faculty Supervisor:** Dr. P. Chitra, M.E., Ph.D. (Professor & Head, Dept. of CSE – Emerging Tech)



## Project Authors & Contributions

* **D. Raghuram** (Register Number: `RA2511027040004`)
  * **Contributions:** Strategy Pattern Simulation Engine, Core Java 21 Services, MySQL 8.4 Relational Architecture & Big Data (Spark/Kafka) Pipeline.
* **B. Likith Adhithya** (Register Number: `RA2511027040022`)
  * **Contributions:** Version Control (Git), Mobile Application Architecture (Kotlin), User Interface (UI) Design & Full-Stack Implementation Support.

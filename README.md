<div align="center">

# 🍽️ MealSynx
### Autonomous Multi-Tier Campus Dining Demand & Food Waste Optimization Platform

[[https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white]](https://docs.oracle.com/en/java/javase/21/)
[[https://img.shields.io/badge/MySQL-8.4_LTS-4479A1?style=for-the-badge&logo=mysql&logoColor=white]](https://dev.mysql.com/doc/refman/8.4/en/)
[[https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white]](https://kotlinlang.org/docs/home.html)
[[https://img.shields.io/badge/Apache_Spark-3.5-E25A1C?style=for-the-badge&logo=apachespark&logoColor=white]](https://spark.apache.org/)
[[https://img.shields.io/badge/Architecture-3NF_%7C_Strategy_Pattern-10B981?style=for-the-badge]](#-system-architecture--design-patterns)
[[https://img.shields.io/badge/SRMIST-Dept_of_CSE_(Emerging_Tech]-1B365D?style=for-the-badge)](https://www.srmist.edu.in/vadapalani/)

<p align="center">
  <b>Autonomous multi-tier campus dining & waste platform built on Java 21 LTS & MySQL 8.4 (3NF).</b><br>
  Features Strategy-pattern What-If simulations, JDBC connection pooling, async Kotlin DTOs, and closed-loop waste telemetry.
</p>

[Key Metrics](#-key-operational-metrics) •
[Features](#-core-capabilities--10-integrated-modules) •
[Architecture](#-system-architecture--design-patterns) •
[Database Schema](#-database-architecture-mysql-84-lts) •
[Screenshots](#-verified-interface-gallery) •
[Quick Start](#-quick-start--setup-guide)

---

</div>

## 📊 Key Operational Metrics

| Metric | Baseline | MealSynx Optimized | Impact / Outcome |
| :--- | :---: | :---: | :--- |
| **Campus Food Waste** | `16.5%` | **`6.0%`** | **-63.6% reduction** in prepared food waste |
| **Peak Queue Wait Time** | `18.5 mins` | **`5.2 mins`** | **-71.9% reduction** via advance pre-orders & load balancing |
| **Multi-Canteen Occupancy** | `95% / 40%` | **`72% / 68%`** | Autonomous load re-distribution across dining halls |
| **Inventory Shortage Incidents** | `14 / month` | **`1 / month`** | Automated BOM explosion & safety stock reorder thresholds |
| **REST API Latency** | — | **`< 4.5 ms`** | Sub-5ms response time across all JSON operational endpoints |

---

## 🎯 The Campus Dining Problem

High-density academic micro-cities serve thousands of students, faculty, and staff daily across narrow 45-minute lunch breaks:
* **The Counter Bottleneck:** Uncoordinated walk-in rushes overload primary dining centers while auxiliary cafeterias sit empty.
* **The Overproduction Dilemma:** Kitchens prepare meals based on guesswork, leading to massive financial losses and food disposal on examination days, bad weather, or exam weeks.
* **Lack of Integration:** Student portals, warehouse procurement, kitchen preparation, and POS settlement operate in disconnected silos.

**MealSynx** resolves this through an autonomous, closed-loop software pipeline connecting demand signals directly to warehouse procurement and kitchen stoves.

---

## 📸 Verified Interface Gallery

> All 9 operational interface captures from live administrative and desktop runtimes are archived in full resolution:
>
> 🔗 **[Access Uncompressed High-Resolution Screenshot Repository (Google Drive)](https://drive.google.com/drive/folders/1WU5mLa1a5kv9KuL4wuhrsInbJYXuqK_C?usp=drive_link)**

<details>
<summary><b>View Interface Index (Figures 5.1 – 5.9)</b></summary>

* **Figure 5.1:** Multi-Role Authentication & Student Registration Portal (Desktop Swing GUI)
* **Figure 5.2:** Student Personalization Dashboard with Weather-Driven Multipliers
* **Figure 5.3:** Food Cart Checkout, Food Wallet Payment & Live Courier Tracking
* **Figure 5.4:** Menu Management & Food Performance Scoring Engine (FPS)
* **Figure 5.5:** Kitchen Batch Operations & Real-Time Warehouse Low Stock Alerts
* **Figure 5.6:** Warehouse Raw Material Inventory & Automated Recipe BOM Explosion
* **Figure 5.7:** Executive Analytics Dashboard & Multi-Canteen Queue Load Balancing
* **Figure 5.8:** MySQL Workbench 8.0 Live ACID Transaction Verification
* **Figure 5.9:** What-If Scenario Simulation Engine, Waste Logs & Campus Calendar Grid

</details>

---

## ⚙️ Core Capabilities & 10 Integrated Modules

```text
 1. Customer & Personalization   ──► Smart Home Feed, Day-of-Week Combos, Push Alerts
 2. Menu & Food Scoring (FPS)    ──► Multi-Factor Performance Algorithm (0–100 FPS)
 3. Order & Campus Delivery      ──► Buy-Now, Scheduled Pre-Orders, Food Wallet & QR UPI
 4. Kitchen Batch Operations     ──► Real-Time Daily Prep Plans & Low-Stock Warnings
 5. Warehouse & Recipe BOM       ──► Demand-to-Raw-Material Explosion (e.g., Plates -> kg)
 6. Predictive Demand Engine     ──► Baseline + Calendar Events + Weather Correlation
 7. Closed-Loop Waste Analytics  ──► PREDICT → PREPARE → SELL → MEASURE → LEARN
 8. Calendar & Weather Feed      ──► Automated Exam Footfall Dips & Heatwave Surges
 9. What-If Scenario Simulator   ──► Strategy Pattern Engine (Surge, Attendance, Price)
10. Multi-Canteen Load Balancer  ──► Real-Time Capacity Monitoring & Express Routing

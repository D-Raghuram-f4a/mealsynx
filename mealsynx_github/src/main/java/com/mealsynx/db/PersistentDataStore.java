package com.mealsynx.db;

import com.mealsynx.dao.FeedbackDAO;
import com.mealsynx.model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class PersistentDataStore {
    public static class DataSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        public final Map<String, Canteen> canteens = new LinkedHashMap<>();
        public final Map<String, Customer> customers = new LinkedHashMap<>();
        public final List<Worker> workers = new ArrayList<>();
        public final List<Admin> admins = new ArrayList<>();
        public final Map<String, FoodItem> menuItems = new LinkedHashMap<>();
        public final Map<String, Order> orders = new LinkedHashMap<>();
        public final Map<String, IngredientStock> inventory = new LinkedHashMap<>();
        public final List<String> paymentTransactions = new ArrayList<>();
        public final List<FeedbackDAO.FeedbackEntry> feedbacks = new ArrayList<>();
        public final List<CampusEvent> events = new ArrayList<>();
        public final List<WasteRecord> wasteRecords = new ArrayList<>();
        public final List<SupportTicket> supportTickets = new ArrayList<>();
        public final List<SimulationRequest> simulationRequests = new ArrayList<>();
        public WeatherSnapshot currentWeather;
    }

    private static final String LOCAL_STORE_FILE = "mealsynx_persistent_store.dat";
    private static final String HOME_STORE_FILE = System.getProperty("user.home") + File.separator + ".mealsynx_persistent_store_v6.dat";

    private static PersistentDataStore instance;
    private DataSnapshot snapshot;

    private PersistentDataStore() {
        loadFromDiskOrSeed();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveToDisk));
    }

    public static synchronized PersistentDataStore getInstance() {
        if (instance == null) {
            instance = new PersistentDataStore();
        }
        return instance;
    }

    public DataSnapshot data() {
        return snapshot;
    }

    private void loadFromDiskOrSeed() {
        File localFile = new File(LOCAL_STORE_FILE);
        File homeFile = new File(HOME_STORE_FILE);
        File target = localFile.exists() ? localFile : (homeFile.exists() ? homeFile : null);

        if (target != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(target))) {
                Object obj = ois.readObject();
                if (obj instanceof DataSnapshot loaded && !loaded.menuItems.isEmpty()) {
                    this.snapshot = loaded;
                    return;
                }
            } catch (Exception ignored) {}
        }
        this.snapshot = createInitialRichDataset();
        saveToDisk();
    }

    public synchronized void saveToDisk() {
        if (snapshot == null) return;
        writeSnapshotToFile(new File(LOCAL_STORE_FILE));
        writeSnapshotToFile(new File(HOME_STORE_FILE));
    }

    private void writeSnapshotToFile(File f) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(snapshot);
            oos.flush();
        } catch (Exception ignored) {}
    }

    public synchronized void resetToFactoryDataset() {
        this.snapshot = createInitialRichDataset();
        saveToDisk();
    }

    private DataSnapshot createInitialRichDataset() {
        DataSnapshot ds = new DataSnapshot();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. 3 Specialized Canteens
        Canteen c1 = new Canteen("CAN-1", "Canteen 1 — Annapoorna Pure Veg", "Pure Veg",
                "Academic Block A — Ground Floor", 450, 4, 46850.00, 16400.00, 10200.00, 4300.00);
        c1.addAdminUpdate("🔔 [Admin Advisory] High morning & lunch tiffin demand at Block A. Maintain extra Sambar, Ghee Roast & Filter Coffee batches.");

        Canteen c2 = new Canteen("CAN-2", "Canteen 2 — Spice Route Non-Veg", "Purely Non-Veg",
                "Central Dining Plaza — Block B", 500, 7, 68400.00, 28200.00, 12800.00, 5900.00);
        c2.addAdminUpdate("🔔 [Admin Advisory] Canteen 2 queue is currently Busy. Open Express Pre-Order counter for Ambur Chicken Dum Biryani.");

        Canteen c3 = new Canteen("CAN-3", "Canteen 3 — Campus Grills & Fast Food", "Fast Food & Grills (Mixed)",
                "Student Activity Center — Block C", 400, 2, 39600.00, 16100.00, 9400.00, 4800.00);
        c3.addAdminUpdate("🔔 [Admin Advisory] Lab & Sports break crowd expected 3:30 PM – 5:30 PM. Prep BBQ Grill Wraps, Burgers & Cold Frappes.");

        ds.canteens.put(c1.getCanteenId(), c1);
        ds.canteens.put(c2.getCanteenId(), c2);
        ds.canteens.put(c3.getCanteenId(), c3);

        // 2. 5 Students + 3 Workers + 1 Admin
        Customer s1 = new Customer("STU-001", "Raghuram", "D Raghuram", "RA2511027040004", "raghuram@campus.edu",
                "Raghuram123*", 640.00, "Non-Veg Biryani & Grills", "12:30 PM – 1:30 PM", 135.00);
        s1.addFavouriteFood("FD-201");
        s1.addFavouriteFood("FD-301");
        s1.addFavouriteFood("FD-105");
        s1.addFavouriteFood("FD-205");
        s1.recordFoodOrdered("FD-201", "Ambur Chicken Dum Biryani", 14);
        s1.recordFoodOrdered("FD-301", "Smoky BBQ Chicken Grill Wrap", 9);
        s1.recordFoodOrdered("FD-105", "Fresh Sweet Lime (Mosambi) Juice", 11);
        s1.recordFoodOrdered("FD-102", "Ghee Roast Masala Dosa", 6);
        s1.setDaySpecialtyPreference("FRIDAY", "Ambur Chicken Dum Biryani");
        s1.setCompanionDrinkPairing("Ambur Chicken Dum Biryani", "Fresh Sweet Lime (Mosambi) Juice");
        s1.pushNotification("🔔 Welcome Raghuram! Your favourites, orders, and reviews are permanently saved across sessions.");

        Customer s2 = new Customer("STU-002", "Likith", "B Likith Adhithya", "RA2511027040022", "likith@campus.edu",
                "Likith123*", 720.00, "Fast Food & Grills", "1:00 PM – 2:00 PM", 125.00);
        s2.addFavouriteFood("FD-301");
        s2.addFavouriteFood("FD-303");
        s2.addFavouriteFood("FD-305");
        s2.recordFoodOrdered("FD-301", "Smoky BBQ Chicken Grill Wrap", 11);
        s2.recordFoodOrdered("FD-302", "Schezwan Veg Hakka Noodles", 7);
        s2.recordFoodOrdered("FD-305", "Cold Belgian Chocolate Frappe", 10);
        s2.pushNotification("🔔 Hi Likith! Canteen 3 Grills & Fast Food is currently FREE with fast prep.");

        Customer s3 = new Customer("STU-003", "Student A", "Aarav Krishnan (Student A)", "RA2511027040031", "studenta@campus.edu",
                "Student A123*", 480.00, "Pure Veg South Indian", "12:15 PM – 1:00 PM", 95.00);
        s3.addFavouriteFood("FD-103");
        s3.addFavouriteFood("FD-106");
        s3.addFavouriteFood("FD-107");
        s3.recordFoodOrdered("FD-103", "Executive South Indian Veg Meals", 15);
        s3.recordFoodOrdered("FD-101", "Mini Sambar Idli Tiffin (12 pcs)", 10);
        s3.recordFoodOrdered("FD-106", "Kumbakonam Degree Filter Coffee", 13);
        s3.pushNotification("🔔 Hi Student A! Fresh Pure Veg Meals & Mushroom Biryani ready at Canteen 1.");

        Customer s4 = new Customer("STU-004", "Student B", "Divya Nair (Student B)", "RA2511027040045", "studentb@campus.edu",
                "Student B123*", 550.00, "Grills & Beverages", "3:30 PM – 4:30 PM", 85.00);
        s4.addFavouriteFood("FD-104");
        s4.addFavouriteFood("FD-304");
        s4.addFavouriteFood("FD-109");
        s4.recordFoodOrdered("FD-104", "Paneer Butter Masala & 2 Parotta Bowl", 9);
        s4.recordFoodOrdered("FD-304", "Loaded Peri-Peri Cheese Fries", 12);
        s4.pushNotification("🔔 Hi Student B! Check out today's weather-adjusted cold & hot specials.");

        Customer s5 = new Customer("STU-005", "Student C", "Karthik Raja (Student C)", "RA2511027040058", "studentc@campus.edu",
                "Student C123*", 810.00, "Non-Veg Curries & Biryani", "12:45 PM – 1:45 PM", 150.00);
        s5.addFavouriteFood("FD-202");
        s5.addFavouriteFood("FD-205");
        s5.addFavouriteFood("FD-207");
        s5.recordFoodOrdered("FD-202", "Chettinad Pepper Chicken Meals", 10);
        s5.recordFoodOrdered("FD-203", "Madurai Egg Kothu Parotta", 8);
        s5.recordFoodOrdered("FD-207", "Hyderabadi Mutton Ghee Roast", 5);
        s5.pushNotification("🔔 Hi Student C! Your Food Wallet balance is ₹810.00.");

        ds.customers.put(s1.getUserId(), s1);
        ds.customers.put(s2.getUserId(), s2);
        ds.customers.put(s3.getUserId(), s3);
        ds.customers.put(s4.getUserId(), s4);
        ds.customers.put(s5.getUserId(), s5);

        Worker w1 = new Worker("WRK-101", "Cant1workA", "Murugan S (Veg Kitchen Lead)", "EMP-CAN1-01",
                "cant1worka@mealsynx.edu", "Cant1workA123*", "CAN-1", "07:00 AM – 03:00 PM", "Pure Veg South & North Indian");
        w1.pushNotification("🔔 [Warehouse Alert] Cow Ghee in Canteen 1 is approaching safety stock (4.5 L left).");
        Worker w2 = new Worker("WRK-102", "Cant2workA", "Senthil Pandian (Non-Veg Head Chef)", "EMP-CAN2-01",
                "cant2worka@mealsynx.edu", "Cant2workA123*", "CAN-2", "08:30 AM – 04:30 PM", "Dum Biryani & Coastal Non-Veg");
        w2.pushNotification("🔔 [Expiry Alert] Fresh Broiler Chicken batch in Canteen 2 expires in 2 days!");
        Worker w3 = new Worker("WRK-103", "Cant3workA", "Ramesh Babu (Grill & Fast Food Master)", "EMP-CAN3-01",
                "cant3worka@mealsynx.edu", "Cant3workA123*", "CAN-3", "10:00 AM – 06:00 PM", "Charcoal Grills, Burgers & Wok");
        w3.pushNotification("🔔 [Warehouse Alert] Brioche Burger Buns expiring in 2 days (Quality Warning).");
        ds.workers.add(w1);
        ds.workers.add(w2);
        ds.workers.add(w3);

        Admin adm = new Admin("ADM-001", "admin", "Campus Food Intelligence Director", "ADM-CENTRAL-01",
                "admin@mealsynx.edu", "admin123*", "Central Campus Dining & Big Data Operations");
        adm.pushNotification("🔔 [Worker Sim Request] Cant2workA requested a What-If Simulation for Canteen 2 Friday Biryani surge.");
        ds.admins.add(adm);

        // 3. 30 Rich Menu Items (10 in CAN-1 Pure Veg, 10 in CAN-2 Pure Non-Veg, 10 in CAN-3 Fast Food & Grills)
        Object[][] menuSeed = {
            // CAN-1: Pure Veg (10 Items)
            {"FD-101", "Mini Sambar Idli Tiffin (12 pcs)", "Breakfast / Tiffin", 45.0, 20.0, 6, "290 kcal | 9g Protein | Pure Veg | Ghee Drizzle", "CAN-1", 210, 72, 2.5, 1, 4.8, 36, "ING-101", 0.12},
            {"FD-102", "Ghee Roast Masala Dosa", "Breakfast / Tiffin", 65.0, 28.0, 9, "410 kcal | 10g Protein | Pure Veg | Fermented Crepe", "CAN-1", 195, 65, 3.0, 2, 4.7, 31, "ING-101", 0.15},
            {"FD-103", "Executive South Indian Veg Meals", "Main Course (Veg)", 85.0, 38.0, 8, "620 kcal | 18g Protein | Rice, Sambar, Rasam, Kootu, Curd", "CAN-1", 265, 98, 4.0, 2, 4.9, 52, "ING-102", 0.15},
            {"FD-104", "Paneer Butter Masala & 2 Parotta Bowl", "Main Course (Veg)", 110.0, 52.0, 12, "680 kcal | 22g Protein | Fresh Malai Paneer in Cashew Gravy", "CAN-1", 175, 58, 4.8, 3, 4.7, 33, "ING-104", 0.10},
            {"FD-105", "Fresh Sweet Lime (Mosambi) Juice", "Cold Beverages", 35.0, 14.0, 4, "110 kcal | Vitamin C Boost | No Artificial Sugar", "CAN-1", 190, 81, 2.0, 1, 4.8, 42, "ING-105", 0.20},
            {"FD-106", "Kumbakonam Degree Filter Coffee", "Hot Beverages", 25.0, 9.0, 3, "95 kcal | Chicory Arabica Decoction & Farm Milk", "CAN-1", 240, 92, 1.5, 1, 4.9, 56, "ING-103", 0.01},
            {"FD-107", "Kongu Mushroom Dum Biryani (Pure Veg)", "Biryani & Rice (Veg)", 105.0, 48.0, 11, "560 kcal | 16g Protein | Seeraga Samba & Button Mushrooms", "CAN-1", 180, 64, 3.8, 2, 4.8, 29, "ING-102", 0.13},
            {"FD-108", "Chole Bhature & Pickled Onion Platter", "North Indian Veg", 80.0, 35.0, 10, "590 kcal | 17g Protein | Spiced Chickpea Curry & 2 Bhature", "CAN-1", 160, 53, 4.2, 2, 4.6, 27, "ING-104", 0.06},
            {"FD-109", "Chilled Mango Lassi & Malai Cup", "Cold Beverages & Desserts", 45.0, 18.0, 4, "210 kcal | 8g Protein | Alphonso Mango & Probiotic Curd", "CAN-1", 170, 69, 2.2, 1, 4.8, 38, "ING-105", 0.15},
            {"FD-110", "Crispy Medhu Vada & Coconut Chutney (3 pcs)", "Hot Snacks (Veg)", 40.0, 16.0, 6, "270 kcal | 10g Protein | Golden Urad Dal Donuts", "CAN-1", 200, 74, 2.8, 1, 4.7, 35, "ING-101", 0.10},

            // CAN-2: Purely Non-Veg (10 Items)
            {"FD-201", "Ambur Chicken Dum Biryani", "Biryani & Rice (Non-Veg)", 130.0, 64.0, 14, "710 kcal | 32g Protein | Seeraga Samba Rice & Chicken + Egg", "CAN-2", 315, 124, 5.2, 3, 4.9, 68, "ING-202", 0.14},
            {"FD-202", "Chettinad Pepper Chicken Meals", "Main Course (Non-Veg)", 140.0, 68.0, 15, "740 kcal | 35g Protein | Roasted Black Pepper Chicken & Rice", "CAN-2", 220, 78, 5.8, 3, 4.8, 44, "ING-202", 0.15},
            {"FD-203", "Madurai Egg Kothu Parotta", "Tiffin / Dinner (Non-Veg)", 95.0, 42.0, 11, "590 kcal | 21g Protein | Minced Flaky Parotta with Double Egg", "CAN-2", 185, 66, 4.2, 2, 4.7, 39, "ING-203", 2.0},
            {"FD-204", "Malabar Seer Fish Curry & Steamed Rice", "Main Course (Non-Veg)", 155.0, 78.0, 16, "660 kcal | 34g Protein | Omega-3 Seer Fish in Coconut Curry", "CAN-2", 140, 45, 8.5, 4, 4.5, 25, "ING-201", 0.13},
            {"FD-205", "Crispy Chicken 65 Starter (Boneless)", "Hot Starters (Non-Veg)", 105.0, 48.0, 10, "460 kcal | 28g Protein | Curry Leaf & Red Chilli Chicken", "CAN-2", 195, 75, 3.8, 2, 4.8, 41, "ING-202", 0.13},
            {"FD-206", "Chilled Badam & Rose Milk", "Cold Beverages", 40.0, 16.0, 3, "180 kcal | 6g Protein | Cooling Almond & Sabja Seed Milk", "CAN-2", 180, 62, 2.5, 1, 4.7, 30, "ING-204", 0.01},
            {"FD-207", "Hyderabadi Mutton Ghee Roast & Parotta", "Main Course (Non-Veg)", 175.0, 92.0, 18, "780 kcal | 38g Protein | Slow-Cooked Tender Lamb in Ghee Masala", "CAN-2", 150, 52, 4.5, 4, 4.9, 34, "ING-202", 0.16},
            {"FD-208", "Prawn Pepper Masala Fry", "Hot Starters (Non-Veg)", 150.0, 74.0, 12, "420 kcal | 31g Protein | Coastal Tiger Prawns Tossed in Garlic Pepper", "CAN-2", 130, 48, 5.0, 3, 4.7, 26, "ING-204", 0.03},
            {"FD-209", "Double Egg Podimas & Kal Dosa Set", "Breakfast / Tiffin (Non-Veg)", 75.0, 32.0, 8, "440 kcal | 19g Protein | Spiced Scrambled Eggs with 2 Kal Dosas", "CAN-2", 175, 60, 3.0, 1, 4.8, 32, "ING-203", 2.0},
            {"FD-210", "Hot Kozhi Rasam & Pepper Soup Cup", "Hot Beverages & Soups", 45.0, 18.0, 5, "140 kcal | 11g Protein | Immunity-Boosting Spiced Chicken Broth", "CAN-2", 165, 57, 2.0, 1, 4.8, 28, "ING-202", 0.05},

            // CAN-3: Fast Food & Grills (10 Items)
            {"FD-301", "Smoky BBQ Chicken Grill Wrap", "Grills & Wraps", 115.0, 52.0, 10, "530 kcal | 29g Protein | Charcoal Grilled Chicken in Tortilla Roll", "CAN-3", 230, 88, 4.0, 2, 4.8, 48, "ING-301", 0.12},
            {"FD-302", "Schezwan Veg Hakka Noodles", "Fast Food / Wok", 90.0, 40.0, 9, "510 kcal | 11g Protein | Wok-Tossed Noodles with Bell Peppers", "CAN-3", 160, 51, 11.5, 3, 4.2, 26, "ING-302", 0.14},
            {"FD-303", "Peri-Peri Grilled Paneer Burger", "Grills & Burgers", 105.0, 46.0, 10, "560 kcal | 20g Protein | Flame-Grilled Paneer Steak & Cheddar", "CAN-3", 195, 70, 4.5, 2, 4.7, 36, "ING-303", 1.0},
            {"FD-304", "Loaded Peri-Peri Cheese Fries", "Hot Snacks & Sides", 80.0, 32.0, 7, "440 kcal | 9g Protein | Crispy Golden Potato Fries with Liquid Cheese", "CAN-3", 210, 76, 3.5, 1, 4.6, 39, "ING-302", 0.10},
            {"FD-305", "Cold Belgian Chocolate Frappe", "Cold Beverages", 75.0, 28.0, 5, "310 kcal | 7g Protein | Iced Espresso Blended with Dark Cocoa", "CAN-3", 200, 83, 2.8, 1, 4.9, 46, "ING-304", 0.18},
            {"FD-306", "Hot Ginger Masala Chai & 2 Punjabi Samosas", "Hot Beverages & Snacks", 45.0, 18.0, 5, "320 kcal | 7g Protein | Fresh Cardamom Ginger Tea + 2 Samosas", "CAN-3", 220, 85, 3.2, 1, 4.8, 43, "ING-302", 0.08},
            {"FD-307", "Charcoal Al-Faham Grilled Chicken (Quarter)", "Grills & Barbeque", 145.0, 68.0, 14, "610 kcal | 39g Protein | Arabian Spiced Charcoal Grilled Chicken + Kuboos", "CAN-3", 215, 79, 4.0, 3, 4.9, 45, "ING-301", 0.18},
            {"FD-308", "Crispy Zinger Chicken Fillet Burger", "Grills & Burgers", 125.0, 56.0, 11, "590 kcal | 27g Protein | Crunchy Chicken Breast, Chipotle Mayo & Brioche", "CAN-3", 185, 68, 3.8, 2, 4.8, 37, "ING-303", 1.0},
            {"FD-309", "Grilled Corn & Jalapeno Cheese Sandwich", "Grills & Sandwiches", 70.0, 29.0, 7, "390 kcal | 13g Protein | Toasted Sourdough with Sweet Corn & Mozzarella", "CAN-3", 175, 63, 3.0, 1, 4.7, 32, "ING-303", 1.0},
            {"FD-310", "Chilled Blue Curacao Mint Mojito", "Cold Beverages", 55.0, 20.0, 4, "130 kcal | Fresh Mint Leaves, Lime Wedges & Sparkling Cooler", "CAN-3", 180, 71, 2.2, 1, 4.8, 34, "ING-304", 0.15}
        };

        for (Object[] row : menuSeed) {
            FoodItem fi = new FoodItem(
                    (String) row[0], (String) row[1], (String) row[2],
                    (Double) row[3], (Double) row[4], (Integer) row[5],
                    true, (String) row[6], (String) row[7],
                    (Integer) row[8], (Integer) row[9], (Double) row[10],
                    (Integer) row[11], (Double) row[12], (Integer) row[13]
            );
            fi.addIngredientRequirement((String) row[14], (Double) row[15]);
            ds.menuItems.put(fi.getFoodId(), fi);
        }

        // 4. Warehouse Inventory across CAN-1, CAN-2, CAN-3
        ds.inventory.put("ING-101", new IngredientStock("ING-101", "Fermented Idli/Dosa Batter", "Grains & Batter", "CAN-1",
                today.minusDays(2), today.plusDays(2), 28.0, 10.0, 34.0, "kg", 45.0));
        ds.inventory.put("ING-102", new IngredientStock("ING-102", "Sona Masoori & Seeraga Rice", "Grains", "CAN-1",
                today.minusDays(10), today.plusDays(75), 48.0, 15.0, 40.0, "kg", 58.0));
        ds.inventory.put("ING-103", new IngredientStock("ING-103", "Pure Cow Ghee & Filter Coffee Powder", "Dairy & Decoction", "CAN-1",
                today.minusDays(14), today.plusDays(40), 4.5, 5.0, 6.5, "L", 520.0));
        ds.inventory.put("ING-104", new IngredientStock("ING-104", "Fresh Malai Paneer & Mushrooms", "Dairy & Produce", "CAN-1",
                today.minusDays(2), today.plusDays(2), 11.5, 5.0, 16.0, "kg", 310.0));
        ds.inventory.put("ING-105", new IngredientStock("ING-105", "Fresh Mosambi, Mangoes & Lemons", "Produce", "CAN-1",
                today.minusDays(3), today.plusDays(5), 26.0, 8.0, 22.0, "kg", 65.0));

        ds.inventory.put("ING-201", new IngredientStock("ING-201", "Aged Seeraga Samba Biryani Rice", "Grains", "CAN-2",
                today.minusDays(9), today.plusDays(60), 27.0, 12.0, 38.0, "kg", 95.0));
        ds.inventory.put("ING-202", new IngredientStock("ING-202", "Fresh Halal Broiler Chicken & Mutton", "Poultry & Meat", "CAN-2",
                today.minusDays(1), today.plusDays(2), 31.0, 10.0, 44.0, "kg", 215.0));
        ds.inventory.put("ING-203", new IngredientStock("ING-203", "Farm Fresh Eggs (Crates)", "Poultry", "CAN-2",
                today.minusDays(4), today.plusDays(9), 190.0, 60.0, 160.0, "pcs", 6.5));
        ds.inventory.put("ING-204", new IngredientStock("ING-204", "Cold-Pressed Groundnut Oil & Spices", "Cooking Oils", "CAN-2",
                today.minusDays(12), today.plusDays(50), 9.5, 6.0, 14.0, "L", 165.0));

        ds.inventory.put("ING-301", new IngredientStock("ING-301", "BBQ & Al-Faham Marinated Chicken", "Poultry Grills", "CAN-3",
                today.minusDays(2), today.plusDays(2), 14.5, 6.0, 22.0, "kg", 240.0));
        ds.inventory.put("ING-302", new IngredientStock("ING-302", "Hakka Noodles, Fries & Tortilla Wraps", "Bakery & Wok", "CAN-3",
                today.minusDays(6), today.plusDays(25), 28.0, 8.0, 22.0, "kg", 85.0));
        ds.inventory.put("ING-303", new IngredientStock("ING-303", "Brioche Burger Buns & Cheddar Slices", "Bakery & Dairy", "CAN-3",
                today.minusDays(2), today.plusDays(2), 32.0, 20.0, 50.0, "packs", 75.0));
        ds.inventory.put("ING-304", new IngredientStock("ING-304", "Belgian Cocoa, Mojito Syrup & Espresso", "Beverages", "CAN-3",
                today.minusDays(11), today.plusDays(45), 18.0, 5.0, 15.0, "L", 190.0));

        // 5. Orders
        Order o1 = new Order("ORD-1001", "STU-001", "Raghuram", "CAN-2",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 2 Counter",
                "Immediate", "FOOD_WALLET", false, now.minusDays(2).withHour(12).withMinute(45));
        o1.addItem("FD-201", "Ambur Chicken Dum Biryani", 1, 130.0, "Extra raita & boiled egg");
        o1.addItem("FD-206", "Chilled Badam & Rose Milk", 1, 40.0, "Less ice");
        o1.setStatus(Order.OrderStatus.COLLECTED);

        Order o2 = new Order("ORD-1002", "STU-001", "Raghuram", "CAN-3",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.CAMPUS_DELIVERY, "Lab 1",
                "Immediate", "FOOD_WALLET", false, now.minusDays(1).withHour(16).withMinute(15));
        o2.addItem("FD-301", "Smoky BBQ Chicken Grill Wrap", 1, 115.0, "Extra smoky BBQ dip");
        o2.addItem("FD-305", "Cold Belgian Chocolate Frappe", 1, 75.0, "Standard Prep");
        o2.setStatus(Order.OrderStatus.DELIVERED);

        Order o3 = new Order("ORD-1003", "STU-001", "Raghuram", "CAN-1",
                Order.OrderType.PRE_ORDER, Order.DeliveryMode.PICKUP, "Canteen 1 Counter",
                "08:45 AM", "FOOD_WALLET", false, now.minusHours(5));
        o3.addItem("FD-102", "Ghee Roast Masala Dosa", 1, 65.0, "Crispy roast, extra chutney");
        o3.addItem("FD-105", "Fresh Sweet Lime (Mosambi) Juice", 1, 35.0, "No ice");
        o3.setStatus(Order.OrderStatus.COLLECTED);

        Order o4 = new Order("ORD-1004", "STU-001", "Raghuram", "CAN-2",
                Order.OrderType.PRE_ORDER, Order.DeliveryMode.CAMPUS_DELIVERY, "Library",
                "01:15 PM", "FOOD_WALLET", false, now.minusMinutes(18));
        o4.addItem("FD-201", "Ambur Chicken Dum Biryani", 2, 130.0, "Medium spice, extra onion salad");
        o4.addItem("FD-205", "Crispy Chicken 65 Starter (Boneless)", 1, 105.0, "Crispy & hot");
        o4.setStatus(Order.OrderStatus.PREPARING);

        Order o5 = new Order("ORD-1005", "STU-002", "Likith", "CAN-3",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.CAMPUS_DELIVERY, "Floor 1 Delivery Point",
                "Immediate", "FOOD_WALLET", false, now.minusDays(1).withHour(13).withMinute(10));
        o5.addItem("FD-301", "Smoky BBQ Chicken Grill Wrap", 1, 115.0, "Extra cheese slice");
        o5.addItem("FD-302", "Schezwan Veg Hakka Noodles", 1, 90.0, "Less oil");
        o5.setStatus(Order.OrderStatus.DELIVERED);

        Order o6 = new Order("ORD-1006", "STU-002", "Likith", "CAN-3",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 3 Counter",
                "Immediate", "PAY_ON_DELIVERY", false, now.minusMinutes(12));
        o6.addItem("FD-303", "Peri-Peri Grilled Paneer Burger", 1, 105.0, "Extra jalapenos");
        o6.addItem("FD-305", "Cold Belgian Chocolate Frappe", 1, 75.0, "Less sugar");
        o6.setStatus(Order.OrderStatus.PREPARING);

        Order o7 = new Order("ORD-1007", "STU-003", "Student A", "CAN-1",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 1 Counter",
                "Immediate", "FOOD_WALLET", false, now.minusDays(1).withHour(12).withMinute(30));
        o7.addItem("FD-103", "Executive South Indian Veg Meals", 1, 85.0, "Extra sambar & papad");
        o7.addItem("FD-106", "Kumbakonam Degree Filter Coffee", 1, 25.0, "Strong decoction");
        o7.setStatus(Order.OrderStatus.COLLECTED);

        Order o8 = new Order("ORD-1008", "STU-003", "Student A", "CAN-1",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.CAMPUS_DELIVERY, "Lab 1",
                "Immediate", "FOOD_WALLET", false, now.minusMinutes(22));
        o8.addItem("FD-101", "Mini Sambar Idli Tiffin (12 pcs)", 1, 45.0, "Hot sambar immersed");
        o8.addItem("FD-104", "Paneer Butter Masala & 2 Parotta Bowl", 1, 110.0, "Less butter");
        o8.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);

        Order o9 = new Order("ORD-1009", "STU-004", "Student B", "CAN-1",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.CAMPUS_DELIVERY, "Library",
                "Immediate", "PAY_ON_DELIVERY", false, now.minusMinutes(8));
        o9.addItem("FD-103", "Executive South Indian Veg Meals", 1, 85.0, "Standard Prep");
        o9.addItem("FD-105", "Fresh Sweet Lime (Mosambi) Juice", 1, 35.0, "No ice");
        o9.setStatus(Order.OrderStatus.PLACED);

        Order o10 = new Order("ORD-1010", "STU-004", "Student B", "CAN-3",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 3 Counter",
                "Immediate", "FOOD_WALLET", false, now.minusDays(2).withHour(17).withMinute(0));
        o10.addItem("FD-304", "Loaded Peri-Peri Cheese Fries", 1, 80.0, "Extra cheese sauce");
        o10.addItem("FD-306", "Hot Ginger Masala Chai & 2 Punjabi Samosas", 1, 45.0, "Mint chutney");
        o10.setStatus(Order.OrderStatus.COLLECTED);

        Order o11 = new Order("ORD-1011", "STU-005", "Student C", "CAN-2",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.CAMPUS_DELIVERY, "Floor 1 Delivery Point",
                "Immediate", "FOOD_WALLET", false, now.minusMinutes(25));
        o11.addItem("FD-202", "Chettinad Pepper Chicken Meals", 1, 140.0, "Spicy pepper masala");
        o11.addItem("FD-203", "Madurai Egg Kothu Parotta", 1, 95.0, "Extra salna");
        o11.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);

        Order o12 = new Order("ORD-1012", "STU-005", "Student C", "CAN-2",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 2 Counter",
                "Immediate", "FOOD_WALLET", false, now.minusDays(1).withHour(13).withMinute(20));
        o12.addItem("FD-201", "Ambur Chicken Dum Biryani", 1, 130.0, "Standard Prep");
        o12.addItem("FD-207", "Hyderabadi Mutton Ghee Roast & Parotta", 1, 175.0, "Standard Prep");
        o12.setStatus(Order.OrderStatus.COLLECTED);

        Order o13 = new Order("ORD-1013", "STU-001", "Raghuram", "CAN-1",
                Order.OrderType.PRE_ORDER, Order.DeliveryMode.PICKUP, "Canteen 1 Counter",
                "01:30 PM", "FOOD_WALLET", false, now.minusMinutes(10));
        o13.addItem("FD-107", "Kongu Mushroom Dum Biryani (Pure Veg)", 1, 105.0, "Extra onion raita");
        o13.setStatus(Order.OrderStatus.PLACED);

        Order o14 = new Order("ORD-1014", "STU-002", "Likith", "CAN-1",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 1 Counter",
                "Immediate", "FOOD_WALLET", false, now.minusMinutes(6));
        o14.addItem("FD-106", "Kumbakonam Degree Filter Coffee", 2, 25.0, "Less sugar");
        o14.setStatus(Order.OrderStatus.PREPARING);

        Order o15 = new Order("ORD-1015", "STU-002", "Likith", "CAN-2",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.CAMPUS_DELIVERY, "Lab 1",
                "Immediate", "PAY_ON_DELIVERY", false, now.minusMinutes(9));
        o15.addItem("FD-201", "Ambur Chicken Dum Biryani", 1, 130.0, "Double brinjal curry");
        o15.setStatus(Order.OrderStatus.PLACED);

        Order o16 = new Order("ORD-1016", "STU-003", "Student A", "CAN-3",
                Order.OrderType.BUY_NOW, Order.DeliveryMode.PICKUP, "Canteen 3 Counter",
                "Immediate", "FOOD_WALLET", false, now.minusMinutes(5));
        o16.addItem("FD-306", "Hot Ginger Masala Chai & 2 Punjabi Samosas", 1, 45.0, "Hot tea");
        o16.setStatus(Order.OrderStatus.PLACED);

        for (Order o : new Order[]{o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16}) {
            ds.orders.put(o.getOrderId(), o);
        }

        // 6. Verified Feedbacks
        ds.feedbacks.add(new FeedbackDAO.FeedbackEntry("Raghuram", "CAN-2", "FD-201", "Ambur Chicken Dum Biryani", 5,
                "Authentic Seeraga Samba aroma and tender chicken! Best biryani on campus.", "Positive", "Optimal", "Positive"));
        ds.feedbacks.add(new FeedbackDAO.FeedbackEntry("Likith", "CAN-3", "FD-302", "Schezwan Veg Hakka Noodles", 3,
                "The noodles tasted good but was slightly too spicy and portion quantity felt a bit low.", "Positive", "Negative (Too Spicy)", "Negative (Low Portion)"));
        ds.feedbacks.add(new FeedbackDAO.FeedbackEntry("Student A", "CAN-1", "FD-103", "Executive South Indian Veg Meals", 5,
                "Fresh hot sambar, great variety of poriyal and generous quantity.", "Positive", "Optimal", "Positive"));
        ds.feedbacks.add(new FeedbackDAO.FeedbackEntry("Student B", "CAN-3", "FD-304", "Loaded Peri-Peri Cheese Fries", 5,
                "Crispy fries and generous cheese sauce.", "Positive", "Optimal", "Positive"));
        ds.feedbacks.add(new FeedbackDAO.FeedbackEntry("Student C", "CAN-2", "FD-207", "Hyderabadi Mutton Ghee Roast & Parotta", 5,
                "Rich ghee roast masala and very tender mutton pieces.", "Positive", "Optimal", "Positive"));

        // 7. Campus Calendar Events
        ds.events.add(new CampusEvent("EVT-101", "SRM Milan Techno-Cultural Fest", "Cultural Event",
                today.plusDays(3), 2400, "09:30 AM – 05:30 PM", 1.28));
        ds.events.add(new CampusEvent("EVT-102", "Inter-Hostel Athletics & Sports Meet", "Sports Day",
                today.plusDays(7), 1650, "07:30 AM – 04:00 PM", 1.22));
        ds.events.add(new CampusEvent("EVT-103", "AI, Cloud & Big Data National Hackathon", "Workshop / Hackathon",
                today.plusDays(1), 950, "09:00 AM – 06:00 PM", 1.18));
        ds.events.add(new CampusEvent("EVT-104", "Mid-Semester Lab Examinations", "Academic Exam",
                today.plusDays(12), 3100, "08:30 AM – 01:00 PM", 0.92));
        ds.events.add(new CampusEvent("EVT-105", "Deepavali Campus Holiday", "Public Holiday",
                today.plusDays(18), 450, "All Day", 0.45));

        // 8. Waste Logs
        ds.wasteRecords.add(new WasteRecord("WST-101", "CAN-1", "Executive South Indian Veg Meals",
                today.minusDays(1), 260, 270, 256, 14, 6.2, WasteRecord.WasteReason.OVERPRODUCTION));
        ds.wasteRecords.add(new WasteRecord("WST-102", "CAN-2", "Malabar Seer Fish Curry & Steamed Rice",
                today.minusDays(1), 140, 150, 128, 22, 9.8, WasteRecord.WasteReason.LOW_DEMAND));
        ds.wasteRecords.add(new WasteRecord("WST-103", "CAN-3", "Schezwan Veg Hakka Noodles",
                today.minusDays(1), 165, 180, 152, 28, 11.4, WasteRecord.WasteReason.OVERPRODUCTION));
        ds.wasteRecords.add(new WasteRecord("WST-104", "CAN-2", "Ambur Chicken Dum Biryani",
                today.minusDays(2), 310, 320, 308, 12, 5.4, WasteRecord.WasteReason.PREPARATION_ERROR));
        ds.wasteRecords.add(new WasteRecord("WST-105", "CAN-1", "Paneer Butter Masala & 2 Parotta Bowl",
                today.minusDays(2), 170, 175, 165, 10, 4.1, WasteRecord.WasteReason.SPOILAGE));

        // 9. Support Tickets & Simulation Requests
        ds.supportTickets.add(new SupportTicket("TKT-501", "Student A", "Canteen Queue / Seating Issue",
                "Peak lunch hour queue near Canteen 1 handwash area needs an extra token display screen."));
        ds.supportTickets.add(new SupportTicket("TKT-502", "Likith", "App / Delivery Point Suggestion",
                "Please add an express drop rack at Lab 1 entrance for rainy days."));

        ds.simulationRequests.add(new SimulationRequest("SIMREQ-101", "Cant2workA", "CAN-2", "EVENT_SURGE",
                "Please simulate +600 extra students at Canteen 2 for Friday Cultural Fest lunch."));

        ds.currentWeather = new WeatherSnapshot("SUMMER_HOT", 34.8, false, 64,
                "Chennai Campus Live Weather (34.8°C) — Open-Meteo / Google Weather Sync");
        return ds;
    }
}

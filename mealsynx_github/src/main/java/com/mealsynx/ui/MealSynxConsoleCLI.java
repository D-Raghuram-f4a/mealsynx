package com.mealsynx.ui;

import com.mealsynx.bigdata.BigDataPipelineService;
import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.dao.FeedbackDAO;
import com.mealsynx.exception.MealSynxException;
import com.mealsynx.model.*;
import com.mealsynx.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MealSynxConsoleCLI {

    private final AuthService authService;
    private final UserService userService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final WorkerOperationsService workerService;
    private final EventWeatherService eventWeatherService;
    private final PredictionService predictionService;
    private final WasteService wasteService;
    private final SimulationEngine simulationEngine;
    private final AnalyticsService analyticsService;
    private final NotificationService notificationService;
    private final SupportService supportService;
    private final CanteenDAO canteenDAO;
    private final EmployeeDAO employeeDAO;
    private final BigDataPipelineService bigDataPipeline;
    private final Scanner scanner = new Scanner(System.in);

    public MealSynxConsoleCLI(
            AuthService authService, UserService userService, MenuService menuService,
            OrderService orderService, InventoryService inventoryService,
            WorkerOperationsService workerService, EventWeatherService eventWeatherService,
            PredictionService predictionService, WasteService wasteService,
            SimulationEngine simulationEngine, AnalyticsService analyticsService,
            NotificationService notificationService, SupportService supportService,
            CanteenDAO canteenDAO, EmployeeDAO employeeDAO,
            BigDataPipelineService bigDataPipeline) {
        this.authService = authService;
        this.userService = userService;
        this.menuService = menuService;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.workerService = workerService;
        this.eventWeatherService = eventWeatherService;
        this.predictionService = predictionService;
        this.wasteService = wasteService;
        this.simulationEngine = simulationEngine;
        this.analyticsService = analyticsService;
        this.notificationService = notificationService;
        this.supportService = supportService;
        this.canteenDAO = canteenDAO;
        this.employeeDAO = employeeDAO;
        this.bigDataPipeline = bigDataPipeline;
    }

    public void runInteractiveLoop() {
        while (true) {
            System.out.println("\n================================================================================");
            System.out.println("                                  MEALSYNX                                      ");
            System.out.println("================================================================================");
            System.out.println("Tick Role Option:  [1] Student    [2] Worker    [3] Admin    [0] Exit");
            System.out.print("Select Role (1/2/3/0): ");
            String roleChoice = readLine();
            if ("0".equals(roleChoice)) {
                System.out.println("Exiting MealSynx. Goodbye!");
                break;
            }

            UserRole selectedRole;
            if ("1".equals(roleChoice)) selectedRole = UserRole.STUDENT;
            else if ("2".equals(roleChoice)) selectedRole = UserRole.WORKER;
            else if ("3".equals(roleChoice)) selectedRole = UserRole.ADMIN;
            else {
                System.out.println("Invalid option. Please choose 1 (Student), 2 (Worker), or 3 (Admin).");
                continue;
            }

            if (selectedRole == UserRole.STUDENT) {
                System.out.println("\n--- STUDENT LOGIN ---");
                System.out.println("1. Login with existing Student account (Raghuram, Likith, Student A, Student B, Student C)");
                System.out.println("2. Create a new account");
                System.out.print("Choose (1 or 2): ");
                String sub = readLine();
                if ("2".equals(sub)) {
                    handleCreateStudentAccount();
                    continue;
                }
            } else {
                System.out.println("\n--- " + selectedRole + " LOGIN ---");
            }

            System.out.print("User: ");
            String username = readLine();
            System.out.print("Password: ");
            String password = readLine();

            Optional<User> loggedIn = authService.loginWithRole(selectedRole, username, password);
            if (loggedIn.isEmpty()) {
                System.out.println("Login failed! Remember password is <username>123* (e.g., Raghuram123*, Cant1workA123*, admin123*).");
                continue;
            }

            User user = loggedIn.get();
            if (user instanceof Customer student) {
                runStudentPortal(student);
            } else if (user instanceof Worker worker) {
                runWorkerPortal(worker);
            } else if (user instanceof Admin admin) {
                runAdminPortal(admin);
            }
        }
    }

    private void handleCreateStudentAccount() {
        System.out.print("Enter new Student Username: ");
        String uname = readLine();
        if (uname.isEmpty()) return;
        System.out.print("Enter Full Name: ");
        String fname = readLine();
        System.out.print("Enter Register No: ");
        String reg = readLine();
        System.out.print("Enter Password (default " + uname + "123*): ");
        String pwd = readLine();
        if (pwd.isEmpty()) pwd = uname + "123*";

        Customer created = authService.registerNewStudent(uname, fname, reg, uname + "@campus.edu", pwd, 500.00, "Fast Food & Grills");
        System.out.println("Account created for " + created.getUsername() + " with ₹500.00 Food Wallet!");
        runStudentPortal(created);
    }

    // =========================================================================
    // STUDENT CLI PORTAL
    // =========================================================================
    private void runStudentPortal(Customer student) {
        while (true) {
            orderService.recalculateAllCanteenActiveCounts();
            System.out.println("\n================================================================================");
            System.out.printf(" 👤 User: %s (%s)  |  💳 Wallet Balance: ₹%.2f  |  🔔 Notifications (%d)  |  [0] Logout%n",
                    student.getUsername(), student.getFullName(), student.getWalletBalance(), student.getNotifications().size());
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" [1] Place Order   [2] Track Order   [3] Past Orders/Order History   [4] Ratings & Feedback   [5] Help & Support   [6] View Notifications   [0] Logout");
            System.out.println("--------------------------------------------------------------------------------");

            // Home Sections below options
            System.out.println(" 💡 SMART FOOD RECOMMENDATIONS (Based on Past Orders & Favourites):");
            userService.getSmartFoodRecommendations(student, eventWeatherService.getCurrentWeather())
                    .forEach(r -> System.out.println("    " + r));

            System.out.println("\n 🔥 TRENDING FOODS FROM ALL CANTEENS:");
            userService.getTrendingFoodsAllCanteens().forEach(t -> System.out.println("    " + t));

            System.out.println("\n 🔄 RECENTLY ORDERED / ORDER AGAIN:");
            for (FoodItem f : userService.getRecentlyOrderedItems(student)) {
                System.out.printf("    • %s (₹%.0f | ★%.2f | %s)%n", f.getName(), f.getPrice(), f.getCustomerRating(), f.getCanteenId());
            }

            System.out.println("\n ❤️ YOUR FAVOURITE ITEM LIST:");
            for (FoodItem f : userService.getFavouriteItemsList(student)) {
                System.out.printf("    • [%s] %s (₹%.0f | ★%.2f | %s)%n", f.getFoodId(), f.getName(), f.getPrice(), f.getCustomerRating(), f.getCanteenId());
            }

            System.out.print("\nSelect Option (1-6 or 0 to Logout): ");
            String choice = readLine();
            if ("0".equals(choice)) {
                authService.logout();
                break;
            } else if ("1".equals(choice)) {
                handleStudentPlaceOrder(student);
            } else if ("2".equals(choice)) {
                handleStudentTrackOrder(student);
            } else if ("3".equals(choice)) {
                handleStudentPastOrders(student);
            } else if ("4".equals(choice)) {
                handleStudentRatingsAndFeedback(student);
            } else if ("5".equals(choice)) {
                handleStudentHelpAndSupport(student);
            } else if ("6".equals(choice)) {
                System.out.println("\n--- YOUR NOTIFICATIONS ---");
                student.getNotifications().forEach(n -> System.out.println("  " + n));
            }
        }
    }

    private void handleStudentPlaceOrder(Customer student) {
        orderService.recalculateAllCanteenActiveCounts();
        System.out.println("\n--- SELECT A CANTEEN ---");
        List<Canteen> canteens = canteenDAO.findAll();
        for (int i = 0; i < canteens.size(); i++) {
            Canteen c = canteens.get(i);
            System.out.printf(" [%d] %s (%s) — Busyness: %s (%d active orders | %.2fx prep time)%n",
                    i + 1, c.getName(), c.getSpecialtyType(), c.getBusyStatus().toUpperCase(),
                    c.getActiveOrderCount(), c.getPrepTimeMultiplier());
        }
        System.out.print("Select Canteen (1-" + canteens.size() + "): ");
        int cIdx = parseInt(readLine(), 1) - 1;
        if (cIdx < 0 || cIdx >= canteens.size()) cIdx = 0;
        Canteen selectedCanteen = canteens.get(cIdx);

        System.out.print("Search food keyword (or press Enter to view full menu of " + selectedCanteen.getName() + "): ");
        String kw = readLine();
        List<FoodItem> menu = menuService.getMenuForCanteen(selectedCanteen.getCanteenId(), kw);

        List<Order.OrderLineItem> cart = new ArrayList<>();
        while (true) {
            System.out.println("\n--- MENU OF " + selectedCanteen.getName().toUpperCase() + " (Busyness: " + selectedCanteen.getBusyStatus().toUpperCase() + ") ---");
            for (int i = 0; i < menu.size(); i++) {
                FoodItem f = menu.get(i);
                int approxPrep = selectedCanteen.calculateApproxPrepTime(f.getBasePrepTimeMinutes());
                String favMark = student.isFavourite(f.getFoodId()) ? "❤️" : "♡";
                System.out.printf(" [%d] %s %-36s | ₹%6.2f | Approx Prep: ~%2d mins | ★%.2f (%d)%n",
                        i + 1, favMark, f.getName(), f.getPrice(), approxPrep, f.getCustomerRating(), f.getRatingCount());
            }
            System.out.println(" [F] Toggle ❤️ Favourite on an item   |   [P] Proceed to Checkout (" + cart.size() + " items selected)   |   [0] Cancel");
            System.out.print("Enter item number to select (or F / P / 0): ");
            String inp = readLine();
            if ("0".equalsIgnoreCase(inp)) return;
            if ("F".equalsIgnoreCase(inp)) {
                System.out.print("Enter item number to Favourite/Unfavourite: ");
                int fIdx = parseInt(readLine(), 1) - 1;
                if (fIdx >= 0 && fIdx < menu.size()) {
                    FoodItem item = menu.get(fIdx);
                    boolean nowFav = student.toggleFavouriteFood(item.getFoodId());
                    System.out.println(item.getName() + (nowFav ? " added to ❤️ Favourites!" : " removed from Favourites."));
                }
                continue;
            }
            if ("P".equalsIgnoreCase(inp)) {
                if (cart.isEmpty()) {
                    System.out.println("Please select at least one food item before proceeding!");
                    continue;
                }
                break;
            }
            int itemIdx = parseInt(inp, -1) - 1;
            if (itemIdx >= 0 && itemIdx < menu.size()) {
                FoodItem chosen = menu.get(itemIdx);
                System.out.print("Enter Quantity for " + chosen.getName() + ": ");
                int qty = Math.max(1, parseInt(readLine(), 1));
                System.out.print("Enter any Special Requirement (or press Enter for Standard Prep): ");
                String spec = readLine();
                cart.add(new Order.OrderLineItem(chosen.getFoodId(), chosen.getName(), qty, chosen.getPrice(), spec));
                System.out.println("Added " + qty + "x " + chosen.getName() + " to selection.");
            }
        }

        // Proceed asks the 3 required questions
        System.out.println("\n--- PROCEED TO CHECKOUT ---");
        System.out.print("1. Select Order Timing ([1] Buy Now  /  [2] Pre-order): ");
        boolean isPreOrder = "2".equals(readLine());
        String schedTime = "Immediate";
        if (isPreOrder) {
            System.out.print("   Enter Pre-order collection time (e.g., 1:15 PM): ");
            schedTime = readLine();
            if (schedTime.isEmpty()) schedTime = "1:15 PM";
        }

        System.out.print("2. Select Delivery Mode ([1] Pick-up  /  [2] Campus Delivery): ");
        boolean isDelivery = "2".equals(readLine());
        String deliveryPoint = "Canteen Pickup Counter";
        if (isDelivery) {
            System.out.println("   Select Campus Delivery Point:");
            for (int i = 0; i < OrderService.CAMPUS_DELIVERY_POINTS.length; i++) {
                System.out.printf("   [%d] %s%n", i + 1, OrderService.CAMPUS_DELIVERY_POINTS[i]);
            }
            System.out.print("   Choose Delivery Point (1-" + OrderService.CAMPUS_DELIVERY_POINTS.length + "): ");
            int dpIdx = parseInt(readLine(), 1) - 1;
            if (dpIdx < 0 || dpIdx >= OrderService.CAMPUS_DELIVERY_POINTS.length) dpIdx = 0;
            deliveryPoint = OrderService.CAMPUS_DELIVERY_POINTS[dpIdx];
        }

        System.out.print("3. Select Payment Method ([1] Food Wallet  /  [2] Pay on Delivery): ");
        String payMethod = "2".equals(readLine()) ? "PAY_ON_DELIVERY" : "FOOD_WALLET";

        Order newOrder = new Order(orderService.nextOrderId(), student.getUserId(), student.getUsername(),
                selectedCanteen.getCanteenId(),
                isPreOrder ? Order.OrderType.PRE_ORDER : Order.OrderType.BUY_NOW,
                isDelivery ? Order.DeliveryMode.CAMPUS_DELIVERY : Order.DeliveryMode.PICKUP,
                deliveryPoint, schedTime, payMethod, false, LocalDateTime.now());
        for (Order.OrderLineItem li : cart) {
            newOrder.addItem(li.getFoodId(), li.getFoodName(), li.getQuantity(), li.getUnitPrice(), li.getSpecialRequirement());
        }

        try {
            orderService.placeStudentOrder(student, newOrder);
            System.out.printf("✅ Order %s Placed Successfully! Total: ₹%.2f | Status: %s%n",
                    newOrder.getOrderId(), newOrder.getTotalAmount(), newOrder.getStatus());
        } catch (MealSynxException ex) {
            System.out.println("❌ Payment Failed: " + ex.getMessage());
        }
    }

    private void handleStudentTrackOrder(Customer student) {
        System.out.println("\n--- TRACK ORDER (LIVE PROGRESS) ---");
        for (Order o : orderService.getOrdersForStudent(student.getUsername())) {
            System.out.printf(" • %s | %s | %s | %s -> Status: %s%n",
                    o.getOrderId(), o.getCanteenId(), o.getItemsSummary(), o.getDeliveryPoint(), o.getStatus());
        }
    }

    private void handleStudentPastOrders(Customer student) {
        List<Order> past = orderService.getOrdersForStudent(student.getUsername());
        System.out.println("\n--- PAST ORDERS / ORDER HISTORY ---");
        for (int i = 0; i < past.size(); i++) {
            Order o = past.get(i);
            System.out.printf(" [%d] %s (%s) | %s | %s | ₹%.2f | %s%n",
                    i + 1, o.getOrderId(), o.getFormattedTimestamp(), o.getCanteenId(),
                    o.getItemsSummary(), o.getTotalAmount(), o.getStatus());
        }
        System.out.println(" [R] Reorder a Past Order   |   [F] Submit Review & Feedback   |   [0] Back");
        System.out.print("Choice: ");
        String ch = readLine();
        if ("R".equalsIgnoreCase(ch) && !past.isEmpty()) {
            System.out.print("Enter order number (1-" + past.size() + ") to Reorder: ");
            int idx = parseInt(readLine(), 1) - 1;
            if (idx >= 0 && idx < past.size()) {
                Order target = past.get(idx);
                try {
                    Order re = orderService.reorderPastOrder(student, target, Order.OrderType.BUY_NOW,
                            target.getDeliveryMode(), target.getDeliveryPoint(), "Immediate", "FOOD_WALLET");
                    System.out.println("✅ Reordered successfully as " + re.getOrderId() + "!");
                } catch (MealSynxException ex) {
                    System.out.println("❌ " + ex.getMessage());
                }
            }
        } else if ("F".equalsIgnoreCase(ch)) {
            handleStudentRatingsAndFeedback(student);
        }
    }

    private void handleStudentRatingsAndFeedback(Customer student) {
        List<FoodItem> ordered = userService.getFoodsOrderedByStudentForReview(student);
        System.out.println("\n--- RATINGS AND FEEDBACK (Verified Foods You Have Ordered) ---");
        if (ordered.isEmpty()) {
            System.out.println("You haven't ordered any food items yet.");
            return;
        }
        for (int i = 0; i < ordered.size(); i++) {
            FoodItem f = ordered.get(i);
            System.out.printf(" [%d] %s (%s) — Current Rating: ★%.2f (%d reviews)%n",
                    i + 1, f.getName(), f.getCanteenId(), f.getCustomerRating(), f.getRatingCount());
        }
        System.out.print("Select food number to review (or 0 to cancel): ");
        int idx = parseInt(readLine(), 0) - 1;
        if (idx < 0 || idx >= ordered.size()) return;
        FoodItem food = ordered.get(idx);
        System.out.print("Enter Star Rating (1-5): ");
        int stars = Math.max(1, Math.min(5, parseInt(readLine(), 5)));
        System.out.print("Enter Review Comment: ");
        String comment = readLine();
        FeedbackDAO.FeedbackEntry fe = wasteService.submitStudentFoodReview(student.getUsername(), food, stars, comment);
        System.out.printf("✅ Updated '%s' live rating to ★%.2f! NLP Sentiment -> Taste: %s | Spice: %s | Quantity: %s%n",
                food.getName(), food.getCustomerRating(), fe.tasteSentiment, fe.spicinessSentiment, fe.quantitySentiment);
    }

    private void handleStudentHelpAndSupport(Customer student) {
        System.out.println("\n--- HELP AND SUPPORT ---");
        System.out.println(" 📞 Contact Number : " + SupportService.SUPPORT_HELPLINE);
        System.out.println(" ✉ Support Email  : " + SupportService.SUPPORT_EMAIL);
        System.out.println(" 📍 Helpdesk       : " + SupportService.SUPPORT_LOCATION);
        System.out.print("\nWould you like to submit a non-food complaint in the Complaint Box? (y/n): ");
        if ("y".equalsIgnoreCase(readLine())) {
            System.out.print("Enter Complaint Category (e.g. Canteen Queue, App/Wallet Issue, Hygiene): ");
            String cat = readLine();
            System.out.print("Enter Complaint Description: ");
            String desc = readLine();
            SupportTicket t = supportService.submitComplaint(student.getUsername(), cat, desc);
            System.out.println("✅ Complaint registered under Ticket ID: " + t.getTicketId());
        }
    }

    // =========================================================================
    // WORKER CLI PORTAL
    // =========================================================================
    private void runWorkerPortal(Worker worker) {
        Canteen canteen = canteenDAO.findById(worker.getAssignedCanteenId()).orElseThrow();
        while (true) {
            orderService.recalculateAllCanteenActiveCounts();
            System.out.println("\n================================================================================");
            System.out.printf(" 👷 Worker: %s  |  🏢 %s [%s - %s]  |  🕒 Shift: %s  |  🔔 Notifications (%d)%n",
                    worker.getUsername(), canteen.getName(), canteen.getSpecialtyType(),
                    canteen.getBusyStatus().toUpperCase(), worker.getShiftTiming(), worker.getNotifications().size());
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" [1] Orders Received   [2] Update Menu & Kitchen Plan   [3] Warehouse Inventory & Expiry");
            System.out.println(" [4] Sales, Expenses & Money Management   [5] Admin Updates & Request Sim   [6] Live Counter Billing (Offline POS)   [0] Logout");
            System.out.print("Select Option: ");
            String ch = readLine();
            if ("0".equals(ch)) {
                authService.logout();
                break;
            } else if ("1".equals(ch)) {
                List<Order> list = orderService.getOrdersForCanteen(canteen.getCanteenId());
                for (int i = 0; i < list.size(); i++) {
                    Order o = list.get(i);
                    System.out.printf(" [%d] %s | %s | %s | %s | Status: %s%n",
                            i + 1, o.getOrderId(), o.getCustomerUsername(), o.getItemsSummary(), o.getDeliveryPoint(), o.getStatus());
                }
                System.out.print("Enter order number to press [ + ] (advance to next status, cannot be reversed) or 0 to skip: ");
                int idx = parseInt(readLine(), 0) - 1;
                if (idx >= 0 && idx < list.size()) {
                    Order target = list.get(idx);
                    if (orderService.advanceOrderStatusByPlusButton(target.getOrderId())) {
                        System.out.println("✅ [ + ] Order " + target.getOrderId() + " moved to: " + target.getDisplayStatusLabel());
                    } else {
                        System.out.println("🔒 Order " + target.getOrderId() + " is already " + target.getDisplayStatusLabel() + " (Locked - cannot be reversed).");
                    }
                }
            } else if ("2".equals(ch)) {
                WeatherSnapshot w = eventWeatherService.getCurrentWeather();
                double evM = predictionService.getActiveEventMultiplier();
                for (FoodItem f : menuService.getMenuForCanteen(canteen.getCanteenId(), "")) {
                    int est = f.getSystemEstimatedKitchenPrep(w.getCategoryDemandMultiplier(f.getCategory()), evM);
                    System.out.printf(" • [%s] %-35s | ₹%.0f | Est Kitchen Prep: %d plates%n",
                            f.getFoodId(), f.getName(), f.getPrice(), est);
                }
            } else if ("3".equals(ch)) {
                for (IngredientStock s : inventoryService.getInventoryForCanteen(canteen.getCanteenId())) {
                    System.out.printf(" • [%s] %-32s | Purchased: %s | Expiry: %s | Present: %.1f %s | Next Day Req: %.1f %s | %s%n",
                            s.getIngredientId(), s.getName(), s.getPurchaseDate(), s.getExpiryDate(),
                            s.getCurrentStock(), s.getUnit(), s.getNextDayRequiredQty(), s.getUnit(), s.getQualityStatusLabel());
                }
            } else if ("4".equals(ch)) {
                System.out.printf(" Total Sales Revenue       : ₹%,.2f%n", canteen.getTotalSalesRevenue());
                System.out.printf(" Ingredient Purchases      : ₹%,.2f%n", canteen.getIngredientPurchaseExpenses());
                System.out.printf(" Worker Wages              : ₹%,.2f%n", canteen.getWorkerWagesExpenses());
                System.out.printf(" Utilities (Current/Gas)   : ₹%,.2f%n", canteen.getUtilitiesExpenses());
                System.out.printf(" Net Profit / Loss         : %s₹%,.2f%n",
                        canteen.getNetProfitOrLoss() >= 0 ? "+" : "-", Math.abs(canteen.getNetProfitOrLoss()));
                if (canteen.isRunningInLoss()) {
                    System.out.println(" 🚨 WARNING: CANTEEN IS GOING INTO LOSS!");
                }
            } else if ("5".equals(ch)) {
                System.out.println("--- ADMIN UPDATES ---");
                canteen.getAdminUpdatesFeed().forEach(u -> System.out.println("  " + u));
                System.out.print("Request What-If Simulation from Admin? (y/n): ");
                if ("y".equalsIgnoreCase(readLine())) {
                    System.out.print("Enter simulation request note: ");
                    String note = readLine();
                    workerService.submitWhatIfSimulationRequest(worker.getUsername(), canteen.getCanteenId(), "EVENT_SURGE", note);
                    System.out.println("✅ Simulation request sent to Admin!");
                }
            } else if ("6".equals(ch)) {
                List<FoodItem> menu = menuService.getMenuForCanteen(canteen.getCanteenId(), "");
                for (int i = 0; i < menu.size(); i++) {
                    System.out.printf(" [%d] %s (₹%.0f)%n", i + 1, menu.get(i).getName(), menu.get(i).getPrice());
                }
                System.out.print("Select item number for Live Counter Walk-in Bill: ");
                int idx = parseInt(readLine(), 1) - 1;
                if (idx >= 0 && idx < menu.size()) {
                    FoodItem f = menu.get(idx);
                    System.out.print("Enter Quantity: ");
                    int q = Math.max(1, parseInt(readLine(), 1));
                    Order pos = orderService.createLiveCounterOfflineOrder(canteen.getCanteenId(), "Walk-In Student",
                            List.of(new Order.OrderLineItem(f.getFoodId(), f.getName(), q, f.getPrice(), "Live Counter")), "LIVE_COUNTER_CASH");
                    System.out.printf("✅ Live Counter Bill %s completed for ₹%.2f!%n", pos.getOrderId(), pos.getTotalAmount());
                }
            }
        }
    }

    // =========================================================================
    // ADMIN CLI PORTAL
    // =========================================================================
    private void runAdminPortal(Admin admin) {
        while (true) {
            System.out.println("\n================================================================================");
            System.out.println(" 👨‍💼 Admin: " + admin.getUsername() + "   |   [0] Logout");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" [1] Update Balance   [2] Stats & Performance   [3] What-If Simulator   [4] Waste & Feedback Loop   [5] Upcoming Events & Weather   [6] Alert Canteen   [7] Add Worker / Add Admin   [0] Logout");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 📊 REAL-TIME CAMPUS DASHBOARD:");
            analyticsService.getRealTimeExecutiveMetrics().forEach((k, v) -> System.out.printf("    %-28s : %s%n", k, v));
            analyticsService.getMultiCanteenLoadAnalysis().forEach(l -> System.out.println("    • " + l));

            System.out.print("\nSelect Option (1-6 or 0 to Logout): ");
            String ch = readLine();
            if ("0".equals(ch)) {
                authService.logout();
                break;
            } else if ("1".equals(ch)) {
                System.out.print("Enter Student Username (Raghuram / Likith / Student A / Student B / Student C): ");
                String u = readLine();
                System.out.print("Enter Amount (₹) to add to student wallet: ");
                try {
                    double amt = Double.parseDouble(readLine());
                    userService.addMoneyToStudentWallet(u, amt).ifPresentOrElse(
                            s -> System.out.printf("✅ Updated %s's Wallet Balance to ₹%.2f!%n", s.getUsername(), s.getWalletBalance()),
                            () -> System.out.println("❌ Student not found!")
                    );
                } catch (Exception e) {
                    System.out.println("Invalid amount.");
                }
            } else if ("2".equals(ch)) {
                analyticsService.getMultiCanteenLoadAnalysis().forEach(System.out::println);
            } else if ("3".equals(ch)) {
                SimulationResult res = simulationEngine.runSimulation("EVENT_SURGE", Map.of("extraStudents", 750, "attendancePct", 88));
                System.out.println(res.getScenarioTitle() + " -> " + res.getSummaryDescription());
                res.getCategoryDemandDelta().forEach((k, v) -> System.out.println("  " + k + ": " + v));
            } else if ("4".equals(ch)) {
                wasteService.getWasteReasonBreakdown().forEach((k, v) -> System.out.printf("  %-20s : %.1f%%%n", k, v));
            } else if ("5".equals(ch)) {
                System.out.println("  " + eventWeatherService.getCurrentWeather().getWeatherAnalyticsRecommendation());
            } else if ("6".equals(ch)) {
                System.out.print("Enter Canteen ID (CAN-1 / CAN-2 / CAN-3 / ALL): ");
                String cid = readLine();
                System.out.print("Enter Alert Message for Canteen Workers: ");
                String msg = readLine();
                notificationService.sendAdminAlertToCanteens(cid, "ADMIN DIRECTIVE", msg, canteenDAO, employeeDAO);
                System.out.println("✅ Alert broadcasted to " + cid + "!");
            } else if ("7".equals(ch)) {
                System.out.print("Add [1] New Canteen Worker  or  [2] New Admin? (1/2): ");
                String sub = readLine();
                if ("1".equals(sub)) {
                    System.out.print("Enter Worker Username (e.g. Cant1workB): ");
                    String wu = readLine();
                    System.out.print("Enter Full Name: ");
                    String wn = readLine();
                    System.out.print("Assign Canteen (CAN-1 / CAN-2 / CAN-3): ");
                    String wc = readLine();
                    if (wc.isEmpty()) wc = "CAN-1";
                    System.out.print("Enter Shift Timing (e.g. 02:00 PM - 10:00 PM): ");
                    String ws = readLine();
                    Worker w = authService.addNewWorkerByAdmin(wu, wn, wu + "123*", wc, ws, "Kitchen & Counter Ops");
                    System.out.println("✅ Added Worker " + w.getUsername() + " (Password: " + w.getPassword() + ") to " + w.getAssignedCanteenId());
                } else if ("2".equals(sub)) {
                    System.out.print("Enter New Admin Username (e.g. admin2): ");
                    String au = readLine();
                    System.out.print("Enter Full Name: ");
                    String an = readLine();
                    Admin na = authService.addNewAdminByAdmin(au, an, au + "123*", "Campus Food Intelligence");
                    System.out.println("✅ Added Admin " + na.getUsername() + " (Password: " + na.getPassword() + ")");
                }
            }
        }
    }

    private String readLine() {
        try {
            return scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
        } catch (Exception e) {
            return "0";
        }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}

package com.mealsynx.ui;

import com.mealsynx.bigdata.BigDataPipelineService;
import com.mealsynx.dao.CanteenDAO;
import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.dao.FeedbackDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.exception.MealSynxException;
import com.mealsynx.model.*;
import com.mealsynx.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MealSynxDesktopApp extends JFrame {

    private static final Color PRIMARY_BLUE = new Color(21, 94, 195);
    private static final Color SUCCESS_GREEN = new Color(22, 132, 72);
    private static final Color WARNING_AMBER = new Color(210, 110, 12);
    private static final Color DANGER_RED = new Color(198, 40, 40);
    private static final Color PURPLE_ACCENT = new Color(115, 55, 165);
    private static final Color DARK_SLATE = new Color(30, 41, 59);
    private static final Color NAV_BG = new Color(255, 255, 255);
    private static final Color NAV_FG = new Color(15, 23, 42);

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

    private final CardLayout rootCardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCardLayout);

    private String selectedStudentCanteenId = "ALL";

    private static class ModernButton extends JButton {
        private Color baseBg;
        private Color baseFg;
        private boolean hovered = false;

        public ModernButton(String text, Color bg, Color fg, int fontSize) {
            super(text);
            this.baseBg = bg;
            this.baseFg = fg;
            setFont(new Font("SansSerif", Font.BOLD, fontSize));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setColors(Color bg, Color fg) {
            this.baseBg = bg;
            this.baseFg = fg;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            String t = getText() != null ? getText() : "";
            int w = fm.stringWidth(t) + 32;
            int h = Math.max(34, fm.getHeight() + 14);
            Dimension sup = super.getPreferredSize();
            return new Dimension(Math.max(w, sup.width), Math.max(h, sup.height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = baseBg;
            if (!isEnabled()) {
                fill = new Color(180, 190, 205);
            } else if (hovered) {
                if (baseBg.equals(Color.WHITE)) {
                    fill = new Color(224, 236, 255);
                } else {
                    fill = new Color(
                            Math.max(0, baseBg.getRed() - 25),
                            Math.max(0, baseBg.getGreen() - 25),
                            Math.max(0, baseBg.getBlue() - 25)
                    );
                }
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            Color borderCol = baseBg.equals(Color.WHITE) ? new Color(148, 163, 184) : fill.darker();
            g2.setColor(borderCol);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String txt = getText() != null ? getText() : "";
            int tx = Math.max(6, (w - fm.stringWidth(txt)) / 2);
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(isEnabled() ? baseFg : new Color(90, 100, 115));
            g2.drawString(txt, tx, ty);
            g2.dispose();
        }
    }

    public MealSynxDesktopApp(
            AuthService authService, UserService userService, MenuService menuService,
            OrderService orderService, InventoryService inventoryService,
            WorkerOperationsService workerService, EventWeatherService eventWeatherService,
            PredictionService predictionService, WasteService wasteService,
            SimulationEngine simulationEngine, AnalyticsService analyticsService,
            NotificationService notificationService, SupportService supportService,
            CanteenDAO canteenDAO, EmployeeDAO employeeDAO,
            BigDataPipelineService bigDataPipeline) {

        super("MealSynx — Intelligent Campus Food & Multi-Canteen Platform");
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

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 850);
        setLocationRelativeTo(null);

        rootPanel.add(buildLoginScreen(), "LOGIN");
        setContentPane(rootPanel);
        rootCardLayout.show(rootPanel, "LOGIN");
    }

    private ModernButton createStyledButton(String text, Color bgColor, Color fgColor, int fontSize) {
        return new ModernButton(text, bgColor, fgColor, fontSize);
    }

    private ModernButton createNavButton(String text) {
        return new ModernButton(text, NAV_BG, NAV_FG, 13);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(15, 23, 42));
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
            table.getTableHeader().setBackground(new Color(241, 245, 249));
            table.getTableHeader().setForeground(new Color(15, 23, 42));
        }
    }

    private JPanel buildLoginScreen() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(235, 242, 252));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 198, 226), 2, true),
                new EmptyBorder(32, 50, 32, 50)
        ));

        JLabel title = new JLabel("Mealsynx");
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setForeground(new Color(15, 45, 105));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Intelligent Campus Food Demand, Weather/Calendar Analytics & Multi-Canteen Platform");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(85, 100, 122));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel tickPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        tickPanel.setBackground(new Color(244, 248, 255));
        tickPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(185, 202, 228)),
                "Select Role (Tick Only One Option)",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), new Color(25, 55, 110)
        ));

        JCheckBox studentTick = new JCheckBox("Student", true);
        JCheckBox workerTick = new JCheckBox("Worker", false);
        JCheckBox adminTick = new JCheckBox("Admin", false);

        Font tickFont = new Font("SansSerif", Font.BOLD, 15);
        for (JCheckBox cb : new JCheckBox[]{studentTick, workerTick, adminTick}) {
            cb.setFont(tickFont);
            cb.setForeground(new Color(15, 35, 75));
            cb.setBackground(new Color(244, 248, 255));
            cb.setFocusPainted(false);
        }

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentTick);
        roleGroup.add(workerTick);
        roleGroup.add(adminTick);

        tickPanel.add(studentTick);
        tickPanel.add(workerTick);
        tickPanel.add(adminTick);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(16, 10, 16, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLbl = new JLabel("User:");
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLbl.setForeground(new Color(15, 35, 75));
        JTextField userField = new JTextField("Raghuram", 22);
        userField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        passLbl.setForeground(new Color(15, 35, 75));
        JPasswordField passField = new JPasswordField("Raghuram123*", 22);
        passField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(userLbl, gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(passLbl, gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(passField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        btnPanel.setBackground(Color.WHITE);

        ModernButton loginBtn = createStyledButton("Login", PRIMARY_BLUE, Color.WHITE, 15);
        loginBtn.setPreferredSize(new Dimension(150, 42));

        ModernButton createAccountBtn = createStyledButton("Create a new account", SUCCESS_GREEN, Color.WHITE, 14);
        createAccountBtn.setPreferredSize(new Dimension(225, 42));

        btnPanel.add(loginBtn);
        btnPanel.add(createAccountBtn);

        JTextArea credHint = new JTextArea(
                "All changes (New Users, Favourites, Reviews, Orders, Staff) save automatically to disk!\n" +
                "• Students : Raghuram | Likith | Student A | Student B | Student C  (Pass: <username>123*)\n" +
                "• Workers  : Cant1workA (Pure Veg) | Cant2workA (Non-Veg) | Cant3workA (Grills) (Pass: <user>123*)\n" +
                "• Admin    : admin (Pass: admin123*)"
        );
        credHint.setEditable(false);
        credHint.setFont(new Font("Monospaced", Font.PLAIN, 11));
        credHint.setForeground(new Color(30, 45, 70));
        credHint.setBackground(new Color(246, 249, 254));
        credHint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        Runnable updateRoleUi = () -> {
            if (studentTick.isSelected()) {
                createAccountBtn.setVisible(true);
                if (userField.getText().equals("Cant1workA") || userField.getText().equals("admin")) {
                    userField.setText("Raghuram");
                    passField.setText("Raghuram123*");
                }
            } else if (workerTick.isSelected()) {
                createAccountBtn.setVisible(false);
                if (userField.getText().equals("Raghuram") || userField.getText().equals("admin")) {
                    userField.setText("Cant1workA");
                    passField.setText("Cant1workA123*");
                }
            } else if (adminTick.isSelected()) {
                createAccountBtn.setVisible(false);
                if (userField.getText().equals("Raghuram") || userField.getText().equals("Cant1workA")) {
                    userField.setText("admin");
                    passField.setText("admin123*");
                }
            }
            card.revalidate();
            card.repaint();
        };

        studentTick.addActionListener(e -> updateRoleUi.run());
        workerTick.addActionListener(e -> updateRoleUi.run());
        adminTick.addActionListener(e -> updateRoleUi.run());

        loginBtn.addActionListener(e -> {
            UserRole role = studentTick.isSelected() ? UserRole.STUDENT
                    : (workerTick.isSelected() ? UserRole.WORKER : UserRole.ADMIN);
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();

            Optional<User> loggedIn = authService.loginWithRole(role, u, p);
            if (loggedIn.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials for role " + role + "!\n" +
                        "Default password format: <username>123* (e.g., Raghuram123*, Cant1workA123*, admin123*)",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User user = loggedIn.get();
            if (user instanceof Customer student) {
                rootPanel.add(buildStudentPortal(student), "STUDENT_PORTAL");
                rootCardLayout.show(rootPanel, "STUDENT_PORTAL");
            } else if (user instanceof Worker worker) {
                rootPanel.add(buildWorkerPortal(worker), "WORKER_PORTAL");
                rootCardLayout.show(rootPanel, "WORKER_PORTAL");
            } else if (user instanceof Admin admin) {
                rootPanel.add(buildAdminPortal(admin), "ADMIN_PORTAL");
                rootCardLayout.show(rootPanel, "ADMIN_PORTAL");
            }
        });

        createAccountBtn.addActionListener(e -> {
            JTextField newUsername = new JTextField(16);
            JTextField newFullName = new JTextField(16);
            JTextField newRegNo = new JTextField("RA2511027040099", 16);
            JPasswordField newPass = new JPasswordField(16);
            JComboBox<String> favCatBox = new JComboBox<>(new String[]{
                    "Pure Veg South Indian", "Biryani & Rice (Non-Veg)", "Grills & Wraps", "Cold Beverages"
            });

            JPanel regForm = new JPanel(new GridLayout(0, 2, 8, 8));
            regForm.add(new JLabel("User (Username):")); regForm.add(newUsername);
            regForm.add(new JLabel("Full Name:")); regForm.add(newFullName);
            regForm.add(new JLabel("Register No:")); regForm.add(newRegNo);
            regForm.add(new JLabel("Password (default <user>123*):")); regForm.add(newPass);
            regForm.add(new JLabel("Favourite Category:")); regForm.add(favCatBox);

            int res = JOptionPane.showConfirmDialog(this, regForm,
                    "Create a New Student Account (Saved Permanently)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String uname = newUsername.getText().trim();
                if (uname.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Username cannot be empty!");
                    return;
                }
                String pwd = new String(newPass.getPassword()).trim();
                if (pwd.isEmpty()) pwd = uname + "123*";
                Customer created = authService.registerNewStudent(
                        uname, newFullName.getText().trim(), newRegNo.getText().trim(),
                        uname.toLowerCase().replaceAll("\\s+", "") + "@campus.edu",
                        pwd, 500.00, (String) favCatBox.getSelectedItem()
                );
                JOptionPane.showMessageDialog(this,
                        "Account '" + created.getUsername() + "' created and saved permanently!\nPassword: " + created.getPassword());
                rootPanel.add(buildStudentPortal(created), "STUDENT_PORTAL");
                rootCardLayout.show(rootPanel, "STUDENT_PORTAL");
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));
        card.add(tickPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(formPanel);
        card.add(btnPanel);
        card.add(Box.createVerticalStrut(14));
        card.add(credHint);

        outer.add(card);
        return outer;
    }

    private JPanel buildStudentPortal(Customer student) {
        JPanel main = new JPanel(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 38, 82));
        topBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel userAndWalletLbl = new JLabel();
        userAndWalletLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        userAndWalletLbl.setForeground(Color.WHITE);

        ModernButton notifBtn = createStyledButton("🔔 Notifications (0)", new Color(255, 202, 40), new Color(20, 25, 35), 13);
        ModernButton logoutBtn = createStyledButton("Logout", DANGER_RED, Color.WHITE, 13);

        Runnable refreshStudentHeader = () -> {
            WeatherSnapshot w = eventWeatherService.getCurrentWeather();
            userAndWalletLbl.setText(String.format("👤 %s (%s)   |   💳 Wallet: ₹%.2f   |   🌤 %s (%.1f°C)",
                    student.getUsername(), student.getFullName(), student.getWalletBalance(),
                    w.getSeasonMode(), w.getTemperatureCelsius()));
            notifBtn.setText("🔔 Notifications (" + student.getNotifications().size() + ")");
        };
        refreshStudentHeader.run();

        notifBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Recent Notifications for " + student.getUsername() + ":\n\n");
            if (student.getNotifications().isEmpty()) {
                sb.append("No notifications yet.");
            } else {
                for (String n : student.getNotifications()) {
                    sb.append("• ").append(n).append("\n\n");
                }
            }
            JTextArea area = new JTextArea(sb.toString(), 14, 55);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(area),
                    "Notifications — " + student.getUsername(), JOptionPane.INFORMATION_MESSAGE);
        });

        logoutBtn.addActionListener(e -> {
            PersistentDataStore.getInstance().saveToDisk();
            authService.logout();
            rootPanel.add(buildLoginScreen(), "LOGIN");
            rootCardLayout.show(rootPanel, "LOGIN");
        });

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(notifBtn);
        topRight.add(logoutBtn);

        topBar.add(userAndWalletLbl, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        navBar.setBackground(new Color(228, 236, 248));
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(180, 196, 222)));

        ModernButton homeNavBtn = createNavButton("🏠 Home");
        ModernButton placeOrderNavBtn = createNavButton("🛒 Place Order (All 30 Dishes)");
        ModernButton trackOrderNavBtn = createNavButton("🚚 Track Order");
        ModernButton pastOrdersNavBtn = createNavButton("📜 Past Orders / Order History");
        ModernButton ratingsNavBtn = createNavButton("⭐ Ratings and Feedback");
        ModernButton supportNavBtn = createNavButton("🛠 Help and Support");

        navBar.add(homeNavBtn);
        navBar.add(placeOrderNavBtn);
        navBar.add(trackOrderNavBtn);
        navBar.add(pastOrdersNavBtn);
        navBar.add(ratingsNavBtn);
        navBar.add(supportNavBtn);

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(topBar, BorderLayout.NORTH);
        northContainer.add(navBar, BorderLayout.SOUTH);

        CardLayout contentCards = new CardLayout();
        JPanel contentArea = new JPanel(contentCards);

        Runnable showHomeView = () -> {
            refreshStudentHeader.run();
            contentArea.add(buildStudentHomeFeedView(student, () -> {
                contentArea.add(buildStudentPlaceOrderView(student, refreshStudentHeader), "PLACE_ORDER");
                contentCards.show(contentArea, "PLACE_ORDER");
            }), "HOME");
            contentCards.show(contentArea, "HOME");
        };

        homeNavBtn.addActionListener(e -> showHomeView.run());
        placeOrderNavBtn.addActionListener(e -> {
            refreshStudentHeader.run();
            contentArea.add(buildStudentPlaceOrderView(student, refreshStudentHeader), "PLACE_ORDER");
            contentCards.show(contentArea, "PLACE_ORDER");
        });
        trackOrderNavBtn.addActionListener(e -> {
            refreshStudentHeader.run();
            contentArea.add(buildStudentTrackOrderView(student), "TRACK_ORDER");
            contentCards.show(contentArea, "TRACK_ORDER");
        });
        pastOrdersNavBtn.addActionListener(e -> {
            refreshStudentHeader.run();
            contentArea.add(buildStudentPastOrdersView(student, refreshStudentHeader, () -> {
                contentArea.add(buildStudentRatingsFeedbackView(student), "RATINGS");
                contentCards.show(contentArea, "RATINGS");
            }), "PAST_ORDERS");
            contentCards.show(contentArea, "PAST_ORDERS");
        });
        ratingsNavBtn.addActionListener(e -> {
            refreshStudentHeader.run();
            contentArea.add(buildStudentRatingsFeedbackView(student), "RATINGS");
            contentCards.show(contentArea, "RATINGS");
        });
        supportNavBtn.addActionListener(e -> {
            refreshStudentHeader.run();
            contentArea.add(buildStudentHelpSupportView(student), "SUPPORT");
            contentCards.show(contentArea, "SUPPORT");
        });

        showHomeView.run();

        main.add(northContainer, BorderLayout.NORTH);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildStudentHomeFeedView(Customer student, Runnable goToPlaceOrder) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(12, 16, 12, 16));
        container.setBackground(new Color(245, 248, 253));

        WeatherSnapshot weather = eventWeatherService.getCurrentWeather();

        JPanel sec1 = createSectionPanel("1. 💡 Smart Food Recommendations (Based on Your Past Orders, Favourites & Live Weather)");
        DefaultListModel<String> smartModel = new DefaultListModel<>();
        for (String rec : userService.getSmartFoodRecommendations(student, weather)) {
            smartModel.addElement(rec);
        }
        JList<String> smartList = new JList<>(smartModel);
        smartList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sec1.add(new JScrollPane(smartList), BorderLayout.CENTER);

        JPanel sec2 = createSectionPanel("2. 🔥 Trending Foods From All Canteens (Live Order Velocity & User Ratings)");
        DefaultListModel<String> trendModel = new DefaultListModel<>();
        for (String tr : userService.getTrendingFoodsAllCanteens()) {
            trendModel.addElement(tr);
        }
        JList<String> trendList = new JList<>(trendModel);
        trendList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sec2.add(new JScrollPane(trendList), BorderLayout.CENTER);

        JPanel sec3 = createSectionPanel("3. 🔄 Recently Ordered / Order Again");
        DefaultListModel<String> recentModel = new DefaultListModel<>();
        for (FoodItem item : userService.getRecentlyOrderedItems(student)) {
            Canteen c = canteenDAO.findById(item.getCanteenId()).orElse(null);
            int prep = c != null ? c.calculateApproxPrepTime(item.getBasePrepTimeMinutes()) : item.getBasePrepTimeMinutes();
            recentModel.addElement(String.format("⭐ %s — ₹%.0f | ★%.2f | ~%d mins (%s)",
                    item.getName(), item.getPrice(), item.getCustomerRating(), prep,
                    c != null ? c.getName() : item.getCanteenId()));
        }
        JList<String> recentList = new JList<>(recentModel);
        recentList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ModernButton quickOrderBtn = createStyledButton("🛒 Browse Canteens & Place Order", PRIMARY_BLUE, Color.WHITE, 13);
        quickOrderBtn.addActionListener(e -> goToPlaceOrder.run());
        sec3.add(new JScrollPane(recentList), BorderLayout.CENTER);
        sec3.add(quickOrderBtn, BorderLayout.SOUTH);

        JPanel sec4 = createSectionPanel("4. ❤️ Your Favourite Item List (Permanently Saved Across Sessions)");
        DefaultListModel<String> favModel = new DefaultListModel<>();
        List<FoodItem> favItems = userService.getFavouriteItemsList(student);
        if (favItems.isEmpty()) {
            favModel.addElement("No favourite items yet. Click the ❤️ Favourite button next to any food in 'Place Order'!");
        } else {
            for (FoodItem item : favItems) {
                Canteen c = canteenDAO.findById(item.getCanteenId()).orElse(null);
                int prep = c != null ? c.calculateApproxPrepTime(item.getBasePrepTimeMinutes()) : item.getBasePrepTimeMinutes();
                favModel.addElement(String.format("❤️ [%s] %s — ₹%.0f | ★%.2f | Approx Prep: ~%d mins | %s",
                        item.getFoodId(), item.getName(), item.getPrice(), item.getCustomerRating(), prep,
                        c != null ? c.getName() : item.getCanteenId()));
            }
        }
        JList<String> favList = new JList<>(favModel);
        favList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sec4.add(new JScrollPane(favList), BorderLayout.CENTER);

        container.add(sec1);
        container.add(Box.createVerticalStrut(8));
        container.add(sec2);
        container.add(Box.createVerticalStrut(8));
        container.add(sec3);
        container.add(Box.createVerticalStrut(8));
        container.add(sec4);

        JScrollPane scroll = new JScrollPane(container);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildStudentPlaceOrderView(Customer student, Runnable onHeaderUpdate) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));
        panel.setBackground(new Color(245, 248, 253));

        orderService.recalculateAllCanteenActiveCounts();

        JPanel canteenCardsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        canteenCardsPanel.setBackground(new Color(245, 248, 253));
        canteenCardsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(175, 195, 225)),
                "Step 1: Select a Canteen (Shows Canteen Name, How Busy It Is: Free / Nominal / Busy, and Approx Prep Time Multiplier)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13), new Color(15, 45, 100)));

        JPanel centerMenuContainer = new JPanel(new BorderLayout(8, 8));
        centerMenuContainer.setBackground(Color.WHITE);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchBarPanel.setBackground(new Color(238, 244, 252));
        searchBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(195, 210, 232)));

        JLabel selectedCanteenBanner = new JLabel();
        selectedCanteenBanner.setFont(new Font("SansSerif", Font.BOLD, 14));
        selectedCanteenBanner.setForeground(new Color(15, 50, 115));

        JTextField searchField = new JTextField(18);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ModernButton searchBtn = createStyledButton("🔍 Search Food", PRIMARY_BLUE, Color.WHITE, 12);
        ModernButton clearSearchBtn = createStyledButton("Show All Items", DARK_SLATE, Color.WHITE, 12);

        searchBarPanel.add(selectedCanteenBanner);
        searchBarPanel.add(Box.createHorizontalStrut(10));
        searchBarPanel.add(new JLabel("Search Food:"));
        searchBarPanel.add(searchField);
        searchBarPanel.add(searchBtn);
        searchBarPanel.add(clearSearchBtn);

        JPanel foodRowsPanel = new JPanel(new GridBagLayout());
        foodRowsPanel.setBackground(Color.WHITE);
        JScrollPane menuScroll = new JScrollPane(foodRowsPanel);
        menuScroll.getVerticalScrollBar().setUnitIncrement(20);
        menuScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 212, 230)));

        class FoodRowControl {
            final FoodItem food;
            final JCheckBox selectCheck;
            final JSpinner qtySpinner;
            final JTextField specialReqField;

            FoodRowControl(FoodItem food, JCheckBox selectCheck, JSpinner qtySpinner, JTextField specialReqField) {
                this.food = food;
                this.selectCheck = selectCheck;
                this.qtySpinner = qtySpinner;
                this.specialReqField = specialReqField;
            }
        }
        List<FoodRowControl> currentRowControls = new ArrayList<>();

        Runnable populateMenuRows = () -> {
            foodRowsPanel.removeAll();
            currentRowControls.clear();

            List<FoodItem> items = menuService.getMenuForCanteen(selectedStudentCanteenId, searchField.getText());
            if ("ALL".equalsIgnoreCase(selectedStudentCanteenId)) {
                selectedCanteenBanner.setText(String.format("📍 Showing All 3 Canteens (%d Menu Items Available)", items.size()));
            } else {
                Canteen c = canteenDAO.findById(selectedStudentCanteenId).orElseThrow();
                selectedCanteenBanner.setText(String.format("📍 %s [%s] — Busyness: %s (%d active orders | %d Dishes)",
                        c.getName(), c.getSpecialtyType(), c.getBusyStatus().toUpperCase(), c.getActiveOrderCount(), items.size()));
            }

            GridBagConstraints rgbc = new GridBagConstraints();
            rgbc.gridx = 0;
            rgbc.gridy = 0;
            rgbc.weightx = 1.0;
            rgbc.fill = GridBagConstraints.HORIZONTAL;
            rgbc.insets = new Insets(4, 6, 4, 6);

            for (FoodItem item : items) {
                Canteen itemCanteen = canteenDAO.findById(item.getCanteenId()).orElse(null);
                String busyStatus = itemCanteen != null ? itemCanteen.getBusyStatus() : "Nominal";
                int approxPrepMins = itemCanteen != null
                        ? itemCanteen.calculateApproxPrepTime(item.getBasePrepTimeMinutes())
                        : item.getBasePrepTimeMinutes();

                JPanel rowCard = new JPanel(new BorderLayout(10, 4));
                rowCard.setBackground(item.isAvailable() ? Color.WHITE : new Color(252, 242, 242));
                rowCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(215, 224, 238), 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));

                JCheckBox selectBox = new JCheckBox("Select");
                selectBox.setFont(new Font("SansSerif", Font.BOLD, 13));
                selectBox.setBackground(rowCard.getBackground());
                selectBox.setForeground(new Color(15, 40, 85));
                selectBox.setEnabled(item.isAvailable());

                String leftHtml = String.format(
                        "<html><b style='font-size:11pt;color:#0F172A;'>[%s] %s</b> &nbsp;<span style='color:#475569;'>(%s — %s)</span> %s<br/>" +
                        "<span style='color:#64748B;'>%s</span> &nbsp;|&nbsp; <b style='color:#155EC3;'>₹%.0f</b> &nbsp;|&nbsp; " +
                        "⏱ Approx Prep: <b>~%d mins</b> (%s) &nbsp;|&nbsp; <b style='color:#D97706;'>★ %.2f</b> (%d reviews)</html>",
                        item.getCanteenId(), item.getName(), item.getCategory(),
                        itemCanteen != null ? itemCanteen.getSpecialtyType() : "",
                        item.isAvailable() ? "" : "<b style='color:#DC2626;'>[SOLD OUT]</b>",
                        item.getNutritionalInfo(), item.getPrice(), approxPrepMins, busyStatus,
                        item.getCustomerRating(), item.getRatingCount()
                );
                JLabel infoLbl = new JLabel(leftHtml);

                JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
                leftWrap.setOpaque(false);
                leftWrap.add(selectBox);
                leftWrap.add(infoLbl);

                JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
                rightControls.setOpaque(false);

                JSpinner qtySpin = new JSpinner(new SpinnerNumberModel(1, 1, 25, 1));
                qtySpin.setPreferredSize(new Dimension(56, 30));

                JTextField specField = new JTextField(12);
                specField.setFont(new Font("SansSerif", Font.PLAIN, 12));
                specField.setToolTipText("Special requirement (e.g., less spicy, no onions)");

                boolean isFav = student.isFavourite(item.getFoodId());
                ModernButton favBtn = createStyledButton(
                        isFav ? "❤️ Favourited" : "♡ Favourite",
                        isFav ? DANGER_RED : new Color(241, 245, 249),
                        isFav ? Color.WHITE : new Color(30, 41, 59),
                        12
                );
                favBtn.addActionListener(ev -> {
                    boolean nowFav = userService.toggleStudentFavourite(student, item.getFoodId());
                    favBtn.setText(nowFav ? "❤️ Favourited" : "♡ Favourite");
                    favBtn.setColors(
                            nowFav ? DANGER_RED : new Color(241, 245, 249),
                            nowFav ? Color.WHITE : new Color(30, 41, 59)
                    );
                });

                rightControls.add(new JLabel("Qty:"));
                rightControls.add(qtySpin);
                rightControls.add(new JLabel("Special Note:"));
                rightControls.add(specField);
                rightControls.add(favBtn);

                rowCard.add(leftWrap, BorderLayout.CENTER);
                rowCard.add(rightControls, BorderLayout.EAST);

                currentRowControls.add(new FoodRowControl(item, selectBox, qtySpin, specField));
                foodRowsPanel.add(rowCard, rgbc);
                rgbc.gridy++;
            }

            rgbc.weighty = 1.0;
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            foodRowsPanel.add(filler, rgbc);

            foodRowsPanel.revalidate();
            foodRowsPanel.repaint();
        };

        // Card 0: All Canteens
        JPanel allCard = new JPanel();
        allCard.setLayout(new BoxLayout(allCard, BoxLayout.Y_AXIS));
        allCard.setBackground(Color.WHITE);
        allCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(165, 185, 220), 2, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel allTitle = new JLabel("All 3 Campus Canteens");
        allTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel allSub = new JLabel("View all 30 Veg, Non-Veg & Grill items");
        allSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ModernButton allBtn = createStyledButton("Show All 30 Dishes", DARK_SLATE, Color.WHITE, 12);
        allBtn.addActionListener(e -> {
            selectedStudentCanteenId = "ALL";
            populateMenuRows.run();
        });
        allCard.add(allTitle);
        allCard.add(Box.createVerticalStrut(4));
        allCard.add(allSub);
        allCard.add(Box.createVerticalStrut(8));
        allCard.add(allBtn);
        canteenCardsPanel.add(allCard);

        for (Canteen c : canteenDAO.findAll()) {
            JPanel cCard = new JPanel();
            cCard.setLayout(new BoxLayout(cCard, BoxLayout.Y_AXIS));
            cCard.setBackground(Color.WHITE);
            cCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(165, 185, 220), 2, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            String busy = c.getBusyStatus();
            Color busyColor = "Free".equals(busy) ? SUCCESS_GREEN
                    : ("Nominal".equals(busy) ? WARNING_AMBER : DANGER_RED);

            JLabel cTitle = new JLabel(c.getName());
            cTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel cType = new JLabel("Specialty: " + c.getSpecialtyType());
            cType.setFont(new Font("SansSerif", Font.PLAIN, 11));
            JLabel cBusy = new JLabel(String.format("How Busy: %s (%d active | %.2fx time)",
                    busy.toUpperCase(), c.getActiveOrderCount(), c.getPrepTimeMultiplier()));
            cBusy.setFont(new Font("SansSerif", Font.BOLD, 12));
            cBusy.setForeground(busyColor);

            ModernButton selectCanteenBtn = createStyledButton("Open " + c.getCanteenId() + " Menu (10 Dishes)", PRIMARY_BLUE, Color.WHITE, 12);
            selectCanteenBtn.addActionListener(e -> {
                selectedStudentCanteenId = c.getCanteenId();
                populateMenuRows.run();
            });

            cCard.add(cTitle);
            cCard.add(Box.createVerticalStrut(3));
            cCard.add(cType);
            cCard.add(Box.createVerticalStrut(3));
            cCard.add(cBusy);
            cCard.add(Box.createVerticalStrut(6));
            cCard.add(selectCanteenBtn);
            canteenCardsPanel.add(cCard);
        }

        searchBtn.addActionListener(e -> populateMenuRows.run());
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            selectedStudentCanteenId = "ALL";
            populateMenuRows.run();
        });

        populateMenuRows.run();

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        bottomBar.setBackground(Color.WHITE);
        ModernButton proceedBtn = createStyledButton("➡ Proceed with Selected Food Items", SUCCESS_GREEN, Color.WHITE, 15);
        proceedBtn.setPreferredSize(new Dimension(330, 42));

        proceedBtn.addActionListener(e -> {
            List<FoodRowControl> chosen = currentRowControls.stream()
                    .filter(rc -> rc.selectCheck.isSelected())
                    .toList();
            if (chosen.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please tick the 'Select' checkbox on at least one food item to place an order!",
                        "No Food Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String orderCanteenId = chosen.get(0).food.getCanteenId();
            double total = 0.0;
            StringBuilder summary = new StringBuilder("Selected Items:\n");
            for (FoodRowControl rc : chosen) {
                int qty = (int) rc.qtySpinner.getValue();
                double sub = qty * rc.food.getPrice();
                total += sub;
                String note = rc.specialReqField.getText().trim();
                summary.append(String.format("• %dx %s [%s] (₹%.0f) %s\n",
                        qty, rc.food.getName(), rc.food.getCanteenId(), sub, note.isEmpty() ? "" : "[Spec: " + note + "]"));
            }
            summary.append(String.format("\nTotal Order Amount: ₹%.2f (Your Wallet: ₹%.2f)", total, student.getWalletBalance()));

            JComboBox<String> orderTypeBox = new JComboBox<>(new String[]{"BUY_NOW", "PRE_ORDER"});
            JTextField preOrderTimeField = new JTextField("01:15 PM", 12);
            JComboBox<String> deliveryModeBox = new JComboBox<>(new String[]{"PICKUP", "CAMPUS_DELIVERY"});
            JComboBox<String> deliveryPointBox = new JComboBox<>(OrderService.CAMPUS_DELIVERY_POINTS);
            deliveryPointBox.setEnabled(false);
            JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"FOOD_WALLET", "PAY_ON_DELIVERY"});

            deliveryModeBox.addActionListener(ev ->
                    deliveryPointBox.setEnabled("CAMPUS_DELIVERY".equals(deliveryModeBox.getSelectedItem())));

            JPanel askPanel = new JPanel(new GridLayout(0, 2, 8, 8));
            askPanel.add(new JLabel("1. Buy Now / Pre-order:")); askPanel.add(orderTypeBox);
            askPanel.add(new JLabel("   Pre-Order Time (if Pre-order):")); askPanel.add(preOrderTimeField);
            askPanel.add(new JLabel("2. Pick-up / Campus Delivery:")); askPanel.add(deliveryModeBox);
            askPanel.add(new JLabel("   Campus Delivery Point:")); askPanel.add(deliveryPointBox);
            askPanel.add(new JLabel("3. Payment Method:")); askPanel.add(paymentMethodBox);

            JPanel dialogContainer = new JPanel(new BorderLayout(8, 8));
            JTextArea sumArea = new JTextArea(summary.toString(), 6, 40);
            sumArea.setEditable(false);
            dialogContainer.add(new JScrollPane(sumArea), BorderLayout.NORTH);
            dialogContainer.add(askPanel, BorderLayout.CENTER);

            int confirm = JOptionPane.showConfirmDialog(this, dialogContainer,
                    "Proceed to Checkout (Buy Now/Pre-order | Pick-up/Campus Delivery | Payment)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (confirm == JOptionPane.OK_OPTION) {
                Order.OrderType oType = Order.OrderType.valueOf((String) orderTypeBox.getSelectedItem());
                Order.DeliveryMode dMode = Order.DeliveryMode.valueOf((String) deliveryModeBox.getSelectedItem());
                String dPoint = dMode == Order.DeliveryMode.CAMPUS_DELIVERY
                        ? (String) deliveryPointBox.getSelectedItem() : "Canteen Pickup Counter";
                String schedTime = oType == Order.OrderType.PRE_ORDER ? preOrderTimeField.getText().trim() : "Immediate";
                String payMethod = (String) paymentMethodBox.getSelectedItem();

                Order newOrder = new Order(orderService.nextOrderId(), student.getUserId(), student.getUsername(),
                        orderCanteenId, oType, dMode, dPoint, schedTime, payMethod, false, LocalDateTime.now());
                for (FoodRowControl rc : chosen) {
                    int qty = (int) rc.qtySpinner.getValue();
                    newOrder.addItem(rc.food.getFoodId(), rc.food.getName(), qty, rc.food.getPrice(), rc.specialReqField.getText());
                }

                try {
                    orderService.placeStudentOrder(student, newOrder);
                    onHeaderUpdate.run();
                    populateMenuRows.run();
                    JOptionPane.showMessageDialog(this,
                            String.format("✅ Order %s Placed & Saved Permanently!\nDefault Status: Order Placed\nCanteen: %s\nMode: %s (%s)\nPayment: %s\nTotal: ₹%.2f",
                                    newOrder.getOrderId(), orderCanteenId, dMode, dPoint, payMethod, newOrder.getTotalAmount()));
                } catch (MealSynxException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bottomBar.add(proceedBtn);

        centerMenuContainer.add(searchBarPanel, BorderLayout.NORTH);
        centerMenuContainer.add(menuScroll, BorderLayout.CENTER);
        centerMenuContainer.add(bottomBar, BorderLayout.SOUTH);

        panel.add(canteenCardsPanel, BorderLayout.NORTH);
        panel.add(centerMenuContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStudentTrackOrderView(Customer student) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        String[] cols = {"Order ID", "Canteen", "Items & Special Notes", "Type", "Pick-up / Delivery Point", "Amount", "Live Progress Chain"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Order o : orderService.getOrdersForStudent(student.getUsername())) {
            boolean isDeliv = o.getDeliveryMode() == Order.DeliveryMode.CAMPUS_DELIVERY;
            String progressVisual = switch (o.getStatus()) {
                case PLACED -> "[●○○○] Order Placed";
                case PREPARING -> "[●●○○] Preparing in Kitchen";
                case PREPARED -> "[●●●○] Prepared (Ready for Pick-up)";
                case OUT_FOR_DELIVERY -> "[●●●○] Out for Delivery to " + o.getDeliveryPoint();
                case DELIVERED -> "[●●●●] Delivered";
                case COLLECTED -> "[●●●●] Collected";
                case REJECTED -> "[✕] Rejected";
            };
            model.addRow(new Object[]{
                    o.getOrderId(), o.getCanteenId(), o.getItemsSummary(),
                    o.getOrderType(), (isDeliv ? "Campus Delivery -> " : "Pick-up -> ") + o.getDeliveryPoint(),
                    String.format("₹%.2f", o.getTotalAmount()), progressVisual
            });
        }

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JLabel("🚚 Track Order — Live Progress (Order Placed ➔ Preparing ➔ Out for Delivery / Prepared ➔ Delivered / Collected):"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStudentPastOrdersView(Customer student, Runnable onHeaderUpdate, Runnable goToReviewTab) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        List<Order> myOrders = orderService.getOrdersForStudent(student.getUsername());
        String[] cols = {"Order ID", "Date/Time", "Canteen", "Items Ordered", "Delivery Point", "Payment", "Total (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Order o : myOrders) {
            model.addRow(new Object[]{
                    o.getOrderId(), o.getFormattedTimestamp(), o.getCanteenId(),
                    o.getItemsSummary(), o.getDeliveryPoint(), o.getPaymentMethod(),
                    String.format("₹%.2f", o.getTotalAmount()), o.getDisplayStatusLabel()
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        ModernButton reorderBtn = createStyledButton("🔄 Reorder Selected Order", PRIMARY_BLUE, Color.WHITE, 13);
        ModernButton reviewOrderBtn = createStyledButton("⭐ Submit Review & Feedback for Ordered Food", WARNING_AMBER, Color.WHITE, 13);

        reorderBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a past order in the table first to Reorder!");
                return;
            }
            Order pastOrder = myOrders.get(row);
            String[] payOpts = {"FOOD_WALLET", "PAY_ON_DELIVERY"};
            String payChoice = (String) JOptionPane.showInputDialog(this,
                    "Reordering " + pastOrder.getItemsSummary() + " (₹" + pastOrder.getTotalAmount() + ")\nChoose Payment Method:",
                    "One-Click Reorder", JOptionPane.QUESTION_MESSAGE, null, payOpts, payOpts[0]);
            if (payChoice != null) {
                try {
                    Order reordered = orderService.reorderPastOrder(student, pastOrder,
                            Order.OrderType.BUY_NOW, pastOrder.getDeliveryMode(),
                            pastOrder.getDeliveryPoint(), "Immediate", payChoice);
                    onHeaderUpdate.run();
                    model.insertRow(0, new Object[]{
                            reordered.getOrderId(), reordered.getFormattedTimestamp(), reordered.getCanteenId(),
                            reordered.getItemsSummary(), reordered.getDeliveryPoint(), reordered.getPaymentMethod(),
                            String.format("₹%.2f", reordered.getTotalAmount()), reordered.getDisplayStatusLabel()
                    });
                    JOptionPane.showMessageDialog(this, "✅ Reorder placed as " + reordered.getOrderId() + "!");
                } catch (MealSynxException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Reorder Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        reviewOrderBtn.addActionListener(e -> goToReviewTab.run());

        btnBar.add(reorderBtn);
        btnBar.add(reviewOrderBtn);

        panel.add(new JLabel("📜 Past Orders / Order History (Select any order to Reorder or Submit Review):"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStudentRatingsFeedbackView(Customer student) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        List<FoodItem> orderedFoods = userService.getFoodsOrderedByStudentForReview(student);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder("Submit Verified Review (Only Foods You Have Ordered Are Listed — Saved Forever)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> foodCombo = new JComboBox<>();
        for (FoodItem f : orderedFoods) {
            foodCombo.addItem(f.getFoodId() + " — " + f.getName() + " (Current Rating: ★" + f.getCustomerRating() + ")");
        }

        JComboBox<Integer> starsCombo = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JTextField commentField = new JTextField("The food was fresh and tasty!", 32);
        ModernButton submitReviewBtn = createStyledButton("⭐ Submit Review & Save Permanently", SUCCESS_GREEN, Color.WHITE, 13);

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Ordered Food Item:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(foodCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Star Rating (1-5):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(starsCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Your Review / Feedback:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(commentField, gbc);
        gbc.gridx = 1; gbc.gridy = 3; form.add(submitReviewBtn, gbc);

        String[] cols = {"Time", "Student", "Canteen", "Food Item", "Stars", "Comment", "NLP Taste", "NLP Spice", "NLP Quantity"};
        DefaultTableModel fbModel = new DefaultTableModel(cols, 0);
        Runnable loadReviews = () -> {
            fbModel.setRowCount(0);
            for (FeedbackDAO.FeedbackEntry fe : wasteService.getAllFeedbacks()) {
                fbModel.addRow(new Object[]{
                        fe.timestamp, fe.customerUsername, fe.canteenId, fe.foodName,
                        "★".repeat(fe.rating), fe.comment, fe.tasteSentiment, fe.spicinessSentiment, fe.quantitySentiment
                });
            }
        };
        loadReviews.run();

        submitReviewBtn.addActionListener(e -> {
            int idx = foodCombo.getSelectedIndex();
            if (idx < 0 || orderedFoods.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must order a food item before you can review it!");
                return;
            }
            FoodItem targetFood = orderedFoods.get(idx);
            int stars = (Integer) starsCombo.getSelectedItem();
            String comment = commentField.getText().trim();
            if (comment.isEmpty()) comment = "Good food!";

            FeedbackDAO.FeedbackEntry entry = wasteService.submitStudentFoodReview(student.getUsername(), targetFood, stars, comment);
            loadReviews.run();
            JOptionPane.showMessageDialog(this,
                    String.format("✅ Review Saved Permanently!\n'%s' live rating is now ★%.2f (%d reviews).\nNLP Sentiment -> Taste: %s | Spice: %s | Quantity: %s",
                            targetFood.getName(), targetFood.getCustomerRating(), targetFood.getRatingCount(),
                            entry.tasteSentiment, entry.spicinessSentiment, entry.quantitySentiment));
        });

        JTable fbTable = new JTable(fbModel);
        styleTable(fbTable);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(fbTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStudentHelpSupportView(Customer student) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel contactCard = new JPanel(new GridLayout(0, 1, 4, 4));
        contactCard.setBackground(new Color(238, 245, 255));
        contactCard.setBorder(BorderFactory.createTitledBorder("📞 MealSynx Campus Support Contact Desk"));
        contactCard.add(new JLabel("• 24x7 Support Contact Number : " + SupportService.SUPPORT_HELPLINE));
        contactCard.add(new JLabel("• Support Email               : " + SupportService.SUPPORT_EMAIL));
        contactCard.add(new JLabel("• Campus Helpdesk Office      : " + SupportService.SUPPORT_LOCATION));

        JPanel complaintForm = new JPanel(new BorderLayout(8, 8));
        complaintForm.setBorder(BorderFactory.createTitledBorder("📩 Complaint Box (For Non-Food Issues: Canteen Hygiene/Queue, App/Wallet Issue, Delivery Issue)"));

        JComboBox<String> issueCatBox = new JComboBox<>(new String[]{
                "Canteen Queue / Seating / Hygiene Issue",
                "App / Wallet Technical Issue",
                "Campus Delivery Point Issue",
                "Live Counter Billing Issue",
                "Other Non-Food Issue"
        });
        JTextArea complaintArea = new JTextArea(4, 45);
        ModernButton submitComplaintBtn = createStyledButton("📩 Submit Complaint to Support & Admin", DANGER_RED, Color.WHITE, 13);

        String[] cols = {"Ticket ID", "Time", "Student", "Issue Category", "Complaint Description", "Status"};
        DefaultTableModel tktModel = new DefaultTableModel(cols, 0);
        Runnable refreshTickets = () -> {
            tktModel.setRowCount(0);
            for (SupportTicket t : supportService.getAllTickets()) {
                tktModel.addRow(new Object[]{
                        t.getTicketId(), t.getFormattedTime(), t.getStudentUsername(),
                        t.getIssueCategory(), t.getDescription(), t.getStatus()
                });
            }
        };
        refreshTickets.run();

        submitComplaintBtn.addActionListener(e -> {
            String desc = complaintArea.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please describe the issue in the complaint box!");
                return;
            }
            SupportTicket t = supportService.submitComplaint(student.getUsername(), (String) issueCatBox.getSelectedItem(), desc);
            complaintArea.setText("");
            refreshTickets.run();
            JOptionPane.showMessageDialog(this, "✅ Complaint Ticket " + t.getTicketId() + " saved permanently!");
        });

        JPanel topFormRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topFormRow.add(new JLabel("Issue Type:"));
        topFormRow.add(issueCatBox);
        topFormRow.add(submitComplaintBtn);

        complaintForm.add(topFormRow, BorderLayout.NORTH);
        complaintForm.add(new JScrollPane(complaintArea), BorderLayout.CENTER);

        JPanel northWrap = new JPanel(new BorderLayout(8, 8));
        northWrap.add(contactCard, BorderLayout.NORTH);
        northWrap.add(complaintForm, BorderLayout.CENTER);

        JTable tktTable = new JTable(tktModel);
        styleTable(tktTable);
        panel.add(northWrap, BorderLayout.NORTH);
        panel.add(new JScrollPane(tktTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildWorkerPortal(Worker worker) {
        JPanel main = new JPanel(new BorderLayout());
        Canteen assignedCanteen = canteenDAO.findById(worker.getAssignedCanteenId()).orElseThrow();

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(18, 72, 55));
        topBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel workerHeaderLbl = new JLabel();
        workerHeaderLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        workerHeaderLbl.setForeground(Color.WHITE);

        ModernButton notifBtn = createStyledButton("🔔 Notifications (0)", new Color(255, 202, 40), new Color(20, 25, 35), 13);
        ModernButton logoutBtn = createStyledButton("Logout", DANGER_RED, Color.WHITE, 13);

        Runnable refreshWorkerHeader = () -> {
            orderService.recalculateAllCanteenActiveCounts();
            workerHeaderLbl.setText(String.format("👷 Worker: %s (%s)  |  🏢 %s [%s - %s]  |  🕒 Shift: %s",
                    worker.getUsername(), worker.getFullName(),
                    assignedCanteen.getName(), assignedCanteen.getSpecialtyType(),
                    assignedCanteen.getBusyStatus().toUpperCase(), worker.getShiftTiming()));
            notifBtn.setText("🔔 Notifications (" + worker.getNotifications().size() + ")");
        };
        refreshWorkerHeader.run();

        notifBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Worker & Canteen Notifications (" + worker.getUsername() + "):\n\n");
            for (String n : worker.getNotifications()) {
                sb.append("• ").append(n).append("\n\n");
            }
            JTextArea area = new JTextArea(sb.toString(), 14, 55);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area),
                    "Notifications — " + worker.getUsername(), JOptionPane.INFORMATION_MESSAGE);
        });

        logoutBtn.addActionListener(e -> {
            PersistentDataStore.getInstance().saveToDisk();
            authService.logout();
            rootPanel.add(buildLoginScreen(), "LOGIN");
            rootCardLayout.show(rootPanel, "LOGIN");
        });

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(notifBtn);
        topRight.add(logoutBtn);

        topBar.add(workerHeaderLbl, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        navBar.setBackground(new Color(228, 244, 236));
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(180, 210, 195)));

        ModernButton ordersRecBtn = createNavButton("📥 Orders Received ([ + ] Status)");
        ModernButton updateMenuBtn = createNavButton("🍔 Update Menu & Kitchen Plan");
        ModernButton warehouseBtn = createNavButton("📦 Warehouse Inventory & Expiry");
        ModernButton moneyMgmtBtn = createNavButton("💰 Sales, Expenses & Money Mgmt");
        ModernButton updatesBtn = createNavButton("📢 Admin Updates & Sim Request");
        ModernButton liveCounterBtn = createNavButton("🧾 Live Counter Billing (Offline POS)");

        navBar.add(ordersRecBtn);
        navBar.add(updateMenuBtn);
        navBar.add(warehouseBtn);
        navBar.add(moneyMgmtBtn);
        navBar.add(updatesBtn);
        navBar.add(liveCounterBtn);

        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.add(topBar, BorderLayout.NORTH);
        northWrap.add(navBar, BorderLayout.SOUTH);

        CardLayout workerCards = new CardLayout();
        JPanel contentArea = new JPanel(workerCards);

        ordersRecBtn.addActionListener(e -> {
            refreshWorkerHeader.run();
            contentArea.add(buildWorkerOrdersReceivedView(assignedCanteen, refreshWorkerHeader), "ORDERS_REC");
            workerCards.show(contentArea, "ORDERS_REC");
        });
        updateMenuBtn.addActionListener(e -> {
            refreshWorkerHeader.run();
            contentArea.add(buildWorkerUpdateMenuAndKitchenPlanView(assignedCanteen), "UPDATE_MENU");
            workerCards.show(contentArea, "UPDATE_MENU");
        });
        warehouseBtn.addActionListener(e -> {
            refreshWorkerHeader.run();
            contentArea.add(buildWorkerWarehouseInventoryView(assignedCanteen, refreshWorkerHeader), "WAREHOUSE");
            workerCards.show(contentArea, "WAREHOUSE");
        });
        moneyMgmtBtn.addActionListener(e -> {
            refreshWorkerHeader.run();
            contentArea.add(buildWorkerMoneyManagementView(assignedCanteen, refreshWorkerHeader), "MONEY_MGMT");
            workerCards.show(contentArea, "MONEY_MGMT");
        });
        updatesBtn.addActionListener(e -> {
            refreshWorkerHeader.run();
            contentArea.add(buildWorkerUpdatesAndSimRequestView(worker, assignedCanteen), "UPDATES");
            workerCards.show(contentArea, "UPDATES");
        });
        liveCounterBtn.addActionListener(e -> {
            refreshWorkerHeader.run();
            contentArea.add(buildWorkerLiveCounterView(assignedCanteen, refreshWorkerHeader), "LIVE_POS");
            workerCards.show(contentArea, "LIVE_POS");
        });

        contentArea.add(buildWorkerOrdersReceivedView(assignedCanteen, refreshWorkerHeader), "ORDERS_REC");
        workerCards.show(contentArea, "ORDERS_REC");

        main.add(northWrap, BorderLayout.NORTH);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildWorkerOrdersReceivedView(Canteen canteen, Runnable onUpdate) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel headerInfo = new JLabel(
                "<html><b>📥 Orders Received at " + canteen.getName() + "</b> — Press the <b>[ + ]</b> button next to any order to move it forward:<br/>" +
                "• <b>Campus Delivery Flow:</b> Order Placed ➔ Preparing ➔ Out for Delivery ➔ Delivered <i>(Irreversible & Saved Permanently)</i><br/>" +
                "• <b>Pick-up Flow:</b> Order Placed ➔ Preparing ➔ Prepared ➔ Collected <i>(Irreversible & Saved Permanently)</i></html>"
        );
        headerInfo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel rowsContainer = new JPanel(new GridBagLayout());
        rowsContainer.setBackground(new Color(245, 248, 252));

        Runnable[] rebuildRowsRef = new Runnable[1];
        rebuildRowsRef[0] = () -> {
            rowsContainer.removeAll();
            List<Order> orders = orderService.getOrdersForCanteen(canteen.getCanteenId());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(4, 6, 4, 6);

            for (Order o : orders) {
                JPanel rowCard = new JPanel(new BorderLayout(12, 6));
                rowCard.setBackground(Color.WHITE);
                rowCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));

                boolean isDelivery = o.getDeliveryMode() == Order.DeliveryMode.CAMPUS_DELIVERY;
                String flowChain = isDelivery
                        ? buildVisualChain(o.getStatus(), Order.OrderStatus.PLACED, Order.OrderStatus.PREPARING, Order.OrderStatus.OUT_FOR_DELIVERY, Order.OrderStatus.DELIVERED)
                        : buildVisualChain(o.getStatus(), Order.OrderStatus.PLACED, Order.OrderStatus.PREPARING, Order.OrderStatus.PREPARED, Order.OrderStatus.COLLECTED);

                String leftHtml = String.format(
                        "<html><b>%s</b> &nbsp;|&nbsp; 👤 <b>%s</b> &nbsp;|&nbsp; 🕒 %s (%s) &nbsp;|&nbsp; <b>₹%.2f</b> (%s)<br/>" +
                        "🍽 <b>Items:</b> %s &nbsp;&nbsp;|&nbsp;&nbsp; 📍 <b>%s:</b> %s</html>",
                        o.getOrderId(), o.getCustomerUsername(), o.getOrderType(), o.getScheduledTime(),
                        o.getTotalAmount(), o.getPaymentMethod(), o.getItemsSummary(),
                        isDelivery ? "Campus Delivery" : "Pick-up", o.getDeliveryPoint()
                );
                JLabel detailsLbl = new JLabel(leftHtml);
                detailsLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));

                JPanel rightActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
                rightActionPanel.setOpaque(false);

                JLabel chainLbl = new JLabel(flowChain);
                chainLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

                boolean canAdvance = o.canAdvanceStatus();
                ModernButton plusBtn = createStyledButton(
                        o.getPlusButtonLabel(),
                        canAdvance ? SUCCESS_GREEN : new Color(160, 170, 182),
                        Color.WHITE,
                        13
                );
                plusBtn.setEnabled(canAdvance);

                plusBtn.addActionListener(e -> {
                    boolean advanced = orderService.advanceOrderStatusByPlusButton(o.getOrderId());
                    if (advanced) {
                        onUpdate.run();
                        rebuildRowsRef[0].run();
                    }
                });

                rightActionPanel.add(chainLbl);
                rightActionPanel.add(plusBtn);

                rowCard.add(detailsLbl, BorderLayout.CENTER);
                rowCard.add(rightActionPanel, BorderLayout.EAST);
                rowsContainer.add(rowCard, gbc);
                gbc.gridy++;
            }

            gbc.weighty = 1.0;
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            rowsContainer.add(filler, gbc);

            rowsContainer.revalidate();
            rowsContainer.repaint();
        };

        rebuildRowsRef[0].run();

        JScrollPane scroll = new JScrollPane(rowsContainer);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        panel.add(headerInfo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private String buildVisualChain(Order.OrderStatus current, Order.OrderStatus... steps) {
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) sb.append(" <font color='#64748B'>➔</font> ");
            Order.OrderStatus st = steps[i];
            if (st == current) {
                sb.append("<font color='#155EC3'><b>[").append(Order.formatStatusName(st)).append("]</b></font>");
            } else {
                sb.append("<font color='#64748B'>").append(Order.formatStatusName(st)).append("</font>");
            }
        }
        sb.append("</html>");
        return sb.toString();
    }

    private JPanel buildWorkerLiveCounterView(Canteen canteen, Runnable onUpdate) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel(String.format(
                "🧾 Live Counter & Offline Walk-In Billing — %s (Current Busyness: %s | Revenue Today: ₹%,.2f)",
                canteen.getName(), canteen.getBusyStatus().toUpperCase(), canteen.getTotalSalesRevenue()));
        title.setFont(new Font("SansSerif", Font.BOLD, 15));

        JPanel itemsGrid = new JPanel(new GridBagLayout());
        itemsGrid.setBackground(Color.WHITE);

        class PosRow {
            final FoodItem food;
            final JCheckBox tick;
            final JSpinner qty;
            PosRow(FoodItem f, JCheckBox t, JSpinner q) { this.food = f; this.tick = t; this.qty = q; }
        }
        List<PosRow> posRows = new ArrayList<>();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;

        for (FoodItem item : menuService.getMenuForCanteen(canteen.getCanteenId(), "")) {
            JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
            r.setBackground(Color.WHITE);
            r.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)));

            JCheckBox cb = new JCheckBox(item.getName() + " (" + item.getCategory() + ")");
            cb.setFont(new Font("SansSerif", Font.BOLD, 13));
            cb.setBackground(Color.WHITE);
            cb.setForeground(new Color(20, 45, 85));
            cb.setPreferredSize(new Dimension(380, 28));
            cb.setEnabled(item.isAvailable());

            JLabel priceLbl = new JLabel(String.format("Price: ₹%.0f   |   ★%.2f", item.getPrice(), item.getCustomerRating()));
            priceLbl.setPreferredSize(new Dimension(180, 28));

            JSpinner qSpin = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
            qSpin.setPreferredSize(new Dimension(60, 28));

            r.add(cb);
            r.add(priceLbl);
            r.add(new JLabel("Counter Qty:"));
            r.add(qSpin);
            posRows.add(new PosRow(item, cb, qSpin));
            itemsGrid.add(r, gbc);
            gbc.gridy++;
        }

        JPanel bottomPosBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField walkInNameField = new JTextField("Walk-In Student", 14);
        JComboBox<String> posPayMode = new JComboBox<>(new String[]{"LIVE_COUNTER_CASH", "LIVE_COUNTER_UPI_QR"});
        ModernButton billBtn = createStyledButton("🧾 Generate Live Counter Bill & Record Offline Purchase", SUCCESS_GREEN, Color.WHITE, 14);

        billBtn.addActionListener(e -> {
            List<Order.OrderLineItem> items = new ArrayList<>();
            for (PosRow pr : posRows) {
                if (pr.tick.isSelected()) {
                    int q = (int) pr.qty.getValue();
                    items.add(new Order.OrderLineItem(pr.food.getFoodId(), pr.food.getName(), q, pr.food.getPrice(), "Live Counter"));
                }
            }
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tick at least one menu item for the live counter bill!");
                return;
            }
            Order posOrder = orderService.createLiveCounterOfflineOrder(
                    canteen.getCanteenId(), walkInNameField.getText(), items, (String) posPayMode.getSelectedItem());
            onUpdate.run();
            for (PosRow pr : posRows) pr.tick.setSelected(false);

            JOptionPane.showMessageDialog(this,
                    String.format("✅ Live Counter Receipt %s\nCanteen: %s\nCustomer: %s\nItems: %s\nTotal Paid: ₹%.2f (%s)\nStatus: Collected (Saved Permanently)",
                            posOrder.getOrderId(), canteen.getName(), posOrder.getCustomerUsername(),
                            posOrder.getItemsSummary(), posOrder.getTotalAmount(), posOrder.getPaymentMethod()),
                    "Live Counter Receipt", JOptionPane.INFORMATION_MESSAGE);
        });

        bottomPosBar.add(new JLabel("Walk-in Student / Token:"));
        bottomPosBar.add(walkInNameField);
        bottomPosBar.add(new JLabel("Payment:"));
        bottomPosBar.add(posPayMode);
        bottomPosBar.add(billBtn);

        JScrollPane scroll = new JScrollPane(itemsGrid);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottomPosBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWorkerUpdateMenuAndKitchenPlanView(Canteen canteen) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = {"Food ID", "Food Name", "Category", "Price (₹)", "Base Prep (m)", "Live Rating", "Available", "System Estimated Kitchen Prep", "Worker Kitchen Plan Target"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<FoodItem> itemsList = new ArrayList<>();
        Runnable loadMenu = () -> {
            model.setRowCount(0);
            itemsList.clear();
            itemsList.addAll(menuService.getMenuForCanteen(canteen.getCanteenId(), ""));
            WeatherSnapshot w = eventWeatherService.getCurrentWeather();
            double evMult = predictionService.getActiveEventMultiplier();

            for (FoodItem f : itemsList) {
                int sysEst = f.getSystemEstimatedKitchenPrep(w.getCategoryDemandMultiplier(f.getCategory()), evMult);
                int target = f.getCustomKitchenPlanTarget() > 0 ? f.getCustomKitchenPlanTarget() : sysEst;
                model.addRow(new Object[]{
                        f.getFoodId(), f.getName(), f.getCategory(), String.format("₹%.2f", f.getPrice()),
                        f.getBasePrepTimeMinutes() + " mins", String.format("★%.2f (%d)", f.getCustomerRating(), f.getRatingCount()),
                        f.isAvailable() ? "Available" : "SOLD OUT",
                        sysEst + " plates (Weather/Calendar ML)",
                        target + " plates"
                });
            }
        };
        loadMenu.run();

        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        ModernButton addFoodBtn = createStyledButton("➕ Add Food Item", SUCCESS_GREEN, Color.WHITE, 13);
        ModernButton removeFoodBtn = createStyledButton("🗑 Remove Food Item", DANGER_RED, Color.WHITE, 13);
        ModernButton changePriceBtn = createStyledButton("💲 Change Price", PRIMARY_BLUE, Color.WHITE, 13);
        ModernButton toggleAvailBtn = createStyledButton("🔄 Toggle Available/Sold Out", WARNING_AMBER, Color.WHITE, 13);
        ModernButton kitchenPlanBtn = createStyledButton("📋 Set Kitchen Plan Target", PURPLE_ACCENT, Color.WHITE, 13);

        addFoodBtn.addActionListener(e -> {
            JTextField nameF = new JTextField(16);
            JTextField catF = new JTextField("Main Course", 16);
            JTextField priceF = new JTextField("95", 10);
            JTextField costF = new JTextField("42", 10);
            JTextField prepF = new JTextField("10", 10);
            JTextField nutF = new JTextField("520 kcal | 18g Protein", 18);

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Food Name:")); p.add(nameF);
            p.add(new JLabel("Category:")); p.add(catF);
            p.add(new JLabel("Selling Price (₹):")); p.add(priceF);
            p.add(new JLabel("Prep Cost (₹):")); p.add(costF);
            p.add(new JLabel("Base Prep Time (mins):")); p.add(prepF);
            p.add(new JLabel("Nutritional Info:")); p.add(nutF);

            if (JOptionPane.showConfirmDialog(this, p, "Add New Food to " + canteen.getName(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    menuService.addNewFoodItem(canteen.getCanteenId(), nameF.getText().trim(), catF.getText().trim(),
                            Double.parseDouble(priceF.getText().trim()), Double.parseDouble(costF.getText().trim()),
                            Integer.parseInt(prepF.getText().trim()), nutF.getText().trim());
                    loadMenu.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid numeric input!");
                }
            }
        });

        removeFoodBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                FoodItem f = itemsList.get(row);
                menuService.removeFoodItem(f.getFoodId());
                loadMenu.run();
            }
        });

        changePriceBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                FoodItem f = itemsList.get(row);
                String val = JOptionPane.showInputDialog(this, "Enter new price (₹) for " + f.getName() + ":", f.getPrice());
                if (val != null) {
                    try {
                        menuService.updatePrice(f.getFoodId(), Double.parseDouble(val.trim()));
                        loadMenu.run();
                    } catch (Exception ignored) {}
                }
            }
        });

        toggleAvailBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                menuService.toggleAvailability(itemsList.get(row).getFoodId());
                loadMenu.run();
            }
        });

        kitchenPlanBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                FoodItem f = itemsList.get(row);
                String val = JOptionPane.showInputDialog(this, "Enter Today's Kitchen Plan Preparation Target (plates) for " + f.getName() + ":", 250);
                if (val != null) {
                    try {
                        menuService.updateKitchenPlanTarget(f.getFoodId(), Integer.parseInt(val.trim()));
                        loadMenu.run();
                    } catch (Exception ignored) {}
                }
            }
        });

        btnBar.add(addFoodBtn);
        btnBar.add(removeFoodBtn);
        btnBar.add(changePriceBtn);
        btnBar.add(toggleAvailBtn);
        btnBar.add(kitchenPlanBtn);

        panel.add(new JLabel("🍔 Update Menu & Kitchen Preparation Plan (All Edits Saved Permanently):"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWorkerWarehouseInventoryView(Canteen canteen, Runnable onUpdate) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = {"Ingredient ID", "Ingredient Name", "Purchased Date", "Expiry Date", "Qty Present", "Needed for Next Day", "Recommended Purchase", "Unit Cost", "Quality & Stock Alert"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<IngredientStock> stocks = new ArrayList<>();
        Runnable loadStock = () -> {
            model.setRowCount(0);
            stocks.clear();
            stocks.addAll(inventoryService.getInventoryForCanteen(canteen.getCanteenId()));
            for (IngredientStock s : stocks) {
                model.addRow(new Object[]{
                        s.getIngredientId(), s.getName(), s.getPurchaseDate(), s.getExpiryDate(),
                        String.format("%.1f %s", s.getCurrentStock(), s.getUnit()),
                        String.format("%.1f %s", s.getNextDayRequiredQty(), s.getUnit()),
                        String.format("%.1f %s", s.getRecommendedPurchaseQty(), s.getUnit()),
                        String.format("₹%.2f/%s", s.getCostPerUnit(), s.getUnit()),
                        s.getQualityStatusLabel()
                });
            }
            onUpdate.run();
        };
        loadStock.run();

        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        ModernButton purchaseBtn = createStyledButton("🛒 Purchase Selected Ingredient", PRIMARY_BLUE, Color.WHITE, 13);
        ModernButton consumeBtn = createStyledButton("📉 Record Daily Consumption", WARNING_AMBER, Color.WHITE, 13);
        ModernButton addNewIngBtn = createStyledButton("➕ Add New Warehouse Ingredient", SUCCESS_GREEN, Color.WHITE, 13);

        purchaseBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an ingredient row first!");
                return;
            }
            IngredientStock s = stocks.get(row);
            String qtyStr = JOptionPane.showInputDialog(this,
                    String.format("Purchase %s (Cost: ₹%.2f/%s | Recommended: %.1f %s)\nEnter Quantity to Purchase:",
                            s.getName(), s.getCostPerUnit(), s.getUnit(), s.getRecommendedPurchaseQty(), s.getUnit()),
                    Math.max(10.0, s.getRecommendedPurchaseQty()));
            if (qtyStr != null) {
                try {
                    double q = Double.parseDouble(qtyStr.trim());
                    inventoryService.recordIngredientPurchase(s.getIngredientId(), q, 14);
                    loadStock.run();
                    JOptionPane.showMessageDialog(this,
                            String.format("✅ Purchased %.1f %s of %s (₹%.2f added to Expenses & Saved Permanently).",
                                    q, s.getUnit(), s.getName(), q * s.getCostPerUnit()));
                } catch (Exception ignored) {}
            }
        });

        consumeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            IngredientStock s = stocks.get(row);
            String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity consumed today (" + s.getUnit() + "):", "5.0");
            if (qtyStr != null) {
                try {
                    inventoryService.recordDailyConsumption(s.getIngredientId(), Double.parseDouble(qtyStr.trim()));
                    loadStock.run();
                } catch (Exception ignored) {}
            }
        });

        addNewIngBtn.addActionListener(e -> {
            JTextField nameF = new JTextField(14);
            JTextField qtyF = new JTextField("20", 8);
            JTextField nextReqF = new JTextField("15", 8);
            JTextField unitF = new JTextField("kg", 6);
            JTextField costF = new JTextField("80", 8);
            JTextField expDaysF = new JTextField("10", 6);

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Ingredient Name:")); p.add(nameF);
            p.add(new JLabel("Purchased Qty:")); p.add(qtyF);
            p.add(new JLabel("Needed for Next Day:")); p.add(nextReqF);
            p.add(new JLabel("Unit (kg/L/pcs):")); p.add(unitF);
            p.add(new JLabel("Cost per Unit (₹):")); p.add(costF);
            p.add(new JLabel("Expiry Days from Today:")); p.add(expDaysF);

            if (JOptionPane.showConfirmDialog(this, p, "Purchase & Add New Ingredient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    inventoryService.addNewWarehouseIngredient(canteen.getCanteenId(), nameF.getText().trim(), "General",
                            Double.parseDouble(qtyF.getText().trim()), 5.0, Double.parseDouble(nextReqF.getText().trim()),
                            unitF.getText().trim(), Double.parseDouble(costF.getText().trim()), Integer.parseInt(expDaysF.getText().trim()));
                    loadStock.run();
                } catch (Exception ignored) {}
            }
        });

        btnBar.add(purchaseBtn);
        btnBar.add(consumeBtn);
        btnBar.add(addNewIngBtn);

        panel.add(new JLabel("📦 Warehouse Inventory & Expiry Tracking — Notifications Trigger Automatically When Quality/Expiry or Stock Goes Low:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWorkerMoneyManagementView(Canteen canteen, Runnable onUpdate) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel summaryPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createTitledBorder("💰 Real-Time Canteen Financial Statement — " + canteen.getName()));

        JLabel revLbl = new JLabel();
        JLabel ingExpLbl = new JLabel();
        JLabel wagesLbl = new JLabel();
        JLabel utilLbl = new JLabel();
        JLabel totalExpLbl = new JLabel();
        JLabel netPlLbl = new JLabel();
        JLabel warningBanner = new JLabel();
        warningBanner.setFont(new Font("SansSerif", Font.BOLD, 15));

        Runnable refreshFinance = () -> {
            revLbl.setText(String.format("1. Total Sales Revenue (Online + Live Counter Orders) : ₹%,.2f", canteen.getTotalSalesRevenue()));
            ingExpLbl.setText(String.format("2. Spent on Purchasing Required Ingredients           : ₹%,.2f", canteen.getIngredientPurchaseExpenses()));
            wagesLbl.setText(String.format("3. Worker Wages & Staff Salaries                      : ₹%,.2f", canteen.getWorkerWagesExpenses()));
            utilLbl.setText(String.format("4. Utilities (Electricity/Current, Gas, Water)        : ₹%,.2f", canteen.getUtilitiesExpenses()));
            totalExpLbl.setText(String.format("5. Total Canteen Expenditure (2 + 3 + 4)              : ₹%,.2f", canteen.getTotalExpenses()));

            double pl = canteen.getNetProfitOrLoss();
            if (pl >= 0) {
                netPlLbl.setText(String.format("✅ NET PROFIT : +₹%,.2f (Healthy Margin)", pl));
                netPlLbl.setForeground(SUCCESS_GREEN);
                warningBanner.setText("Status: Operating Profitably (Revenue exceeds Expenses)");
                warningBanner.setForeground(SUCCESS_GREEN);
            } else {
                netPlLbl.setText(String.format("🚨 NET LOSS : -₹%,.2f", Math.abs(pl)));
                netPlLbl.setForeground(DANGER_RED);
                warningBanner.setText("🚨 WARNING: CANTEEN IS GOING INTO LOSS! Total Expenses exceed Revenue by ₹" + String.format("%,.2f", Math.abs(pl)));
                warningBanner.setForeground(DANGER_RED);
            }
            onUpdate.run();
            panel.repaint();
        };
        refreshFinance.run();

        Font f = new Font("Monospaced", Font.BOLD, 14);
        revLbl.setFont(f); ingExpLbl.setFont(f); wagesLbl.setFont(f); utilLbl.setFont(f); totalExpLbl.setFont(f); netPlLbl.setFont(f);

        summaryPanel.add(revLbl);
        summaryPanel.add(ingExpLbl);
        summaryPanel.add(wagesLbl);
        summaryPanel.add(utilLbl);
        summaryPanel.add(totalExpLbl);
        summaryPanel.add(netPlLbl);
        summaryPanel.add(warningBanner);

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);

                double maxVal = Math.max(1000.0, Math.max(canteen.getTotalSalesRevenue(), canteen.getTotalExpenses()) * 1.15);
                String[] labels = {"Sales Revenue", "Ingredient Cost", "Worker Wages", "Utilities (Current)", "Total Expenses"};
                double[] vals = {
                        canteen.getTotalSalesRevenue(),
                        canteen.getIngredientPurchaseExpenses(),
                        canteen.getWorkerWagesExpenses(),
                        canteen.getUtilitiesExpenses(),
                        canteen.getTotalExpenses()
                };
                Color[] colors = {
                        SUCCESS_GREEN, PRIMARY_BLUE,
                        WARNING_AMBER, PURPLE_ACCENT,
                        canteen.isRunningInLoss() ? DANGER_RED : DARK_SLATE
                };

                int barWidth = Math.max(50, (w - 160) / labels.length - 30);
                int baseY = h - 45;
                int maxBarH = h - 90;

                for (int i = 0; i < labels.length; i++) {
                    int x = 60 + i * (barWidth + 35);
                    int barH = (int) ((vals[i] / maxVal) * maxBarH);
                    g2.setColor(colors[i]);
                    g2.fillRoundRect(x, baseY - barH, barWidth, barH, 8, 8);
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.drawString(String.format("₹%,.0f", vals[i]), x + 4, baseY - barH - 6);
                    g2.drawString(labels[i], x, baseY + 20);
                }
            }
        };
        chartPanel.setBorder(BorderFactory.createTitledBorder("Graphical Financial Breakdown"));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        ModernButton addWageBtn = createStyledButton("➕ Record Worker Wage Payout", PRIMARY_BLUE, Color.WHITE, 13);
        ModernButton addUtilBtn = createStyledButton("⚡ Record Electricity / Utility Bill", WARNING_AMBER, Color.WHITE, 13);

        addWageBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(this, "Enter Worker Wage amount (₹) to record:", "2500");
            if (amt != null) {
                try {
                    workerService.recordWagesOrUtilitiesExpense(canteen.getCanteenId(), "WORKER_WAGES", Double.parseDouble(amt.trim()));
                    refreshFinance.run();
                } catch (Exception ignored) {}
            }
        });

        addUtilBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(this, "Enter Electricity / Current / Gas Utility amount (₹) to record:", "1800");
            if (amt != null) {
                try {
                    workerService.recordWagesOrUtilitiesExpense(canteen.getCanteenId(), "UTILITIES", Double.parseDouble(amt.trim()));
                    refreshFinance.run();
                } catch (Exception ignored) {}
            }
        });

        btnBar.add(addWageBtn);
        btnBar.add(addUtilBtn);

        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWorkerUpdatesAndSimRequestView(Worker worker, Canteen canteen) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        DefaultListModel<String> updatesModel = new DefaultListModel<>();
        for (String u : canteen.getAdminUpdatesFeed()) {
            updatesModel.addElement(u);
        }
        JList<String> updatesList = new JList<>(updatesModel);
        updatesList.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel reqPanel = new JPanel(new BorderLayout(8, 8));
        reqPanel.setBorder(BorderFactory.createTitledBorder("🧠 Request a What-If Simulation from Admin (Waiter / Worker Request)"));

        JComboBox<String> scenarioBox = new JComboBox<>(new String[]{"EVENT_SURGE", "ATTENDANCE_DROP", "PRICE_ELASTICITY"});
        JTextField detailsField = new JTextField("Please simulate impact of +500 extra students for tomorrow's special lunch at " + canteen.getName(), 36);
        ModernButton sendReqBtn = createStyledButton("📤 Send Simulation Request to Admin", PRIMARY_BLUE, Color.WHITE, 13);

        sendReqBtn.addActionListener(e -> {
            SimulationRequest req = workerService.submitWhatIfSimulationRequest(
                    worker.getUsername(), canteen.getCanteenId(),
                    (String) scenarioBox.getSelectedItem(), detailsField.getText().trim());
            JOptionPane.showMessageDialog(this,
                    "✅ Request " + req.getRequestId() + " saved & sent to Admin!");
        });

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formRow.add(new JLabel("Scenario Type:"));
        formRow.add(scenarioBox);
        formRow.add(new JLabel("Details:"));
        formRow.add(detailsField);
        formRow.add(sendReqBtn);

        reqPanel.add(formRow, BorderLayout.CENTER);

        panel.add(new JLabel("📢 Official Updates & Alerts Received from Admin for " + canteen.getName() + ":"), BorderLayout.NORTH);
        panel.add(new JScrollPane(updatesList), BorderLayout.CENTER);
        panel.add(reqPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAdminPortal(Admin admin) {
        JPanel main = new JPanel(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(32, 22, 68));
        topBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel adminTitleLbl = new JLabel("👨‍💼 Admin: " + admin.getUsername() + " (" + admin.getFullName() + ")");
        adminTitleLbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        adminTitleLbl.setForeground(Color.WHITE);

        CardLayout adminCards = new CardLayout();
        JPanel contentArea = new JPanel(adminCards);

        ModernButton quickAddStaffTopBtn = createStyledButton("👥 + Add Workers / Admins", SUCCESS_GREEN, Color.WHITE, 13);
        quickAddStaffTopBtn.addActionListener(e -> {
            contentArea.add(buildAdminManageWorkersAndAdminsView(), "MANAGE_STAFF");
            adminCards.show(contentArea, "MANAGE_STAFF");
        });

        ModernButton alertCanteenTopBtn = createStyledButton("📢 Alert Canteen", WARNING_AMBER, Color.WHITE, 13);
        alertCanteenTopBtn.addActionListener(e -> openAdminAlertCanteenDialog("GENERAL PERFORMANCE & OPS UPDATE", ""));

        ModernButton notifBtn = createStyledButton("🔔 Notifications (" + admin.getNotifications().size() + ")", new Color(255, 202, 40), new Color(20, 25, 35), 13);
        notifBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Admin Notifications & Worker Requests:\n\n");
            for (String n : admin.getNotifications()) sb.append("• ").append(n).append("\n\n");
            JTextArea area = new JTextArea(sb.toString(), 14, 55);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Admin Notifications", JOptionPane.INFORMATION_MESSAGE);
        });

        ModernButton logoutBtn = createStyledButton("Logout", DANGER_RED, Color.WHITE, 13);
        logoutBtn.addActionListener(e -> {
            PersistentDataStore.getInstance().saveToDisk();
            authService.logout();
            rootPanel.add(buildLoginScreen(), "LOGIN");
            rootCardLayout.show(rootPanel, "LOGIN");
        });

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        topRight.add(quickAddStaffTopBtn);
        topRight.add(alertCanteenTopBtn);
        topRight.add(notifBtn);
        topRight.add(logoutBtn);

        topBar.add(adminTitleLbl, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        navBar.setBackground(new Color(235, 230, 248));
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(195, 185, 225)));

        ModernButton dashBtn = createNavButton("📊 Dashboard (Home)");
        ModernButton updateBalBtn = createNavButton("💳 Update Balance");
        ModernButton manageStaffBtn = createNavButton("👥 Add Workers & Admins");
        ModernButton statsPerfBtn = createNavButton("📈 Stats & Performance");
        ModernButton whatIfBtn = createNavButton("🧠 What-If Simulator");
        ModernButton wasteLoopBtn = createNavButton("🗑️ Waste & Feedback Loop");
        ModernButton eventsWeatherBtn = createNavButton("📅 Live Weather & Calendar");

        navBar.add(dashBtn);
        navBar.add(updateBalBtn);
        navBar.add(manageStaffBtn);
        navBar.add(statsPerfBtn);
        navBar.add(whatIfBtn);
        navBar.add(wasteLoopBtn);
        navBar.add(eventsWeatherBtn);

        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.add(topBar, BorderLayout.NORTH);
        northWrap.add(navBar, BorderLayout.SOUTH);

        dashBtn.addActionListener(e -> {
            contentArea.add(buildAdminGraphicalDashboardView(() -> {
                contentArea.add(buildAdminManageWorkersAndAdminsView(), "MANAGE_STAFF");
                adminCards.show(contentArea, "MANAGE_STAFF");
            }), "DASH");
            adminCards.show(contentArea, "DASH");
        });
        updateBalBtn.addActionListener(e -> {
            contentArea.add(buildAdminUpdateBalanceView(), "UPDATE_BAL");
            adminCards.show(contentArea, "UPDATE_BAL");
        });
        manageStaffBtn.addActionListener(e -> {
            contentArea.add(buildAdminManageWorkersAndAdminsView(), "MANAGE_STAFF");
            adminCards.show(contentArea, "MANAGE_STAFF");
        });
        statsPerfBtn.addActionListener(e -> {
            contentArea.add(buildAdminStatsAndPerformanceView(), "STATS");
            adminCards.show(contentArea, "STATS");
        });
        whatIfBtn.addActionListener(e -> {
            contentArea.add(buildAdminWhatIfSimulatorView(), "WHAT_IF");
            adminCards.show(contentArea, "WHAT_IF");
        });
        wasteLoopBtn.addActionListener(e -> {
            contentArea.add(buildAdminWasteAndFeedbackLoopView(), "WASTE_LOOP");
            adminCards.show(contentArea, "WASTE_LOOP");
        });
        eventsWeatherBtn.addActionListener(e -> {
            contentArea.add(buildAdminEventsWeatherAndCalendarView(), "EVENTS_WEATHER");
            adminCards.show(contentArea, "EVENTS_WEATHER");
        });

        contentArea.add(buildAdminGraphicalDashboardView(() -> {
            contentArea.add(buildAdminManageWorkersAndAdminsView(), "MANAGE_STAFF");
            adminCards.show(contentArea, "MANAGE_STAFF");
        }), "DASH");
        adminCards.show(contentArea, "DASH");

        main.add(northWrap, BorderLayout.NORTH);
        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildAdminManageWorkersAndAdminsView() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel formsGrid = new JPanel(new GridLayout(1, 2, 16, 0));

        JPanel workerForm = new JPanel(new GridBagLayout());
        workerForm.setBackground(Color.WHITE);
        workerForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                "👷 Add New Worker to Respective Canteen (Saved Permanently)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13), new Color(20, 55, 115)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField wUserField = new JTextField("Cant1workB", 14);
        JTextField wNameField = new JTextField("Surya Prakash", 14);
        JTextField wPassField = new JTextField("Cant1workB123*", 14);
        JComboBox<String> wCanteenBox = new JComboBox<>(new String[]{
                "CAN-1 (Canteen 1 — Annapoorna Pure Veg)",
                "CAN-2 (Canteen 2 — Spice Route Non-Veg)",
                "CAN-3 (Canteen 3 — Campus Grills & Fast Food)"
        });
        JTextField wShiftField = new JTextField("02:00 PM – 10:00 PM", 14);
        JTextField wSpecField = new JTextField("Counter & Kitchen Operations", 14);
        ModernButton addWorkerBtn = createStyledButton("➕ Add Worker to Selected Canteen", SUCCESS_GREEN, Color.WHITE, 13);

        wUserField.addCaretListener(e -> {
            String u = wUserField.getText().trim();
            if (!u.isEmpty()) wPassField.setText(u + "123*");
        });

        gbc.gridx = 0; gbc.gridy = 0; workerForm.add(new JLabel("Worker Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; workerForm.add(wUserField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; workerForm.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; workerForm.add(wNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; workerForm.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; workerForm.add(wPassField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; workerForm.add(new JLabel("Assign Canteen:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; workerForm.add(wCanteenBox, gbc);
        gbc.gridx = 0; gbc.gridy = 4; workerForm.add(new JLabel("Shift Timings:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; workerForm.add(wShiftField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; workerForm.add(new JLabel("Specialization:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; workerForm.add(wSpecField, gbc);
        gbc.gridx = 1; gbc.gridy = 6; workerForm.add(addWorkerBtn, gbc);

        JPanel adminForm = new JPanel(new GridBagLayout());
        adminForm.setBackground(Color.WHITE);
        adminForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 230)),
                "👨‍💼 Add New Admin Account (Saved Permanently)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13), new Color(45, 25, 95)
        ));
        GridBagConstraints agbc = new GridBagConstraints();
        agbc.insets = new Insets(6, 8, 6, 8);
        agbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField aUserField = new JTextField("admin2", 14);
        JTextField aNameField = new JTextField("Dr. R. Meenakshi", 14);
        JTextField aPassField = new JTextField("admin2123*", 14);
        JTextField aDeptField = new JTextField("Campus Food Quality & Analytics", 14);
        ModernButton addAdminBtn = createStyledButton("➕ Add New Admin", PURPLE_ACCENT, Color.WHITE, 13);

        aUserField.addCaretListener(e -> {
            String u = aUserField.getText().trim();
            if (!u.isEmpty()) aPassField.setText(u + "123*");
        });

        agbc.gridx = 0; agbc.gridy = 0; adminForm.add(new JLabel("Admin Username:"), agbc);
        agbc.gridx = 1; agbc.gridy = 0; adminForm.add(aUserField, agbc);
        agbc.gridx = 0; agbc.gridy = 1; adminForm.add(new JLabel("Full Name:"), agbc);
        agbc.gridx = 1; agbc.gridy = 1; adminForm.add(aNameField, agbc);
        agbc.gridx = 0; agbc.gridy = 2; adminForm.add(new JLabel("Password:"), agbc);
        agbc.gridx = 1; agbc.gridy = 2; adminForm.add(aPassField, agbc);
        agbc.gridx = 0; agbc.gridy = 3; adminForm.add(new JLabel("Department:"), agbc);
        agbc.gridx = 1; agbc.gridy = 3; adminForm.add(aDeptField, agbc);
        agbc.gridx = 1; agbc.gridy = 4; adminForm.add(addAdminBtn, agbc);

        formsGrid.add(workerForm);
        formsGrid.add(adminForm);

        String[] wCols = {"Worker ID", "Username (Login)", "Password", "Full Name", "Assigned Canteen", "Shift Timings", "Specialization"};
        DefaultTableModel wModel = new DefaultTableModel(wCols, 0);

        String[] aCols = {"Admin ID", "Username (Login)", "Password", "Full Name", "Admin Code", "Department"};
        DefaultTableModel aModel = new DefaultTableModel(aCols, 0);

        Runnable reloadStaffTables = () -> {
            wModel.setRowCount(0);
            for (Worker w : authService.getAllWorkers()) {
                wModel.addRow(new Object[]{
                        w.getUserId(), w.getUsername(), w.getPassword(), w.getFullName(),
                        w.getAssignedCanteenId(), w.getShiftTiming(), w.getSpecialization()
                });
            }
            aModel.setRowCount(0);
            for (Admin a : authService.getAllAdmins()) {
                aModel.addRow(new Object[]{
                        a.getUserId(), a.getUsername(), a.getPassword(), a.getFullName(),
                        a.getRegisterNo(), a.getDepartment()
                });
            }
        };
        reloadStaffTables.run();

        addWorkerBtn.addActionListener(e -> {
            String uname = wUserField.getText().trim();
            if (uname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a Worker Username!");
                return;
            }
            String cSel = (String) wCanteenBox.getSelectedItem();
            String canteenId = cSel.substring(0, 5);
            Worker created = authService.addNewWorkerByAdmin(
                    uname, wNameField.getText().trim(), wPassField.getText().trim(),
                    canteenId, wShiftField.getText().trim(), wSpecField.getText().trim()
            );
            reloadStaffTables.run();
            JOptionPane.showMessageDialog(this,
                    String.format("✅ Worker '%s' (Password: %s) added to %s and saved permanently!",
                            created.getUsername(), created.getPassword(), canteenId));
        });

        addAdminBtn.addActionListener(e -> {
            String uname = aUserField.getText().trim();
            if (uname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter an Admin Username!");
                return;
            }
            Admin created = authService.addNewAdminByAdmin(
                    uname, aNameField.getText().trim(), aPassField.getText().trim(), aDeptField.getText().trim()
            );
            reloadStaffTables.run();
            JOptionPane.showMessageDialog(this,
                    String.format("✅ Admin '%s' (Password: %s) added and saved permanently!",
                            created.getUsername(), created.getPassword()));
        });

        JTable wTable = new JTable(wModel);
        styleTable(wTable);
        JScrollPane wScroll = new JScrollPane(wTable);
        wScroll.setBorder(BorderFactory.createTitledBorder("Active Canteen Workers Directory"));

        JTable aTable = new JTable(aModel);
        styleTable(aTable);
        JScrollPane aScroll = new JScrollPane(aTable);
        aScroll.setBorder(BorderFactory.createTitledBorder("Active Campus Admins Directory"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, wScroll, aScroll);
        split.setDividerLocation(190);

        panel.add(formsGrid, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void openAdminAlertCanteenDialog(String defaultCategory, String prefilledMessage) {
        JComboBox<String> targetBox = new JComboBox<>(new String[]{
                "ALL (Broadcast to All 3 Canteens)",
                "CAN-1 (Canteen 1 — Annapoorna Pure Veg)",
                "CAN-2 (Canteen 2 — Spice Route Non-Veg)",
                "CAN-3 (Canteen 3 — Campus Grills & Fast Food)"
        });
        JTextField catField = new JTextField(defaultCategory, 25);
        JTextArea msgArea = new JTextArea(prefilledMessage.isEmpty()
                ? "Please review current queue load, adjust kitchen preparation plan, and monitor expiring stock."
                : prefilledMessage, 5, 38);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(0, 2, 6, 6));
        top.add(new JLabel("Target Canteen:")); top.add(targetBox);
        top.add(new JLabel("Alert Topic / Category:")); top.add(catField);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(msgArea), BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(this, p, "📢 Alert Canteen (Push Real-Time Update to Workers)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String sel = (String) targetBox.getSelectedItem();
            String canteenKey = sel.startsWith("ALL") ? "ALL" : sel.substring(0, 5);
            notificationService.sendAdminAlertToCanteens(canteenKey, catField.getText().trim(),
                    msgArea.getText().trim(), canteenDAO, employeeDAO);
            JOptionPane.showMessageDialog(this, "✅ Alert broadcast delivered & saved to " + sel + "!");
        }
    }

    private JPanel buildAdminGraphicalDashboardView(Runnable goToAddStaff) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 10, 10));
        for (Map.Entry<String, String> entry : analyticsService.getRealTimeExecutiveMetrics().entrySet()) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(195, 205, 225), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));
            JLabel kLbl = new JLabel(entry.getKey());
            kLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            kLbl.setForeground(new Color(90, 100, 120));
            JLabel vLbl = new JLabel(entry.getValue());
            vLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
            vLbl.setForeground(new Color(22, 55, 115));
            card.add(kLbl, BorderLayout.NORTH);
            card.add(vLbl, BorderLayout.CENTER);
            kpiGrid.add(card);
        }

        JPanel chartsCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);

                List<Canteen> canteens = canteenDAO.findAll();
                int halfW = w / 2;

                g2.setColor(new Color(25, 50, 100));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("📊 Multi-Canteen Revenue vs Expenses & Net Profit (Real-Time ₹)", 25, 28);

                double maxRev = 75000.0;
                for (Canteen c : canteens) {
                    maxRev = Math.max(maxRev, Math.max(c.getTotalSalesRevenue(), c.getTotalExpenses()) * 1.15);
                }

                int baseY = h - 45;
                int chartH = h - 95;
                int groupW = Math.max(90, (halfW - 80) / 3);

                for (int i = 0; i < canteens.size(); i++) {
                    Canteen c = canteens.get(i);
                    int gx = 35 + i * groupW;
                    int bw = 26;

                    int revH = (int) ((c.getTotalSalesRevenue() / maxRev) * chartH);
                    int expH = (int) ((c.getTotalExpenses() / maxRev) * chartH);
                    int plH = (int) ((Math.abs(c.getNetProfitOrLoss()) / maxRev) * chartH);

                    g2.setColor(PRIMARY_BLUE);
                    g2.fillRoundRect(gx, baseY - revH, bw, revH, 6, 6);
                    g2.setColor(WARNING_AMBER);
                    g2.fillRoundRect(gx + bw + 4, baseY - expH, bw, expH, 6, 6);
                    g2.setColor(c.isRunningInLoss() ? DANGER_RED : SUCCESS_GREEN);
                    g2.fillRoundRect(gx + (bw + 4) * 2, baseY - plH, bw, plH, 6, 6);

                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g2.drawString(c.getCanteenId() + " (" + c.getBusyStatus() + ")", gx, baseY + 16);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    g2.drawString(String.format("Rev:₹%.0fk | P/L:₹%.0fk",
                            c.getTotalSalesRevenue() / 1000.0, c.getNetProfitOrLoss() / 1000.0), gx, baseY + 30);
                }

                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(PRIMARY_BLUE); g2.fillRect(25, 40, 12, 12); g2.drawString("Revenue", 42, 50);
                g2.setColor(WARNING_AMBER); g2.fillRect(110, 40, 12, 12); g2.drawString("Total Expenses", 127, 50);
                g2.setColor(SUCCESS_GREEN); g2.fillRect(230, 40, 12, 12); g2.drawString("Net Profit/Loss", 247, 50);

                int rx = halfW + 20;
                g2.setColor(new Color(25, 50, 100));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("📈 Live Canteen Queue Busyness & Campus Waste Breakdown", rx, 28);

                int yCursor = 65;
                for (Canteen c : canteens) {
                    String busy = c.getBusyStatus();
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.drawString(String.format("%s [%s] — %s (%d active orders)",
                            c.getName(), c.getSpecialtyType(), busy.toUpperCase(), c.getActiveOrderCount()), rx, yCursor);

                    int maxBarW = halfW - 60;
                    int fillW = (int) Math.min(maxBarW, (c.getActiveOrderCount() / 10.0) * maxBarW);
                    g2.setColor(new Color(230, 235, 245));
                    g2.fillRoundRect(rx, yCursor + 6, maxBarW, 18, 8, 8);

                    g2.setColor("Free".equals(busy) ? SUCCESS_GREEN
                            : ("Nominal".equals(busy) ? WARNING_AMBER : DANGER_RED));
                    g2.fillRoundRect(rx, yCursor + 6, Math.max(24, fillW), 18, 8, 8);
                    yCursor += 50;
                }

                yCursor += 10;
                g2.setColor(new Color(25, 50, 100));
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString("🗑️ Waste Intelligence Breakdown (Total: " + wasteService.getTotalWasteKg() + " kg):", rx, yCursor);
                yCursor += 22;

                for (Map.Entry<String, Double> e : wasteService.getWasteReasonBreakdown().entrySet()) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g2.drawString(String.format("%-20s : %.1f%%", e.getKey(), e.getValue()), rx, yCursor);
                    int barW = (int) ((e.getValue() / 100.0) * (halfW - 220));
                    g2.setColor(PURPLE_ACCENT);
                    g2.fillRoundRect(rx + 170, yCursor - 11, Math.max(10, barW), 13, 6, 6);
                    yCursor += 24;
                }
            }
        };
        chartsCanvas.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 228)));

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        ModernButton addStaffDashBtn = createStyledButton("👥 Add Workers / Admins", SUCCESS_GREEN, Color.WHITE, 13);
        addStaffDashBtn.addActionListener(e -> goToAddStaff.run());

        ModernButton exportCsvBtn = createStyledButton("📥 Export Live Report (.CSV)", PRIMARY_BLUE, Color.WHITE, 13);
        exportCsvBtn.addActionListener(e -> {
            String path = analyticsService.exportAnalyticsCsvReport();
            JOptionPane.showMessageDialog(this, "✅ Exported live financial & canteen analytics report to:\n" + path);
        });

        ModernButton alertBtn = createStyledButton("📢 Alert Selected / All Canteens", WARNING_AMBER, Color.WHITE, 13);
        alertBtn.addActionListener(e -> openAdminAlertCanteenDialog("DASHBOARD PERFORMANCE & QUEUE ALERT",
                "Canteen 2 is currently Busy while Canteen 3 is Free. Recommend promoting Grill & Fast Food combos at Canteen 3."));

        bottomBar.add(addStaffDashBtn);
        bottomBar.add(exportCsvBtn);
        bottomBar.add(alertBtn);

        panel.add(kpiGrid, BorderLayout.NORTH);
        panel.add(chartsCanvas, BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAdminUpdateBalanceView() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        form.setBackground(new Color(240, 246, 255));
        form.setBorder(BorderFactory.createTitledBorder("💳 Update Student Food Wallet Balance (Saved Permanently)"));

        JTextField userField = new JTextField("Raghuram", 16);
        JTextField amtField = new JTextField("250.00", 10);
        ModernButton addMoneyBtn = createStyledButton("➕ Add Money to Student Wallet", SUCCESS_GREEN, Color.WHITE, 13);

        form.add(new JLabel("Student Username:"));
        form.add(userField);
        form.add(new JLabel("Amount to Add (₹):"));
        form.add(amtField);
        form.add(addMoneyBtn);

        String[] cols = {"Student ID", "Username", "Full Name", "Register No", "Current Food Wallet Balance (₹)", "Favourite Category"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        Runnable loadStudents = () -> {
            model.setRowCount(0);
            for (Customer c : userService.getAllStudents()) {
                model.addRow(new Object[]{
                        c.getUserId(), c.getUsername(), c.getFullName(), c.getRegisterNo(),
                        String.format("₹%.2f", c.getWalletBalance()), c.getFavouriteCategory()
                });
            }
        };
        loadStudents.run();

        JTable table = new JTable(model);
        styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                userField.setText((String) model.getValueAt(r, 1));
            }
        });

        addMoneyBtn.addActionListener(e -> {
            String uname = userField.getText().trim();
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                Optional<Customer> updated = userService.addMoneyToStudentWallet(uname, amt);
                if (updated.isPresent()) {
                    loadStudents.run();
                    JOptionPane.showMessageDialog(this,
                            String.format("✅ Added ₹%.2f to %s's Food Wallet!\nNew Wallet Balance: ₹%.2f (Saved Permanently)",
                                    amt, updated.get().getUsername(), updated.get().getWalletBalance()));
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Student '" + uname + "' not found! Available students: Raghuram, Likith, Student A, Student B, Student C",
                            "User Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive amount!");
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAdminStatsAndPerformanceView() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        String[] cols = {"Canteen ID", "Canteen Name", "Specialty", "Busy Status", "Active Queue", "Sales Revenue", "Ingredient Cost", "Wages", "Utilities", "Net Profit / Loss"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (Canteen c : canteenDAO.findAll()) {
            model.addRow(new Object[]{
                    c.getCanteenId(), c.getName(), c.getSpecialtyType(), c.getBusyStatus().toUpperCase(),
                    c.getActiveOrderCount() + " orders",
                    String.format("₹%,.2f", c.getTotalSalesRevenue()),
                    String.format("₹%,.2f", c.getIngredientPurchaseExpenses()),
                    String.format("₹%,.2f", c.getWorkerWagesExpenses()),
                    String.format("₹%,.2f", c.getUtilitiesExpenses()),
                    String.format("%s₹%,.2f", c.getNetProfitOrLoss() >= 0 ? "+" : "-", Math.abs(c.getNetProfitOrLoss()))
            });
        }
        JTable canteenTable = new JTable(model);
        styleTable(canteenTable);

        String[] foodCols = {"Canteen", "Food ID", "Dish Name", "Price", "Total Orders", "Live Rating", "Waste Rate %", "Food Performance Score (0-100)"};
        DefaultTableModel foodModel = new DefaultTableModel(foodCols, 0);
        for (FoodItem f : menuService.getAllFoodItems()) {
            foodModel.addRow(new Object[]{
                    f.getCanteenId(), f.getFoodId(), f.getName(), String.format("₹%.0f", f.getPrice()),
                    f.getTotalOrdersCount(), String.format("★%.2f (%d)", f.getCustomerRating(), f.getRatingCount()),
                    f.getWasteRatePercent() + "%", f.calculatePerformanceScore()
            });
        }
        JTable foodTable = new JTable(foodModel);
        styleTable(foodTable);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(canteenTable), new JScrollPane(foodTable));
        split.setDividerLocation(160);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ModernButton alertPerfBtn = createStyledButton("📢 Send Performance & Menu Score Alert to Canteens", WARNING_AMBER, Color.WHITE, 13);
        alertPerfBtn.addActionListener(e -> openAdminAlertCanteenDialog("CANTEEN STATS & FOOD SCORE ADVISORY",
                "Increase prep for top-scoring items (Executive Veg Meals, Ambur Chicken Biryani) and trim high-waste noodles batch by 10%."));
        bottom.add(alertPerfBtn);

        panel.add(new JLabel("📈 Detailed Canteen Financials, Queue Load & Dish Performance Scores across All 30 Dishes:"), BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAdminWhatIfSimulatorView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] reqCols = {"Request ID", "Time", "Worker", "Canteen", "Scenario Type", "Worker's Request Details", "Status"};
        DefaultTableModel reqModel = new DefaultTableModel(reqCols, 0);
        List<SimulationRequest> reqs = workerService.getAllSimulationRequests();
        for (SimulationRequest r : reqs) {
            reqModel.addRow(new Object[]{
                    r.getRequestId(), r.getFormattedTime(), r.getWorkerUsername(),
                    r.getCanteenId(), r.getScenarioType(), r.getRequestDetails(), r.getStatus()
            });
        }
        JTable reqTable = new JTable(reqModel);
        styleTable(reqTable);
        JScrollPane reqScroll = new JScrollPane(reqTable);
        reqScroll.setPreferredSize(new Dimension(900, 120));
        reqScroll.setBorder(BorderFactory.createTitledBorder("📥 What-If Simulation Requests Received from Canteen Workers / Waiters"));

        JPanel simControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JComboBox<String> scenarioBox = new JComboBox<>(new String[]{"EVENT_SURGE", "ATTENDANCE_DROP", "PRICE_ELASTICITY"});
        JComboBox<String> canteenBox = new JComboBox<>(new String[]{"ALL", "CAN-1", "CAN-2", "CAN-3"});
        JTextField param1Field = new JTextField("750", 6);
        JTextField param2Field = new JTextField("88", 6);
        ModernButton runSimBtn = createStyledButton("▶ Run What-If Simulation", PRIMARY_BLUE, Color.WHITE, 13);
        ModernButton runWorkerReqBtn = createStyledButton("⚡ Run Worker Request & Alert Canteen", SUCCESS_GREEN, Color.WHITE, 13);
        ModernButton alertSimBtn = createStyledButton("📢 Push Simulation Report to Canteen", WARNING_AMBER, Color.WHITE, 13);

        simControls.add(new JLabel("Scenario:")); simControls.add(scenarioBox);
        simControls.add(new JLabel("Canteen:")); simControls.add(canteenBox);
        simControls.add(new JLabel("Extra Students / Old Val:")); simControls.add(param1Field);
        simControls.add(new JLabel("Attendance % / New Val:")); simControls.add(param2Field);
        simControls.add(runSimBtn);
        simControls.add(runWorkerReqBtn);
        simControls.add(alertSimBtn);

        JTextArea simOutput = new JTextArea(14, 70);
        simOutput.setEditable(false);
        simOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));

        Runnable executeSim = () -> {
            String sc = (String) scenarioBox.getSelectedItem();
            String cid = (String) canteenBox.getSelectedItem();
            double p1 = 750, p2 = 88;
            try { p1 = Double.parseDouble(param1Field.getText().trim()); } catch (Exception ignored) {}
            try { p2 = Double.parseDouble(param2Field.getText().trim()); } catch (Exception ignored) {}

            Map<String, Object> params = new HashMap<>();
            params.put("canteenId", cid);
            params.put("extraStudents", (int) p1);
            params.put("attendancePct", (int) p2);
            params.put("fromAttendance", (int) p1);
            params.put("toAttendance", (int) p2);
            params.put("oldPrice", p1);
            params.put("newPrice", p2);

            SimulationResult res = simulationEngine.runSimulation(sc, params);
            StringBuilder sb = new StringBuilder();
            sb.append("=========================================================================\n");
            sb.append("  ").append(res.getScenarioTitle()).append("\n");
            sb.append("  ").append(res.getSummaryDescription()).append("\n");
            sb.append("=========================================================================\n\n");
            sb.append("1. PROJECTED DEMAND IMPACT:\n");
            res.getCategoryDemandDelta().forEach((k, v) -> sb.append("   • ").append(k).append(" : ").append(v).append("\n"));
            sb.append("\n2. WAREHOUSE INGREDIENT RISK MATRIX:\n");
            res.getIngredientRiskStatus().forEach((k, v) -> sb.append("   • ").append(k).append(" -> ").append(v).append("\n"));
            sb.append("\n3. RECOMMENDED KITCHEN PREPARATION ADJUSTMENTS:\n");
            res.getPreparationAdjustments().forEach((k, v) -> sb.append(String.format("   • %-38s : %+d plates\n", k, v)));
            sb.append("\n4. STRATEGIC RECOMMENDATIONS:\n");
            res.getStrategicRecommendations().forEach(r -> sb.append("   💡 ").append(r).append("\n"));
            sb.append(String.format("\nProjected Revenue Impact: %+.1f%%   |   Projected Waste Impact: %+.1f%%\n",
                    res.getProjectedRevenueDeltaPercent(), res.getProjectedWasteDeltaPercent()));
            simOutput.setText(sb.toString());
        };
        executeSim.run();

        runSimBtn.addActionListener(e -> executeSim.run());

        runWorkerReqBtn.addActionListener(e -> {
            int row = reqTable.getSelectedRow();
            if (row < 0 && !reqs.isEmpty()) row = 0;
            if (row >= 0) {
                SimulationRequest r = reqs.get(row);
                scenarioBox.setSelectedItem(r.getScenarioType());
                canteenBox.setSelectedItem(r.getCanteenId());
                executeSim.run();
                r.setStatus("COMPLETED & PUSHED TO " + r.getCanteenId());
                reqModel.setValueAt(r.getStatus(), row, 6);
                PersistentDataStore.getInstance().saveToDisk();
                notificationService.sendAdminAlertToCanteens(r.getCanteenId(),
                        "WHAT-IF SIMULATION RESPONSE (" + r.getRequestId() + ")",
                        "Simulated " + r.getScenarioType() + " for " + r.getCanteenId() + ": Pre-prep +145 Biryani plates & buffer +20kg poultry.",
                        canteenDAO, employeeDAO);
                JOptionPane.showMessageDialog(this, "✅ Executed Worker Request " + r.getRequestId() + " and pushed results to " + r.getCanteenId() + "!");
            }
        });

        alertSimBtn.addActionListener(e -> openAdminAlertCanteenDialog("WHAT-IF SIMULATION DIRECTIVE", simOutput.getText()));

        JPanel centerWrap = new JPanel(new BorderLayout(6, 6));
        centerWrap.add(simControls, BorderLayout.NORTH);
        centerWrap.add(new JScrollPane(simOutput), BorderLayout.CENTER);

        panel.add(reqScroll, BorderLayout.NORTH);
        panel.add(centerWrap, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAdminWasteAndFeedbackLoopView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] wCols = {"Waste ID", "Date", "Canteen", "Food Item", "Predicted", "Prepared", "Sold", "Wasted (Plates)", "Waste Weight (kg)", "Primary Reason"};
        DefaultTableModel wModel = new DefaultTableModel(wCols, 0);
        Runnable loadWaste = () -> {
            wModel.setRowCount(0);
            for (WasteRecord r : wasteService.getAllWasteRecords()) {
                wModel.addRow(new Object[]{
                        r.getWasteId(), r.getDate(), r.getCanteenId(), r.getFoodName(),
                        r.getPredictedQty(), r.getPreparedQty(), r.getSoldQty(),
                        r.getWastedQty(), r.getWasteWeightKg() + " kg", r.getReason()
                });
            }
        };
        loadWaste.run();

        String[] fbCols = {"Timestamp", "Student", "Canteen", "Dish Reviewed", "Rating", "Review Comment", "Taste NLP", "Spice NLP", "Quantity NLP"};
        DefaultTableModel fbModel = new DefaultTableModel(fbCols, 0);
        for (FeedbackDAO.FeedbackEntry fe : wasteService.getAllFeedbacks()) {
            fbModel.addRow(new Object[]{
                    fe.timestamp, fe.customerUsername, fe.canteenId, fe.foodName,
                    "★".repeat(fe.rating), fe.comment, fe.tasteSentiment, fe.spicinessSentiment, fe.quantitySentiment
            });
        }

        JTable wTable = new JTable(wModel);
        styleTable(wTable);
        JTable fbTable = new JTable(fbModel);
        styleTable(fbTable);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(wTable), new JScrollPane(fbTable));
        split.setDividerLocation(220);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        ModernButton logWasteBtn = createStyledButton("➕ Log Daily Batch Waste", PRIMARY_BLUE, Color.WHITE, 13);
        ModernButton alertWasteBtn = createStyledButton("📢 Alert Canteen on Waste & Feedback Loop", WARNING_AMBER, Color.WHITE, 13);

        logWasteBtn.addActionListener(e -> {
            JComboBox<String> cBox = new JComboBox<>(new String[]{"CAN-1", "CAN-2", "CAN-3"});
            JTextField dishF = new JTextField("Schezwan Veg Hakka Noodles", 16);
            JTextField predF = new JTextField("160", 6);
            JTextField prepF = new JTextField("175", 6);
            JTextField soldF = new JTextField("150", 6);
            JComboBox<WasteRecord.WasteReason> rBox = new JComboBox<>(WasteRecord.WasteReason.values());

            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Canteen:")); p.add(cBox);
            p.add(new JLabel("Dish Name:")); p.add(dishF);
            p.add(new JLabel("Predicted Plates:")); p.add(predF);
            p.add(new JLabel("Prepared Plates:")); p.add(prepF);
            p.add(new JLabel("Sold Plates:")); p.add(soldF);
            p.add(new JLabel("Waste Reason:")); p.add(rBox);

            if (JOptionPane.showConfirmDialog(this, p, "Log Waste Intelligence Record", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    wasteService.recordDailyBatchWaste((String) cBox.getSelectedItem(), dishF.getText().trim(),
                            Integer.parseInt(predF.getText().trim()), Integer.parseInt(prepF.getText().trim()),
                            Integer.parseInt(soldF.getText().trim()), (WasteRecord.WasteReason) rBox.getSelectedItem());
                    loadWaste.run();
                } catch (Exception ignored) {}
            }
        });

        alertWasteBtn.addActionListener(e -> openAdminAlertCanteenDialog("WASTE & FEEDBACK INTELLIGENCE LOOP",
                "Overproduction accounts for majority of waste in Canteen 3 Noodles. Reduce second batch prep by 12% and adjust spice level per student NLP feedback."));

        bottom.add(logWasteBtn);
        bottom.add(alertWasteBtn);

        panel.add(new JLabel("🗑️ Closed-Loop Waste Intelligence (Top) & Verified Student Feedback Sentiment (Bottom):"), BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAdminEventsWeatherAndCalendarView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel weatherCard = new JPanel(new BorderLayout(8, 8));
        weatherCard.setBackground(new Color(235, 245, 255));
        weatherCard.setBorder(BorderFactory.createTitledBorder("🌤 Live Weather API (Open-Meteo / Google Weather) & Calendar Demand Intelligence"));

        JLabel weatherStatusLbl = new JLabel();
        weatherStatusLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        Runnable refreshWeatherLbl = () -> {
            WeatherSnapshot w = eventWeatherService.getCurrentWeather();
            weatherStatusLbl.setText("<html>" + w.getConditionLabel() + "<br/><b>ML Demand Analytics:</b> " +
                    w.getWeatherAnalyticsRecommendation() + "</html>");
        };
        refreshWeatherLbl.run();

        JPanel weatherBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        ModernButton liveApiSyncBtn = createStyledButton("🌐 Sync Live Chennai Weather API", SUCCESS_GREEN, Color.WHITE, 12);
        ModernButton syncHolidayApiBtn = createStyledButton("📅 Sync Public Holidays & Calendar API", DARK_SLATE, Color.WHITE, 12);
        ModernButton summerModeBtn = createStyledButton("☀ Summer Hot (36°C -> Cold Drinks +42%)", PRIMARY_BLUE, Color.WHITE, 12);
        ModernButton winterModeBtn = createStyledButton("🌧 Winter/Rainy (20°C -> Coffee/Tea/Snacks +38%)", PURPLE_ACCENT, Color.WHITE, 12);
        ModernButton alertWeatherBtn = createStyledButton("📢 Alert Canteens with Weather/Calendar Advisory", WARNING_AMBER, Color.WHITE, 12);

        String[] evCols = {"Event ID", "Event Name", "Type", "Calendar Date", "Expected Attendance", "Duration", "ML Demand Multiplier"};
        DefaultTableModel evModel = new DefaultTableModel(evCols, 0);
        JPanel calendarGridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGridPanel.setBackground(new Color(241, 245, 249));
        calendarGridPanel.setBorder(BorderFactory.createTitledBorder("📅 Interactive Campus Calendar Grid (" + YearMonth.now() + ")"));

        Runnable loadEventsAndCalendar = () -> {
            evModel.setRowCount(0);
            for (CampusEvent ev : eventWeatherService.getUpcomingEvents()) {
                evModel.addRow(new Object[]{
                        ev.getEventId(), ev.getEventName(), ev.getEventType(), ev.getEventDate(),
                        ev.getExpectedAttendance(), ev.getDurationWindow(), ev.getDemandMultiplier() + "x"
                });
            }

            calendarGridPanel.removeAll();
            String[] dayHeaders = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String dh : dayHeaders) {
                JLabel dl = new JLabel(dh, SwingConstants.CENTER);
                dl.setFont(new Font("SansSerif", Font.BOLD, 12));
                dl.setForeground(new Color(30, 58, 138));
                calendarGridPanel.add(dl);
            }

            YearMonth ym = YearMonth.now();
            Map<LocalDate, List<CampusEvent>> monthEvents = eventWeatherService.getEventsByDateForMonth(ym);
            int firstDow = ym.atDay(1).getDayOfWeek().getValue();
            for (int i = 1; i < firstDow; i++) {
                calendarGridPanel.add(new JLabel(""));
            }
            for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                LocalDate date = ym.atDay(day);
                List<CampusEvent> dayEvs = monthEvents.get(date);
                JPanel cell = new JPanel(new BorderLayout());
                boolean hasEvent = dayEvs != null && !dayEvs.isEmpty();
                cell.setBackground(hasEvent ? new Color(254, 243, 199) : Color.WHITE);
                cell.setBorder(BorderFactory.createLineBorder(hasEvent ? WARNING_AMBER : new Color(203, 213, 225)));

                String text = hasEvent
                        ? String.format("<html><b>%d</b><br/><font color='#B45309' size='2'>%s (%.2fx)</font></html>",
                        day, dayEvs.get(0).getEventName(), dayEvs.get(0).getDemandMultiplier())
                        : String.format("<html><b>%d</b></html>", day);
                JLabel cl = new JLabel(text);
                cl.setBorder(new EmptyBorder(2, 4, 2, 4));
                cell.add(cl, BorderLayout.CENTER);
                calendarGridPanel.add(cell);
            }
            calendarGridPanel.revalidate();
            calendarGridPanel.repaint();
        };
        loadEventsAndCalendar.run();

        liveApiSyncBtn.addActionListener(e -> {
            String msg = eventWeatherService.syncLiveWeatherFromApi();
            refreshWeatherLbl.run();
            JOptionPane.showMessageDialog(this, "✅ " + msg);
        });

        syncHolidayApiBtn.addActionListener(e -> {
            int count = eventWeatherService.syncPublicHolidaysAndAcademicCalendar();
            loadEventsAndCalendar.run();
            JOptionPane.showMessageDialog(this, "✅ Synced " + count + " Calendar Event(s) into the Demand Prediction Engine!");
        });

        summerModeBtn.addActionListener(e -> {
            eventWeatherService.updateWeatherMode("SUMMER_HOT", 36.0, false, "Google Weather / Open-Meteo Sync: Summer Sunny & Hot (36.0°C)");
            refreshWeatherLbl.run();
        });
        winterModeBtn.addActionListener(e -> {
            eventWeatherService.updateWeatherMode("WINTER_COOL", 20.5, true, "Google Weather / Open-Meteo Sync: Winter Cool & Rain Showers (20.5°C)");
            refreshWeatherLbl.run();
        });
        alertWeatherBtn.addActionListener(e -> openAdminAlertCanteenDialog("WEATHER & CALENDAR DEMAND ADVISORY",
                eventWeatherService.getCurrentWeather().getWeatherAnalyticsRecommendation()));

        weatherBtns.add(liveApiSyncBtn);
        weatherBtns.add(syncHolidayApiBtn);
        weatherBtns.add(summerModeBtn);
        weatherBtns.add(winterModeBtn);
        weatherBtns.add(alertWeatherBtn);

        weatherCard.add(weatherStatusLbl, BorderLayout.CENTER);
        weatherCard.add(weatherBtns, BorderLayout.SOUTH);

        JTable evTable = new JTable(evModel);
        styleTable(evTable);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                calendarGridPanel, new JScrollPane(evTable));
        split.setDividerLocation(230);

        ModernButton addEvBtn = createStyledButton("➕ Add Upcoming Campus Calendar Event", SUCCESS_GREEN, Color.WHITE, 13);
        addEvBtn.addActionListener(e -> {
            JTextField nameF = new JTextField("Annual Alumni Conclave", 16);
            JTextField daysAheadF = new JTextField("5", 6);
            JTextField attF = new JTextField("1400", 8);
            JTextField multF = new JTextField("1.24", 6);
            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
            p.add(new JLabel("Event Name:")); p.add(nameF);
            p.add(new JLabel("Days from Today:")); p.add(daysAheadF);
            p.add(new JLabel("Expected Attendance:")); p.add(attF);
            p.add(new JLabel("Demand Multiplier (e.g. 1.24):")); p.add(multF);
            if (JOptionPane.showConfirmDialog(this, p, "Add Campus Calendar Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int days = Integer.parseInt(daysAheadF.getText().trim());
                    eventWeatherService.addCampusEvent(nameF.getText().trim(), "Calendar Event", LocalDate.now().plusDays(days),
                            Integer.parseInt(attF.getText().trim()), "10:00 AM – 04:00 PM", Double.parseDouble(multF.getText().trim()));
                    loadEventsAndCalendar.run();
                } catch (Exception ignored) {}
            }
        });

        panel.add(weatherCard, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(addEvBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(195, 208, 230), 1, true),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(22, 60, 125)));
        p.setPreferredSize(new Dimension(950, 150));
        return p;
    }
}

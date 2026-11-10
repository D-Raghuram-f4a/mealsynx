package com.mealsynx.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;
    private String department;

    public Admin(String userId, String username, String fullName, String adminCode,
                 String email, String password, String department) {
        super(userId, username, fullName, adminCode, email, password, UserRole.ADMIN);
        this.department = department;
    }

    @Override
    public String getDashboardTitle() {
        return "MealSynx Admin Central Intelligence — " + getUsername();
    }

    public String getDepartment() { return department; }
}

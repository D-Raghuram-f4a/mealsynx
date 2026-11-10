package com.mealsynx.model;

public class Worker extends User {
    private static final long serialVersionUID = 1L;
    private String assignedCanteenId;
    private String shiftTiming;
    private String specialization;

    public Worker(String userId, String username, String fullName, String employeeCode,
                  String email, String password, String assignedCanteenId,
                  String shiftTiming, String specialization) {
        super(userId, username, fullName, employeeCode, email, password, UserRole.WORKER);
        this.assignedCanteenId = assignedCanteenId;
        this.shiftTiming = shiftTiming;
        this.specialization = specialization;
    }

    @Override
    public String getDashboardTitle() {
        return "MealSynx Worker Portal — " + getUsername() + " [" + assignedCanteenId + "]";
    }

    public String getAssignedCanteenId() { return assignedCanteenId; }
    public void setAssignedCanteenId(String assignedCanteenId) { this.assignedCanteenId = assignedCanteenId; }
    public String getShiftTiming() { return shiftTiming; }
    public void setShiftTiming(String shiftTiming) { this.shiftTiming = shiftTiming; }
    public String getSpecialization() { return specialization; }
}

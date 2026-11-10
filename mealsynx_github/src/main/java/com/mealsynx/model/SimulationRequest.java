package com.mealsynx.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimulationRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String requestId;
    private final String workerUsername;
    private final String canteenId;
    private final String scenarioType;
    private final String requestDetails;
    private String status;
    private final LocalDateTime requestedAt;

    public SimulationRequest(String requestId, String workerUsername, String canteenId,
                             String scenarioType, String requestDetails) {
        this.requestId = requestId;
        this.workerUsername = workerUsername;
        this.canteenId = canteenId;
        this.scenarioType = scenarioType;
        this.requestDetails = requestDetails;
        this.status = "PENDING ADMIN SIMULATION";
        this.requestedAt = LocalDateTime.now();
    }

    public String getRequestId() { return requestId; }
    public String getWorkerUsername() { return workerUsername; }
    public String getCanteenId() { return canteenId; }
    public String getScenarioType() { return scenarioType; }
    public String getRequestDetails() { return requestDetails; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFormattedTime() { return requestedAt.format(DateTimeFormatter.ofPattern("dd MMM hh:mm a")); }
}

package com.mealsynx.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SupportTicket implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String ticketId;
    private final String studentUsername;
    private final String issueCategory;
    private final String description;
    private String status;
    private final LocalDateTime createdAt;

    public SupportTicket(String ticketId, String studentUsername, String issueCategory, String description) {
        this.ticketId = ticketId;
        this.studentUsername = studentUsername;
        this.issueCategory = issueCategory;
        this.description = description;
        this.status = "OPEN — Assigned to Campus Support";
        this.createdAt = LocalDateTime.now();
    }

    public String getTicketId() { return ticketId; }
    public String getStudentUsername() { return studentUsername; }
    public String getIssueCategory() { return issueCategory; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFormattedTime() { return createdAt.format(DateTimeFormatter.ofPattern("dd MMM hh:mm a")); }
}

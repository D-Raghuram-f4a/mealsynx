package com.mealsynx.model;

import java.io.Serializable;
import java.time.LocalDate;

public class CampusEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String eventId;
    private final String eventName;
    private final String eventType;
    private final LocalDate eventDate;
    private final int expectedAttendance;
    private final String durationWindow;
    private final double demandMultiplier;

    public CampusEvent(String eventId, String eventName, String eventType, LocalDate eventDate,
                       int expectedAttendance, String durationWindow, double demandMultiplier) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.expectedAttendance = expectedAttendance;
        this.durationWindow = durationWindow;
        this.demandMultiplier = demandMultiplier;
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getEventType() { return eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public int getExpectedAttendance() { return expectedAttendance; }
    public String getDurationWindow() { return durationWindow; }
    public double getDemandMultiplier() { return demandMultiplier; }
}

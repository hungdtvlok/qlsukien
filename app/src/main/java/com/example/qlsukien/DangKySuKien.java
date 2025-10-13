package com.example.qlsukien;

public class DangKySuKien {
    private String userId;
    private String eventId;
    private String status;

    // Constructor
    public DangKySuKien(String userId, String eventId, String status) {
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
    }

    // Getter và Setter cho userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter và Setter cho eventId
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    // Getter và Setter cho status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

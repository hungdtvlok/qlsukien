package com.example.qlsukien;

public class SuKienDaDangKy {
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;

    private String eventId;
    private String eventName;
    private String startTime;
    private String endTime;
    private String createdAt;

    private String status; // "pending" hoặc "joined"

    // Constructor đầy đủ
    public SuKienDaDangKy(String userId, String username, String fullName, String email, String phone,
                          String eventId, String eventName, String startTime, String endTime,
                          String createdAt,String status) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.eventId = eventId;
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getter
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getCreatedAt() { return createdAt; }

    public String getStatus() {
        return status;
    }



    // Setter
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) {
        this.status = status;
    }
}

package com.example.qlsukien;

public class SuKienDaDangKyAll {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String eventId;
    private String eventName;
    private String startTime;
    private String endTime;
    private String createdAt;
    private String registrationId;


    // Constructor đầy đủ
    public SuKienDaDangKyAll(String userId, String fullName, String email, String phone,
                             String eventId, String eventName, String startTime, String endTime,
                             String createdAt, String registrationId) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.eventId = eventId;
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.registrationId = registrationId;


    }

    // Getter
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getCreatedAt() { return createdAt; }


    public String getRegistrationId() { return registrationId; }

    // Setter
    public void setUserId(String userId) { this.userId = userId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
}

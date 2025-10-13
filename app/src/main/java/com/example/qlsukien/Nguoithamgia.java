package com.example.qlsukien;



public class Nguoithamgia {
    private String id, fullName, email, phone, eventName, startTime, endTime, registeredBy, createdAt;

    public Nguoithamgia(String id, String fullName, String email, String phone,
                             String eventName, String startTime, String endTime,
                             String registeredBy, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.registeredBy = registeredBy;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getEventName() { return eventName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getRegisteredBy() { return registeredBy; }
    public String getCreatedAt() { return createdAt; }
}


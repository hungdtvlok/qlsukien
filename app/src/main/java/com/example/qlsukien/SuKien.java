package com.example.qlsukien;
public class SuKien {
    private String id;
    private String name;
    private String startTime;
    private String endTime;
    private String location;
    private String description;

    public SuKien(String id, String name, String startTime, String endTime, String location, String description) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
    }

    // Getter + Setter
    public String getId() { return id; }
    public String getName() { return name; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
}

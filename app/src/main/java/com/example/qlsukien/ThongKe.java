package com.example.qlsukien;

public class ThongKe {
    private String eventName;
    private int count;

    public ThongKe(String eventName, int count) {
        this.eventName = eventName;
        this.count = count;
    }

    public String getEventName() {
        return eventName;
    }

    public int getCount() {
        return count;
    }
}

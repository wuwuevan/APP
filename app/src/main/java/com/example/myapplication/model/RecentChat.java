package com.example.myapplication.model;

public class RecentChat {
    private String doctorName;
    private String firstMessage;
    private String lastMessage;
    private long lastTimestamp;

    public RecentChat(String doctorName, String firstMessage, String lastMessage, long lastTimestamp) {
        this.doctorName = doctorName;
        this.firstMessage = firstMessage;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getFirstMessage() {
        return firstMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }
}

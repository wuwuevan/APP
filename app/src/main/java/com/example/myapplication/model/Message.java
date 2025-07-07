package com.example.myapplication.model;

public class Message {
    private long id;
    private String content;
    private long timestamp;
    private boolean isSent; // true表示发送的消息，false表示接收的消息
    private String doctorName;

    public Message() {
    }

    public Message(String content, long timestamp, boolean isSent, String doctorName) {
        this.content = content;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.doctorName = doctorName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
} 
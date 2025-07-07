package com.example.myapplication.model;

/**
 * 疼痛评分实体类
 */
public class PainScore {
    private int id;
    private int taskId;
    private int userId;
    private int score; // 0-10分
    private String location; // 疼痛部位
    private String description; // 疼痛描述
    private String recordTime;

    public PainScore() {
    }

    public PainScore(int taskId, int userId, int score, String location, String description, String recordTime) {
        this.taskId = taskId;
        this.userId = userId;
        this.score = score;
        this.location = location;
        this.description = description;
        this.recordTime = recordTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }
} 
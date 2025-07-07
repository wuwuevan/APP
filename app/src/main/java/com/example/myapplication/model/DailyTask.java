package com.example.myapplication.model;

/**
 * 每日任务实体类
 */
public class DailyTask {
    private int id;
    private int userId;
    private String taskName;
    private String taskDesc;
    private String taskType; // 教育内容、问卷、疼痛评分等
    private String startTime;
    private String endTime;
    private String status; // 未完成、已完成、已跳过、已延后
    private float completionRate;

    public DailyTask() {
    }

    public DailyTask(int userId, String taskName, String taskDesc, String taskType, String startTime, String endTime) {
        this.userId = userId;
        this.taskName = taskName;
        this.taskDesc = taskDesc;
        this.taskType = taskType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "未完成";
        this.completionRate = 0.0f;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(float completionRate) {
        this.completionRate = completionRate;
    }
} 
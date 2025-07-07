package com.example.myapplication.model;

/**
 * 康复训练实体类
 */
public class RehabilitationExercise {
    private int id;
    private int userId;
    private String exerciseName;
    private String exerciseDesc;
    private String videoUrl;
    private String imageUrl;
    private int duration; // 训练持续时间（秒）
    private int count; // 训练次数
    private String finishedTime;
    private String completionStatus; // 未完成、已完成、部分完成

    public RehabilitationExercise() {
    }

    public RehabilitationExercise(int userId, String exerciseName, String exerciseDesc, String videoUrl, String imageUrl, int duration, int count) {
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.exerciseDesc = exerciseDesc;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.duration = duration;
        this.count = count;
        this.completionStatus = "未完成";
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

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getExerciseDesc() {
        return exerciseDesc;
    }

    public void setExerciseDesc(String exerciseDesc) {
        this.exerciseDesc = exerciseDesc;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(String finishedTime) {
        this.finishedTime = finishedTime;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }
} 
package com.example.myapplication.model;

/**
 * 健康指标实体类
 */
public class HealthIndicator {
    private int id;
    private int userId;
    private String indicatorType; // 步数、步距、心率、睡眠、体重等
    private float indicatorValue;
    private String recordTime;
    private int isAbnormal; // 0-正常，1-异常

    public HealthIndicator() {
    }

    public HealthIndicator(int userId, String indicatorType, float indicatorValue, String recordTime) {
        this.userId = userId;
        this.indicatorType = indicatorType;
        this.indicatorValue = indicatorValue;
        this.recordTime = recordTime;
        this.isAbnormal = 0;
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

    public String getIndicatorType() {
        return indicatorType;
    }

    public void setIndicatorType(String indicatorType) {
        this.indicatorType = indicatorType;
    }

    public float getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(float indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public int getIsAbnormal() {
        return isAbnormal;
    }

    public void setIsAbnormal(int isAbnormal) {
        this.isAbnormal = isAbnormal;
    }
} 
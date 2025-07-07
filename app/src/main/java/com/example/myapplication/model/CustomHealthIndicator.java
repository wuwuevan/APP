package com.example.myapplication.model;

/**
 * 自定义健康指标实体类，存储额外的自定义指标信息
 */
public class CustomHealthIndicator {
    private int id;
    private int indicatorId; // 关联到HealthIndicator的ID
    private String unit; // 单位
    private float normalMinValue; // 正常范围最小值
    private float normalMaxValue; // 正常范围最大值
    private String notes; // 备注

    public CustomHealthIndicator() {
    }

    public CustomHealthIndicator(int indicatorId, String unit, float normalMinValue, float normalMaxValue, String notes) {
        this.indicatorId = indicatorId;
        this.unit = unit;
        this.normalMinValue = normalMinValue;
        this.normalMaxValue = normalMaxValue;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(int indicatorId) {
        this.indicatorId = indicatorId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getNormalMinValue() {
        return normalMinValue;
    }

    public void setNormalMinValue(float normalMinValue) {
        this.normalMinValue = normalMinValue;
    }

    public float getNormalMaxValue() {
        return normalMaxValue;
    }

    public void setNormalMaxValue(float normalMaxValue) {
        this.normalMaxValue = normalMaxValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 
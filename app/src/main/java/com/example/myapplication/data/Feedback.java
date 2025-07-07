package com.example.myapplication.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 反馈信息实体类
 */
@Entity(tableName = "feedback")
public class Feedback {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String feedbackType; // 反馈类型
    private String content;      // 反馈内容
    private String contactInfo;  // 联系方式
    private long timestamp;      // 提交时间戳
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getFeedbackType() {
        return feedbackType;
    }
    
    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getContactInfo() {
        return contactInfo;
    }
    
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 
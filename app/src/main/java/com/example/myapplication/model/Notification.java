package com.example.myapplication.model;

import java.io.Serializable;

/**
 * 通知消息实体类
 */
public class Notification implements Serializable {
    private int id;
    private int userId;
    private String notificationType; // TASK-任务提醒，ABNORMAL-异常预警，FOLLOW_UP-随访通知
    private String title;
    private String content;
    private String createTime;
    private int isRead; // 0-未读，1-已读

    public Notification() {
    }

    public Notification(int userId, String notificationType, String title, String content, String createTime) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.isRead = 0;
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

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
} 
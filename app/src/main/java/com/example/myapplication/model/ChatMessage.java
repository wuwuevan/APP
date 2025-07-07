package com.example.myapplication.model;

/**
 * 聊天消息实体类
 */
public class ChatMessage {
    private int id;
    private int userId;
    private String senderType; // USER-用户，BOT-机器人，DOCTOR-医生
    private String content;
    private String sendTime;
    private int isRead; // 0-未读，1-已读
    private int isRisk; // 0-正常，1-高风险
    private int isEncrypted; // 0-未加密，1-已加密

    public ChatMessage() {
    }

    public ChatMessage(int userId, String senderType, String content, String sendTime) {
        this.userId = userId;
        this.senderType = senderType;
        this.content = content;
        this.sendTime = sendTime;
        this.isRead = 0;
        this.isRisk = 0;
        this.isEncrypted = 1; // 默认加密
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

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public int getIsRisk() {
        return isRisk;
    }

    public void setIsRisk(int isRisk) {
        this.isRisk = isRisk;
    }

    public int getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(int isEncrypted) {
        this.isEncrypted = isEncrypted;
    }
} 
package com.example.myapplication.model;

/**
 * 教育内容实体类
 */
public class EducationContent {
    private int id;
    private int taskId;
    private String title;
    private String content;
    private String type; // 术前、术后等
    private String createTime;

    public EducationContent() {
    }

    public EducationContent(int taskId, String title, String content, String type, String createTime) {
        this.taskId = taskId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createTime = createTime;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
} 
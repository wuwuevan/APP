package com.example.myapplication.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 数据模型：社区中用户分享的健康数据。
 */
public class HealthShareEntry {

    private int id;
    private long authorId;
    @NonNull
    private String authorName;
    @NonNull
    private String metricName;
    @NonNull
    private String metricValue;
    @Nullable
    private String note;
    private long updatedAt;

    public HealthShareEntry() {
        this(-1, "", "", "", null, System.currentTimeMillis());
    }

    public HealthShareEntry(long authorId, @NonNull String authorName,
                             @NonNull String metricName, @NonNull String metricValue,
                             @Nullable String note, long updatedAt) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.note = note;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    @NonNull
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(@NonNull String authorName) {
        this.authorName = authorName;
    }

    @NonNull
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(@NonNull String metricName) {
        this.metricName = metricName;
    }

    @NonNull
    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(@NonNull String metricValue) {
        this.metricValue = metricValue;
    }

    @Nullable
    public String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

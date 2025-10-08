package com.example.myapplication.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 社区帖子实体。
 */
@Entity(tableName = "community_posts")
public class CommunityPost {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long authorId;

    @NonNull
    private String authorName;

    @NonNull
    private String title;

    @NonNull
    private String content;

    private long createdAt;

    public CommunityPost() {
        this.authorName = "";
        this.title = "";
        this.content = "";
    }

    public CommunityPost(long authorId, @NonNull String authorName, @NonNull String title,
                          @NonNull String content, long createdAt) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
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
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

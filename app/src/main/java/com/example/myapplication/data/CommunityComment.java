package com.example.myapplication.data;

import androidx.annotation.NonNull;
/**
 * 社区回复实体。
 */
public class CommunityComment {

    private int id;

    private int postId;

    private long authorId;

    @NonNull
    private String authorName;

    @NonNull
    private String content;

    private long createdAt;

    public CommunityComment() {
        this.authorName = "";
        this.content = "";
    }

    public CommunityComment(int postId, long authorId, @NonNull String authorName,
                             @NonNull String content, long createdAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
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

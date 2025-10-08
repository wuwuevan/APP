package com.example.myapplication.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 帖子及其回复的组合实体。
 */
public class PostWithComments {

    private final CommunityPost post;
    private final List<CommunityComment> comments;

    public PostWithComments(@NonNull CommunityPost post, @NonNull List<CommunityComment> comments) {
        this.post = post;
        this.comments = new ArrayList<>(comments);
    }

    @NonNull
    public CommunityPost getPost() {
        return post;
    }

    @NonNull
    public List<CommunityComment> getComments() {
        return Collections.unmodifiableList(comments);
    }
}

package com.example.myapplication.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子及其回复的组合实体。
 */
public class PostWithComments {

    @Embedded
    private CommunityPost post;

    @Relation(parentColumn = "id", entityColumn = "postId")
    private List<CommunityComment> comments;

    public PostWithComments() {
        comments = new ArrayList<>();
    }

    public CommunityPost getPost() {
        return post;
    }

    public void setPost(CommunityPost post) {
        this.post = post;
    }

    public List<CommunityComment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    public void setComments(List<CommunityComment> comments) {
        this.comments = comments;
    }
}

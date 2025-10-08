package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * 社区帖子 DAO。
 */
@Dao
public interface CommunityPostDao {

    @Transaction
    @Query("SELECT * FROM community_posts")
    List<PostWithComments> getAllPostsWithComments();

    @Transaction
    @Query("SELECT * FROM community_posts WHERE title LIKE :keyword OR content LIKE :keyword OR authorName LIKE :keyword")
    List<PostWithComments> searchPostsWithComments(String keyword);

    @Query("SELECT COUNT(*) FROM community_posts")
    int getPostCount();

    @Insert
    long insert(CommunityPost post);

    @Delete
    void delete(CommunityPost post);
}

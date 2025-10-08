package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * 社区回复 DAO。
 */
@Dao
public interface CommunityCommentDao {

    @Query("SELECT * FROM community_comments WHERE postId = :postId ORDER BY createdAt ASC")
    List<CommunityComment> getCommentsForPost(int postId);

    @Insert
    long insert(CommunityComment comment);

    @Query("DELETE FROM community_comments WHERE postId = :postId")
    void deleteCommentsForPost(int postId);
}

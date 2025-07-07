package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

/**
 * 反馈信息数据访问对象
 */
@Dao
public interface FeedbackDao {
    
    /**
     * 插入一条新的反馈信息
     * @param feedback 反馈对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFeedback(Feedback feedback);
    
} 
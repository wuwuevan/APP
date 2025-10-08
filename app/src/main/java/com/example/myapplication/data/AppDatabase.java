package com.example.myapplication.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 应用数据库类，使用Room持久化库
 */
@Database(entities = {User.class, Feedback.class, CommunityPost.class, CommunityComment.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "rehabilitation_app.db";
    private static volatile AppDatabase instance;
    
    /**
     * 获取用户DAO
     * @return UserDao实例
     */
    public abstract UserDao userDao();
    
    /**
     * 获取反馈DAO
     * @return FeedbackDao实例
     */
    public abstract FeedbackDao feedbackDao();

    /**
     * 获取社区帖子 DAO
     */
    public abstract CommunityPostDao communityPostDao();

    /**
     * 获取社区回复 DAO
     */
    public abstract CommunityCommentDao communityCommentDao();
    
    /**
     * 获取数据库实例（单例模式）
     * @param context 上下文
     * @return 数据库实例
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .allowMainThreadQueries() // 仅用于简化示例，实际应用中应使用异步操作
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
} 
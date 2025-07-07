package com.example.myapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类，负责创建和升级数据库
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // 数据库名称
    private static final String DATABASE_NAME = "rehabilitation.db";
    // 数据库版本
    private static final int DATABASE_VERSION = 3;

    // 表名
    public static final String TABLE_USER = "user";
    public static final String TABLE_DAILY_TASK = "daily_task";
    public static final String TABLE_REHABILITATION_EXERCISE = "rehabilitation_exercise";
    public static final String TABLE_HEALTH_INDICATOR = "health_indicator";
    public static final String TABLE_CHAT_MESSAGE = "chat_message";
    public static final String TABLE_NOTIFICATION = "notification";
    public static final String TABLE_EDUCATION_CONTENT = "education_content";
    public static final String TABLE_SURVEY = "survey";
    public static final String TABLE_PAIN_SCORE = "pain_score";
    public static final String TABLE_CUSTOM_HEALTH_INDICATOR = "custom_health_indicator";

    // 单例模式
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "avatar TEXT," +
                "gender TEXT," +
                "age INTEGER," +
                "height REAL," +
                "weight REAL," +
                "medical_record TEXT," +
                "register_time TEXT" +
                ")");

        // 创建每日任务表
        db.execSQL("CREATE TABLE " + TABLE_DAILY_TASK + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "task_name TEXT NOT NULL," +
                "task_desc TEXT," +
                "task_type TEXT," +
                "start_time TEXT," +
                "end_time TEXT," +
                "status TEXT," +
                "completion_rate REAL," +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                ")");

        // 创建康复训练表
        db.execSQL("CREATE TABLE " + TABLE_REHABILITATION_EXERCISE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "exercise_name TEXT NOT NULL," +
                "exercise_desc TEXT," +
                "video_url TEXT," +
                "image_url TEXT," +
                "duration INTEGER," +
                "count INTEGER," +
                "finished_time TEXT," +
                "completion_status TEXT," +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                ")");

        // 创建健康指标表
        db.execSQL("CREATE TABLE " + TABLE_HEALTH_INDICATOR + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "indicator_type TEXT NOT NULL," +
                "indicator_value REAL," +
                "record_time TEXT," +
                "is_abnormal INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                ")");

        // 创建聊天消息表
        db.execSQL("CREATE TABLE " + TABLE_CHAT_MESSAGE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "sender_type TEXT," +
                "content TEXT," +
                "send_time TEXT," +
                "is_read INTEGER," +
                "is_risk INTEGER," +
                "is_encrypted INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                ")");

        // 创建通知消息表
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "notification_type TEXT," +
                "title TEXT," +
                "content TEXT," +
                "create_time TEXT," +
                "is_read INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                ")");
                
        // 创建教育内容表
        db.execSQL("CREATE TABLE " + TABLE_EDUCATION_CONTENT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER," +
                "title TEXT NOT NULL," +
                "content TEXT," +
                "type TEXT," +
                "create_time TEXT," +
                "FOREIGN KEY(task_id) REFERENCES " + TABLE_DAILY_TASK + "(id)" +
                ")");
                
        // 创建问卷表
        db.execSQL("CREATE TABLE " + TABLE_SURVEY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "questions TEXT," + // JSON格式存储问题
                "answers TEXT," + // JSON格式存储回答
                "submit_time TEXT," +
                "FOREIGN KEY(task_id) REFERENCES " + TABLE_DAILY_TASK + "(id)" +
                ")");
                
        // 创建疼痛评分表
        db.execSQL("CREATE TABLE " + TABLE_PAIN_SCORE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER," +
                "user_id INTEGER," +
                "score INTEGER," + // 0-10分
                "location TEXT," + // 疼痛部位
                "description TEXT," + // 疼痛描述
                "record_time TEXT," +
                "FOREIGN KEY(task_id) REFERENCES " + TABLE_DAILY_TASK + "(id)," +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                ")");
                
        // 创建自定义健康指标表
        db.execSQL("CREATE TABLE " + TABLE_CUSTOM_HEALTH_INDICATOR + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "indicator_id INTEGER," + // 关联到健康指标表的ID
                "unit TEXT," + // 单位
                "normal_min_value REAL," + // 正常范围最小值
                "normal_max_value REAL," + // 正常范围最大值
                "notes TEXT," + // 备注
                "FOREIGN KEY(indicator_id) REFERENCES " + TABLE_HEALTH_INDICATOR + "(id)" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库
        if (oldVersion < 2) {
            // 添加教育内容表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EDUCATION_CONTENT + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "task_id INTEGER," +
                    "title TEXT NOT NULL," +
                    "content TEXT," +
                    "type TEXT," +
                    "create_time TEXT," +
                    "FOREIGN KEY(task_id) REFERENCES " + TABLE_DAILY_TASK + "(id)" +
                    ")");
                    
            // 添加问卷表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SURVEY + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "task_id INTEGER," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "questions TEXT," + // JSON格式存储问题
                    "answers TEXT," + // JSON格式存储回答
                    "submit_time TEXT," +
                    "FOREIGN KEY(task_id) REFERENCES " + TABLE_DAILY_TASK + "(id)" +
                    ")");
                    
            // 添加疼痛评分表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PAIN_SCORE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "task_id INTEGER," +
                    "user_id INTEGER," +
                    "score INTEGER," + // 0-10分
                    "location TEXT," + // 疼痛部位
                    "description TEXT," + // 疼痛描述
                    "record_time TEXT," +
                    "FOREIGN KEY(task_id) REFERENCES " + TABLE_DAILY_TASK + "(id)," +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USER + "(id)" +
                    ")");
        }
        
        if (oldVersion < 3) {
            // 添加自定义健康指标表
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOM_HEALTH_INDICATOR + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "indicator_id INTEGER," + // 关联到健康指标表的ID
                    "unit TEXT," + // 单位
                    "normal_min_value REAL," + // 正常范围最小值
                    "normal_max_value REAL," + // 正常范围最大值
                    "notes TEXT," + // 备注
                    "FOREIGN KEY(indicator_id) REFERENCES " + TABLE_HEALTH_INDICATOR + "(id)" +
                    ")");
        }
    }
} 
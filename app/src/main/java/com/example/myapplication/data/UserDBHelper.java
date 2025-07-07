package com.example.myapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 用户数据库帮助类，负责创建和管理用户数据库
 */
public class UserDBHelper extends SQLiteOpenHelper {
    // 数据库名称
    private static final String DATABASE_NAME = "user.db";
    // 数据库版本
    private static final int DATABASE_VERSION = 1;
    // 用户表名
    public static final String TABLE_USER = "user";
    // 用户表字段
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_REGISTER_TIME = "register_time";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_MEDICAL_RECORD = "medical_record";
    
    // 创建用户表的SQL语句
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
            COLUMN_PASSWORD + " TEXT NOT NULL, " +
            COLUMN_NICKNAME + " TEXT, " +
            COLUMN_REGISTER_TIME + " TEXT, " +
            COLUMN_GENDER + " TEXT, " +
            COLUMN_AGE + " INTEGER, " +
            COLUMN_HEIGHT + " REAL, " +
            COLUMN_WEIGHT + " REAL, " +
            COLUMN_MEDICAL_RECORD + " TEXT" +
            ");";
    
    /**
     * 构造方法
     * @param context 上下文
     */
    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * 创建数据库表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
    }
    
    /**
     * 数据库升级
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单处理，直接删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
} 
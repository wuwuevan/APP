package com.example.myapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "rehabilitation_assistant.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_EXERCISE = "exercise";
    public static final String TABLE_USER = "user";

    // 训练计划表字段
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_DETAIL_DESCRIPTION = "detail_description";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_IMAGE_RES_ID = "image_res_id";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_IS_DEFAULT = "is_default";
    
    // 用户表字段
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_REGISTER_TIME = "register_time";
    public static final String COLUMN_LAST_LOGIN_TIME = "last_login_time";
    public static final String COLUMN_IS_LOGGED_IN = "is_logged_in";

    // 创建训练计划表的SQL语句
    private static final String CREATE_TABLE_EXERCISE = "CREATE TABLE " + TABLE_EXERCISE + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " TEXT NOT NULL, "
            + COLUMN_DESCRIPTION + " TEXT NOT NULL, "
            + COLUMN_DURATION + " TEXT NOT NULL, "
            + COLUMN_CALORIES + " INTEGER NOT NULL, "
            + COLUMN_DIFFICULTY + " TEXT NOT NULL, "
            + COLUMN_DETAIL_DESCRIPTION + " TEXT NOT NULL, "
            + COLUMN_STEPS + " TEXT, "
            + COLUMN_NOTES + " TEXT, "
            + COLUMN_IMAGE_RES_ID + " INTEGER NOT NULL, "
            + COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + COLUMN_IS_DEFAULT + " INTEGER DEFAULT 0"
            + ");";
            
    // 创建用户表的SQL语句
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_AVATAR + " TEXT, "
            + COLUMN_GENDER + " TEXT, "
            + COLUMN_AGE + " INTEGER, "
            + COLUMN_HEIGHT + " REAL, "
            + COLUMN_WEIGHT + " REAL, "
            + COLUMN_PHONE + " TEXT, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_REGISTER_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + COLUMN_LAST_LOGIN_TIME + " TIMESTAMP, "
            + COLUMN_IS_LOGGED_IN + " INTEGER DEFAULT 0"
            + ");";

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
        // 创建表
        db.execSQL(CREATE_TABLE_EXERCISE);
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库
        if (oldVersion < newVersion) {
            // 简单处理，直接删除旧表，创建新表
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            onCreate(db);
        }
    }
} 
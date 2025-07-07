package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类，用于管理用户登录信息等
 */
public class SharedPreferencesUtil {

    private static final String PREF_NAME = "user_info";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_CURRENT_USERNAME = "current_username";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    
    public SharedPreferencesUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * 保存登录信息
     */
    public void saveLoginInfo(long userId, String username) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_CURRENT_USERNAME, username);
        editor.apply();
    }
    
    /**
     * 获取当前登录用户ID
     */
    public long getCurrentUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }
    
    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }
    
    /**
     * 清除登录信息
     */
    public void clearLoginInfo() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_CURRENT_USERNAME);
        editor.apply();
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return getCurrentUserId() != -1;
    }
    
    /**
     * 保存字符串值
     */
    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }
    
    /**
     * 获取字符串值
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
    
    /**
     * 保存整数值
     */
    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }
    
    /**
     * 获取整数值
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
    
    /**
     * 保存整数值（别名方法，与 putInt 功能相同）
     */
    public void saveInt(String key, int value) {
        putInt(key, value);
    }
    
    /**
     * 保存布尔值
     */
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    /**
     * 获取布尔值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
} 
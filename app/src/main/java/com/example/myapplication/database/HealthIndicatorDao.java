package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.CustomHealthIndicator;
import com.example.myapplication.model.HealthIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康指标数据访问对象
 */
public class HealthIndicatorDao {
    private DatabaseHelper dbHelper;

    public HealthIndicatorDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 添加健康指标
     * @param healthIndicator 健康指标对象
     * @return 新增记录的ID
     */
    public long addHealthIndicator(HealthIndicator healthIndicator) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", healthIndicator.getUserId());
        values.put("indicator_type", healthIndicator.getIndicatorType());
        values.put("indicator_value", healthIndicator.getIndicatorValue());
        values.put("record_time", healthIndicator.getRecordTime());
        values.put("is_abnormal", healthIndicator.getIsAbnormal());

        long id = db.insert(DatabaseHelper.TABLE_HEALTH_INDICATOR, null, values);
        db.close();
        return id;
    }

    /**
     * 获取用户最新的健康指标
     * @param userId 用户ID
     * @param indicatorType 指标类型
     * @return 健康指标对象
     */
    public HealthIndicator getLatestHealthIndicator(int userId, String indicatorType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HEALTH_INDICATOR, null, 
                "user_id = ? AND indicator_type = ?", 
                new String[]{String.valueOf(userId), indicatorType}, 
                null, null, "record_time DESC", "1");
        
        HealthIndicator indicator = null;
        if (cursor != null && cursor.moveToFirst()) {
            indicator = cursorToHealthIndicator(cursor);
            cursor.close();
        }
        db.close();
        return indicator;
    }

    /**
     * 获取用户所有健康指标
     * @param userId 用户ID
     * @return 健康指标列表
     */
    public List<HealthIndicator> getUserHealthIndicators(int userId) {
        List<HealthIndicator> indicatorList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_HEALTH_INDICATOR, null, 
                "user_id = ?", new String[]{String.valueOf(userId)}, 
                null, null, "record_time DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                HealthIndicator indicator = cursorToHealthIndicator(cursor);
                indicatorList.add(indicator);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return indicatorList;
    }

    /**
     * 获取用户特定类型的健康指标
     * @param userId 用户ID
     * @param indicatorType 指标类型
     * @return 健康指标列表
     */
    public List<HealthIndicator> getUserHealthIndicatorsByType(int userId, String indicatorType) {
        List<HealthIndicator> indicatorList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_HEALTH_INDICATOR, null, 
                "user_id = ? AND indicator_type = ?", 
                new String[]{String.valueOf(userId), indicatorType}, 
                null, null, "record_time DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                HealthIndicator indicator = cursorToHealthIndicator(cursor);
                indicatorList.add(indicator);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return indicatorList;
    }

    /**
     * 将游标转换为健康指标对象
     * @param cursor 游标
     * @return 健康指标对象
     */
    private HealthIndicator cursorToHealthIndicator(Cursor cursor) {
        HealthIndicator indicator = new HealthIndicator();
        indicator.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        indicator.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        indicator.setIndicatorType(cursor.getString(cursor.getColumnIndexOrThrow("indicator_type")));
        indicator.setIndicatorValue(cursor.getFloat(cursor.getColumnIndexOrThrow("indicator_value")));
        indicator.setRecordTime(cursor.getString(cursor.getColumnIndexOrThrow("record_time")));
        indicator.setIsAbnormal(cursor.getInt(cursor.getColumnIndexOrThrow("is_abnormal")));
        return indicator;
    }
    
    /**
     * 添加自定义健康指标信息
     * @param customHealthIndicator 自定义健康指标对象
     * @return 是否添加成功
     */
    public boolean addCustomHealthIndicatorInfo(CustomHealthIndicator customHealthIndicator) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("indicator_id", customHealthIndicator.getIndicatorId());
        values.put("unit", customHealthIndicator.getUnit());
        values.put("normal_min_value", customHealthIndicator.getNormalMinValue());
        values.put("normal_max_value", customHealthIndicator.getNormalMaxValue());
        values.put("notes", customHealthIndicator.getNotes());
        
        long id = db.insert(DatabaseHelper.TABLE_CUSTOM_HEALTH_INDICATOR, null, values);
        db.close();
        return id > 0;
    }
    
    /**
     * 获取自定义健康指标信息
     * @param indicatorId 健康指标ID
     * @return 自定义健康指标对象
     */
    public CustomHealthIndicator getCustomHealthIndicatorInfo(int indicatorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOM_HEALTH_INDICATOR, null,
                "indicator_id = ?", new String[]{String.valueOf(indicatorId)},
                null, null, null);
                
        CustomHealthIndicator customIndicator = null;
        if (cursor != null && cursor.moveToFirst()) {
            customIndicator = new CustomHealthIndicator();
            customIndicator.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            customIndicator.setIndicatorId(cursor.getInt(cursor.getColumnIndexOrThrow("indicator_id")));
            customIndicator.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
            customIndicator.setNormalMinValue(cursor.getFloat(cursor.getColumnIndexOrThrow("normal_min_value")));
            customIndicator.setNormalMaxValue(cursor.getFloat(cursor.getColumnIndexOrThrow("normal_max_value")));
            customIndicator.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
            cursor.close();
        }
        db.close();
        return customIndicator;
    }
    
    /**
     * 获取用户所有自定义健康指标
     * @param userId 用户ID
     * @return 健康指标列表 (包含自定义信息)
     */
    public List<HealthIndicator> getUserCustomHealthIndicators(int userId) {
        // 创建保存自定义指标的列表
        List<HealthIndicator> customIndicatorList = new ArrayList<>();
        
        // 获取所有指标ID在自定义指标表中的记录
        String query = "SELECT hi.* FROM " + DatabaseHelper.TABLE_HEALTH_INDICATOR + " hi " +
                "JOIN " + DatabaseHelper.TABLE_CUSTOM_HEALTH_INDICATOR + " ci ON hi.id = ci.indicator_id " +
                "WHERE hi.user_id = ? " +
                "ORDER BY hi.record_time DESC";
                
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                HealthIndicator indicator = cursorToHealthIndicator(cursor);
                customIndicatorList.add(indicator);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        // 如果没有找到自定义指标，检查是否有"health_suggestion"类型的指标
        // 这种类型的指标是从健康报告生成的
        if (customIndicatorList.isEmpty()) {
            List<HealthIndicator> suggestionIndicators = getUserHealthIndicatorsByType(userId, "health_suggestion");
            if (!suggestionIndicators.isEmpty()) {
                customIndicatorList.addAll(suggestionIndicators);
            }
        }
        
        db.close();
        return customIndicatorList;
    }
    
    /**
     * 获取用户所有指标类型
     * @param userId 用户ID
     * @return 指标类型列表
     */
    public List<String> getAllIndicatorTypes(int userId) {
        List<String> typeList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(true, DatabaseHelper.TABLE_HEALTH_INDICATOR, 
                new String[]{"indicator_type"}, "user_id = ?", 
                new String[]{String.valueOf(userId)}, "indicator_type", null, null, null);
                
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("indicator_type"));
                typeList.add(type);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return typeList;
    }

    /**
     * 根据ID获取健康指标
     */
    public HealthIndicator getHealthIndicatorById(int indicatorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HEALTH_INDICATOR, null,
                "id = ?", new String[]{String.valueOf(indicatorId)},
                null, null, null);
        HealthIndicator indicator = null;
        if (cursor != null && cursor.moveToFirst()) {
            indicator = cursorToHealthIndicator(cursor);
            cursor.close();
        }
        db.close();
        return indicator;
    }
    
    /**
     * 更新健康指标
     * @param indicator 健康指标对象
     * @return 是否更新成功
     */
    public boolean updateHealthIndicator(HealthIndicator indicator) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("indicator_type", indicator.getIndicatorType());
        values.put("indicator_value", indicator.getIndicatorValue());
        values.put("record_time", indicator.getRecordTime());
        values.put("is_abnormal", indicator.getIsAbnormal());
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_HEALTH_INDICATOR, values, 
                "id = ?", new String[]{String.valueOf(indicator.getId())});
        db.close();
        return rowsAffected > 0;
    }
    
    /**
     * 更新自定义健康指标信息
     * @param customIndicator 自定义健康指标对象
     * @return 是否更新成功
     */
    public boolean updateCustomHealthIndicatorInfo(CustomHealthIndicator customIndicator) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("unit", customIndicator.getUnit());
        values.put("normal_min_value", customIndicator.getNormalMinValue());
        values.put("normal_max_value", customIndicator.getNormalMaxValue());
        values.put("notes", customIndicator.getNotes());
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_CUSTOM_HEALTH_INDICATOR, values, 
                "indicator_id = ?", new String[]{String.valueOf(customIndicator.getIndicatorId())});
        db.close();
        return rowsAffected > 0;
    }
    
    /**
     * 删除健康指标
     * @param indicatorId 健康指标ID
     * @return 是否删除成功
     */
    public boolean deleteHealthIndicator(int indicatorId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // 首先删除相关的自定义信息
        db.delete(DatabaseHelper.TABLE_CUSTOM_HEALTH_INDICATOR, 
                "indicator_id = ?", new String[]{String.valueOf(indicatorId)});
                
        // 然后删除指标本身
        int rowsAffected = db.delete(DatabaseHelper.TABLE_HEALTH_INDICATOR, 
                "id = ?", new String[]{String.valueOf(indicatorId)});
        db.close();
        return rowsAffected > 0;
    }
} 
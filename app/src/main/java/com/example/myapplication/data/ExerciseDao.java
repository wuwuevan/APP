package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.R;
import com.example.myapplication.ui.exercise.ExerciseFragment.ExerciseItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 训练计划数据访问对象
 */
public class ExerciseDao {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public ExerciseDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 打开数据库连接
     */
    private void open() {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * 关闭数据库连接
     */
    private void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    /**
     * 添加训练计划
     */
    public long addExercise(String title, String description, String duration, int calories,
                           String difficulty, String detailDescription, String steps, String notes) {
        open();
        
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_CALORIES, calories);
        values.put(DatabaseHelper.COLUMN_DIFFICULTY, difficulty);
        values.put(DatabaseHelper.COLUMN_DETAIL_DESCRIPTION, detailDescription);
        values.put(DatabaseHelper.COLUMN_STEPS, steps);
        values.put(DatabaseHelper.COLUMN_NOTES, notes);
        values.put(DatabaseHelper.COLUMN_IMAGE_RES_ID, R.drawable.ic_exercise);
        values.put(DatabaseHelper.COLUMN_IS_DEFAULT, 0); // 用户添加的，非默认
        
        long id = database.insert(DatabaseHelper.TABLE_EXERCISE, null, values);
        
        close();
        return id;
    }

    /**
     * 获取所有训练计划
     */
    public List<ExerciseItem> getAllExercises() {
        List<ExerciseItem> exerciseList = new ArrayList<>();
        
        open();
        
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_EXERCISE,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_CREATE_TIME + " DESC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
                int calories = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALORIES));
                String difficulty = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULTY));
                String detailDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_DESCRIPTION));
                int imageResId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_RES_ID));
                
                ExerciseItem item = new ExerciseItem(title, description, duration, calories, difficulty, detailDescription, imageResId);
                exerciseList.add(item);
                
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        close();
        
        return exerciseList;
    }
    
    /**
     * 添加默认训练计划数据
     */
    public void addDefaultExercises(List<ExerciseItem> defaultExercises) {
        open();
        
        // 检查是否已经存在默认数据
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_EXERCISE,
                null,
                DatabaseHelper.COLUMN_IS_DEFAULT + " = ?",
                new String[]{"1"},
                null,
                null,
                null
        );
        
        boolean hasDefaultData = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) {
            cursor.close();
        }
        
        // 如果没有默认数据，添加
        if (!hasDefaultData && defaultExercises != null && !defaultExercises.isEmpty()) {
            database.beginTransaction();
            try {
                for (ExerciseItem item : defaultExercises) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_TITLE, item.getTitle());
                    values.put(DatabaseHelper.COLUMN_DESCRIPTION, item.getDescription());
                    values.put(DatabaseHelper.COLUMN_DURATION, item.getDuration());
                    values.put(DatabaseHelper.COLUMN_CALORIES, item.getCalories());
                    values.put(DatabaseHelper.COLUMN_DIFFICULTY, item.getDifficulty());
                    values.put(DatabaseHelper.COLUMN_DETAIL_DESCRIPTION, item.getDetailDescription());
                    values.put(DatabaseHelper.COLUMN_STEPS, ""); // 默认数据没有步骤
                    values.put(DatabaseHelper.COLUMN_NOTES, ""); // 默认数据没有注意事项
                    values.put(DatabaseHelper.COLUMN_IMAGE_RES_ID, item.getImageResId());
                    values.put(DatabaseHelper.COLUMN_IS_DEFAULT, 1); // 默认数据
                    
                    database.insert(DatabaseHelper.TABLE_EXERCISE, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        
        close();
    }
} 
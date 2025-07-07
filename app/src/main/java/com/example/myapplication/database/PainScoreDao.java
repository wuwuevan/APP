package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.PainScore;

import java.util.ArrayList;
import java.util.List;

/**
 * 疼痛评分数据访问对象
 */
public class PainScoreDao {
    private DatabaseHelper dbHelper;

    public PainScoreDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 添加疼痛评分记录
     * @param painScore 疼痛评分对象
     * @return 新增记录的ID
     */
    public long addPainScore(PainScore painScore) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("task_id", painScore.getTaskId());
        values.put("user_id", painScore.getUserId());
        values.put("score", painScore.getScore());
        values.put("location", painScore.getLocation());
        values.put("description", painScore.getDescription());
        values.put("record_time", painScore.getRecordTime());

        long id = db.insert(DatabaseHelper.TABLE_PAIN_SCORE, null, values);
        db.close();
        return id;
    }

    /**
     * 根据任务ID获取疼痛评分记录
     * @param taskId 任务ID
     * @return 疼痛评分对象
     */
    public PainScore getPainScoreByTaskId(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PAIN_SCORE, null, "task_id = ?", 
                new String[]{String.valueOf(taskId)}, null, null, null);
        
        PainScore painScore = null;
        if (cursor != null && cursor.moveToFirst()) {
            painScore = cursorToPainScore(cursor);
            cursor.close();
        }
        db.close();
        return painScore;
    }

    /**
     * 获取用户所有疼痛评分记录
     * @param userId 用户ID
     * @return 疼痛评分列表
     */
    public List<PainScore> getUserPainScores(int userId) {
        List<PainScore> painScoreList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_PAIN_SCORE, null, "user_id = ?", 
                new String[]{String.valueOf(userId)}, null, null, "record_time DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                PainScore painScore = cursorToPainScore(cursor);
                painScoreList.add(painScore);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return painScoreList;
    }

    /**
     * 更新疼痛评分记录
     * @param painScore 疼痛评分对象
     * @return 影响的行数
     */
    public int updatePainScore(PainScore painScore) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("score", painScore.getScore());
        values.put("location", painScore.getLocation());
        values.put("description", painScore.getDescription());
        values.put("record_time", painScore.getRecordTime());

        int rows = db.update(DatabaseHelper.TABLE_PAIN_SCORE, values, "id = ?", 
                new String[]{String.valueOf(painScore.getId())});
        db.close();
        return rows;
    }

    /**
     * 删除疼痛评分记录
     * @param id 记录ID
     * @return 影响的行数
     */
    public int deletePainScore(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_PAIN_SCORE, "id = ?", 
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /**
     * 将游标转换为疼痛评分对象
     * @param cursor 游标
     * @return 疼痛评分对象
     */
    private PainScore cursorToPainScore(Cursor cursor) {
        PainScore painScore = new PainScore();
        painScore.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        painScore.setTaskId(cursor.getInt(cursor.getColumnIndexOrThrow("task_id")));
        painScore.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        painScore.setScore(cursor.getInt(cursor.getColumnIndexOrThrow("score")));
        painScore.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
        painScore.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        painScore.setRecordTime(cursor.getString(cursor.getColumnIndexOrThrow("record_time")));
        return painScore;
    }
} 
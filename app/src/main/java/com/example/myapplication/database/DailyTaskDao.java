package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.DailyTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 每日任务数据访问对象
 */
public class DailyTaskDao {
    private DatabaseHelper dbHelper;

    public DailyTaskDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 添加每日任务
     * @param dailyTask 任务对象
     * @return 新增任务的ID
     */
    public long addDailyTask(DailyTask dailyTask) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", dailyTask.getUserId());
        values.put("task_name", dailyTask.getTaskName());
        values.put("task_desc", dailyTask.getTaskDesc());
        values.put("task_type", dailyTask.getTaskType());
        values.put("start_time", dailyTask.getStartTime());
        values.put("end_time", dailyTask.getEndTime());
        values.put("status", dailyTask.getStatus());
        values.put("completion_rate", dailyTask.getCompletionRate());

        long id = db.insert(DatabaseHelper.TABLE_DAILY_TASK, null, values);
        db.close();
        return id;
    }

    /**
     * 批量添加每日任务
     * @param dailyTaskList 任务列表
     * @return 成功添加的数量
     */
    public int addDailyTaskBatch(List<DailyTask> dailyTaskList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        try {
            db.beginTransaction();
            for (DailyTask dailyTask : dailyTaskList) {
                ContentValues values = new ContentValues();
                values.put("user_id", dailyTask.getUserId());
                values.put("task_name", dailyTask.getTaskName());
                values.put("task_desc", dailyTask.getTaskDesc());
                values.put("task_type", dailyTask.getTaskType());
                values.put("start_time", dailyTask.getStartTime());
                values.put("end_time", dailyTask.getEndTime());
                values.put("status", dailyTask.getStatus());
                values.put("completion_rate", dailyTask.getCompletionRate());

                long id = db.insert(DatabaseHelper.TABLE_DAILY_TASK, null, values);
                if (id != -1) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return count;
    }

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 新状态
     * @param completionRate 完成率
     * @return 影响的行数
     */
    public int updateTaskStatus(int id, String status, float completionRate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("completion_rate", completionRate);

        int rows = db.update(DatabaseHelper.TABLE_DAILY_TASK, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /**
     * 获取用户今天的任务列表
     * @param userId 用户ID
     * @param today 今天的日期字符串，格式如："2025-06-01"
     * @return 任务列表
     */
    public List<DailyTask> getTodayTasks(int userId, String today) {
        List<DailyTask> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查找今天的任务（开始时间小于等于今天，结束时间大于等于今天）
        String selection = "user_id = ? AND start_time <= ? AND end_time >= ?";
        String[] selectionArgs = {String.valueOf(userId), today, today};
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_DAILY_TASK, null, selection, selectionArgs, null, null, "start_time ASC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DailyTask task = cursorToDailyTask(cursor);
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    /**
     * 获取用户所有任务
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<DailyTask> getAllUserTasks(int userId) {
        List<DailyTask> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_DAILY_TASK, null, "user_id = ?", 
                new String[]{String.valueOf(userId)}, null, null, "start_time DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DailyTask task = cursorToDailyTask(cursor);
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    /**
     * 获取任务详情
     * @param taskId 任务ID
     * @return 任务对象
     */
    public DailyTask getTaskById(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DAILY_TASK, null, "id = ?", 
                new String[]{String.valueOf(taskId)}, null, null, null);
        
        DailyTask task = null;
        if (cursor != null && cursor.moveToFirst()) {
            task = cursorToDailyTask(cursor);
            cursor.close();
        }
        db.close();
        return task;
    }

    /**
     * 删除任务
     * @param taskId 任务ID
     * @return 影响的行数
     */
    public int deleteTask(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_DAILY_TASK, "id = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rows;
    }

    /**
     * 将游标转换为每日任务对象
     * @param cursor 游标
     * @return 每日任务对象
     */
    private DailyTask cursorToDailyTask(Cursor cursor) {
        DailyTask task = new DailyTask();
        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        task.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        task.setTaskName(cursor.getString(cursor.getColumnIndexOrThrow("task_name")));
        task.setTaskDesc(cursor.getString(cursor.getColumnIndexOrThrow("task_desc")));
        task.setTaskType(cursor.getString(cursor.getColumnIndexOrThrow("task_type")));
        task.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
        task.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("end_time")));
        task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        task.setCompletionRate(cursor.getFloat(cursor.getColumnIndexOrThrow("completion_rate")));
        return task;
    }
} 
package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.EducationContent;

import java.util.ArrayList;
import java.util.List;

/**
 * 教育内容数据访问对象
 */
public class EducationContentDao {
    private DatabaseHelper dbHelper;

    public EducationContentDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 添加教育内容
     * @param content 教育内容对象
     * @return 新增内容的ID
     */
    public long addContent(EducationContent content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("task_id", content.getTaskId());
        values.put("title", content.getTitle());
        values.put("content", content.getContent());
        values.put("type", content.getType());
        values.put("create_time", content.getCreateTime());

        long id = db.insert(DatabaseHelper.TABLE_EDUCATION_CONTENT, null, values);
        db.close();
        return id;
    }

    /**
     * 根据任务ID获取教育内容
     * @param taskId 任务ID
     * @return 教育内容对象
     */
    public EducationContent getContentByTaskId(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EDUCATION_CONTENT, null, "task_id = ?", 
                new String[]{String.valueOf(taskId)}, null, null, null);
        
        EducationContent content = null;
        if (cursor != null && cursor.moveToFirst()) {
            content = cursorToContent(cursor);
            cursor.close();
        }
        db.close();
        return content;
    }

    /**
     * 获取所有教育内容
     * @return 教育内容列表
     */
    public List<EducationContent> getAllContents() {
        List<EducationContent> contentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_EDUCATION_CONTENT, null, null, null, null, null, "create_time DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                EducationContent content = cursorToContent(cursor);
                contentList.add(content);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return contentList;
    }

    /**
     * 更新教育内容
     * @param content 教育内容对象
     * @return 影响的行数
     */
    public int updateContent(EducationContent content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", content.getTitle());
        values.put("content", content.getContent());
        values.put("type", content.getType());

        int rows = db.update(DatabaseHelper.TABLE_EDUCATION_CONTENT, values, "id = ?", 
                new String[]{String.valueOf(content.getId())});
        db.close();
        return rows;
    }

    /**
     * 删除教育内容
     * @param contentId 内容ID
     * @return 影响的行数
     */
    public int deleteContent(int contentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_EDUCATION_CONTENT, "id = ?", 
                new String[]{String.valueOf(contentId)});
        db.close();
        return rows;
    }

    /**
     * 将游标转换为教育内容对象
     * @param cursor 游标
     * @return 教育内容对象
     */
    private EducationContent cursorToContent(Cursor cursor) {
        EducationContent content = new EducationContent();
        content.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        content.setTaskId(cursor.getInt(cursor.getColumnIndexOrThrow("task_id")));
        content.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        content.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        content.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        content.setCreateTime(cursor.getString(cursor.getColumnIndexOrThrow("create_time")));
        return content;
    }
} 
package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Survey;

import java.util.ArrayList;
import java.util.List;

/**
 * 问卷数据访问对象
 */
public class SurveyDao {
    private DatabaseHelper dbHelper;

    public SurveyDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 添加问卷
     * @param survey 问卷对象
     * @return 新增问卷的ID
     */
    public long addSurvey(Survey survey) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("task_id", survey.getTaskId());
        values.put("title", survey.getTitle());
        values.put("description", survey.getDescription());
        values.put("questions", survey.getQuestions());
        values.put("answers", survey.getAnswers());
        values.put("submit_time", survey.getSubmitTime());

        long id = db.insert(DatabaseHelper.TABLE_SURVEY, null, values);
        db.close();
        return id;
    }

    /**
     * 根据任务ID获取问卷
     * @param taskId 任务ID
     * @return 问卷对象
     */
    public Survey getSurveyByTaskId(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SURVEY, null, "task_id = ?", 
                new String[]{String.valueOf(taskId)}, null, null, null);
        
        Survey survey = null;
        if (cursor != null && cursor.moveToFirst()) {
            survey = cursorToSurvey(cursor);
            cursor.close();
        }
        db.close();
        return survey;
    }

    /**
     * 更新问卷答案
     * @param id 问卷ID
     * @param answers 答案JSON字符串
     * @param submitTime 提交时间
     * @return 影响的行数
     */
    public int updateSurveyAnswers(int id, String answers, String submitTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("answers", answers);
        values.put("submit_time", submitTime);

        int rows = db.update(DatabaseHelper.TABLE_SURVEY, values, "id = ?", 
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /**
     * 获取所有已提交的问卷
     * @return 问卷列表
     */
    public List<Survey> getSubmittedSurveys() {
        List<Survey> surveyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_SURVEY, null, "submit_time IS NOT NULL", 
                null, null, null, "submit_time DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Survey survey = cursorToSurvey(cursor);
                surveyList.add(survey);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return surveyList;
    }

    /**
     * 将游标转换为问卷对象
     * @param cursor 游标
     * @return 问卷对象
     */
    private Survey cursorToSurvey(Cursor cursor) {
        Survey survey = new Survey();
        survey.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        survey.setTaskId(cursor.getInt(cursor.getColumnIndexOrThrow("task_id")));
        survey.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        survey.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        survey.setQuestions(cursor.getString(cursor.getColumnIndexOrThrow("questions")));
        survey.setAnswers(cursor.getString(cursor.getColumnIndexOrThrow("answers")));
        survey.setSubmitTime(cursor.getString(cursor.getColumnIndexOrThrow("submit_time")));
        return survey;
    }
} 
package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Message;
import com.example.myapplication.model.RecentChat;

import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    
    private DatabaseHelper dbHelper;
    
    public MessageDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    /**
     * 保存消息到数据库
     */
    public long saveMessage(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CONTENT, message.getContent());
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(DatabaseHelper.COLUMN_IS_SENT, message.isSent() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_DOCTOR_NAME, message.getDoctorName());
        
        long id = db.insert(DatabaseHelper.TABLE_MESSAGES, null, values);
        message.setId(id);
        return id;
    }
    
    /**
     * 获取与特定医生的所有聊天记录
     */
    public List<Message> getMessagesByDoctor(String doctorName) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = DatabaseHelper.COLUMN_DOCTOR_NAME + " = ?";
        String[] selectionArgs = {doctorName};
        String orderBy = DatabaseHelper.COLUMN_TIMESTAMP + " ASC";
        
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MESSAGES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Message message = cursorToMessage(cursor);
                messages.add(message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return messages;
    }
    
    /**
     * 删除与特定医生的所有聊天记录
     */
    public int deleteMessagesByDoctor(String doctorName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_DOCTOR_NAME + " = ?";
        String[] whereArgs = {doctorName};
        return db.delete(DatabaseHelper.TABLE_MESSAGES, whereClause, whereArgs);
    }

    /**
     * 获取最近的会话列表，每个医生对应一条记录
     */
    public List<RecentChat> getRecentChats() {
        List<RecentChat> chats = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor doctorCursor = db.rawQuery("SELECT DISTINCT " + DatabaseHelper.COLUMN_DOCTOR_NAME + " FROM " + DatabaseHelper.TABLE_MESSAGES, null);
        if (doctorCursor != null && doctorCursor.moveToFirst()) {
            do {
                String doctorName = doctorCursor.getString(0);

                // 获取该会话中用户发送的第一条消息
                String firstMessage = "";
                Cursor firstCursor = db.query(
                        DatabaseHelper.TABLE_MESSAGES,
                        new String[]{DatabaseHelper.COLUMN_CONTENT},
                        DatabaseHelper.COLUMN_DOCTOR_NAME + "=? AND " + DatabaseHelper.COLUMN_IS_SENT + "=1",
                        new String[]{doctorName},
                        null,
                        null,
                        DatabaseHelper.COLUMN_TIMESTAMP + " ASC",
                        "1");
                if (firstCursor != null && firstCursor.moveToFirst()) {
                    firstMessage = firstCursor.getString(0);
                    firstCursor.close();
                }

                // 获取最后一条消息内容及时间
                String lastMessage = "";
                long lastTime = 0;
                Cursor lastCursor = db.query(
                        DatabaseHelper.TABLE_MESSAGES,
                        new String[]{DatabaseHelper.COLUMN_CONTENT, DatabaseHelper.COLUMN_TIMESTAMP},
                        DatabaseHelper.COLUMN_DOCTOR_NAME + "=?",
                        new String[]{doctorName},
                        null,
                        null,
                        DatabaseHelper.COLUMN_TIMESTAMP + " DESC",
                        "1");
                if (lastCursor != null && lastCursor.moveToFirst()) {
                    lastMessage = lastCursor.getString(0);
                    lastTime = lastCursor.getLong(1);
                    lastCursor.close();
                }

                chats.add(new RecentChat(doctorName, firstMessage, lastMessage, lastTime));
            } while (doctorCursor.moveToNext());
            doctorCursor.close();
        }

        return chats;
    }
    
    /**
     * 从Cursor中提取Message对象
     */
    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
        message.setContent(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTENT)));
        message.setTimestamp(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP)));
        message.setSent(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_SENT)) == 1);
        message.setDoctorName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOCTOR_NAME)));
        return message;
    }
} 
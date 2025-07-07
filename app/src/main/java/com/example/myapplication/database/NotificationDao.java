package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知数据访问对象，提供通知相关的数据库操作
 */
public class NotificationDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public NotificationDao(Context context) {
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
        if (database != null) {
            database.close();
        }
    }

    /**
     * 插入一条通知记录
     * @param notification 通知对象
     * @return 插入的记录ID，-1表示插入失败
     */
    public long insertNotification(Notification notification) {
        open();
        ContentValues values = new ContentValues();
        values.put("user_id", notification.getUserId());
        values.put("notification_type", notification.getNotificationType());
        values.put("title", notification.getTitle());
        values.put("content", notification.getContent());
        values.put("create_time", notification.getCreateTime());
        values.put("is_read", notification.getIsRead());
        
        long id = database.insert(DatabaseHelper.TABLE_NOTIFICATION, null, values);
        close();
        return id;
    }

    /**
     * 更新通知记录
     * @param notification 通知对象
     * @return 更新的记录数
     */
    public int updateNotification(Notification notification) {
        open();
        ContentValues values = new ContentValues();
        values.put("user_id", notification.getUserId());
        values.put("notification_type", notification.getNotificationType());
        values.put("title", notification.getTitle());
        values.put("content", notification.getContent());
        values.put("create_time", notification.getCreateTime());
        values.put("is_read", notification.getIsRead());
        
        int count = database.update(
            DatabaseHelper.TABLE_NOTIFICATION, 
            values, 
            "id = ?", 
            new String[]{String.valueOf(notification.getId())}
        );
        close();
        return count;
    }

    /**
     * 删除通知记录
     * @param notificationId 通知ID
     * @return 删除的记录数
     */
    public int deleteNotification(int notificationId) {
        open();
        int count = database.delete(
            DatabaseHelper.TABLE_NOTIFICATION, 
            "id = ?", 
            new String[]{String.valueOf(notificationId)}
        );
        close();
        return count;
    }

    /**
     * 根据ID获取通知
     * @param notificationId 通知ID
     * @return 通知对象，如果不存在则返回null
     */
    public Notification getNotificationById(int notificationId) {
        open();
        Notification notification = null;
        
        Cursor cursor = database.query(
            DatabaseHelper.TABLE_NOTIFICATION,
            null,
            "id = ?",
            new String[]{String.valueOf(notificationId)},
            null,
            null,
            null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            notification = cursorToNotification(cursor);
            cursor.close();
        }
        
        close();
        return notification;
    }

    /**
     * 获取用户的所有通知
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<Notification> getNotificationsByUserId(int userId) {
        open();
        List<Notification> notifications = new ArrayList<>();
        
        Cursor cursor = database.query(
            DatabaseHelper.TABLE_NOTIFICATION,
            null,
            "user_id = ?",
            new String[]{String.valueOf(userId)},
            null,
            null,
            "create_time DESC" // 按创建时间降序排列
        );
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                notifications.add(cursorToNotification(cursor));
            }
            cursor.close();
        }
        
        close();
        return notifications;
    }

    /**
     * 获取用户的未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    public int getUnreadNotificationCount(int userId) {
        open();
        int count = 0;
        
        Cursor cursor = database.query(
            DatabaseHelper.TABLE_NOTIFICATION,
            new String[]{"COUNT(id) as count"},
            "user_id = ? AND is_read = 0",
            new String[]{String.valueOf(userId)},
            null,
            null,
            null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex("count"));
            cursor.close();
        }
        
        close();
        return count;
    }

    /**
     * 将用户的所有通知标记为已读
     * @param userId 用户ID
     * @return 更新的记录数
     */
    public int markAllNotificationsAsRead(int userId) {
        open();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        
        int count = database.update(
            DatabaseHelper.TABLE_NOTIFICATION,
            values,
            "user_id = ? AND is_read = 0",
            new String[]{String.valueOf(userId)}
        );
        
        close();
        return count;
    }

    /**
     * 根据类型获取用户的通知
     * @param userId 用户ID
     * @param notificationType 通知类型
     * @return 通知列表
     */
    public List<Notification> getNotificationsByType(int userId, String notificationType) {
        open();
        List<Notification> notifications = new ArrayList<>();
        
        Cursor cursor = database.query(
            DatabaseHelper.TABLE_NOTIFICATION,
            null,
            "user_id = ? AND notification_type = ?",
            new String[]{String.valueOf(userId), notificationType},
            null,
            null,
            "create_time DESC" // 按创建时间降序排列
        );
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                notifications.add(cursorToNotification(cursor));
            }
            cursor.close();
        }
        
        close();
        return notifications;
    }

    /**
     * 将Cursor转换为Notification对象
     * @param cursor 数据库游标
     * @return Notification对象
     */
    private Notification cursorToNotification(Cursor cursor) {
        Notification notification = new Notification();
        notification.setId(cursor.getInt(cursor.getColumnIndex("id")));
        notification.setUserId(cursor.getInt(cursor.getColumnIndex("user_id")));
        notification.setNotificationType(cursor.getString(cursor.getColumnIndex("notification_type")));
        notification.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        notification.setContent(cursor.getString(cursor.getColumnIndex("content")));
        notification.setCreateTime(cursor.getString(cursor.getColumnIndex("create_time")));
        notification.setIsRead(cursor.getInt(cursor.getColumnIndex("is_read")));
        return notification;
    }
} 
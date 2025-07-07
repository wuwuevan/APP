package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象
 */
public class UserDao {
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * 添加用户
     * @param user 用户对象
     * @return 新增用户的ID
     */
    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("nickname", user.getNickname());
        values.put("avatar", user.getAvatar());
        values.put("gender", user.getGender());
        values.put("age", user.getAge());
        values.put("height", user.getHeight());
        values.put("weight", user.getWeight());
        values.put("medical_record", user.getMedicalRecord());
        values.put("register_time", user.getRegisterTime());

        long id = db.insert(DatabaseHelper.TABLE_USER, null, values);
        db.close();
        return id;
    }

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("nickname", user.getNickname());
        values.put("avatar", user.getAvatar());
        values.put("gender", user.getGender());
        values.put("age", user.getAge());
        values.put("height", user.getHeight());
        values.put("weight", user.getWeight());
        values.put("medical_record", user.getMedicalRecord());

        int rows = db.update(DatabaseHelper.TABLE_USER, values, "id = ?", new String[]{String.valueOf(user.getId())});
        db.close();
        return rows;
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    public User getUserById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * 根据用户名和密码查询用户（登录）
     * @param username 用户名
     * @param password 密码
     * @return 用户对象
     */
    public User login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, null, "username = ? AND password = ?", new String[]{username, password}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * 查询所有用户
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, null, null, null, null, null, "id ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = cursorToUser(cursor);
                userList.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return userList;
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响的行数
     */
    public int deleteUser(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_USER, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /**
     * 将游标转换为用户对象
     * @param cursor 游标
     * @return 用户对象
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow("avatar")));
        user.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        user.setAge(cursor.getInt(cursor.getColumnIndexOrThrow("age")));
        user.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow("height")));
        user.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow("weight")));
        user.setMedicalRecord(cursor.getString(cursor.getColumnIndexOrThrow("medical_record")));
        user.setRegisterTime(cursor.getString(cursor.getColumnIndexOrThrow("register_time")));
        return user;
    }
} 
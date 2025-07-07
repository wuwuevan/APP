package com.example.myapplication.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 用户数据访问对象，定义对用户表的操作
 */
@Dao
public interface UserDao {
    
    /**
     * 插入用户，如果已存在则替换
     * @param user 用户对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);
    
    /**
     * 更新用户信息
     * @param user 用户对象
     */
    @Update
    void updateUser(User user);
    
    /**
     * 删除用户
     * @param user 用户对象
     */
    @Delete
    void deleteUser(User user);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
    
    /**
     * 检查用户是否存在
     * @param username 用户名
     * @return 存在返回1，不存在返回0
     */
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int checkUserExists(String username);
} 
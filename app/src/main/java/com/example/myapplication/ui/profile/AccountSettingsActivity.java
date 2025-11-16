package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.example.myapplication.ui.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 账号设置页面，用于修改密码
 * 注意：当前Room实体User类中没有password字段，此页面仅作为示例
 */
public class AccountSettingsActivity extends BaseActivity {

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnChangePassword;
    private Toolbar toolbar;

    private UserDao userDao;
    private SharedPreferencesUtil spUtil;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        
        // 设置工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // 初始化DAO和工具类
        userDao = AppDatabase.getInstance(this).userDao();
        spUtil = new SharedPreferencesUtil(this);

        // 初始化视图
        initViews();

        // 加载用户数据
        loadUserData();

        // 设置修改密码按钮点击事件
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        String username = spUtil.getString("current_username", "");
        if (!username.isEmpty()) {
            currentUser = userDao.getUserByUsername(username);
        }
        
        if (currentUser == null) {
            Toast.makeText(this, "用户数据加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 修改密码
     */
    private void changePassword() {
        if (currentUser == null) {
            Toast.makeText(this, "用户数据加载失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取输入数据
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "请输入当前密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证当前密码是否正确
        if (!currentUser.getPassword().equals(currentPassword)) {
            Toast.makeText(this, "当前密码不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 更新密码
        try {
            currentUser.setPassword(newPassword);
            userDao.updateUser(currentUser);
            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "密码修改失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 
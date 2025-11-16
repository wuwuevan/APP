package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.ui.BaseActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

/**
 * 用户登录页面
 */
public class LoginActivity extends BaseActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private UserDao userDao;
    private SharedPreferencesUtil spUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化数据访问对象
        userDao = AppDatabase.getInstance(this).userDao();
        spUtil = new SharedPreferencesUtil(this);

        // 设置标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("登录");
        }

        // 检查是否已登录
        if (checkLoggedIn()) {
            // 已登录，直接进入主页
            goToMainActivity();
            return;
        }

        // 初始化视图
        initViews();

        // 设置点击事件
        setClickListeners();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
    }

    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        // 登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        // 注册文本点击事件
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 检查是否已登录
     */
    private boolean checkLoggedIn() {
        // 检查SharedPreferences中是否有已登录用户
        return spUtil.isLoggedIn();
    }

    /**
     * 登录
     */
    private void login() {
        // 获取输入
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, R.string.username_empty, Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.password_empty, Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        // 验证用户和密码
        User user = userDao.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            // 登录成功，保存登录信息
            spUtil.putString("current_username", username);
            spUtil.saveLoginInfo(1, username); // 使用固定ID，实际应该使用用户的真实ID
            
            // 登录成功提示
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
            
            // 跳转到主页
            goToMainActivity();
        } else {
            // 用户名或密码错误
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 跳转到主页
     */
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 
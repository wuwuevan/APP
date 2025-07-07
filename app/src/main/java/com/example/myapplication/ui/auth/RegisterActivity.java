package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.utils.SharedPreferencesUtil;

/**
 * 用户注册页面
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private Toolbar toolbar;

    private UserDao userDao;
    private SharedPreferencesUtil spUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化数据访问对象
        userDao = AppDatabase.getInstance(this).userDao();
        spUtil = new SharedPreferencesUtil(this);

        // 设置标题和返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("注册");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        toolbar = findViewById(R.id.toolbar);
        
        // 设置自定义工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        // 注册按钮点击事件
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        // 登录文本点击事件
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到登录页面
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 注册
     */
    private void register() {
        // 获取输入
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

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

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.password_not_match, Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }

        // 检查用户名是否已存在
        int userExists = userDao.checkUserExists(username);
        
        if (userExists > 0) {
            // 用户名已存在
            Toast.makeText(this, R.string.username_exists, Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        // 创建新用户
        User newUser = new User(username);
        newUser.setPassword(password);
        newUser.setRegisterTimestamp(System.currentTimeMillis());
        
        try {
            // 注册用户
            long id = userDao.insertUser(newUser);
            
            // 保存登录信息
            spUtil.putString("current_username", username);
            spUtil.saveLoginInfo(id, username);
            
            // 注册成功提示
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
            
            // 注册成功，跳转到初始健康信息页面
            Intent intent = new Intent(RegisterActivity.this, com.example.myapplication.ui.health.InitialHealthMetricsActivity.class);
            startActivity(intent);
            finish(); // 结束当前活动
        } catch (Exception e) {
            // 注册失败
            Toast.makeText(this, R.string.register_failed + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
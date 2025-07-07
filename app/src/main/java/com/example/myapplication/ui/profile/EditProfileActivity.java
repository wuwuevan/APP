package com.example.myapplication.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.utils.FileUtil;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;

/**
 * 个人信息编辑页面
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_PICK_IMAGE = 101;

    private ImageView ivAvatar;
    private Button btnChangeAvatar;
    private TextInputEditText etUsername;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private TextInputEditText etAge;
    private TextInputEditText etHeight;
    private TextInputEditText etWeight;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextInputEditText etMedicalRecord;
    private Button btnSave;
    private Toolbar toolbar;

    private UserDao userDao;
    private SharedPreferencesUtil spUtil;
    private User currentUser;
    private String selectedImagePath; // 选择的图片路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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

        // 设置点击事件
        setClickListeners();

        // 加载用户数据
        loadUserData();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        etUsername = findViewById(R.id.et_username);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etMedicalRecord = findViewById(R.id.et_medical_record);
        btnSave = findViewById(R.id.btn_save);
    }

    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        // 更换头像按钮点击事件
        btnChangeAvatar.setOnClickListener(v -> {
            // 检查并请求权限
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> saveUserData());
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        // 从SharedPreferences获取当前登录用户名
        String username = spUtil.getString("current_username", "");
        
        if (!TextUtils.isEmpty(username)) {
            // 从数据库获取用户信息
            currentUser = userDao.getUserByUsername(username);
            
            // 如果数据库中没有该用户，则创建一个新用户
            if (currentUser == null) {
                currentUser = new User(username);
                userDao.insertUser(currentUser);
            }
            
            // 填充表单数据
            etUsername.setText(username);
            

            // 设置性别
            if ("男".equals(currentUser.getGender())) {
                rbMale.setChecked(true);
            } else if ("女".equals(currentUser.getGender())) {
                rbFemale.setChecked(true);
            }

            // 设置年龄、身高、体重
            if (currentUser.getAge() > 0) {
                etAge.setText(String.valueOf(currentUser.getAge()));
            }
            if (currentUser.getHeight() > 0) {
                etHeight.setText(String.valueOf(currentUser.getHeight()));
            }
            if (currentUser.getWeight() > 0) {
                etWeight.setText(String.valueOf(currentUser.getWeight()));
            }

            // 设置联系方式
            if (currentUser.getPhone() != null) {
                etPhone.setText(currentUser.getPhone());
            }
            if (currentUser.getEmail() != null) {
                etEmail.setText(currentUser.getEmail());
            }
            
            // 设置病历信息
            if (currentUser.getMedicalRecord() != null) {
                etMedicalRecord.setText(currentUser.getMedicalRecord());
            }
            
            // 加载头像
            int defaultAvatar = "女".equals(currentUser.getGender()) ? R.drawable.ic_avatar_female : R.drawable.ic_avatar_male;
            if (currentUser.getAvatarPath() != null && !currentUser.getAvatarPath().isEmpty()) {
                // 使用Glide加载头像图片
                Glide.with(this)
                        .load(new File(currentUser.getAvatarPath()))
                        .circleCrop()
                        .placeholder(defaultAvatar)
                        .into(ivAvatar);

                // 保存当前选择的图片路径
                selectedImagePath = currentUser.getAvatarPath();
            } else {
                ivAvatar.setImageResource(defaultAvatar);
            }
        } else {
            Toast.makeText(this, "未登录，请先登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 保存用户数据
     */
    private void saveUserData() {
        if (currentUser == null) {
            Toast.makeText(this, "用户数据加载失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取输入数据
        String gender = rbMale.isChecked() ? "男" : "女";
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String medicalRecord = etMedicalRecord.getText().toString().trim();


        // 更新用户数据
        currentUser.setGender(gender);
        currentUser.setPhone(phone);
        currentUser.setEmail(email);
        currentUser.setMedicalRecord(medicalRecord);
        
        // 更新头像路径
        if (selectedImagePath != null) {
            currentUser.setAvatarPath(selectedImagePath);
        }

        // 解析年龄
        if (!TextUtils.isEmpty(ageStr)) {
            try {
                int age = Integer.parseInt(ageStr);
                currentUser.setAge(age);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "年龄格式不正确", Toast.LENGTH_SHORT).show();
                etAge.requestFocus();
                return;
            }
        }

        // 解析身高
        if (!TextUtils.isEmpty(heightStr)) {
            try {
                float height = Float.parseFloat(heightStr);
                currentUser.setHeight(height);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "身高格式不正确", Toast.LENGTH_SHORT).show();
                etHeight.requestFocus();
                return;
            }
        }

        // 解析体重
        if (!TextUtils.isEmpty(weightStr)) {
            try {
                float weight = Float.parseFloat(weightStr);
                currentUser.setWeight(weight);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "体重格式不正确", Toast.LENGTH_SHORT).show();
                etWeight.requestFocus();
                return;
            }
        }

        // 更新数据库
        try {
            userDao.updateUser(currentUser);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查存储权限
     */
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION
        );
    }

    /**
     * 打开相册选择图片
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，打开相册
                openGallery();
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要读取存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // 使用 FileUtil 获取可靠的文件路径
                    selectedImagePath = FileUtil.getPathFromUri(this, selectedImageUri);
                    
                    if (selectedImagePath != null) {
                        // 使用Glide加载并显示图片
                        Glide.with(this)
                                .load(new File(selectedImagePath))
                                .circleCrop()
                                .into(ivAvatar);
                        
                        Toast.makeText(this, "图片已选择，保存后生效", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "获取图片路径失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "获取图片路径失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
} 
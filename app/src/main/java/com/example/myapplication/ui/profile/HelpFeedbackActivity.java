package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.Feedback;
import com.example.myapplication.data.FeedbackDao;
import com.example.myapplication.ui.BaseActivity;

public class HelpFeedbackActivity extends BaseActivity {

    private Spinner spinnerFeedbackType;
    private EditText etFeedbackContent;
    private EditText etContactInfo;
    private Button btnSubmit;
    private Toolbar toolbar;
    
    private FeedbackDao feedbackDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feedback);

        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        
        // 设置工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        
        // 初始化DAO
        feedbackDao = AppDatabase.getInstance(this).feedbackDao();
        
        // 初始化视图
        initViews();
        
        // 设置点击事件
        setClickListeners();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        spinnerFeedbackType = findViewById(R.id.spinner_feedback_type);
        etFeedbackContent = findViewById(R.id.et_feedback_content);
        etContactInfo = findViewById(R.id.et_contact_info);
        btnSubmit = findViewById(R.id.btn_submit);
        
        // 设置下拉框数据
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.feedback_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFeedbackType.setAdapter(adapter);
    }
    
    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }
    
    /**
     * 提交反馈
     */
    private void submitFeedback() {
        String feedbackType = spinnerFeedbackType.getSelectedItem().toString();
        String content = etFeedbackContent.getText().toString().trim();
        String contactInfo = etContactInfo.getText().toString().trim();
        
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show();
            etFeedbackContent.requestFocus();
            return;
        }
        
        // 创建反馈对象
        Feedback feedback = new Feedback();
        feedback.setFeedbackType(feedbackType);
        feedback.setContent(content);
        feedback.setContactInfo(contactInfo);
        feedback.setTimestamp(System.currentTimeMillis());
        
        // 插入到数据库
        try {
            feedbackDao.insertFeedback(feedback);
            Toast.makeText(this, "反馈已提交，感谢您的支持！", Toast.LENGTH_SHORT).show();
            // 提交成功后清空输入框
            etFeedbackContent.setText("");
            etContactInfo.setText("");
            spinnerFeedbackType.setSelection(0);
        } catch (Exception e) {
            Toast.makeText(this, "提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 
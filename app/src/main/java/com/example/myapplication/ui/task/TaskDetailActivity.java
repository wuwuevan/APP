package com.example.myapplication.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.model.DailyTask;
import com.example.myapplication.ui.BaseActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

/**
 * 任务详情页面
 */
public class TaskDetailActivity extends BaseActivity {
    private ImageView ivTaskIcon;
    private TextView tvTaskName;
    private TextView tvTaskDesc;
    private TextView tvTaskType;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvStatus;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private Button btnContent;
    private Toolbar toolbar;
    
    private DailyTaskDao taskDao;
    private DailyTask currentTask;
    private int taskId;
    private SharedPreferencesUtil spUtil;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        
        // 初始化工具类
        taskDao = new DailyTaskDao(this);
        spUtil = new SharedPreferencesUtil(this);
        
        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        
        // 设置工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        
        // 初始化视图
        initViews();
        
        // 获取传递的任务ID
        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 加载任务数据
        loadTaskData();
        
        // 设置点击事件
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载任务数据以刷新进度和状态
        loadTaskData();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        ivTaskIcon = findViewById(R.id.iv_task_icon);
        tvTaskName = findViewById(R.id.tv_task_name);
        tvTaskDesc = findViewById(R.id.tv_task_desc);
        tvTaskType = findViewById(R.id.tv_task_type);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvStatus = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.progress_bar);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        btnContent = findViewById(R.id.btn_content);
    }
    
    /**
     * 加载任务数据
     */
    private void loadTaskData() {
        currentTask = taskDao.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 设置任务信息
        tvTaskName.setText(currentTask.getTaskName());
        tvTaskDesc.setText(currentTask.getTaskDesc());
        tvTaskType.setText("任务类型：" + currentTask.getTaskType());
        tvStartTime.setText("开始时间：" + currentTask.getStartTime());
        tvEndTime.setText("结束时间：" + currentTask.getEndTime());
        tvStatus.setText("状态：" + currentTask.getStatus());
        
        // 设置进度条
        int progress = (int) (currentTask.getCompletionRate() * 100);
        progressBar.setProgress(progress);
        tvProgressPercent.setText(progress + "%");
        
        // 设置图标
        if ("教育内容".equals(currentTask.getTaskType())) {
            ivTaskIcon.setImageResource(R.drawable.ic_education);
            btnContent.setText("查看教育内容");
            btnContent.setVisibility(View.VISIBLE);
        } else if ("问卷".equals(currentTask.getTaskType())) {
            ivTaskIcon.setImageResource(R.drawable.ic_survey);
            btnContent.setText("填写问卷");
            btnContent.setVisibility(View.VISIBLE);
        } else if ("疼痛评分".equals(currentTask.getTaskType())) {
            ivTaskIcon.setImageResource(R.drawable.ic_health);
            btnContent.setText("进行疼痛评分");
            btnContent.setVisibility(View.VISIBLE);
        } else {
            ivTaskIcon.setImageResource(R.drawable.ic_task);
            btnContent.setVisibility(View.GONE);
        }

        // 如果任务已完成，禁用按钮并显示已完成状态
        if ("已完成".equals(currentTask.getStatus())) {
            btnContent.setEnabled(false);
            btnContent.setText("已完成");
        } else {
            btnContent.setEnabled(true);
        }
        
    }
    
    
    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        // 内容按钮
        btnContent.setOnClickListener(v -> {
            if (currentTask != null) {
                openTaskContent();
            }
        });
    }
    
    
    /**
     * 打开任务内容
     */
    private void openTaskContent() {
        if ("教育内容".equals(currentTask.getTaskType())) {
            // 打开教育内容页面
            Intent intent = new Intent(this, EducationContentActivity.class);
            intent.putExtra("task_id", currentTask.getId());
            startActivity(intent);
        } else if ("问卷".equals(currentTask.getTaskType())) {
            // 打开问卷页面
            Intent intent = new Intent(this, SurveyActivity.class);
            intent.putExtra("task_id", currentTask.getId());
            startActivity(intent);
        } else if ("疼痛评分".equals(currentTask.getTaskType())) {
            // 打开疼痛评分页面
            Intent intent = new Intent(this, PainScoreActivity.class);
            intent.putExtra("task_id", currentTask.getId());
            startActivity(intent);
        }
    }
} 
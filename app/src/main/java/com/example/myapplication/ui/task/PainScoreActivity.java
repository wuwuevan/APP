package com.example.myapplication.ui.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.database.PainScoreDao;
import com.example.myapplication.model.DailyTask;
import com.example.myapplication.model.PainScore;
import com.example.myapplication.ui.BaseActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 疼痛评分页面
 */
public class PainScoreActivity extends BaseActivity {
    private TextView tvTitle;
    private TextView tvScoreValue;
    private SeekBar seekBarPain;
    private EditText etLocation;
    private EditText etDescription;
    private Button btnSubmit;
    
    private DailyTaskDao taskDao;
    private PainScoreDao painScoreDao;
    private SharedPreferencesUtil spUtil;
    private int taskId;
    private long userId;
    private DailyTask currentTask;
    private PainScore currentPainScore;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pain_score);
        
        // 初始化DAO和工具类
        taskDao = new DailyTaskDao(this);
        painScoreDao = new PainScoreDao(this);
        spUtil = new SharedPreferencesUtil(this);
        
        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("疼痛评分");
        }
        
        // 初始化视图
        initViews();
        
        // 获取传递的任务ID和用户ID
        taskId = getIntent().getIntExtra("task_id", -1);
        userId = spUtil.getCurrentUserId();
        
        if (taskId == -1) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (userId == -1) {
            // 如果没有获取到用户ID，尝试从SharedPreferences获取当前用户名
            String username = spUtil.getString("current_username", "");
            if (!TextUtils.isEmpty(username)) {
                // 假设用户ID为1，实际应用中应该根据用户名查询数据库获取用户ID
                userId = 1;
            } else {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        
        // 加载数据
        loadData();
        
        // 设置SeekBar监听器
        seekBarPain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvScoreValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        
        // 设置提交按钮点击事件
        btnSubmit.setOnClickListener(v -> submitPainScore());
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvScoreValue = findViewById(R.id.tv_score_value);
        seekBarPain = findViewById(R.id.seek_bar_pain);
        etLocation = findViewById(R.id.et_location);
        etDescription = findViewById(R.id.et_description);
        btnSubmit = findViewById(R.id.btn_submit);
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        // 获取任务信息
        currentTask = taskDao.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 设置标题
        tvTitle.setText(currentTask.getTaskName());
        
        // 获取疼痛评分记录
        currentPainScore = painScoreDao.getPainScoreByTaskId(taskId);
        if (currentPainScore != null) {
            // 如果已经有记录，显示已有的评分
            seekBarPain.setProgress(currentPainScore.getScore());
            tvScoreValue.setText(String.valueOf(currentPainScore.getScore()));
            etLocation.setText(currentPainScore.getLocation());
            etDescription.setText(currentPainScore.getDescription());
            
            // 如果任务已完成，禁用提交按钮
            if ("已完成".equals(currentTask.getStatus())) {
                btnSubmit.setEnabled(false);
                btnSubmit.setText("已提交");
                Toast.makeText(this, "您已提交过疼痛评分", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 如果没有记录，初始化为0
            seekBarPain.setProgress(0);
            tvScoreValue.setText("0");
        }
        
        // 如果任务未完成，标记为进行中
        if ("未完成".equals(currentTask.getStatus())) {
            taskDao.updateTaskStatus(taskId, "进行中", 0.5f);
        }
    }
    
    /**
     * 提交疼痛评分
     */
    private void submitPainScore() {
        // 获取输入
        int score = seekBarPain.getProgress();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "请输入疼痛部位", Toast.LENGTH_SHORT).show();
            etLocation.requestFocus();
            return;
        }
        
        // 获取当前时间
        String recordTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        // 保存或更新评分
        if (currentPainScore != null) {
            // 更新现有记录
            currentPainScore.setScore(score);
            currentPainScore.setLocation(location);
            currentPainScore.setDescription(description);
            currentPainScore.setRecordTime(recordTime);
            
            int result = painScoreDao.updatePainScore(currentPainScore);
            if (result > 0) {
                // 更新任务状态
                taskDao.updateTaskStatus(taskId, "已完成", 1.0f);
                
                Toast.makeText(this, "评分已更新", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);
                btnSubmit.setText("已提交");
                
                // 返回上一页
                finish();
            } else {
                Toast.makeText(this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 创建新记录
            PainScore painScore = new PainScore(taskId, (int)userId, score, location, description, recordTime);
            long id = painScoreDao.addPainScore(painScore);
            
            if (id > 0) {
                // 更新任务状态
                taskDao.updateTaskStatus(taskId, "已完成", 1.0f);
                
                Toast.makeText(this, "评分已提交", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);
                btnSubmit.setText("已提交");
                
                // 返回上一页
                finish();
            } else {
                Toast.makeText(this, "提交失败，请重试", Toast.LENGTH_SHORT).show();
            }
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
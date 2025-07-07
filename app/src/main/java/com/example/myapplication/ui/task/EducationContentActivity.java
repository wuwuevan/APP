package com.example.myapplication.ui.task;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.database.EducationContentDao;
import com.example.myapplication.model.DailyTask;
import com.example.myapplication.model.EducationContent;

/**
 * 教育内容页面
 */
public class EducationContentActivity extends AppCompatActivity {
    private TextView tvTitle;
    private WebView webView;
    private Toolbar toolbar;
    
    private DailyTaskDao taskDao;
    private EducationContentDao contentDao;
    private int taskId;
    private DailyTask currentTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_content);
        
        // 初始化DAO
        taskDao = new DailyTaskDao(this);
        contentDao = new EducationContentDao(this);
        
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
            Toast.makeText(this, "内容不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 加载内容
        loadContent();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        webView = findViewById(R.id.web_view);
    }
    
    /**
     * 加载内容
     */
    private void loadContent() {
        // 获取任务信息
        currentTask = taskDao.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 设置标题
        tvTitle.setText(currentTask.getTaskName());
        
        // 获取教育内容
        EducationContent content = contentDao.getContentByTaskId(taskId);
        if (content != null) {
            // 加载HTML内容
            String htmlContent = content.getContent();
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            
            // 如果任务未完成，标记为进行中
            if ("未完成".equals(currentTask.getStatus())) {
                taskDao.updateTaskStatus(taskId, "进行中", 0.5f);
            }
        } else {
            // 如果没有找到内容，显示默认内容
            String defaultHtml = "<html><body>" +
                    "<h1>术后康复知识</h1>" +
                    "<p>术后康复是手术治疗的重要组成部分，良好的康复可以帮助患者更快地恢复健康。</p>" +
                    "<h2>术后注意事项</h2>" +
                    "<ul>" +
                    "<li>保持伤口清洁，避免感染</li>" +
                    "<li>按医嘱服用药物，不要擅自停药或加药</li>" +
                    "<li>适当休息，避免过度劳累</li>" +
                    "<li>均衡饮食，补充足够的营养</li>" +
                    "<li>遵循医生建议进行适当的康复训练</li>" +
                    "</ul>" +
                    "<h2>康复训练要点</h2>" +
                    "<p>康复训练应循序渐进，不可操之过急。初期以轻柔活动为主，逐渐增加强度。</p>" +
                    "<p>如果在训练过程中出现疼痛加剧、伤口渗液增多等异常情况，应立即停止并咨询医生。</p>" +
                    "</body></html>";
            webView.loadDataWithBaseURL(null, defaultHtml, "text/html", "UTF-8", null);
            
            // 如果任务未完成，标记为进行中
            if ("未完成".equals(currentTask.getStatus())) {
                taskDao.updateTaskStatus(taskId, "进行中", 0.5f);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果任务是进行中状态，完成任务
        if (currentTask != null && "进行中".equals(currentTask.getStatus())) {
            taskDao.updateTaskStatus(taskId, "已完成", 1.0f);
            Toast.makeText(this, "学习完成！", Toast.LENGTH_SHORT).show();
        }
    }
} 
package com.example.myapplication.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.database.HealthIndicatorDao;
import com.example.myapplication.model.DailyTask;
import com.example.myapplication.model.HealthIndicator;
import com.example.myapplication.ui.task.TaskActivity;
import com.example.myapplication.ui.task.TaskAdapter;
import com.example.myapplication.ui.task.TaskDetailActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 首页仪表盘Fragment，显示用户的主要信息和任务概览
 */
public class HomeFragment extends Fragment {
    private TextView tvTodayTaskCount;
    private TextView tvCompletedTaskCount;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvUserName;
    private TextView tvHeartRate;
    private TextView tvStepCount;
    private TextView tvSleepHours;
    private CardView cardTodayTask;
    private CardView cardHealthIndicators;
    private TextView tvViewAll;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private List<DailyTask> taskList = new ArrayList<>();
    
    private DailyTaskDao taskDao;
    private HealthIndicatorDao healthIndicatorDao;
    private SharedPreferencesUtil spUtil;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        // 初始化DAO和工具类
        taskDao = new DailyTaskDao(requireContext());
        healthIndicatorDao = new HealthIndicatorDao(requireContext());
        spUtil = new SharedPreferencesUtil(requireContext());
        
        // 获取用户ID
        userId = spUtil.getCurrentUserId();
        if (userId == -1) {
            // 如果没有获取到用户ID，尝试从SharedPreferences获取当前用户名
            String username = spUtil.getString("current_username", "");
            if (!username.isEmpty()) {
                // 假设用户ID为1，实际应用中应该根据用户名查询数据库获取用户ID
                userId = 1;
            }
        }
        
        initView(view);
        tvUserName.setText(String.valueOf(userId));
        setClickListeners();
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 如果需要，生成默认任务
        generateDefaultTasksIfNeeded();
        // 每次恢复时重新加载数据
        loadData();
    }

    /**
     * 初始化视图
     */
    private void initView(View view) {
        tvTodayTaskCount = view.findViewById(R.id.tv_today_task_count);
        tvCompletedTaskCount = view.findViewById(R.id.tv_completed_task_count);
        progressBar = view.findViewById(R.id.progress_bar);
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvHeartRate = view.findViewById(R.id.tv_heart_rate);
        tvStepCount = view.findViewById(R.id.tv_step_count);
        tvSleepHours = view.findViewById(R.id.tv_sleep_hours);
        cardTodayTask = view.findViewById(R.id.card_today_task);
        cardHealthIndicators = view.findViewById(R.id.card_health_indicators);
        tvViewAll = view.findViewById(R.id.tv_view_all);
        
        // 初始化RecyclerView
        rvTasks = view.findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(taskList);
        rvTasks.setAdapter(taskAdapter);
        rvTasks.setNestedScrollingEnabled(false); // 在NestedScrollView中禁用滚动
    }
    
    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        // 点击今日任务卡片，跳转到任务中心
        cardTodayTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TaskActivity.class);
            startActivity(intent);
        });
        
        // "查看全部"跳转到任务中心
        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TaskActivity.class);
            startActivity(intent);
        });
        
        // 点击健康指标卡片，跳转到健康页面
        cardHealthIndicators.setOnClickListener(v -> {
            // 切换到健康页面的底部导航
            if (getActivity() != null) {
                getActivity().findViewById(R.id.navigation_health).performClick();
            }
        });

        // 设置任务列表项点击事件
        taskAdapter.setOnTaskActionListener(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskClick(DailyTask task) {
                Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
                intent.putExtra("task_id", task.getId());
                startActivity(intent);
            }

            @Override
            public void onTaskComplete(DailyTask task) {
                // 在主页预览中，完成按钮通常用于快速操作，这里我们只刷新状态
                task.setStatus("已完成");
                task.setCompletionRate(1.0f);
                taskDao.updateTaskStatus(task.getId(), task.getStatus(), task.getCompletionRate());
                loadData(); // 重新加载数据以更新UI
            }

            @Override
            public void onTaskSkip(DailyTask task) {
                // 主页预览暂不处理跳过
            }

            @Override
            public void onTaskDelay(DailyTask task) {
                // 主页预览暂不处理延后
            }
        });
    }

    /**
     * 如果当天没有任务，则生成默认任务
     */
    private void generateDefaultTasksIfNeeded() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<DailyTask> todayTasks = taskDao.getTodayTasks((int) userId, today);
        
        if (todayTasks.isEmpty()) {
            // 创建默认任务
            DailyTask task1 = new DailyTask((int) userId, "阅读康复文章", "了解术后恢复的注意事项", "教育内容", today, null);
            DailyTask task2 = new DailyTask((int) userId, "填写健康问卷", "让我们更好地了解您的恢复情况", "问卷", today, null);
            DailyTask task3 = new DailyTask((int) userId, "记录今日疼痛评分", "记录疼痛变化，帮助医生评估", "疼痛评分", today, null);
            
            // 添加到数据库
            taskDao.addDailyTask(task1);
            taskDao.addDailyTask(task2);
            taskDao.addDailyTask(task3);
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        // 获取当前日期
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // 获取当日任务
        List<DailyTask> todayTasks = taskDao.getTodayTasks((int)userId, today);
        
        // 更新任务列表数据
        taskList.clear();
        taskList.addAll(todayTasks);
        taskAdapter.notifyDataSetChanged();
        
        // 设置任务数量
        int totalTasks = todayTasks.size();
        int completedTasks = 0;
        
        // 计算已完成任务
        for (DailyTask task : todayTasks) {
            if ("已完成".equals(task.getStatus())) {
                completedTasks++;
            }
        }
        
        // 设置显示数据
        tvTodayTaskCount.setText(String.valueOf(totalTasks));
        tvCompletedTaskCount.setText(String.valueOf(completedTasks));
        
        // 设置进度
        int progress = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;
        progressBar.setProgress(progress);
        tvProgressPercent.setText(progress + "%");
        
        // 加载健康指标数据
        loadHealthIndicators();
    }
    
    /**
     * 加载健康指标数据
     */
    private void loadHealthIndicators() {
        // 获取最新的心率数据
        HealthIndicator heartRate = healthIndicatorDao.getLatestHealthIndicator((int)userId, "heart_rate");
        if (heartRate != null) {
            tvHeartRate.setText(String.format(Locale.getDefault(), "%.0f bpm", heartRate.getIndicatorValue()));
        } else {
            // 没有数据时显示默认值
            tvHeartRate.setText("72 bpm");
            
            // 创建并保存默认数据
            saveDefaultHealthIndicator("heart_rate", 72);
        }
        
        // 获取最新的步数数据
        HealthIndicator stepCount = healthIndicatorDao.getLatestHealthIndicator((int)userId, "step_count");
        if (stepCount != null) {
            tvStepCount.setText(String.format(Locale.getDefault(), "%,.0f 步", stepCount.getIndicatorValue()));
        } else {
            // 没有数据时显示默认值
            tvStepCount.setText("6,243 步");
            
            // 创建并保存默认数据
            saveDefaultHealthIndicator("step_count", 6243);
        }
        
        // 获取最新的睡眠时长数据
        HealthIndicator sleepHours = healthIndicatorDao.getLatestHealthIndicator((int)userId, "sleep_hours");
        if (sleepHours != null) {
            tvSleepHours.setText(String.format(Locale.getDefault(), "%.1f 小时", sleepHours.getIndicatorValue()));
        } else {
            // 没有数据时显示默认值
            tvSleepHours.setText("7.5 小时");
            
            // 创建并保存默认数据
            saveDefaultHealthIndicator("sleep_hours", 7.5f);
        }
    }
    
    /**
     * 保存默认健康指标数据
     */
    private void saveDefaultHealthIndicator(String type, float value) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        HealthIndicator indicator = new HealthIndicator((int)userId, type, value, currentTime);
        healthIndicatorDao.addHealthIndicator(indicator);
    }
} 
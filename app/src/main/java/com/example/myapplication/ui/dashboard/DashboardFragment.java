package com.example.myapplication.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.model.DailyTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 仪表盘Fragment，显示主页概览
 */
public class DashboardFragment extends Fragment {
    private TextView tvTodayTaskCount;
    private TextView tvCompletedTaskCount;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvUserName;
    private TextView tvHeartRate;
    private TextView tvStepCount;
    private TextView tvSleepHours;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initView(view);
        loadData();
        return view;
    }

    private void initView(View view) {
        tvTodayTaskCount = view.findViewById(R.id.tv_today_task_count);
        tvCompletedTaskCount = view.findViewById(R.id.tv_completed_task_count);
        progressBar = view.findViewById(R.id.progress_bar);
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvHeartRate = view.findViewById(R.id.tv_heart_rate);
        tvStepCount = view.findViewById(R.id.tv_step_count);
        tvSleepHours = view.findViewById(R.id.tv_sleep_hours);
    }

    private void loadData() {
        // 这里应该从数据库加载真实数据
        // 目前使用模拟数据进行演示
        
        // 获取当前日期
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        SharedPreferencesUtil spUtil = new SharedPreferencesUtil(requireContext());
        int userId = (int) spUtil.getCurrentUserId();
        String username = spUtil.getCurrentUsername();
        if (!username.isEmpty()) {
            tvUserName.setText(username);
        } else {
            tvUserName.setText(String.valueOf(userId));
        }
        
        // 获取当日任务
        DailyTaskDao taskDao = new DailyTaskDao(getContext());
        List<DailyTask> todayTasks = taskDao.getTodayTasks(userId, today);
        
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
        
        // 设置健康指标（模拟数据）
        tvHeartRate.setText("72 bpm");
        tvStepCount.setText("6,243 步");
        tvSleepHours.setText("7.5 小时");
    }
} 
package com.example.myapplication.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.model.DailyTask;
import com.example.myapplication.ui.task.EducationContentActivity;
import com.example.myapplication.ui.task.SurveyActivity;
import com.example.myapplication.ui.task.PainScoreActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 每日任务中心Fragment
 */
public class TaskFragment extends Fragment implements TaskAdapter.OnTaskActionListener {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<DailyTask> taskList = new ArrayList<>();
    private DailyTaskDao taskDao;
    private SharedPreferencesUtil spUtil;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        
        // 初始化DAO和工具类
        taskDao = new DailyTaskDao(requireContext());
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
        loadData();
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次恢复时重新加载数据，以更新任务状态
        loadData();
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(taskList);
        taskAdapter.setOnTaskActionListener(this);
        recyclerView.setAdapter(taskAdapter);
    }

    private void loadData() {
        // 清空列表
        taskList.clear();
        
        // 获取当前日期
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // 加载真实数据
        List<DailyTask> tasks = taskDao.getTodayTasks((int)userId, today);
        
        // 如果没有数据，添加一些示例数据
        if (tasks.isEmpty()) {
            // 添加教育内容任务
            DailyTask educationTask = new DailyTask((int)userId, "术后康复知识学习", 
                    "了解手术后需要注意的事项和康复知识", "教育内容", today, today);
            educationTask.setStatus("未完成");
            educationTask.setCompletionRate(0.0f);
            long id = taskDao.addDailyTask(educationTask);
            if (id > 0) {
                educationTask.setId((int) id);
                taskList.add(educationTask);
            }
            
            // 添加问卷任务
            DailyTask surveyTask = new DailyTask((int)userId, "术后恢复状况问卷", 
                    "填写术后恢复状况评估问卷", "问卷", today, today);
            surveyTask.setStatus("未完成");
            surveyTask.setCompletionRate(0.0f);
            id = taskDao.addDailyTask(surveyTask);
            if (id > 0) {
                surveyTask.setId((int) id);
                taskList.add(surveyTask);
            }
            
            // 添加疼痛评分任务
            DailyTask painScoreTask = new DailyTask((int)userId, "疼痛评分记录", 
                    "记录今日疼痛程度评分", "疼痛评分", today, today);
            painScoreTask.setStatus("未完成");
            painScoreTask.setCompletionRate(0.0f);
            id = taskDao.addDailyTask(painScoreTask);
            if (id > 0) {
                painScoreTask.setId((int) id);
                taskList.add(painScoreTask);
            }
        } else {
            taskList.addAll(tasks);
        }
        
        // 通知适配器数据已更新
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskClick(DailyTask task) {
        // 打开任务详情页
        Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
        intent.putExtra("task_id", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskComplete(DailyTask task) {
        // 点击完成任务时仅跳转到对应页面，不直接修改状态
        if ("教育内容".equals(task.getTaskType())) {
            Intent intent = new Intent(requireContext(), EducationContentActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        } else if ("问卷".equals(task.getTaskType())) {
            Intent intent = new Intent(requireContext(), SurveyActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        } else if ("疼痛评分".equals(task.getTaskType())) {
            Intent intent = new Intent(requireContext(), PainScoreActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        } else {
            Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
            intent.putExtra("task_id", task.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onTaskSkip(DailyTask task) {
        // 功能已移除，保留空实现以兼容旧接口
    }

    @Override
    public void onTaskDelay(DailyTask task) {
        // 功能已移除，保留空实现以兼容旧接口
    }
}

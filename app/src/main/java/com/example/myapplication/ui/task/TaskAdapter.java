package com.example.myapplication.ui.task;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.DailyTask;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * 任务列表适配器
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<DailyTask> taskList;
    private OnTaskActionListener listener;

    public TaskAdapter(List<DailyTask> taskList) {
        this.taskList = taskList;
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        DailyTask task = taskList.get(position);
        
        holder.tvTaskTitle.setText(task.getTaskName());
        holder.tvTaskDescription.setText(task.getTaskDesc());
        
        // 设置图标
        if ("教育内容".equals(task.getTaskType())) {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_education);
        } else if ("问卷".equals(task.getTaskType())) {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_survey);
        } else if ("疼痛评分".equals(task.getTaskType())) {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_health);
        } else {
            holder.ivTaskIcon.setImageResource(R.drawable.ic_task);
        }
        
        // 设置任务标签
        holder.tvTaskTag.setText(task.getStatus());
        if ("已完成".equals(task.getStatus())) {
            holder.tvTaskTag.setTextColor(Color.parseColor("#4CAF50"));
            holder.btnTaskComplete.setEnabled(false);
            holder.btnTaskComplete.setText("已完成");
        } else if ("已跳过".equals(task.getStatus())) {
            holder.tvTaskTag.setTextColor(Color.parseColor("#FF9800"));
            holder.btnTaskComplete.setEnabled(false);
            holder.btnTaskComplete.setText("已跳过");
        } else if ("已延后".equals(task.getStatus())) {
            holder.tvTaskTag.setTextColor(Color.parseColor("#2196F3"));
            holder.btnTaskComplete.setEnabled(true);
            holder.btnTaskComplete.setText("完成任务");
        } else {
            holder.tvTaskTag.setTextColor(Color.parseColor("#F44336"));
            holder.btnTaskComplete.setEnabled(true);
            holder.btnTaskComplete.setText("完成任务");
        }
        
        // 设置时间
        if (task.getStartTime() != null) {
            holder.tvTaskTime.setText(task.getStartTime());
        } else {
            holder.tvTaskTime.setText("全天");
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
        
        holder.btnTaskComplete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskComplete(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTaskIcon;
        TextView tvTaskTitle;
        TextView tvTaskDescription;
        TextView tvTaskTag;
        TextView tvTaskTime;
        MaterialButton btnTaskComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskTag = itemView.findViewById(R.id.tv_task_tag);
            tvTaskTime = itemView.findViewById(R.id.tv_task_time);
            btnTaskComplete = itemView.findViewById(R.id.btn_task_complete);
        }
    }

    /**
     * 任务操作监听器
     */
    public interface OnTaskActionListener {
        void onTaskClick(DailyTask task);
        void onTaskComplete(DailyTask task);
        void onTaskSkip(DailyTask task);
        void onTaskDelay(DailyTask task);
    }
} 
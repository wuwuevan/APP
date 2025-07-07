package com.example.myapplication.ui.exercise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * 训练列表适配器
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    
    private List<ExerciseFragment.ExerciseItem> exerciseList;
    private OnItemClickListener listener;
    
    /**
     * 构造方法
     * @param exerciseList 训练列表数据
     */
    public ExerciseAdapter(List<ExerciseFragment.ExerciseItem> exerciseList) {
        this.exerciseList = exerciseList;
    }
    
    /**
     * 设置条目点击监听器
     * @param listener 监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseFragment.ExerciseItem item = exerciseList.get(position);
        
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        holder.chipDuration.setText(item.getDuration());
        holder.chipDifficulty.setText(item.getDifficulty());
        
        // 设置点击事件
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(pos);
                    }
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return exerciseList.size();
    }
    
    /**
     * 训练ViewHolder
     */
    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle;
        TextView tvDescription;
        Chip chipDuration;
        Chip chipDifficulty;
        
        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            chipDuration = itemView.findViewById(R.id.chip_duration);
            chipDifficulty = itemView.findViewById(R.id.chip_difficulty);
        }
    }
    
    /**
     * 条目点击监听器接口
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
} 
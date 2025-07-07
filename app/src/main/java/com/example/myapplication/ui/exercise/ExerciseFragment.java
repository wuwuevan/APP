package com.example.myapplication.ui.exercise;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.ExerciseDao;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 训练页面Fragment
 */
public class ExerciseFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private ExtendedFloatingActionButton fabAddExercise;
    private ChipGroup chipGroupFilter;
    private Chip chipAll;
    private Chip chipBeginner;
    private Chip chipIntermediate;
    private Chip chipAdvanced;
    private List<ExerciseItem> exerciseList;
    private static final int REQUEST_ADD_EXERCISE = 100;
    private ExerciseDao exerciseDao;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exercise, container, false);
        
        // 初始化数据访问对象
        exerciseDao = new ExerciseDao(getContext());
        dbHelper = DatabaseHelper.getInstance(getContext());
        
        // 初始化视图
        initViews(root);
        
        // 初始化数据
        initData();
        
        // 设置点击事件
        setClickListeners();
        
        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时刷新数据
        loadExercisesFromDatabase();
    }
    
    /**
     * 初始化视图
     */
    private void initViews(View root) {
        recyclerView = root.findViewById(R.id.recycler_exercise);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddExercise = root.findViewById(R.id.fab_add_exercise);

        chipGroupFilter = root.findViewById(R.id.chip_group_filter);
        chipAll = root.findViewById(R.id.chip_all);
        chipBeginner = root.findViewById(R.id.chip_beginner);
        chipIntermediate = root.findViewById(R.id.chip_intermediate);
        chipAdvanced = root.findViewById(R.id.chip_advanced);

        setupFilterChips();
    }

    /**
     * 设置筛选芯片
     */
    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId == R.id.chip_all || checkedId == View.NO_ID) {
                    loadExercisesFromDatabase();
                } else if (checkedId == R.id.chip_beginner) {
                    filterExercises("初级");
                } else if (checkedId == R.id.chip_intermediate) {
                    filterExercises("中级");
                } else if (checkedId == R.id.chip_advanced) {
                    filterExercises("高级");
                }
            }
        });
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        // 创建示例数据
        exerciseList = new ArrayList<>();
        
        // 添加默认数据
        createDefaultExercises();
        
        // 设置适配器
        adapter = new ExerciseAdapter(exerciseList);
        recyclerView.setAdapter(adapter);
        
        // 设置条目点击事件
        adapter.setOnItemClickListener(new ExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ExerciseItem item = exerciseList.get(position);
                
                // 获取训练步骤和注意事项
                String[] stepsAndNotes = getStepsAndNotes(item.getTitle());
                String steps = stepsAndNotes[0];
                String notes = stepsAndNotes[1];
                
                // 跳转到训练详情页面
                Intent intent = new Intent(getContext(), ExerciseDetailActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("duration", item.getDuration());
                intent.putExtra("calories", item.getCalories());
                intent.putExtra("difficulty", item.getDifficulty());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("image_res_id", item.getImageResId());
                intent.putExtra("steps", steps);
                intent.putExtra("notes", notes);
                startActivity(intent);
            }
        });
        
        // 从数据库加载数据
        loadExercisesFromDatabase();
    }
    
    /**
     * 获取训练步骤和注意事项
     */
    private String[] getStepsAndNotes(String title) {
        String[] result = new String[2];
        result[0] = ""; // 步骤
        result[1] = ""; // 注意事项
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXERCISE,
                new String[]{DatabaseHelper.COLUMN_STEPS, DatabaseHelper.COLUMN_NOTES},
                DatabaseHelper.COLUMN_TITLE + " = ?",
                new String[]{title},
                null,
                null,
                null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            int stepsIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STEPS);
            int notesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES);
            
            result[0] = cursor.getString(stepsIndex);
            result[1] = cursor.getString(notesIndex);
            
            cursor.close();
        }
        
        return result;
    }
    
    /**
     * 从数据库加载训练计划
     */
    private void loadExercisesFromDatabase() {
        // 清空当前列表
        exerciseList.clear();
        
        // 从数据库获取所有训练计划
        List<ExerciseItem> dbExercises = exerciseDao.getAllExercises();
        
        // 添加到列表
        if (dbExercises != null && !dbExercises.isEmpty()) {
            exerciseList.addAll(dbExercises);
        }
        
        // 通知适配器数据已更新
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 根据难度筛选训练计划
     */
    private void filterExercises(String level) {
        exerciseList.clear();
        List<ExerciseItem> all = exerciseDao.getAllExercises();
        for (ExerciseItem item : all) {
            if (item.getDifficulty().equals(level)) {
                exerciseList.add(item);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    
    /**
     * 创建默认训练计划数据
     */
    private void createDefaultExercises() {
        List<ExerciseItem> defaultExercises = new ArrayList<>();
        
        // 常规运动项目
        defaultExercises.add(new ExerciseItem("快走", "适合各年龄段人群的低强度有氧运动", "30分钟", 150, "初级", 
                "快走是一种低强度有氧运动，适合各年龄段人群，特别是初学者和老年人。它能提高心肺功能，增强下肢力量，消耗热量，改善心情。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("慢跑", "中等强度有氧运动，提高心肺功能", "20分钟", 200, "中级", 
                "慢跑是一种中等强度有氧运动，能有效提高心肺功能，增强下肢肌肉力量，促进新陈代谢，帮助控制体重。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("瑜伽", "提高身体柔韧性和平衡能力的运动", "45分钟", 180, "初级", 
                "瑜伽是一种结合了身体姿势、呼吸控制和冥想的运动形式，能提高身体柔韧性、平衡能力和核心力量，同时减轻压力，改善心理健康。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("力量训练", "增强肌肉力量和耐力的训练", "40分钟", 250, "高级",
                "力量训练通过对抗阻力来增强肌肉力量和耐力，提高基础代谢率，塑造体型，预防骨质疏松，改善姿势和平衡能力。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("游泳", "全身性有氧运动，关节负担小", "30分钟", 300, "高级",
                "游泳是一种全身性有氧运动，对关节冲击小，适合各年龄段人群。它能提高心肺功能，增强全身肌肉力量，改善姿势和灵活性。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("骑行", "中高强度有氧运动，锻炼下肢力量", "45分钟", 350, "中级",
                "骑行是一种中高强度有氧运动，能有效锻炼下肢肌肉力量，提高心肺功能，消耗热量，同时对关节冲击较小。", 
                R.drawable.ic_exercise));
        
        // 康复训练项目
        defaultExercises.add(new ExerciseItem("上肢康复训练", "适合上肢功能障碍患者的训练计划", "30分钟", 120, "初级", 
                "上肢康复训练针对肩部、手臂和手部功能障碍设计，通过一系列渐进式练习，恢复上肢力量、灵活性和协调性。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("下肢康复训练", "适合下肢功能障碍患者的训练计划", "45分钟", 150, "中级",
                "下肢康复训练针对髋部、膝盖和脚踝功能障碍设计，通过渐进式练习，恢复下肢力量、灵活性和平衡能力。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("平衡能力训练", "提高身体平衡能力的训练计划", "20分钟", 100, "初级", 
                "平衡能力训练通过一系列静态和动态练习，提高身体稳定性和协调性，预防跌倒，适合老年人和康复期患者。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("手指灵活性训练", "提高手指灵活度和精细动作的训练", "15分钟", 80, "初级", 
                "手指灵活性训练针对手部精细动作障碍设计，通过各种手指练习，提高灵活度、力量和协调性，改善日常生活能力。", 
                R.drawable.ic_exercise));
        
        defaultExercises.add(new ExerciseItem("颈椎康复训练", "缓解颈椎疼痛和改善活动度的训练", "10分钟", 60, "高级",
                "颈椎康复训练通过温和的伸展和强化练习，缓解颈部疼痛，增加活动范围，改善姿势，预防颈椎问题恶化。", 
                R.drawable.ic_exercise));
        
        // 添加默认数据到数据库
        exerciseDao.addDefaultExercises(defaultExercises);
    }
    
    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        fabAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到添加训练计划页面
                Intent intent = new Intent(getContext(), AddExerciseActivity.class);
                startActivityForResult(intent, REQUEST_ADD_EXERCISE);
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_EXERCISE && resultCode == getActivity().RESULT_OK) {
            // 添加训练计划成功后刷新列表
            loadExercisesFromDatabase();
            Toast.makeText(getContext(), "训练计划已添加", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 训练项数据类
     */
    public static class ExerciseItem {
        private String title;
        private String description;
        private String duration;
        private int calories;
        private String difficulty;
        private String detailDescription;
        private int imageResId;
        
        public ExerciseItem(String title, String description, String duration) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.calories = 100;
            this.difficulty = "初级";
            this.detailDescription = description;
            this.imageResId = R.drawable.ic_exercise;
        }
        
        public ExerciseItem(String title, String description, String duration, int calories, 
                           String difficulty, String detailDescription, int imageResId) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.calories = calories;
            this.difficulty = difficulty;
            this.detailDescription = detailDescription;
            this.imageResId = imageResId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getDuration() {
            return duration;
        }
        
        public int getCalories() {
            return calories;
        }
        
        public String getDifficulty() {
            return difficulty;
        }
        
        public String getDetailDescription() {
            return detailDescription;
        }
        
        public int getImageResId() {
            return imageResId;
        }
    }
} 
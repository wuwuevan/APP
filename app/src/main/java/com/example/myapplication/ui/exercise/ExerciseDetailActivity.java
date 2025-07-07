package com.example.myapplication.ui.exercise;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.ExerciseDao;

/**
 * 训练详情页面
 */
public class ExerciseDetailActivity extends AppCompatActivity {

    private ImageView ivExercise;
    private TextView tvTitle;
    private TextView tvDuration;
    private TextView tvCalories;
    private TextView tvDifficulty;
    private TextView tvDescription;
    private TextView tvSteps;
    private TextView tvNotes;
    private Toolbar toolbar;
    private Button btnDelete;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

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

        // 获取传递的数据
        String title = getIntent().getStringExtra("title");
        String duration = getIntent().getStringExtra("duration");
        int calories = getIntent().getIntExtra("calories", 0);
        String difficulty = getIntent().getStringExtra("difficulty");
        String description = getIntent().getStringExtra("description");
        int imageResId = getIntent().getIntExtra("image_res_id", R.drawable.ic_exercise);
        String steps = getIntent().getStringExtra("steps");
        String notes = getIntent().getStringExtra("notes");

        // 加载数据
        loadData(title, duration, calories, difficulty, description, imageResId, steps, notes);

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("删除训练计划")
                .setMessage("是否确认删除？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    ExerciseDao dao = new ExerciseDao(this);
                    dao.deleteExerciseByTitle(title);
                    Toast.makeText(this, "训练计划已删除", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show());
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        ivExercise = findViewById(R.id.iv_exercise);
        tvTitle = findViewById(R.id.tv_title);
        tvDuration = findViewById(R.id.tv_duration);
        tvCalories = findViewById(R.id.tv_calories);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        tvDescription = findViewById(R.id.tv_description);
        tvSteps = findViewById(R.id.tv_steps);
        tvNotes = findViewById(R.id.tv_notes);
        btnDelete = findViewById(R.id.btn_delete);
    }

    /**
     * 加载数据
     */
    private void loadData(String title, String duration, int calories, String difficulty, String description, int imageResId, String steps, String notes) {
        // 设置图片
        ivExercise.setImageResource(imageResId);

        // 设置基本信息
        tvTitle.setText(title);
        tvDuration.setText(duration);
        tvCalories.setText(calories + " 千卡");
        tvDifficulty.setText(difficulty);
        tvDescription.setText(description);

        // 设置训练步骤和注意事项
        if (steps != null && !steps.isEmpty()) {
            tvSteps.setText(steps);
        } else {
            // 根据不同的运动项目设置不同的训练步骤
            setExerciseDetails(title);
        }

        // 设置注意事项
        if (notes != null && !notes.isEmpty()) {
            tvNotes.setText(notes);
        }
    }

    /**
     * 根据运动项目设置详细信息
     */
    private void setExerciseDetails(String title) {
        StringBuilder steps = new StringBuilder();
        StringBuilder notes = new StringBuilder();

        switch (title) {
            case "快走":
                steps.append("1. 挺直腰背，收紧腹部\n");
                steps.append("2. 手臂自然摆动，与步伐协调\n");
                steps.append("3. 脚跟先着地，然后是脚掌\n");
                steps.append("4. 保持中等速度，每分钟100-120步\n");
                steps.append("5. 每次坚持30分钟以上");

                notes.append("1. 选择平坦、宽敞的路面\n");
                notes.append("2. 穿着舒适的运动鞋\n");
                notes.append("3. 避免饭后立即进行\n");
                notes.append("4. 注意天气变化，及时增减衣物");
                break;

            case "慢跑":
                steps.append("1. 保持上身挺直，略微前倾\n");
                steps.append("2. 手臂弯曲约90度，轻松摆动\n");
                steps.append("3. 脚掌中部着地，避免脚跟着地\n");
                steps.append("4. 保持均匀呼吸，步伐稳定\n");
                steps.append("5. 初学者可采用间歇跑法");

                notes.append("1. 热身充分，避免肌肉拉伤\n");
                notes.append("2. 选择专业跑鞋，减少冲击\n");
                notes.append("3. 注意补充水分\n");
                notes.append("4. 出现不适应立即停止");
                break;

            case "瑜伽":
                steps.append("1. 准备瑜伽垫和宽松服装\n");
                steps.append("2. 开始前进行简单热身\n");
                steps.append("3. 按照基础体式顺序练习：山式、战士式、下犬式等\n");
                steps.append("4. 每个动作保持5-10个呼吸\n");
                steps.append("5. 结束时进行放松和冥想");

                notes.append("1. 动作要缓慢平稳，不要急于求成\n");
                notes.append("2. 注意呼吸与动作的协调\n");
                notes.append("3. 有伤病部位需避免相关动作\n");
                notes.append("4. 孕妇、高血压患者需选择专门的体式");
                break;

            case "力量训练":
                steps.append("1. 开始前进行5-10分钟有氧热身\n");
                steps.append("2. 先练大肌群，再练小肌群\n");
                steps.append("3. 每个动作8-12次为一组，做2-3组\n");
                steps.append("4. 组间休息60-90秒\n");
                steps.append("5. 动作要规范，避免借力");

                notes.append("1. 初学者应在专业指导下进行\n");
                notes.append("2. 选择适合自己的重量\n");
                notes.append("3. 训练后注意补充蛋白质\n");
                notes.append("4. 同一肌群需间隔48小时再次训练");
                break;

            case "游泳":
                steps.append("1. 入水前做好热身准备\n");
                steps.append("2. 初学者可从自由泳或蛙泳开始\n");
                steps.append("3. 注意呼吸节奏，避免呛水\n");
                steps.append("4. 动作要协调，不要过度用力\n");
                steps.append("5. 每次游泳时间控制在30-60分钟");

                notes.append("1. 游泳前不要空腹或过饱\n");
                notes.append("2. 选择正规泳池，注意安全\n");
                notes.append("3. 游泳后及时擦干身体，避免着凉\n");
                notes.append("4. 眼睛不适可佩戴泳镜");
                break;

            case "骑行":
                steps.append("1. 调整座椅高度，脚能轻触地面\n");
                steps.append("2. 保持上身放松，略微前倾\n");
                steps.append("3. 脚掌前部用力踩踏\n");
                steps.append("4. 保持均匀呼吸和节奏\n");
                steps.append("5. 初学者先选择平坦路段");

                notes.append("1. 骑行前检查车辆状况\n");
                notes.append("2. 佩戴头盔和防护装备\n");
                notes.append("3. 遵守交通规则，注意安全\n");
                notes.append("4. 长时间骑行注意补充水分和能量");
                break;

            default:
                steps.append("1. 进行适当的热身活动\n");
                steps.append("2. 掌握正确的动作要领\n");
                steps.append("3. 控制适当的运动强度\n");
                steps.append("4. 保持规律的训练频率\n");
                steps.append("5. 运动后进行充分的放松");

                notes.append("1. 选择适合自己的运动方式\n");
                notes.append("2. 运动前做好充分准备\n");
                notes.append("3. 注意安全，避免受伤\n");
                notes.append("4. 坚持长期锻炼，保持健康");
                break;
        }

        tvSteps.setText(steps.toString());
        
        // 如果没有传入注意事项，使用默认的
        if (tvNotes.getText().toString().isEmpty()) {
            tvNotes.setText(notes.toString());
        }
    }
} 
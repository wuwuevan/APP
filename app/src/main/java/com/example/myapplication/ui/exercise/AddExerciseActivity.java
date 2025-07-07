package com.example.myapplication.ui.exercise;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.ExerciseDao;

/**
 * 添加训练计划页面
 */
public class AddExerciseActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private EditText etDuration;
    private EditText etCalories;
    private Spinner spinnerDifficulty;
    private EditText etSteps;
    private EditText etNotes;
    private Button btnSave;
    private Toolbar toolbar;
    private ExerciseDao exerciseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

        // 初始化数据访问对象
        exerciseDao = new ExerciseDao(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 初始化视图
        initViews();

        // 设置点击事件
        setClickListeners();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etDuration = findViewById(R.id.et_duration);
        etCalories = findViewById(R.id.et_calories);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        etSteps = findViewById(R.id.et_steps);
        etNotes = findViewById(R.id.et_notes);
        btnSave = findViewById(R.id.btn_save);

        // 设置难度级别下拉菜单
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }

    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 验证输入
                if (validateInput()) {
                    // 保存训练计划
                    saveExercise();
                    // 关闭页面并返回
                    finish();
                }
            }
        });
    }

    /**
     * 验证输入
     */
    private boolean validateInput() {
        // 验证标题
        if (etTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入训练计划标题", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return false;
        }

        // 验证描述
        if (etDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入训练计划描述", Toast.LENGTH_SHORT).show();
            etDescription.requestFocus();
            return false;
        }

        // 验证时长
        if (etDuration.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入训练时长", Toast.LENGTH_SHORT).show();
            etDuration.requestFocus();
            return false;
        }

        // 验证卡路里
        if (etCalories.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入消耗卡路里", Toast.LENGTH_SHORT).show();
            etCalories.requestFocus();
            return false;
        }

        // 验证训练步骤
        if (etSteps.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入训练步骤", Toast.LENGTH_SHORT).show();
            etSteps.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 保存训练计划
     */
    private void saveExercise() {
        // 获取输入的数据
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String duration = etDuration.getText().toString().trim() + "分钟";
        int calories = Integer.parseInt(etCalories.getText().toString().trim());
        String difficulty = spinnerDifficulty.getSelectedItem().toString();
        String steps = etSteps.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // 保存到数据库
        long id = exerciseDao.addExercise(title, description, duration, calories, difficulty, description, steps, notes);

        if (id > 0) {
            Toast.makeText(this, "训练计划已保存", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 
package com.example.myapplication.ui.health;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.database.HealthIndicatorDao;
import com.example.myapplication.model.HealthIndicator;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InitialHealthMetricsActivity extends AppCompatActivity {

    private EditText etHeight, etWeight, etHeartRate, etSystolic, etDiastolic;
    private EditText etBloodSugar, etSleepHours, etStepCount;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Button btnGenerateReport;
    private HealthIndicatorDao healthIndicatorDao;
    private UserDao userDao;
    private SharedPreferencesUtil prefsUtil;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_health_metrics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("初始健康信息");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        healthIndicatorDao = new HealthIndicatorDao(this);
        prefsUtil = new SharedPreferencesUtil(this);
        userDao = AppDatabase.getInstance(this).userDao();
        userId = (int) prefsUtil.getCurrentUserId();

        initViews();

        btnGenerateReport.setOnClickListener(v -> {
            if (validateInput()) {
                saveHealthData();
                Intent intent = new Intent(InitialHealthMetricsActivity.this, HealthReportActivity.class);
                intent.putExtra("from_onboarding", true); // Flag to indicate onboarding flow
                startActivity(intent);
                finish(); // Finish this activity
            }
        });
    }

    private void initViews() {
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        etHeartRate = findViewById(R.id.et_heart_rate);
        etSystolic = findViewById(R.id.et_systolic);
        etDiastolic = findViewById(R.id.et_diastolic);
        etBloodSugar = findViewById(R.id.et_blood_sugar);
        etSleepHours = findViewById(R.id.et_sleep_hours);
        etStepCount = findViewById(R.id.et_step_count);
        btnGenerateReport = findViewById(R.id.btn_generate_report);
    }

    private boolean validateInput() {
        if (rgGender.getCheckedRadioButtonId() == -1 ||
            TextUtils.isEmpty(etHeight.getText()) || TextUtils.isEmpty(etWeight.getText()) ||
            TextUtils.isEmpty(etHeartRate.getText()) || TextUtils.isEmpty(etSystolic.getText()) ||
            TextUtils.isEmpty(etDiastolic.getText()) || TextUtils.isEmpty(etBloodSugar.getText()) ||
            TextUtils.isEmpty(etSleepHours.getText()) || TextUtils.isEmpty(etStepCount.getText())) {
            Toast.makeText(this, "所有字段均为必填项", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveHealthData() {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String gender = rbMale.isChecked() ? "男" : "女";
        String username = prefsUtil.getString("current_username", "");
        if (!username.isEmpty()) {
            User user = userDao.getUserByUsername(username);
            if (user != null) {
                user.setGender(gender);
                userDao.updateUser(user);
            }
        }

        // For simplicity, we are creating separate HealthIndicator objects for each metric.
        // In a real-world app, you might handle this differently.
        float height = Float.parseFloat(etHeight.getText().toString());
        float weight = Float.parseFloat(etWeight.getText().toString());
        float bmi = calculateBmi(height, weight);
        float bloodSugar = Float.parseFloat(etBloodSugar.getText().toString());
        float sleepHours = Float.parseFloat(etSleepHours.getText().toString());
        int stepCount = Integer.parseInt(etStepCount.getText().toString());

        saveIndicator("身高", height, currentTime);
        saveIndicator("体重", weight, currentTime);
        saveIndicator("BMI", bmi, currentTime);
        saveIndicator("心率", Float.parseFloat(etHeartRate.getText().toString()), currentTime);
        saveIndicator("收缩压", Float.parseFloat(etSystolic.getText().toString()), currentTime);
        saveIndicator("舒张压", Float.parseFloat(etDiastolic.getText().toString()), currentTime);
        saveIndicator("血糖", bloodSugar, currentTime);
        saveIndicator("睡眠时长", sleepHours, currentTime);
        saveStepCountIndicator(stepCount, currentTime);
        
        Toast.makeText(this, "健康数据已保存", Toast.LENGTH_SHORT).show();
    }
    
    private void saveIndicator(String type, float value, String time) {
        HealthIndicator indicator = new HealthIndicator(userId, type, value, time);
        healthIndicatorDao.addHealthIndicator(indicator);
    }

    private void saveStepCountIndicator(int stepCount, String time) {
        HealthIndicator indicator = new HealthIndicator(userId, "步数", stepCount, time);
        if (stepCount < 2000 || stepCount > 30000) {
            indicator.setIsAbnormal(1);
        }
        healthIndicatorDao.addHealthIndicator(indicator);
    }

    private float calculateBmi(float heightCm, float weightKg) {
        if (heightCm <= 0) return 0;
        float heightM = heightCm / 100;
        return weightKg / (heightM * heightM);
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
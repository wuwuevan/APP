package com.example.myapplication.ui.health;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.HealthIndicatorDao;
import com.example.myapplication.model.CustomHealthIndicator;
import com.example.myapplication.model.HealthIndicator;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 健康报告详情页面
 */
public class HealthReportActivity extends AppCompatActivity {

    private TextView tvBMI, tvBMIStatus;
    private TextView tvHeartRate, tvHeartRateStatus;
    private TextView tvBloodPressure, tvBloodPressureStatus;
    private TextView tvBloodSugar, tvBloodSugarStatus;
    private TextView tvSleepQuality, tvSleepQualityStatus;
    private TextView tvSuggestion;
    private Button btnUpdateSuggestion;
    private CardView cvCustomIndicators;
    private LinearLayout containerCustomIndicatorsReport;
    private Button btnContinueToHome;
    private Toolbar toolbar;
    
    private HealthIndicatorDao healthIndicatorDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);

        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        
        // 设置工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        
        healthIndicatorDao = new HealthIndicatorDao(this);
        
        SharedPreferencesUtil prefsUtil = new SharedPreferencesUtil(this);
        userId = (int) prefsUtil.getCurrentUserId();

        initViews();
        setupClickListeners();
        loadDataFromDatabase();
    }
    
    private void setupClickListeners() {
        btnUpdateSuggestion.setOnClickListener(v -> showUpdateSuggestionDialog());
    }
    
    private void showUpdateSuggestionDialog() {
        final EditText editText = new EditText(this);
        editText.setMinLines(5);
        editText.setText(tvSuggestion.getText());
        
        new AlertDialog.Builder(this)
               .setTitle("更新健康建议")
               .setView(editText)
               .setPositiveButton("保存", (dialog, which) -> {
                   String newSuggestion = editText.getText().toString().trim();
                   if (!newSuggestion.isEmpty()) {
                       tvSuggestion.setText(newSuggestion);
                       saveHealthSuggestion(newSuggestion);
                       Toast.makeText(HealthReportActivity.this, "健康建议已更新", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("取消", null)
               .show();
    }
    
    private void saveHealthSuggestion(String suggestion) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        HealthIndicator indicator = new HealthIndicator(userId, "health_suggestion", 0, currentTime);
        long id = healthIndicatorDao.addHealthIndicator(indicator);
        
        if (id > 0) {
            CustomHealthIndicator customIndicator = new CustomHealthIndicator();
            customIndicator.setIndicatorId((int) id);
            customIndicator.setUnit("");
            customIndicator.setNormalMinValue(0);
            customIndicator.setNormalMaxValue(0);
            customIndicator.setNotes(suggestion);
            healthIndicatorDao.addCustomHealthIndicatorInfo(customIndicator);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_health_report, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_update_report) {
            showUpdateSuggestionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        tvBMI = findViewById(R.id.tv_bmi_value);
        tvBMIStatus = findViewById(R.id.tv_bmi_status);
        tvHeartRate = findViewById(R.id.tv_heart_rate_value);
        tvHeartRateStatus = findViewById(R.id.tv_heart_rate_status);
        tvBloodPressure = findViewById(R.id.tv_blood_pressure_value);
        tvBloodPressureStatus = findViewById(R.id.tv_blood_pressure_status);
        tvBloodSugar = findViewById(R.id.tv_blood_sugar_value);
        tvBloodSugarStatus = findViewById(R.id.tv_blood_sugar_status);
        tvSleepQuality = findViewById(R.id.tv_sleep_quality_value);
        tvSleepQualityStatus = findViewById(R.id.tv_sleep_quality_status);
        tvSuggestion = findViewById(R.id.tv_health_suggestion);
        btnUpdateSuggestion = findViewById(R.id.btn_update_suggestion);
        cvCustomIndicators = findViewById(R.id.cv_custom_indicators);
        containerCustomIndicatorsReport = findViewById(R.id.container_custom_indicators_report);
        btnContinueToHome = findViewById(R.id.btn_continue_to_home);
    }

    private void loadDataFromDatabase() {
        // Load standard indicators
        HealthIndicator bmiIndicator = healthIndicatorDao.getLatestHealthIndicator(userId, "BMI");
        if (bmiIndicator != null) {
            float bmi = bmiIndicator.getIndicatorValue();
            tvBMI.setText(String.format(Locale.getDefault(), "%.1f", bmi));
            updateStatus(tvBMIStatus, bmi, 18.5f, 24f);
        }

        HealthIndicator heartRateIndicator = healthIndicatorDao.getLatestHealthIndicator(userId, "心率");
        if (heartRateIndicator != null) {
            int heartRate = (int) heartRateIndicator.getIndicatorValue();
            tvHeartRate.setText(String.valueOf(heartRate));
            updateStatus(tvHeartRateStatus, heartRate, 60, 100);
        }

        HealthIndicator systolicIndicator = healthIndicatorDao.getLatestHealthIndicator(userId, "收缩压");
        HealthIndicator diastolicIndicator = healthIndicatorDao.getLatestHealthIndicator(userId, "舒张压");
        if (systolicIndicator != null && diastolicIndicator != null) {
            int systolic = (int) systolicIndicator.getIndicatorValue();
            int diastolic = (int) diastolicIndicator.getIndicatorValue();
            tvBloodPressure.setText(systolic + "/" + diastolic);
            if (systolic > 140 || diastolic > 90 || systolic < 90 || diastolic < 60) {
                tvBloodPressureStatus.setText("异常");
                tvBloodPressureStatus.setTextColor(getResources().getColor(R.color.colorWarning));
            } else {
                tvBloodPressureStatus.setText("正常");
                tvBloodPressureStatus.setTextColor(getResources().getColor(R.color.colorSuccess));
            }
        }

        HealthIndicator bloodSugarIndicator = healthIndicatorDao.getLatestHealthIndicator(userId, "血糖");
        if (bloodSugarIndicator != null) {
            float bs = bloodSugarIndicator.getIndicatorValue();
            tvBloodSugar.setText(String.format(Locale.getDefault(), "%.1f", bs));
            updateStatus(tvBloodSugarStatus, bs, 3.9f, 6.1f);
        }

        HealthIndicator sleepIndicator = healthIndicatorDao.getLatestHealthIndicator(userId, "睡眠时长");
        if (sleepIndicator != null) {
            float hours = sleepIndicator.getIndicatorValue();
            tvSleepQuality.setText(String.format(Locale.getDefault(), "%.1f", hours));
            updateStatus(tvSleepQualityStatus, hours, 6f, 9f);
        }

        // Load and display custom indicators
        List<HealthIndicator> customIndicators = healthIndicatorDao.getUserCustomHealthIndicators(userId);
        containerCustomIndicatorsReport.removeAllViews(); // Clear previous views
        if (customIndicators != null && !customIndicators.isEmpty()) {
            cvCustomIndicators.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (HealthIndicator indicator : customIndicators) {
                // We shouldn't display the suggestion as a normal indicator
                if (indicator.getIndicatorType().equals("health_suggestion")) {
                    continue;
                }
                View itemView = inflater.inflate(R.layout.item_custom_indicator, containerCustomIndicatorsReport, false);
                
                TextView tvName = itemView.findViewById(R.id.tv_indicator_name);
                TextView tvValue = itemView.findViewById(R.id.tv_indicator_value);
                TextView tvUnit = itemView.findViewById(R.id.tv_indicator_unit);
                TextView tvStatus = itemView.findViewById(R.id.tv_indicator_status);
                TextView tvRange = itemView.findViewById(R.id.tv_indicator_range);
                TextView tvTime = itemView.findViewById(R.id.tv_record_time);
                
                tvName.setText(indicator.getIndicatorType());
                tvValue.setText(String.format(Locale.getDefault(), "%.1f", indicator.getIndicatorValue()));
                
                CustomHealthIndicator customInfo = healthIndicatorDao.getCustomHealthIndicatorInfo(indicator.getId());
                if (customInfo != null) {
                    tvUnit.setText(customInfo.getUnit());
                    float value = indicator.getIndicatorValue();
                    float min = customInfo.getNormalMinValue();
                    float max = customInfo.getNormalMaxValue();
                    
                    if (max > min) { // Check if a range is set
                        tvRange.setText(String.format(Locale.getDefault(), "正常范围: %.1f - %.1f", min, max));
                        updateStatus(tvStatus, value, min, max);
                    } else {
                        tvRange.setVisibility(View.GONE);
                        tvStatus.setVisibility(View.GONE);
                    }
                } else {
                     tvUnit.setVisibility(View.GONE);
                     tvRange.setVisibility(View.GONE);
                     tvStatus.setVisibility(View.GONE);
                }

                tvTime.setText(indicator.getRecordTime().split(" ")[0]); // Show only date
                containerCustomIndicatorsReport.addView(itemView);
            }
        } else {
            cvCustomIndicators.setVisibility(View.GONE);
        }

        // Handle onboarding flow
        boolean fromOnboarding = getIntent().getBooleanExtra("from_onboarding", false);
        if (fromOnboarding) {
            btnContinueToHome.setVisibility(View.VISIBLE);
            btnContinueToHome.setOnClickListener(v -> {
                Intent intent = new Intent(HealthReportActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            // Disable back button during onboarding
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        } else {
            btnContinueToHome.setVisibility(View.GONE);
        }
    }
    
    private void updateStatus(TextView statusView, float value, float min, float max) {
        if (value < min || value > max) {
            statusView.setText("异常");
            statusView.setTextColor(getResources().getColor(R.color.colorWarning));
        } else {
            statusView.setText("正常");
            statusView.setTextColor(getResources().getColor(R.color.colorSuccess));
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back during the onboarding process
        if (getIntent().getBooleanExtra("from_onboarding", false)) {
            // Optionally, show a toast or do nothing
            Toast.makeText(this, "请先完成健康报告查阅", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Prevent navigating up during the onboarding process
        if (getIntent().getBooleanExtra("from_onboarding", false)) {
            return false;
        }
        onBackPressed();
        return true;
    }
} 
package com.example.myapplication.ui.health;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.database.HealthIndicatorDao;
import com.example.myapplication.model.CustomHealthIndicator;
import com.example.myapplication.model.HealthIndicator;
import com.example.myapplication.ui.BaseActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddHealthIndicatorActivity extends BaseActivity {

    public static final String EXTRA_INDICATOR_ID = "indicator_id";

    private EditText etIndicatorName, etIndicatorValue, etIndicatorUnit;
    private EditText etNormalMin, etNormalMax, etIndicatorNotes;
    private Button btnSaveIndicator;
    private HealthIndicatorDao healthIndicatorDao;
    private int userId;

    private boolean isEditMode = false;
    private int indicatorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_health_indicator);

        // 检查是否为编辑模式
        if (getIntent() != null && getIntent().hasExtra(EXTRA_INDICATOR_ID)) {
            indicatorId = getIntent().getIntExtra(EXTRA_INDICATOR_ID, -1);
            isEditMode = indicatorId != -1;
        }

        // 设置标题和返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "编辑健康指标" : "添加健康指标");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化DAO
        healthIndicatorDao = new HealthIndicatorDao(this);
        
        // 获取当前用户ID
        SharedPreferencesUtil prefsUtil = new SharedPreferencesUtil(this);
        userId = (int) prefsUtil.getCurrentUserId();

        // 初始化视图
        initViews();

        // 如果是编辑模式，加载数据
        if (isEditMode) {
            loadIndicatorData(indicatorId);
        }

        // 设置监听器
        setListeners();
    }

    private void initViews() {
        etIndicatorName = findViewById(R.id.et_indicator_name);
        etIndicatorValue = findViewById(R.id.et_indicator_value);
        etIndicatorUnit = findViewById(R.id.et_indicator_unit);
        etNormalMin = findViewById(R.id.et_normal_min);
        etNormalMax = findViewById(R.id.et_normal_max);
        etIndicatorNotes = findViewById(R.id.et_indicator_notes);
        btnSaveIndicator = findViewById(R.id.btn_save_indicator);
    }

    private void setListeners() {
        btnSaveIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndicator();
            }
        });
    }

    private void loadIndicatorData(int id) {
        HealthIndicator indicator = healthIndicatorDao.getHealthIndicatorById(id);
        if (indicator != null) {
            etIndicatorName.setText(indicator.getIndicatorType());
            etIndicatorValue.setText(String.valueOf(indicator.getIndicatorValue()));

            CustomHealthIndicator custom = healthIndicatorDao.getCustomHealthIndicatorInfo(id);
            if (custom != null) {
                etIndicatorUnit.setText(custom.getUnit());
                if (custom.getNormalMinValue() != 0) {
                    etNormalMin.setText(String.valueOf(custom.getNormalMinValue()));
                }
                if (custom.getNormalMaxValue() != 0) {
                    etNormalMax.setText(String.valueOf(custom.getNormalMaxValue()));
                }
                etIndicatorNotes.setText(custom.getNotes());
            }
        }
    }

    private void saveIndicator() {
        // 获取输入值
        String name = etIndicatorName.getText().toString().trim();
        String valueStr = etIndicatorValue.getText().toString().trim();
        String unit = etIndicatorUnit.getText().toString().trim();
        String minStr = etNormalMin.getText().toString().trim();
        String maxStr = etNormalMax.getText().toString().trim();
        String notes = etIndicatorNotes.getText().toString().trim();

        // 验证必填字段
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入指标名称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(valueStr)) {
            Toast.makeText(this, "请输入指标数值", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 解析数值
            float value = Float.parseFloat(valueStr);
            
            // 创建健康指标对象
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            if (isEditMode) {
                HealthIndicator indicator = new HealthIndicator(userId, name, value, currentTime);
                indicator.setId(indicatorId);
                boolean updateSuccess = healthIndicatorDao.updateHealthIndicator(indicator);

                float min = TextUtils.isEmpty(minStr) ? 0 : Float.parseFloat(minStr);
                float max = TextUtils.isEmpty(maxStr) ? 0 : Float.parseFloat(maxStr);
                CustomHealthIndicator custom = new CustomHealthIndicator();
                custom.setIndicatorId(indicatorId);
                custom.setUnit(unit);
                custom.setNormalMinValue(min);
                custom.setNormalMaxValue(max);
                custom.setNotes(notes);
                healthIndicatorDao.updateCustomHealthIndicatorInfo(custom);

                if (updateSuccess) {
                    Toast.makeText(this, "健康指标已更新", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                HealthIndicator indicator = new HealthIndicator(userId, name, value, currentTime);
                long id = healthIndicatorDao.addHealthIndicator(indicator);

                if (id > 0) {
                    float min = TextUtils.isEmpty(minStr) ? 0 : Float.parseFloat(minStr);
                    float max = TextUtils.isEmpty(maxStr) ? 0 : Float.parseFloat(maxStr);

                    CustomHealthIndicator customIndicator = new CustomHealthIndicator();
                    customIndicator.setIndicatorId((int) id);
                    customIndicator.setUnit(unit);
                    customIndicator.setNormalMinValue(min);
                    customIndicator.setNormalMaxValue(max);
                    customIndicator.setNotes(notes);

                    boolean success = healthIndicatorDao.addCustomHealthIndicatorInfo(customIndicator);

                    if (success) {
                        Toast.makeText(this, "健康指标添加成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "保存自定义指标信息失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "保存健康指标失败", Toast.LENGTH_SHORT).show();
                }
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数值", Toast.LENGTH_SHORT).show();
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
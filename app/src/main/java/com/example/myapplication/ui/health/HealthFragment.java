package com.example.myapplication.ui.health;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.database.HealthIndicatorDao;
import com.example.myapplication.model.CustomHealthIndicator;
import com.example.myapplication.model.HealthIndicator;
import com.example.myapplication.utils.MyMarkerView;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.util.List;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * 健康页面Fragment
 */
public class HealthFragment extends Fragment {

    private TextView tvHeartRate;
    private TextView tvBloodPressure;
    private TextView tvBloodSugar;
    private TextView tvHealthReport;
    private Button btnUpdateHealthData;
    private Button btnViewReport;
    private Button btnAddCustomIndicator;
    private LinearLayout containerCustomIndicators;
    private TextView tvNoIndicators;
    private LineChart healthChart;
    
    // 当前健康数据
    private int heartRate;
    private int systolic;
    private int diastolic;
    private float bmi = 22.5f; // 默认BMI值
    private float bloodSugar = 5.2f; // 默认血糖值
    private int sleepHours = 7; // 默认睡眠时间
    
    private HealthIndicatorDao healthIndicatorDao;
    private int userId;
    
    // 自定义指标请求码
    private static final int REQUEST_ADD_INDICATOR = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_health, container, false);
        
        // 初始化DAO
        healthIndicatorDao = new HealthIndicatorDao(getContext());
        
        // 获取用户ID
        SharedPreferencesUtil prefsUtil = new SharedPreferencesUtil(getContext());
        userId = (int) prefsUtil.getCurrentUserId();
        
        // 初始化视图
        initViews(root);
        
        // 为演示目的，生成初始的连续数据
        generateInitialDataIfNeeded();
        
        // 初始化图表
        setupHealthTrendChart();
        
        // 设置点击事件
        setClickListeners();
        
        // 加载初始数据
        updateHealthData();
        loadCustomHealthIndicators();
        loadChartData();
        
        return root;
    }
    
    /**
     * 初始化视图
     */
    private void initViews(View root) {
        tvHeartRate = root.findViewById(R.id.tv_heart_rate);
        tvBloodPressure = root.findViewById(R.id.tv_blood_pressure);
        tvBloodSugar = root.findViewById(R.id.tv_blood_sugar);
        tvHealthReport = root.findViewById(R.id.tv_health_report);
        btnUpdateHealthData = root.findViewById(R.id.btn_update_health_data);
        btnViewReport = root.findViewById(R.id.btn_view_report);
        btnAddCustomIndicator = root.findViewById(R.id.btn_add_custom_indicator);
        containerCustomIndicators = root.findViewById(R.id.container_custom_indicators);
        tvNoIndicators = root.findViewById(R.id.tv_no_indicators);
        healthChart = root.findViewById(R.id.chart_health_trend);
    }
    
    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        btnUpdateHealthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), InitialHealthMetricsActivity.class);
                startActivity(intent);
            }
        });
        
        btnViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到健康报告详情页面
                Intent intent = new Intent(getContext(), HealthReportActivity.class);
                startActivity(intent);
            }
        });
        
        btnAddCustomIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到添加自定义健康指标页面
                Intent intent = new Intent(getContext(), AddHealthIndicatorActivity.class);
                startActivityForResult(intent, REQUEST_ADD_INDICATOR);
            }
        });
    }
    
    /**
     * 更新健康数据
     */
    private void updateHealthData() {
        HealthIndicator hr = healthIndicatorDao.getLatestHealthIndicator(userId, "心率");
        HealthIndicator sys = healthIndicatorDao.getLatestHealthIndicator(userId, "收缩压");
        HealthIndicator dia = healthIndicatorDao.getLatestHealthIndicator(userId, "舒张压");
        HealthIndicator sugar = healthIndicatorDao.getLatestHealthIndicator(userId, "血糖");

        if (hr != null) {
            heartRate = (int) hr.getIndicatorValue();
        }
        if (sys != null) {
            systolic = (int) sys.getIndicatorValue();
        }
        if (dia != null) {
            diastolic = (int) dia.getIndicatorValue();
        }
        if (sugar != null) {
            bloodSugar = sugar.getIndicatorValue();
        }

        tvHeartRate.setText(String.valueOf(heartRate));
        tvBloodPressure.setText(systolic + "/" + diastolic);
        tvBloodSugar.setText(String.format(Locale.getDefault(), "%.1f", bloodSugar));

        updateHealthReport(heartRate, systolic, diastolic, bloodSugar);
    }
    
    /**
     * 更新健康报告
     */
    private void updateHealthReport(int heartRate, int systolic, int diastolic, float bloodSugar) {
        StringBuilder report = new StringBuilder();
        
        // 添加时间戳
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        report.append("检测时间：").append(currentTime).append("\n\n");
        
        // 心率评估
        report.append("心率：");
        if (heartRate < 60) {
            report.append("偏低，可能是休息状态或运动员的正常表现。");
        } else if (heartRate <= 100) {
            report.append("正常范围内，继续保持健康的生活方式。");
        } else {
            report.append("偏高，建议减少咖啡因摄入，避免剧烈运动后立即测量。");
        }
        report.append("\n\n");
        
        // 血压评估
        report.append("血压：");
        if (systolic < 90 || diastolic < 60) {
            report.append("偏低，注意补充水分并适当休息。");
        } else if (systolic < 120 && diastolic < 80) {
            report.append("理想血压，继续保持健康的生活方式。");
        } else if (systolic < 130 && diastolic < 85) {
            report.append("正常血压，定期监测。");
        } else if (systolic < 140 && diastolic < 90) {
            report.append("正常偏高，建议减少盐分摄入，增加运动。");
        } else {
            report.append("偏高，建议咨询医生并定期监测。");
        }
        report.append("\n\n");
        
        // 血糖评估
        report.append("血糖：");
        if (bloodSugar >= 3.9f && bloodSugar <= 6.1f) {
            report.append("正常范围内，继续保持健康的生活方式。");
        } else if (bloodSugar < 3.9f) {
            report.append("偏低，注意补充能量。");
        } else {
            report.append("偏高，建议控制饮食并咨询医生。");
        }
        
        // 更新UI
        tvHealthReport.setText(report.toString());
    }
    
    /**
     * 加载自定义健康指标
     */
    private void loadCustomHealthIndicators() {
        // 清空容器中的旧数据（保留"暂无指标"的TextView）
        if (containerCustomIndicators.getChildCount() > 1) {
            containerCustomIndicators.removeViews(1, containerCustomIndicators.getChildCount() - 1);
        }
        
        // 打印用户ID，确认是否有效
        Log.d("HealthFragment", "当前用户ID: " + userId);
        
        if (userId <= 0) {
            Log.e("HealthFragment", "用户ID无效，无法加载自定义指标");
            tvNoIndicators.setText("请先登录后再添加自定义指标");
            tvNoIndicators.setVisibility(View.VISIBLE);
            return;
        }
        
        // 从数据库获取自定义健康指标
        List<HealthIndicator> customIndicators = healthIndicatorDao.getUserCustomHealthIndicators(userId);
        
        // 添加日志
        Log.d("HealthFragment", "获取到自定义指标数量: " + customIndicators.size() + ", 用户ID: " + userId);
        
        // 打印每个指标的详细信息
        for (HealthIndicator indicator : customIndicators) {
            Log.d("HealthFragment", "指标详情 - ID: " + indicator.getId() + 
                                  ", 类型: " + indicator.getIndicatorType() + 
                                  ", 值: " + indicator.getIndicatorValue() +
                                  ", 记录时间: " + indicator.getRecordTime());
        }
        
        if (customIndicators.size() > 0) {
            // 隐藏"暂无指标"的提示
            tvNoIndicators.setVisibility(View.GONE);
            
            // 循环添加自定义指标
            for (HealthIndicator indicator : customIndicators) {
                View itemView = getLayoutInflater().inflate(R.layout.item_custom_indicator, containerCustomIndicators, false);
                
                // 查找视图
                TextView tvName = itemView.findViewById(R.id.tv_indicator_name);
                TextView tvValue = itemView.findViewById(R.id.tv_indicator_value);
                TextView tvUnit = itemView.findViewById(R.id.tv_indicator_unit);
                TextView tvStatus = itemView.findViewById(R.id.tv_indicator_status);
                TextView tvRange = itemView.findViewById(R.id.tv_indicator_range);
                TextView tvTime = itemView.findViewById(R.id.tv_record_time);
                
                // 获取自定义信息
                CustomHealthIndicator customInfo = healthIndicatorDao.getCustomHealthIndicatorInfo(indicator.getId());
                
                // 添加日志
                Log.d("HealthFragment", "指标ID: " + indicator.getId() + 
                                      ", 类型: " + indicator.getIndicatorType() + 
                                      ", 值: " + indicator.getIndicatorValue() + 
                                      ", 有自定义信息: " + (customInfo != null));
                
                // 设置数据
                tvName.setText(indicator.getIndicatorType());
                tvValue.setText(String.format(Locale.getDefault(), "%.1f", indicator.getIndicatorValue()));
                
                // 如果有自定义信息，填充额外信息
                if (customInfo != null) {
                    tvUnit.setText(customInfo.getUnit());
                    
                    float value = indicator.getIndicatorValue();
                    float minValue = customInfo.getNormalMinValue();
                    float maxValue = customInfo.getNormalMaxValue();
                    
                    if (minValue > 0 || maxValue > 0) {
                        tvRange.setText(String.format(Locale.getDefault(), "正常范围: %.1f - %.1f", 
                                minValue, maxValue));
                                
                        // 设置状态
                        if (value < minValue || value > maxValue) {
                            tvStatus.setText("异常");
                            tvStatus.setTextColor(getResources().getColor(R.color.colorWarning));
                        } else {
                            tvStatus.setText("正常");
                            tvStatus.setTextColor(getResources().getColor(R.color.colorSuccess));
                        }
                    } else {
                        tvRange.setVisibility(View.GONE);
                        tvStatus.setVisibility(View.GONE);
                    }
                } else {
                    tvUnit.setText("");
                    tvRange.setVisibility(View.GONE);
                    tvStatus.setVisibility(View.GONE);
                }
                
                // 设置记录时间
                String recordTime = indicator.getRecordTime();
                if (recordTime != null && !recordTime.isEmpty()) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date date = inputFormat.parse(recordTime);
                        tvTime.setText(outputFormat.format(date));
                    } catch (Exception e) {
                        tvTime.setText(recordTime);
                    }
                } else {
                    tvTime.setVisibility(View.GONE);
                }
                
                // 添加到容器
                containerCustomIndicators.addView(itemView);
            }
        } else {
            // 显示"暂无指标"的提示
            tvNoIndicators.setVisibility(View.VISIBLE);
        }
    }

    private void setupHealthTrendChart() {
        healthChart.getDescription().setEnabled(false);
        healthChart.setDrawGridBackground(false);
        healthChart.getAxisRight().setEnabled(false);
        healthChart.setTouchEnabled(true);
        healthChart.setDragEnabled(true);
        healthChart.setScaleEnabled(true);
        healthChart.setPinchZoom(true);
        healthChart.setNoDataText("暂无健康数据趋势");
        healthChart.setNoDataTextColor(getResources().getColor(R.color.colorTextSecondary));

        // 设置 Marker
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(healthChart);
        healthChart.setMarker(mv);

        XAxis xAxis = healthChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.colorTextSecondary));
        xAxis.setAxisLineColor(getResources().getColor(R.color.colorDivider));
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });
    }

    private void loadChartData() {
        List<HealthIndicator> heartRateData = healthIndicatorDao.getUserHealthIndicatorsByType(userId, "心率");

        // 数据需要按时间升序排列
        Collections.reverse(heartRateData);
        
        if (heartRateData.isEmpty()) {
            healthChart.clear();
            healthChart.invalidate();
            return;
        }

        ArrayList<Entry> values = new ArrayList<>();
        for (HealthIndicator data : heartRateData) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(data.getRecordTime());
                if (date != null) {
                    values.add(new Entry(date.getTime(), data.getIndicatorValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (values.isEmpty()) {
            healthChart.clear();
            healthChart.invalidate();
            return;
        }

        LineDataSet set1;

        if (healthChart.getData() != null &&
                healthChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) healthChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            healthChart.getData().notifyDataChanged();
            healthChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "心率 (BPM)");
            set1.setColor(getResources().getColor(R.color.colorPrimary));
            set1.setCircleColor(getResources().getColor(R.color.colorPrimary));
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(0f); // 不在点上显示数值
            set1.setDrawFilled(false); // 不再填充线下方的区域，让线条更清晰
            set1.setMode(LineDataSet.Mode.LINEAR); // 设置为线性模式，让线条穿过数据点

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData lineData = new LineData(dataSets);
            healthChart.setData(lineData);
        }
        
        healthChart.animateX(1500); // 添加X轴动画
        healthChart.invalidate(); // refresh
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时更新健康数据
        updateHealthData();
        // 加载自定义健康指标
        loadCustomHealthIndicators();
        // 加载图表数据
        loadChartData();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("HealthFragment", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode == REQUEST_ADD_INDICATOR && resultCode == Activity.RESULT_OK) {
            // 自定义健康指标添加成功，更新UI
            Toast.makeText(getContext(), "自定义健康指标添加成功", Toast.LENGTH_SHORT).show();
            
            // 加载自定义健康指标
            loadCustomHealthIndicators();
            
            // 强制刷新UI
            if (containerCustomIndicators != null) {
                containerCustomIndicators.invalidate();
            }
        }
    }

    /**
     * 如果数据库没有数据，则生成一些用于演示的初始历史数据
     */
    private void generateInitialDataIfNeeded() {
        List<HealthIndicator> existingData = healthIndicatorDao.getUserHealthIndicatorsByType(userId, "心率");
        if (existingData.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // 为过去5天生成数据，减少数据点密度
            for (int i = 4; i >= 0; i--) {
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_YEAR, -i);
                
                // 设置为每天上午10点
                calendar.set(Calendar.HOUR_OF_DAY, 10);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                // 生成一个清晰的、略微上升的趋势
                int fakeHeartRate = 72 + (4 - i); // 产生 72, 73, 74, 75, 76 的序列
                String fakeTimestamp = sdf.format(calendar.getTime());
                healthIndicatorDao.addHealthIndicator(new HealthIndicator(userId, "心率", fakeHeartRate, fakeTimestamp));
            }
        }
    }
} 
package com.example.myapplication.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.database.NotificationDao;
import com.example.myapplication.model.Notification;
import com.example.myapplication.ui.notification.NotificationDetailActivity;
import com.example.myapplication.ui.BaseActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity {

    private ListView lvNotifications;
    private Spinner spNotificationType;
    private TextView tvEmptyNotification;
    private Toolbar toolbar;
    
    private NotificationDao notificationDao;
    private SharedPreferencesUtil spUtil;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        
        // 初始化工具栏
        toolbar = findViewById(R.id.toolbar);
        
        // 设置工具栏
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        
        // 初始化工具类
        notificationDao = new NotificationDao(this);
        spUtil = new SharedPreferencesUtil(this);
        
        // 获取当前用户ID
        String username = spUtil.getString("current_username", "");
        currentUserId = spUtil.getInt("current_user_id", -1);
        
        // 初始化视图
        initViews();
        
        // 设置通知类型筛选器
        setupNotificationTypeFilter();
        
        // 加载通知数据
        loadNotifications(null);
        
        // 将所有通知标记为已读
        notificationDao.markAllNotificationsAsRead(currentUserId);
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        lvNotifications = findViewById(R.id.lv_notifications);
        spNotificationType = findViewById(R.id.sp_notification_type);
        tvEmptyNotification = findViewById(R.id.tv_empty_notification);
        
        // 初始化通知列表
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        lvNotifications.setAdapter(adapter);
        
        // 设置点击事件
        lvNotifications.setOnItemClickListener((parent, view, position, id) -> {
            // 处理通知点击事件，可以跳转到通知详情页面
            Notification notification = notificationList.get(position);
            Intent intent = new Intent(NotificationActivity.this, NotificationDetailActivity.class);
            intent.putExtra("notification", notification);
            startActivity(intent);
        });
    }
    
    /**
     * 设置通知类型筛选器
     */
    private void setupNotificationTypeFilter() {
        // 通知类型选项
        String[] notificationTypes = {"全部", "任务提醒", "异常预警", "随访通知"};
        
        // 设置下拉框适配器
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, notificationTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNotificationType.setAdapter(typeAdapter);
        
        // 设置选择监听器
        spNotificationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = null;
                switch (position) {
                    case 0: // 全部
                        selectedType = null;
                        break;
                    case 1: // 任务提醒
                        selectedType = "TASK";
                        break;
                    case 2: // 异常预警
                        selectedType = "ABNORMAL";
                        break;
                    case 3: // 随访通知
                        selectedType = "FOLLOW_UP";
                        break;
                }
                
                // 加载选定类型的通知
                loadNotifications(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不做任何操作
            }
        });
    }
    
    /**
     * 加载通知数据
     * @param notificationType 通知类型，null表示加载所有类型
     */
    private void loadNotifications(String notificationType) {
        // 清空当前列表
        notificationList.clear();
        
        // 根据类型加载通知
        if (notificationType == null) {
            notificationList.addAll(notificationDao.getNotificationsByUserId(currentUserId));
        } else {
            notificationList.addAll(notificationDao.getNotificationsByType(currentUserId, notificationType));
        }
        
        // 更新适配器
        adapter.notifyDataSetChanged();
        
        // 显示空视图或列表
        if (notificationList.isEmpty()) {
            tvEmptyNotification.setVisibility(View.VISIBLE);
            lvNotifications.setVisibility(View.GONE);
        } else {
            tvEmptyNotification.setVisibility(View.GONE);
            lvNotifications.setVisibility(View.VISIBLE);
        }
    }

    private void filterNotifications(String type) {
        // Implementation of filterNotifications method
    }
} 
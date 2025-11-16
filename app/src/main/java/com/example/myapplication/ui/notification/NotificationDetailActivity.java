package com.example.myapplication.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.model.Notification;
import com.example.myapplication.ui.BaseActivity;

public class NotificationDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("通知详情");
        }

        // 获取传递的通知对象
        Intent intent = getIntent();
        Notification notification = (Notification) intent.getSerializableExtra("notification");

        if (notification != null) {
            // 设置数据到视图
            TextView tvTitle = findViewById(R.id.tv_title);
            TextView tvSender = findViewById(R.id.tv_sender);
            TextView tvTime = findViewById(R.id.tv_time);
            TextView tvContent = findViewById(R.id.tv_content);

            tvTitle.setText(notification.getTitle());
            tvSender.setText("类型：" + notification.getNotificationType());
            tvTime.setText("时间：" + notification.getCreateTime());
            tvContent.setText(notification.getContent());
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
} 
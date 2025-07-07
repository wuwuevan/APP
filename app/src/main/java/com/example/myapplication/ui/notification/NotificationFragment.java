package com.example.myapplication.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.database.NotificationDao;
import com.example.myapplication.model.Notification;
import com.example.myapplication.utils.SharedPreferencesUtil;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知页面，用于显示系统通知和消息通知
 */
public class NotificationFragment extends Fragment {
    
    private TabLayout tabLayout;
    private ListView listView;
    private TextView tvEmpty;
    
    private ArrayAdapter<String> adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationDao notificationDao;
    private SharedPreferencesUtil spUtil;
    private int currentUserId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        
        // 初始化工具类
        notificationDao = new NotificationDao(requireContext());
        spUtil = new SharedPreferencesUtil(requireContext());
        currentUserId = spUtil.getInt("current_user_id", -1);
        
        // 初始化视图
        initViews(view);
        
        // 加载数据
        loadNotifications(null);
        
        // 设置Tab监听
        setTabListener();
        
        return view;
    }
    
    /**
     * 初始化视图
     */
    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        listView = view.findViewById(R.id.list_notifications);
        tvEmpty = view.findViewById(R.id.tv_empty);
        
        // 设置列表项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notification notification = notificationList.get(position);
                Intent intent = new Intent(getContext(), NotificationDetailActivity.class);
                intent.putExtra("title", notification.getTitle());
                intent.putExtra("content", notification.getContent());
                intent.putExtra("time", notification.getCreateTime());
                startActivity(intent);
                
                // 标记为已读
                if (notification.getIsRead() == 0) {
                    notification.setIsRead(1);
                    notificationDao.updateNotification(notification);
                }
            }
        });
    }
    
    /**
     * 加载通知数据
     */
    private void loadNotifications(String notificationType) {
        if (currentUserId <= 0) {
            return;
        }
        
        // 清空当前列表
        notificationList.clear();
        
        // 根据类型加载通知
        if (notificationType == null) {
            notificationList.addAll(notificationDao.getNotificationsByUserId(currentUserId));
        } else {
            notificationList.addAll(notificationDao.getNotificationsByType(currentUserId, notificationType));
        }
        
        // 更新适配器
        List<String> displayList = new ArrayList<>();
        for (Notification notification : notificationList) {
            displayList.add(notification.getTitle() + "\n" + notification.getContent() + "\n" + notification.getCreateTime());
        }
        
        if (adapter == null) {
            adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(displayList);
            adapter.notifyDataSetChanged();
        }
        
        // 更新空状态视图
        if (notificationList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 设置Tab监听
     */
    private void setTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadNotifications(null);
                        break;
                    case 1:
                        loadNotifications("TASK");
                        break;
                    case 2:
                        loadNotifications("ABNORMAL");
                        break;
                    case 3:
                        loadNotifications("FOLLOW_UP");
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
} 
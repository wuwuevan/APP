package com.example.myapplication.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.database.NotificationDao;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.ui.task.TaskActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用户信息页面，用于显示和编辑用户个人信息
 */
public class ProfileFragment extends Fragment {
    
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvGenderSymbol;
    private TextView tvRegisterTime;
    private Button btnPersonalInfo;
    private Button btnAccountSettings;
    private Button btnPrivacySettings;
    private Button btnDailyTask;
    private Button btnHelpFeedback;
    private Button btnAboutUs;
    private Button btnLogout;
    private Button btnNotification;
    private TextView tvNotificationCount;
    
    private SharedPreferencesUtil spUtil;
    private UserDao userDao;
    private NotificationDao notificationDao;
    private User currentUser;
    private int currentUserId;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spUtil = new SharedPreferencesUtil(requireContext());
        userDao = AppDatabase.getInstance(requireContext()).userDao();
        notificationDao = new NotificationDao(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // 初始化视图
        initViews(view);
        // 设置点击事件
        setClickListeners();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 加载用户信息
        loadUserInfo();
        // 更新未读通知数量
        updateNotificationCount();
    }
    
    /**
     * 初始化视图
     */
    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvUsername = view.findViewById(R.id.tv_username);
        tvGenderSymbol = view.findViewById(R.id.tv_gender_symbol);
        tvRegisterTime = view.findViewById(R.id.tv_register_time);
        btnPersonalInfo = view.findViewById(R.id.btn_personal_info);
        btnAccountSettings = view.findViewById(R.id.btn_account_settings);
        btnPrivacySettings = view.findViewById(R.id.btn_privacy_settings);
        btnDailyTask = view.findViewById(R.id.btn_daily_task);
        btnHelpFeedback = view.findViewById(R.id.btn_help_feedback);
        btnAboutUs = view.findViewById(R.id.btn_about_us);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnNotification = view.findViewById(R.id.btn_notification);
        tvNotificationCount = view.findViewById(R.id.tv_notification_count);
    }
    
    /**
     * 设置点击事件
     */
    private void setClickListeners() {
        btnPersonalInfo.setOnClickListener(v -> {
            // 跳转到个人信息编辑页面
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });
        
        btnAccountSettings.setOnClickListener(v -> {
            // 跳转到账号设置页面
            Intent intent = new Intent(requireContext(), AccountSettingsActivity.class);
            startActivity(intent);
        });
        
        btnPrivacySettings.setOnClickListener(v -> {
            // 跳转到隐私设置页面
            Intent intent = new Intent(requireContext(), PrivacySettingsActivity.class);
            startActivity(intent);
        });
        
        btnDailyTask.setOnClickListener(v -> {
            // 跳转到每日任务中心页面
            Intent intent = new Intent(requireContext(), TaskActivity.class);
            startActivity(intent);
        });
        
        btnHelpFeedback.setOnClickListener(v -> {
            // 跳转到帮助与反馈页面
            Intent intent = new Intent(requireContext(), HelpFeedbackActivity.class);
            startActivity(intent);
        });
        
        btnAboutUs.setOnClickListener(v -> {
            // 跳转到关于我们页面
            Intent intent = new Intent(requireContext(), AboutUsActivity.class);
            startActivity(intent);
        });
        
        btnNotification.setOnClickListener(v -> {
            // 跳转到通知页面
            Intent intent = new Intent(requireContext(), NotificationActivity.class);
            startActivity(intent);
        });
        
        btnLogout.setOnClickListener(v -> {
            // 退出登录
            logout();
        });
    }
    
    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        // 从SharedPreferences获取当前登录用户名
        String username = spUtil.getString("current_username", "");
        
        if (!username.isEmpty()) {
            // 从数据库获取用户信息
            currentUser = userDao.getUserByUsername(username);
            
            if (currentUser != null) {
                currentUserId = currentUser.getId();
                // 保存用户ID到SharedPreferences，方便其他地方使用
                spUtil.saveInt("current_user_id", currentUserId);
                
                tvUsername.setText(username);
                if ("女".equals(currentUser.getGender())) {
                    tvGenderSymbol.setText("\u2640");
                    tvGenderSymbol.setTextColor(getResources().getColor(R.color.colorFemale));
                } else {
                    tvGenderSymbol.setText("\u2642");
                    tvGenderSymbol.setTextColor(getResources().getColor(R.color.colorMale));
                }
                
                // 设置注册时间
                if (currentUser.getRegisterTimestamp() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String registerDate = sdf.format(new Date(currentUser.getRegisterTimestamp()));
                    tvRegisterTime.setText("注册时间：" + registerDate);
                } else {
                    tvRegisterTime.setText("注册时间：未知");
                }
                
                // 加载用户头像
                int defaultAvatar = "女".equals(currentUser.getGender()) ? R.drawable.ic_avatar_female : R.drawable.ic_avatar_male;
                if (currentUser.getAvatarPath() != null && !currentUser.getAvatarPath().isEmpty()) {
                    // 使用Glide加载头像
                    Glide.with(this)
                            .load(new File(currentUser.getAvatarPath()))
                            .circleCrop()
                            .placeholder(defaultAvatar)
                            .into(ivAvatar);
                } else {
                    // 使用默认头像
                    ivAvatar.setImageResource(defaultAvatar);
                }
            } else {
                // 用户不存在，跳转到登录页面
                navigateToLogin();
            }
        } else {
            // 如果没有登录用户，跳转到登录页面
            navigateToLogin();
        }
    }
    
    /**
     * 更新未读通知数量
     */
    private void updateNotificationCount() {
        if (currentUserId > 0) {
            int unreadCount = notificationDao.getUnreadNotificationCount(currentUserId);
            if (unreadCount > 0) {
                tvNotificationCount.setVisibility(View.VISIBLE);
                tvNotificationCount.setText(String.valueOf(unreadCount));
            } else {
                tvNotificationCount.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * 退出登录
     */
    private void logout() {
        // 清除SharedPreferences中的登录信息
        spUtil.clearLoginInfo();
        
        Toast.makeText(requireContext(), "退出登录成功", Toast.LENGTH_SHORT).show();
        
        // 跳转到登录页面
        navigateToLogin();
    }
    
    /**
     * 跳转到登录页面
     */
    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
} 
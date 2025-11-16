package com.example.myapplication.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.splashscreen.SplashScreen;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivitySplashBinding;
import com.example.myapplication.ui.auth.LoginActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

/**
 * Modern splash screen with animations
 */
public class SplashActivity extends BaseActivity {
    
    private static final long SPLASH_DELAY = 3000; // 3 seconds
    private ActivitySplashBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the splash screen
        SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        
        // Use view binding for cleaner code
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize UI components
        setupUI();
        
        // Start animations
        startAnimations();
        
        // Check login status after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_DELAY);
    }
    
    /**
     * Set up UI components
     */
    private void setupUI() {
        // Set version text
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.tvVersion.setText("v" + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            binding.tvVersion.setText("v1.0.0");
        }
    }
    
    /**
     * Start all animations for the splash screen
     */
    private void startAnimations() {
        // Start with all elements invisible
        binding.containerLogo.setScaleX(0);
        binding.containerLogo.setScaleY(0);
        binding.tvAppName.setAlpha(0);
        binding.tvTagline.setAlpha(0);
        binding.tvVersion.setAlpha(0);
        
        // Logo animation with bounce effect
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.containerLogo, "scaleX", 0f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.containerLogo, "scaleY", 0f, 1f);
        logoScaleX.setDuration(800);
        logoScaleY.setDuration(800);
        logoScaleX.setInterpolator(new OvershootInterpolator());
        logoScaleY.setInterpolator(new OvershootInterpolator());
        
        // App name fade in animation
        ObjectAnimator appNameFade = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f);
        appNameFade.setDuration(800);
        appNameFade.setStartDelay(300);
        
        // Tagline fade in animation
        ObjectAnimator taglineFade = ObjectAnimator.ofFloat(binding.tvTagline, "alpha", 0f, 1f);
        taglineFade.setDuration(800);
        taglineFade.setStartDelay(500);
        
        // Version text fade in animation
        ObjectAnimator versionFade = ObjectAnimator.ofFloat(binding.tvVersion, "alpha", 0f, 1f);
        versionFade.setDuration(800);
        versionFade.setStartDelay(700);
        
        // Start all animations
        logoScaleX.start();
        logoScaleY.start();
        appNameFade.start();
        taglineFade.start();
        versionFade.start();
        
        // Optional: add a subtle continuous pulse animation to the logo
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.05f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            binding.ivLogo.setScaleX(value);
            binding.ivLogo.setScaleY(value);
        });
        
        // Start pulse animation after the initial scale animation
        logoScaleY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pulseAnimator.start();
            }
        });
    }
    
    /**
     * Check login status and navigate to appropriate screen
     */
    private void checkLoginStatus() {
        SharedPreferencesUtil spUtil = new SharedPreferencesUtil(this);
        
        Intent intent;
        if (spUtil.isLoggedIn()) {
            // User is logged in, go to MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User needs to log in
            intent = new Intent(this, LoginActivity.class);
        }
        
        // Use shared element transition for a smoother experience
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, binding.containerLogo, "app_logo");
        
        // Start the next activity with animation
        startActivity(intent, options.toBundle());
        
        // Add a smooth fade out exit transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        // Finish this activity
        finish();
    }
} 
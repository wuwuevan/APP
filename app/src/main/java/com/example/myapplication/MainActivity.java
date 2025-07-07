package com.example.myapplication;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;

/**
 * Main activity that hosts all fragments and includes the bottom navigation
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private TextView toolbarTitle;
    private ImageView toolbarLogo;
    private View statusBarScrim;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupToolbar();
        setupStatusBar();
        setupBottomNavigation();
        setupNavigation();
    }
    
    /**
     * Set up custom toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Hide default title and show custom title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarLogo = findViewById(R.id.toolbar_logo);
    }
    
    /**
     * Setup status bar scrim for a more cohesive look
     */
    private void setupStatusBar() {
        statusBarScrim = findViewById(R.id.status_bar_scrim);
        int statusBarColor = SurfaceColors.SURFACE_2.getColor(this);
        getWindow().setStatusBarColor(statusBarColor);
        statusBarScrim.setBackgroundColor(statusBarColor);
    }
    
    /**
     * Setup bottom navigation with animation
     */
    private void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        
        // Apply surface color to bottom navigation for Material You look
        navView.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));
        
        // Add bottom navigation elevation animation
        navView.post(() -> {
            // Start with no elevation
            navView.setElevation(0f);
            
            // Animate to final elevation
            ValueAnimator elevationAnimator = ValueAnimator.ofFloat(0f, 8f);
            elevationAnimator.setDuration(500);
            elevationAnimator.setStartDelay(300);
            elevationAnimator.setInterpolator(new DecelerateInterpolator());
            elevationAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                navView.setElevation(value);
            });
            elevationAnimator.start();
        });
    }
    
    /**
     * Setup navigation components
     */
    private void setupNavigation() {
        // 使用NavHostFragment方式获取NavController，这样更可靠
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        
        // Setup navigation UI
        NavigationUI.setupWithNavController(navView, navController);
        
        // Listen for navigation changes to update toolbar
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Update toolbar based on destination
            updateToolbarForDestination(destination.getId());
        });
        }
    }
    
    /**
     * Update toolbar appearance based on current destination
     */
    private void updateToolbarForDestination(int destinationId) {
        if (destinationId == R.id.navigation_home) {
            // Show logo on home screen
            toolbarLogo.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
        } else {
            // Show title on other screens
            toolbarLogo.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.VISIBLE);
            
            // Set title text based on destination
            if (destinationId == R.id.navigation_exercise) {
                toolbarTitle.setText(R.string.title_exercise);
            } else if (destinationId == R.id.navigation_health) {
                toolbarTitle.setText(R.string.title_health);
            } else if (destinationId == R.id.navigation_chat) {
                toolbarTitle.setText(R.string.title_chat);
            } else if (destinationId == R.id.navigation_profile) {
                toolbarTitle.setText(R.string.title_profile);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
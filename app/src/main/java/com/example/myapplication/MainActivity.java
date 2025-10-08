package com.example.myapplication;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.elevation.SurfaceColors;

/**
 * Main activity that hosts all fragments and includes the bottom navigation
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private TextView toolbarTitle;
    private ImageView toolbarLogo;
    private View statusBarScrim;
    private View bottomNavContainer;
    private final SparseIntArray destinationByViewId = new SparseIntArray();
    
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
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);

        // Hide default title and show custom title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarLogo = toolbar.findViewById(R.id.toolbar_logo);
    }
    
    /**
     * Setup status bar scrim for a more cohesive look
     */
    private void setupStatusBar() {
        statusBarScrim = findViewById(R.id.status_bar_scrim);
        int statusBarColor = SurfaceColors.SURFACE_2.getColor(this);
        getWindow().setStatusBarColor(statusBarColor);
        if (statusBarScrim != null) {
            statusBarScrim.setBackgroundColor(statusBarColor);
        }
    }
    
    /**
     * Setup bottom navigation with animation
     */
    private void setupBottomNavigation() {
        bottomNavContainer = findViewById(R.id.bottom_nav_container);
        if (bottomNavContainer == null) {
            return;
        }

        bottomNavContainer.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));

        registerNavItem(R.id.nav_item_home, R.id.navigation_home);
        registerNavItem(R.id.nav_item_gait, R.id.navigation_gait);
        registerNavItem(R.id.nav_item_health, R.id.navigation_health);
        registerNavItem(R.id.nav_item_chat, R.id.navigation_chat);
        registerNavItem(R.id.nav_item_profile, R.id.navigation_profile);
        registerNavItem(R.id.nav_item_community, R.id.navigation_community);

        View homeNav = findViewById(R.id.nav_item_home);
        if (homeNav != null) {
            homeNav.setSelected(true);
            homeNav.setActivated(true);
        }

        // Add elevation animation similar to Material bottom navigation
        bottomNavContainer.post(() -> {
            bottomNavContainer.setElevation(0f);
            ValueAnimator elevationAnimator = ValueAnimator.ofFloat(0f, 8f);
            elevationAnimator.setDuration(500);
            elevationAnimator.setStartDelay(300);
            elevationAnimator.setInterpolator(new DecelerateInterpolator());
            elevationAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                bottomNavContainer.setElevation(value);
            });
            elevationAnimator.start();
        });
    }

    private void registerNavItem(int viewId, int destinationId) {
        View navItem = findViewById(viewId);
        if (navItem == null) {
            return;
        }
        destinationByViewId.put(viewId, destinationId);
        navItem.setOnClickListener(v -> navigateToDestination(destinationId));
    }

    private void navigateToDestination(int destinationId) {
        if (navController == null) {
            return;
        }
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null && currentDestination.getId() == destinationId) {
            return;
        }
        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                .setRestoreState(true)
                .build();
        navController.navigate(destinationId, null, navOptions);
    }
    
    /**
     * Setup navigation components
     */
    private void setupNavigation() {
        // 使用NavHostFragment方式获取NavController，这样更可靠
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            return;
        }

        navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateToolbarForDestination(destination.getId());
            updateBottomNavigationSelection(destination.getId());
        });

        // Ensure the initial destination is highlighted
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null) {
            updateBottomNavigationSelection(currentDestination.getId());
        } else {
            updateBottomNavigationSelection(navController.getGraph().getStartDestinationId());
        }
    }

    private void updateBottomNavigationSelection(int destinationId) {
        for (int i = 0; i < destinationByViewId.size(); i++) {
            int viewId = destinationByViewId.keyAt(i);
            View navItem = findViewById(viewId);
            if (navItem == null) {
                continue;
            }
            boolean isSelected = destinationByViewId.get(viewId) == destinationId;
            navItem.setSelected(isSelected);
            navItem.setActivated(isSelected);
        }
    }
    
    /**
     * Update toolbar appearance based on current destination
     */
    private void updateToolbarForDestination(int destinationId) {
        if (toolbarLogo == null || toolbarTitle == null) {
            return;
        }

        if (destinationId == R.id.navigation_home) {
            // Show logo on home screen
            toolbarLogo.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
        } else {
            // Show title on other screens
            toolbarLogo.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.VISIBLE);
            
            // Set title text based on destination
            if (destinationId == R.id.navigation_gait) {
                toolbarTitle.setText(R.string.title_gait);
            } else if (destinationId == R.id.navigation_health) {
                toolbarTitle.setText(R.string.title_health);
            } else if (destinationId == R.id.navigation_chat) {
                toolbarTitle.setText(R.string.title_chat);
            } else if (destinationId == R.id.navigation_profile) {
                toolbarTitle.setText(R.string.title_profile);
            } else if (destinationId == R.id.navigation_community) {
                toolbarTitle.setText(R.string.title_community);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null && navController.navigateUp()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }
}

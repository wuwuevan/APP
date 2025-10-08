package com.example.myapplication;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.elevation.SurfaceColors;

/**
 * Main activity that hosts all fragments and includes the bottom navigation
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private TextView toolbarTitle;
    private ImageView toolbarLogo;
    private View statusBarScrim;
    private MaterialButtonToggleGroup navToggleGroup;
    
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
        navToggleGroup = findViewById(R.id.nav_toggle_group);

        if (navToggleGroup != null) {
            // Apply surface color to bottom navigation for Material You look
            navToggleGroup.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));

            // Pre-select home tab
            navToggleGroup.check(R.id.navigation_home);

            // Handle user navigation actions
            navToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked || navController == null) {
                    return;
                }

                int destinationId = mapButtonToDestination(checkedId);
                if (destinationId != 0
                        && navController.getCurrentDestination() != null
                        && navController.getCurrentDestination().getId() == destinationId) {
                    return;
                }

                if (destinationId != 0) {
                    navController.navigate(destinationId);
                }
            });

            // Add bottom navigation elevation animation
            navToggleGroup.post(() -> {
                navToggleGroup.setElevation(0f);

                ValueAnimator elevationAnimator = ValueAnimator.ofFloat(0f, 8f);
                elevationAnimator.setDuration(500);
                elevationAnimator.setStartDelay(300);
                elevationAnimator.setInterpolator(new DecelerateInterpolator());
                elevationAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    navToggleGroup.setElevation(value);
                });
                elevationAnimator.start();
            });
        }
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
            // Listen for navigation changes to update toolbar
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Update toolbar based on destination
                updateToolbarForDestination(destination.getId());
                updateBottomNavigationSelection(destination.getId());
            });

            if (navController.getCurrentDestination() != null) {
                updateBottomNavigationSelection(navController.getCurrentDestination().getId());
            }
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
            if (destinationId == R.id.navigation_gait) {
                toolbarTitle.setText(R.string.title_gait);
            } else if (destinationId == R.id.navigation_health) {
                toolbarTitle.setText(R.string.title_health);
            } else if (destinationId == R.id.navigation_chat) {
                toolbarTitle.setText(R.string.title_chat);
            } else if (destinationId == R.id.navigation_community) {
                toolbarTitle.setText(R.string.title_community);
            } else if (destinationId == R.id.navigation_profile) {
                toolbarTitle.setText(R.string.title_profile);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void updateBottomNavigationSelection(@IdRes int destinationId) {
        if (navToggleGroup == null) {
            return;
        }

        int buttonId = mapDestinationToButton(destinationId);
        if (buttonId != 0 && navToggleGroup.getCheckedButtonId() != buttonId) {
            navToggleGroup.check(buttonId);
        }
    }

    private int mapButtonToDestination(@IdRes int buttonId) {
        if (buttonId == R.id.navigation_home) {
            return R.id.navigation_home;
        } else if (buttonId == R.id.navigation_gait) {
            return R.id.navigation_gait;
        } else if (buttonId == R.id.navigation_health) {
            return R.id.navigation_health;
        } else if (buttonId == R.id.navigation_chat) {
            return R.id.navigation_chat;
        } else if (buttonId == R.id.navigation_community) {
            return R.id.navigation_community;
        } else if (buttonId == R.id.navigation_profile) {
            return R.id.navigation_profile;
        }
        return 0;
    }

    private int mapDestinationToButton(@IdRes int destinationId) {
        if (destinationId == R.id.navigation_home) {
            return R.id.navigation_home;
        } else if (destinationId == R.id.navigation_gait) {
            return R.id.navigation_gait;
        } else if (destinationId == R.id.navigation_health) {
            return R.id.navigation_health;
        } else if (destinationId == R.id.navigation_chat) {
            return R.id.navigation_chat;
        } else if (destinationId == R.id.navigation_community) {
            return R.id.navigation_community;
        } else if (destinationId == R.id.navigation_profile) {
            return R.id.navigation_profile;
        }
        return 0;
    }
}
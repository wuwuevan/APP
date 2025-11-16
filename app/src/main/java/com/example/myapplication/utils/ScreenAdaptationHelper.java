package com.example.myapplication.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

/**
 * Utility class that keeps the UI layout consistent across different
 * resolutions and densities by recalculating the display metrics based
 * on the baseline design width.
 */
public final class ScreenAdaptationHelper {

    private static float sNonCompatDensity;
    private static float sNonCompatScaledDensity;

    private ScreenAdaptationHelper() {
        // Utility class
    }

    /**
     * Apply custom density so that dp values correspond to the baseline design width.
     *
     * @param activity      target activity
     * @param designWidthDp baseline design width in dp
     */
    public static void applyCustomDensity(@NonNull Activity activity, float designWidthDp) {
        if (designWidthDp <= 0) {
            return;
        }
        final Application application = activity.getApplication();
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (sNonCompatDensity == 0f) {
            sNonCompatDensity = appDisplayMetrics.density;
            sNonCompatScaledDensity = appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(@NonNull Configuration newConfig) {
                    if (newConfig.fontScale > 0) {
                        sNonCompatScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {
                    // No-op
                }
            });
        }

        final float targetDensity = appDisplayMetrics.widthPixels / designWidthDp;
        final float targetScaledDensity = targetDensity * (sNonCompatScaledDensity / sNonCompatDensity);
        final int targetDensityDpi = (int) (160 * targetDensity);

        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaledDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        final DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaledDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }
}

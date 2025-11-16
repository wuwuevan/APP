package com.example.myapplication.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.utils.ScreenAdaptationHelper;

/**
 * Base activity that applies screen density adaptation before inflating layouts.
 * This keeps the UI consistent with the 900x1600 (dp320) baseline design.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * The baseline design width in dp (900px @ 320dpi = 450dp).
     */
    private static final float DESIGN_WIDTH_DP = 450f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ScreenAdaptationHelper.applyCustomDensity(this, DESIGN_WIDTH_DP);
        super.onCreate(savedInstanceState);
    }
}

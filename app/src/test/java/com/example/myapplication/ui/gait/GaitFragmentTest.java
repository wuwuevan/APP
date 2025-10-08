package com.example.myapplication.ui.gait;

import static org.junit.Assert.assertNotNull;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.R;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class GaitFragmentTest {

    @Test
    public void launchingGaitFragment_doesNotCrash() {
        ActivityController<FragmentActivity> controller = Robolectric.buildActivity(FragmentActivity.class, new Intent());
        FragmentActivity activity = controller.get();
        activity.setTheme(R.style.Theme_MyApplication);
        controller.setup();

        GaitFragment fragment = new GaitFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNow();

        assertNotNull("Gait fragment view should be inflated", fragment.getView());
        assertNotNull("Gait speed view should exist", fragment.getView().findViewById(R.id.tv_gait_speed));
    }
}

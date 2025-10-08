package com.example.myapplication.ui.gait;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentGaitBinding;

import java.util.Locale;

/**
 * 步态分析页面 Fragment，仅提供前端展示效果和模拟数据。
 */
public class GaitFragment extends Fragment {

    private FragmentGaitBinding binding;
    private GaitEventAdapter eventAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGaitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupChipInteractions();
        setupRefreshButton();

        if (binding != null) {
            binding.chipGroupActivity.check(R.id.chip_walk);
        }
        loadGaitData(GaitDataGenerator.ActivityType.WALKING);
    }

    private void setupRecyclerView() {
        FragmentGaitBinding currentBinding = binding;
        if (currentBinding == null) {
            return;
        }

        RecyclerView recyclerView = currentBinding.recyclerRecentEvents;
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        eventAdapter = new GaitEventAdapter();
        recyclerView.setAdapter(eventAdapter);
    }

    private void setupChipInteractions() {
        FragmentGaitBinding currentBinding = binding;
        if (currentBinding == null) {
            return;
        }

        currentBinding.chipGroupActivity.setOnCheckedChangeListener((group, checkedId) -> handleChipSelection(checkedId));
    }

    private void setupRefreshButton() {
        FragmentGaitBinding currentBinding = binding;
        if (currentBinding == null) {
            return;
        }

        currentBinding.btnRefreshGait.setOnClickListener(v -> {
            FragmentGaitBinding bindingSnapshot = binding;
            if (bindingSnapshot == null) {
                return;
            }
            handleChipSelection(bindingSnapshot.chipGroupActivity.getCheckedChipId());
        });
    }

    private void handleChipSelection(int checkedId) {
        if (checkedId == View.NO_ID) {
            checkedId = R.id.chip_walk;
        }

        GaitDataGenerator.ActivityType type;
        if (checkedId == R.id.chip_stairs) {
            type = GaitDataGenerator.ActivityType.STAIR_CLIMBING;
        } else if (checkedId == R.id.chip_outdoor) {
            type = GaitDataGenerator.ActivityType.OUTDOOR_ACTIVITY;
        } else {
            type = GaitDataGenerator.ActivityType.WALKING;
        }

        if (binding != null && binding.chipGroupActivity.getCheckedChipId() != checkedId) {
            binding.chipGroupActivity.check(checkedId);
        }
        loadGaitData(type);
    }

    private void loadGaitData(GaitDataGenerator.ActivityType type) {
        FragmentGaitBinding currentBinding = binding;
        if (currentBinding == null || !isAdded()) {
            return;
        }

        GaitDataGenerator.GaitSummary summary = GaitDataGenerator.generateSummary(type);
        currentBinding.tvGaitSpeed.setText(formatDecimal(summary.gaitSpeed));
        currentBinding.tvCadence.setText(String.valueOf(summary.cadence));
        currentBinding.tvStepLength.setText(formatDecimal(summary.stepLength));
        currentBinding.tvSymmetry.setText(getString(R.string.gait_symmetry_value, summary.symmetryScore));
        currentBinding.tvCurrentActivity.setText(summary.activityLabel);
        currentBinding.tvStabilityLevel.setText(summary.stabilityLevel);
        currentBinding.indicatorStability.setProgress(clampProgress(summary.stabilityScore));
        currentBinding.indicatorFallRisk.setProgress(clampProgress(summary.fallRiskPercent));
        currentBinding.tvAnalysisSummary.setText(summary.analysisSummary);
        currentBinding.tvFallStatus.setText(summary.fallDetectionMessage);
        currentBinding.tvLastUpdated.setText(getString(R.string.gait_last_updated_format, summary.lastUpdatedTime));

        if (eventAdapter != null) {
            eventAdapter.updateEvents(GaitDataGenerator.generateRecentEvents(type));
        }
    }

    private String formatDecimal(float value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private int clampProgress(int value) {
        return Math.max(0, Math.min(100, value));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.recyclerRecentEvents.setAdapter(null);
        }
        binding = null;
        eventAdapter = null;
    }
}

package com.example.myapplication.ui.gait;

import android.content.Context;
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

import java.text.DecimalFormat;

/**
 * 步态分析页面 Fragment，仅提供前端展示效果和模拟数据。
 */
public class GaitFragment extends Fragment {

    private FragmentGaitBinding binding;
    private GaitEventAdapter eventAdapter;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGaitBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupChipInteractions();
        setupRefreshButton();

        // 默认展示步行数据
        if (binding != null) {
            binding.chipWalk.setChecked(true);
        }
        loadGaitData(GaitDataGenerator.ActivityType.WALKING);

        return binding != null ? binding.getRoot() : null;
    }

    private void setupRecyclerView() {
        if (binding == null) {
            return;
        }

        RecyclerView recyclerView = binding.recyclerRecentEvents;
        Context context = recyclerView.getContext();
        if (context != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        recyclerView.setNestedScrollingEnabled(false);
        eventAdapter = new GaitEventAdapter();
        recyclerView.setAdapter(eventAdapter);
    }

    private void setupChipInteractions() {
        if (binding == null) {
            return;
        }

        binding.chipGroupActivity.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                return;
            }
            if (checkedId == R.id.chip_walk) {
                loadGaitData(GaitDataGenerator.ActivityType.WALKING);
            } else if (checkedId == R.id.chip_stairs) {
                loadGaitData(GaitDataGenerator.ActivityType.STAIR_CLIMBING);
            } else if (checkedId == R.id.chip_outdoor) {
                loadGaitData(GaitDataGenerator.ActivityType.OUTDOOR_ACTIVITY);
            }
        });
    }

    private void setupRefreshButton() {
        if (binding == null) {
            return;
        }

        binding.btnRefreshGait.setOnClickListener(v -> {
            if (binding == null) {
                return;
            }

            int checkedId = binding.chipGroupActivity.getCheckedChipId();
            if (checkedId == R.id.chip_stairs) {
                loadGaitData(GaitDataGenerator.ActivityType.STAIR_CLIMBING);
            } else if (checkedId == R.id.chip_outdoor) {
                loadGaitData(GaitDataGenerator.ActivityType.OUTDOOR_ACTIVITY);
            } else {
                binding.chipWalk.setChecked(true);
                loadGaitData(GaitDataGenerator.ActivityType.WALKING);
            }
        });
    }

    private void loadGaitData(GaitDataGenerator.ActivityType type) {
        FragmentGaitBinding currentBinding = binding;
        if (currentBinding == null || !isAdded()) {
            return;
        }

        GaitDataGenerator.GaitSummary summary = GaitDataGenerator.generateSummary(type);
        currentBinding.tvGaitSpeed.setText(decimalFormat.format(summary.gaitSpeed));
        currentBinding.tvCadence.setText(String.valueOf(summary.cadence));
        currentBinding.tvStepLength.setText(decimalFormat.format(summary.stepLength));
        currentBinding.tvSymmetry.setText(getString(R.string.gait_symmetry_value, summary.symmetryScore));
        currentBinding.tvCurrentActivity.setText(summary.activityLabel);
        currentBinding.tvStabilityLevel.setText(summary.stabilityLevel);
        currentBinding.indicatorStability.setProgress(summary.stabilityScore);
        currentBinding.indicatorFallRisk.setProgress(summary.fallRiskPercent);
        currentBinding.tvAnalysisSummary.setText(summary.analysisSummary);
        currentBinding.tvFallStatus.setText(summary.fallDetectionMessage);
        currentBinding.tvLastUpdated.setText(getString(R.string.gait_last_updated_format, summary.lastUpdatedTime));

        if (eventAdapter != null) {
            eventAdapter.updateEvents(GaitDataGenerator.generateRecentEvents(type));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        eventAdapter = null;
    }
}

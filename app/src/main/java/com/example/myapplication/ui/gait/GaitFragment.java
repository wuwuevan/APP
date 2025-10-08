package com.example.myapplication.ui.gait;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DecimalFormat;

/**
 * 步态分析页面 Fragment，仅提供前端展示效果和模拟数据。
 */
public class GaitFragment extends Fragment {

    private TextView tvGaitSpeed;
    private TextView tvCadence;
    private TextView tvStepLength;
    private TextView tvSymmetry;
    private TextView tvCurrentActivity;
    private TextView tvStabilityLevel;
    private TextView tvAnalysisSummary;
    private TextView tvFallStatus;
    private TextView tvLastUpdated;
    private LinearProgressIndicator stabilityIndicator;
    private LinearProgressIndicator fallRiskIndicator;
    private ChipGroup chipGroupActivity;
    private Chip chipWalk;
    private Chip chipStairs;
    private Chip chipOutdoor;
    private MaterialButton btnRefresh;
    private GaitEventAdapter eventAdapter;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gait, container, false);

        initViews(root);
        setupRecyclerView(root);
        setupChipInteractions();
        setupRefreshButton();

        // 默认展示步行数据
        chipWalk.setChecked(true);
        loadGaitData(GaitDataGenerator.ActivityType.WALKING);

        return root;
    }

    private void initViews(View root) {
        tvGaitSpeed = root.findViewById(R.id.tv_gait_speed);
        tvCadence = root.findViewById(R.id.tv_cadence);
        tvStepLength = root.findViewById(R.id.tv_step_length);
        tvSymmetry = root.findViewById(R.id.tv_symmetry);
        tvCurrentActivity = root.findViewById(R.id.tv_current_activity);
        tvStabilityLevel = root.findViewById(R.id.tv_stability_level);
        tvAnalysisSummary = root.findViewById(R.id.tv_analysis_summary);
        tvFallStatus = root.findViewById(R.id.tv_fall_status);
        tvLastUpdated = root.findViewById(R.id.tv_last_updated);
        stabilityIndicator = root.findViewById(R.id.indicator_stability);
        fallRiskIndicator = root.findViewById(R.id.indicator_fall_risk);
        chipGroupActivity = root.findViewById(R.id.chip_group_activity);
        chipWalk = root.findViewById(R.id.chip_walk);
        chipStairs = root.findViewById(R.id.chip_stairs);
        chipOutdoor = root.findViewById(R.id.chip_outdoor);
        btnRefresh = root.findViewById(R.id.btn_refresh_gait);
    }

    private void setupRecyclerView(View root) {
        RecyclerView recyclerView = root.findViewById(R.id.recycler_recent_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
        eventAdapter = new GaitEventAdapter();
        recyclerView.setAdapter(eventAdapter);
    }

    private void setupChipInteractions() {
        chipGroupActivity.setOnCheckedChangeListener((group, checkedId) -> {
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
        btnRefresh.setOnClickListener(v -> {
            int checkedId = chipGroupActivity.getCheckedChipId();
            if (checkedId == R.id.chip_stairs) {
                loadGaitData(GaitDataGenerator.ActivityType.STAIR_CLIMBING);
            } else if (checkedId == R.id.chip_outdoor) {
                loadGaitData(GaitDataGenerator.ActivityType.OUTDOOR_ACTIVITY);
            } else {
                chipWalk.setChecked(true);
                loadGaitData(GaitDataGenerator.ActivityType.WALKING);
            }
        });
    }

    private void loadGaitData(GaitDataGenerator.ActivityType type) {
        GaitDataGenerator.GaitSummary summary = GaitDataGenerator.generateSummary(type);
        tvGaitSpeed.setText(decimalFormat.format(summary.gaitSpeed));
        tvCadence.setText(String.valueOf(summary.cadence));
        tvStepLength.setText(decimalFormat.format(summary.stepLength));
        tvSymmetry.setText(getString(R.string.gait_symmetry_value, summary.symmetryScore));
        tvCurrentActivity.setText(summary.activityLabel);
        tvStabilityLevel.setText(summary.stabilityLevel);
        stabilityIndicator.setProgress(summary.stabilityScore);
        fallRiskIndicator.setProgress(summary.fallRiskPercent);
        tvAnalysisSummary.setText(summary.analysisSummary);
        tvFallStatus.setText(summary.fallDetectionMessage);
        tvLastUpdated.setText(getString(R.string.gait_last_updated_format, summary.lastUpdatedTime));

        eventAdapter.updateEvents(GaitDataGenerator.generateRecentEvents(type));
    }
}

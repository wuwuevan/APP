package com.example.myapplication.ui.gait;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentGaitBinding;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 步态分析页面的前端展示，采用模拟数据填充。 
 */
public class GaitFragment extends Fragment {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private FragmentGaitBinding binding;
    private GaitEventAdapter eventAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGaitBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupChips();
        setupButtons();

        if (binding != null) {
            binding.chipWalk.setChecked(true);
            loadGaitData(GaitDataGenerator.ActivityType.WALKING);
        }

        return binding != null ? binding.getRoot() : null;
    }

    private void setupRecyclerView() {
        if (binding == null) {
            return;
        }
        eventAdapter = new GaitEventAdapter();
        binding.recyclerRecentEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecentEvents.setAdapter(eventAdapter);
        binding.recyclerRecentEvents.setNestedScrollingEnabled(false);
    }

    private void setupChips() {
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

    private void setupButtons() {
        if (binding == null) {
            return;
        }
        binding.btnRefreshGait.setOnClickListener(v -> reloadCurrentActivity());
        binding.btnViewAll.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.gait_view_all_placeholder, Toast.LENGTH_SHORT).show());
    }

    private void reloadCurrentActivity() {
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
    }

    private void loadGaitData(GaitDataGenerator.ActivityType type) {
        FragmentGaitBinding currentBinding = binding;
        if (currentBinding == null || !isAdded()) {
            return;
        }

        GaitDataGenerator.GaitSnapshot snapshot = GaitDataGenerator.requestSnapshot(type);
        GaitDataGenerator.GaitSummary summary = snapshot.summary;

        currentBinding.tvLastUpdated.setText(getString(R.string.gait_last_updated_format, summary.lastUpdatedTime));
        currentBinding.tvCurrentActivity.setText(summary.activityLabel);
        currentBinding.tvGaitSpeed.setText(decimalFormat.format(summary.gaitSpeed));
        currentBinding.tvSpeedTrend.setText(summary.speedTrend.label);
        applyTrendColor(currentBinding.tvSpeedTrend, summary.speedTrend.colorRes);

        currentBinding.tvStepLength.setText(decimalFormat.format(summary.stepLength));
        currentBinding.tvStepTrend.setText(summary.stepTrend.label);
        applyTrendColor(currentBinding.tvStepTrend, summary.stepTrend.colorRes);

        currentBinding.tvCadence.setText(String.valueOf(summary.cadence));
        currentBinding.tvCadenceTrend.setText(summary.cadenceTrend.label);
        applyTrendColor(currentBinding.tvCadenceTrend, summary.cadenceTrend.colorRes);

        currentBinding.tvSymmetry.setText(getString(R.string.gait_symmetry_short_value, summary.symmetryScore));
        currentBinding.tvSymmetryTrend.setText(summary.symmetryTrend.label);
        applyTrendColor(currentBinding.tvSymmetryTrend, summary.symmetryTrend.colorRes);

        currentBinding.tvSupportTime.setText(getString(R.string.gait_percent_format, summary.supportPhase));
        currentBinding.tvSupportTimeHint.setText(summary.supportHint);
        currentBinding.tvSwingTime.setText(getString(R.string.gait_percent_format, summary.swingPhase));
        currentBinding.tvSwingTimeHint.setText(summary.swingHint);
        currentBinding.tvVariability.setText(decimalFormat.format(summary.variability) + "%");
        currentBinding.tvVariabilityHint.setText(summary.variabilityHint);

        currentBinding.indicatorStability.setProgress(summary.stabilityScore);
        currentBinding.tvStabilityLevel.setText(summary.stabilityLevel);
        currentBinding.indicatorFallRisk.setProgress(summary.fallRiskPercent);
        currentBinding.tvFallStatus.setText(summary.fallDetectionMessage);
        currentBinding.tvFallRiskLabel.setText(summary.fallRiskLabel);

        currentBinding.chipStabilityBadge.setText(summary.stabilityBadge);
        currentBinding.chipFallBadge.setText(summary.fallBadge);

        currentBinding.tvAnalysisSummary.setText(summary.analysisSummary);
        bindInsights(summary.insights);

        if (eventAdapter != null) {
            eventAdapter.updateEvents(snapshot.events);
        }
    }

    private void bindInsights(List<String> insights) {
        if (binding == null) {
            return;
        }
        updateInsight(binding.tvInsightPrimary, insights, 0);
        updateInsight(binding.tvInsightSecondary, insights, 1);
        updateInsight(binding.tvInsightTertiary, insights, 2);
    }

    private void updateInsight(TextView textView, List<String> insights, int index) {
        if (textView == null) {
            return;
        }
        if (insights != null && insights.size() > index) {
            textView.setText(insights.get(index));
        } else {
            textView.setText(R.string.gait_insight_placeholder);
        }
    }

    private void applyTrendColor(TextView view, @ColorRes int colorRes) {
        if (view == null) {
            return;
        }
        int resolvedColor = ContextCompat.getColor(requireContext(), colorRes);
        view.setTextColor(resolvedColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        eventAdapter = null;
    }
}

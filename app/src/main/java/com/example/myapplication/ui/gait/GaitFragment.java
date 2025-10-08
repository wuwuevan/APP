package com.example.myapplication.ui.gait;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.gait.GaitMockService.ActivityPrediction;
import com.example.myapplication.ui.gait.GaitMockService.FallDetection;
import com.example.myapplication.ui.gait.GaitMockService.GaitDashboardData;
import com.example.myapplication.ui.gait.GaitMockService.GaitMetric;
import com.example.myapplication.ui.gait.GaitMockService.GaitTimelineEvent;
import com.example.myapplication.ui.gait.GaitMockService.Trend;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Locale;

/**
 * 步态分析界面
 */
public class GaitFragment extends Fragment {

    private TextView tvGaitScore;
    private TextView tvGaitStatus;
    private TextView tvGaitStatusHint;
    private TextView tvGaitSummary;

    private TextView tvSpeedValue;
    private TextView tvSpeedTrend;
    private TextView tvCadenceValue;
    private TextView tvCadenceTrend;
    private TextView tvStrideValue;
    private TextView tvStrideTrend;
    private TextView tvSymmetryValue;
    private TextView tvSymmetryTrend;
    private TextView tvStanceValue;
    private TextView tvStanceTrend;
    private TextView tvVariabilityValue;
    private TextView tvVariabilityTrend;

    private ChipGroup chipActivityTypes;
    private TextView tvGaitInsights;
    private LinearLayout containerTimeline;
    private TextView tvTimelineEmpty;

    private TextView tvFallStatus;
    private TextView tvFallProbability;
    private TextView tvFallLastEvent;
    private TextView tvFallRecommendation;

    private MaterialButton btnRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gait, container, false);
        initViews(root);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindDashboardData();
            }
        });
        bindDashboardData();
        return root;
    }

    private void initViews(View root) {
        tvGaitScore = root.findViewById(R.id.tv_gait_score);
        tvGaitStatus = root.findViewById(R.id.tv_gait_status);
        tvGaitStatusHint = root.findViewById(R.id.tv_gait_status_hint);
        tvGaitSummary = root.findViewById(R.id.tv_gait_summary);

        tvSpeedValue = root.findViewById(R.id.tv_metric_speed_value);
        tvSpeedTrend = root.findViewById(R.id.tv_metric_speed_trend);
        tvCadenceValue = root.findViewById(R.id.tv_metric_cadence_value);
        tvCadenceTrend = root.findViewById(R.id.tv_metric_cadence_trend);
        tvStrideValue = root.findViewById(R.id.tv_metric_stride_value);
        tvStrideTrend = root.findViewById(R.id.tv_metric_stride_trend);
        tvSymmetryValue = root.findViewById(R.id.tv_metric_symmetry_value);
        tvSymmetryTrend = root.findViewById(R.id.tv_metric_symmetry_trend);
        tvStanceValue = root.findViewById(R.id.tv_metric_stance_value);
        tvStanceTrend = root.findViewById(R.id.tv_metric_stance_trend);
        tvVariabilityValue = root.findViewById(R.id.tv_metric_variability_value);
        tvVariabilityTrend = root.findViewById(R.id.tv_metric_variability_trend);

        chipActivityTypes = root.findViewById(R.id.chip_activity_types);
        tvGaitInsights = root.findViewById(R.id.tv_gait_insights);
        containerTimeline = root.findViewById(R.id.container_gait_timeline);
        tvTimelineEmpty = root.findViewById(R.id.tv_timeline_empty);

        tvFallStatus = root.findViewById(R.id.tv_fall_status);
        tvFallProbability = root.findViewById(R.id.tv_fall_probability);
        tvFallLastEvent = root.findViewById(R.id.tv_fall_last_event);
        tvFallRecommendation = root.findViewById(R.id.tv_fall_recommendation);

        btnRefresh = root.findViewById(R.id.btn_refresh_gait);
    }

    private void bindDashboardData() {
        GaitDashboardData data = GaitMockService.fetchGaitDashboard();
        tvGaitScore.setText(String.valueOf(data.getScore()));
        tvGaitStatus.setText(data.getStatus());
        tvGaitStatusHint.setText(data.getStatusHint());
        tvGaitSummary.setText(data.getSummary());

        List<GaitMetric> metrics = data.getMetrics();
        for (GaitMetric metric : metrics) {
            applyMetric(metric);
        }

        populateActivityChips(data.getActivities());
        populateInsights(data.getInsights());
        populateTimeline(data.getTimeline());
        bindFallDetection(data.getFallDetection());
    }

    private void applyMetric(GaitMetric metric) {
        if (metric == null) {
            return;
        }
        switch (metric.getId()) {
            case "speed":
                updateMetricViews(tvSpeedValue, tvSpeedTrend, metric);
                break;
            case "cadence":
                updateMetricViews(tvCadenceValue, tvCadenceTrend, metric);
                break;
            case "stride":
                updateMetricViews(tvStrideValue, tvStrideTrend, metric);
                break;
            case "symmetry":
                updateMetricViews(tvSymmetryValue, tvSymmetryTrend, metric);
                break;
            case "stance":
                updateMetricViews(tvStanceValue, tvStanceTrend, metric);
                break;
            case "variability":
                updateMetricViews(tvVariabilityValue, tvVariabilityTrend, metric);
                break;
            default:
                break;
        }
    }

    private void updateMetricViews(TextView valueView, TextView trendView, GaitMetric metric) {
        if (valueView == null || trendView == null) {
            return;
        }
        valueView.setText(metric.getValue());
        trendView.setText(metric.getTrendText());
        int colorRes;
        Trend trend = metric.getTrend();
        if (trend == Trend.IMPROVING) {
            colorRes = R.color.colorSuccess;
        } else if (trend == Trend.DECLINING) {
            colorRes = R.color.colorWarning;
        } else {
            colorRes = R.color.colorTextSecondary;
        }
        trendView.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void populateActivityChips(List<ActivityPrediction> activities) {
        chipActivityTypes.removeAllViews();
        Context context = getContext();
        if (context == null || activities == null || activities.isEmpty()) {
            return;
        }
        for (ActivityPrediction activity : activities) {
            Chip chip = new Chip(context);
            chip.setText(String.format(Locale.CHINA, "%s · %d%%", activity.getLabel(), activity.getConfidence()));
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setChipStrokeWidth(0f);
            if (activity.isPrimary()) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_background_color_selected)));
                chip.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_background_color_normal)));
                chip.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
            }
            chipActivityTypes.addView(chip);
        }
    }

    private void populateInsights(List<String> insights) {
        if (insights == null || insights.isEmpty()) {
            tvGaitInsights.setText(R.string.gait_analysis_placeholder);
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String insight : insights) {
            builder.append("• ").append(insight).append('\n');
        }
        tvGaitInsights.setText(builder.toString().trim());
    }

    private void populateTimeline(List<GaitTimelineEvent> events) {
        containerTimeline.removeAllViews();
        if (events == null || events.isEmpty()) {
            tvTimelineEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvTimelineEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (GaitTimelineEvent event : events) {
            View itemView = inflater.inflate(R.layout.item_gait_event, containerTimeline, false);
            TextView title = itemView.findViewById(R.id.tv_event_title);
            TextView time = itemView.findViewById(R.id.tv_event_time);
            TextView description = itemView.findViewById(R.id.tv_event_description);
            title.setText(event.getTitle());
            time.setText(event.getTime());
            description.setText(event.getDescription());
            containerTimeline.addView(itemView);
        }
    }

    private void bindFallDetection(FallDetection detection) {
        if (detection == null) {
            return;
        }
        tvFallStatus.setText(detection.getStatus());
        tvFallProbability.setText(detection.getProbabilityText());
        tvFallLastEvent.setText(detection.getLastEventText());
        tvFallRecommendation.setText(detection.getRecommendation());
        int statusColor = detection.isWarning() ? R.color.colorWarning : R.color.colorSuccess;
        tvFallStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColor));
        tvFallRecommendation.setTextColor(ContextCompat.getColor(requireContext(), detection.isWarning() ? R.color.colorWarning : R.color.colorInfo));
    }
}

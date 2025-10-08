package com.example.myapplication.ui.gait;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 生成步态分析所需的模拟数据，便于前端展示效果。
 */
public final class GaitDataGenerator {

    private static final Random RANDOM = new Random();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private GaitDataGenerator() {
        // no-op
    }

    public static GaitSummary generateSummary(ActivityType type) {
        float speedBase;
        int cadenceBase;
        float stepLengthBase;
        switch (type) {
            case STAIR_CLIMBING:
                speedBase = 0.65f;
                cadenceBase = 96;
                stepLengthBase = 0.45f;
                break;
            case OUTDOOR_ACTIVITY:
                speedBase = 1.15f;
                cadenceBase = 116;
                stepLengthBase = 0.65f;
                break;
            case WALKING:
            default:
                speedBase = 0.95f;
                cadenceBase = 108;
                stepLengthBase = 0.58f;
                break;
        }

        float gaitSpeed = round(speedBase + randomOffset(0.15f));
        int cadence = cadenceBase + RANDOM.nextInt(11) - 5;
        float stepLength = round(stepLengthBase + randomOffset(0.08f));
        int symmetryScore = clamp(78 + RANDOM.nextInt(15), 60, 100);
        int stabilityScore = clamp(72 + RANDOM.nextInt(18), 50, 100);
        int fallRiskPercent = clamp(100 - stabilityScore + RANDOM.nextInt(8), 5, 55);

        String activityLabel;
        switch (type) {
            case STAIR_CLIMBING:
                activityLabel = "爬楼梯训练";
                break;
            case OUTDOOR_ACTIVITY:
                activityLabel = "户外步行";
                break;
            case WALKING:
            default:
                activityLabel = "室内步行";
                break;
        }

        String stabilityLevel = stabilityScore >= 88 ? "表现稳定" : stabilityScore >= 75 ? "轻度波动" : "需重点关注";
        String analysisSummary = buildAnalysisSummary(gaitSpeed, symmetryScore, fallRiskPercent);
        String fallDetectionMessage = fallRiskPercent < 28 ? "未检测到跌倒风险" : fallRiskPercent < 45 ? "存在轻度跌倒风险，注意放慢节奏" : "检测到较高跌倒风险，请使用辅助工具";
        String lastUpdatedTime = TIME_FORMAT.format(new Date());
        String gaitPhase = determineGaitPhase(gaitSpeed, type);
        String focusArea = determineFocusArea(symmetryScore, stabilityScore, type);
        String trainingRecommendation = buildTrainingRecommendation(focusArea, fallRiskPercent);
        int sessionDurationMinutes = 12 + RANDOM.nextInt(18);
        int stepCount = 600 + RANDOM.nextInt(1500);
        int confidenceScore = clamp(82 + RANDOM.nextInt(12), 70, 98);

        return new GaitSummary(gaitSpeed, cadence, stepLength, symmetryScore, stabilityScore, fallRiskPercent,
                activityLabel, stabilityLevel, analysisSummary, fallDetectionMessage, lastUpdatedTime,
                gaitPhase, focusArea, trainingRecommendation, sessionDurationMinutes, stepCount, confidenceScore);
    }

    public static List<GaitEvent> generateRecentEvents(ActivityType type) {
        List<GaitEvent> events = new ArrayList<>();
        int eventCount = 4 + RANDOM.nextInt(2);
        for (int i = 0; i < eventCount; i++) {
            boolean isAlert = RANDOM.nextInt(100) < (type == ActivityType.STAIR_CLIMBING ? 35 : 18);
            String timeStamp = TIME_FORMAT.format(new Date(System.currentTimeMillis() - i * 7 * 60 * 1000L));
            String title = isAlert ? "姿态偏移预警" : "步态记录";
            String subtitle = buildEventSubtitle(type, timeStamp);
            String statusLabel = isAlert ? "异常" : "正常";
            String description = buildEventDescription(isAlert, type);
            @ColorRes int statusColor = isAlert ? R.color.colorWarning : R.color.colorSuccess;
            @DrawableRes int iconRes = isAlert ? R.drawable.ic_warning : R.drawable.ic_gait;
            events.add(new GaitEvent(title, subtitle, description, statusLabel, statusColor, iconRes));
        }
        return events;
    }

    private static String buildAnalysisSummary(float speed, int symmetryScore, int fallRiskPercent) {
        StringBuilder builder = new StringBuilder();
        builder.append("当前步速为").append(String.format(Locale.getDefault(), "%.2f", speed)).append(" m/s，");
        builder.append("步态对称性达到").append(symmetryScore).append("%，");
        if (fallRiskPercent < 25) {
            builder.append("整体表现稳定，可继续保持现有训练节奏。");
        } else if (fallRiskPercent < 45) {
            builder.append("建议加入核心力量和下肢稳定性练习。");
        } else {
            builder.append("建议立即联系康复师进行个性化评估。");
        }
        return builder.toString();
    }

    private static String determineGaitPhase(float speed, ActivityType type) {
        if (type == ActivityType.STAIR_CLIMBING) {
            return speed > 0.75f ? "上楼推进" : "缓步爬升";
        }
        if (type == ActivityType.OUTDOOR_ACTIVITY) {
            return speed > 1.2f ? "快速行进" : "平稳巡航";
        }
        return speed > 1.05f ? "快速步行" : speed > 0.85f ? "标准步态" : "热身调整";
    }

    private static String determineFocusArea(int symmetryScore, int stabilityScore, ActivityType type) {
        if (symmetryScore < 80) {
            return "侧向平衡与核心稳定";
        }
        if (stabilityScore < 78) {
            return "下肢力量与支撑";
        }
        if (type == ActivityType.STAIR_CLIMBING) {
            return "膝踝协调与力量";
        }
        return "维持当前训练节奏";
    }

    private static String buildTrainingRecommendation(String focusArea, int fallRisk) {
        StringBuilder recommendation = new StringBuilder();
        recommendation.append("今日建议关注：").append(focusArea).append("。");
        if (fallRisk < 25) {
            recommendation.append("可加入轻量速度训练，保持节奏。");
        } else if (fallRisk < 40) {
            recommendation.append("建议搭配核心力量训练并在转身时减速。");
        } else {
            recommendation.append("请使用辅助器具并与康复师沟通调整训练计划。");
        }
        return recommendation.toString();
    }

    private static String buildEventSubtitle(ActivityType type, String timeStamp) {
        switch (type) {
            case STAIR_CLIMBING:
                return timeStamp + " · 楼梯训练";
            case OUTDOOR_ACTIVITY:
                return timeStamp + " · 户外活动";
            case WALKING:
            default:
                return timeStamp + " · 室内步行";
        }
    }

    private static String buildEventDescription(boolean isAlert, ActivityType type) {
        if (isAlert) {
            if (type == ActivityType.STAIR_CLIMBING) {
                return "检测到踏步不稳定，建议扶手辅助并放慢节奏。";
            }
            return "重心偏移增大，请注意调整步幅并保持核心收紧。";
        }
        if (type == ActivityType.OUTDOOR_ACTIVITY) {
            return "户外步态平稳，心率与步速处于安全范围。";
        }
        return "步态稳定，未检测到异常波动。";
    }

    private static float randomOffset(float maxOffset) {
        return (RANDOM.nextFloat() * maxOffset) - (maxOffset / 2f);
    }

    private static float round(float value) {
        return Math.round(value * 100f) / 100f;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public enum ActivityType {
        WALKING,
        STAIR_CLIMBING,
        OUTDOOR_ACTIVITY
    }

    /**
     * 步态汇总信息模型。
     */
    public static class GaitSummary {
        public final float gaitSpeed;
        public final int cadence;
        public final float stepLength;
        public final int symmetryScore;
        public final int stabilityScore;
        public final int fallRiskPercent;
        public final String activityLabel;
        public final String stabilityLevel;
        public final String analysisSummary;
        public final String fallDetectionMessage;
        public final String lastUpdatedTime;
        public final String gaitPhase;
        public final String focusArea;
        public final String trainingRecommendation;
        public final int sessionDurationMinutes;
        public final int stepCount;
        public final int confidenceScore;

        GaitSummary(float gaitSpeed, int cadence, float stepLength, int symmetryScore, int stabilityScore,
                    int fallRiskPercent, String activityLabel, String stabilityLevel, String analysisSummary,
                    String fallDetectionMessage, String lastUpdatedTime, String gaitPhase, String focusArea,
                    String trainingRecommendation, int sessionDurationMinutes, int stepCount, int confidenceScore) {
            this.gaitSpeed = gaitSpeed;
            this.cadence = cadence;
            this.stepLength = stepLength;
            this.symmetryScore = symmetryScore;
            this.stabilityScore = stabilityScore;
            this.fallRiskPercent = fallRiskPercent;
            this.activityLabel = activityLabel;
            this.stabilityLevel = stabilityLevel;
            this.analysisSummary = analysisSummary;
            this.fallDetectionMessage = fallDetectionMessage;
            this.lastUpdatedTime = lastUpdatedTime;
            this.gaitPhase = gaitPhase;
            this.focusArea = focusArea;
            this.trainingRecommendation = trainingRecommendation;
            this.sessionDurationMinutes = sessionDurationMinutes;
            this.stepCount = stepCount;
            this.confidenceScore = confidenceScore;
        }
    }

    /**
     * 步态事件模型。
     */
    public static class GaitEvent {
        public final String title;
        public final String subtitle;
        public final String description;
        public final String statusLabel;
        @ColorRes
        public final int statusColorRes;
        @DrawableRes
        public final int iconRes;

        GaitEvent(String title, String subtitle, String description, String statusLabel,
                  @ColorRes int statusColorRes, @DrawableRes int iconRes) {
            this.title = title;
            this.subtitle = subtitle;
            this.description = description;
            this.statusLabel = statusLabel;
            this.statusColorRes = statusColorRes;
            this.iconRes = iconRes;
        }
    }
}

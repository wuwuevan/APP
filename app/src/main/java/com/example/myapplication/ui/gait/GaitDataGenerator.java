package com.example.myapplication.ui.gait;

import androidx.annotation.ColorRes;

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
        int stabilityScore = clamp(70 + RANDOM.nextInt(20), 50, 100);
        int fallRiskPercent = clamp(100 - stabilityScore + RANDOM.nextInt(10), 5, 60);

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

        String stabilityLevel = stabilityScore >= 85 ? "稳定" : stabilityScore >= 70 ? "轻度波动" : "需关注";
        String analysisSummary = buildAnalysisSummary(gaitSpeed, symmetryScore, fallRiskPercent);
        String fallDetectionMessage = fallRiskPercent < 30 ? "未检测到跌倒风险" : "存在轻度跌倒风险，建议使用辅助工具";
        String lastUpdatedTime = TIME_FORMAT.format(new Date());

        return new GaitSummary(gaitSpeed, cadence, stepLength, symmetryScore, stabilityScore, fallRiskPercent,
                activityLabel, stabilityLevel, analysisSummary, fallDetectionMessage, lastUpdatedTime);
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
            @ColorRes int statusColor = isAlert ? R.color.colorWarning : R.color.colorSuccess;
            events.add(new GaitEvent(title, subtitle, statusLabel, statusColor));
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

        GaitSummary(float gaitSpeed, int cadence, float stepLength, int symmetryScore, int stabilityScore,
                    int fallRiskPercent, String activityLabel, String stabilityLevel, String analysisSummary,
                    String fallDetectionMessage, String lastUpdatedTime) {
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
        }
    }

    /**
     * 步态事件模型。
     */
    public static class GaitEvent {
        public final String title;
        public final String subtitle;
        public final String statusLabel;
        @ColorRes
        public final int statusColorRes;

        GaitEvent(String title, String subtitle, String statusLabel, @ColorRes int statusColorRes) {
            this.title = title;
            this.subtitle = subtitle;
            this.statusLabel = statusLabel;
            this.statusColorRes = statusColorRes;
        }
    }
}

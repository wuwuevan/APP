package com.example.myapplication.ui.gait;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.example.myapplication.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 为步态分析页面提供模拟数据，便于前端展示效果。所有数据均为随机生成的示例。 
 */
public final class GaitDataGenerator {

    private static final Random RANDOM = new Random();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    private GaitDataGenerator() {
        // Utility class
    }

    /**
     * 模拟请求后端接口，返回步态概览及事件列表。 
     */
    public static GaitSnapshot requestSnapshot(ActivityType type) {
        GaitSummary summary = buildSummary(type);
        List<GaitEvent> events = buildEvents(type);
        return new GaitSnapshot(summary, events);
    }

    private static GaitSummary buildSummary(ActivityType type) {
        float speedBase;
        int cadenceBase;
        float stepLengthBase;
        switch (type) {
            case STAIR_CLIMBING:
                speedBase = 0.68f;
                cadenceBase = 102;
                stepLengthBase = 0.46f;
                break;
            case OUTDOOR_ACTIVITY:
                speedBase = 1.18f;
                cadenceBase = 118;
                stepLengthBase = 0.67f;
                break;
            case WALKING:
            default:
                speedBase = 0.96f;
                cadenceBase = 108;
                stepLengthBase = 0.58f;
                break;
        }

        float gaitSpeed = round(speedBase + randomOffset(0.18f));
        int cadence = cadenceBase + RANDOM.nextInt(11) - 5;
        float stepLength = round(stepLengthBase + randomOffset(0.09f));
        int symmetryScore = clamp(80 + RANDOM.nextInt(12), 62, 100);
        int stabilityScore = clamp(72 + RANDOM.nextInt(20), 55, 98);
        int fallRiskPercent = clamp(100 - stabilityScore + RANDOM.nextInt(12), 6, 58);

        TrendInfo speedTrend = buildTrendInfo(2.8f, true);
        TrendInfo stepTrend = buildTrendInfo(2.0f, true);
        TrendInfo cadenceTrend = buildTrendInfo(3.5f, false);
        TrendInfo symmetryTrend = buildTrendInfo(1.8f, true);

        float supportPhase = round(clampFloat(60f + RANDOM.nextFloat() * 6f - 3f, 55f, 65f));
        float swingPhase = round(100f - supportPhase);
        float variability = round(clampFloat(2.2f + RANDOM.nextFloat() * 3.2f, 1.5f, 5.5f));

        String activityLabel;
        switch (type) {
            case STAIR_CLIMBING:
                activityLabel = "楼梯训练 · 上肢辅助";
                break;
            case OUTDOOR_ACTIVITY:
                activityLabel = "户外步行 · 公园道路";
                break;
            case WALKING:
            default:
                activityLabel = "室内步行 · 康复大厅";
                break;
        }

        String stabilityLevel = stabilityScore >= 85 ? "稳定度优秀" : stabilityScore >= 72 ? "轻度波动" : "需要重点关注";
        String analysisSummary = buildAnalysisSummary(type, gaitSpeed, symmetryScore, fallRiskPercent);
        String fallDetectionMessage = fallRiskPercent < 28 ? "未检测到明显跌倒风险，系统持续巡检中。" :
                fallRiskPercent < 45 ? "检测到轻度跌倒风险，建议佩戴防护装置。" :
                        "跌倒风险偏高，请立即联系康复师确认方案。";
        String lastUpdatedTime = TIME_FORMAT.format(new Date());

        List<String> insights = buildInsights(type, stabilityScore, symmetryScore, variability);
        String stabilityBadge = stabilityScore >= 85 ? "稳定状态 · 优" : stabilityScore >= 75 ? "稳定状态 · 良" : "稳定状态 · 待提升";
        String fallBadge = fallRiskPercent <= 25 ? "跌倒风险 · 低" : fallRiskPercent <= 45 ? "跌倒风险 · 中" : "跌倒风险 · 高";
        String fallRiskLabel = "当前风险：" + fallRiskPercent + "%";
        String supportHint = supportPhase >= 60 ? "支撑时长充足，重心控制稳定。" : "支撑略短，建议加强核心力量。";
        String swingHint = swingPhase >= 36 ? "摆动顺畅，跨步较为舒展。" : "摆动略短，关注脚尖抬起。";
        String variabilityHint = variability < 3.2f ? "步频稳定性良好，节律均衡。" : "步频略有波动，可加入节律训练。";

        return new GaitSummary(
                gaitSpeed,
                cadence,
                stepLength,
                symmetryScore,
                speedTrend,
                stepTrend,
                cadenceTrend,
                symmetryTrend,
                supportPhase,
                swingPhase,
                variability,
                supportHint,
                swingHint,
                variabilityHint,
                stabilityScore,
                fallRiskPercent,
                fallRiskLabel,
                stabilityLevel,
                stabilityBadge,
                fallBadge,
                analysisSummary,
                fallDetectionMessage,
                activityLabel,
                lastUpdatedTime,
                insights
        );
    }

    private static String buildAnalysisSummary(ActivityType type, float speed, int symmetryScore, int fallRiskPercent) {
        StringBuilder builder = new StringBuilder();
        builder.append("当前步速").append(DECIMAL_FORMAT.format(speed)).append(" m/s，");
        builder.append("对称性").append(symmetryScore).append("%，");
        if (type == ActivityType.STAIR_CLIMBING) {
            builder.append("楼梯段训练核心稳定性表现");
        } else if (type == ActivityType.OUTDOOR_ACTIVITY) {
            builder.append("户外路面抓地与步幅配合较好");
        } else {
            builder.append("室内康复训练节律平稳");
        }
        builder.append("，跌倒风险评估为");
        if (fallRiskPercent <= 25) {
            builder.append("低等级，可继续维持当前训练计划。");
        } else if (fallRiskPercent <= 45) {
            builder.append("中等级，建议加入平衡垫练习。");
        } else {
            builder.append("较高，需康复师重点干预。");
        }
        return builder.toString();
    }

    private static List<String> buildInsights(ActivityType type, int stabilityScore, int symmetryScore, float variability) {
        List<String> insights = new ArrayList<>();
        if (stabilityScore >= 85) {
            insights.add("骨盆控制稳定，支撑期姿态保持良好。");
        } else if (stabilityScore >= 75) {
            insights.add("稳定度略有波动，建议继续进行核心力量训练。");
        } else {
            insights.add("核心控制不足，可安排重点强化稳定训练。");
        }

        if (symmetryScore >= 88) {
            insights.add("左右步长差异小于 3%，对称性良好。");
        } else if (symmetryScore >= 80) {
            insights.add("左右步态尚需微调，可增加跨步协调练习。");
        } else {
            insights.add("检测到明显左右差异，建议康复师现场评估。");
        }

        if (variability < 3.0f) {
            insights.add("步频波动控制理想，可尝试提高训练强度。");
        } else {
            insights.add("节律略不稳定，考虑加入节拍器辅助训练。");
        }

        if (type == ActivityType.OUTDOOR_ACTIVITY && insights.size() > 2) {
            insights.set(0, "户外环境应激反应良好，未见异常支撑波动。");
        }
        return insights;
    }

    private static List<GaitEvent> buildEvents(ActivityType type) {
        List<GaitEvent> events = new ArrayList<>();
        int eventCount = 5 + RANDOM.nextInt(2);
        for (int i = 0; i < eventCount; i++) {
            long timeOffset = i * (6L + RANDOM.nextInt(4)) * 60 * 1000L;
            String timeStamp = TIME_FORMAT.format(new Date(System.currentTimeMillis() - timeOffset));

            boolean highAlert = RANDOM.nextInt(100) < (type == ActivityType.STAIR_CLIMBING ? 32 : 18);
            boolean warning = !highAlert && RANDOM.nextInt(100) < 28;

            String title;
            if (highAlert) {
                title = "姿态预警";
            } else if (warning) {
                title = "平衡提示";
            } else {
                title = "步态记录";
            }

            String subtitle;
            switch (type) {
                case STAIR_CLIMBING:
                    subtitle = timeStamp + " · 楼梯训练";
                    break;
                case OUTDOOR_ACTIVITY:
                    subtitle = timeStamp + " · 户外步行";
                    break;
                case WALKING:
                default:
                    subtitle = timeStamp + " · 室内步行";
                    break;
            }

            String detail;
            if (highAlert) {
                detail = "左脚支撑时间缩短 " + (4 + RANDOM.nextInt(4)) + "%";
            } else if (warning) {
                detail = "步速波动 " + DECIMAL_FORMAT.format(0.4f + RANDOM.nextFloat() * 0.8f) + " m/s";
            } else {
                detail = "节律稳定，步频维持在 " + (102 + RANDOM.nextInt(12)) + " 次/分";
            }

            String statusLabel;
            @ColorRes int statusColor;
            @DrawableRes int backgroundRes;
            @DrawableRes int iconRes;
            if (highAlert) {
                statusLabel = "需关注";
                statusColor = R.color.colorDanger;
                backgroundRes = R.drawable.bg_status_alert;
                iconRes = R.drawable.ic_warning;
            } else if (warning) {
                statusLabel = "提示";
                statusColor = R.color.colorWarning;
                backgroundRes = R.drawable.bg_status_warning;
                iconRes = R.drawable.ic_sync;
            } else {
                statusLabel = "正常";
                statusColor = R.color.colorSuccess;
                backgroundRes = R.drawable.bg_status_success;
                iconRes = R.drawable.ic_steps;
            }

            events.add(new GaitEvent(title, subtitle, detail, statusLabel, statusColor, backgroundRes, iconRes));
        }
        return events;
    }

    private static TrendInfo buildTrendInfo(float maxChange, boolean percent) {
        float delta = (RANDOM.nextFloat() * maxChange);
        if (RANDOM.nextBoolean()) {
            delta = -delta;
        }
        String valueText = percent ? String.format(Locale.getDefault(), "%+.1f%%", delta)
                : String.format(Locale.getDefault(), "%+.0f", delta);
        @ColorRes int colorRes;
        if (delta > 0.05f) {
            colorRes = R.color.colorSuccess;
        } else if (delta < -0.05f) {
            colorRes = R.color.colorDanger;
        } else {
            colorRes = R.color.colorTextSecondary;
        }
        return new TrendInfo(valueText, colorRes);
    }

    private static float randomOffset(float maxOffset) {
        return (RANDOM.nextFloat() * maxOffset) - (maxOffset / 2f);
    }

    private static float round(float value) {
        return Math.round(value * 100f) / 100f;
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** 活动类型。 */
    public enum ActivityType {
        WALKING,
        STAIR_CLIMBING,
        OUTDOOR_ACTIVITY
    }

    /** 趋势信息。 */
    public static class TrendInfo {
        public final String label;
        @ColorRes
        public final int colorRes;

        TrendInfo(String label, @ColorRes int colorRes) {
            this.label = label;
            this.colorRes = colorRes;
        }
    }

    /** 步态概览数据模型。 */
    public static class GaitSummary {
        public final float gaitSpeed;
        public final int cadence;
        public final float stepLength;
        public final int symmetryScore;
        public final TrendInfo speedTrend;
        public final TrendInfo stepTrend;
        public final TrendInfo cadenceTrend;
        public final TrendInfo symmetryTrend;
        public final float supportPhase;
        public final float swingPhase;
        public final float variability;
        public final String supportHint;
        public final String swingHint;
        public final String variabilityHint;
        public final int stabilityScore;
        public final int fallRiskPercent;
        public final String fallRiskLabel;
        public final String stabilityLevel;
        public final String stabilityBadge;
        public final String fallBadge;
        public final String analysisSummary;
        public final String fallDetectionMessage;
        public final String activityLabel;
        public final String lastUpdatedTime;
        public final List<String> insights;

        GaitSummary(float gaitSpeed,
                    int cadence,
                    float stepLength,
                    int symmetryScore,
                    TrendInfo speedTrend,
                    TrendInfo stepTrend,
                    TrendInfo cadenceTrend,
                    TrendInfo symmetryTrend,
                    float supportPhase,
                    float swingPhase,
                    float variability,
                    String supportHint,
                    String swingHint,
                    String variabilityHint,
                    int stabilityScore,
                    int fallRiskPercent,
                    String fallRiskLabel,
                    String stabilityLevel,
                    String stabilityBadge,
                    String fallBadge,
                    String analysisSummary,
                    String fallDetectionMessage,
                    String activityLabel,
                    String lastUpdatedTime,
                    List<String> insights) {
            this.gaitSpeed = gaitSpeed;
            this.cadence = cadence;
            this.stepLength = stepLength;
            this.symmetryScore = symmetryScore;
            this.speedTrend = speedTrend;
            this.stepTrend = stepTrend;
            this.cadenceTrend = cadenceTrend;
            this.symmetryTrend = symmetryTrend;
            this.supportPhase = supportPhase;
            this.swingPhase = swingPhase;
            this.variability = variability;
            this.supportHint = supportHint;
            this.swingHint = swingHint;
            this.variabilityHint = variabilityHint;
            this.stabilityScore = stabilityScore;
            this.fallRiskPercent = fallRiskPercent;
            this.fallRiskLabel = fallRiskLabel;
            this.stabilityLevel = stabilityLevel;
            this.stabilityBadge = stabilityBadge;
            this.fallBadge = fallBadge;
            this.analysisSummary = analysisSummary;
            this.fallDetectionMessage = fallDetectionMessage;
            this.activityLabel = activityLabel;
            this.lastUpdatedTime = lastUpdatedTime;
            this.insights = insights;
        }
    }

    /** 步态事件模型。 */
    public static class GaitEvent {
        public final String title;
        public final String subtitle;
        public final String detail;
        public final String statusLabel;
        @ColorRes
        public final int statusColorRes;
        @DrawableRes
        public final int statusBackgroundRes;
        @DrawableRes
        public final int iconRes;

        GaitEvent(String title,
                  String subtitle,
                  String detail,
                  String statusLabel,
                  @ColorRes int statusColorRes,
                  @DrawableRes int statusBackgroundRes,
                  @DrawableRes int iconRes) {
            this.title = title;
            this.subtitle = subtitle;
            this.detail = detail;
            this.statusLabel = statusLabel;
            this.statusColorRes = statusColorRes;
            this.statusBackgroundRes = statusBackgroundRes;
            this.iconRes = iconRes;
        }
    }

    /** 步态页面全部展示数据。 */
    public static class GaitSnapshot {
        public final GaitSummary summary;
        public final List<GaitEvent> events;

        GaitSnapshot(GaitSummary summary, List<GaitEvent> events) {
            this.summary = summary;
            this.events = events;
        }
    }
}

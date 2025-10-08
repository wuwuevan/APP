package com.example.myapplication.ui.gait;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 模拟后端接口，随机生成步态分析数据
 */
public final class GaitMockService {

    private static final Random RANDOM = new Random();
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");
    private static final DecimalFormat TWO_DECIMAL = new DecimalFormat("0.00");

    private GaitMockService() {
        // no-op
    }

    /**
     * 获取随机生成的步态看板数据
     */
    public static GaitDashboardData fetchGaitDashboard() {
        int score = 70 + RANDOM.nextInt(26);
        String status;
        String statusHint;
        if (score >= 85) {
            status = "步态稳定";
            statusHint = "保持当前节奏与训练频率";
        } else if (score >= 75) {
            status = "轻度波动";
            statusHint = "注意放慢节奏，关注左右对称";
        } else {
            status = "需重点关注";
            statusHint = "建议进行专项训练与评估";
        }

        double stepSpeed = 0.9 + RANDOM.nextDouble() * 0.6; // 0.9 - 1.5 m/s
        int cadence = 90 + RANDOM.nextInt(36); // 90 - 125 步/分
        double stride = 0.95 + RANDOM.nextDouble() * 0.45; // 0.95 - 1.4 m
        int symmetry = 82 + RANDOM.nextInt(15); // 82% - 96%
        double stance = 0.45 + RANDOM.nextDouble() * 0.35; // 0.45 - 0.8 s
        double variability = 1 + RANDOM.nextDouble() * 2.5; // 1 - 3.5%

        List<GaitMetric> metrics = new ArrayList<>();
        metrics.add(buildMetric("speed", format(TWO_DECIMAL, stepSpeed) + " m/s", randomTrend()));
        metrics.add(buildMetric("cadence", cadence + " 步/分", randomTrend()));
        metrics.add(buildMetric("stride", format(TWO_DECIMAL, stride) + " m", randomTrend()));
        metrics.add(buildMetric("symmetry", symmetry + " %", randomTrend()));
        metrics.add(buildMetric("stance", format(ONE_DECIMAL, stance) + " s", randomTrend()));
        metrics.add(buildMetric("variability", format(ONE_DECIMAL, variability) + " %", randomTrend()));

        List<ActivityPrediction> activities = buildActivities();
        List<String> insights = buildInsights(stepSpeed, symmetry, variability);
        List<GaitTimelineEvent> timelineEvents = buildTimeline(stepSpeed, cadence, symmetry);
        FallDetection fallDetection = buildFallDetection();

        double distance = 3.0 + RANDOM.nextDouble() * 4.0; // 3 - 7km
        int samples = 8 + RANDOM.nextInt(7);
        String summary = String.format(Locale.CHINA, "今日累计行走 %.1f 公里，记录 %d 次采样数据", distance, samples);

        return new GaitDashboardData(score, status, statusHint, summary, metrics, activities, insights, timelineEvents, fallDetection);
    }

    private static GaitMetric buildMetric(String id, String value, Trend trend) {
        String trendPrefix;
        switch (trend) {
            case IMPROVING:
                trendPrefix = "较昨日 +" + (2 + RANDOM.nextInt(5)) + "%";
                break;
            case DECLINING:
                trendPrefix = "较昨日 -" + (2 + RANDOM.nextInt(5)) + "%";
                break;
            default:
                trendPrefix = "保持稳定";
                break;
        }
        return new GaitMetric(id, value, trendPrefix, trend);
    }

    private static Trend randomTrend() {
        int roll = RANDOM.nextInt(100);
        if (roll < 45) {
            return Trend.IMPROVING;
        } else if (roll < 70) {
            return Trend.DECLINING;
        } else {
            return Trend.STABLE;
        }
    }

    private static List<ActivityPrediction> buildActivities() {
        String[] labels = new String[]{"室内走路", "户外快走", "爬楼梯", "慢跑", "下楼梯"};
        List<ActivityPrediction> activities = new ArrayList<>();
        for (String label : labels) {
            int confidence = 30 + RANDOM.nextInt(55); // 30 - 84
            activities.add(new ActivityPrediction(label, confidence, false));
        }
        Collections.sort(activities, new Comparator<ActivityPrediction>() {
            @Override
            public int compare(ActivityPrediction o1, ActivityPrediction o2) {
                return o2.getConfidence() - o1.getConfidence();
            }
        });
        if (!activities.isEmpty()) {
            ActivityPrediction top = activities.get(0);
            activities.set(0, new ActivityPrediction(top.getLabel(), top.getConfidence(), true));
        }
        return activities.subList(0, Math.min(3, activities.size()));
    }

    private static List<String> buildInsights(double speed, int symmetry, double variability) {
        List<String> insights = new ArrayList<>();
        if (speed > 1.2) {
            insights.add("步速高于平均水平，保持出行安全与补水");
        } else if (speed < 1.0) {
            insights.add("步速略低，建议进行轻量力量训练提升步频");
        } else {
            insights.add("步速处于健康区间，可继续保持当前节奏");
        }

        if (symmetry > 90) {
            insights.add("左右步态对称度良好，未发现明显偏差");
        } else {
            insights.add("左右步态对称度偏低，留意下肢力量均衡");
        }

        if (variability > 2.5) {
            insights.add("步态稳定性略有波动，建议在平坦区域进行训练");
        } else {
            insights.add("步态稳定性表现良好");
        }
        return insights;
    }

    private static List<GaitTimelineEvent> buildTimeline(double speed, int cadence, int symmetry) {
        List<GaitTimelineEvent> events = new ArrayList<>();
        events.add(new GaitTimelineEvent(
                "晨间热身走",
                "今天 · 08:15",
                String.format(Locale.CHINA, "步速 %s m/s · 步频 %d", format(TWO_DECIMAL, speed * 0.95), cadence - 6)));
        events.add(new GaitTimelineEvent(
                "午间康复训练",
                "今天 · 13:40",
                String.format(Locale.CHINA, "对称度 %d%% · 稳定性 %s%%", symmetry, format(ONE_DECIMAL, 1 + RANDOM.nextDouble() * 2))));
        events.add(new GaitTimelineEvent(
                "晚间室外散步",
                "今天 · 19:10",
                String.format(Locale.CHINA, "平均步速 %s m/s · 步频 %d", format(TWO_DECIMAL, speed), cadence)));
        return events;
    }

    private static FallDetection buildFallDetection() {
        boolean warning = RANDOM.nextBoolean() && RANDOM.nextBoolean();
        String status = warning ? "存在轻度风险" : "当前状态：稳定";
        int probability = warning ? 15 + RANDOM.nextInt(10) : 5 + RANDOM.nextInt(6);
        String probabilityText = "跌倒概率：" + probability + "%";
        String lastEvent = warning ? "上次异常：" + randomRecentTime() : "最近 7 天未检测到异常";
        String recommendation = warning
                ? "建议：增加核心力量训练并在室内铺设防滑垫"
                : "建议：维持规律作息，睡前做踝关节拉伸";
        return new FallDetection(status, probabilityText, lastEvent, recommendation, warning);
    }

    private static String randomRecentTime() {
        int day = 1 + RANDOM.nextInt(3);
        int hour = 6 + RANDOM.nextInt(12);
        int minute = RANDOM.nextInt(6) * 10;
        return String.format(Locale.CHINA, "%d 天前 · %02d:%02d", day, hour, minute);
    }

    private static String format(DecimalFormat format, double value) {
        return format.format(value);
    }

    /**
     * 步态看板核心数据
     */
    public static class GaitDashboardData {
        private final int score;
        private final String status;
        private final String statusHint;
        private final String summary;
        private final List<GaitMetric> metrics;
        private final List<ActivityPrediction> activities;
        private final List<String> insights;
        private final List<GaitTimelineEvent> timeline;
        private final FallDetection fallDetection;

        public GaitDashboardData(int score,
                                 String status,
                                 String statusHint,
                                 String summary,
                                 List<GaitMetric> metrics,
                                 List<ActivityPrediction> activities,
                                 List<String> insights,
                                 List<GaitTimelineEvent> timeline,
                                 FallDetection fallDetection) {
            this.score = score;
            this.status = status;
            this.statusHint = statusHint;
            this.summary = summary;
            this.metrics = metrics;
            this.activities = activities;
            this.insights = insights;
            this.timeline = timeline;
            this.fallDetection = fallDetection;
        }

        public int getScore() {
            return score;
        }

        public String getStatus() {
            return status;
        }

        public String getStatusHint() {
            return statusHint;
        }

        public String getSummary() {
            return summary;
        }

        public List<GaitMetric> getMetrics() {
            return metrics;
        }

        public List<ActivityPrediction> getActivities() {
            return activities;
        }

        public List<String> getInsights() {
            return insights;
        }

        public List<GaitTimelineEvent> getTimeline() {
            return timeline;
        }

        public FallDetection getFallDetection() {
            return fallDetection;
        }
    }

    /**
     * 单项指标数据
     */
    public static class GaitMetric {
        private final String id;
        private final String value;
        private final String trendText;
        private final Trend trend;

        public GaitMetric(String id, String value, String trendText, Trend trend) {
            this.id = id;
            this.value = value;
            this.trendText = trendText;
            this.trend = trend;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public String getTrendText() {
            return trendText;
        }

        public Trend getTrend() {
            return trend;
        }
    }

    /**
     * 活动识别概率
     */
    public static class ActivityPrediction {
        private final String label;
        private final int confidence;
        private final boolean primary;

        public ActivityPrediction(String label, int confidence, boolean primary) {
            this.label = label;
            this.confidence = confidence;
            this.primary = primary;
        }

        public String getLabel() {
            return label;
        }

        public int getConfidence() {
            return confidence;
        }

        public boolean isPrimary() {
            return primary;
        }
    }

    /**
     * 时间线事件
     */
    public static class GaitTimelineEvent {
        private final String title;
        private final String time;
        private final String description;

        public GaitTimelineEvent(String title, String time, String description) {
            this.title = title;
            this.time = time;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getTime() {
            return time;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 跌倒检测数据
     */
    public static class FallDetection {
        private final String status;
        private final String probabilityText;
        private final String lastEventText;
        private final String recommendation;
        private final boolean warning;

        public FallDetection(String status, String probabilityText, String lastEventText, String recommendation, boolean warning) {
            this.status = status;
            this.probabilityText = probabilityText;
            this.lastEventText = lastEventText;
            this.recommendation = recommendation;
            this.warning = warning;
        }

        public String getStatus() {
            return status;
        }

        public String getProbabilityText() {
            return probabilityText;
        }

        public String getLastEventText() {
            return lastEventText;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public boolean isWarning() {
            return warning;
        }
    }

    /**
     * 指标趋势
     */
    public enum Trend {
        IMPROVING,
        DECLINING,
        STABLE
    }
}

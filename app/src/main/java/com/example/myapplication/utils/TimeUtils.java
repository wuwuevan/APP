package com.example.myapplication.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类，提供相对时间显示。
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    /**
     * 根据时间戳返回相对时间描述。
     */
    @NonNull
    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60_000) {
            return "刚刚";
        } else if (diff < 60 * 60_000) {
            long minutes = Math.max(1, diff / 60_000);
            return minutes + " 分钟前";
        } else if (diff < 24 * 60 * 60_000) {
            long hours = Math.max(1, diff / (60 * 60_000));
            return hours + " 小时前";
        } else if (diff < 7L * 24 * 60 * 60_000) {
            long days = Math.max(1, diff / (24 * 60 * 60_000));
            return days + " 天前";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}

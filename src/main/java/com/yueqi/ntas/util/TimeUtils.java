package com.yueqi.ntas.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtils {
    // 时间转换相关
    public static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public static String formatDuration(int minutes) {
        int days = minutes / (24 * 60);
        int hours = (minutes % (24 * 60)) / 60;
        int mins = minutes % 60;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours, mins);
        }
        return String.format("%d小时%d分钟", hours, mins);
    }

    // 时间差计算
    public static int calculateTimeDifference(String time1, String time2) {
        int minutes1 = timeToMinutes(time1);
        int minutes2 = timeToMinutes(time2);
        
        if (minutes2 < minutes1) {
            minutes2 += 24 * 60; // 跨天处理
        }
        return minutes2 - minutes1;
    }
} 
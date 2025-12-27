package com.unicorn.backend.notification;

import java.time.LocalDateTime;

/**
 * DTO for notification statistics.
 */
public record NotificationStatsDTO(
        long total,
        long unread,
        long readCount,
        double readRate,
        long todayCount,
        String topNotificationType,
        long topTypeCount) {
    public static NotificationStatsDTO of(
            long total,
            long unread,
            long todayCount,
            String topType,
            long topTypeCount) {
        long readCount = total - unread;
        double readRate = total > 0 ? (double) readCount / total * 100 : 0;
        return new NotificationStatsDTO(
                total,
                unread,
                readCount,
                Math.round(readRate * 100.0) / 100.0, // Round to 2 decimal places
                todayCount,
                topType,
                topTypeCount);
    }
}

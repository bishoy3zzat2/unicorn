package com.unicorn.backend.admin;

import java.util.Map;

public record SecurityStats(
        long totalTokens,
        long activeSessions,
        long expiredTokens,
        long onlineUsers,
        Map<String, Long> deviceStats,
        Map<String, Long> activityTrend) {
}

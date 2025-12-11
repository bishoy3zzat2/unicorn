package com.loyalixa.backend.auth;
import java.time.LocalDateTime;
import java.util.UUID;
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String username,
        UUID userId,
        SuspensionBanInfo suspensionBanInfo,
        Boolean canAccessDashboard) {
    public record SuspensionBanInfo(
            String action,
            String reason,
            LocalDateTime actionAt,
            LocalDateTime until,
            String type) {
    }
}
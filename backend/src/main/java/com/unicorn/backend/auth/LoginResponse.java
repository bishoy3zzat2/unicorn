package com.unicorn.backend.auth;

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
            java.time.LocalDateTime actionAt,
            java.time.LocalDateTime until,
            String type,
            Boolean isTemporary) {
    }
}

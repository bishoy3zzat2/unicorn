package com.unicorn.backend.admin;

import com.unicorn.backend.user.ModerationActionType;
import com.unicorn.backend.user.UserModerationLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for moderation log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationLogResponse {
    private UUID id;
    private ModerationActionType actionType;
    private String reason;
    private String durationType;
    private LocalDateTime expiresAt;
    private String previousStatus;
    private String newStatus;
    private boolean isActive;

    // Admin info
    private UUID adminId;
    private String adminEmail;

    // Revocation info
    private LocalDateTime revokedAt;
    private UUID revokedBy;
    private String revokeReason;

    private LocalDateTime createdAt;

    public static ModerationLogResponse fromEntity(UserModerationLog log) {
        return ModerationLogResponse.builder()
                .id(log.getId())
                .actionType(log.getActionType())
                .reason(log.getReason())
                .durationType(log.getDurationType())
                .expiresAt(log.getExpiresAt())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .isActive(log.getIsActive())
                .adminId(log.getAdminId())
                .adminEmail(log.getAdminEmail())
                .revokedAt(log.getRevokedAt())
                .revokedBy(log.getRevokedBy())
                .revokeReason(log.getRevokeReason())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

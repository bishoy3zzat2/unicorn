package com.unicorn.backend.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to track user moderation history (warnings, suspensions, bans).
 * Provides complete audit trail for admin actions on users.
 */
@Entity
@Table(name = "user_moderation_logs", indexes = {
        @Index(name = "idx_moderation_user", columnList = "user_id"),
        @Index(name = "idx_moderation_type", columnList = "action_type"),
        @Index(name = "idx_moderation_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "admin_id")
    private UUID adminId;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(name = "action_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ModerationActionType actionType;

    @Column(name = "reason", length = 2000)
    private String reason;

    @Column(name = "duration_type", length = 20)
    private String durationType; // PERMANENT, TEMPORARY, or null for warnings

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "previous_status", length = 20)
    private String previousStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by")
    private UUID revokedBy;

    @Column(name = "revoke_reason")
    private String revokeReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

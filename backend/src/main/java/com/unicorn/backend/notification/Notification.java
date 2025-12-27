package com.unicorn.backend.notification;

import com.unicorn.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a notification sent to a user or broadcast to all.
 * Stores notification content, metadata, and read status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_notification_broadcast", columnList = "is_broadcast, target_audience")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Recipient for individual notifications. NULL for broadcasts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    /**
     * JSON string containing flexible metadata.
     */
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "actor_avatar_url", length = 500)
    private String actorAvatarUrl;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    // ============== Broadcast Support ==============

    /**
     * If true, this is a broadcast notification visible to all matching users.
     * recipient field will be NULL for broadcasts.
     */
    @Column(name = "is_broadcast", nullable = false)
    @Builder.Default
    private boolean broadcast = false;

    /**
     * Target audience for broadcast notifications.
     * ALL_USERS, INVESTORS_ONLY, STARTUP_OWNERS_ONLY
     */
    @Column(name = "target_audience", length = 30)
    private String targetAudience;
}

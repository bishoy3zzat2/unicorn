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
 * Tracks which users have dismissed broadcast notifications.
 * This allows broadcasts to have 1 record while still tracking dismissals per
 * user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "broadcast_dismissals", indexes = {
        @Index(name = "idx_broadcast_dismissal_user", columnList = "user_id"),
        @Index(name = "idx_broadcast_dismissal_notification", columnList = "notification_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_broadcast_dismissal", columnNames = { "user_id", "notification_id" })
})
public class BroadcastDismissal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @CreationTimestamp
    @Column(name = "dismissed_at", nullable = false, updatable = false)
    private LocalDateTime dismissedAt;
}

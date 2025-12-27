package com.unicorn.backend.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for notification persistence and queries.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find notifications for a user, ordered by creation date descending.
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    /**
     * Find unread notifications for a user.
     */
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(UUID recipientId);

    /**
     * Count unread notifications for a user.
     */
    long countByRecipientIdAndReadFalse(UUID recipientId);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.recipient.id = :recipientId AND n.read = false")
    int markAllAsRead(@Param("recipientId") UUID recipientId, @Param("readAt") LocalDateTime readAt);

    /**
     * Find notifications by type for a user.
     */
    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(
            UUID recipientId,
            NotificationType type,
            Pageable pageable);

    /**
     * Delete old read notifications (for cleanup jobs).
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.read = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ============== Admin Queries ==============

    /**
     * Find all notifications (admin), ordered by creation date descending.
     */
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find notifications by type (admin).
     */
    Page<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type, Pageable pageable);

    /**
     * Find notifications by read status (admin).
     */
    Page<Notification> findByReadOrderByCreatedAtDesc(boolean read, Pageable pageable);

    /**
     * Find notifications by type and read status (admin).
     */
    Page<Notification> findByTypeAndReadOrderByCreatedAtDesc(NotificationType type, boolean read, Pageable pageable);

    /**
     * Count all unread notifications globally.
     */
    long countByReadFalse();

    /**
     * Count notifications created today.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :startOfDay")
    long countCreatedAfter(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Find the most common notification type.
     */
    @Query("SELECT n.type, COUNT(n) as cnt FROM Notification n GROUP BY n.type ORDER BY cnt DESC")
    List<Object[]> findTopNotificationTypes();

    /**
     * Count notifications by type.
     */
    long countByType(NotificationType type);

    // ============== Broadcast Queries ==============

    /**
     * Find all broadcast notifications matching user's role.
     */
    @Query("SELECT n FROM Notification n WHERE n.broadcast = true AND " +
            "(n.targetAudience = 'ALL_USERS' OR n.targetAudience = :userRole) " +
            "AND n.id NOT IN :dismissedIds ORDER BY n.createdAt DESC")
    List<Notification> findBroadcastsForUser(
            @Param("userRole") String userRole,
            @Param("dismissedIds") List<UUID> dismissedIds);

    /**
     * Find all broadcasts (admin view).
     */
    List<Notification> findByBroadcastTrueOrderByCreatedAtDesc();

    /**
     * Count broadcasts.
     */
    long countByBroadcastTrue();

    /**
     * Count individual (non-broadcast) notifications.
     */
    long countByBroadcastFalse();
}

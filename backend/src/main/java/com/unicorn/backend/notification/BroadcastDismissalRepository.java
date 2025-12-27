package com.unicorn.backend.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for broadcast dismissal tracking.
 */
@Repository
public interface BroadcastDismissalRepository extends JpaRepository<BroadcastDismissal, UUID> {

    /**
     * Check if user has dismissed a specific broadcast.
     */
    boolean existsByUserIdAndNotificationId(UUID userId, UUID notificationId);

    /**
     * Get all notification IDs dismissed by a user.
     */
    @Query("SELECT bd.notification.id FROM BroadcastDismissal bd WHERE bd.user.id = :userId")
    List<UUID> findDismissedNotificationIdsByUserId(@Param("userId") UUID userId);

    /**
     * Count dismissals for a broadcast.
     */
    long countByNotificationId(UUID notificationId);
}

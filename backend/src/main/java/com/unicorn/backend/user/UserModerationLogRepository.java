package com.unicorn.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for user moderation logs.
 */
@Repository
public interface UserModerationLogRepository extends JpaRepository<UserModerationLog, UUID> {

    /**
     * Find all moderation logs for a user, ordered by most recent first.
     */
    List<UserModerationLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find active moderation logs for a user (not revoked).
     */
    List<UserModerationLog> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);

    /**
     * Find logs by action type for a user.
     */
    List<UserModerationLog> findByUserIdAndActionTypeOrderByCreatedAtDesc(UUID userId, ModerationActionType actionType);

    /**
     * Count warnings for a user.
     */
    long countByUserIdAndActionTypeAndIsActiveTrue(UUID userId, ModerationActionType actionType);

    /**
     * Find current active suspension for a user.
     */
    UserModerationLog findFirstByUserIdAndActionTypeAndIsActiveTrueOrderByCreatedAtDesc(UUID userId,
            ModerationActionType actionType);
}

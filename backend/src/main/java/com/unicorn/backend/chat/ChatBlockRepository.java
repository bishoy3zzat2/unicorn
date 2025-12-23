package com.unicorn.backend.chat;

import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ChatBlock entity operations.
 */
@Repository
public interface ChatBlockRepository extends JpaRepository<ChatBlock, UUID> {

    /**
     * Check if user1 has blocked user2.
     */
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    /**
     * Find a specific block relationship.
     */
    Optional<ChatBlock> findByBlockerAndBlocked(User blocker, User blocked);

    /**
     * Find all users blocked by a specific user.
     */
    List<ChatBlock> findByBlocker(User blocker);

    /**
     * Find all blocks involving a user (either as blocker or blocked).
     */
    List<ChatBlock> findByBlockerOrBlocked(User user1, User user2);
}

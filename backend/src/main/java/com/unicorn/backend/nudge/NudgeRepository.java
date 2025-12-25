package com.unicorn.backend.nudge;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Nudge entity with queries for limit and cooldown checks.
 */
@Repository
public interface NudgeRepository extends JpaRepository<Nudge, Long> {

    /**
     * Count nudges sent by a user after a given date (for monthly limit).
     */
    long countBySenderAndCreatedAtAfter(User sender, LocalDateTime after);

    /**
     * Check if a sender has ever nudged a specific receiver (for FREE plan).
     */
    boolean existsBySenderAndReceiver(User sender, User receiver);

    /**
     * Find the most recent nudge from sender to receiver (for cooldown check).
     */
    Optional<Nudge> findTopBySenderAndReceiverOrderByCreatedAtDesc(User sender, User receiver);

    /**
     * Get all nudges sent by a user (for admin dashboard).
     */
    List<Nudge> findBySenderOrderByCreatedAtDesc(User sender);

    /**
     * Get all nudges received by a user (for admin dashboard).
     */
    List<Nudge> findByReceiverOrderByCreatedAtDesc(User receiver);

    /**
     * Count nudges received by a user.
     */
    long countByReceiver(User receiver);

    /**
     * Count nudges sent by a user in the current month.
     */
    @Query("SELECT COUNT(n) FROM Nudge n WHERE n.sender = :sender AND n.createdAt >= :startOfMonth")
    long countSentThisMonth(@Param("sender") User sender, @Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * Get all nudges for a specific startup (for admin dashboard).
     */
    List<Nudge> findByStartupIdOrderByCreatedAtDesc(UUID startupId);

    /**
     * Count nudges for a specific startup.
     */
    long countByStartupId(UUID startupId);
}

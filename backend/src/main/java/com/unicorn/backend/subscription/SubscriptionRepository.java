package com.unicorn.backend.subscription;

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
 * Repository for Subscription entity operations.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find all subscriptions for a user ordered by creation date descending.
     */
    List<Subscription> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find the active subscription for a user.
     */
    Optional<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);

    /**
     * Find active subscription by user ID.
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Count subscriptions by plan type.
     */
    long countByPlanType(SubscriptionPlan planType);

    /**
     * Count subscriptions by status.
     */
    long countByStatus(SubscriptionStatus status);

    /**
     * Count subscriptions by plan type and status.
     */
    long countByPlanTypeAndStatus(SubscriptionPlan planType, SubscriptionStatus status);

    /**
     * Find subscriptions created between dates (for MRR calculation).
     */
    @Query("SELECT s FROM Subscription s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<Subscription> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get total revenue for a month.
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subscription s " +
            "WHERE s.status = 'ACTIVE' AND s.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueForPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

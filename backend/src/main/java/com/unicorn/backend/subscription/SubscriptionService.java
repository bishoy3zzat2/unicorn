package com.unicorn.backend.subscription;

import com.unicorn.backend.user.ModerationActionType;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserModerationLog;
import com.unicorn.backend.user.UserModerationLogRepository;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing user subscriptions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final UserModerationLogRepository moderationLogRepository;

    /**
     * Revoke a user's subscription (for refund cases).
     * Downgrades the user to FREE plan and logs the action.
     * 
     * @param userId     The user whose subscription to revoke
     * @param adminId    The admin performing the action
     * @param adminEmail The admin's email for audit trail
     * @param reason     The reason for revocation
     * @return The cancelled subscription, or null if no active subscription
     */
    @Transactional
    public Subscription revokeSubscription(UUID userId, UUID adminId, String adminEmail, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Find and cancel active subscription
        Subscription activeSubscription = subscriptionRepository.findActiveByUserId(userId)
                .orElse(null);

        String previousPlan = "FREE";
        if (activeSubscription != null) {
            previousPlan = activeSubscription.getPlanType().name();
            activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
            activeSubscription.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(activeSubscription);
            log.info("Revoked {} subscription for user: {}", previousPlan, userId);
        } else {
            log.warn("No active subscription found for user: {}", userId);
        }

        // Log the moderation action
        UserModerationLog logEntry = UserModerationLog.builder()
                .user(user)
                .adminId(adminId)
                .adminEmail(adminEmail)
                .actionType(ModerationActionType.SUBSCRIPTION_REVOKED)
                .reason(reason != null ? reason : "Subscription revoked due to refund")
                .previousStatus(previousPlan)
                .newStatus("FREE")
                .isActive(true)
                .build();

        moderationLogRepository.save(logEntry);
        log.info("Subscription revocation logged for user: {} by admin: {}", userId, adminEmail);

        return activeSubscription;
    }

    /**
     * Subscription duration options.
     */
    public enum SubscriptionDuration {
        MONTHLY(1),
        YEARLY(12);

        private final int months;

        SubscriptionDuration(int months) {
            this.months = months;
        }

        public int getMonths() {
            return months;
        }
    }

    /**
     * Create a new subscription for a user with specified duration.
     * 
     * @param userId   The user ID
     * @param plan     The subscription plan (PRO or ELITE)
     * @param duration The subscription duration (MONTHLY or YEARLY)
     * @param amount   The payment amount
     * @return The created subscription
     */
    @Transactional
    public Subscription createSubscription(UUID userId, SubscriptionPlan plan, SubscriptionDuration duration,
            BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Cancel any existing active subscription
        subscriptionRepository.findActiveByUserId(userId).ifPresent(existing -> {
            existing.setStatus(SubscriptionStatus.CANCELLED);
            existing.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(existing);
        });

        // Calculate end date based on duration
        LocalDateTime endDate = LocalDateTime.now().plusMonths(duration.getMonths());

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(plan)
                .startDate(LocalDateTime.now())
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .amount(amount)
                .currency("EGP")
                .build();

        log.info("Created {} subscription for user: {}, plan: {}, expires: {}",
                duration, userId, plan, endDate);

        return subscriptionRepository.save(subscription);
    }

    /**
     * Create a new subscription for a user (defaults to MONTHLY for backward
     * compatibility).
     */
    @Transactional
    public Subscription createSubscription(UUID userId, SubscriptionPlan plan, BigDecimal amount) {
        return createSubscription(userId, plan, SubscriptionDuration.MONTHLY, amount);
    }

    /**
     * Get the active subscription for a user.
     */
    public Subscription getActiveSubscription(UUID userId) {
        return subscriptionRepository.findActiveByUserId(userId).orElse(null);
    }

    /**
     * Get subscription history for a user.
     */
    public List<Subscription> getSubscriptionHistory(User user) {
        return subscriptionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Cancel a subscription.
     */
    @Transactional
    public Subscription cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    /**
     * Get subscription statistics for dashboard.
     */
    public Map<String, Object> getSubscriptionStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total by plan
        Map<String, Long> byPlan = new HashMap<>();
        byPlan.put("FREE",
                subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE));
        byPlan.put("PRO",
                subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.PRO, SubscriptionStatus.ACTIVE));
        byPlan.put("ELITE",
                subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.ELITE, SubscriptionStatus.ACTIVE));

        stats.put("byPlan", byPlan);
        stats.put("totalSubscriptions", subscriptionRepository.count());
        stats.put("activeSubscriptions", subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE));

        return stats;
    }

    /**
     * Calculate MRR (Monthly Recurring Revenue).
     */
    public BigDecimal calculateMRR() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        BigDecimal revenue = subscriptionRepository.getTotalRevenueForPeriod(startOfMonth, endOfMonth);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}

package com.unicorn.backend.subscription;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
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
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Create a new subscription for a user.
     */
    @Transactional
    public Subscription createSubscription(UUID userId, SubscriptionPlan plan, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Cancel any existing active subscription
        subscriptionRepository.findActiveByUserId(userId).ifPresent(existing -> {
            existing.setStatus(SubscriptionStatus.CANCELLED);
            existing.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(existing);
        });

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(plan)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .amount(amount)
                .currency("EGP")
                .build();

        return subscriptionRepository.save(subscription);
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

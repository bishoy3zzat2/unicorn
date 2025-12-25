package com.unicorn.backend.subscription;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.unicorn.backend.config.GooglePlayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Service responsible for checking and processing subscription auto-renewals.
 * Runs as a scheduled job to poll Google Play for subscription status updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoRenewService {

    private final SubscriptionRepository subscriptionRepository;
    private final AndroidPublisher androidPublisher;
    private final GooglePlayConfig googlePlayConfig;

    /**
     * Scheduled task that runs daily at 2:00 AM to check subscription renewals.
     * 
     * This method:
     * 1. Finds all active subscriptions expiring within the next 24 hours
     * 2. Polls Google Play API for each subscription's current status
     * 3. Updates subscription records based on Google's response
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkSubscriptionRenewals() {
        log.info("Starting scheduled subscription renewal check");

        try {
            // Find subscriptions expiring in the next 24 hours
            LocalDateTime checkThreshold = LocalDateTime.now().plusDays(1);
            List<Subscription> expiringSubscriptions = subscriptionRepository
                    .findActiveSubscriptionsExpiringBefore(checkThreshold);

            log.info("Found {} subscriptions to check for renewal", expiringSubscriptions.size());

            int renewed = 0;
            int cancelled = 0;
            int errors = 0;

            for (Subscription subscription : expiringSubscriptions) {
                try {
                    boolean wasRenewed = processSubscriptionRenewal(subscription);
                    if (wasRenewed) {
                        renewed++;
                    } else {
                        cancelled++;
                    }
                } catch (Exception e) {
                    log.error("Error processing renewal for subscription {}: {}",
                            subscription.getId(), e.getMessage());
                    errors++;
                }
            }

            log.info("Subscription renewal check completed. Renewed: {}, Cancelled: {}, Errors: {}",
                    renewed, cancelled, errors);

        } catch (Exception e) {
            log.error("Error during subscription renewal check", e);
        }
    }

    /**
     * Processes a single subscription renewal by checking with Google Play.
     * 
     * @param subscription The subscription to check
     * @return true if subscription was renewed, false if cancelled/expired
     */
    private boolean processSubscriptionRenewal(Subscription subscription) throws IOException {
        String purchaseToken = subscription.getGooglePlayPurchaseToken();

        if (purchaseToken == null || purchaseToken.isEmpty()) {
            log.warn("Subscription {} has no Google Play purchase token, skipping", subscription.getId());
            return false;
        }

        // Determine the subscription ID based on plan type
        String subscriptionId = getSubscriptionIdForPlan(subscription.getPlanType());

        try {
            SubscriptionPurchase purchase = androidPublisher
                    .purchases()
                    .subscriptions()
                    .get(googlePlayConfig.getPackageName(), subscriptionId, purchaseToken)
                    .execute();

            return handleGooglePlayResponse(subscription, purchase);

        } catch (IOException e) {
            log.error("Failed to verify renewal for subscription {}: {}", subscription.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Handles the response from Google Play and updates the subscription
     * accordingly.
     * 
     * @param subscription The local subscription record
     * @param purchase     The purchase details from Google Play
     * @return true if subscription was renewed, false otherwise
     */
    private boolean handleGooglePlayResponse(Subscription subscription, SubscriptionPurchase purchase) {
        // Check if subscription was cancelled
        Integer cancelReason = purchase.getCancelReason();
        if (cancelReason != null) {
            log.info("Subscription {} has been cancelled by user. Reason: {}", subscription.getId(), cancelReason);
            markSubscriptionCancelled(subscription);
            return false;
        }

        // Check payment state (1 = Payment received)
        Integer paymentState = purchase.getPaymentState();
        if (paymentState == null || paymentState != 1) {
            log.warn("Subscription {} has invalid payment state: {}", subscription.getId(), paymentState);
            return false;
        }

        // Get new expiry date
        Long expiryTimeMillis = purchase.getExpiryTimeMillis();
        LocalDateTime newExpiryDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(expiryTimeMillis),
                ZoneId.systemDefault());

        // Check if subscription has been renewed (new expiry is after current expiry)
        if (newExpiryDate.isAfter(subscription.getEndDate())) {
            log.info("Subscription {} has been renewed. New expiry: {}", subscription.getId(), newExpiryDate);
            updateSubscriptionExpiry(subscription, newExpiryDate);
            return true;
        }

        // Check if subscription has expired
        if (newExpiryDate.isBefore(LocalDateTime.now())) {
            log.info("Subscription {} has expired", subscription.getId());
            markSubscriptionExpired(subscription);
            return false;
        }

        // No change - subscription is still active
        log.debug("Subscription {} unchanged. Current expiry: {}", subscription.getId(), subscription.getEndDate());
        return false;
    }

    /**
     * Updates the subscription expiry date after successful renewal.
     */
    private void updateSubscriptionExpiry(Subscription subscription, LocalDateTime newExpiryDate) {
        subscription.setEndDate(newExpiryDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
    }

    /**
     * Marks a subscription as cancelled.
     */
    private void markSubscriptionCancelled(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }

    /**
     * Marks a subscription as expired.
     */
    private void markSubscriptionExpired(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(subscription);
    }

    /**
     * Maps a subscription plan to its Google Play product ID.
     * Defaults to monthly subscription for simplicity.
     */
    private String getSubscriptionIdForPlan(SubscriptionPlan plan) {
        return switch (plan) {
            case PRO -> "pro_monthly";
            case ELITE -> "elite_monthly";
            default -> throw new IllegalArgumentException("No subscription ID for plan: " + plan);
        };
    }

    /**
     * Manual trigger for subscription renewal check.
     * Useful for testing or administrative purposes.
     */
    public void triggerManualRenewalCheck() {
        log.info("Manual subscription renewal check triggered");
        checkSubscriptionRenewals();
    }
}

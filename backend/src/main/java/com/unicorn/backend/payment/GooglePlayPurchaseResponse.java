package com.unicorn.backend.payment;

import com.unicorn.backend.subscription.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Google Play purchase verification responses.
 * Returns the result of the verification process to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GooglePlayPurchaseResponse {

    /**
     * Indicates whether the purchase verification was successful.
     */
    private boolean success;

    /**
     * Human-readable message describing the verification result.
     */
    private String message;

    /**
     * The subscription plan that was activated (PRO, ELITE).
     * Null if verification failed.
     */
    private SubscriptionPlan subscriptionPlan;

    /**
     * The expiration date/time of the subscription.
     * Null if verification failed.
     */
    private LocalDateTime expiryDate;

    /**
     * The transaction ID for the payment record.
     * Null if verification failed.
     */
    private String transactionId;

    /**
     * Creates a success response.
     */
    public static GooglePlayPurchaseResponse success(SubscriptionPlan plan, LocalDateTime expiryDate,
            String transactionId) {
        return GooglePlayPurchaseResponse.builder()
                .success(true)
                .message("Subscription activated successfully")
                .subscriptionPlan(plan)
                .expiryDate(expiryDate)
                .transactionId(transactionId)
                .build();
    }

    /**
     * Creates a failure response.
     */
    public static GooglePlayPurchaseResponse failure(String message) {
        return GooglePlayPurchaseResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}

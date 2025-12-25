package com.unicorn.backend.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for Google Play purchase verification requests.
 * Contains all information needed to verify a subscription purchase with Google
 * Play.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GooglePlayPurchaseRequest {

    /**
     * The purchase token provided by Google Play after a successful purchase.
     * This token is used to verify the purchase with Google's servers.
     */
    @NotBlank(message = "Purchase token is required")
    private String purchaseToken;

    /**
     * The subscription product ID as configured in Google Play Console.
     * Example: "pro_monthly", "elite_yearly"
     */
    @NotBlank(message = "Subscription ID is required")
    private String subscriptionId;

    /**
     * The ID of the user making the purchase.
     * Used to associate the subscription with the correct user account.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;
}

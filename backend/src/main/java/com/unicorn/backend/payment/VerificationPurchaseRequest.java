package com.unicorn.backend.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for Google Play one-time purchase verification (investor verification
 * fee).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPurchaseRequest {

    /**
     * The purchase token provided by Google Play after a successful purchase.
     */
    @NotBlank(message = "Purchase token is required")
    private String purchaseToken;

    /**
     * The product ID as configured in Google Play Console.
     * Example: "investor_verification_fee"
     */
    @NotBlank(message = "Product ID is required")
    private String productId;

    /**
     * The ID of the user making the purchase.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;
}

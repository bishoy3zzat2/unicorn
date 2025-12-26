package com.unicorn.backend.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for one-time verification purchase verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPurchaseResponse {

    private boolean success;
    private String message;
    private String transactionId;
    private LocalDateTime verifiedAt;
    private BigDecimal amount;
    private String currency;

    /**
     * Create a success response.
     */
    public static VerificationPurchaseResponse success(
            String transactionId,
            LocalDateTime verifiedAt,
            BigDecimal amount,
            String currency) {
        return VerificationPurchaseResponse.builder()
                .success(true)
                .message("Verification completed successfully")
                .transactionId(transactionId)
                .verifiedAt(verifiedAt)
                .amount(amount)
                .currency(currency)
                .build();
    }

    /**
     * Create a failure response.
     */
    public static VerificationPurchaseResponse failure(String message) {
        return VerificationPurchaseResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}

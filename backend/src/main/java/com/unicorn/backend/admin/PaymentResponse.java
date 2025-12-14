package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private String userEmail;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private String paymentMethod;
    private LocalDateTime timestamp;
}

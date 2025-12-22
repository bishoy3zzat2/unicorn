package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for investor verification queue response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestorVerificationResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String userAvatar;
    private String bio;
    private BigDecimal investmentBudget;
    private String preferredIndustries;
    private String linkedInUrl;
    private LocalDateTime verificationRequestedAt;
    private Boolean readyForPayment;
}

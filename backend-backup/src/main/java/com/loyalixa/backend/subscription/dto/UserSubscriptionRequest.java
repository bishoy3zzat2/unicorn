package com.loyalixa.backend.subscription.dto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
public record UserSubscriptionRequest(
    @NotNull(message = "Plan ID is required")
    UUID planId,
    @NotNull(message = "User ID is required")
    UUID userId,
    LocalDateTime startDate,  
    LocalDateTime endDate,  
    Boolean autoRenew,
    String paymentReference,
    String notes
) {}

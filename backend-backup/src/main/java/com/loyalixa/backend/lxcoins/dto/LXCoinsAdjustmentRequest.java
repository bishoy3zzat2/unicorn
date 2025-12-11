package com.loyalixa.backend.lxcoins.dto;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
public record LXCoinsAdjustmentRequest(
    @NotNull(message = "User ID is required.")
    UUID userId,
    @NotNull(message = "Amount is required.")
    BigDecimal amount,
    String description,
    String transactionType  
) {}

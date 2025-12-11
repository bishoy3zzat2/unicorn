package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
public record FinancialAdjustmentRequest(
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,
    @NotBlank(message = "Transaction type is required")
    String transactionType,  
    @NotBlank(message = "Description is required")
    String description,
    String notes
) {}

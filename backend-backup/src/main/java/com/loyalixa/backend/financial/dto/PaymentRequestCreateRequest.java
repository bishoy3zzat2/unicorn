package com.loyalixa.backend.financial.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record PaymentRequestCreateRequest(
    @NotBlank
    String requestType,  
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount,
    String reason,
    String paymentMethod,  
    String paymentDetails  
) {}

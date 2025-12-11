package com.loyalixa.backend.marketing.dto;
import jakarta.validation.constraints.NotBlank;
public record RedeemRequest(
    @NotBlank(message = "Voucher code cannot be empty.")
    String voucherCode  
) {}
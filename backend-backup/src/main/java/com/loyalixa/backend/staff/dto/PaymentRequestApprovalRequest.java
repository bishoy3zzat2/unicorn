package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.NotBlank;
public record PaymentRequestApprovalRequest(
    @NotBlank(message = "Status is required")
    String status,  
    String rejectionReason
) {}

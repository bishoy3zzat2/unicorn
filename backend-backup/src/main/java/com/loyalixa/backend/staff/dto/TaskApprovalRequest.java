package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.NotBlank;
public record TaskApprovalRequest(
    @NotBlank(message = "Status is required")
    String status,  
    String rejectionReason,
    String notes
) {}

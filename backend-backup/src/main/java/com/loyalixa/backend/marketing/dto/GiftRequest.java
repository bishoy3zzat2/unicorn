package com.loyalixa.backend.marketing.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record GiftRequest(
    @NotBlank
    UUID courseId,  
    @NotBlank
    @Email
    String recipientEmail  
) {}
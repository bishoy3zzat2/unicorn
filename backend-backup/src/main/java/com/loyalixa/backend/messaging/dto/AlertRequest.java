package com.loyalixa.backend.messaging.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record AlertRequest(
    @NotBlank
    UUID recipientId,  
    @NotBlank
    @Size(max = 255)
    String subject,
    @NotBlank
    String content,
    @NotBlank
    String alertType  
) {}
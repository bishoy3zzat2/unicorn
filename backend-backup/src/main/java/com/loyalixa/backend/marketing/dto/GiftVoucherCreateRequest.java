package com.loyalixa.backend.marketing.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.UUID;
public record GiftVoucherCreateRequest(
    @NotNull(message = "Course ID is required")
    UUID courseId,  
    @NotNull(message = "Sender ID is required")
    UUID senderId,  
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    String recipientEmail,  
    String voucherCode,  
    String status,  
    String description,  
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime expirationDate  
) {}

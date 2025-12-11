package com.loyalixa.backend.marketing.dto;
import jakarta.validation.constraints.Email;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.UUID;
public record GiftVoucherUpdateRequest(
    UUID courseId,  
    UUID senderId,  
    @Email(message = "Invalid email format")
    String recipientEmail,  
    String voucherCode,  
    String status,  
    String description,  
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime expirationDate  
) {}

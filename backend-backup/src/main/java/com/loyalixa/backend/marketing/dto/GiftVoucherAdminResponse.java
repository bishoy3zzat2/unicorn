package com.loyalixa.backend.marketing.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record GiftVoucherAdminResponse(
    UUID id,
    String voucherCode,
    String recipientEmail,
    String status,  
    LocalDateTime issuedAt,
    LocalDateTime redeemedAt,
    LocalDateTime expirationDate,  
    String description,  
    CourseInfo course,
    UserInfo sender,
    UserInfo redeemer
) {
    public record CourseInfo(UUID id, String title, String slug) {}
    public record UserInfo(UUID id, String email, String username) {}
}

package com.loyalixa.backend.marketing.dto;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.UUID;
public record GiftVoucherSearchRequest(
    String voucherCode,
    String recipientEmail,
    String status,  
    UUID courseId,
    UUID senderId,
    UUID redeemerId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime issuedFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime issuedTo,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime redeemedFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime redeemedTo
) {}

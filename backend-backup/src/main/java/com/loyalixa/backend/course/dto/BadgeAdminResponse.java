package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.Duration;
public record BadgeAdminResponse(
    UUID id,
    String name,
    String colorCode,  
    String customCss,  
    Double weight,  
    String targetType,
    LocalDateTime expirationDate,  
    Long usageDurationMinutes  
) {}
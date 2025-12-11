package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record BadgeRankingWeightResponse(
    Long id,
    UUID badgeId,
    String badgeName,
    String badgeIconClass,
    String badgeColorCode,
    Double weightValue,  
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

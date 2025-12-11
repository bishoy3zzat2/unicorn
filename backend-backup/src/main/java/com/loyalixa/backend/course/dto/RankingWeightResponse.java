package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
public record RankingWeightResponse(
    Long id,
    String factorName,
    Double weightValue,
    Double decayRate,
    Integer decayPeriodDays,
    Integer daysUntilZero,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

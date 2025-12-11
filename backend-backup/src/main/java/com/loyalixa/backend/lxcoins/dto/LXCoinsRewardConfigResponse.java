package com.loyalixa.backend.lxcoins.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record LXCoinsRewardConfigResponse(
    Long id,
    String activityType,
    BigDecimal baseReward,
    Boolean isEnabled,
    String configJson,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

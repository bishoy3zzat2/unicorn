package com.loyalixa.backend.lxcoins.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record LXCoinsRewardConfigRequest(
    @NotBlank(message = "Activity type cannot be empty.")
    String activityType,
    @NotNull(message = "Base reward cannot be null.")
    BigDecimal baseReward,
    Boolean isEnabled,
    String configJson,
    String description
) {}

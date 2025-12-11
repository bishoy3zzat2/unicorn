package com.loyalixa.backend.lxcoins.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public record LXCoinsAccountResponse(
    UUID id,
    UUID userId,
    String userEmail,
    String userName,
    BigDecimal balance,
    BigDecimal totalEarned,
    BigDecimal totalSpent,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

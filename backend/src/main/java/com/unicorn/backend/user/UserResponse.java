package com.unicorn.backend.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
                UUID id,
                String email,
                String role,
                String status,
                String authProvider,
                LocalDateTime createdAt,
                LocalDateTime lastLoginAt,
                LocalDateTime suspendedAt,
                LocalDateTime suspendedUntil,
                String suspensionType,
                String username,
                String firstName,
                String lastName,
                String displayName,
                String phoneNumber,
                String country,
                String avatarUrl,
                String suspendReason,
                boolean hasInvestorProfile,
                boolean hasStartups,
                boolean hasActiveSession) {
}

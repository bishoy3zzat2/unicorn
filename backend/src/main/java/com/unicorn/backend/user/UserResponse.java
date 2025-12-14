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
                String suspensionType) {

        public static UserResponse fromEntity(User user) {
                return new UserResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getRole(),
                                user.getStatus(),
                                user.getAuthProvider(),
                                user.getCreatedAt(),
                                user.getLastLoginAt(),
                                user.getSuspendedAt(),
                                user.getSuspendedUntil(),
                                user.getSuspensionType());
        }
}

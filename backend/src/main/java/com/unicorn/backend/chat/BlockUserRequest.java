package com.unicorn.backend.chat;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for blocking a user.
 *
 * @param userId the ID of the user to block
 */
public record BlockUserRequest(
        @NotNull(message = "User ID is required") UUID userId) {
}

package com.unicorn.backend.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for sending a chat request from Elite startup to investor.
 *
 * @param investorId     the target investor ID
 * @param startupId      the startup sending the request
 * @param initialMessage the introductory message
 */
public record SendChatRequestRequest(
        @NotNull(message = "Investor ID is required") UUID investorId,

        @NotNull(message = "Startup ID is required") UUID startupId,

        @NotBlank(message = "Initial message is required") @Size(max = 1000, message = "Message must be less than 1000 characters") String initialMessage) {
}

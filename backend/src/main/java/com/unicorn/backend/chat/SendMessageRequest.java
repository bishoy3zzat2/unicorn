package com.unicorn.backend.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending a message in a chat.
 *
 * @param content the message content
 */
public record SendMessageRequest(
        @NotBlank(message = "Message content is required") @Size(max = 5000, message = "Message must be less than 5000 characters") String content) {
}

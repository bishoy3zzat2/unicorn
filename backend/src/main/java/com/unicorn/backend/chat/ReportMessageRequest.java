package com.unicorn.backend.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for reporting a message.
 *
 * @param messageId the ID of the message to report
 * @param reason    the reason for reporting
 */
public record ReportMessageRequest(
        @NotNull(message = "Message ID is required") UUID messageId,

        @NotBlank(message = "Reason is required") @Size(max = 500, message = "Reason must be less than 500 characters") String reason) {
}

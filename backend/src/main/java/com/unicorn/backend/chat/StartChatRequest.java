package com.unicorn.backend.chat;

import java.util.UUID;

/**
 * Request DTO for starting a chat.
 *
 * @param startupId the target startup ID
 */
public record StartChatRequest(UUID startupId) {
}

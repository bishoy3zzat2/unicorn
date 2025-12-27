package com.unicorn.backend.notification;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for sending system announcements.
 */
public record SendAnnouncementRequest(
        String title,
        String message,
        TargetAudience targetAudience,
        UUID specificUserId,
        String specificUserEmail,
        Set<NotificationChannel> channels) {
    public enum TargetAudience {
        ALL_USERS,
        INVESTORS_ONLY,
        STARTUP_OWNERS_ONLY,
        SPECIFIC_USER
    }

    /**
     * Validate the request.
     */
    public void validate() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (targetAudience == null) {
            throw new IllegalArgumentException("Target audience is required");
        }
        if (targetAudience == TargetAudience.SPECIFIC_USER
                && specificUserId == null
                && (specificUserEmail == null || specificUserEmail.isBlank())) {
            throw new IllegalArgumentException("Specific user ID or email is required when targeting a specific user");
        }
        if (channels == null || channels.isEmpty()) {
            throw new IllegalArgumentException("At least one notification channel is required");
        }
    }
}

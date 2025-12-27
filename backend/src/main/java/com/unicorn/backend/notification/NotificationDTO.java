package com.unicorn.backend.notification;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for notification responses.
 * Used for REST API responses and WebSocket messages.
 */
public record NotificationDTO(
        UUID id,
        String type,
        String title,
        String message,
        Map<String, Object> data,
        boolean read,
        LocalDateTime createdAt,
        // Actor information (who triggered the notification)
        UUID actorId,
        String actorName,
        String actorAvatarUrl,
        // Broadcast info
        boolean isBroadcast,
        String targetAudience,
        // Recipient info (for non-broadcast)
        UUID recipientId,
        String recipientEmail,
        String recipientName) {
    /**
     * Create a NotificationDTO from a Notification entity.
     */
    public static NotificationDTO from(Notification notification, Map<String, Object> parsedData) {
        String recipientEmail = null;
        String recipientName = null;
        UUID recipientId = null;

        if (notification.getRecipient() != null) {
            recipientId = notification.getRecipient().getId();
            recipientEmail = notification.getRecipient().getEmail();
            recipientName = notification.getRecipient().getDisplayName() != null
                    ? notification.getRecipient().getDisplayName()
                    : notification.getRecipient().getUsername();
        }

        return new NotificationDTO(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                parsedData,
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getActor() != null ? notification.getActor().getId() : null,
                notification.getActorName(),
                notification.getActorAvatarUrl(),
                notification.isBroadcast(),
                notification.getTargetAudience(),
                recipientId,
                recipientEmail,
                recipientName);
    }
}

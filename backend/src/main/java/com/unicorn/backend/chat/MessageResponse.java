package com.unicorn.backend.chat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for chat message.
 */
public record MessageResponse(
                UUID id,
                UUID chatId,
                UUID senderId,
                String senderName,
                String senderAvatarUrl,
                String content,
                Boolean isRead,
                Boolean isDeleted,
                LocalDateTime createdAt,
                LocalDateTime readAt) {
        /**
         * Create a MessageResponse from a ChatMessage entity.
         */
        public static MessageResponse fromEntity(ChatMessage message) {
                return new MessageResponse(
                                message.getId(),
                                message.getChat().getId(),
                                message.getSender().getId(),
                                message.getSender().getDisplayName() != null ? message.getSender().getDisplayName()
                                                : message.getSender().getEmail(),
                                message.getSender().getAvatarUrl(),
                                message.getIsDeleted() ? "[Deleted]" : message.getContent(),
                                message.getIsRead(),
                                message.getIsDeleted(),
                                message.getCreatedAt(),
                                message.getReadAt());
        }
}

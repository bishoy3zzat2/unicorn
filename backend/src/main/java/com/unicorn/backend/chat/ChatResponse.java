package com.unicorn.backend.chat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for chat conversation.
 */
public record ChatResponse(
        UUID id,
        UUID investorId,
        String investorName,
        UUID startupId,
        String startupName,
        ChatStatus status,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt,
        long unreadCount) {
    /**
     * Create a ChatResponse from a Chat entity.
     */
    public static ChatResponse fromEntity(Chat chat, long unreadCount) {
        return new ChatResponse(
                chat.getId(),
                chat.getInvestor().getId(),
                chat.getInvestor().getDisplayName() != null ? chat.getInvestor().getDisplayName()
                        : chat.getInvestor().getEmail(),
                chat.getStartup().getId(),
                chat.getStartup().getName(),
                chat.getStatus(),
                chat.getCreatedAt(),
                chat.getLastMessageAt(),
                unreadCount);
    }
}

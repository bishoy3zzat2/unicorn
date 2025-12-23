package com.unicorn.backend.chat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for chat request.
 */
public record ChatRequestResponse(
        UUID id,
        UUID startupId,
        String startupName,
        UUID investorId,
        String investorName,
        String initialMessage,
        RequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt) {
    /**
     * Create a ChatRequestResponse from a ChatRequest entity.
     */
    public static ChatRequestResponse fromEntity(ChatRequest request) {
        return new ChatRequestResponse(
                request.getId(),
                request.getStartup().getId(),
                request.getStartup().getName(),
                request.getInvestor().getId(),
                request.getInvestor().getDisplayName() != null ? request.getInvestor().getDisplayName()
                        : request.getInvestor().getEmail(),
                request.getInitialMessage(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getRespondedAt());
    }
}

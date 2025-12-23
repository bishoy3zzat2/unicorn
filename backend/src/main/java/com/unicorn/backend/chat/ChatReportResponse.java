package com.unicorn.backend.chat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for chat report.
 */
public record ChatReportResponse(
        UUID id,
        UUID messageId,
        String messageContent,
        UUID reporterId,
        String reporterName,
        UUID reportedUserId,
        String reportedUserName,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        String reviewedByName) {
    /**
     * Create a ChatReportResponse from a ChatReport entity.
     */
    public static ChatReportResponse fromEntity(ChatReport report) {
        return new ChatReportResponse(
                report.getId(),
                report.getMessage().getId(),
                report.getMessage().getIsDeleted() ? "[Deleted]" : report.getMessage().getContent(),
                report.getReporter().getId(),
                report.getReporter().getDisplayName() != null ? report.getReporter().getDisplayName()
                        : report.getReporter().getEmail(),
                report.getMessage().getSender().getId(),
                report.getMessage().getSender().getDisplayName() != null
                        ? report.getMessage().getSender().getDisplayName()
                        : report.getMessage().getSender().getEmail(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt(),
                report.getReviewedBy() != null
                        ? (report.getReviewedBy().getDisplayName() != null ? report.getReviewedBy().getDisplayName()
                                : report.getReviewedBy().getEmail())
                        : null);
    }
}

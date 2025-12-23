package com.unicorn.backend.chat;

/**
 * Enum representing the status of a chat message report.
 */
public enum ReportStatus {
    /**
     * Report is pending admin review.
     */
    PENDING,

    /**
     * Report has been reviewed by admin.
     */
    REVIEWED,

    /**
     * Report was dismissed without action.
     */
    DISMISSED,

    /**
     * Action was taken (e.g., message deleted, user warned).
     */
    ACTION_TAKEN
}

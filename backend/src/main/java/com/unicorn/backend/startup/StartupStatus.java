package com.unicorn.backend.startup;

/**
 * Enum representing the approval status of a startup application.
 */
public enum StartupStatus {
    PENDING, // Awaiting admin review
    APPROVED, // Approved by admin
    REJECTED // Rejected by admin
}

package com.unicorn.backend.user;

/**
 * Types of moderation actions that can be performed on users.
 */
public enum ModerationActionType {
    WARNING, // Issue a warning to user
    SUSPENSION, // Temporarily suspend user
    PERMANENT_BAN, // Permanently ban user
    UNSUSPEND, // Remove suspension
    UNBAN, // Remove ban
    DELETE, // Soft delete user
    RESTORE, // Restore deleted user
    STATUS_CHANGE // General status change
}

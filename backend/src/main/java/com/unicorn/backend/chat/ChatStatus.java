package com.unicorn.backend.chat;

/**
 * Enum representing the status of a chat conversation.
 */
public enum ChatStatus {
    /**
     * Chat is active and messages can be exchanged.
     */
    ACTIVE,

    /**
     * Chat has been blocked by one of the participants.
     */
    BLOCKED,

    /**
     * Chat has been deleted (soft delete).
     */
    DELETED
}

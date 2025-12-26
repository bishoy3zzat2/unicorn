package com.unicorn.backend.feed;

/**
 * Enum representing the status of a post in the feed.
 */
public enum PostStatus {
    ACTIVE, // Visible in feed
    HIDDEN, // Hidden by admin (not deleted)
    DELETED // Soft-deleted
}

package com.unicorn.backend.report;

/**
 * Enum representing the type of entity being reported.
 * This design is extensible - new entity types can be added without schema
 * changes.
 */
public enum ReportedEntityType {
    USER, // Report against a user
    STARTUP, // Report against a startup
    POST, // Report against a feed post
    COMMENT // Report against a comment
}

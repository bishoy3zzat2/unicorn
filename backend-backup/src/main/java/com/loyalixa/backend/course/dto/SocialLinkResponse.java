package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record SocialLinkResponse(
    UUID id,
    String platform,
    String iconClass,
    String url,
    String username,
    Boolean isUsernameBased,
    String displayText,
    Integer orderIndex,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

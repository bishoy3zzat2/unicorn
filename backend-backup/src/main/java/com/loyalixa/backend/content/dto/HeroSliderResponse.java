package com.loyalixa.backend.content.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record HeroSliderResponse(
    Long id,
    String mainTitle,
    String description,
    String mediaUrl,
    String mediaType,
    String buttonText,
    String buttonLink,
    Integer displayDurationMs,
    Boolean autoplay,
    Boolean loop,
    Boolean muted,
    Boolean controls,
    Integer orderIndex,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UserInfo createdBy,
    UserInfo updatedBy
) {
    public record UserInfo(UUID id, String email, String username) {}
}

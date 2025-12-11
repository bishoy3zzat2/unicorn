package com.loyalixa.backend.content.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record FaqResponse(
    Long id,
    String question,
    String answer,
    String category,
    Integer orderIndex,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UserInfo createdBy,
    UserInfo updatedBy
) {
    public record UserInfo(UUID id, String email, String username) {}
}
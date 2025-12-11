package com.loyalixa.backend.content.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record FaqDetailsResponse(
    Long id,
    String question,
    String answer,
    String category,
    Integer orderIndex,
    UserInfo createdBy,
    UserInfo updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record UserInfo(UUID id, String email, String username, String roleName) {}
}

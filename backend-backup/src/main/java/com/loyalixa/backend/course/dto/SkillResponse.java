package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record SkillResponse(
    UUID id,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UserInfo createdBy,
    UserInfo updatedBy
) {
    public record UserInfo(UUID id, String email, String username) {}
}

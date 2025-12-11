package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record SkillDetailsResponse(
    UUID id,
    String name,
    List<CourseInfo> courses,
    UserInfo createdBy,
    UserInfo updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record CourseInfo(UUID id, String title, String slug, String status) {}
    public record UserInfo(UUID id, String email, String username, String roleName) {}
}

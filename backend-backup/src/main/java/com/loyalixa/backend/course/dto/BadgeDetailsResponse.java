package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record BadgeDetailsResponse(
    UUID id,
    String name,
    String colorCode,  
    String customCss,  
    Double weight,  
    String iconClass,
    String targetType,
    Boolean isDynamic,
    LocalDateTime expirationDate,
    LocalDateTime validUntil,
    Long usageDurationMinutes,  
    List<CourseInfo> courses,
    UserInfo createdBy,
    UserInfo updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record CourseInfo(UUID id, String title, String slug, String status, LocalDateTime assignedAt, LocalDateTime expirationDate) {}
    public record UserInfo(UUID id, String email, String username, String roleName) {}
}

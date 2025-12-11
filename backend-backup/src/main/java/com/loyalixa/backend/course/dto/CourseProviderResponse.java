package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record CourseProviderResponse(
    UUID id,
    String name,
    String slug,
    String logoUrl,
    String websiteUrl,
    String description,
    Boolean isActive,
    List<SocialLinkResponse> socialLinks,
    UserInfo createdBy,
    UserInfo updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record UserInfo(
        UUID id,
        String username,
        String email
    ) {}
}

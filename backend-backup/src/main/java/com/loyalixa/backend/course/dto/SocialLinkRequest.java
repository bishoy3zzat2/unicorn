package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SocialLinkRequest(
    @NotBlank(message = "Platform is required")
    @Size(max = 50, message = "Platform name must not exceed 50 characters")
    String platform,
    @Size(max = 200, message = "Icon class must not exceed 200 characters")
    String iconClass,
    @Size(max = 500, message = "URL must not exceed 500 characters")
    String url,
    @Size(max = 200, message = "Username must not exceed 200 characters")
    String username,
    Boolean isUsernameBased,
    @Size(max = 100, message = "Display text must not exceed 100 characters")
    String displayText,
    Integer orderIndex
) {}

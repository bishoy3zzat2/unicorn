package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
public record BadgeRequest(
    @NotBlank(message = "Badge name cannot be empty.")
    @Size(max = 50)
    String name,
    String color,  
    String customCss,  
    Double weight,  
    LocalDateTime expirationDate,
    Long usageDurationMinutes,
    String targetType
) {}
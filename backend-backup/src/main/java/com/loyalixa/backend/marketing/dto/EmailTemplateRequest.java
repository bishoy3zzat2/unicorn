package com.loyalixa.backend.marketing.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record EmailTemplateRequest(
    @NotBlank(message = "Template type is required")
    String templateType,
    @NotBlank(message = "Subject is required")
    String subject,
    @NotBlank(message = "HTML content is required")
    String htmlContent,
    String textContent,
    String description,
    @NotNull(message = "isActive is required")
    Boolean isActive
) {}

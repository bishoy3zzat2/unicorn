package com.loyalixa.backend.marketing.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public record EmailTemplateResponse(
    UUID id,
    String templateType,
    String subject,
    String htmlContent,
    String textContent,
    String description,
    Boolean isActive,
    UUID createdById,
    String createdByUsername,
    UUID updatedById,
    String updatedByUsername,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

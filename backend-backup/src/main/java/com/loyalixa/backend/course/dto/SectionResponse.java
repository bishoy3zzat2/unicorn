package com.loyalixa.backend.course.dto;
import java.util.UUID;
public record SectionResponse(
    Long id,
    String title,
    Integer orderIndex,
    Boolean isFreePreview,
    UUID courseId
) {}

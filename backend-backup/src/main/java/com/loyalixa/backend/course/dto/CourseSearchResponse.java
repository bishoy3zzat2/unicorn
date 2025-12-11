package com.loyalixa.backend.course.dto;
import java.util.UUID;
public record CourseSearchResponse(
    UUID id,
    String title,
    String slug
) {}

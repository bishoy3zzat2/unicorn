package com.loyalixa.backend.course.dto;
import java.util.List;
import java.util.UUID;
public record CourseValidationResponse(
    UUID courseId,
    boolean isValid,
    List<String> missingFields,
    List<String> warnings,
    String courseTitle,
    String status,
    String approvalStatus
) {}

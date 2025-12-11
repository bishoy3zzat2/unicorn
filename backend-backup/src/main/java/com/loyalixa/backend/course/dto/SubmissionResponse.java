package com.loyalixa.backend.course.dto;
import com.loyalixa.backend.course.AttemptStatus;
import java.util.UUID;
public record SubmissionResponse(
    UUID attemptId,
    UUID quizId,
    Integer finalScore,
    Boolean isPassed,
    AttemptStatus status,
    Integer browserLeaveCount
) {}
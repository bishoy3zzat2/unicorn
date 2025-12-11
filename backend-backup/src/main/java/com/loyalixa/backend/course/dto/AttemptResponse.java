package com.loyalixa.backend.course.dto;
import com.loyalixa.backend.course.AttemptStatus;
import java.time.LocalDateTime;
import java.util.UUID;
public record AttemptResponse(
    UUID attemptId,
    UUID quizId,
    UUID studentId,
    AttemptStatus status,
    LocalDateTime startedAt
) {}
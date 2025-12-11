package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import java.util.List;
public record QuizUpdateRequest(
    @Size(max = 255, message = "Quiz title must be less than 255 characters.")
    String title,
    Integer durationMinutes,
    Integer orderIndex,
    Integer passScorePercentage,
    Boolean requiresProctoring,
    String quizType,
    Integer maxAttempts,
    String gradingStrategy,
    String gradingType,
    Boolean allowLateSubmission,
    String requiredDeviceType,
    String allowedBrowsers,
    String instructions,  
    Long lessonId,
    UUID bundleId,
    List<UUID> badgeIds,  
    List<QuestionRequest> questions
) {}

package com.loyalixa.backend.course.dto;
import java.util.List;
import java.util.UUID;
public record QuizSubmissionRequest(
    UUID quizId,
    UUID userAttemptId,  
    List<AnswerSubmissionRequest> answers,  
    Integer timeTakenSeconds,  
    Integer browserLeaveCount,  
    String proctoringDetails  
) {}
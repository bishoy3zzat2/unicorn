package com.loyalixa.backend.course.dto;
public record AnswerSubmissionRequest(
    Long questionId,
    String studentAnswer,  
    Integer timeToAnswerSeconds,  
    Boolean isCopied,  
    Boolean screenCaptured  
) {}
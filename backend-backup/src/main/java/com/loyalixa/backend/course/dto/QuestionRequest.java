package com.loyalixa.backend.course.dto;
import java.util.List;
public record QuestionRequest(
    String questionText,
    String questionType,  
    Integer points,
    Integer orderIndex,
    String correctAnswer,  
    List<String> options  
) {}
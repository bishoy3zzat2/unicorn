package com.loyalixa.backend.course.dto;
import java.util.UUID;
public record CertificateRequest(
    UUID id,  
    String slug,  
    String title,  
    String description,  
    String requirements,  
    Integer minCompletionPercentage,  
    Boolean requiresInterview,  
    Boolean requiresSpecialExam,  
    String examRequirements,  
    String templateUrl,  
    Boolean isActive,  
    Integer validityMonths  
) {}

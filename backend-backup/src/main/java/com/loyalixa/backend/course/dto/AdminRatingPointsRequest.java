package com.loyalixa.backend.course.dto;
import java.time.LocalDateTime;
public record AdminRatingPointsRequest(
    Integer points,  
    Boolean isFixed,  
    LocalDateTime expiresAt  
) {}

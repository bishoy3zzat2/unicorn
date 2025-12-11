package com.loyalixa.backend.course.dto;
public record DeviceCheckRequest(
    String userAgent,  
    int screenWidth,    
    boolean isTouchEnabled  
) {}
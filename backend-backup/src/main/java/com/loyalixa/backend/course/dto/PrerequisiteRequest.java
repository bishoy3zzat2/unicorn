package com.loyalixa.backend.course.dto;
import java.util.UUID;
public record PrerequisiteRequest(
    String type,  
    String id,  
    String requirementType  
) {}

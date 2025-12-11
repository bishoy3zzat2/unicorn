package com.loyalixa.backend.content.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AdvantageFeatureRequest(
    @NotBlank @Size(max = 255)
    String title,
    @NotBlank
    String description,
    @Size(max = 500)
    String iconUrl,  
    Integer orderIndex
) {}
package com.loyalixa.backend.content.dto;
import jakarta.validation.constraints.NotBlank;
public record HeroSliderRequest(
    @NotBlank
    String mainTitle,
    String description,
    @NotBlank
    String mediaUrl,
    @NotBlank
    String mediaType,  
    String buttonText,
    String buttonLink,
    Integer displayDurationMs,  
    Boolean autoplay,  
    Boolean loop,  
    Boolean muted,  
    Boolean controls,  
    Integer orderIndex
) {}

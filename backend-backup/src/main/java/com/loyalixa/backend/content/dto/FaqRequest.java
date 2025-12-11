package com.loyalixa.backend.content.dto;
import jakarta.validation.constraints.NotBlank;
public record FaqRequest(
    @NotBlank
    String question,
    @NotBlank
    String answer,
    @NotBlank
    String category,
    Integer orderIndex
) {}
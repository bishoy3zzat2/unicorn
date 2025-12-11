package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record SectionRequest(
    @NotBlank(message = "Section title cannot be empty.")
    @Size(max = 255, message = "Section title must be less than 255 characters.")
    String title,
    @NotNull(message = "Order index is required.")
    Integer orderIndex,
    Boolean isFreePreview
) {}

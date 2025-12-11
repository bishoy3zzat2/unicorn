package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record TagRequest(
    @NotBlank(message = "Tag name cannot be empty.")
    @Size(max = 50, message = "Tag name must be less than 50 characters.")
    String name
) {}
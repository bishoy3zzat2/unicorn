package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CategoryRequest(
    @NotBlank(message = "Category name cannot be empty.")
    @Size(max = 100, message = "Category name must be less than 100 characters.")
    String name,
    @NotBlank(message = "Category slug cannot be empty.")
    @Size(max = 100, message = "Category slug must be less than 100 characters.")
    String slug,
    @Size(max = 50, message = "Icon class must be less than 50 characters.")
    String iconClass,
    Integer orderIndex
) {}

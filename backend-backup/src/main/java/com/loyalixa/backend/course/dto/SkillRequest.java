package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SkillRequest(
    @NotBlank(message = "Skill name cannot be empty.")
    @Size(max = 100, message = "Skill name must be less than 100 characters.")
    String name
) {}
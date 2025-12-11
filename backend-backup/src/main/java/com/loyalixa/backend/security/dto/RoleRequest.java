package com.loyalixa.backend.security.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RoleRequest(
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    String name,
    String description
) {}

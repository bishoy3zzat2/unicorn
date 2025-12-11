package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record StaffCreateRequest(
    @NotBlank(message = "Username is required")
    String username,
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,
    @NotBlank(message = "Password is required")
    String password,
    @NotNull(message = "Role ID is required")
    UUID roleId,
    Boolean canAccessDashboard
) {}

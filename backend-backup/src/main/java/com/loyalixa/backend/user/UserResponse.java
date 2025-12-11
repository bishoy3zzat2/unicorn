package com.loyalixa.backend.user;
import java.util.UUID;
public record UserResponse(
    UUID id,
    String username,
    String email,
    String role,
    Boolean canAccessDashboard
) {}
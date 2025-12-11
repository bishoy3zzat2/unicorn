package com.loyalixa.backend.user.dto;
import java.util.UUID;
public record UserPublicResponse(
    UUID id,
    String username,
    String role
) {}
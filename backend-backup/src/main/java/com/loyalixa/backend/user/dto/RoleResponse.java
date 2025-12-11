package com.loyalixa.backend.user.dto;
import java.util.UUID;
public record RoleResponse(
    UUID id,
    String name,
    String description
) {}

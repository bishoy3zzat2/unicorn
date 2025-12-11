package com.loyalixa.backend.security.dto;
import java.util.List;
import java.util.UUID;
public record PermissionDetailsResponse(
    UUID id,
    String name,
    String description,
    long rolesCount,
    List<RoleInfo> roles
) {
    public record RoleInfo(
        UUID id,
        String name,
        String description,
        long usersCount  
    ) {}
}

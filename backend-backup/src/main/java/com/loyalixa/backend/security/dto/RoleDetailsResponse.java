package com.loyalixa.backend.security.dto;
import com.loyalixa.backend.user.dto.UserAdminResponse;
import java.util.List;
import java.util.UUID;
public record RoleDetailsResponse(
    UUID id,
    String name,
    String description,
    int permissionsCount,
    List<PermissionInfo> permissions,
    long usersCount,
    List<UserAdminResponse> users
) {
    public record PermissionInfo(
        UUID id,
        String name,
        String description
    ) {}
}

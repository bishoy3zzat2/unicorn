package com.loyalixa.backend.user.dto;
import jakarta.validation.constraints.Email;
public record UserFullDetailsUpdateRequest(
    String email,
    String username,
    String status,
    String roleName,
    String firstName,
    String lastName,
    String bio,
    String avatarUrl,
    String phoneNumber,
    String phoneSocialApp,
    String secondaryEmail,
    String tshirtSize,
    String extraInfo,
    String uiTheme,
    String uiLanguage,
    String timezone,
    java.util.Map<String, Boolean> notifications
) {}

package com.loyalixa.backend.course.dto;
import com.loyalixa.backend.user.dto.UserPublicResponse;
public record ReviewResponse(
    String quote,
    Integer rating,
    UserPublicResponse student
) {}
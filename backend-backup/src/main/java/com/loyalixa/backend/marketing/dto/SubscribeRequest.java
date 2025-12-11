package com.loyalixa.backend.marketing.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record SubscribeRequest(
    @NotBlank
    @Email
    String email,
    Integer screenWidth,
    Integer screenHeight,
    Integer viewportWidth,
    Integer viewportHeight,
    Double devicePixelRatio,
    String timezone,
    String platform,
    Integer hardwareConcurrency,
    Double deviceMemoryGb,
    Boolean touchSupport
) {}
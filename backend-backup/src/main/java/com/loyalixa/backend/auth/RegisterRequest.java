package com.loyalixa.backend.auth;
public record RegisterRequest(
        String username,
        String email,
        String password,
        String deviceName,
        String deviceType,
        Integer screenWidth,
        Integer screenHeight,
        Integer viewportWidth,
        Integer viewportHeight,
        String timezone,
        String platform,
        Integer hardwareConcurrency,
        Double deviceMemory,
        Double devicePixelRatio,
        Boolean touchSupport) {
}
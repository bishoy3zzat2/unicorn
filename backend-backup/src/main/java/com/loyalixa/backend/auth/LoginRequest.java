package com.loyalixa.backend.auth;
public record LoginRequest(
        String email,
        String password,
        String deviceId,
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
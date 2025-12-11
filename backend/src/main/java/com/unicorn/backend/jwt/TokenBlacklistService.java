package com.unicorn.backend.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> deviceTokens = new ConcurrentHashMap<>();

    private final JwtService jwtService;

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token, long expirationSeconds) {
        blacklistToken(token, expirationSeconds, null);
    }

    public void blacklistToken(String token, long expirationSeconds, String deviceId) {
        try {
            long expiryTime = System.currentTimeMillis() + (expirationSeconds * 1000);
            blacklistedTokens.put(token, expiryTime);

            if (deviceId != null && !deviceId.isEmpty()) {
                deviceTokens.computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet()).add(token);
            }

            cleanupExpiredTokens();

        } catch (Exception e) {
            logger.error("Unexpected error while blacklisting token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            Long expiryTime = blacklistedTokens.get(token);
            if (expiryTime == null) {
                return false;
            }

            if (System.currentTimeMillis() > expiryTime) {
                blacklistedTokens.remove(token);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error while checking token blacklist", e);
            return false;
        }
    }

    public void registerTokenForDevice(String token, String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }
        try {
            deviceTokens.computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet()).add(token);
        } catch (Exception e) {
            logger.error("Unexpected error while registering token for device", e);
        }
    }

    private void cleanupExpiredTokens() {
        // Simple cleanup: remove all entries that are expired
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> now > entry.getValue());
    }
}

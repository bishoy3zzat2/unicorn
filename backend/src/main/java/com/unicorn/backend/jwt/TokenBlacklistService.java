package com.unicorn.backend.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    // In-memory replacement for Redis
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    private final Map<String, Long> userRevocationList = new ConcurrentHashMap<>();

    public TokenBlacklistService() {
        logger.info("Initialized In-Memory TokenBlacklistService");
    }

    public void blacklistToken(String token, long expirationSeconds) {
        try {
            // Store absolute expiry time in millis
            long expiryTime = System.currentTimeMillis() + (expirationSeconds * 1000);
            tokenBlacklist.put(token, expiryTime);
        } catch (Exception e) {
            logger.error("Unexpected error while blacklisting token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            Long expiry = tokenBlacklist.get(token);
            if (expiry == null) {
                return false;
            }
            // Check if expired
            if (System.currentTimeMillis() > expiry) {
                tokenBlacklist.remove(token); // Lazy cleanup
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error while checking token blacklist", e);
            return false;
        }
    }

    // Revoke user access (e.g. on logout all)
    public void revokeUserAccess(String userId) {
        try {
            // Store current timestamp as revocation time
            userRevocationList.put(userId, System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("Error revoking user access in memory", e);
        }
    }

    public boolean isUserRevoked(String userId, long tokenIssuedAt) {
        try {
            Long revocationTime = userRevocationList.get(userId);
            if (revocationTime != null) {
                // If token was issued BEFORE the revocation timestamp, it is invalid
                // Note: Ensure tokenIssuedAt is in Milliseconds to match
                // System.currentTimeMillis()
                return tokenIssuedAt < revocationTime;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking user revocation in memory", e);
            return false;
        }
    }

    public long getBlacklistedTokenCount() {
        return tokenBlacklist.size();
    }
}

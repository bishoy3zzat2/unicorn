package com.loyalixa.backend.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class JwtConfigService {
    @Value("${jwt.access.expiration.ms:600000}")
    private long defaultAccessTokenDurationMs;
    @Value("${jwt.refresh.expiration.ms:604800000}")
    private long defaultRefreshTokenDurationMs;
    private volatile long accessTokenDurationMs;
    private volatile long refreshTokenDurationMs;
    public JwtConfigService() {
    }
    @jakarta.annotation.PostConstruct
    public void init() {
        this.accessTokenDurationMs = defaultAccessTokenDurationMs;
        this.refreshTokenDurationMs = defaultRefreshTokenDurationMs;
    }
    public long getAccessTokenDurationMs() {
        return accessTokenDurationMs;
    }
    public void setAccessTokenDurationMs(long accessTokenDurationMs) {
        if (accessTokenDurationMs < 60000) {
            throw new IllegalArgumentException("Access token duration must be at least 1 minute (60000 ms)");
        }
        if (accessTokenDurationMs > 86400000) {
            throw new IllegalArgumentException("Access token duration should not exceed 1 day (86400000 ms)");
        }
        this.accessTokenDurationMs = accessTokenDurationMs;
    }
    public long getRefreshTokenDurationMs() {
        return refreshTokenDurationMs;
    }
    public void setRefreshTokenDurationMs(long refreshTokenDurationMs) {
        if (refreshTokenDurationMs < 3600000) {
            throw new IllegalArgumentException("Refresh token duration must be at least 1 hour (3600000 ms)");
        }
        if (refreshTokenDurationMs > 2592000000L) {
            throw new IllegalArgumentException("Refresh token duration should not exceed 30 days (2592000000 ms)");
        }
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }
    public long getDefaultAccessTokenDurationMs() {
        return defaultAccessTokenDurationMs;
    }
    public long getDefaultRefreshTokenDurationMs() {
        return defaultRefreshTokenDurationMs;
    }
    public void resetToDefaults() {
        this.accessTokenDurationMs = defaultAccessTokenDurationMs;
        this.refreshTokenDurationMs = defaultRefreshTokenDurationMs;
    }
}

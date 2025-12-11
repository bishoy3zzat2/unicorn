package com.unicorn.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

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

    @PostConstruct
    public void init() {
        this.accessTokenDurationMs = defaultAccessTokenDurationMs;
        this.refreshTokenDurationMs = defaultRefreshTokenDurationMs;
    }

    public long getAccessTokenDurationMs() {
        return accessTokenDurationMs;
    }

    public long getRefreshTokenDurationMs() {
        return refreshTokenDurationMs;
    }
}

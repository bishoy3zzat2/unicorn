package com.unicorn.backend.security;

import com.unicorn.backend.config.JwtConfigService;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfigService jwtConfigService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtConfigService jwtConfigService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtConfigService = jwtConfigService;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(User user, String userAgent, String ipAddress) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtConfigService.getRefreshTokenDurationMs()));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserAgent(userAgent);
        refreshToken.setIpAddress(ipAddress);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public List<RefreshToken> findAllActiveByUserId(UUID userId) {
        return refreshTokenRepository.findByUserId(userId);
    }

    public void updateLastUsed(String token) {
        findByToken(token).ifPresent(rt -> {
            rt.setLastUsedAt(Instant.now());
            refreshTokenRepository.save(rt);
        });
    }

    public void logoutUser(UUID userId) {
        // Implementation to revoke all tokens
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new com.unicorn.backend.exception.TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}

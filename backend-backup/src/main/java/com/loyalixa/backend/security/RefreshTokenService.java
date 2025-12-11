package com.loyalixa.backend.security;
import com.loyalixa.backend.config.JwtConfigService;
import com.loyalixa.backend.user.User;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;
    private final JwtConfigService jwtConfigService;
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, EntityManager entityManager, JwtConfigService jwtConfigService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.entityManager = entityManager;
        this.jwtConfigService = jwtConfigService;
    }
    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceId, String deviceName, 
                                          String deviceType, String userAgent, String ipAddress) {
        Instant now = Instant.now();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString();
        }
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            if (token.getExpiryDate().isAfter(now)) {
                token.setLastUsedAt(now);
                return refreshTokenRepository.save(token);
            }
        }
        long activeTokenCount = refreshTokenRepository.countActiveByUserId(user.getId(), now);
        int maxDevices = user.getMaxDevices() != null && user.getMaxDevices() > 0 ? user.getMaxDevices() : 1;
        logger.debug("User {} has {} active tokens, maxDevices: {}", user.getEmail(), activeTokenCount, maxDevices);
        if (activeTokenCount >= maxDevices) {
            int tokensToDelete = (int)(activeTokenCount - maxDevices + 1);
            logger.debug("Need to delete {} tokens for user {}", tokensToDelete, user.getEmail());
            try {
                for (int i = 0; i < tokensToDelete; i++) {
                    int deleted = refreshTokenRepository.deleteOldestActiveTokenByUserId(user.getId(), now);
                    logger.debug("Deleted {} token(s) for user {} (attempt {}/{})", deleted, user.getEmail(), i + 1, tokensToDelete);
                    if (deleted == 0) {
                        logger.debug("No more tokens to delete for user {}", user.getEmail());
                        break;
                    }
                }
                entityManager.flush();
            } catch (Exception e) {
                logger.error("Error deleting old tokens for user {}: {}", user.getEmail(), e.getMessage(), e);
            }
        }
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setDeviceName(deviceName != null ? deviceName : generateDeviceName(deviceType, userAgent));
        refreshToken.setDeviceType(deviceType);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setLastUsedAt(now);
        refreshToken.setExpiryDate(now.plusMillis(jwtConfigService.getRefreshTokenDurationMs()));
        return refreshTokenRepository.save(refreshToken);
    }
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String defaultDeviceId = UUID.randomUUID().toString();
        return createRefreshToken(user, defaultDeviceId, "Unknown Device", "UNKNOWN", null, null);
    }
    @Transactional
    public void updateLastUsed(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setLastUsedAt(Instant.now());
            refreshTokenRepository.save(rt);
        });
    }
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    @Deprecated
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUserId(UUID userId) {
        return refreshTokenRepository.findByUserId(userId);
    }
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUserIdAndDeviceId(UUID userId, String deviceId) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByUserIdAndDeviceId(userId, deviceId);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            if (token.getExpiryDate().isAfter(Instant.now())) {
                return tokenOpt;
            }
        }
        return Optional.empty();
    }
    @Transactional(readOnly = true)
    public List<RefreshToken> findAllActiveByUserId(UUID userId) {
        return refreshTokenRepository.findAllActiveByUserId(userId, Instant.now());
    }
    @Transactional(readOnly = true)
    public List<RefreshToken> findAllByUserId(UUID userId) {
        return refreshTokenRepository.findAllByUserIdOrderByLastUsedAtDescCreatedAtDesc(userId);
    }
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
            .ifPresent(refreshTokenRepository::delete);
    }
    @Transactional
    public void deleteByDeviceId(UUID userId, String deviceId) {
        refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }
    @Transactional
    public void logoutUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    @Transactional
    public void logoutDevice(UUID userId, String deviceId) {
        refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }
    @Transactional(readOnly = true)
    public long getTotalActiveRefreshTokens() {
        Instant now = Instant.now();
        return refreshTokenRepository.findAll().stream()
                .filter(token -> token.getExpiryDate() != null && token.getExpiryDate().isAfter(now))
                .count();
    }
    @Transactional(readOnly = true)
    public long countActiveDevices(UUID userId) {
        return refreshTokenRepository.countActiveByUserId(userId, Instant.now());
    }
    private String generateDeviceName(String deviceType, String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return deviceType != null ? deviceType : "Unknown Device";
        }
        String ua = userAgent.toLowerCase();
        String name = deviceType != null ? deviceType : "Device";
        if (ua.contains("chrome") && !ua.contains("edg")) {
            name += " - Chrome";
        } else if (ua.contains("firefox")) {
            name += " - Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            name += " - Safari";
        } else if (ua.contains("edg")) {
            name += " - Edge";
        }
        if (ua.contains("windows")) {
            name += " (Windows)";
        } else if (ua.contains("mac")) {
            name += " (macOS)";
        } else if (ua.contains("android")) {
            name += " (Android)";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            name += " (iOS)";
        } else if (ua.contains("linux")) {
            name += " (Linux)";
        }
        return name;
    }
}
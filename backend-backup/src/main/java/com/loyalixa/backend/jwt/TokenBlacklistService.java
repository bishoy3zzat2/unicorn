package com.loyalixa.backend.jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.TimeUnit;
@Service
public class TokenBlacklistService {
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;
    private volatile boolean redisAvailable = true;
    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }
    public void blacklistToken(String token, long expirationSeconds) {
        blacklistToken(token, expirationSeconds, null);
    }
    public void blacklistToken(String token, long expirationSeconds, String deviceId) {
        if (!redisAvailable) {
            logger.warn("Redis is not available. Token blacklisting is disabled. Token will not be blacklisted.");
            return;
        }
        try {
            redisTemplate.opsForValue().set(token, "blacklisted", expirationSeconds, TimeUnit.SECONDS);
            if (deviceId != null && !deviceId.isEmpty()) {
                String deviceTokensKey = "device:tokens:" + deviceId;
                redisTemplate.opsForSet().add(deviceTokensKey, token);
                redisTemplate.expire(deviceTokensKey, 7, TimeUnit.DAYS);
            }
        } catch (RedisConnectionFailureException e) {
            logger.warn("Failed to blacklist token - Redis connection failed: {}", e.getMessage());
            redisAvailable = false;
        } catch (Exception e) {
            logger.error("Unexpected error while blacklisting token", e);
        }
    }
    public int blacklistTokensByDeviceId(String deviceId) {
        if (!redisAvailable || deviceId == null || deviceId.isEmpty()) {
            return 0;
        }
        try {
            String deviceTokensKey = "device:tokens:" + deviceId;
            Set<Object> tokens = redisTemplate.opsForSet().members(deviceTokensKey);
            if (tokens == null || tokens.isEmpty()) {
                return 0;
            }
            int blacklistedCount = 0;
            for (Object tokenObj : tokens) {
                if (tokenObj instanceof String) {
                    String token = (String) tokenObj;
                    try {
                        if (!isTokenBlacklisted(token)) {
                            try {
                                java.util.Date exp = jwtService.getExpiration(token);
                                long expirySeconds = exp != null ? (exp.getTime() - System.currentTimeMillis()) / 1000 : 0;
                                if (expirySeconds > 0) {
                                    redisTemplate.opsForValue().set(token, "blacklisted", expirySeconds, TimeUnit.SECONDS);
                                    blacklistedCount++;
                                } else {
                                    logger.debug("Token {} is already expired, skipping blacklist", token);
                                }
                            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                                logger.debug("Token {} is expired, skipping blacklist", token);
                            } catch (Exception e) {
                                logger.warn("Failed to extract expiration from token {}, using default TTL: {}", token, e.getMessage());
                                redisTemplate.opsForValue().set(token, "blacklisted", 600, TimeUnit.SECONDS);
                                blacklistedCount++;
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to blacklist token {}: {}", token, e.getMessage());
                    }
                }
            }
            redisTemplate.delete(deviceTokensKey);
            return blacklistedCount;
        } catch (RedisConnectionFailureException e) {
            logger.warn("Failed to blacklist tokens by device - Redis connection failed: {}", e.getMessage());
            redisAvailable = false;
            return 0;
        } catch (Exception e) {
            logger.error("Unexpected error while blacklisting tokens by device", e);
            return 0;
        }
    }
    public void registerTokenForDevice(String token, String deviceId) {
        if (!redisAvailable || deviceId == null || deviceId.isEmpty()) {
            return;
        }
        try {
            String deviceTokensKey = "device:tokens:" + deviceId;
            redisTemplate.opsForSet().add(deviceTokensKey, token);
            redisTemplate.expire(deviceTokensKey, 7, TimeUnit.DAYS);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Failed to register token for device - Redis connection failed: {}", e.getMessage());
            redisAvailable = false;
        } catch (Exception e) {
            logger.error("Unexpected error while registering token for device", e);
        }
    }
    public boolean isTokenBlacklisted(String token) {
        if (!redisAvailable) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(token));
        } catch (RedisConnectionFailureException e) {
            logger.warn("Failed to check token blacklist - Redis connection failed: {}", e.getMessage());
            redisAvailable = false;
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while checking token blacklist", e);
            return false;
        }
    }
}
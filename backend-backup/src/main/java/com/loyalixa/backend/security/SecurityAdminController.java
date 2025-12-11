package com.loyalixa.backend.security;
import com.loyalixa.backend.config.JwtConfigService;
import com.loyalixa.backend.jwt.JwtService;
import com.loyalixa.backend.jwt.TokenBlacklistService;
import com.loyalixa.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/v1/admin/security")
@PreAuthorize("hasRole('ADMIN')")
public class SecurityAdminController {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final JwtConfigService jwtConfigService;
    @Value("${jwt.secret.key}")
    private String secretKey;
    public SecurityAdminController(
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            RefreshTokenService refreshTokenService,
            JwtConfigService jwtConfigService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenService = refreshTokenService;
        this.jwtConfigService = jwtConfigService;
    }
    @GetMapping("/settings")
    @PreAuthorize("hasAuthority('security:view')")
    public ResponseEntity<Map<String, Object>> getJwtSettings() {
        Map<String, Object> settings = new HashMap<>();
        long accessTokenMs = jwtConfigService.getAccessTokenDurationMs();
        long refreshTokenMs = jwtConfigService.getRefreshTokenDurationMs();
        settings.put("accessTokenDurationMs", accessTokenMs);
        settings.put("accessTokenDurationMinutes", accessTokenMs / 60000.0);
        settings.put("accessTokenDurationHours", accessTokenMs / 3600000.0);
        settings.put("refreshTokenDurationMs", refreshTokenMs);
        settings.put("refreshTokenDurationMinutes", refreshTokenMs / 60000.0);
        settings.put("refreshTokenDurationDays", refreshTokenMs / 86400000.0);
        settings.put("secretKeyLength", secretKey != null ? secretKey.length() : 0);
        settings.put("secretKeyPreview", secretKey != null && secretKey.length() > 10 
            ? secretKey.substring(0, 10) + "..." : "N/A");
        settings.put("defaultAccessTokenDurationMs", jwtConfigService.getDefaultAccessTokenDurationMs());
        settings.put("defaultRefreshTokenDurationMs", jwtConfigService.getDefaultRefreshTokenDurationMs());
        return ResponseEntity.ok(settings);
    }
    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('security:manage')")
    public ResponseEntity<Map<String, Object>> updateJwtSettings(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (request.containsKey("accessTokenDurationMs")) {
                Object accessTokenValue = request.get("accessTokenDurationMs");
                if (accessTokenValue instanceof Number) {
                    long accessTokenMs = ((Number) accessTokenValue).longValue();
                    jwtConfigService.setAccessTokenDurationMs(accessTokenMs);
                    result.put("accessTokenDurationMs", accessTokenMs);
                    result.put("accessTokenDurationMinutes", accessTokenMs / 60000.0);
                } else {
                    result.put("error", "accessTokenDurationMs must be a number");
                    return ResponseEntity.badRequest().body(result);
                }
            }
            if (request.containsKey("refreshTokenDurationMs")) {
                Object refreshTokenValue = request.get("refreshTokenDurationMs");
                if (refreshTokenValue instanceof Number) {
                    long refreshTokenMs = ((Number) refreshTokenValue).longValue();
                    jwtConfigService.setRefreshTokenDurationMs(refreshTokenMs);
                    result.put("refreshTokenDurationMs", refreshTokenMs);
                    result.put("refreshTokenDurationDays", refreshTokenMs / 86400000.0);
                } else {
                    result.put("error", "refreshTokenDurationMs must be a number");
                    return ResponseEntity.badRequest().body(result);
                }
            }
            if (request.containsKey("resetToDefaults") && Boolean.TRUE.equals(request.get("resetToDefaults"))) {
                jwtConfigService.resetToDefaults();
                result.put("message", "Settings reset to default values");
                result.put("accessTokenDurationMs", jwtConfigService.getAccessTokenDurationMs());
                result.put("refreshTokenDurationMs", jwtConfigService.getRefreshTokenDurationMs());
            }
            if (result.isEmpty()) {
                result.put("message", "No settings were updated. Provide accessTokenDurationMs or refreshTokenDurationMs");
                return ResponseEntity.badRequest().body(result);
            }
            result.put("success", true);
            result.put("message", "JWT settings updated successfully. New tokens will use the new durations.");
            result.put("note", "Existing tokens will continue to use their original expiration times.");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("error", "Failed to update settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @PostMapping("/tokens/validate")
    @PreAuthorize("hasAuthority('security:manage')")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token is required");
            return ResponseEntity.badRequest().body(error);
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Map<String, Object> result = new HashMap<>();
        try {
            Claims claims;
            try {
                claims = jwtService.extractAllClaims(token);
            } catch (ExpiredJwtException e) {
                result.put("valid", false);
                result.put("expired", true);
                result.put("blacklisted", tokenBlacklistService.isTokenBlacklisted(token));
                Claims expiredClaims = e.getClaims();
                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("subject", expiredClaims.getSubject());
                tokenInfo.put("userId", expiredClaims.get("userId"));
                tokenInfo.put("role", expiredClaims.get("role"));
                tokenInfo.put("issuedAt", expiredClaims.getIssuedAt());
                tokenInfo.put("expiration", expiredClaims.getExpiration());
                result.put("tokenInfo", tokenInfo);
                return ResponseEntity.ok(result);
            }
            result.put("valid", true);
            result.put("expired", false);
            result.put("blacklisted", tokenBlacklistService.isTokenBlacklisted(token));
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", claims.getSubject());  
            tokenInfo.put("userId", claims.get("userId"));
            tokenInfo.put("role", claims.get("role"));
            tokenInfo.put("issuedAt", claims.getIssuedAt());
            tokenInfo.put("expiration", claims.getExpiration());
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remainingMs = expiration.getTime() - now.getTime();
            tokenInfo.put("remainingMs", remainingMs);
            tokenInfo.put("remainingMinutes", remainingMs / 60000.0);
            tokenInfo.put("remainingHours", remainingMs / 3600000.0);
            result.put("tokenInfo", tokenInfo);
        } catch (ExpiredJwtException e) {
            result.put("valid", false);
            result.put("expired", true);
            result.put("blacklisted", tokenBlacklistService.isTokenBlacklisted(token));
            Claims claims = e.getClaims();
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", claims.getSubject());
            tokenInfo.put("userId", claims.get("userId"));
            tokenInfo.put("role", claims.get("role"));
            tokenInfo.put("issuedAt", claims.getIssuedAt());
            tokenInfo.put("expiration", claims.getExpiration());
            result.put("tokenInfo", tokenInfo);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/tokens/blacklist")
    @PreAuthorize("hasAuthority('security:manage')")
    public ResponseEntity<Map<String, Object>> blacklistToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token is required");
            return ResponseEntity.badRequest().body(error);
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Map<String, Object> result = new HashMap<>();
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                result.put("alreadyBlacklisted", true);
                result.put("message", "Token is already blacklisted");
                return ResponseEntity.ok(result);
            }
            Date expiration = jwtService.getExpiration(token);
            long expirySeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            if (expirySeconds > 0) {
                tokenBlacklistService.blacklistToken(token, expirySeconds);
                result.put("success", true);
                result.put("message", "Token added to blacklist successfully");
                result.put("expiresInSeconds", expirySeconds);
            } else {
                result.put("success", false);
                result.put("message", "Token is already expired");
            }
        } catch (ExpiredJwtException e) {
            result.put("success", false);
            result.put("message", "Token is already expired");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('security:view')")
    public ResponseEntity<Map<String, Object>> getSecurityStats(@AuthenticationPrincipal User adminUser) {
        Map<String, Object> stats = new HashMap<>();
        try {
            long totalRefreshTokens = refreshTokenService.getTotalActiveRefreshTokens();
            stats.put("totalActiveRefreshTokens", totalRefreshTokens);
            if (adminUser != null) {
                stats.put("currentAdminEmail", adminUser.getEmail());
                stats.put("currentAdminRole", adminUser.getRole() != null ? adminUser.getRole().getName() : "N/A");
            }
            stats.put("accessTokenDurationMs", jwtConfigService.getAccessTokenDurationMs());
            stats.put("refreshTokenDurationMs", jwtConfigService.getRefreshTokenDurationMs());
        } catch (Exception e) {
            stats.put("error", "Failed to retrieve some statistics: " + e.getMessage());
        }
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/refresh/current")
    @PreAuthorize("hasAuthority('security:view')")
    public ResponseEntity<Map<String, Object>> getCurrentRefreshTokenInfo(@AuthenticationPrincipal User user) {
        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            result.put("error", "No authenticated user context");
            return ResponseEntity.badRequest().body(result);
        }
        try {
            List<RefreshToken> activeTokens = refreshTokenService
                    .findAllActiveByUserId(user.getId());
            Instant now = Instant.now();
            List<Map<String, Object>> refreshTokensList = new java.util.ArrayList<>();
            for (RefreshToken rt : activeTokens) {
                Map<String, Object> tokenInfo = new HashMap<>();
                String token = rt.getToken();
                String masked = token == null ? "" : (token.length() <= 8 ? token : token.substring(0, 4) + "..." + token.substring(token.length() - 4));
                boolean expired = rt.getExpiryDate() != null && rt.getExpiryDate().isBefore(now);
                tokenInfo.put("tokenMasked", masked);
                tokenInfo.put("tokenId", rt.getId());
                tokenInfo.put("deviceId", rt.getDeviceId());
                tokenInfo.put("deviceName", rt.getDeviceName() != null ? rt.getDeviceName() : "Unknown Device");
                tokenInfo.put("deviceType", rt.getDeviceType() != null ? rt.getDeviceType() : "UNKNOWN");
                tokenInfo.put("userAgent", rt.getUserAgent());
                tokenInfo.put("ipAddress", rt.getIpAddress());
                tokenInfo.put("expiryDate", rt.getExpiryDate());
                tokenInfo.put("lastUsedAt", rt.getLastUsedAt());
                tokenInfo.put("createdAt", rt.getCreatedAt());
                tokenInfo.put("expired", expired);
                if (rt.getExpiryDate() != null) {
                    long remainingMs = ChronoUnit.MILLIS.between(now, rt.getExpiryDate());
                    tokenInfo.put("remainingMs", remainingMs);
                    tokenInfo.put("remainingHours", remainingMs / 3600000.0);
                    tokenInfo.put("remainingDays", remainingMs / 86400000.0);
                }
                refreshTokensList.add(tokenInfo);
            }
            result.put("hasRefreshToken", !refreshTokensList.isEmpty());
            result.put("refreshTokensCount", refreshTokensList.size());
            result.put("refreshTokens", refreshTokensList);
            result.put("userId", user.getId());
            result.put("userEmail", user.getEmail());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", "Failed to load refresh token info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @GetMapping("/tokens/current")
    @PreAuthorize("hasAuthority('security:view')")
    public ResponseEntity<Map<String, Object>> getCurrentTokenInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal User user) {
        Map<String, Object> result = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            result.put("error", "No authorization header found");
            return ResponseEntity.badRequest().body(result);
        }
        String token = authHeader.substring(7);
        try {
            Claims claims;
            try {
                claims = jwtService.extractAllClaims(token);
            } catch (ExpiredJwtException e) {
                result.put("expired", true);
                Claims expiredClaims = e.getClaims();
                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("subject", expiredClaims.getSubject());
                tokenInfo.put("userId", expiredClaims.get("userId"));
                tokenInfo.put("role", expiredClaims.get("role"));
                tokenInfo.put("expiration", expiredClaims.getExpiration());
                result.put("tokenInfo", tokenInfo);
                result.put("userEmail", user != null ? user.getEmail() : "N/A");
                return ResponseEntity.ok(result);
            }
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", claims.getSubject());
            tokenInfo.put("userId", claims.get("userId"));
            tokenInfo.put("role", claims.get("role"));
            tokenInfo.put("issuedAt", claims.getIssuedAt());
            tokenInfo.put("expiration", claims.getExpiration());
            tokenInfo.put("blacklisted", tokenBlacklistService.isTokenBlacklisted(token));
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remainingMs = expiration.getTime() - now.getTime();
            tokenInfo.put("remainingMs", remainingMs);
            tokenInfo.put("remainingMinutes", Math.round(remainingMs / 60000.0 * 100.0) / 100.0);
            result.put("tokenInfo", tokenInfo);
            result.put("userEmail", user != null ? user.getEmail() : "N/A");
        } catch (ExpiredJwtException e) {
            result.put("expired", true);
            Claims claims = e.getClaims();
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", claims.getSubject());
            tokenInfo.put("userId", claims.get("userId"));
            tokenInfo.put("role", claims.get("role"));
            tokenInfo.put("expiration", claims.getExpiration());
            result.put("tokenInfo", tokenInfo);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        return ResponseEntity.ok(result);
    }
    @DeleteMapping("/refresh/devices/{deviceId}")
    public ResponseEntity<Map<String, Object>> deleteDeviceRefreshToken(
            @PathVariable String deviceId,
            @AuthenticationPrincipal User user) {
        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            result.put("error", "No authenticated user context");
            return ResponseEntity.badRequest().body(result);
        }
        try {
            refreshTokenService.deleteByDeviceId(user.getId(), deviceId);
            result.put("refreshTokenDeleted", true);
            int blacklistedTokensCount = tokenBlacklistService.blacklistTokensByDeviceId(deviceId);
            result.put("accessTokensBlacklisted", blacklistedTokensCount);
            result.put("success", true);
            result.put("message", "Device logged out successfully. Refresh token has been revoked and " + 
                       blacklistedTokensCount + " access token(s) have been invalidated.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to logout device: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}

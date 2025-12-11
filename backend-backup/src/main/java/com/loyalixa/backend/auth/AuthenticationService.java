package com.loyalixa.backend.auth;
import com.loyalixa.backend.jwt.JwtService;
import com.loyalixa.backend.jwt.TokenBlacklistService;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.loyalixa.backend.security.RefreshToken;
import com.loyalixa.backend.security.RefreshTokenService;
import com.loyalixa.backend.security.DeviceFingerprintService;
@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final DeviceFingerprintService deviceFingerprintService;
    private final TokenBlacklistService tokenBlacklistService;
    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
            JwtService jwtService, RefreshTokenService refreshTokenService,
            DeviceFingerprintService deviceFingerprintService, TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.deviceFingerprintService = deviceFingerprintService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            logger.debug("Starting login process for email: {}", request.email());
            logger.debug("Authenticating user credentials");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()));
            logger.debug("Fetching user from database");
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
            LoginResponse.SuspensionBanInfo suspensionBanInfo = null;
            logger.debug("Updating last login time for user: {}", user.getId());
            user.setLastLoginAt(LocalDateTime.now());
            logger.debug("Generating device fingerprint");
            String deviceId = deviceFingerprintService.generateDeviceFingerprint(
                    httpRequest,
                    request.screenWidth(),
                    request.screenHeight(),
                    request.timezone(),
                    request.platform() != null ? request.platform() : request.deviceType(),
                    request.hardwareConcurrency(),
                    request.deviceMemory(),
                    request.devicePixelRatio(),
                    request.touchSupport());
            if (deviceId == null || deviceId.isEmpty()) {
                logger.warn("Device fingerprint is null or empty, generating fallback device ID");
                deviceId = UUID.randomUUID().toString();
            }
            String deviceName = request.deviceName();
            String deviceType = request.deviceType() != null ? request.deviceType() : extractDeviceType(httpRequest);
            String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
            String ipAddress = resolveClientIp(httpRequest);
            logger.debug("Populating user device metadata");
            populateUserDeviceMetadata(user, httpRequest, request);
            logger.debug("Generating access token");
            String accessToken = jwtService.generateAccessToken(user, deviceId);
            if (deviceId != null && !deviceId.isEmpty()) {
                tokenBlacklistService.registerTokenForDevice(accessToken, deviceId);
            }
            logger.debug("Creating refresh token");
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user, deviceId, deviceName, deviceType, userAgent, ipAddress);
            logger.debug("Saving user with updated device metadata");
            userRepository.save(user);
            logger.debug("Login successful for user: {}", user.getId());
            Boolean canAccessDashboard = user.getCanAccessDashboard();
            return new LoginResponse(
                    accessToken,
                    refreshToken.getToken(),
                    user.getUsername(),
                    user.getId(),
                    suspensionBanInfo,
                    canAccessDashboard);
        } catch (Exception e) {
            logger.error("Error in login process for email: " + request.email(), e);
            throw e;
        }
    }
    public LoginResponse login(LoginRequest request) {
        return login(request, null);
    }
    private String extractDeviceType(HttpServletRequest request) {
        if (request == null)
            return "UNKNOWN";
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isEmpty())
            return "UNKNOWN";
        String u = ua.toLowerCase();
        if (u.contains("mobile") || u.contains("iphone") || (u.contains("android") && !u.contains("tablet"))) {
            return "MOBILE";
        } else if (u.contains("ipad") || u.contains("tablet") || (u.contains("android") && u.contains("tablet"))) {
            return "TABLET";
        } else if (u.contains("bot") || u.contains("spider") || u.contains("crawler")) {
            return "BOT";
        } else {
            return "DESKTOP";
        }
    }
    private String resolveClientIp(HttpServletRequest request) {
        if (request == null)
            return null;
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP" };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.trim().isEmpty()) {
                int comma = value.indexOf(',');
                return comma > 0 ? value.substring(0, comma).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }
    private void populateUserDeviceMetadata(User user, HttpServletRequest httpRequest, LoginRequest request) {
        if (user == null)
            return;
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        String ipAddress = resolveClientIp(httpRequest);
        String acceptLanguage = httpRequest != null ? httpRequest.getHeader("Accept-Language") : null;
        String acceptEncoding = httpRequest != null ? httpRequest.getHeader("Accept-Encoding") : null;
        String dnt = httpRequest != null ? httpRequest.getHeader("DNT") : null;
        user.setUserAgent(userAgent);
        if (userAgent != null && !userAgent.isEmpty()) {
            user.setBrowser(extractBrowser(userAgent));
            user.setOperatingSystem(extractOperatingSystem(userAgent));
        }
        String deviceType = request.deviceType() != null ? request.deviceType() : extractDeviceType(httpRequest);
        user.setDeviceType(deviceType);
        user.setIpAddress(ipAddress);
        user.setAcceptLanguage(acceptLanguage);
        user.setAcceptEncoding(acceptEncoding);
        user.setDnt(dnt);
        String referrer = httpRequest != null ? httpRequest.getHeader("Referer") : null;
        String host = httpRequest != null ? httpRequest.getHeader("Host") : null;
        String origin = httpRequest != null ? httpRequest.getHeader("Origin") : null;
        user.setReferrer(referrer);
        user.setHost(host);
        user.setOrigin(origin);
        user.setTimezone(request.timezone());
        user.setPlatform(request.platform() != null ? request.platform() : request.deviceType());
        user.setScreenWidth(request.screenWidth());
        user.setScreenHeight(request.screenHeight());
        user.setViewportWidth(request.viewportWidth());
        user.setViewportHeight(request.viewportHeight());
        user.setDevicePixelRatio(request.devicePixelRatio());
        user.setHardwareConcurrency(request.hardwareConcurrency());
        user.setDeviceMemoryGb(request.deviceMemory());
        user.setTouchSupport(request.touchSupport());
    }
    private String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty())
            return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/") || ua.contains("edge/"))
            return "Edge";
        if (ua.contains("chrome/") && !ua.contains("edg/"))
            return "Chrome";
        if (ua.contains("safari/") && !ua.contains("chrome/"))
            return "Safari";
        if (ua.contains("firefox/"))
            return "Firefox";
        if (ua.contains("opera/") || ua.contains("opr/"))
            return "Opera";
        if (ua.contains("msie") || ua.contains("trident/"))
            return "Internet Explorer";
        return "UNKNOWN";
    }
    private String extractOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty())
            return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 10.0"))
            return "Windows 10";
        if (ua.contains("windows nt 6.3"))
            return "Windows 8.1";
        if (ua.contains("windows nt 6.2"))
            return "Windows 8";
        if (ua.contains("windows nt 6.1"))
            return "Windows 7";
        if (ua.contains("windows"))
            return "Windows";
        if (ua.contains("mac os x") || ua.contains("macintosh"))
            return "macOS";
        if (ua.contains("android"))
            return "Android";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios"))
            return "iOS";
        if (ua.contains("linux"))
            return "Linux";
        if (ua.contains("ubuntu"))
            return "Ubuntu";
        if (ua.contains("fedora"))
            return "Fedora";
        return "UNKNOWN";
    }
    @Transactional
    public LoginResponse refreshToken(String oldRefreshToken, String currentAccessToken) {
        RefreshToken refreshToken = refreshTokenService.findByToken(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token is invalid or expired."));
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenService.deleteByToken(oldRefreshToken);
            throw new IllegalStateException("Refresh Token expired. Please log in again.");
        }
        refreshTokenService.updateLastUsed(oldRefreshToken);
        User user = refreshToken.getUser();
        String status = user.getStatus();
        if ("SUSPENDED".equals(status) || "BANNED".equals(status) || "DELETED".equals(status)
                || "BLOCKED".equals(status)) {
            refreshTokenService.logoutUser(user.getId());
            throw new IllegalStateException("Account is " + status.toLowerCase() + ". Refresh token revoked.");
        }
        if (currentAccessToken != null && !currentAccessToken.trim().isEmpty()) {
            try {
                if (jwtService.getExpiration(currentAccessToken).after(new Date())) {
                    refreshTokenService.deleteByToken(oldRefreshToken);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                            user,
                            refreshToken.getDeviceId(),
                            refreshToken.getDeviceName(),
                            refreshToken.getDeviceType(),
                            refreshToken.getUserAgent(),
                            refreshToken.getIpAddress());
                    LoginResponse.SuspensionBanInfo suspensionBanInfo = null;
                    return new LoginResponse(
                            currentAccessToken,
                            newRefreshToken.getToken(),
                            user.getUsername(),
                            user.getId(),
                            suspensionBanInfo,
                            user.getCanAccessDashboard());
                }
            } catch (Exception e) {
            }
        }
        String newAccessToken = jwtService.generateAccessToken(user, refreshToken.getDeviceId());
        if (refreshToken.getDeviceId() != null && !refreshToken.getDeviceId().isEmpty()) {
            tokenBlacklistService.registerTokenForDevice(newAccessToken, refreshToken.getDeviceId());
        }
        refreshTokenService.deleteByToken(oldRefreshToken);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                user,
                refreshToken.getDeviceId(),
                refreshToken.getDeviceName(),
                refreshToken.getDeviceType(),
                refreshToken.getUserAgent(),
                refreshToken.getIpAddress());
        LoginResponse.SuspensionBanInfo suspensionBanInfo = null;
        return new LoginResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                user.getUsername(),
                user.getId(),
                suspensionBanInfo,
                user.getCanAccessDashboard());
    }
}
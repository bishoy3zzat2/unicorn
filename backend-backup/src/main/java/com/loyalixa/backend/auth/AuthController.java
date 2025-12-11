package com.loyalixa.backend.auth;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserService;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.user.UserAdminService;
import com.loyalixa.backend.jwt.JwtService;
import com.loyalixa.backend.jwt.TokenBlacklistService;
import com.loyalixa.backend.security.RefreshTokenService;
import com.loyalixa.backend.security.RefreshToken;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationService authService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;
    private final UserAdminService userAdminService;
    public AuthController(UserService userService, UserRepository userRepository, AuthenticationService authService,
            RefreshTokenService refreshTokenService, TokenBlacklistService tokenBlacklistService, JwtService jwtService,
            UserAdminService userAdminService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
        this.userAdminService = userAdminService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            User newUser = userService.registerUser(
                    request.username(),
                    request.email(),
                    request.password(),
                    httpRequest,
                    request.deviceType(),
                    request.timezone(),
                    request.platform(),
                    request.screenWidth(),
                    request.screenHeight(),
                    request.viewportWidth(),
                    request.viewportHeight(),
                    request.hardwareConcurrency(),
                    request.deviceMemory(),
                    request.devicePixelRatio(),
                    request.touchSupport());
            return ResponseEntity.ok("User registered successfully! ID: " + newUser.getId());
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.email());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userAdminService.checkAndReactivateUserIfExpired(user);
                user = userRepository.findByEmail(request.email()).orElse(user);
                String status = user.getStatus();
                if ("SUSPENDED".equals(status) || "BANNED".equals(status) || "DELETED".equals(status)
                        || "BLOCKED".equals(status)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Account is " + status.toLowerCase());
                    errorResponse.put("status", status);
                    errorResponse.put("code", "ACCOUNT_" + status);
                    if ("SUSPENDED".equals(status)) {
                        Map<String, Object> suspensionInfo = new HashMap<>();
                        suspensionInfo.put("action", "SUSPENDED");
                        suspensionInfo.put("reason", user.getSuspendReason());
                        suspensionInfo.put("actionAt", user.getSuspendedAt());
                        suspensionInfo.put("until", user.getSuspendedUntil());
                        suspensionInfo.put("type", user.getSuspensionType());
                        suspensionInfo.put("isTemporary", "TEMPORARY".equals(user.getSuspensionType()));
                        errorResponse.put("suspensionBanInfo", suspensionInfo);
                    } else if ("BANNED".equals(status)) {
                        Map<String, Object> banInfo = new HashMap<>();
                        banInfo.put("action", "BANNED");
                        banInfo.put("reason", user.getBanReason());
                        banInfo.put("actionAt", user.getBannedAt());
                        banInfo.put("until", user.getBannedUntil());
                        banInfo.put("type", user.getBanType());
                        banInfo.put("isTemporary", "TEMPORARY".equals(user.getBanType()));
                        errorResponse.put("suspensionBanInfo", banInfo);
                    } else if ("DELETED".equals(status)) {
                        Map<String, Object> deletionInfo = new HashMap<>();
                        deletionInfo.put("action", "DELETED");
                        deletionInfo.put("reason", user.getDeletionReason());
                        deletionInfo.put("deletedAt", user.getDeletedAt());
                        errorResponse.put("suspensionBanInfo", deletionInfo);
                    } else if ("BLOCKED".equals(status)) {
                        Map<String, Object> blockInfo = new HashMap<>();
                        blockInfo.put("action", "BLOCKED");
                        errorResponse.put("suspensionBanInfo", blockInfo);
                    }
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                            .body(errorResponse);
                }
            }
            LoginResponse response = authService.login(request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or password");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        } catch (Exception e) {
            logger.error("Error during login for email: " + request.email(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred during login. Please try again later.");
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + ": "
                        + (e.getCause() != null ? e.getCause().getMessage() : "Unknown error");
            }
            errorResponse.put("message", errorMessage);
            errorResponse.put("exceptionType", e.getClass().getSimpleName());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    @GetMapping("/check-status")
    public ResponseEntity<Map<String, Object>> checkAccountStatus(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                response.put("exists", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }
            User user = userOpt.get();
            userAdminService.checkAndReactivateUserIfExpired(user);
            user = userRepository.findByEmail(email).orElse(user);
            response.put("exists", true);
            response.put("status", user.getStatus());
            response.put("email", user.getEmail());
            String status = user.getStatus();
            response.put("code", "ACCOUNT_" + status);
            if ("SUSPENDED".equals(status)) {
                Map<String, Object> suspensionInfo = new HashMap<>();
                suspensionInfo.put("action", "SUSPENDED");
                suspensionInfo.put("reason", user.getSuspendReason());
                suspensionInfo.put("actionAt", user.getSuspendedAt());
                suspensionInfo.put("until", user.getSuspendedUntil());
                suspensionInfo.put("type", user.getSuspensionType());
                suspensionInfo.put("isTemporary", "TEMPORARY".equals(user.getSuspensionType()));
                response.put("suspensionBanInfo", suspensionInfo);
            } else if ("BANNED".equals(status)) {
                Map<String, Object> banInfo = new HashMap<>();
                banInfo.put("action", "BANNED");
                banInfo.put("reason", user.getBanReason());
                banInfo.put("actionAt", user.getBannedAt());
                banInfo.put("until", user.getBannedUntil());
                banInfo.put("type", user.getBanType());
                banInfo.put("isTemporary", "TEMPORARY".equals(user.getBanType()));
                response.put("suspensionBanInfo", banInfo);
            } else if ("DELETED".equals(status)) {
                Map<String, Object> deletionInfo = new HashMap<>();
                deletionInfo.put("action", "DELETED");
                deletionInfo.put("reason", user.getDeletionReason());
                deletionInfo.put("deletedAt", user.getDeletedAt());
                response.put("suspensionBanInfo", deletionInfo);
            } else if ("BLOCKED".equals(status)) {
                Map<String, Object> blockInfo = new HashMap<>();
                blockInfo.put("action", "BLOCKED");
                response.put("suspensionBanInfo", blockInfo);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String currentAccessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                currentAccessToken = authHeader.substring(7);
            }
            LoginResponse response = authService.refreshToken(request.refreshToken(), currentAccessToken);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            try {
                RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken()).orElse(null);
                if (refreshToken != null && refreshToken.getUser() != null) {
                    User user = refreshToken.getUser();
                    String status = user.getStatus();
                    errorResponse.put("status", status);
                    errorResponse.put("code", "ACCOUNT_" + status);
                    if ("SUSPENDED".equals(status)) {
                        Map<String, Object> suspensionInfo = new HashMap<>();
                        suspensionInfo.put("action", "SUSPENDED");
                        suspensionInfo.put("reason", user.getSuspendReason());
                        suspensionInfo.put("actionAt", user.getSuspendedAt());
                        suspensionInfo.put("until", user.getSuspendedUntil());
                        suspensionInfo.put("type", user.getSuspensionType());
                        suspensionInfo.put("isTemporary", "TEMPORARY".equals(user.getSuspensionType()));
                        errorResponse.put("suspensionBanInfo", suspensionInfo);
                    } else if ("BANNED".equals(status)) {
                        Map<String, Object> banInfo = new HashMap<>();
                        banInfo.put("action", "BANNED");
                        banInfo.put("reason", user.getBanReason());
                        banInfo.put("actionAt", user.getBannedAt());
                        banInfo.put("until", user.getBannedUntil());
                        banInfo.put("type", user.getBanType());
                        banInfo.put("isTemporary", "TEMPORARY".equals(user.getBanType()));
                        errorResponse.put("suspensionBanInfo", banInfo);
                    } else if ("DELETED".equals(status)) {
                        Map<String, Object> deletionInfo = new HashMap<>();
                        deletionInfo.put("action", "DELETED");
                        deletionInfo.put("reason", user.getDeletionReason());
                        deletionInfo.put("deletedAt", user.getDeletedAt());
                        errorResponse.put("suspensionBanInfo", deletionInfo);
                    } else if ("BLOCKED".equals(status)) {
                        Map<String, Object> blockInfo = new HashMap<>();
                        blockInfo.put("action", "BLOCKED");
                        errorResponse.put("suspensionBanInfo", blockInfo);
                    }
                }
            } catch (Exception ex) {
                logger.debug("Could not fetch user details for error response", ex);
            }
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal User user) {
        if (user == null || authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid logout request.");
        }
        String accessToken = authHeader.substring(7);
        String deviceId = null;
        try {
            Claims claims = jwtService.extractAllClaims(accessToken);
            Object deviceIdObj = claims.get("deviceId");
            if (deviceIdObj != null) {
                deviceId = deviceIdObj.toString();
            }
        } catch (Exception e) {
        }
        long expirySeconds = jwtService.getExpiration(accessToken).getTime() - System.currentTimeMillis();
        if (expirySeconds > 0) {
            tokenBlacklistService.blacklistToken(accessToken, expirySeconds / 1000, deviceId);
        }
        refreshTokenService.logoutUser(user.getId());
        return ResponseEntity.ok("User logged out successfully and tokens revoked.");
    }
}
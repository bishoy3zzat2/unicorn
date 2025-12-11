package com.loyalixa.backend.user;
import com.loyalixa.backend.user.dto.PageResponse;
import com.loyalixa.backend.user.dto.UserAdminRequest;
import com.loyalixa.backend.user.dto.UserAdminResponse;
import com.loyalixa.backend.user.dto.UserSearchRequest;
import com.loyalixa.backend.user.dto.UserStatsResponse;
import com.loyalixa.backend.user.dto.UserFullDetailsResponse;
import com.loyalixa.backend.user.dto.UserFullDetailsUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final com.loyalixa.backend.security.RefreshTokenService refreshTokenService;
    private final com.loyalixa.backend.jwt.TokenBlacklistService tokenBlacklistService;
    private final com.loyalixa.backend.config.JwtConfigService jwtConfigService;
    private final com.loyalixa.backend.jwt.JwtService jwtService;
    private final UserRepository userRepository;
    public UserAdminController(
            UserAdminService userAdminService,
            com.loyalixa.backend.security.RefreshTokenService refreshTokenService,
            com.loyalixa.backend.jwt.TokenBlacklistService tokenBlacklistService,
            com.loyalixa.backend.config.JwtConfigService jwtConfigService,
            com.loyalixa.backend.jwt.JwtService jwtService,
            UserRepository userRepository
    ) {
        this.userAdminService = userAdminService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtConfigService = jwtConfigService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('user:view_all') or hasRole('ADMIN') or " +
                  "hasAnyAuthority('course:create', 'course:update', 'quiz:create', 'quiz:update')")
    public ResponseEntity<PageResponse<UserAdminResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) List<String> authProviders,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastLoginFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastLoginTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime passwordChangedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime passwordChangedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime suspendedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime suspendedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bannedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bannedTo,
            @RequestParam(required = false) List<String> deviceTypes,
            @RequestParam(required = false) List<String> browsers,
            @RequestParam(required = false) List<String> operatingSystems,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Integer maxDevices,
            @RequestParam(required = false) String appealStatus,
            @RequestParam(required = false) Boolean inverse,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UserSearchRequest searchRequest = new UserSearchRequest(
            search, roles, statuses, username, authProviders,
            createdAtFrom, createdAtTo, lastLoginFrom, lastLoginTo,
            passwordChangedFrom, passwordChangedTo,
            suspendedFrom, suspendedTo, bannedFrom, bannedTo,
            deviceTypes, browsers, operatingSystems, ipAddress,
            maxDevices, appealStatus,
            inverse,
            page, size
        );
        return ResponseEntity.ok(userAdminService.searchUsers(searchRequest));
    }
    @GetMapping("/current-authorities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUserAuthorities(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("email", user.getEmail());
            response.put("role", user.getRole() != null ? user.getRole().getName() : null);
            response.put("authorities", user.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(java.util.stream.Collectors.toList()));
            response.put("hasUserViewAll", user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("user:view_all")));
            response.put("hasAdminRole", user.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        } else {
            response.put("error", "User not authenticated");
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('user:view_all') or hasRole('ADMIN')")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(userAdminService.getUserStats());
    }
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> updateUserRoleAndStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserAdminRequest request,
            @AuthenticationPrincipal User adminUser  
    ) {
        try {
            System.out.println("[UserAdminController] Received request for userId: " + userId);
            System.out.println("[UserAdminController] newStatus: " + request.newStatus());
            System.out.println("[UserAdminController] suspensionType: " + request.suspensionType());
            System.out.println("[UserAdminController] suspendedUntil: " + request.suspendedUntil());
            System.out.println("[UserAdminController] banType: " + request.banType());
            System.out.println("[UserAdminController] bannedUntil: " + request.bannedUntil());
            UserAdminResponse updatedUser = userAdminService.updateUserRoleAndStatus(userId, request, adminUser.getId());
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:view_details') or hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> getUserDetails(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(userAdminService.getUserDetails(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/{userId}/full-details")
    @PreAuthorize("hasAuthority('user:view_details') or hasRole('ADMIN')")
    public ResponseEntity<UserFullDetailsResponse> getUserFullDetails(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(userAdminService.getUserFullDetails(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/{userId}/full-details")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<UserFullDetailsResponse> updateUserFullDetails(
            @PathVariable UUID userId,
            @Valid @RequestBody UserFullDetailsUpdateRequest request
    ) {
        try {
            return ResponseEntity.ok(userAdminService.updateUserFullDetails(userId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('user:view_all') or hasRole('ADMIN') or " +
                  "hasAnyAuthority('course:create', 'course:update', 'quiz:create', 'quiz:update')")
    public ResponseEntity<List<UserAdminResponse>> searchUsersMultiple(
            @RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        List<String> identifiers = java.util.Arrays.stream(query.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        List<UserAdminResponse> users = userAdminService.findUsersByMultipleIdentifiers(identifiers);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/{userId}/security")
    @PreAuthorize("hasAuthority('user:view_details') or hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getUserSecurity(@PathVariable java.util.UUID userId) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            int maxDevices = user.getMaxDevices() != null ? user.getMaxDevices() : 1;
            result.put("maxDevices", maxDevices);
            java.util.List<com.loyalixa.backend.security.RefreshToken> activeTokens = refreshTokenService.findAllActiveByUserId(userId);
            java.time.Instant now = java.time.Instant.now();
            java.util.List<java.util.Map<String, Object>> devices = new java.util.ArrayList<>();
            for (com.loyalixa.backend.security.RefreshToken rt : activeTokens) {
                java.util.Map<String, Object> deviceInfo = new java.util.HashMap<>();
                String token = rt.getToken();
                String masked = token == null ? "" : (token.length() <= 8 ? token : token.substring(0, 4) + "..." + token.substring(token.length() - 4));
                deviceInfo.put("tokenMasked", masked);
                deviceInfo.put("deviceId", rt.getDeviceId());
                deviceInfo.put("deviceName", rt.getDeviceName() != null ? rt.getDeviceName() : "Unknown Device");
                deviceInfo.put("deviceType", rt.getDeviceType() != null ? rt.getDeviceType() : "UNKNOWN");
                deviceInfo.put("userAgent", rt.getUserAgent());
                deviceInfo.put("ipAddress", rt.getIpAddress());
                deviceInfo.put("expiryDate", rt.getExpiryDate());
                deviceInfo.put("lastUsedAt", rt.getLastUsedAt());
                deviceInfo.put("createdAt", rt.getCreatedAt());
                boolean expired = rt.getExpiryDate() != null && rt.getExpiryDate().isBefore(now);
                deviceInfo.put("expired", expired);
                if (rt.getExpiryDate() != null) {
                    long remainingMs = java.time.temporal.ChronoUnit.MILLIS.between(now, rt.getExpiryDate());
                    deviceInfo.put("remainingMs", remainingMs);
                }
                devices.add(deviceInfo);
            }
            result.put("activeDevices", devices);
            result.put("activeDeviceCount", devices.size());
            result.put("hasRefreshToken", !devices.isEmpty());
            result.put("accessTokenDurationMs", jwtConfigService.getAccessTokenDurationMs());
            result.put("refreshTokenDurationMs", jwtConfigService.getRefreshTokenDurationMs());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @PutMapping("/{userId}/max-devices")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> updateMaxDevices(
            @PathVariable UUID userId,
            @RequestBody java.util.Map<String, Integer> body
    ) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            Integer maxDevices = body.get("maxDevices");
            if (maxDevices == null || maxDevices < 1) {
                result.put("error", "maxDevices must be at least 1");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setMaxDevices(maxDevices);
            userRepository.save(user);
            long activeCount = refreshTokenService.countActiveDevices(userId);
            if (activeCount > maxDevices) {
                java.util.List<com.loyalixa.backend.security.RefreshToken> activeTokens = refreshTokenService.findAllActiveByUserId(userId);
                int toDelete = (int)(activeCount - maxDevices);
                for (int i = activeTokens.size() - 1; i >= activeTokens.size() - toDelete && i >= 0; i--) {
                    refreshTokenService.deleteByToken(activeTokens.get(i).getToken());
                }
            }
            result.put("success", true);
            result.put("maxDevices", maxDevices);
            result.put("message", "Max devices updated successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @DeleteMapping("/{userId}/devices/{deviceId}")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> deleteDevice(
            @PathVariable UUID userId,
            @PathVariable String deviceId
    ) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            refreshTokenService.deleteByDeviceId(userId, deviceId);
            result.put("success", true);
            result.put("message", "Device logged out successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @PostMapping("/{userId}/logout")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> forceLogoutUser(
            @PathVariable java.util.UUID userId,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            refreshTokenService.logoutUser(userId);
            result.put("refreshTokensDeleted", true);
            String accessToken = body != null ? body.get("accessToken") : null;
            if (accessToken != null && !accessToken.isBlank()) {
                if (accessToken.startsWith("Bearer ")) {
                    accessToken = accessToken.substring(7);
                }
                try {
                    java.util.Date exp = jwtService.getExpiration(accessToken);
                    long expirySeconds = exp != null ? (exp.getTime() - System.currentTimeMillis()) / 1000 : 0;
                    if (expirySeconds > 0) {
                        tokenBlacklistService.blacklistToken(accessToken, expirySeconds);
                        result.put("accessTokenBlacklisted", true);
                        result.put("accessTokenTtlSeconds", expirySeconds);
                    } else {
                        result.put("accessTokenBlacklisted", false);
                        result.put("message", "Access token already expired");
                    }
                } catch (Exception ex) {
                    result.put("accessTokenBlacklisted", false);
                    result.put("accessTokenError", ex.getMessage());
                }
            }
            result.put("success", true);
            result.put("message", "User has been logged out: refresh tokens deleted" + (result.getOrDefault("accessTokenBlacklisted", false).equals(true) ? " and access token blacklisted" : ""));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @PostMapping("/{userId}/appeal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitAppeal(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!currentUser.getId().equals(userId)) {
                result.put("error", "You can only submit an appeal for your own account.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }
            String appealReason = body.get("appealReason");
            if (appealReason == null || appealReason.trim().isEmpty()) {
                result.put("error", "Appeal reason is required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            userAdminService.submitAppeal(userId, appealReason.trim());
            result.put("success", true);
            result.put("message", "Appeal submitted successfully. It will be reviewed by an administrator.");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } catch (IllegalStateException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @GetMapping("/appeals")
    @PreAuthorize("hasAuthority('user:view_all') or hasRole('ADMIN')")
    public ResponseEntity<List<UserAdminResponse>> getPendingAppeals() {
        List<User> pendingAppeals = userAdminService.getPendingAppeals();
        List<UserAdminResponse> response = pendingAppeals.stream()
                .map(user -> userAdminService.mapToUserAdminResponse(user))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{userId}/appeal/review")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reviewAppeal(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User reviewer
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            String decision = body.get("decision");
            if (decision == null || decision.trim().isEmpty()) {
                result.put("error", "Decision is required (APPROVED or REJECTED).");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            userAdminService.reviewAppeal(userId, decision, reviewer.getId());
            result.put("success", true);
            result.put("message", "Appeal reviewed successfully.");
            result.put("decision", decision.toUpperCase());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (IllegalStateException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @PostMapping("/check-expired-suspensions-bans")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkExpiredSuspensionsAndBans() {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("[UserAdminController] Manual check for expired suspensions/bans triggered");
            Map<String, Object> checkResult = userAdminService.checkAndReactivateExpiredSuspensionsAndBans();
            int reactivatedCount = (Integer) checkResult.get("reactivatedCount");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> reactivatedUsers = (List<Map<String, Object>>) checkResult.get("reactivatedUsers");
            result.put("success", true);
            result.put("reactivatedCount", reactivatedCount);
            result.put("reactivatedUsers", reactivatedUsers);
            result.put("message", "Check completed. " + reactivatedCount + " user(s) reactivated.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:manage_roles') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUserPermanently(@PathVariable UUID userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            userAdminService.deleteUserPermanently(userId);
            result.put("success", true);
            result.put("message", "User deleted permanently from database");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
package com.loyalixa.backend.staff;
import com.loyalixa.backend.financial.dto.FinancialTransactionResponse;
import com.loyalixa.backend.financial.dto.PaymentRequestResponse;
import com.loyalixa.backend.financial.dto.TaskPaymentResponse;
import com.loyalixa.backend.jwt.JwtService;
import com.loyalixa.backend.jwt.TokenBlacklistService;
import com.loyalixa.backend.staff.dto.*;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.dto.UserAdminResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/staff")
public class StaffAdminController {
    private final StaffAdminService staffAdminService;
    private final com.loyalixa.backend.security.RefreshTokenService refreshTokenService;
    private final com.loyalixa.backend.config.JwtConfigService jwtConfigService;
    private final com.loyalixa.backend.user.UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;
    public StaffAdminController(
            StaffAdminService staffAdminService,
            com.loyalixa.backend.security.RefreshTokenService refreshTokenService,
            com.loyalixa.backend.config.JwtConfigService jwtConfigService,
            com.loyalixa.backend.user.UserRepository userRepository,
            TokenBlacklistService tokenBlacklistService,
            JwtService jwtService) {
        this.staffAdminService = staffAdminService;
        this.refreshTokenService = refreshTokenService;
        this.jwtConfigService = jwtConfigService;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAdminResponse>> getAllStaff(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskStatus,
            @RequestParam(required = false) Boolean hasPendingTasks,
            @RequestParam(required = false) Boolean hasRejectedTasks,
            @RequestParam(required = false) Boolean hasCompletedTasks,
            @RequestParam(required = false) String paymentRequestStatus,
            @RequestParam(required = false) Boolean hasPaymentRequests,
            @RequestParam(required = false) Boolean hasDiscounts,
            @RequestParam(required = false) Boolean hasBonuses,
            @RequestParam(required = false) Boolean hasPenalties,
            @RequestParam(required = false) Boolean hasDeposits,
            @RequestParam(required = false) UUID roleId) {
        StaffSearchRequest searchRequest = new StaffSearchRequest(
                search, role, status, taskStatus,
                hasPendingTasks, hasRejectedTasks, hasCompletedTasks,
                paymentRequestStatus, hasPaymentRequests,
                hasDiscounts, hasBonuses, hasPenalties, hasDeposits,
                roleId, 0, 1000
        );
        List<UserAdminResponse> staff = staffAdminService.searchStaff(searchRequest);
        return ResponseEntity.ok(staff);
    }
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffResponse> getStaffDetails(@PathVariable UUID userId) {
        StaffResponse staff = staffAdminService.getStaffDetails(userId);
        return ResponseEntity.ok(staff);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> createStaff(
            @Valid @RequestBody StaffCreateRequest request) {
        UserAdminResponse staff = staffAdminService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(staff);
    }
    @PostMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskPaymentResponse> createTask(
            @AuthenticationPrincipal User adminUser,
            @Valid @RequestBody TaskCreateRequest request) {
        TaskPaymentResponse task = staffAdminService.createTask(request, adminUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    @PutMapping("/tasks/{taskId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskPaymentResponse> approveTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal User adminUser,
            @Valid @RequestBody TaskApprovalRequest request) {
        TaskPaymentResponse task = staffAdminService.approveTask(taskId, request, adminUser);
        return ResponseEntity.ok(task);
    }
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaskPaymentResponse>> getAllTasks(
            @RequestParam(required = false) String status) {
        List<TaskPaymentResponse> tasks = staffAdminService.getAllTasks(status);
        return ResponseEntity.ok(tasks);
    }
    @GetMapping("/tasks/completed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaskPaymentResponse>> getCompletedTasksForReview() {
        List<TaskPaymentResponse> tasks = staffAdminService.getCompletedTasksForReview();
        return ResponseEntity.ok(tasks);
    }
    @PutMapping("/payment-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentRequestResponse> approvePaymentRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal User adminUser,
            @Valid @RequestBody PaymentRequestApprovalRequest request) {
        PaymentRequestResponse paymentRequest = staffAdminService.approvePaymentRequest(requestId, request, adminUser);
        return ResponseEntity.ok(paymentRequest);
    }
    @GetMapping("/payment-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentRequestResponse>> getAllPaymentRequests(
            @RequestParam(required = false) String status) {
        List<PaymentRequestResponse> requests = staffAdminService.getAllPaymentRequests(status);
        return ResponseEntity.ok(requests);
    }
    @PostMapping("/{userId}/financial-adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialTransactionResponse> adjustStaffAccount(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User adminUser,
            @Valid @RequestBody FinancialAdjustmentRequest request) {
        FinancialTransactionResponse transaction = staffAdminService.adjustStaffAccount(userId, request, adminUser);
        return ResponseEntity.ok(transaction);
    }
    @PutMapping("/{userId}/financial-account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.loyalixa.backend.financial.dto.FinancialAccountResponse> updateFinancialAccount(
            @PathVariable UUID userId,
            @Valid @RequestBody FinancialAccountUpdateRequest request) {
        com.loyalixa.backend.financial.dto.FinancialAccountResponse account = 
                staffAdminService.updateFinancialAccount(userId, request);
        return ResponseEntity.ok(account);
    }
    @PutMapping("/{userId}/employment-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.loyalixa.backend.financial.dto.FinancialAccountResponse> updateEmploymentInfo(
            @PathVariable UUID userId,
            @Valid @RequestBody EmploymentInfoUpdateRequest request) {
        com.loyalixa.backend.financial.dto.FinancialAccountResponse account = 
                staffAdminService.updateEmploymentInfo(userId, request);
        return ResponseEntity.ok(account);
    }
    @GetMapping("/{userId}/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getStaffSecurity(@PathVariable UUID userId) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            com.loyalixa.backend.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (user.getRole() == null || "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
                throw new IllegalArgumentException("User is not a staff member");
            }
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> updateMaxDevices(
            @PathVariable UUID userId,
            @RequestBody java.util.Map<String, Integer> body) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            Integer maxDevices = body.get("maxDevices");
            if (maxDevices == null || maxDevices < 1) {
                result.put("error", "maxDevices must be at least 1");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            com.loyalixa.backend.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (user.getRole() == null || "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
                throw new IllegalArgumentException("User is not a staff member");
            }
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> deleteDevice(
            @PathVariable UUID userId,
            @PathVariable String deviceId,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        try {
            refreshTokenService.deleteByDeviceId(userId, deviceId);
            String accessToken = body != null ? body.get("accessToken") : null;
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                if (accessToken.startsWith("Bearer ")) {
                    accessToken = accessToken.substring(7);
                }
                try {
                    io.jsonwebtoken.Claims claims = jwtService.extractAllClaims(accessToken);
                    Object tokenUserIdObj = claims.get("userId");
                    UUID tokenUserId = null;
                    if (tokenUserIdObj instanceof String) {
                        tokenUserId = UUID.fromString((String) tokenUserIdObj);
                    } else if (tokenUserIdObj instanceof UUID) {
                        tokenUserId = (UUID) tokenUserIdObj;
                    }
                    if (tokenUserId != null && tokenUserId.equals(userId)) {
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
                    } else {
                        result.put("accessTokenBlacklisted", false);
                        result.put("message", "Access token does not belong to this user. User will be logged out when access token expires.");
                    }
                } catch (Exception ex) {
                    result.put("accessTokenBlacklisted", false);
                    result.put("accessTokenError", ex.getMessage());
                }
            } else {
                result.put("accessTokenBlacklisted", false);
                result.put("message", "No access token provided. User will be logged out when access token expires.");
            }
            result.put("success", true);
            result.put("message", "Device logged out successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}

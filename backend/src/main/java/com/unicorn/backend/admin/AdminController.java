package com.unicorn.backend.admin;

import com.unicorn.backend.startup.*;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import com.unicorn.backend.user.UserResponse;
import com.unicorn.backend.user.UserResponseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final StartupService startupService;
    private final com.unicorn.backend.security.RefreshTokenRepository refreshTokenRepository;
    private final StartupRepository startupRepository;
    private final UserResponseService userResponseService;

    public AdminController(UserRepository userRepository, StartupService startupService,
            com.unicorn.backend.security.RefreshTokenRepository refreshTokenRepository,
            StartupRepository startupRepository,
            UserResponseService userResponseService) {
        this.userRepository = userRepository;
        this.startupService = startupService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.startupRepository = startupRepository;
        this.userResponseService = userResponseService;
    }

    /**
     * Get all users with advanced filtering.
     * Supports text search, role/status filters, date ranges, and boolean filters.
     * Each filter supports negation via *Negate parameters.
     * 
     * GET /api/v1/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String query,
            // Advanced Filters
            @org.springframework.web.bind.annotation.RequestParam(required = false) String email,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean emailNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String username,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean usernameNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String firstName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean firstNameNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String lastName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean lastNameNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String displayName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean displayNameNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String country,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean countryNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String role,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean roleNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean statusNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String authProvider,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean authProviderNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime createdAtFrom,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime createdAtTo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean createdAtNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime lastLoginFrom,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime lastLoginTo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean lastLoginNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean hasInvestorProfile,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean hasInvestorProfileNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean hasStartups,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean hasStartupsNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean isSuspended,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean isSuspendedNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer minWarningCount,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean minWarningCountNegate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean hasActiveSession,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean hasActiveSessionNegate,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        try {
            Page<User> usersPage;

            // Check if we have advanced filters (not just the simple query)
            boolean hasAdvancedFilters = email != null || username != null ||
                    firstName != null || lastName != null ||
                    role != null || status != null || authProvider != null ||
                    createdAtFrom != null || createdAtTo != null ||
                    lastLoginFrom != null || lastLoginTo != null ||
                    hasInvestorProfile != null || hasStartups != null || isSuspended != null ||
                    minWarningCount != null || hasActiveSession != null;

            if (hasAdvancedFilters) {
                UserFilterRequest filter = UserFilterRequest.builder()
                        .email(email)
                        .emailNegate(emailNegate)
                        .username(username)
                        .usernameNegate(usernameNegate)
                        .firstName(firstName)
                        .firstNameNegate(firstNameNegate)
                        .lastName(lastName)
                        .lastNameNegate(lastNameNegate)
                        .displayName(displayName)
                        .displayNameNegate(displayNameNegate)
                        .country(country)
                        .countryNegate(countryNegate)
                        .role(role)
                        .roleNegate(roleNegate)
                        .status(status)
                        .statusNegate(statusNegate)
                        .authProvider(authProvider)
                        .authProviderNegate(authProviderNegate)
                        .createdAtFrom(createdAtFrom)
                        .createdAtTo(createdAtTo)
                        .createdAtNegate(createdAtNegate)
                        .lastLoginFrom(lastLoginFrom)
                        .lastLoginTo(lastLoginTo)
                        .lastLoginNegate(lastLoginNegate)
                        .hasInvestorProfile(hasInvestorProfile)
                        .hasInvestorProfileNegate(hasInvestorProfileNegate)
                        .hasStartups(hasStartups)
                        .hasStartupsNegate(hasStartupsNegate)
                        .isSuspended(isSuspended)
                        .isSuspendedNegate(isSuspendedNegate)
                        .minWarningCount(minWarningCount)
                        .minWarningCountNegate(minWarningCountNegate)
                        .hasActiveSession(hasActiveSession)
                        .hasActiveSessionNegate(hasActiveSessionNegate)
                        .build();

                usersPage = userRepository.findAll(UserSpecification.buildSpecification(filter), pageable);
            } else if (query != null && !query.trim().isEmpty()) {
                // Simple text search using existing method
                usersPage = userRepository.searchUsers(query.trim(), pageable);
            } else {
                usersPage = userRepository.findAll(pageable);
            }

            Page<UserResponse> responsePage = usersPage.map(userResponseService::fromEntity);

            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            System.err.println("======= ERROR IN getAllUsers =======");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("====================================");
            throw e;
        }
    }

    /**
     * Get user statistics for admin dashboard.
     * 
     * GET /api/v1/admin/users/stats
     */
    @GetMapping("/users/stats")
    public ResponseEntity<java.util.Map<String, Object>> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // Total Users
        long total = userRepository.count();
        java.time.LocalDateTime startOfMonth = java.time.YearMonth.now().atDay(1).atStartOfDay();
        long newThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        java.util.Map<String, Object> totalStats = new java.util.HashMap<>();
        totalStats.put("value", total);
        totalStats.put("newThisMonth", newThisMonth);
        stats.put("total", totalStats);

        // Active Users (Status = ACTIVE) & Online Users (Active Session)
        long active = userRepository.countByStatus("ACTIVE");
        long onlineNow = refreshTokenRepository.countDistinctUserByExpiryDateAfter(java.time.Instant.now());

        java.util.Map<String, Object> activeStats = new java.util.HashMap<>();
        activeStats.put("value", active);
        activeStats.put("onlineNow", onlineNow);
        stats.put("active", activeStats);

        // Investors
        long investorsCount = userRepository.countByRole("INVESTOR");
        long verifiedInvestors = userRepository.countByRoleAndInvestorProfile_IsVerifiedTrue("INVESTOR");

        java.util.Map<String, Object> investorStats = new java.util.HashMap<>();
        investorStats.put("value", investorsCount);
        investorStats.put("verifiedCount", verifiedInvestors);
        stats.put("investors", investorStats);

        // Startups
        // Using StartupRepository countByStatus is better for actual startups
        // But logic says "Startups" box usually means Startup Users or Startup
        // profiles.
        // Assuming box title "Startups" refers to Startup entities:
        long totalStartups = startupRepository.count();
        java.math.BigDecimal totalRaised = startupRepository.getTotalFundingRaised();

        java.util.Map<String, Object> startupStats = new java.util.HashMap<>();
        startupStats.put("value", totalStartups);
        startupStats.put("totalRaised", totalRaised != null ? totalRaised : java.math.BigDecimal.ZERO);
        stats.put("startups", startupStats);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get all startups by status (for admin review).
     *
     * @param status the status to filter by
     * @return list of startups
     */
    @GetMapping("/startups")
    public ResponseEntity<List<StartupResponse>> getStartupsByStatus(
            @RequestParam(required = false, defaultValue = "PENDING") StartupStatus status) {
        List<StartupResponse> startups = startupService.getStartupsByStatus(status);
        return ResponseEntity.ok(startups);
    }

    /**
     * Delete a startup (Admin only).
     * Performs cascading delete from database.
     *
     * @param id the startup ID
     * @return ResponseEntity with 204 No Content
     */
    @DeleteMapping("/startups/{id}")
    public ResponseEntity<Void> deleteStartup(@PathVariable UUID id) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Startup not found: " + id));

        startupRepository.delete(startup);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfer startup ownership to another user.
     *
     * @param id      the startup ID
     * @param request contains the new owner's user ID
     * @return the updated startup
     */
    @PutMapping("/startups/{id}/transfer-ownership")
    public ResponseEntity<StartupResponse> transferOwnership(
            @PathVariable UUID id,
            @RequestBody TransferOwnershipRequest request) {

        // Validate new owner exists
        User newOwner = userRepository.findById(request.getNewOwnerId())
                .orElseThrow(() -> new RuntimeException("New owner not found: " + request.getNewOwnerId()));

        StartupResponse updated = startupService.transferOwnership(id, newOwner);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get all startups (paginated) for admin management.
     */
    @GetMapping("/startups/all")
    public ResponseEntity<Page<StartupResponse>> getAllStartups(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) UUID ownerId,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StartupResponse> startups = startupService.getAllStartups(pageable);
        return ResponseEntity.ok(startups);
    }
}

package com.unicorn.backend.admin;

import com.unicorn.backend.startup.*;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import com.unicorn.backend.user.UserResponse;
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

    public AdminController(UserRepository userRepository, StartupService startupService) {
        this.userRepository = userRepository;
        this.startupService = startupService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String query,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<User> usersPage;
        if (query != null && !query.trim().isEmpty()) {
            usersPage = userRepository.searchUsers(query.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        Page<UserResponse> responsePage = usersPage.map(UserResponse::fromEntity);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get user statistics for admin dashboard.
     * 
     * GET /api/v1/admin/users/stats
     */
    @GetMapping("/users/stats")
    public ResponseEntity<java.util.Map<String, Object>> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        long total = userRepository.count();
        long active = userRepository.countByStatus("ACTIVE");
        // Note: These would need proper counting - for now using simplified counts
        long investors = userRepository.countByRole("INVESTOR");
        long startups = userRepository.countByRole("STARTUP_OWNER");

        stats.put("total", total);
        stats.put("investors", investors);
        stats.put("startups", startups);
        stats.put("active", active);

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
     * Update startup status (approve or reject).
     *
     * @param id      the startup ID
     * @param request the status update request
     * @return the updated startup
     */
    @PutMapping("/startups/{id}/status")
    public ResponseEntity<StartupResponse> updateStartupStatus(
            @PathVariable UUID id,
            @RequestBody UpdateStartupStatusRequest request) {
        StartupResponse updated = startupService.updateStartupStatus(id, request.getStatus());
        // Note: rejectionReason is in the request but not currently stored in the
        // entity
        // Future enhancement: add rejectionReason field to Startup entity
        return ResponseEntity.ok(updated);
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

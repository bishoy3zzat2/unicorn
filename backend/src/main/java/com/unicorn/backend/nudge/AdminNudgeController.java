package com.unicorn.backend.nudge;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin controller for viewing nudge statistics in dashboard.
 */
@RestController
@RequestMapping("/api/v1/admin/nudges")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminNudgeController {

    private final NudgeService nudgeService;

    /**
     * Get nudge statistics for a specific user.
     * For INVESTOR: returns received nudges
     * For STARTUP_OWNER: returns sent nudges with remaining count
     * 
     * GET /api/v1/admin/nudges/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserNudgeStatsResponse> getUserNudgeStats(@PathVariable UUID userId) {
        UserNudgeStatsResponse stats = nudgeService.getUserNudgeStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all nudges sent for a specific startup.
     * Returns list of nudge info with sender/receiver details.
     * 
     * GET /api/v1/admin/nudges/startup/{startupId}
     */
    @GetMapping("/startup/{startupId}")
    public ResponseEntity<?> getStartupNudges(@PathVariable UUID startupId) {
        var nudges = nudgeService.getStartupNudges(startupId);
        long count = nudgeService.countStartupNudges(startupId);
        return ResponseEntity.ok(java.util.Map.of(
                "count", count,
                "nudges", nudges));
    }
}

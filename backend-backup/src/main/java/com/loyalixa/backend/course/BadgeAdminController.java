package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.BadgeAdminResponse;
import com.loyalixa.backend.course.dto.BadgeRequest;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/badges")
public class BadgeAdminController {
    private final BadgeService badgeService;
    private final BadgeRepository badgeRepository;
    private final CourseBadgeRepository courseBadgeRepository;
    public BadgeAdminController(BadgeService badgeService, BadgeRepository badgeRepository, CourseBadgeRepository courseBadgeRepository) {
        this.badgeService = badgeService;
        this.badgeRepository = badgeRepository;
        this.courseBadgeRepository = courseBadgeRepository;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('badge:view_all')")
    public ResponseEntity<List<BadgeAdminResponse>> getAllBadges(@AuthenticationPrincipal User currentUser) { 
        return ResponseEntity.ok(badgeService.getAllBadgesResponse());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('badge:view_all')")
    public ResponseEntity<BadgeAdminResponse> getBadge(@PathVariable UUID id) {
        try {
            Badge badge = badgeRepository.findByIdWithCreatedAndUpdatedBy(id)
                    .orElseThrow(() -> new IllegalArgumentException("Badge not found."));
            BadgeAdminResponse response = badgeService.toAdminResponse(badge);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('badge:view_all')")
    public ResponseEntity<com.loyalixa.backend.course.dto.BadgeDetailsResponse> getBadgeDetails(@PathVariable UUID id) {
        try {
            com.loyalixa.backend.course.dto.BadgeDetailsResponse details = badgeService.getBadgeDetails(id);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('badge:create') or hasRole('ADMIN')")
    public ResponseEntity<?> createBadge(@Valid @RequestBody BadgeRequest request, @AuthenticationPrincipal User adminUser) {
        try {
            Badge newBadge = badgeService.createBadge(request, adminUser);
            Badge loadedBadge = badgeRepository.findByIdWithCreatedAndUpdatedBy(newBadge.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load created badge."));
            BadgeAdminResponse response = badgeService.toAdminResponse(loadedBadge);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "A badge with the same name and properties already exists."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to create badge: " + e.getMessage()));
        }
    }
    @PutMapping("/{badgeId}")
    @PreAuthorize("hasAuthority('badge:update')")
    public ResponseEntity<BadgeAdminResponse> updateBadge(
            @PathVariable UUID badgeId,
            @Valid @RequestBody BadgeRequest request,
            @AuthenticationPrincipal User adminUser
    ) {
        try {
            Badge updatedBadge = badgeService.updateBadge(badgeId, request, adminUser);
            Badge loadedBadge = badgeRepository.findByIdWithCreatedAndUpdatedBy(updatedBadge.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load updated badge."));
            BadgeAdminResponse response = badgeService.toAdminResponse(loadedBadge);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }
    }
    @DeleteMapping("/{badgeId}")
    @PreAuthorize("hasAuthority('badge:delete')")
    public ResponseEntity<?> deleteBadge(@PathVariable UUID badgeId) {
        try {
            badgeService.deleteBadge(badgeId);
            return ResponseEntity.noContent().build(); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of("error", e.getMessage())
            ); 
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                java.util.Map.of("error", e.getMessage())
            );
        }
    }
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasAuthority('badge:delete') or hasRole('ADMIN')")
    public ResponseEntity<?> cleanupExpiredBadges() {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            int unassignedCount = courseBadgeRepository.deleteExpiredBadges(now);
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Cleanup completed successfully",
                "unassignedCount", unassignedCount,
                "note", "Expired badges are automatically filtered when fetching courses. This endpoint is optional for manual cleanup."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to cleanup expired badges: " + e.getMessage()));
        }
    }
}
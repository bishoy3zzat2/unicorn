package com.unicorn.backend.startup;

import com.unicorn.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for startup management endpoints.
 */
@RestController
@RequestMapping("/api/v1/startups")
@RequiredArgsConstructor
public class StartupController {

    private final StartupService startupService;
    private final com.unicorn.backend.user.UserRepository userRepository;

    /**
     * Create a new startup.
     *
     * @param request the startup creation request
     * @param user    the authenticated user
     * @return the created startup response
     */
    @PostMapping
    public ResponseEntity<StartupResponse> createStartup(
            @Valid @RequestBody CreateStartupRequest request,
            @AuthenticationPrincipal User user) {
        StartupResponse response = startupService.createStartup(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all startups owned by the authenticated user.
     *
     * @param user the authenticated user
     * @return list of startup responses
     */
    @GetMapping("/my-startups")
    public ResponseEntity<List<StartupResponse>> getMyStartups(@AuthenticationPrincipal User user) {
        List<StartupResponse> startups = startupService.getMyStartups(user);
        return ResponseEntity.ok(startups);
    }

    /**
     * Get a startup by ID.
     *
     * @param id the startup ID
     * @return the startup response
     */
    @GetMapping("/{id}")
    public ResponseEntity<StartupResponse> getStartupById(@PathVariable UUID id) {
        StartupResponse response = startupService.getStartupById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing startup.
     *
     * @param id      the startup ID
     * @param request the update request
     * @param user    the authenticated user
     * @return the updated startup response
     */
    @PutMapping("/{id}")
    public ResponseEntity<StartupResponse> updateStartup(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStartupRequest request,
            @AuthenticationPrincipal User user) {
        StartupResponse response = startupService.updateStartup(id, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a startup.
     *
     * @param id   the startup ID
     * @param user the authenticated user
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStartup(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        startupService.deleteStartup(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all startups by status (for admin use).
     *
     * @param status the status filter
     * @return list of startup responses
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<StartupResponse>> getStartupsByStatus(@PathVariable StartupStatus status) {
        List<StartupResponse> startups = startupService.getStartupsByStatus(status);
        return ResponseEntity.ok(startups);
    }

    /**
     * Transfer startup ownership.
     * Accessible by ADMIN or current Startup Owner.
     */
    @PutMapping("/{id}/transfer-ownership")
    public ResponseEntity<StartupResponse> transferOwnership(
            @PathVariable UUID id,
            @RequestBody com.unicorn.backend.admin.TransferOwnershipRequest request,
            @AuthenticationPrincipal User currentUser) {

        StartupResponse startup = startupService.getStartupById(id);

        // Security Check: Is Admin OR Current Owner?
        boolean isAdmin = currentUser.getRole().contains("ADMIN");
        boolean isOwner = startup.getOwnerId().equals(currentUser.getId().toString());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User newOwner = userRepository.findById(request.getNewOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("New owner not found"));

        StartupResponse updated = startupService.transferOwnership(id, newOwner, request.getNewOwnerRole());
        return ResponseEntity.ok(updated);
    }

    /**
     * Add a member to the startup.
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<StartupResponse> addMember(
            @PathVariable UUID id,
            @RequestBody @Valid AddMemberRequest request,
            @AuthenticationPrincipal User user) {

        StartupResponse response = startupService.addMember(
                id,
                request.userId(),
                request.role(),
                request.joinedAt(),
                request.leftAt(),
                user);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a member's role in the startup.
     */
    @PatchMapping("/{id}/members/{userId}/role")
    public ResponseEntity<StartupResponse> updateMemberRole(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user) {

        String newRole = body.get("role");
        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        StartupResponse response = startupService.updateMemberRole(id, userId, newRole, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Leave a startup (Soft Delete).
     */
    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveStartup(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        startupService.leaveStartup(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivate an inactive member.
     */
    @PostMapping("/{id}/members/{userId}/reactivate")
    public ResponseEntity<StartupResponse> reactivateMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user) {
        StartupResponse response = startupService.reactivateMember(id, userId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Unsign from a startup (Hard Delete).
     */
    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<Void> unsignStartup(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        startupService.unsignStartup(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove a member (Soft Delete) - Admin/Owner.
     */
    @PostMapping("/{id}/members/{userId}/remove")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user) {
        startupService.removeMember(id, userId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a member (Hard Delete) - Admin/Owner.
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user) {
        startupService.deleteMember(id, userId, user);
        return ResponseEntity.noContent().build();
    }
}

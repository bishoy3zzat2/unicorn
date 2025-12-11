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
}

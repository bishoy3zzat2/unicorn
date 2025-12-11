package com.loyalixa.backend.user;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/users/follow")
public class FollowController {
    private final FollowService followService;
    public FollowController(FollowService followService) {
        this.followService = followService;
    }
    @PostMapping("/{instructorId}")
    @PreAuthorize("isAuthenticated() and hasRole('STUDENT')")
    public ResponseEntity<String> followInstructor(
            @PathVariable UUID instructorId,
            @AuthenticationPrincipal User follower  
    ) {
        try {
            followService.followInstructor(follower.getId(), instructorId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully followed instructor.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @DeleteMapping("/{instructorId}")
    @PreAuthorize("isAuthenticated() and hasRole('STUDENT')")
    public ResponseEntity<String> unfollowInstructor(
            @PathVariable UUID instructorId,
            @AuthenticationPrincipal User follower
    ) {
        try {
            boolean success = followService.unfollowInstructor(follower.getId(), instructorId);
            if (success) {
                return ResponseEntity.ok("Successfully unfollowed instructor.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Follow relationship not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
    @GetMapping("/{instructorId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> getFollowStatus(
            @PathVariable UUID instructorId,
            @AuthenticationPrincipal User follower
    ) {
        boolean isFollowing = followService.isFollowing(follower.getId(), instructorId);
        return ResponseEntity.ok(isFollowing);
    }
}
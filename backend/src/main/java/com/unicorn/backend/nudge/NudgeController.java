package com.unicorn.backend.nudge;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for nudge operations (user-facing).
 */
@RestController
@RequestMapping("/api/v1/nudges")
@RequiredArgsConstructor
public class NudgeController {

    private final NudgeService nudgeService;
    private final UserRepository userRepository;

    /**
     * Check if the authenticated user can nudge a specific investor.
     * GET /api/nudges/availability/{investorId}
     */
    @GetMapping("/availability/{investorId}")
    public ResponseEntity<NudgeAvailabilityResponse> checkAvailability(
            @PathVariable UUID investorId,
            @AuthenticationPrincipal User sender) {

        User receiver = userRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        NudgeAvailabilityResponse response = nudgeService.canNudge(sender, receiver);
        return ResponseEntity.ok(response);
    }

    /**
     * Send a nudge to an investor for a specific startup.
     * POST /api/v1/nudges/send/{investorId}
     */
    @PostMapping("/send/{investorId}")
    public ResponseEntity<NudgeResponse> sendNudge(
            @PathVariable UUID investorId,
            @RequestParam UUID startupId,
            @AuthenticationPrincipal User sender) {

        try {
            Nudge nudge = nudgeService.sendNudge(sender, investorId, startupId);
            return ResponseEntity.ok(NudgeResponse.success(nudge.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(NudgeResponse.error(e.getMessage()));
        }
    }

    /**
     * Response DTO for send nudge operation.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NudgeResponse {
        private boolean success;
        private String message;
        private Long nudgeId;

        public static NudgeResponse success(Long nudgeId) {
            return NudgeResponse.builder()
                    .success(true)
                    .message("Nudge sent successfully")
                    .nudgeId(nudgeId)
                    .build();
        }

        public static NudgeResponse error(String message) {
            return NudgeResponse.builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}

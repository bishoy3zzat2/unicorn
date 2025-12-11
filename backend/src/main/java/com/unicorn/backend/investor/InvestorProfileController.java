package com.unicorn.backend.investor;

import com.unicorn.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for investor profile management endpoints.
 */
@RestController
@RequestMapping("/api/v1/investors")
@RequiredArgsConstructor
public class InvestorProfileController {

    private final InvestorProfileService investorProfileService;

    /**
     * Create or update the investor profile for the authenticated user.
     *
     * @param request the profile request
     * @param user    the authenticated user
     * @return the profile response
     */
    @PostMapping("/profile")
    public ResponseEntity<InvestorProfileResponse> createOrUpdateProfile(
            @Valid @RequestBody CreateInvestorProfileRequest request,
            @AuthenticationPrincipal User user) {
        InvestorProfileResponse response = investorProfileService.createOrUpdateProfile(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get the investor profile for the authenticated user.
     *
     * @param user the authenticated user
     * @return the profile response, or 404 if not found
     */
    @GetMapping("/profile")
    public ResponseEntity<InvestorProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        InvestorProfileResponse response = investorProfileService.getMyProfile(user);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}

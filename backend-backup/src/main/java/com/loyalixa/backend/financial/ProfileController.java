package com.loyalixa.backend.financial;
import com.loyalixa.backend.financial.dto.ProfileResponse;
import com.loyalixa.backend.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final ProfileService profileService;
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        ProfileResponse profile = profileService.getMyProfile(currentUser);
        return ResponseEntity.ok(profile);
    }
}

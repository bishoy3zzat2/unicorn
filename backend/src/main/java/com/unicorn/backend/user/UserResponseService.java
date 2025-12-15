package com.unicorn.backend.user;

import com.unicorn.backend.security.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for creating UserResponse with session information.
 */
@Service
public class UserResponseService {

    private final RefreshTokenRepository refreshTokenRepository;

    public UserResponseService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Create UserResponse from User entity with active session check.
     */
    public UserResponse fromEntity(User user) {
        boolean hasActiveSession = refreshTokenRepository.findByUserId(user.getId())
                .stream()
                .anyMatch(token -> token.getExpiryDate().isAfter(java.time.Instant.now()));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getAuthProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getSuspendedAt(),
                user.getSuspendedUntil(),
                user.getSuspensionType(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                user.getCountry(),
                user.getAvatarUrl(),
                user.getSuspendReason(),
                user.getInvestorProfile() != null,
                user.getStartups() != null && !user.getStartups().isEmpty(),
                hasActiveSession);
    }
}

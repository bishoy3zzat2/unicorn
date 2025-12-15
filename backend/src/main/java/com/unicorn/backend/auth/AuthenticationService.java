package com.unicorn.backend.auth;

import com.unicorn.backend.jwt.JwtService;
import com.unicorn.backend.jwt.TokenBlacklistService;
import com.unicorn.backend.security.RefreshToken;
import com.unicorn.backend.security.RefreshTokenService;
import com.unicorn.backend.user.AvatarService;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {
        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
        private final AvatarService avatarService;

        public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
                        JwtService jwtService, RefreshTokenService refreshTokenService,
                        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                        AvatarService avatarService) {
                this.authenticationManager = authenticationManager;
                this.userRepository = userRepository;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
                this.passwordEncoder = passwordEncoder;
                this.avatarService = avatarService;
        }

        public LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
                if (userRepository.existsByEmail(request.email())) {
                        throw new IllegalArgumentException("Email already in use");
                }

                User user = new User();
                user.setEmail(request.email());
                user.setPasswordHash(passwordEncoder.encode(request.password()));
                user.setRole(request.role() != null ? request.role().toUpperCase() : "USER");
                user.setStatus("ACTIVE");
                user.setAuthProvider("LOCAL");

                // Handle Username Logic
                String finalUsername;
                if (request.username() != null && !request.username().trim().isEmpty()) {
                        // User provided a username
                        String sanitized = request.username().trim().toLowerCase();

                        // Validation Regex
                        // 1. Starts with a letter [a-z]
                        // 2. Contains only [a-z0-9-_]
                        // 3. No consecutive special chars [-_]
                        String usernameRegex = "^[a-z](?!.*[-_]{2})[a-z0-9-_]*$";

                        if (!sanitized.matches(usernameRegex)) {
                                throw new IllegalArgumentException(
                                                "Username must start with a letter, contain only lowercase letters, numbers, dashes, or underscores, and cannot have consecutive special characters.");
                        }

                        if (userRepository.existsByUsername(sanitized)) {
                                throw new IllegalArgumentException("Username already exists");
                        }
                        finalUsername = sanitized;
                } else {
                        // Generate username from email prefix
                        String emailPrefix = request.email().split("@")[0].toLowerCase();

                        // Sanitize for generation: replace dots with underscore, remove invalid chars
                        String base = emailPrefix.replace(".", "_").replaceAll("[^a-z0-9-_]", "");

                        // Ensure starts with letter
                        if (base.isEmpty() || !base.matches("^[a-z].*")) {
                                base = "u" + base;
                        }

                        // Fix consecutive special chars
                        base = base.replaceAll("[-_]{2,}", "_");

                        finalUsername = base;

                        // If generated username exists, append numbers until unique
                        if (userRepository.existsByUsername(finalUsername)) {
                                int attempts = 0;
                                while (userRepository.existsByUsername(finalUsername) && attempts < 10) {
                                        String randomSuffix = String.valueOf((int) (Math.random() * 1000));
                                        finalUsername = base + randomSuffix;
                                        attempts++;
                                }
                                // Fallback purely random if still colliding
                                if (userRepository.existsByUsername(finalUsername)) {
                                        finalUsername = base + System.currentTimeMillis();
                                }
                        }
                }
                user.setUsername(finalUsername);

                User savedUser = userRepository.save(user);

                // Set default avatar
                savedUser.setAvatarUrl(avatarService.getRandomAvatar(savedUser.getId()));
                savedUser = userRepository.save(savedUser);

                // Auto-login after register
                String jwtToken = jwtService.generateAccessToken(user);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                                user,
                                httpRequest.getHeader("User-Agent"),
                                httpRequest.getRemoteAddr());

                return new LoginResponse(
                                jwtToken,
                                refreshToken.getToken(),
                                user.getUsername(),
                                user.getId(),
                                null,
                                user.getCanAccessDashboard());
        }

        public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

                User user = userRepository.findByEmail(request.email())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                user.setLastLoginAt(LocalDateTime.now());

                String jwtToken = jwtService.generateAccessToken(user);

                RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                                user,
                                httpRequest.getHeader("User-Agent"),
                                httpRequest.getRemoteAddr());

                userRepository.save(user);

                return new LoginResponse(
                                jwtToken,
                                refreshToken.getToken(),
                                user.getUsername(),
                                user.getId(),
                                null,
                                user.getCanAccessDashboard());
        }

        public LoginResponse refreshToken(RefreshTokenRequest request) {
                return refreshTokenService.findByToken(request.token())
                                .map(refreshTokenService::verifyExpiration)
                                .map(RefreshToken::getUser)
                                .map(user -> {
                                        String jwtToken = jwtService.generateAccessToken(user);
                                        return new LoginResponse(
                                                        jwtToken,
                                                        request.token(),
                                                        user.getUsername(),
                                                        user.getId(),
                                                        null,
                                                        user.getCanAccessDashboard());
                                })
                                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));
        }
}
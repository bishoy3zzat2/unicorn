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
        private final com.unicorn.backend.investor.InvestorProfileRepository investorProfileRepository;

        private final com.unicorn.backend.service.EmailService emailService;
        private final com.unicorn.backend.appconfig.AppConfigService appConfigService;
        private final UserOneTimePasswordRepository userOneTimePasswordRepository;

        public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
                        JwtService jwtService, RefreshTokenService refreshTokenService,
                        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                        AvatarService avatarService,
                        com.unicorn.backend.investor.InvestorProfileRepository investorProfileRepository,
                        com.unicorn.backend.service.EmailService emailService,
                        com.unicorn.backend.appconfig.AppConfigService appConfigService,
                        UserOneTimePasswordRepository userOneTimePasswordRepository) {
                this.authenticationManager = authenticationManager;
                this.userRepository = userRepository;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
                this.passwordEncoder = passwordEncoder;
                this.avatarService = avatarService;
                this.investorProfileRepository = investorProfileRepository;
                this.emailService = emailService;
                this.appConfigService = appConfigService;
                this.userOneTimePasswordRepository = userOneTimePasswordRepository;
        }

        public LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
                User user;
                if (userRepository.existsByEmail(request.email())) {
                        User existingUser = userRepository.findByEmail(request.email()).get();

                        // Allow if PENDING_VERIFICATION (Existing Logic) OR DELETED (New Logic)
                        boolean isPending = "PENDING_VERIFICATION".equals(existingUser.getStatus());
                        boolean isDeleted = "DELETED".equals(existingUser.getStatus());

                        if (!isPending && !isDeleted) {
                                throw new IllegalArgumentException("Email already in use");
                        }

                        // RECLAIM LOGIC: Overwrite existing user
                        user = existingUser;

                        // If DELETED, reset critical fields for fresh start
                        if (isDeleted) {
                                user.setDeletedAt(null);
                                user.setDeletionReason(null);
                        }

                        // Clean up previous OTP if exists
                        userOneTimePasswordRepository.findByUser(user).ifPresent(userOneTimePasswordRepository::delete);
                } else {
                        user = new User();
                        user.setEmail(request.email());
                }

                user.setPasswordHash(passwordEncoder.encode(request.password()));
                user.setRole(request.role() != null ? request.role().toUpperCase() : "STARTUP_OWNER");
                user.setStatus("PENDING_VERIFICATION");
                user.setAuthProvider("LOCAL");
                user.setFirstName(request.firstName());
                user.setLastName(request.lastName());
                user.setPhoneNumber(request.phoneNumber());
                user.setCountry(request.country());
                user.setPhoneNumber(request.phoneNumber());
                user.setCountry(request.country());

                // Name Validation
                if (request.firstName() != null) {
                        int maxFirstNameLength = appConfigService.getIntValue("max_user_first_name_length", 50);
                        if (request.firstName().length() > maxFirstNameLength) {
                                throw new IllegalArgumentException(
                                                "First name must not exceed " + maxFirstNameLength + " characters");
                        }
                }

                if (request.lastName() != null) {
                        int maxLastNameLength = appConfigService.getIntValue("max_user_last_name_length", 50);
                        if (request.lastName().length() > maxLastNameLength) {
                                throw new IllegalArgumentException(
                                                "Last name must not exceed " + maxLastNameLength + " characters");
                        }
                }

                // Bio Validation
                if (request.bio() != null) {
                        int maxBioLength = appConfigService.getIntValue("max_bio_length", 250);
                        if (request.bio().length() > maxBioLength) {
                                throw new IllegalArgumentException(
                                                "Bio must not exceed " + maxBioLength + " characters");
                        }
                }
                user.setBio(request.bio());

                // Sanitize and prefix LinkedIn URL
                String linkedInInput = request.linkedInUrl();
                if (linkedInInput != null && !linkedInInput.trim().isEmpty()) {
                        String cleanInput = linkedInInput.trim();
                        if (!cleanInput.toLowerCase().startsWith("http")) {
                                // Assume it's a username or partial path
                                if (!cleanInput.toLowerCase().contains("linkedin.com")) {
                                        cleanInput = "https://www.linkedin.com/in/" + cleanInput.replace("/", "");
                                } else {
                                        // It has linkedin.com but no http
                                        cleanInput = "https://" + cleanInput;
                                }
                        }
                        user.setLinkedInUrl(cleanInput);
                } else {
                        user.setLinkedInUrl(null);
                }

                // Handle Username Logic
                String finalUsername;
                if (request.username() != null && !request.username().trim().isEmpty()) {
                        // User provided a username
                        String sanitized = request.username().trim().toLowerCase();

                        // Validation Regex
                        String usernameRegex = "^[a-z](?!.*[-_]{2})[a-z0-9-_]*$";

                        if (!sanitized.matches(usernameRegex)) {
                                throw new IllegalArgumentException(
                                                "Username must start with a letter, contain only lowercase letters, numbers, dashes, or underscores, and cannot have consecutive special characters.");
                        }

                        // Check if username exists and belongs to ANOTHER user
                        if (userRepository.existsByUsername(sanitized)) {
                                User owner = userRepository.findByUsername(sanitized).orElse(null);
                                if (owner != null && !owner.getId().equals(user.getId())) {
                                        throw new IllegalArgumentException("Username already exists");
                                }
                        }
                        finalUsername = sanitized;
                } else {
                        // Generate username from email prefix
                        String emailPrefix = request.email().split("@")[0].toLowerCase();
                        String base = emailPrefix.replace(".", "_").replaceAll("[^a-z0-9-_]", "");
                        if (base.isEmpty() || !base.matches("^[a-z].*")) {
                                base = "u" + base;
                        }
                        base = base.replaceAll("[-_]{2,}", "_");
                        finalUsername = base;

                        if (userRepository.existsByUsername(finalUsername)) {
                                User owner = userRepository.findByUsername(finalUsername).orElse(null);
                                if (owner != null && !owner.getId().equals(user.getId())) {
                                        int attempts = 0;
                                        while (userRepository.existsByUsername(finalUsername) && attempts < 10) {
                                                String randomSuffix = String.valueOf((int) (Math.random() * 1000));
                                                finalUsername = base + randomSuffix;
                                                attempts++;
                                        }
                                        if (userRepository.existsByUsername(finalUsername)) {
                                                finalUsername = base + System.currentTimeMillis();
                                        }
                                }
                        }
                }
                user.setUsername(finalUsername);

                System.out.println("DEBUG: Saving user " + user.getEmail() + " with status: " + user.getStatus());
                User savedUser = userRepository.saveAndFlush(user);
                System.out.println("DEBUG: Saved user status: " + savedUser.getStatus());

                // Set default avatar only if not set (or overwrite for reclaim)
                savedUser.setAvatarUrl(avatarService.getRandomAvatar(savedUser.getId()));
                savedUser = userRepository.saveAndFlush(savedUser);

                // Create Investor Profile if role is INVESTOR
                if ("INVESTOR".equals(savedUser.getRole())) {
                        com.unicorn.backend.investor.InvestorProfile profile = investorProfileRepository
                                        .findByUser(savedUser)
                                        .orElse(new com.unicorn.backend.investor.InvestorProfile());
                        profile.setUser(savedUser);
                        profile.setInvestmentBudget(request.investmentBudget());
                        profile.setPreferredIndustries(request.preferredIndustries());
                        if (request.preferredStage() != null && !request.preferredStage().isEmpty()) {
                                try {
                                        profile.setPreferredStage(com.unicorn.backend.startup.Stage
                                                        .valueOf(request.preferredStage()));
                                } catch (IllegalArgumentException e) {
                                        // Ignore invalid stage or handle error
                                }
                        }
                        profile.setVerificationRequested(true);
                        profile.setVerificationRequestedAt(LocalDateTime.now());
                        investorProfileRepository.save(profile);
                }

                // Generate OTP
                String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // 6 digit OTP
                UserOneTimePassword otpEntity = new UserOneTimePassword(savedUser, otp, 15); // 15 mins expiry
                userOneTimePasswordRepository.save(otpEntity);

                // Send Email
                emailService.sendOtp(savedUser.getEmail(), otp);

                // Return Empty Response or Partial (No Tokens)
                return new LoginResponse(null, null, savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole(),
                                savedUser.getId(),
                                savedUser.getPreferredCurrency(),
                                null, false);
        }

        public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
                // 1. Resolve User first (by Email or Username)
                String input = request.email();
                User user;

                if (input.contains("@")) {
                        user = userRepository.findByEmail(input)
                                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                } else {
                        user = userRepository.findByUsername(input)
                                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                }

                try {
                        // 2. Authenticate using the resolved Email (as UserDetailsService likely
                        // expects email)
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(user.getEmail(), request.password()));
                } catch (org.springframework.security.authentication.LockedException
                                | org.springframework.security.authentication.DisabledException e) {
                        // Check if user is pending verification first
                        if ("PENDING_VERIFICATION".equals(user.getStatus())) {
                                throw new com.unicorn.backend.exception.UserNotVerifiedException(user.getEmail());
                        }

                        // Check if user is actually suspended/banned/disabled and return rich response
                        if ("SUSPENDED".equals(user.getStatus()) || "BANNED".equals(user.getStatus())) {
                                LoginResponse.SuspensionBanInfo info = new LoginResponse.SuspensionBanInfo(
                                                user.getStatus(),
                                                user.getSuspendReason(),
                                                user.getSuspendedAt(),
                                                user.getSuspendedUntil(),
                                                user.getSuspensionType(),
                                                user.getSuspendedUntil() != null); // isTemporary

                                LoginResponse response = new LoginResponse(
                                                null,
                                                null,
                                                user.getUsername(),
                                                user.getEmail(),
                                                user.getRole(),
                                                user.getId(),
                                                user.getPreferredCurrency(),
                                                info,
                                                user.getCanAccessDashboard());
                                throw new com.unicorn.backend.exception.UserSuspendedException(response);
                        }
                        throw e; // Rethrow if it's some other lock reason or we want default behavior for others
                }

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
                                user.getEmail(),
                                user.getRole(),
                                user.getId(),
                                user.getPreferredCurrency(),
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
                                                        user.getEmail(),
                                                        user.getRole(),
                                                        user.getId(),
                                                        user.getPreferredCurrency(),
                                                        null,
                                                        user.getCanAccessDashboard());
                                })
                                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));
        }

        public LoginResponse verify(String email, String otp, HttpServletRequest httpRequest) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                UserOneTimePassword otpEntity = userOneTimePasswordRepository.findByUser(user)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

                if (otpEntity.isExpired()) {
                        throw new IllegalArgumentException("OTP has expired");
                }

                if (!otpEntity.getOtpCode().equals(otp)) {
                        throw new IllegalArgumentException("Invalid OTP");
                }

                // Verify Success
                user.setStatus("ACTIVE");
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);

                // Cleanup OTP
                userOneTimePasswordRepository.delete(otpEntity);

                // Generate Tokens
                String jwtToken = jwtService.generateAccessToken(user);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                                user,
                                httpRequest.getHeader("User-Agent"),
                                httpRequest.getRemoteAddr());

                return new LoginResponse(
                                jwtToken,
                                refreshToken.getToken(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRole(),
                                user.getId(),
                                user.getPreferredCurrency(),
                                null,
                                user.getCanAccessDashboard());
        }
}
package com.unicorn.backend.auth;

import com.unicorn.backend.jwt.JwtService;
import com.unicorn.backend.jwt.TokenBlacklistService;
import com.unicorn.backend.security.RefreshToken;
import com.unicorn.backend.security.RefreshTokenService;
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

        public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository,
                        JwtService jwtService, RefreshTokenService refreshTokenService,
                        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
                this.authenticationManager = authenticationManager;
                this.userRepository = userRepository;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
                this.passwordEncoder = passwordEncoder;
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

                userRepository.save(user);

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
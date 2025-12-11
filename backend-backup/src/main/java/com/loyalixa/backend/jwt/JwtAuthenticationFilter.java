package com.loyalixa.backend.jwt;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.security.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;  
import org.springframework.security.core.AuthenticationException;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Component  
public class JwtAuthenticationFilter extends OncePerRequestFilter {  
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;  
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;  
    private final RefreshTokenService refreshTokenService;  
    private final ObjectMapper objectMapper;  
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService, UserRepository userRepository, RefreshTokenService refreshTokenService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;  
        this.userRepository = userRepository;  
        this.refreshTokenService = refreshTokenService;  
        this.objectMapper = objectMapper;  
    }
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if (requestPath != null && requestPath.equals("/api/v1/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);  
        try{
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token is blacklisted\"}");
                return;  
           }
            final String userEmail = jwtService.extractEmail(jwt);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (!userDetails.isAccountNonLocked() || !userDetails.isEnabled()) {
                    Optional<User> userOpt = userRepository.findByEmail(userEmail);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        String status = user.getStatus();
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("error", "Account is " + status.toLowerCase());
                        errorResponse.put("status", status);
                        errorResponse.put("code", "ACCOUNT_" + status);
                        if ("SUSPENDED".equals(status)) {
                            Map<String, Object> suspensionInfo = new HashMap<>();
                            suspensionInfo.put("action", "SUSPENDED");
                            suspensionInfo.put("reason", user.getSuspendReason());
                            suspensionInfo.put("actionAt", user.getSuspendedAt());
                            suspensionInfo.put("until", user.getSuspendedUntil());
                            suspensionInfo.put("type", user.getSuspensionType());  
                            suspensionInfo.put("isTemporary", "TEMPORARY".equals(user.getSuspensionType()));
                            errorResponse.put("suspensionBanInfo", suspensionInfo);
                        } else if ("BANNED".equals(status)) {
                            Map<String, Object> banInfo = new HashMap<>();
                            banInfo.put("action", "BANNED");
                            banInfo.put("reason", user.getBanReason());
                            banInfo.put("actionAt", user.getBannedAt());
                            banInfo.put("until", user.getBannedUntil());
                            banInfo.put("type", user.getBanType());  
                            banInfo.put("isTemporary", "TEMPORARY".equals(user.getBanType()));
                            errorResponse.put("suspensionBanInfo", banInfo);
                        } else if ("DELETED".equals(status)) {
                            Map<String, Object> deletionInfo = new HashMap<>();
                            deletionInfo.put("action", "DELETED");
                            deletionInfo.put("reason", user.getDeletionReason());
                            deletionInfo.put("deletedAt", user.getDeletedAt());
                            errorResponse.put("suspensionBanInfo", deletionInfo);
                        } else if ("BLOCKED".equals(status)) {
                            Map<String, Object> blockInfo = new HashMap<>();
                            blockInfo.put("action", "BLOCKED");
                            errorResponse.put("suspensionBanInfo", blockInfo);
                        }
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  
                        response.setContentType("application/json");
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Account is suspended, banned, or disabled\", \"status\": \"FORBIDDEN\"}");
                    }
                    return;  
                }
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    try {
                        io.jsonwebtoken.Claims claims = jwtService.extractAllClaims(jwt);
                        Object deviceIdObj = claims.get("deviceId");
                        Object userIdObj = claims.get("userId");
                        if (deviceIdObj != null && userIdObj != null) {
                            String deviceId = deviceIdObj.toString();
                            java.util.UUID userId;
                            if (userIdObj instanceof String) {
                                userId = java.util.UUID.fromString((String) userIdObj);
                            } else if (userIdObj instanceof java.util.UUID) {
                                userId = (java.util.UUID) userIdObj;
                            } else {
                                userId = null;
                            }
                            if (userId != null && deviceId != null && !deviceId.isEmpty()) {
                                java.util.Optional<com.loyalixa.backend.security.RefreshToken> refreshTokenOpt = 
                                    refreshTokenService.findByUserIdAndDeviceId(userId, deviceId);
                                if (refreshTokenOpt.isEmpty()) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\": \"Device session expired. Please log in again.\", \"code\": \"DEVICE_SESSION_EXPIRED\"}");
                                    return;
                                }
                                com.loyalixa.backend.security.RefreshToken refreshToken = refreshTokenOpt.get();
                                if (refreshToken.getExpiryDate().isBefore(java.time.Instant.now())) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\": \"Session expired. Please log in again.\", \"code\": \"SESSION_EXPIRED\"}");
                                    return;
                                }
                            }
                        } else {
                            if (userIdObj != null) {
                                java.util.UUID userId;
                                if (userIdObj instanceof String) {
                                    userId = java.util.UUID.fromString((String) userIdObj);
                                } else if (userIdObj instanceof java.util.UUID) {
                                    userId = (java.util.UUID) userIdObj;
                                } else {
                                    userId = null;
                                }
                                if (userId != null) {
                                    java.util.List<com.loyalixa.backend.security.RefreshToken> activeTokens = 
                                        refreshTokenService.findAllActiveByUserId(userId);
                                    if (activeTokens.isEmpty()) {
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                        response.setContentType("application/json");
                                        response.getWriter().write("{\"error\": \"Session expired. Please log in again.\", \"code\": \"SESSION_EXPIRED\"}");
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to verify refresh token for access token", e);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Token verification failed. Please log in again.\", \"code\": \"TOKEN_VERIFICATION_FAILED\"}");
                        return;
                    }
                    requestPath = request.getRequestURI();
                    if (requestPath != null && requestPath.startsWith("/api/v1/admin/")) {
                        Optional<User> userOpt = userRepository.findByEmail(userEmail);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            if (user.getRole() != null && "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
                                Map<String, Object> errorResponse = new HashMap<>();
                                errorResponse.put("error", "Students are not allowed to access the dashboard. Only staff members can access admin endpoints.");
                                errorResponse.put("status", "STUDENT_DASHBOARD_ACCESS_DENIED");
                                errorResponse.put("code", "FORBIDDEN");
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);  
                                response.setContentType("application/json");
                                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                                return;  
                            }
                        }
                    }
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,  
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token has expired\"}");
            return;
        } catch (AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
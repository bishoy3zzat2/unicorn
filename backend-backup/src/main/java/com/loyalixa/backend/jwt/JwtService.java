package com.loyalixa.backend.jwt;
import com.loyalixa.backend.config.JwtConfigService;
import com.loyalixa.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;  
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Service
public class JwtService {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    private final JwtConfigService jwtConfigService;
    public JwtService(JwtConfigService jwtConfigService) {
        this.jwtConfigService = jwtConfigService;
    }
    public String generateAccessToken(User user) {
        return generateAccessToken(user, null);
    }
    public String generateAccessToken(User user, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        if (user.getRole() != null) {
            claims.put("role", user.getRole().getName());  
        } else {
            claims.put("role", "USER");  
        }
        if (deviceId != null && !deviceId.isEmpty()) {
            claims.put("deviceId", deviceId);
        }
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfigService.getAccessTokenDurationMs()))
                .signWith(getSigningKey())
                .compact();
    }
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public Date getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
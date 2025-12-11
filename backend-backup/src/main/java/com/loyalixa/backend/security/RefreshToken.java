package com.loyalixa.backend.security;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;  
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_expiry_date", columnList = "expiry_date")
})
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 1000) 
    private String token; 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @Column(nullable = false)
    private Instant expiryDate;
    @Column(name = "device_id", length = 255, nullable = false)
    private String deviceId;
    @Column(name = "device_name", length = 255)
    private String deviceName;
    @Column(name = "device_type", length = 50)
    private String deviceType;
    @Column(name = "user_agent", length = 512)
    private String userAgent;
    @Column(name = "ip_address", length = 100)
    private String ipAddress;
    @Column(name = "last_used_at")
    private Instant lastUsedAt;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
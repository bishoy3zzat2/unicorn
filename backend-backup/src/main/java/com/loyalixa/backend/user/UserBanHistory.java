package com.loyalixa.backend.user;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_ban_history")
public class UserBanHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "action", nullable = false, length = 20)
    private String action;  
    @Column(name = "reason", length = 1000)
    private String reason;
    @Column(name = "ban_type", length = 20)
    private String banType;  
    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;
    @Column(name = "action_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime actionAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;  
    @Column(name = "performed_by_email", length = 255)
    private String performedByEmail;  
    @Column(name = "notes", length = 2000)
    private String notes;  
}

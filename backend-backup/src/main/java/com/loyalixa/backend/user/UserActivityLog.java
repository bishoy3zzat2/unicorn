package com.loyalixa.backend.user;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_activity_logs")
public class UserActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String activityType;  
    @Column(columnDefinition = "TEXT")
    private String details;  
    private String resourceId;  
    private Long durationSeconds;  
    @CreationTimestamp
    private LocalDateTime timestamp;
}
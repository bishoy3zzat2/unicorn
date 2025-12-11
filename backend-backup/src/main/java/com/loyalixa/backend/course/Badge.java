package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    private LocalDateTime validUntil;
    private Duration usageDuration; 
    @Column(nullable = false)
    private Boolean isDynamic = false;
    @Column(nullable = false, length = 50)
    private String name;  
    @Column(length = 50)
    private String iconClass;  
    @Column(length = 10)
    private String colorCode;  
    @Column(name = "custom_css", columnDefinition = "TEXT")
    private String customCss;  
    @Column(name = "weight", nullable = false)
    private Double weight = 0.0;  
    private LocalDateTime expirationDate;  
    // Comma-separated list of targets (e.g. "COURSE", or "COURSE,SHOP_PRODUCT,USER")
    // Increased length to safely support multiple targets.
    @Column(nullable = false, length = 100)
    private String targetType; 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", nullable = true)
    private User updatedBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "badge", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseBadge> coursesUsingBadge;
}
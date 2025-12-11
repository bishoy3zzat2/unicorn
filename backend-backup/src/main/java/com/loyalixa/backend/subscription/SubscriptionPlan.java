package com.loyalixa.backend.subscription;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;  
    @Column(nullable = false, unique = true, length = 50)
    private String code;  
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "plan_type", nullable = false, length = 20)
    private String planType;
    @Column(name = "duration_days")
    private Integer durationDays;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    @Column(length = 10)
    private String currency = "USD";
    @Column(nullable = false)
    private Boolean isActive = true;
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    @Column(name = "allowed_course_types", columnDefinition = "TEXT")
    private String allowedCourseTypes;  
    @Column(name = "max_course_enrollments")
    private Integer maxCourseEnrollments;
    @Column(name = "max_bundle_enrollments")
    private Integer maxBundleEnrollments;
    @Column(name = "daily_coins", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyCoins = BigDecimal.ZERO;
    @Column(name = "streak_bonus_coins", columnDefinition = "TEXT")
    private String streakBonusCoins;  
    @Column(name = "course_completion_coins", nullable = false, precision = 19, scale = 2)
    private BigDecimal courseCompletionCoins = BigDecimal.ZERO;
    @Column(name = "course_enrollment_coins", nullable = false, precision = 19, scale = 2)
    private BigDecimal courseEnrollmentCoins = BigDecimal.ZERO;
    @Column(name = "course_review_coins", nullable = false, precision = 19, scale = 2)
    private BigDecimal courseReviewCoins = BigDecimal.ZERO;
    @Column(name = "certificate_coins", nullable = false, precision = 19, scale = 2)
    private BigDecimal certificateCoins = BigDecimal.ZERO;
    @Column(name = "invitation_coins", nullable = false, precision = 19, scale = 2)
    private BigDecimal invitationCoins = BigDecimal.ZERO;
    @Column(name = "max_devices", nullable = false)
    private Integer maxDevices = 1;
    @Column(name = "has_premium_access", nullable = false)
    private Boolean hasPremiumAccess = false;
    @Column(name = "has_download_access", nullable = false)
    private Boolean hasDownloadAccess = false;
    @Column(name = "has_certificate_access", nullable = false)
    private Boolean hasCertificateAccess = false;
    @Column(name = "has_premium_support", nullable = false)
    private Boolean hasPremiumSupport = false;
    @Column(name = "has_exclusive_content", nullable = false)
    private Boolean hasExclusiveContent = false;
    @Column(name = "has_live_courses", nullable = false)
    private Boolean hasLiveCourses = false;
    @Column(name = "has_quizzes_access", nullable = false)
    private Boolean hasQuizzesAccess = true;
    @Column(name = "has_community_access", nullable = false)
    private Boolean hasCommunityAccess = true;
    @Column(name = "additional_features", columnDefinition = "TEXT")
    private String additionalFeatures;  
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSubscription> userSubscriptions;
}

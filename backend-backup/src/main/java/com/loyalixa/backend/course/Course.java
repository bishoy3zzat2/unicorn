package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID; 
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, unique = true) 
    private String slug;  
    @Column(columnDefinition = "TEXT")
    private String shortDescription;
    @Column(columnDefinition = "TEXT")
    private String fullDescription;
    @Column(nullable = false)
    private BigDecimal price;
    private BigDecimal discountPrice;  
    @Column(length = 20)
    private String discountType;  
    private BigDecimal discountValue;  
    @Column(nullable = false)
    private Boolean discountIsFixed = true;  
    private BigDecimal discountDecayRate;  
    private LocalDateTime discountExpiresAt;
    public void calculateDiscountPrice() {
        if (discountType == null || discountValue == null || price == null) {
            this.discountPrice = null;
            return;
        }
        BigDecimal currentDiscountValue = discountValue;
        if (discountIsFixed != null && !discountIsFixed && discountDecayRate != null && discountDecayRate.compareTo(BigDecimal.ZERO) > 0 && discountExpiresAt != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(discountExpiresAt)) {
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, discountExpiresAt);
                BigDecimal totalDaysDecimal = discountValue.divide(discountDecayRate, 2, java.math.RoundingMode.HALF_UP);
                long totalDays = totalDaysDecimal.longValue();
                if (totalDaysDecimal.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) > 0) {
                    totalDays++;  
                }
                long daysPassed = totalDays - daysRemaining;
                if (daysPassed > 0 && daysRemaining >= 0) {
                    BigDecimal decayAmount = discountDecayRate.multiply(BigDecimal.valueOf(daysPassed));
                    currentDiscountValue = discountValue.subtract(decayAmount);
                    if (currentDiscountValue.compareTo(BigDecimal.ZERO) < 0) {
                        currentDiscountValue = BigDecimal.ZERO;
                    }
                } else if (daysRemaining >= totalDays) {
                    currentDiscountValue = discountValue;
                }
            } else {
                currentDiscountValue = BigDecimal.ZERO;
            }
        }
        if ("PERCENTAGE".equals(discountType)) {
            BigDecimal discountAmount = price.multiply(currentDiscountValue).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            this.discountPrice = price.subtract(discountAmount);
            if (this.discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                this.discountPrice = BigDecimal.ZERO;
            }
        } else if ("FIXED_AMOUNT".equals(discountType)) {
            this.discountPrice = price.subtract(currentDiscountValue);
            if (this.discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                this.discountPrice = BigDecimal.ZERO;
            }
        } else {
            this.discountPrice = null;
        }
    }
    public BigDecimal getCurrentDiscountPrice() {
        calculateDiscountPrice();
        return this.discountPrice;
    }
    @Column(length = 10)
    private String currency;
    @Column(length = 20)
    private String accessType;  
    private Integer accessDurationValue;  
    @Column(length = 20)
    private String accessDurationUnit;  
    @Column(nullable = false, length = 50)
    private String level;
    private String durationText;
    private String coverImageUrl;  
    @Column(nullable = false, length = 20)
    private String status;  
    @Column(length = 20)
    private String approvalStatus;  
    @Column(nullable = false)
    private Boolean isUnderReview = false;
    @Column(nullable = false, length = 20)
    private String visibility = "PUBLIC";  
    @Column(length = 50)
    private String currentStage;  
    @Column(nullable = false)
    private Boolean isFeatured = false;  
    private String learningFormat;  
    @Column(nullable = false, length = 50)
    private String language;  
    private String subtitlesLanguages;  
    @Column(length = 100)
    @Deprecated
    private String organizationName;  
    @Deprecated
    private String providerLogoUrl;  
    @Column(nullable = false)
    private Boolean hasFreeContent = false;
    @Column(length = 50)
    private String academicDegree;  
    @Column(nullable = false)
    private Boolean isRefundable = false;
    @Column(nullable = false)
    private Boolean hasDownloadableContent = false;
    @Column(nullable = false)
    private Integer adminRatingPoints = 0;  
    @Column(nullable = false)
    private Boolean ratingPointsIsFixed = true;  
    private LocalDateTime ratingPointsExpiresAt;  
    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CourseCertificate certificate;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_providers_relation",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "provider_id")
    )
    private Set<CourseProvider> providers;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_instructors",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "instructor_id")
    )
    private Set<User> instructors;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_moderators",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "moderator_id")
    )
    private Set<User> moderators;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_allowed_users",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> allowedUsers; 
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_tags",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")  
    private Set<CourseSection> sections;
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments;
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    private Set<CourseBundle> bundles;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_categories",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseReview> reviews; 
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseBadge> courseBadges = new HashSet<>();  
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "course_skills",  
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CoursePrerequisite> prerequisites;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;  
    private LocalDateTime archivedAt;  
    private LocalDateTime unarchivedAt;  
}
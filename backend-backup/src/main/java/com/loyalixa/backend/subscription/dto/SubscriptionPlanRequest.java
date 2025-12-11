package com.loyalixa.backend.subscription.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
public record SubscriptionPlanRequest(
    @NotBlank(message = "Plan name is required")
    String name,
    @NotBlank(message = "Plan code (slug) is required")
    String code,  
    String description,
    @NotBlank(message = "Plan type is required")
    String planType,  
    Integer durationDays,  
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    BigDecimal price,
    String currency,  
    Boolean isActive,
    Boolean isDefault,
    Integer displayOrder,
    List<String> allowedCourseTypes,  
    Integer maxCourseEnrollments,  
    Integer maxBundleEnrollments,  
    @NotNull(message = "Daily coins is required")
    @PositiveOrZero(message = "Daily coins must be positive or zero")
    BigDecimal dailyCoins,
    Map<String, BigDecimal> streakBonusCoins,  
    @NotNull(message = "Course completion coins is required")
    @PositiveOrZero(message = "Course completion coins must be positive or zero")
    BigDecimal courseCompletionCoins,
    @NotNull(message = "Course enrollment coins is required")
    @PositiveOrZero(message = "Course enrollment coins must be positive or zero")
    BigDecimal courseEnrollmentCoins,
    @NotNull(message = "Course review coins is required")
    @PositiveOrZero(message = "Course review coins must be positive or zero")
    BigDecimal courseReviewCoins,
    @NotNull(message = "Certificate coins is required")
    @PositiveOrZero(message = "Certificate coins must be positive or zero")
    BigDecimal certificateCoins,
    @NotNull(message = "Invitation coins is required")
    @PositiveOrZero(message = "Invitation coins must be positive or zero")
    BigDecimal invitationCoins,
    @NotNull(message = "Max devices is required")
    @PositiveOrZero(message = "Max devices must be positive or zero")
    Integer maxDevices,
    Boolean hasPremiumAccess,
    Boolean hasDownloadAccess,
    Boolean hasCertificateAccess,
    Boolean hasPremiumSupport,
    Boolean hasExclusiveContent,
    Boolean hasLiveCourses,
    Boolean hasQuizzesAccess,
    Boolean hasCommunityAccess,
    Map<String, Object> additionalFeatures
) {}

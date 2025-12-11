package com.loyalixa.backend.user.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record UserFullDetailsResponse(
    AccountInfo accountInfo,
    ProfileInfo profileInfo,
    RoleInfo roleInfo,
    CoursesInfo coursesInfo,
    DiscountsInfo discountsInfo,
    GiftsInfo giftsInfo,
    ProgressInfo progressInfo,
    Statistics statistics,
    PreferencesInfo preferences,
    DeviceMetadataInfo deviceMetadata,
    SuspensionBanHistoryInfo suspensionBanHistory,
    SubscriptionInfo subscriptionInfo
) {
    public record AccountInfo(
        UUID id,
        String username,
        String email,
        String status,
        String authProvider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        LocalDateTime passwordChangedAt,
        LocalDateTime deletedAt,
        String deletionReason,
        LocalDateTime suspendedAt,
        String suspendReason,
        LocalDateTime suspendedUntil,
        String suspensionType,
        LocalDateTime bannedAt,
        String banReason,
        LocalDateTime bannedUntil,
        String banType,
        Boolean appealRequested,
        String appealReason,
        LocalDateTime appealRequestedAt,
        String appealStatus,
        LocalDateTime appealReviewedAt,
        UUID appealReviewedById,
        String appealReviewedByEmail
    ) {}
    public record ProfileInfo(
        String firstName,
        String lastName,
        String bio,
        String avatarUrl,
        String phoneNumber,
        String phoneSocialApp,
        String secondaryEmail,
        String tshirtSize,
        String extraInfo
    ) {}
    public record RoleInfo(
        String roleName,
        List<String> permissions
    ) {}
    public record CourseDetails(
        UUID courseId,
        String courseTitle,
        String enrollmentDate,
        String paymentStatus,
        String enrollmentSource,
        String enrollmentStatus,
        String startDate,
        Long totalLessons,
        Long completedLessons,
        Integer progressPercentage,
        Long totalTimeSpentSeconds
    ) {}
    public record CoursesInfo(
        Long totalEnrollments,
        Long activeEnrollments,
        Long completedEnrollments,
        Long freeEnrollments,
        Long paidEnrollments,
        List<CourseDetails> courses
    ) {}
    public record UserDiscountDetails(
        UUID discountCodeId,
        String discountCode,
        String discountType,
        BigDecimal discountValue,
        Boolean isUsed,
        LocalDateTime createdAt
    ) {}
    public record DiscountsInfo(
        Long totalAssignedCodes,
        Long usedCodes,
        Long unusedCodes,
        Long codesIssuedByUser,
        Long codesIssuedToUser,
        List<UserDiscountDetails> assignedDiscounts,
        List<DiscountCodeCreated> codesCreated
    ) {}
    public record DiscountCodeCreated(
        UUID discountCodeId,
        String code,
        String discountType,
        BigDecimal discountValue,
        Integer maxUses,
        Integer currentUses,
        LocalDateTime createdAt
    ) {}
    public record GiftSent(
        UUID giftId,
        UUID courseId,
        String courseTitle,
        String recipientEmail,
        String voucherCode,
        String status,
        LocalDateTime issuedAt,
        LocalDateTime redeemedAt
    ) {}
    public record GiftReceived(
        UUID giftId,
        UUID courseId,
        String courseTitle,
        String senderEmail,
        String voucherCode,
        LocalDateTime issuedAt,
        LocalDateTime redeemedAt
    ) {}
    public record GiftsInfo(
        Long totalGiftsSent,
        Long redeemedGiftsSent,
        Long pendingGiftsSent,
        Long totalGiftsReceived,
        Long redeemedGiftsReceived,
        List<GiftSent> giftsSent,
        List<GiftReceived> giftsReceived
    ) {}
    public record ProgressInfo(
        Long totalLessonsCompleted,
        Long totalLessonsInProgress,
        Long totalTimeSpentSeconds,
        Long totalCoursesWithProgress
    ) {}
    public record Statistics(
        Long totalEnrollments,
        Long activeEnrollments,
        Long completedEnrollments,
        Long totalDiscountsAssigned,
        Long usedDiscounts,
        Long unusedDiscounts,
        Long totalGiftsSent,
        Long totalGiftsReceived,
        Long totalCodesCreated,
        Long totalLessonsCompleted,
        Long totalTimeSpentHours
    ) {}
    public record PreferencesInfo(
        String uiTheme,
        String uiLanguage,
        String timezone,
        java.util.Map<String, Boolean> notifications
    ) {}
    public record DeviceMetadataInfo(
        String userAgent,
        String browser,
        String operatingSystem,
        String deviceType,
        String ipAddress,
        String acceptLanguage,
        String acceptEncoding,
        String dnt,
        String referrer,
        String host,
        String origin,
        String timezone,
        String platform,
        Integer screenWidth,
        Integer screenHeight,
        Integer viewportWidth,
        Integer viewportHeight,
        Double devicePixelRatio,
        Integer hardwareConcurrency,
        Double deviceMemoryGb,
        Boolean touchSupport
    ) {}
    public record SuspensionHistoryItem(
        String action,  
        String reason,
        String suspensionType,  
        LocalDateTime suspendedUntil,
        LocalDateTime actionAt,
        UUID performedById,
        String performedByEmail
    ) {}
    public record BanHistoryItem(
        String action,  
        String reason,
        String banType,  
        LocalDateTime bannedUntil,
        LocalDateTime actionAt,
        UUID performedById,
        String performedByEmail
    ) {}
    public record SuspensionBanHistoryInfo(
        Long totalSuspensions,
        Long totalBans,
        List<SuspensionHistoryItem> suspensionHistory,
        List<BanHistoryItem> banHistory
    ) {}
    public record SubscriptionDetails(
        UUID subscriptionId,
        UUID planId,
        String planName,
        String planCode,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime cancelledAt,
        String cancellationReason,
        Boolean autoRenew,
        LocalDateTime lastRenewedAt,
        Integer renewalCount,
        String paymentReference,
        String notes,
        Boolean isActive,
        Boolean isExpired,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
    public record SubscriptionInfo(
        Long totalSubscriptions,
        Long activeSubscriptions,
        Long expiredSubscriptions,
        Long cancelledSubscriptions,
        SubscriptionDetails activeSubscription,
        List<SubscriptionDetails> allSubscriptions
    ) {}
}

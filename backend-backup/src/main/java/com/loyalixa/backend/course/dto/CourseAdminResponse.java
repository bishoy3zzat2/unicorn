package com.loyalixa.backend.course.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
public record CourseAdminResponse(
    UUID id,
    String title,
    String slug,
    String shortDescription,
    String fullDescription,
    BigDecimal price,
    BigDecimal discountPrice,  
    String discountType,  
    BigDecimal discountValue,  
    Boolean discountIsFixed,  
    BigDecimal discountDecayRate,  
    LocalDateTime discountExpiresAt,  
    String currency,
    String accessType,
    Integer accessDurationValue,
    String accessDurationUnit,
    String level,
    String durationText,
    String coverImageUrl,
    String status,  
    String approvalStatus,  
    Boolean isUnderReview,  
    String currentStage,  
    Boolean isFeatured,
    String learningFormat,
    String language,
    String subtitlesLanguages,
    String academicDegree,  
    Boolean isRefundable,  
    Boolean hasDownloadableContent,  
    Integer adminRatingPoints,  
    Boolean ratingPointsIsFixed,  
    LocalDateTime ratingPointsExpiresAt,  
    CertificateInfo certificate,  
    List<ProviderInfo> providers,  
    String organizationName,  
    String providerLogoUrl,  
    Boolean hasFreeContent,
    String visibility,  
    UUID createdById,
    String createdByUsername,
    String createdByEmail,
    List<InstructorInfo> instructors,
    List<ModeratorInfo> moderators,
    List<AllowedUserInfo> allowedUsers,  
    List<CategoryInfo> categories,
    List<TagInfo> tags,
    List<SkillInfo> skills,
    List<BadgeInfo> badges,
    List<PrerequisiteInfo> prerequisites,  
    CourseStatistics statistics,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime approvedAt,  
    LocalDateTime archivedAt,  
    LocalDateTime unarchivedAt  
) {
    public record InstructorInfo(UUID id, String username, String email, String roleName) {}
    public record ModeratorInfo(UUID id, String username, String email, String roleName) {}
    public record AllowedUserInfo(UUID id, String username, String email) {}
    public record CategoryInfo(Long id, String name, String slug) {}
    public record TagInfo(UUID id, String name) {}
    public record SkillInfo(UUID id, String name) {}
    public record BadgeInfo(
        UUID id, 
        String name, 
        String colorCode, 
        String iconClass,
        LocalDateTime expirationDate,  
        Long usageDurationMinutes,  
        LocalDateTime courseBadgeExpirationDate,  
        LocalDateTime assignedAt  
    ) {}
    public record ProviderInfo(UUID id, String name, String logoUrl, String websiteUrl) {}
    public record CertificateInfo(
        UUID id,
        String slug,
        String title,
        String description,
        String requirements,
        Integer minCompletionPercentage,
        Boolean requiresInterview,
        Boolean requiresSpecialExam,
        String examRequirements,
        String templateUrl,
        Boolean isActive,
        Integer validityMonths
    ) {}
    public record PrerequisiteInfo(
        String type,  
        String id,  
        String name,  
        String requirementType  
    ) {}
    public record CourseStatistics(
        Long totalEnrollments,  
        Long giftReceiversCount,  
        Long giftSendersCount,  
        Long totalModules,  
        Long totalLessons,  
        List<ModuleInfo> modulesInfo,  
        Integer totalCoursePoints,  
        Double averageStudentScore,  
        List<DiscountCodeInfo> discountCodes  
    ) {
        public record ModuleInfo(
            Long id,
            String title,
            Integer orderIndex,
            Long lessonsCount,
            Boolean isFreePreview
        ) {}
        public record DiscountCodeInfo(
            UUID id,
            String code,
            String discountType,  
            BigDecimal discountValue,
            Integer currentUses,
            Integer maxUses,
            LocalDateTime validUntil,
            Boolean isPrivate,
            String permissions  
        ) {}
    }
}

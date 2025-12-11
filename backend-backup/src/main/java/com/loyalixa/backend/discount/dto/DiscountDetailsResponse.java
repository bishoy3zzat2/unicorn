package com.loyalixa.backend.discount.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record DiscountDetailsResponse(
    UUID id,
    String code,
    String discountType,
    BigDecimal discountValue,
    Integer maxUses,
    Integer currentUses,
    Integer remainingUses,  
    Boolean isPrivate,
    LocalDateTime validUntil,
    LocalDateTime createdAt,
    Boolean isExpired,  
    Long daysUntilExpiry,  
    Long hoursUntilExpiry,  
    Long minutesUntilExpiry,  
    String applicableTo,  
    List<CourseInfo> applicableCourses,
    List<ProductInfo> applicableProducts,
    List<UserInfo> eligibleUsers,
    List<UserUsageInfo> usersWhoUsed,
    UserInfo createdBy,
    UserInfo updatedBy,
    LocalDateTime updatedAt
) {
    public record CourseInfo(UUID id, String title) {}
    public record ProductInfo(UUID id, String name) {}
    public record UserInfo(UUID id, String email, String username, String roleName) {}
    public record UserUsageInfo(UUID userId, String email, String username, Boolean isUsed, LocalDateTime usedAt) {}
}

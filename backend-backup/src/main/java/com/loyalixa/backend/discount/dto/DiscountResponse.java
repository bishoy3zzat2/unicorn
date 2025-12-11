package com.loyalixa.backend.discount.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record DiscountResponse(
    UUID id,
    String code,
    String discountType,
    BigDecimal discountValue,
    Integer maxUses,
    Integer currentUses,
    Boolean isPrivate,
    LocalDateTime validUntil,
    LocalDateTime createdAt,
    String applicableTo,  
    List<CourseInfo> applicableCourses,
    List<ProductInfo> applicableProducts,
    UserInfo createdBy,
    UserInfo updatedBy
) {
    public record CourseInfo(UUID id, String title) {}
    public record ProductInfo(UUID id, String name) {}
    public record UserInfo(UUID id, String email, String username) {}
}
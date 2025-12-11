package com.loyalixa.backend.discount.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record DiscountRequest(
    @NotBlank
    String code,  
    @NotBlank
    String discountType,  
    @NotNull @DecimalMin(value = "0.01")
    BigDecimal discountValue, 
    Integer maxUses,  
    Boolean isPrivate,  
    LocalDateTime validUntil,  
    String applicableTo,  
    List<UUID> applicableCourseIds,  
    List<UUID> applicableProductIds,  
    List<UUID> eligibleUserIds  
) {}
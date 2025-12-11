package com.loyalixa.backend.discount.dto;
import java.time.LocalDateTime;
public record DiscountSearchRequest(
    String code,                     
    String discountType,             
    Boolean isPrivate,               
    Boolean isExpired,               
    Boolean isExhausted,             
    LocalDateTime createdAtFrom,     
    LocalDateTime createdAtTo,       
    LocalDateTime validUntilFrom,    
    LocalDateTime validUntilTo       
) {}

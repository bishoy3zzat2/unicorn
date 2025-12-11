package com.loyalixa.backend.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "Product name cannot be empty.") @Size(max = 255, message = "Product name must be less than 255 characters.") String name,

        @NotBlank(message = "Product slug cannot be empty.") @Size(max = 255, message = "Product slug must be less than 255 characters.") String slug,

        String description,
        String shortDescription,

        BigDecimal price,
        BigDecimal priceInCoins,
        String currency,

        @NotBlank(message = "Payment method cannot be empty.") String paymentMethod, // MONEY_ONLY, COINS_ONLY, BOTH

        String status, // DRAFT, PUBLISHED, ARCHIVED, OUT_OF_STOCK

        Integer stockQuantity,
        Boolean isFeatured,
        String category,

        List<MediaRequest> media) {
    public record MediaRequest(
            String mediaType, // IMAGE, VIDEO
            String mediaUrl,
            String thumbnailUrl,
            Integer orderIndex,
            String altText,
            Boolean autoplay, // For videos
            Boolean muted, // For videos
            Boolean loop, // For videos
            Boolean controls // For videos
    ) {
    }
}

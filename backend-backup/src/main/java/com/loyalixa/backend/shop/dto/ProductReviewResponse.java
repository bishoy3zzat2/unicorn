package com.loyalixa.backend.shop.dto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record ProductReviewResponse(
    UUID id,
    UUID productId,
    UUID userId,
    String userName,
    String userEmail,
    String userAvatarUrl,
    Integer rating,
    String comment,
    String status,
    Boolean isFeatured,
    List<ReviewMediaResponse> media,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record ReviewMediaResponse(
        UUID id,
        String mediaType,
        String mediaUrl,
        String thumbnailUrl,
        String altText
    ) {}
}

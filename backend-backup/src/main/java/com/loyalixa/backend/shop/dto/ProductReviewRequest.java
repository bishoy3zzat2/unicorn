package com.loyalixa.backend.shop.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record ProductReviewRequest(
    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    Integer rating,
    String comment,
    List<ReviewMediaRequest> media
) {
    public record ReviewMediaRequest(
        String mediaType,
        String mediaUrl,
        String thumbnailUrl,
        String altText
    ) {}
}

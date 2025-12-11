package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;
public record BadgeRankingWeightUpdateRequest(
    @NotNull
    List<BadgeWeightUpdateItem> badgeWeights
) {
    public record BadgeWeightUpdateItem(
        @NotNull
        UUID badgeId,
        @PositiveOrZero
        Double weightValue  
    ) {}
}

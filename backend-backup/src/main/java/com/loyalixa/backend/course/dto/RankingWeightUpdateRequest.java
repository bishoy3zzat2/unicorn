package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
public record RankingWeightUpdateRequest(
    @NotNull(message = "Weights list cannot be null")
    List<WeightUpdateItem> weights
) {
    public record WeightUpdateItem(
        @NotNull(message = "Factor name cannot be null")
        String factorName,
        @NotNull(message = "Weight value cannot be null")
        @PositiveOrZero(message = "Weight value must be positive or zero")
        Double weightValue,
        @PositiveOrZero(message = "Decay rate must be positive or zero")
        Double decayRate,
        @Min(value = 1, message = "Decay period days must be at least 1")
        Integer decayPeriodDays
    ) {}
}

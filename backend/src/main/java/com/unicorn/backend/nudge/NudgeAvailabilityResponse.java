package com.unicorn.backend.nudge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for nudge availability check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NudgeAvailabilityResponse {

    private boolean canNudge;
    private String reason;
    private int remainingNudges;
    private LocalDateTime cooldownEndsAt;
    private String currentPlan;
    private int usedThisMonth;
    private int monthlyLimit;

    public static NudgeAvailabilityResponse allowed(String plan, int remaining, int used, int limit) {
        return NudgeAvailabilityResponse.builder()
                .canNudge(true)
                .currentPlan(plan)
                .remainingNudges(remaining)
                .usedThisMonth(used)
                .monthlyLimit(limit)
                .build();
    }

    public static NudgeAvailabilityResponse denied(String reason, String plan, int remaining, int used, int limit,
            LocalDateTime cooldownEnds) {
        return NudgeAvailabilityResponse.builder()
                .canNudge(false)
                .reason(reason)
                .currentPlan(plan)
                .remainingNudges(remaining)
                .usedThisMonth(used)
                .monthlyLimit(limit)
                .cooldownEndsAt(cooldownEnds)
                .build();
    }
}

package com.unicorn.backend.nudge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for user nudge statistics in admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNudgeStatsResponse {

    private String userRole;

    // For INVESTOR - received nudges
    private Long receivedCount;

    // For STARTUP_OWNER - sent nudges
    private Long sentCount;
    private Integer remainingThisMonth;
    private Integer monthlyLimit;
    private String currentPlan;

    // Common - list of nudges
    private List<NudgeInfoResponse> nudges;
}

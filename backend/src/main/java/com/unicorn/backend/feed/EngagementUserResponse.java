package com.unicorn.backend.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for returning user engagement info (likes, shares).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngagementUserResponse {
    private UUID userId;
    private String userName;
    private String userUsername;
    private String userAvatarUrl;
    private String userPlan;
    private LocalDateTime engagedAt;
}

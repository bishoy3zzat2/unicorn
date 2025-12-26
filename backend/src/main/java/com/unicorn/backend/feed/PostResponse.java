package com.unicorn.backend.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning post data to clients.
 * Includes author info and ranking breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private UUID id;

    // ==================== Content ====================
    private String content;
    private String mediaUrl;
    private String contextualTitle;

    // ==================== Author Info ====================
    private UUID authorId;
    private String authorName;
    private String authorUsername;
    private String authorAvatarUrl;
    private Boolean authorIsVerified;

    /**
     * Author's role: INVESTOR, STARTUP_OWNER, or USER
     */
    private String authorRole;

    /**
     * Author's subscription plan: FREE, PRO, ELITE
     */
    private String authorPlan;

    // ==================== Status ====================
    private String status;
    private Boolean isFeatured;
    private LocalDateTime featuredAt;
    private LocalDateTime featuredUntil;
    private UUID featuredBy;
    private Boolean isEdited;
    private Integer editCount;

    // ==================== Engagement ====================
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;

    /**
     * Whether the current user has liked this post.
     * NULL for admin requests.
     */
    private Boolean isLikedByCurrentUser;

    // ==================== Ranking (Admin Only) ====================
    private Double rankingScore;
    private Double subscriptionMultiplier;
    private LocalDateTime scoreCalculatedAt;

    // ==================== Timestamps ====================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastEditedAt;

    // ==================== Moderation (Admin Only) ====================
    private UUID moderatedBy;
    private String moderationReason;
    private LocalDateTime moderatedAt;
}

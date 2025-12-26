package com.unicorn.backend.feed;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a social feed post.
 * Implements the Unicorn Ranking Algorithm with time decay and plan boosts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_ranking", columnList = "ranking_score DESC, created_at DESC"),
        @Index(name = "idx_post_author", columnList = "author_id"),
        @Index(name = "idx_post_status", columnList = "status"),
        @Index(name = "idx_post_featured", columnList = "is_featured"),
        @Index(name = "idx_post_created_at", columnList = "created_at")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ==================== Author ====================

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    // ==================== Content ====================

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    // ==================== Contextual Title ====================

    /**
     * User-selected title linked to their active startup.
     * Example: "CEO at StartupName"
     */
    @Column(name = "contextual_title", length = 200)
    private String contextualTitle;

    /**
     * FK to startup for title resolution.
     * If startup is deleted, contextualTitle should be cleared.
     */
    @Column(name = "contextual_startup_id")
    private UUID contextualStartupId;

    // ==================== Status & Moderation ====================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    /**
     * Admin can pin posts to the top of the feed.
     */
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "featured_at")
    private LocalDateTime featuredAt;

    /**
     * When the featured status should expire.
     * If null, the post stays featured indefinitely until manually unfeatured.
     */
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;

    /**
     * Admin who featured the post.
     */
    @Column(name = "featured_by")
    private UUID featuredBy;

    /**
     * Admin who hid/deleted the post.
     */
    @Column(name = "moderated_by")
    private UUID moderatedBy;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    // ==================== Editing ====================

    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "edit_count")
    @Builder.Default
    private Integer editCount = 0;

    /**
     * Timestamp of when media was last edited.
     * Used to enforce the 2-hour media edit rule.
     */
    @Column(name = "media_edited_at")
    private LocalDateTime mediaEditedAt;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    // ==================== Engagement Counters ====================
    // Denormalized for performance - updated via triggers/service

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "share_count")
    @Builder.Default
    private Integer shareCount = 0;

    // ==================== Ranking ====================

    /**
     * Subscription multiplier frozen at post creation time.
     * This ensures posts keep their earned visibility even if user changes plan.
     * Values: 1.0 (FREE), 1.5 (PRO), 2.0 (ELITE)
     */
    @Column(name = "subscription_multiplier")
    @Builder.Default
    private Double subscriptionMultiplier = 1.0;

    /**
     * Pre-calculated ranking score for feed ordering.
     * Formula: (BaseScore * SubscriptionMultiplier) / (Age + 1)^G - EditPenalty
     */
    @Column(name = "ranking_score")
    @Builder.Default
    private Double rankingScore = 0.0;

    /**
     * Last time the ranking score was recalculated.
     * Used by batch job for time decay recalculation.
     */
    @Column(name = "score_calculated_at")
    private LocalDateTime scoreCalculatedAt;

    // ==================== Timestamps ====================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== Helper Methods ====================

    /**
     * Increment like count and trigger score recalculation marker.
     */
    public void incrementLikes() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    public void decrementLikes() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }

    public void incrementComments() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }

    public void decrementComments() {
        this.commentCount = Math.max(0, (this.commentCount == null ? 0 : this.commentCount) - 1);
    }

    public void incrementShares() {
        this.shareCount = (this.shareCount == null ? 0 : this.shareCount) + 1;
    }

    /**
     * Mark post as edited and apply penalty tracking.
     */
    public void markAsEdited() {
        this.isEdited = true;
        this.editCount = (this.editCount == null ? 0 : this.editCount) + 1;
        this.lastEditedAt = LocalDateTime.now();
    }

    /**
     * Mark media as edited (for 2-hour rule tracking).
     */
    public void markMediaEdited() {
        this.mediaEditedAt = LocalDateTime.now();
    }

    /**
     * Feature this post (pin to top) with optional duration.
     * 
     * @param adminId       The admin who is featuring the post
     * @param durationHours Optional duration in hours (null = indefinite)
     */
    public void feature(UUID adminId, Integer durationHours) {
        this.isFeatured = true;
        this.featuredAt = LocalDateTime.now();
        this.featuredBy = adminId;
        if (durationHours != null && durationHours > 0) {
            this.featuredUntil = LocalDateTime.now().plusHours(durationHours);
        } else {
            this.featuredUntil = null; // Indefinite
        }
    }

    /**
     * Unfeature this post.
     */
    public void unfeature() {
        this.isFeatured = false;
        this.featuredAt = null;
        this.featuredUntil = null;
        this.featuredBy = null;
    }

    /**
     * Check if the featured status is still active.
     * Returns true if featured and not expired.
     */
    public boolean isCurrentlyFeatured() {
        if (!Boolean.TRUE.equals(this.isFeatured)) {
            return false;
        }
        if (this.featuredUntil == null) {
            return true; // Indefinitely featured
        }
        return LocalDateTime.now().isBefore(this.featuredUntil);
    }
}

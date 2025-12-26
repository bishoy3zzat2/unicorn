package com.unicorn.backend.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for feed statistics shown in dashboard KPI cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedStatsResponse {

    private long totalPosts;
    private long activePosts;
    private long hiddenPosts;
    private long deletedPosts;
    private long featuredPosts;
    private long todayPosts;

    /**
     * Average engagement per post (likes + comments + shares).
     */
    private double avgEngagement;

    /**
     * Total likes across all posts.
     */
    private long totalLikes;

    /**
     * Total comments across all posts.
     */
    private long totalComments;

    /**
     * Total shares across all posts.
     */
    private long totalShares;
}

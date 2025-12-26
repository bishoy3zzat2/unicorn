package com.unicorn.backend.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Post entity with custom queries for feed operations.
 * Optimized for scalability with batch limits and cursor-based pagination.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

        // ==================== Basic Queries ====================

        Page<Post> findByStatus(PostStatus status, Pageable pageable);

        Page<Post> findByAuthorId(UUID authorId, Pageable pageable);

        Page<Post> findByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

        // ==================== Feed Queries (Ranked) ====================

        /**
         * Get active posts ordered by ranking score (featured first).
         */
        @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
                        "ORDER BY p.isFeatured DESC, p.featuredAt DESC NULLS LAST, " +
                        "p.rankingScore DESC, p.createdAt DESC")
        Page<Post> findActiveFeedPosts(Pageable pageable);

        /**
         * Get active posts excluding specific author (for "discover" feed).
         */
        @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND p.authorId <> :authorId " +
                        "ORDER BY p.isFeatured DESC, p.rankingScore DESC, p.createdAt DESC")
        Page<Post> findActiveFeedPostsExcludingAuthor(@Param("authorId") UUID authorId, Pageable pageable);

        // ==================== Cursor-Based Pagination (Mobile Infinite Scroll)
        // ====================

        /**
         * Get next page of feed using cursor (last seen rankingScore).
         * More efficient than offset-based pagination for large datasets.
         */
        @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND p.isFeatured = false " +
                        "AND (p.rankingScore < :cursorScore OR (p.rankingScore = :cursorScore AND p.id < :cursorId)) " +
                        "ORDER BY p.rankingScore DESC, p.id DESC")
        List<Post> findFeedPostsAfterCursor(
                        @Param("cursorScore") Double cursorScore,
                        @Param("cursorId") UUID cursorId,
                        Pageable pageable);

        /**
         * Get featured posts (always shown at top, limited).
         */
        @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND p.isFeatured = true " +
                        "ORDER BY p.featuredAt DESC")
        List<Post> findFeaturedPosts(Pageable pageable);

        // ==================== Stats Queries ====================

        long countByStatus(PostStatus status);

        long countByIsFeaturedTrue();

        @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startOfDay")
        long countTodayPosts(@Param("startOfDay") LocalDateTime startOfDay);

        @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt BETWEEN :start AND :end")
        long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        /**
         * Get average engagement for active posts.
         */
        @Query("SELECT COALESCE(AVG(p.likeCount + p.commentCount + p.shareCount), 0) FROM Post p WHERE p.status = 'ACTIVE'")
        Double getAverageEngagement();

        // ==================== Batch Operations (WITH LIMITS) ====================

        /**
         * Find posts needing score recalculation (LIMITED to prevent memory issues).
         * Processes 500 posts per batch, prioritizing recent posts.
         */
        @Query(value = "SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
                        "AND (p.scoreCalculatedAt IS NULL OR p.scoreCalculatedAt < :threshold) " +
                        "ORDER BY p.createdAt DESC")
        List<Post> findPostsNeedingScoreRecalculation(@Param("threshold") LocalDateTime threshold, Pageable pageable);

        /**
         * Find stale posts needing score recalculation (no limit version for manual
         * trigger).
         */
        @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
                        "AND (p.scoreCalculatedAt IS NULL OR p.scoreCalculatedAt < :threshold)")
        List<Post> findPostsNeedingScoreRecalculation(@Param("threshold") LocalDateTime threshold);

        /**
         * Batch update ranking scores (for scheduled job).
         */
        @Modifying
        @Query("UPDATE Post p SET p.rankingScore = :score, p.scoreCalculatedAt = :now WHERE p.id = :id")
        void updateRankingScore(@Param("id") UUID id, @Param("score") Double score, @Param("now") LocalDateTime now);

        // ==================== Contextual Title Cleanup ====================

        /**
         * Clear contextual titles when a startup is deleted.
         */
        @Modifying
        @Query("UPDATE Post p SET p.contextualTitle = NULL, p.contextualStartupId = NULL " +
                        "WHERE p.contextualStartupId = :startupId")
        void clearContextualTitlesByStartupId(@Param("startupId") UUID startupId);

        // ==================== Admin Queries ====================

        /**
         * Find posts by multiple statuses (for admin filtering).
         */
        @Query("SELECT p FROM Post p WHERE p.status IN :statuses ORDER BY p.createdAt DESC")
        Page<Post> findByStatusIn(@Param("statuses") List<PostStatus> statuses, Pageable pageable);

        /**
         * Search posts by content.
         */
        @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
        Page<Post> searchByContent(@Param("query") String query, Pageable pageable);
}

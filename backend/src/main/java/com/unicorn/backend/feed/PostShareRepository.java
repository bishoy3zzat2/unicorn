package com.unicorn.backend.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for PostShare entity.
 */
@Repository
public interface PostShareRepository extends JpaRepository<PostShare, UUID> {

    /**
     * Check if user has already shared a post.
     */
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    /**
     * Count shares for a post.
     */
    long countByPostId(UUID postId);

    /**
     * Get paginated shares for a post (ordered by most recent first).
     */
    Page<PostShare> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);
}

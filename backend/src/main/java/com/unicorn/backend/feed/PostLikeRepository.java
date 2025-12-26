package com.unicorn.backend.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PostLike entity.
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    /**
     * Check if user has liked a post.
     */
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    /**
     * Find a specific like.
     */
    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    /**
     * Count likes for a post.
     */
    long countByPostId(UUID postId);

    /**
     * Delete like by post and user.
     */
    void deleteByPostIdAndUserId(UUID postId, UUID userId);
}

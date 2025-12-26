package com.unicorn.backend.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Comment entity.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Get top-level comments for a post (parentId is null).
     */
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    Page<Comment> findTopLevelCommentsByPostId(@Param("postId") UUID postId, Pageable pageable);

    /**
     * Get replies to a specific comment.
     */
    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") UUID parentId);

    /**
     * Count non-deleted comments for a post.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.isDeleted = false")
    long countByPostId(@Param("postId") UUID postId);

    /**
     * Soft-delete comment and all its replies (cascade).
     */
    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.parentId = :parentId")
    void softDeleteReplies(@Param("parentId") UUID parentId);

    /**
     * Find all comments by author.
     */
    List<Comment> findByAuthorIdAndIsDeletedFalse(UUID authorId);
}

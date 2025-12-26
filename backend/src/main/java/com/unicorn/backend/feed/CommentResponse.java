package com.unicorn.backend.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for returning comment data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private UUID id;
    private UUID postId;
    private UUID parentId;
    private String content;

    // Author info
    private UUID authorId;
    private String authorName;
    private String authorUsername;
    private String authorAvatarUrl;
    private String authorRole;
    private String authorPlan;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested replies (only for top-level comments).
     */
    private List<CommentResponse> replies;
}

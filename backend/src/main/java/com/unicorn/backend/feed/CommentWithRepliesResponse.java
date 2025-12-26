package com.unicorn.backend.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for returning hierarchical comment data.
 * Includes author info and nested replies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentWithRepliesResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    // Author info
    private UUID authorId;
    private String authorName;
    private String authorUsername;
    private String authorAvatarUrl;
    private String authorPlan;

    // Nested replies (only for top-level comments)
    private List<CommentWithRepliesResponse> replies;
    private Integer replyCount;
}

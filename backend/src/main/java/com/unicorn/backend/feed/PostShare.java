package com.unicorn.backend.feed;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity tracking shares to prevent abuse (one share per user per post).
 * Similar to PostLike, ensures users can only count once toward share score.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_shares", uniqueConstraints = @UniqueConstraint(columnNames = { "post_id", "user_id" }), indexes = {
        @Index(name = "idx_share_post", columnList = "post_id"),
        @Index(name = "idx_share_user", columnList = "user_id")
})
public class PostShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

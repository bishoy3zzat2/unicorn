package com.unicorn.backend.feed;

import com.unicorn.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for mobile app feed operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    // ==================== Feed Retrieval ====================

    /**
     * Get the main feed (ranked) - offset-based pagination.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = feedService.getFeed(pageable);

        UUID currentUserId = currentUser != null ? currentUser.getId() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("content", posts.getContent().stream()
                .map(p -> feedService.toPostResponse(p, currentUserId))
                .collect(Collectors.toList()));
        response.put("totalElements", posts.getTotalElements());
        response.put("totalPages", posts.getTotalPages());
        response.put("currentPage", posts.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Get feed using cursor-based pagination (optimized for infinite scroll).
     * More efficient than offset pagination for large datasets.
     */
    @GetMapping("/cursor")
    public ResponseEntity<Map<String, Object>> getFeedWithCursor(
            @RequestParam(required = false) Double cursorScore,
            @RequestParam(required = false) UUID cursorId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User currentUser) {

        java.util.List<Post> posts = feedService.getFeedWithCursor(cursorScore, cursorId, limit);
        UUID currentUserId = currentUser != null ? currentUser.getId() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("content", posts.stream()
                .map(p -> feedService.toPostResponse(p, currentUserId))
                .collect(Collectors.toList()));

        // Include cursor for next page
        if (!posts.isEmpty()) {
            Post lastPost = posts.get(posts.size() - 1);
            response.put("nextCursorScore", lastPost.getRankingScore());
            response.put("nextCursorId", lastPost.getId());
        }
        response.put("hasMore", posts.size() == limit);

        return ResponseEntity.ok(response);
    }

    /**
     * Get discover feed (excludes own posts).
     */
    @GetMapping("/discover")
    public ResponseEntity<Map<String, Object>> getDiscoverFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = currentUser != null
                ? feedService.getDiscoverFeed(currentUser.getId(), pageable)
                : feedService.getFeed(pageable);

        UUID currentUserId = currentUser != null ? currentUser.getId() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("content", posts.getContent().stream()
                .map(p -> feedService.toPostResponse(p, currentUserId))
                .collect(Collectors.toList()));
        response.put("totalElements", posts.getTotalElements());
        response.put("totalPages", posts.getTotalPages());
        response.put("currentPage", posts.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Get posts by a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPosts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = feedService.getPostsByAuthor(userId, pageable);

        UUID currentUserId = currentUser != null ? currentUser.getId() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("content", posts.getContent().stream()
                .map(p -> feedService.toPostResponse(p, currentUserId))
                .collect(Collectors.toList()));
        response.put("totalElements", posts.getTotalElements());
        response.put("totalPages", posts.getTotalPages());
        response.put("currentPage", posts.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single post.
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User currentUser) {

        Post post = feedService.getPostsForAdmin(null, null, PageRequest.of(0, 1))
                .getContent().stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Post not found"));

        UUID currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(feedService.toPostResponse(post, currentUserId));
    }

    // ==================== Post CRUD ====================

    /**
     * Create a new post.
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Post post = feedService.createPost(currentUser.getId(), request);
        return ResponseEntity.ok(feedService.toPostResponse(post, currentUser.getId()));
    }

    /**
     * Update a post.
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Post post = feedService.updatePost(postId, currentUser.getId(), request);
        return ResponseEntity.ok(feedService.toPostResponse(post, currentUser.getId()));
    }

    /**
     * Delete own post.
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        feedService.deletePost(postId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    // ==================== Engagement ====================

    /**
     * Like a post.
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, String>> likePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        feedService.likePost(postId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Post liked"));
    }

    /**
     * Unlike a post.
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Map<String, String>> unlikePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        feedService.unlikePost(postId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Post unliked"));
    }

    /**
     * Share a post (get deep link).
     */
    @PostMapping("/{postId}/share")
    public ResponseEntity<Map<String, String>> sharePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        String deepLink = feedService.sharePost(postId, currentUser.getId());
        return ResponseEntity.ok(Map.of("deepLink", deepLink));
    }

    // ==================== Comments ====================

    /**
     * Get comments for a post.
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = feedService.getComments(postId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", comments.getContent().stream()
                .map(feedService::toCommentResponse)
                .collect(Collectors.toList()));
        response.put("totalElements", comments.getTotalElements());
        response.put("totalPages", comments.getTotalPages());
        response.put("currentPage", comments.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Add a comment to a post.
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Comment comment = feedService.addComment(postId, currentUser.getId(), request);
        return ResponseEntity.ok(feedService.toCommentResponse(comment));
    }

    /**
     * Delete a comment.
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        feedService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Comment deleted"));
    }

    /**
     * Check if media can still be edited for a post.
     */
    @GetMapping("/{postId}/can-edit-media")
    public ResponseEntity<Map<String, Object>> canEditMedia(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User currentUser) {

        Post post = feedService.getPostsForAdmin(null, null, PageRequest.of(0, 1))
                .getContent().stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("canEditMedia", feedService.canEditMedia(post));
        return ResponseEntity.ok(response);
    }
}

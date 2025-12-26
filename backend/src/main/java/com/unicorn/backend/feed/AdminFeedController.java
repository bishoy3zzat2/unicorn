package com.unicorn.backend.feed;

import com.unicorn.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for admin dashboard feed management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/feed")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminFeedController {

    private final FeedService feedService;

    // ==================== Feed Retrieval ====================

    /**
     * Get all posts with optional filtering (for dashboard table).
     * Default sorting: featured posts first, then by ranking score (same as user
     * feed).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "rankingScore") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Default sort: featured first, then by ranking score (matches feed algorithm)
        Sort sort;
        if (sortBy.equals("rankingScore")) {
            // Featured posts always on top, then sort by ranking score
            sort = Sort.by(
                    Sort.Order.desc("isFeatured"),
                    Sort.Order.desc("rankingScore"),
                    Sort.Order.desc("createdAt"));
        } else {
            sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        PostStatus postStatus = null;
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            try {
                postStatus = PostStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
            }
        }

        Page<Post> posts = feedService.getPostsForAdmin(postStatus, search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", posts.getContent().stream()
                .map(p -> feedService.toPostResponse(p, null))
                .collect(Collectors.toList()));
        response.put("totalElements", posts.getTotalElements());
        response.put("totalPages", posts.getTotalPages());
        response.put("currentPage", posts.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Get feed statistics for KPI cards.
     */
    @GetMapping("/stats")
    public ResponseEntity<FeedStatsResponse> getFeedStats() {
        return ResponseEntity.ok(feedService.getFeedStats());
    }

    /**
     * Get a single post by ID.
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID postId) {
        Page<Post> posts = feedService.getPostsForAdmin(null, null,
                PageRequest.of(0, Integer.MAX_VALUE));

        Post post = posts.getContent().stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        return ResponseEntity.ok(feedService.toPostResponse(post, null));
    }

    // ==================== Moderation Actions ====================

    /**
     * Hide a post.
     */
    @PutMapping("/{postId}/hide")
    public ResponseEntity<Map<String, String>> hidePost(
            @PathVariable UUID postId,
            @RequestBody(required = false) AdminPostActionRequest request,
            @AuthenticationPrincipal User admin) {

        String reason = request != null ? request.getReason() : null;
        feedService.hidePost(postId, admin.getId(), reason);
        return ResponseEntity.ok(Map.of("message", "Post hidden successfully"));
    }

    /**
     * Restore a hidden post.
     */
    @PutMapping("/{postId}/restore")
    public ResponseEntity<Map<String, String>> restorePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User admin) {

        feedService.restorePost(postId, admin.getId());
        return ResponseEntity.ok(Map.of("message", "Post restored successfully"));
    }

    /**
     * Delete a post (soft delete).
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable UUID postId,
            @RequestBody(required = false) AdminPostActionRequest request,
            @AuthenticationPrincipal User admin) {

        String reason = request != null ? request.getReason() : null;
        feedService.adminDeletePost(postId, admin.getId(), reason);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    /**
     * Feature a post (pin to top) with optional duration.
     * 
     * @param postId        The post to feature
     * @param durationHours Optional duration in hours (null = indefinite)
     */
    @PutMapping("/{postId}/feature")
    public ResponseEntity<Map<String, String>> featurePost(
            @PathVariable UUID postId,
            @RequestParam(required = false) Integer durationHours,
            @AuthenticationPrincipal User admin) {

        feedService.featurePost(postId, admin.getId(), durationHours);

        String message = durationHours != null
                ? "Post featured for " + durationHours + " hours"
                : "Post featured indefinitely";
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * Unfeature a post.
     */
    @PutMapping("/{postId}/unfeature")
    public ResponseEntity<Map<String, String>> unfeaturePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User admin) {

        feedService.unfeaturePost(postId, admin.getId());
        return ResponseEntity.ok(Map.of("message", "Post unfeatured successfully"));
    }

    /**
     * Manually trigger score recalculation for all posts.
     */
    @PostMapping("/recalculate-scores")
    public ResponseEntity<Map<String, String>> recalculateScores(@AuthenticationPrincipal User admin) {
        log.info("Admin {} triggered manual score recalculation", admin.getId());
        feedService.recalculateAllScores();
        return ResponseEntity.ok(Map.of("message", "Score recalculation triggered"));
    }

    // ==================== Post Engagement Details ====================

    /**
     * Get paginated list of users who liked a post.
     */
    @GetMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> getPostLikes(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var likes = feedService.getPostLikes(postId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", likes.getContent());
        response.put("totalElements", likes.getTotalElements());
        response.put("totalPages", likes.getTotalPages());
        response.put("currentPage", likes.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Get paginated hierarchical comments for a post.
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getPostComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var comments = feedService.getPostCommentsHierarchical(postId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", comments.getContent());
        response.put("totalElements", comments.getTotalElements());
        response.put("totalPages", comments.getTotalPages());
        response.put("currentPage", comments.getNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * Get paginated list of users who shared a post.
     */
    @GetMapping("/{postId}/shares")
    public ResponseEntity<Map<String, Object>> getPostShares(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var shares = feedService.getPostShares(postId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", shares.getContent());
        response.put("totalElements", shares.getTotalElements());
        response.put("totalPages", shares.getTotalPages());
        response.put("currentPage", shares.getNumber());

        return ResponseEntity.ok(response);
    }
}

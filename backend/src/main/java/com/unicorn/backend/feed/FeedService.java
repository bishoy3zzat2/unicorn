package com.unicorn.backend.feed;

import com.unicorn.backend.appconfig.AppConfigService;
import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.startup.StartupMember;
import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.subscription.Subscription;
import com.unicorn.backend.subscription.SubscriptionPlan;
import com.unicorn.backend.subscription.SubscriptionService;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for feed operations and ranking algorithm implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final PostShareRepository shareRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final StartupRepository startupRepository;
    private final AppConfigService appConfigService;

    // ==================== Algorithm Config Keys ====================
    private static final String CONFIG_DECAY_GRAVITY = "feed.decay.gravity";
    private static final String CONFIG_LIKE_POINTS = "feed.like.points";
    private static final String CONFIG_COMMENT_POINTS = "feed.comment.points";
    private static final String CONFIG_SHARE_POINTS = "feed.share.points";
    private static final String CONFIG_EDIT_PENALTY = "feed.edit.penalty";
    private static final String CONFIG_BOOST_FREE = "feed.boost.free";
    private static final String CONFIG_BOOST_PRO = "feed.boost.pro";
    private static final String CONFIG_BOOST_ELITE = "feed.boost.elite";
    private static final String CONFIG_MEDIA_EDIT_HOURS = "feed.media.edit.hours";
    private static final String CONFIG_BASE_FRESHNESS = "feed.base.freshness";
    private static final int BATCH_RECALC_SIZE = 500;

    // ==================== Post Creation ====================

    /**
     * Create a new post.
     */
    @Transactional
    public Post createPost(UUID authorId, CreatePostRequest request) {
        if (!userRepository.existsById(authorId)) {
            throw new RuntimeException("User not found: " + authorId);
        }

        // Validate content length
        int maxPostLength = appConfigService.getIntValue("max_post_length", 2000);
        if (request.getContent() != null && request.getContent().length() > maxPostLength) {
            throw new IllegalArgumentException("Post content must not exceed " + maxPostLength + " characters");
        }

        // Get subscription multiplier at creation time
        double subscriptionMultiplier = getSubscriptionMultiplier(authorId);

        // Resolve contextual title from startup membership (if provided)
        String contextualTitle = null;
        UUID contextualStartupId = request.getContextualStartupId();

        if (contextualStartupId != null) {
            contextualTitle = resolveContextualTitle(authorId, contextualStartupId);
        }

        Post post = Post.builder()
                .authorId(authorId)
                .content(request.getContent())
                .mediaUrl(request.getMediaUrl())
                .contextualTitle(contextualTitle)
                .contextualStartupId(contextualStartupId)
                .subscriptionMultiplier(subscriptionMultiplier)
                .status(PostStatus.ACTIVE)
                .build();

        post = postRepository.save(post);

        // Calculate initial score
        recalculatePostScore(post);

        log.info("Created post {} by user {} with multiplier {}", post.getId(), authorId, subscriptionMultiplier);
        return post;
    }

    /**
     * Resolve contextual title based on user's role in the startup.
     * Validates that user actually belongs to the startup.
     * 
     * @return Generated title like "Founder at StartupName" or "CTO at StartupName"
     * @throws RuntimeException if user is not a member of the startup
     */
    private String resolveContextualTitle(UUID userId, UUID startupId) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new RuntimeException("Startup not found: " + startupId));

        // Check if user is the owner
        if (startup.getOwner() != null && startup.getOwner().getId().equals(userId)) {
            String ownerRole = startup.getOwnerRole() != null
                    ? startup.getOwnerRole().name().replace("_", " ")
                    : "Founder";
            return ownerRole + " at " + startup.getName();
        }

        // Check if user is a team member
        for (StartupMember member : startup.getMembers()) {
            if (member.getUser() != null && member.getUser().getId().equals(userId) && member.isActive()) {
                String role = member.getRole() != null ? member.getRole() : "Team Member";
                return role + " at " + startup.getName();
            }
        }

        // User is not associated with this startup
        throw new RuntimeException("You are not a member of this startup");
    }

    /**
     * Get subscription multiplier for a user based on their current plan.
     */
    private double getSubscriptionMultiplier(UUID userId) {
        Subscription subscription = subscriptionService.getActiveSubscription(userId);
        if (subscription == null) {
            return getConfigDouble(CONFIG_BOOST_FREE, 1.0);
        }

        return switch (subscription.getPlanType()) {
            case PRO -> getConfigDouble(CONFIG_BOOST_PRO, 1.5);
            case ELITE -> getConfigDouble(CONFIG_BOOST_ELITE, 2.0);
            default -> getConfigDouble(CONFIG_BOOST_FREE, 1.0);
        };
    }

    // ==================== Post Update ====================

    /**
     * Update a post with media edit time restrictions.
     */
    @Transactional
    public Post updatePost(UUID postId, UUID userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // Verify ownership
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorized to edit this post");
        }

        // Validate content length if changing
        if (request.getContent() != null) {
            int maxPostLength = appConfigService.getIntValue("max_post_length", 2000);
            if (request.getContent().length() > maxPostLength) {
                throw new IllegalArgumentException("Post content must not exceed " + maxPostLength + " characters");
            }
        }

        // Check if post is active
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new RuntimeException("Cannot edit a hidden or deleted post");
        }

        boolean contentChanged = false;
        boolean mediaChanged = false;

        // Update content (always allowed)
        if (request.getContent() != null && !request.getContent().equals(post.getContent())) {
            post.setContent(request.getContent());
            contentChanged = true;
        }

        // Update media (only within 2 hours if already has media)
        if (request.getMediaUrl() != null) {
            if (canEditMedia(post)) {
                if (!Objects.equals(request.getMediaUrl(), post.getMediaUrl())) {
                    post.setMediaUrl(request.getMediaUrl());
                    post.markMediaEdited();
                    mediaChanged = true;
                }
            } else {
                throw new RuntimeException("Media can only be edited within 2 hours of post creation");
            }
        }

        // Update contextual title
        if (request.getContextualTitle() != null) {
            post.setContextualTitle(request.getContextualTitle());
            post.setContextualStartupId(request.getContextualStartupId());
        }

        // Mark as edited and apply penalty
        if (contentChanged || mediaChanged) {
            post.markAsEdited();
            recalculatePostScore(post);
        }

        log.info("Updated post {} by user {}", postId, userId);
        return postRepository.save(post);
    }

    /**
     * Check if media can still be edited (within 2 hours of creation).
     */
    public boolean canEditMedia(Post post) {
        if (post.getMediaUrl() == null) {
            return true; // No media yet, can add
        }

        int allowedHours = appConfigService.getIntValue(CONFIG_MEDIA_EDIT_HOURS, 2);
        LocalDateTime deadline = post.getCreatedAt().plusHours(allowedHours);
        return LocalDateTime.now().isBefore(deadline);
    }

    // ==================== Ranking Algorithm ====================

    /**
     * Calculate and update the ranking score for a post.
     * Formula: ((BaseFreshness + EngagementScore) * SubscriptionMultiplier) / (Age
     * + 1)^G - EditPenalty
     * 
     * BaseFreshness: New posts start with 10 points so they're visible in feed.
     * EngagementScore: Points from likes, comments, shares.
     * Time Decay: Score decreases as post ages.
     */
    @Transactional
    public void recalculatePostScore(Post post) {
        // Get algorithm config
        double likePoints = getConfigDouble(CONFIG_LIKE_POINTS, 1.0);
        double commentPoints = getConfigDouble(CONFIG_COMMENT_POINTS, 3.0);
        double sharePoints = getConfigDouble(CONFIG_SHARE_POINTS, 5.0);
        double gravity = getConfigDouble(CONFIG_DECAY_GRAVITY, 1.5);
        double editPenalty = getConfigDouble(CONFIG_EDIT_PENALTY, 0.1);

        // Base freshness score - new posts start with visibility (configurable from
        // dashboard)
        double baseFreshness = getConfigDouble(CONFIG_BASE_FRESHNESS, 10.0);

        // Calculate engagement score
        int likes = post.getLikeCount() != null ? post.getLikeCount() : 0;
        int comments = post.getCommentCount() != null ? post.getCommentCount() : 0;
        int shares = post.getShareCount() != null ? post.getShareCount() : 0;

        double engagementScore = (likes * likePoints) + (comments * commentPoints) + (shares * sharePoints);

        // Total base score = freshness + engagement
        double baseScore = baseFreshness + engagementScore;

        // Apply subscription boost (frozen at creation time)
        double multiplier = post.getSubscriptionMultiplier() != null ? post.getSubscriptionMultiplier() : 1.0;
        double boostedScore = baseScore * multiplier;

        // Apply time decay
        LocalDateTime createdAt = post.getCreatedAt();
        long hoursAge = 0;
        if (createdAt != null) {
            hoursAge = Duration.between(createdAt, LocalDateTime.now()).toHours();
        }
        double decayedScore = boostedScore / Math.pow(hoursAge + 1, gravity);

        // Apply edit penalty
        int editCount = post.getEditCount() != null ? post.getEditCount() : 0;
        double penalty = Boolean.TRUE.equals(post.getIsEdited()) ? (editCount * editPenalty) : 0;

        double finalScore = Math.max(0, decayedScore - penalty);

        post.setRankingScore(finalScore);
        post.setScoreCalculatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    /**
     * Batch recalculate scores for active posts (scheduled job).
     * Processes in batches of 500 to prevent memory issues at scale.
     */
    @Scheduled(fixedDelayString = "${feed.score.recalc.interval:900000}") // Default 15 minutes
    @Transactional
    public void recalculateAllScores() {
        log.info("Starting batch score recalculation...");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        Pageable batchPage = PageRequest.of(0, BATCH_RECALC_SIZE);
        List<Post> postsToUpdate = postRepository.findPostsNeedingScoreRecalculation(threshold, batchPage);

        int count = 0;
        for (Post post : postsToUpdate) {
            try {
                recalculatePostScore(post);
                count++;
            } catch (Exception e) {
                log.error("Failed to recalculate score for post {}: {}", post.getId(), e.getMessage());
            }
        }

        log.info("Batch score recalculation complete. Updated {} of {} stale posts.", count, postsToUpdate.size());
    }

    // ==================== Feed Retrieval ====================

    /**
     * Get ranked feed for mobile app.
     */
    public Page<Post> getFeed(Pageable pageable) {
        return postRepository.findActiveFeedPosts(pageable);
    }

    /**
     * Get ranked feed excluding current user's posts.
     */
    public Page<Post> getDiscoverFeed(UUID excludeUserId, Pageable pageable) {
        return postRepository.findActiveFeedPostsExcludingAuthor(excludeUserId, pageable);
    }

    /**
     * Get posts by a specific author.
     */
    public Page<Post> getPostsByAuthor(UUID authorId, Pageable pageable) {
        return postRepository.findByAuthorIdAndStatus(authorId, PostStatus.ACTIVE, pageable);
    }

    // ==================== Cursor-Based Pagination (Mobile) ====================

    /**
     * Get feed using cursor-based pagination (more efficient for infinite scroll).
     * 
     * @param cursorScore Last seen post's rankingScore
     * @param cursorId    Last seen post's ID
     * @param limit       Number of posts to fetch
     * @return List of posts after the cursor
     */
    public List<Post> getFeedWithCursor(Double cursorScore, UUID cursorId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        // If no cursor, get first page including featured
        if (cursorScore == null || cursorId == null) {
            List<Post> featured = postRepository.findFeaturedPosts(PageRequest.of(0, 5));
            List<Post> regular = postRepository.findFeedPostsAfterCursor(Double.MAX_VALUE, UUID.randomUUID(), pageable);
            featured.addAll(regular);
            return featured;
        }

        return postRepository.findFeedPostsAfterCursor(cursorScore, cursorId, pageable);
    }

    // ==================== Engagement ====================

    /**
     * Like a post.
     */
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new RuntimeException("Already liked this post");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        PostLike like = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();
        likeRepository.save(like);

        post.incrementLikes();
        recalculatePostScore(post);

        log.info("User {} liked post {}", userId, postId);
    }

    /**
     * Unlike a post.
     */
    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new RuntimeException("Post not liked");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        likeRepository.deleteByPostIdAndUserId(postId, userId);
        post.decrementLikes();
        recalculatePostScore(post);

        log.info("User {} unliked post {}", userId, postId);
    }

    /**
     * Check if user has liked a post.
     */
    public boolean hasUserLikedPost(UUID postId, UUID userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    /**
     * Record a share and increment counter (only once per user per post).
     * Prevents share spamming abuse.
     */
    @Transactional
    public String sharePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // Generate deep link
        String deepLink = "unicorn://post/" + postId;

        // Check if user already shared this post (anti-abuse)
        if (shareRepository.existsByPostIdAndUserId(postId, userId)) {
            log.debug("User {} already shared post {}, skipping score increment", userId, postId);
            return deepLink;
        }

        // First share by this user - record and count it
        PostShare share = PostShare.builder()
                .postId(postId)
                .userId(userId)
                .build();
        shareRepository.save(share);

        post.incrementShares();
        recalculatePostScore(post);

        log.info("User {} shared post {} (first time)", userId, postId);
        return deepLink;
    }

    // ==================== Comments ====================

    /**
     * Add a comment to a post.
     */
    @Transactional
    public Comment addComment(UUID postId, UUID authorId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // Validate content length
        int maxCommentLength = appConfigService.getIntValue("max_comment_length", 1000);
        if (request.getContent() != null && request.getContent().length() > maxCommentLength) {
            throw new IllegalArgumentException("Comment content must not exceed " + maxCommentLength + " characters");
        }

        // If it's a reply, verify parent exists
        if (request.getParentId() != null) {
            if (!commentRepository.existsById(request.getParentId())) {
                throw new RuntimeException("Parent comment not found: " + request.getParentId());
            }
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .parentId(request.getParentId())
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);

        // Update post comment count
        post.incrementComments();
        recalculatePostScore(post);

        log.info("User {} commented on post {}", authorId, postId);
        return comment;
    }

    /**
     * Delete a comment (cascades to replies).
     */
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        // Verify ownership
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        // Soft-delete replies
        commentRepository.softDeleteReplies(commentId);

        // Soft-delete the comment
        comment.setIsDeleted(true);
        commentRepository.save(comment);

        // Update post comment count
        Post post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.decrementComments();
            recalculatePostScore(post);
        }

        log.info("User {} deleted comment {}", userId, commentId);
    }

    /**
     * Get comments for a post.
     */
    public Page<Comment> getComments(UUID postId, Pageable pageable) {
        return commentRepository.findTopLevelCommentsByPostId(postId, pageable);
    }

    /**
     * Get replies to a comment.
     */
    public List<Comment> getReplies(UUID commentId) {
        return commentRepository.findRepliesByParentId(commentId);
    }

    // ==================== Post Status Changes ====================

    /**
     * Delete own post.
     */
    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // Verify ownership
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);

        log.info("User {} deleted post {}", userId, postId);
    }

    // ==================== Admin Operations ====================

    /**
     * Get all posts for admin dashboard with filtering.
     */
    public Page<Post> getPostsForAdmin(PostStatus status, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return postRepository.searchByContent(search, pageable);
        }
        if (status != null) {
            return postRepository.findByStatus(status, pageable);
        }
        return postRepository.findAll(pageable);
    }

    /**
     * Get feed statistics for dashboard.
     */
    public FeedStatsResponse getFeedStats() {
        long totalPosts = postRepository.count();
        long activePosts = postRepository.countByStatus(PostStatus.ACTIVE);
        long hiddenPosts = postRepository.countByStatus(PostStatus.HIDDEN);
        long deletedPosts = postRepository.countByStatus(PostStatus.DELETED);
        long featuredPosts = postRepository.countByIsFeaturedTrue();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayPosts = postRepository.countTodayPosts(startOfDay);

        // Calculate average engagement using optimized query
        Double avgEngagement = postRepository.getAverageEngagement();
        if (avgEngagement == null)
            avgEngagement = 0.0;

        return FeedStatsResponse.builder()
                .totalPosts(totalPosts)
                .activePosts(activePosts)
                .hiddenPosts(hiddenPosts)
                .deletedPosts(deletedPosts)
                .featuredPosts(featuredPosts)
                .todayPosts(todayPosts)
                .avgEngagement(avgEngagement)
                .build();
    }

    /**
     * Admin: Hide a post.
     */
    @Transactional
    public void hidePost(UUID postId, UUID adminId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        post.setStatus(PostStatus.HIDDEN);
        post.setModeratedBy(adminId);
        post.setModerationReason(reason);
        post.setModeratedAt(LocalDateTime.now());
        postRepository.save(post);

        log.info("Admin {} hid post {} for reason: {}", adminId, postId, reason);
    }

    /**
     * Admin: Restore a hidden post.
     */
    @Transactional
    public void restorePost(UUID postId, UUID adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        if (post.getStatus() != PostStatus.HIDDEN) {
            throw new RuntimeException("Post is not hidden");
        }

        post.setStatus(PostStatus.ACTIVE);
        postRepository.save(post);

        log.info("Admin {} restored post {}", adminId, postId);
    }

    /**
     * Admin: Soft-delete a post.
     */
    @Transactional
    public void adminDeletePost(UUID postId, UUID adminId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        post.setStatus(PostStatus.DELETED);
        post.setModeratedBy(adminId);
        post.setModerationReason(reason);
        post.setModeratedAt(LocalDateTime.now());
        postRepository.save(post);

        log.info("Admin {} deleted post {} for reason: {}", adminId, postId, reason);
    }

    /**
     * Admin: Feature a post (pin to top) with optional duration.
     * 
     * @param postId        The post to feature
     * @param adminId       The admin performing the action
     * @param durationHours Optional duration in hours (null = indefinite)
     */
    @Transactional
    public void featurePost(UUID postId, UUID adminId, Integer durationHours) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        post.feature(adminId, durationHours);
        postRepository.save(post);

        if (durationHours != null) {
            log.info("Admin {} featured post {} for {} hours", adminId, postId, durationHours);
        } else {
            log.info("Admin {} featured post {} indefinitely", adminId, postId);
        }
    }

    /**
     * Admin: Unfeature a post.
     */
    @Transactional
    public void unfeaturePost(UUID postId, UUID adminId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        post.unfeature();
        postRepository.save(post);

        log.info("Admin {} unfeatured post {}", adminId, postId);
    }

    // ==================== Response Mapping ====================

    /**
     * Convert Post entity to PostResponse DTO with author info.
     */
    public PostResponse toPostResponse(Post post, UUID currentUserId) {
        User author = userRepository.findById(post.getAuthorId()).orElse(null);

        PostResponse.PostResponseBuilder builder = PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .contextualTitle(post.getContextualTitle())
                .authorId(post.getAuthorId())
                .status(post.getStatus().name())
                .isFeatured(post.isCurrentlyFeatured()) // Use computed method to check expiry
                .featuredAt(post.getFeaturedAt())
                .featuredUntil(post.getFeaturedUntil())
                .featuredBy(post.getFeaturedBy())
                .isEdited(post.getIsEdited())
                .editCount(post.getEditCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .rankingScore(post.getRankingScore())
                .subscriptionMultiplier(post.getSubscriptionMultiplier())
                .scoreCalculatedAt(post.getScoreCalculatedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .lastEditedAt(post.getLastEditedAt())
                .moderatedBy(post.getModeratedBy())
                .moderationReason(post.getModerationReason())
                .moderatedAt(post.getModeratedAt());

        if (author != null) {
            builder.authorName(author.getDisplayName() != null ? author.getDisplayName()
                    : (author.getFirstName() + " " + author.getLastName()))
                    .authorUsername(author.getUsername())
                    .authorAvatarUrl(author.getAvatarUrl())
                    .authorRole(author.getRole());

            // Get author's current plan
            Subscription subscription = subscriptionService.getActiveSubscription(author.getId());
            builder.authorPlan(subscription != null ? subscription.getPlanType().name() : "FREE");

            // Check if user is a verified investor
            builder.authorIsVerified(author.getInvestorProfile() != null &&
                    author.getInvestorProfile().getIsVerified());
        }

        // Check if current user liked this post
        if (currentUserId != null) {
            builder.isLikedByCurrentUser(hasUserLikedPost(post.getId(), currentUserId));
        }

        return builder.build();
    }

    /**
     * Convert Comment entity to CommentResponse DTO.
     */
    public CommentResponse toCommentResponse(Comment comment) {
        User author = userRepository.findById(comment.getAuthorId()).orElse(null);

        CommentResponse.CommentResponseBuilder builder = CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt());

        if (author != null) {
            builder.authorName(author.getDisplayName() != null ? author.getDisplayName()
                    : (author.getFirstName() + " " + author.getLastName()))
                    .authorUsername(author.getUsername())
                    .authorAvatarUrl(author.getAvatarUrl())
                    .authorRole(author.getRole());

            Subscription subscription = subscriptionService.getActiveSubscription(author.getId());
            builder.authorPlan(subscription != null ? subscription.getPlanType().name() : "FREE");
        }

        // Get replies if this is a top-level comment
        if (comment.getParentId() == null) {
            List<Comment> replies = getReplies(comment.getId());
            builder.replies(replies.stream()
                    .map(this::toCommentResponse)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    // ==================== Helper Methods ====================

    private double getConfigDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(appConfigService.getValue(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== Post Engagement Details (Admin) ====================

    /**
     * Get paginated list of users who liked a post.
     */
    public Page<EngagementUserResponse> getPostLikes(UUID postId, Pageable pageable) {
        Page<PostLike> likes = likeRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

        return likes.map(like -> {
            User user = userRepository.findById(like.getUserId()).orElse(null);
            Subscription subscription = user != null ? subscriptionService.getActiveSubscription(user.getId()) : null;

            return EngagementUserResponse.builder()
                    .userId(like.getUserId())
                    .userName(user != null ? (user.getDisplayName() != null ? user.getDisplayName()
                            : user.getFirstName() + " " + user.getLastName()) : "Unknown User")
                    .userUsername(user != null ? user.getUsername() : null)
                    .userAvatarUrl(user != null ? user.getAvatarUrl() : null)
                    .userPlan(subscription != null ? subscription.getPlanType().name() : "FREE")
                    .engagedAt(like.getCreatedAt())
                    .build();
        });
    }

    /**
     * Get paginated list of users who shared a post.
     */
    public Page<EngagementUserResponse> getPostShares(UUID postId, Pageable pageable) {
        Page<PostShare> shares = shareRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

        return shares.map(share -> {
            User user = userRepository.findById(share.getUserId()).orElse(null);
            Subscription subscription = user != null ? subscriptionService.getActiveSubscription(user.getId()) : null;

            return EngagementUserResponse.builder()
                    .userId(share.getUserId())
                    .userName(user != null ? (user.getDisplayName() != null ? user.getDisplayName()
                            : user.getFirstName() + " " + user.getLastName()) : "Unknown User")
                    .userUsername(user != null ? user.getUsername() : null)
                    .userAvatarUrl(user != null ? user.getAvatarUrl() : null)
                    .userPlan(subscription != null ? subscription.getPlanType().name() : "FREE")
                    .engagedAt(share.getCreatedAt())
                    .build();
        });
    }

    /**
     * Get paginated hierarchical comments for a post.
     * Returns top-level comments with nested replies.
     */
    public Page<CommentWithRepliesResponse> getPostCommentsHierarchical(UUID postId, Pageable pageable) {
        Page<Comment> topLevelComments = commentRepository.findTopLevelCommentsByPostId(postId, pageable);

        return topLevelComments.map(this::toCommentWithRepliesResponse);
    }

    /**
     * Get paginated replies for a comment.
     */
    public Page<CommentWithRepliesResponse> getCommentReplies(UUID commentId, Pageable pageable) {
        Page<Comment> replies = commentRepository.findRepliesByParentId(commentId, pageable);
        return replies.map(this::toCommentWithRepliesResponse);
    }

    /**
     * Convert Comment to CommentWithRepliesResponse with limited nested replies
     * (Preview).
     */
    private CommentWithRepliesResponse toCommentWithRepliesResponse(Comment comment) {
        User author = userRepository.findById(comment.getAuthorId()).orElse(null);
        Subscription subscription = author != null ? subscriptionService.getActiveSubscription(author.getId()) : null;

        // Count total replies
        long totalReplies = commentRepository.countByParentIdAndIsDeletedFalse(comment.getId());

        // Get initial preview of replies (e.g., first 5)
        // We only fetch replies if it's a top-level comment (parentId is null) to avoid
        // deep recursion if we ever support multi-level
        List<CommentWithRepliesResponse> repliesResponse = new ArrayList<>();
        if (comment.getParentId() == null && totalReplies > 0) {
            Pageable previewPage = PageRequest.of(0, 5); // Limit initial replies to 5
            Page<Comment> replies = commentRepository.findRepliesByParentId(comment.getId(), previewPage);
            repliesResponse = replies.stream()
                    .map(this::toCommentWithRepliesResponse)
                    .collect(Collectors.toList());
        }

        return CommentWithRepliesResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .isDeleted(comment.getIsDeleted())
                .authorId(comment.getAuthorId())
                .authorName(author != null ? (author.getDisplayName() != null ? author.getDisplayName()
                        : author.getFirstName() + " " + author.getLastName()) : "Unknown User")
                .authorUsername(author != null ? author.getUsername() : null)
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .authorPlan(subscription != null ? subscription.getPlanType().name() : "FREE")
                .replies(repliesResponse)
                .replyCount((int) totalReplies) // Cast simply for DTO, though long is better
                .build();
    }
}

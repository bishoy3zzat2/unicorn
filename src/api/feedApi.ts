/**
 * API service module for feed operations.
 */

import api from '../lib/axios';

// ==================== Types ====================

export interface PostAuthor {
    authorId: string;
    authorName: string;
    authorUsername: string;
    authorAvatarUrl?: string;
    authorIsVerified: boolean;
    authorRole: string;
    authorPlan: string;
}

export interface PostData extends PostAuthor {
    id: string;
    content: string;
    mediaUrl?: string;
    contextualTitle?: string;
    status: 'ACTIVE' | 'HIDDEN' | 'DELETED';
    isFeatured: boolean;
    featuredAt?: string;
    featuredUntil?: string;
    featuredBy?: string;
    isEdited: boolean;
    editCount: number;
    likeCount: number;
    commentCount: number;
    shareCount: number;
    rankingScore: number;
    subscriptionMultiplier: number;
    scoreCalculatedAt?: string;
    createdAt: string;
    updatedAt?: string;
    lastEditedAt?: string;
    moderatedBy?: string;
    moderationReason?: string;
    moderatedAt?: string;
    isLikedByCurrentUser?: boolean;
}

export interface FeedStats {
    totalPosts: number;
    activePosts: number;
    hiddenPosts: number;
    deletedPosts: number;
    featuredPosts: number;
    todayPosts: number;
    avgEngagement: number;
    totalLikes: number;
    totalComments: number;
    totalShares: number;
}

export interface PagedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
}

// ==================== Admin Feed API ====================

/**
 * Get all posts for admin dashboard with optional filtering.
 * Default sorting: featured first, then by ranking score (same as user feed).
 */
export async function getAdminFeedPosts(
    page: number = 0,
    size: number = 20,
    status?: string,
    search?: string,
    sortBy: string = 'rankingScore',
    sortDir: string = 'desc'
): Promise<PagedResponse<PostData>> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('sortDir', sortDir);
    if (status && status !== 'ALL') params.append('status', status);
    if (search) params.append('search', search);

    const response = await api.get(`/admin/feed?${params.toString()}`);
    return response.data;
}

/**
 * Get feed statistics for KPI cards.
 */
export async function getFeedStats(): Promise<FeedStats> {
    const response = await api.get('/admin/feed/stats');
    return response.data;
}

/**
 * Get a single post by ID.
 */
export async function getPostById(postId: string): Promise<PostData> {
    const response = await api.get(`/admin/feed/${postId}`);
    return response.data;
}

/**
 * Hide a post.
 */
export async function hidePost(postId: string, reason?: string): Promise<void> {
    await api.put(`/admin/feed/${postId}/hide`, { reason });
}

/**
 * Restore a hidden post.
 */
export async function restorePost(postId: string): Promise<void> {
    await api.put(`/admin/feed/${postId}/restore`);
}

/**
 * Delete a post (soft delete).
 */
export async function deletePost(postId: string, reason?: string): Promise<void> {
    await api.delete(`/admin/feed/${postId}`, { data: { reason } });
}

/**
 * Feature a post (pin to top) with optional duration.
 * @param durationHours Optional duration in hours (undefined = indefinite)
 */
export async function featurePost(postId: string, durationHours?: number): Promise<void> {
    const params = durationHours ? `?durationHours=${durationHours}` : '';
    await api.put(`/admin/feed/${postId}/feature${params}`);
}

/**
 * Unfeature a post.
 */
export async function unfeaturePost(postId: string): Promise<void> {
    await api.put(`/admin/feed/${postId}/unfeature`);
}

/**
 * Trigger manual score recalculation.
 */
export async function recalculateScores(): Promise<void> {
    await api.post('/admin/feed/recalculate-scores');
}

// ==================== Engagement Types ====================

export interface EngagementUser {
    userId: string;
    userName: string;
    userUsername?: string;
    userAvatarUrl?: string;
    userPlan: string;
    engagedAt: string;
}

export interface CommentWithReplies {
    id: string;
    content: string;
    createdAt: string;
    isDeleted: boolean;
    authorId: string;
    authorName: string;
    authorUsername?: string;
    authorAvatarUrl?: string;
    authorPlan: string;
    replies: CommentWithReplies[];
    replyCount: number;
}

// ==================== Engagement API ====================

/**
 * Get paginated list of users who liked a post.
 */
export async function getPostLikes(
    postId: string,
    page: number = 0,
    size: number = 20
): Promise<PagedResponse<EngagementUser>> {
    const response = await api.get(`/admin/feed/${postId}/likes`, {
        params: { page, size }
    });
    return response.data;
}

/**
 * Get paginated hierarchical comments for a post.
 */
export async function getPostComments(
    postId: string,
    page: number = 0,
    size: number = 20
): Promise<PagedResponse<CommentWithReplies>> {
    const response = await api.get(`/admin/feed/${postId}/comments`, {
        params: { page, size }
    });
    return response.data;
}

/**
 * Get paginated list of users who shared a post.
 */
export async function getPostShares(
    postId: string,
    page: number = 0,
    size: number = 20
): Promise<PagedResponse<EngagementUser>> {
    const response = await api.get(`/admin/feed/${postId}/shares`, {
        params: { page, size }
    });
    return response.data;
}

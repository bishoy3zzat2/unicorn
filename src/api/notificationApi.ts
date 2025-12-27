import api from '../lib/axios';

// Types
export interface NotificationStats {
    total: number;
    unread: number;
    readCount: number;
    readRate: number;
    todayCount: number;
    topNotificationType: string;
    topTypeCount: number;
}

export interface NotificationData {
    id: string;
    type: string;
    title: string;
    message: string;
    data: Record<string, unknown>;
    read: boolean;
    createdAt: string;
    actorId: string | null;
    actorName: string | null;
    actorAvatarUrl: string | null;
    // Broadcast info
    isBroadcast: boolean;
    targetAudience: string | null;
    // Recipient info (for non-broadcast)
    recipientId: string | null;
    recipientEmail: string | null;
    recipientName: string | null;
}

export interface NotificationPage {
    content: NotificationData[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
}

export type TargetAudience = 'ALL_USERS' | 'INVESTORS_ONLY' | 'STARTUP_OWNERS_ONLY' | 'SPECIFIC_USER';

export interface SendAnnouncementRequest {
    title: string;
    message: string;
    targetAudience: TargetAudience;
    specificUserId?: string;
    specificUserEmail?: string;
    channels: ('IN_APP' | 'PUSH' | 'EMAIL')[];
}

// API Functions

/**
 * Get notification statistics.
 */
export async function getNotificationStats(): Promise<NotificationStats> {
    const response = await api.get('/admin/notifications/stats');
    return response.data;
}

/**
 * Get all notifications (paginated).
 */
export async function getAllNotifications(
    page: number = 0,
    size: number = 20,
    type?: string,
    read?: boolean
): Promise<NotificationPage> {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (type) params.append('type', type);
    if (read !== undefined) params.append('read', read.toString());

    const response = await api.get(`/admin/notifications?${params.toString()}`);
    return response.data;
}

/**
 * Get notifications for a specific user.
 */
export async function getUserNotifications(
    userId: string,
    page: number = 0,
    size: number = 20
): Promise<NotificationPage> {
    const response = await api.get(`/admin/notifications/user/${userId}?page=${page}&size=${size}`);
    return response.data;
}

/**
 * Send a system announcement.
 */
export async function sendAnnouncement(request: SendAnnouncementRequest): Promise<{ success: boolean; message: string; sentCount: number }> {
    const response = await api.post('/admin/notifications/announce', request);
    return response.data;
}

/**
 * Delete old read notifications.
 */
export async function cleanupOldNotifications(daysOld: number = 30): Promise<{ success: boolean; deletedCount: number }> {
    const response = await api.delete(`/admin/notifications/cleanup?daysOld=${daysOld}`);
    return response.data;
}

/**
 * Delete a specific notification.
 */
export async function deleteNotification(notificationId: string): Promise<{ success: boolean }> {
    const response = await api.delete(`/admin/notifications/${notificationId}`);
    return response.data;
}

/**
 * Get available notification types.
 */
export async function getNotificationTypes(): Promise<string[]> {
    const response = await api.get('/admin/notifications/types');
    return response.data;
}

// Notification type labels for display
export const NOTIFICATION_TYPE_LABELS: Record<string, string> = {
    NUDGE_RECEIVED: 'Nudge Received',
    REPORT_RESOLVED: 'Report Resolved',
    REPORT_ACTION_TAKEN: 'Report Action',
    POST_LIKED: 'Post Liked',
    POST_COMMENTED: 'Post Comment',
    COMMENT_LIKED: 'Comment Liked',
    COMMENT_REPLIED: 'Comment Reply',
    STARTUP_FOLLOWED: 'Startup Followed',
    STARTUP_TEAM_INVITE: 'Team Invite',
    STARTUP_TEAM_JOINED: 'Team Joined',
    INVESTOR_VERIFICATION_APPROVED: 'Investor Approved',
    INVESTOR_VERIFICATION_REJECTED: 'Investor Rejected',
    INVESTOR_PAYMENT_REQUIRED: 'Payment Required',
    ACCOUNT_WARNING: 'Account Warning',
    ACCOUNT_SUSPENDED: 'Account Suspended',
    ACCOUNT_UNSUSPENDED: 'Account Restored',
    NEW_CHAT_MESSAGE: 'New Message',
    SYSTEM_ANNOUNCEMENT: 'Announcement',
};

// Get icon color by notification type category
export function getNotificationTypeColor(type: string): string {
    if (type.startsWith('NUDGE')) return 'text-purple-600';
    if (type.startsWith('REPORT')) return 'text-orange-600';
    if (type.startsWith('POST') || type.startsWith('COMMENT')) return 'text-blue-600';
    if (type.startsWith('STARTUP')) return 'text-green-600';
    if (type.startsWith('INVESTOR')) return 'text-indigo-600';
    if (type.startsWith('ACCOUNT')) return 'text-red-600';
    if (type.startsWith('NEW_CHAT')) return 'text-cyan-600';
    if (type.startsWith('SYSTEM')) return 'text-yellow-600';
    return 'text-gray-600';
}

// User search result interface
export interface UserSearchResult {
    id: string;
    email: string;
    name: string;
    avatarUrl: string;
}

/**
 * Search users for announcement autocomplete.
 */
export async function searchUsersForAnnouncement(query: string): Promise<UserSearchResult[]> {
    if (!query || query.length < 2) return [];
    const response = await api.get(`/admin/notifications/users/search?q=${encodeURIComponent(query)}`);
    return response.data;
}

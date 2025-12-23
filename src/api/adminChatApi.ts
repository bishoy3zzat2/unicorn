import { api } from '../lib/api';

export interface ChatData {
    id: string;
    investorId: string;
    investorName: string;
    startupId: string;
    startupName: string;
    status: 'ACTIVE' | 'BLOCKED' | 'DELETED';
    createdAt: string;
    lastMessageAt: string;
    unreadCount: number;
}

export interface MessageData {
    id: string;
    chatId: string;
    senderId: string;
    senderName: string;
    senderAvatarUrl: string | null;
    content: string;
    isRead: boolean;
    isDeleted: boolean;
    createdAt: string;
    readAt: string | null;
}

export interface ChatReportData {
    id: string;
    messageId: string;
    messageContent: string;
    reporterId: string;
    reporterName: string;
    reportedUserId: string;
    reportedUserName: string;
    reason: string;
    status: 'PENDING' | 'REVIEWED' | 'DISMISSED' | 'ACTION_TAKEN';
    createdAt: string;
    reviewedAt: string | null;
    reviewedByName: string | null;
}

/**
 * Get all chats for a specific user (admin view).
 */
export const getUserChats = async (userId: string): Promise<ChatData[]> => {
    const response = await api.get(`/admin/users/${userId}/chats`);
    return response.data;
};

/**
 * Get all chats for a specific startup (admin view).
 */
export const getStartupChats = async (startupId: string): Promise<ChatData[]> => {
    const response = await api.get(`/admin/startups/${startupId}/chats`);
    return response.data;
};

/**
 * Get all messages for a specific chat (admin view).
 */
/**
 * Get all messages for a specific chat (admin view).
 */
export const getChatMessages = async (chatId: string): Promise<MessageData[]> => {
    const response = await api.get(`/admin/chats/${chatId}/messages`);
    return response.data;
};

/**
 * Get all chat reports with optional filtering.
 */
export const getChatReports = async (
    page: number = 0,
    size: number = 20,
    status?: string
): Promise<{ content: ChatReportData[]; totalElements: number; totalPages: number }> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (status) {
        params.append('status', status);
    }

    const response = await api.get(`/admin/chat-reports?${params.toString()}`);
    return response.data;
};

/**
 * Review a chat report.
 */
export const reviewChatReport = async (reportId: string): Promise<ChatReportData> => {
    const response = await api.post(`/admin/chat-reports/${reportId}/review`);
    return response.data;
};

/**
 * Dismiss a chat report.
 */
export const dismissChatReport = async (reportId: string): Promise<ChatReportData> => {
    const response = await api.post(`/admin/chat-reports/${reportId}/dismiss`);
    return response.data;
};

/**
 * Delete a chat message (soft delete).
 */
export const deleteChatMessage = async (messageId: string): Promise<void> => {
    await api.delete(`/admin/chat-messages/${messageId}`);
};

import { api } from '../lib/api';

/**
 * Nudge info for display in dashboard
 */
export interface NudgeInfo {
    id: number;
    senderId: string;
    senderName: string;
    senderEmail: string;
    senderAvatarUrl: string | null;
    receiverId: string;
    receiverName: string;
    receiverEmail: string;
    receiverAvatarUrl: string | null;
    // Startup info
    startupId: string;
    startupName: string;
    startupLogoUrl: string | null;
    startupIndustry: string | null;
    createdAt: string;
}

/**
 * User nudge statistics response
 */
export interface UserNudgeStats {
    userRole: string;
    // For INVESTOR
    receivedCount?: number;
    // For STARTUP_OWNER
    sentCount?: number;
    remainingThisMonth?: number;
    monthlyLimit?: number;
    currentPlan?: string;
    // Common
    nudges: NudgeInfo[];
}

/**
 * Get nudge statistics for a specific user (admin only)
 */
export async function getUserNudgeStats(userId: string): Promise<UserNudgeStats> {
    const response = await api.get<UserNudgeStats>(`/admin/nudges/user/${userId}`);
    return response.data;
}

/**
 * Startup nudge statistics response
 */
export interface StartupNudgeStats {
    count: number;
    nudges: NudgeInfo[];
}

/**
 * Get nudges for a specific startup (admin only)
 */
export async function getStartupNudges(startupId: string): Promise<StartupNudgeStats> {
    const response = await api.get<StartupNudgeStats>(`/admin/nudges/startup/${startupId}`);
    return response.data;
}


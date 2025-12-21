/**
 * API service module for making HTTP requests to the backend.
 * Handles authentication tokens via shared Axios instance and provides typed API methods.
 */

import { Startup, StartupStats, User } from '../types';
import api from './axios';
import { AxiosResponse } from 'axios';

export { api };

// Helper to extract data and handle errors consistently
const request = async <T>(promise: Promise<AxiosResponse<T>>): Promise<T> => {
    try {
        const response = await promise;
        return response.data;
    } catch (error: any) {
        // Extract meaningful error message from backend response if available
        const message = error.response?.data?.message ||
            error.response?.data?.error ||
            error.message ||
            'An error occurred';
        throw new Error(message);
    }
};

// ==================== Dashboard Stats API ====================

export interface DashboardStats {
    totalUsers: number;
    activeStartups: number;
    activeInvestors: number;
    mrr: number;
    pendingVerifications: number;
    totalFunding: number;
    userGrowth: number;
    startupGrowth: number;
    investorGrowth: number;
    mrrGrowth: number;
}

export async function fetchDashboardStats(): Promise<DashboardStats> {
    return request(api.get('/admin/stats'));
}

// ==================== Financial API ====================

export interface RevenueDataPoint {
    month: string;
    revenue: number;
}

export interface SubscriptionStats {
    totalSubscriptions: number;
    activeSubscriptions: number;
    byPlan: {
        FREE: number;
        PRO: number;
        ELITE: number;
    };
}

export interface Payment {
    transactionId: string;
    userEmail: string;
    amount: number;
    currency: string;
    status: string;
    description: string;
    paymentMethod: string;
    timestamp: string;
}

export async function fetchRevenueChart(): Promise<RevenueDataPoint[]> {
    return request(api.get('/admin/financials/revenue-chart'));
}

export async function fetchSubscriptionStats(): Promise<SubscriptionStats> {
    return request(api.get('/admin/financials/subscriptions'));
}

export async function fetchRecentPayments(limit: number = 10): Promise<Payment[]> {
    return request(api.get(`/admin/financials/payments`, { params: { limit } }));
}

export interface FinancialSummary {
    currentMonthRevenue: number;
    mrr: number;
    subscriptions: SubscriptionStats;
}

export async function fetchFinancialSummary(): Promise<FinancialSummary> {
    return request(api.get('/admin/financials/summary'));
}

// ==================== Investor Verification API ====================

export interface InvestorVerification {
    id: string;
    userId: string;
    userEmail: string;
    bio: string;
    investmentBudget: number;
    preferredIndustries: string;
    linkedInUrl: string;
    verificationRequestedAt: string;
}

export interface InvestorStats {
    totalInvestors: number;
    verifiedInvestors: number;
    pendingVerifications: number;
    totalInvestmentBudget: number;
}

export async function fetchVerificationQueue(): Promise<InvestorVerification[]> {
    return request(api.get('/admin/investors/queue'));
}

export async function fetchInvestorStats(): Promise<InvestorStats> {
    return request(api.get('/admin/investors/stats'));
}

export async function approveInvestorForPayment(id: string): Promise<{ message: string }> {
    return request(api.post(`/admin/investors/${id}/approve-verification`));
}

export async function rejectInvestorVerification(id: string, reason?: string): Promise<{ message: string }> {
    return request(api.post(`/admin/investors/${id}/reject-verification`, { reason }));
}

// ==================== App Config API ====================

export interface AppConfigItem {
    key: string;
    value: string;
    description: string;
    category: string;
    valueType: string;
    updatedAt: string;
}

export interface PublicConfig {
    version: number;
    data: Record<string, string>;
}

export async function fetchAllConfigs(): Promise<AppConfigItem[]> {
    return request(api.get('/admin/config'));
}

export async function fetchConfigsGrouped(): Promise<Record<string, AppConfigItem[]>> {
    return request(api.get('/admin/config/grouped'));
}

export async function updateConfig(key: string, value: string): Promise<AppConfigItem> {
    return request(api.put(`/admin/config/${key}`, { value }));
}

export async function batchUpdateConfigs(updates: Record<string, string>): Promise<{ version: number }> {
    return request(api.put('/admin/config', updates));
}

export async function fetchConfigVersion(): Promise<{ version: number }> {
    return request(api.get('/admin/config/version'));
}

// ==================== Startup Management API ====================

// Startup interface imported from types

export interface StartupFilterParams {
    name?: string;
    nameNegate?: boolean;
    industry?: string;
    industryNegate?: boolean;
    ownerEmail?: string;
    ownerEmailNegate?: boolean;
    memberEmail?: string;
    memberEmailNegate?: boolean;
    stage?: string;
    stageNegate?: boolean;
    status?: string;
    statusNegate?: boolean;
    fundingGoalMin?: number;
    fundingGoalMax?: number;
    fundingGoalNegate?: boolean;
    raisedAmountMin?: number;
    raisedAmountMax?: number;
    raisedAmountNegate?: boolean;
    createdAtFrom?: string;
    createdAtTo?: string;
    createdAtNegate?: boolean;
}

export async function fetchAllStartups(
    page: number = 0,
    size: number = 20,
    filters: StartupFilterParams = {}
): Promise<{
    content: Startup[];
    totalElements: number;
    totalPages: number;
}> {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    // Add filters to params
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== '') {
            if (key.endsWith('From') || key.endsWith('To')) {
                params.append(key, new Date(value as string).toISOString());
            } else {
                params.append(key, value.toString());
            }
        }
    });

    return request(api.get('/admin/startups/all', { params }));
}

export async function transferStartupOwnership(startupId: string, newOwnerId: string): Promise<Startup> {
    return request(api.put(`/startups/${startupId}/transfer-ownership`, { newOwnerId }));
}

// StartupStats interface imported from types

export async function fetchStartupStats(): Promise<StartupStats> {
    return request(api.get('/admin/startups/stats-overview'));
}

export async function createStartup(data: Partial<Startup>): Promise<Startup> {
    return request(api.post('/startups', data));
}

export async function getStartupById(id: string): Promise<Startup> {
    return request(api.get(`/startups/${id}`));
}

export async function deleteStartup(id: string): Promise<void> {
    return request(api.delete(`/admin/startups/${id}`));
}

export interface StartupModerationLog {
    id: string;
    adminId: string;
    adminEmail: string;
    actionType: string;
    reason?: string;
    newStatus?: string;
    previousStatus?: string;
    createdAt: string;
}

export async function fetchStartupModerationLogs(startupId: string): Promise<StartupModerationLog[]> {
    return request(api.get(`/admin/startups/${startupId}/audit-logs`));
}

export async function deleteStartupModerationLog(logId: string): Promise<void> {
    return request(api.delete(`/admin/audit-logs/${logId}`));
}

export async function deleteUserModerationLog(logId: string): Promise<void> {
    return request(api.delete(`/admin/users/moderation-logs/${logId}`));
}

// ==================== User API ====================

// User interface imported from types

export async function searchUsers(query: string, role?: string, roleNegate?: boolean, status?: string): Promise<{ content: User[] }> {
    const params: any = {
        query,
        size: 10
    };
    if (role) params.role = role;
    if (roleNegate) params.roleNegate = true;
    if (status) params.status = status;

    return request(api.get('/admin/users', { params }));
}

export async function restoreUser(id: string): Promise<{ message: string }> {
    return request(api.post(`/admin/users/${id}/restore`));
}

export async function updatePreferredCurrency(currency: string): Promise<void> {
    return request(api.put('/users/me/preferred-currency', { currency }));
}

export async function syncExchangeRates(): Promise<Record<string, string>> {
    return request(api.post('/admin/config/sync-rates'));
}

// ==================== Startup Team API ====================

export async function addStartupMember(startupId: string, userId: string, role: string, joinedAt: string, leftAt: string | null): Promise<Startup> {
    return request(api.post(`/startups/${startupId}/members`, { userId, role, joinedAt, leftAt }));
}

// Reuse existing search functionality via getAllStartups if needed, or add specialized search
export async function searchStartups(query: string): Promise<{ content: Startup[] }> {
    return request(api.get('/admin/startups/all', { params: { query, size: 10 } }));
}

export async function leaveStartup(startupId: string): Promise<void> {
    return request(api.post(`/startups/${startupId}/leave`));
}

export async function unsignStartup(startupId: string): Promise<void> {
    return request(api.delete(`/startups/${startupId}/members/me`));
}

export async function removeStartupMember(startupId: string, userId: string): Promise<void> {
    return request(api.post(`/startups/${startupId}/members/${userId}/remove`));
}

export async function unsignStartupMember(startupId: string, userId: string): Promise<void> {
    return request(api.delete(`/startups/${startupId}/members/${userId}`));
}

// ==================== Security API ====================

export interface SecurityStats {
    totalTokens: number;
    activeSessions: number;
    expiredTokens: number;
    onlineUsers: number;
    deviceStats: Record<string, number>;
    activityTrend: Record<string, number>;
}

export async function fetchSecurityStats(): Promise<SecurityStats> {
    return request(api.get('/admin/security/stats'));
}

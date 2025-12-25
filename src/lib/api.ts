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
    monthNum: number;
    revenue: number;
    proRevenue?: number;
    eliteRevenue?: number;
}

export interface DailyRevenuePoint {
    date: string;
    day: number;
    revenue: number;
}

export interface SubscriptionStats {
    totalSubscriptions: number;
    activeSubscriptions: number;
    cancelledSubscriptions: number;
    expiredSubscriptions: number;
    freeUsers: number;
    proMonthly: number;
    proYearly: number;
    eliteMonthly: number;
    eliteYearly: number;
    proRevenue: number;
    eliteRevenue: number;
    totalRevenue: number;
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

export interface PaymentsPage {
    content: Payment[];
    totalElements: number;
    totalPages: number;
}

export interface FinancialSummary {
    currentMonthRevenue: number;
    previousMonthRevenue: number;
    revenueGrowthPercent: number;
    mrr: number;
    arr: number;
    totalLifetimeRevenue: number;
    arpu: number;
    totalUsers: number;
    freeUsers: number;
    proSubscribers: number;
    eliteSubscribers: number;
    activeSubscriptions: number;
    conversionRate: number;
    churnRate: number;
    totalPayments: number;
    completedPayments: number;
    pendingPayments: number;
    failedPayments: number;
    refundedPayments: number;
    totalCommissionRevenue: number;
    totalDeals: number;
    completedDeals: number;
    subscriptions: SubscriptionStats;
}

export async function fetchFinancialSummary(): Promise<FinancialSummary> {
    return request(api.get('/admin/financials/summary'));
}

export async function fetchMonthlyRevenue(): Promise<RevenueDataPoint[]> {
    return request(api.get('/admin/financials/revenue/monthly'));
}

export async function fetchDailyRevenue(): Promise<DailyRevenuePoint[]> {
    return request(api.get('/admin/financials/revenue/daily'));
}

export async function fetchRevenueChart(): Promise<RevenueDataPoint[]> {
    return request(api.get('/admin/financials/revenue/monthly'));
}

export async function fetchSubscriptionStats(): Promise<SubscriptionStats> {
    return request(api.get('/admin/financials/subscriptions/stats'));
}

export async function fetchRecentPayments(page: number = 0, size: number = 10): Promise<PaymentsPage> {
    return request(api.get('/admin/financials/payments', { params: { page, size } }));
}

export async function fetchPaymentStatusBreakdown(): Promise<Record<string, number>> {
    return request(api.get('/admin/financials/payments/status-breakdown'));
}

// ==================== Investor Verification API ====================

export interface InvestorVerification {
    id: string;
    userId: string;
    userEmail: string;
    userName?: string;
    userAvatar?: string;
    bio: string;
    investmentBudget: number;
    preferredIndustries: string;
    linkedInUrl: string;
    verificationRequestedAt: string;
    readyForPayment?: boolean;
}

export interface InvestorStats {
    totalInvestors: number;
    verifiedInvestors: number;
    pendingVerifications: number;
    totalInvestmentBudget: number;
}

export interface InvestorQueueResponse {
    content: InvestorVerification[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
}

export async function fetchVerificationQueue(
    page: number = 0,
    size: number = 20,
    query?: string
): Promise<InvestorQueueResponse> {
    const params: any = { page, size };
    if (query) params.query = query;
    return request(api.get('/admin/investors/queue', { params }));
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

export async function transferStartupOwnership(startupId: string, newOwnerId: string, newOwnerRole?: string): Promise<Startup> {
    return request(api.put(`/startups/${startupId}/transfer-ownership`, { newOwnerId, newOwnerRole }));
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

export async function updateMemberRole(startupId: string, memberUserId: string, role: string): Promise<Startup> {
    return request(api.patch(`/startups/${startupId}/members/${memberUserId}/role`, { role }));
}

export async function reactivateStartupMember(startupId: string, userId: string): Promise<Startup> {
    return request(api.post(`/startups/${startupId}/members/${userId}/reactivate`));
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

// ==================== Reports API ====================

export interface Report {
    id: string;
    reporterId: string;
    reportedEntityType: 'USER' | 'STARTUP' | 'CHAT_MESSAGE';
    reportedEntityId: string;
    reason: string;
    description: string;
    status: 'PENDING' | 'UNDER_REVIEW' | 'RESOLVED' | 'REJECTED' | 'DISMISSED';
    adminAction?: string;
    adminId?: string;
    adminNotes?: string;
    actionDetails?: string;
    notifyReporter: boolean;
    reporterNotified: boolean;
    createdAt: string;
    updatedAt: string;
    resolvedAt?: string;
    reporterName?: string;
    reporterImage?: string;
    reportedEntityName?: string;
    reportedEntityImage?: string;
    reportedEntityStatus?: string;
    notifyReportedEntity?: boolean;
    reportedEntityNotified?: boolean;
}

export interface ReporterStatistics {
    id: string;
    userId: string;
    totalReportsSubmitted: number;
    resolvedReports: number;
    rejectedReports: number;
    falseReportRate: number;
    reportingRestricted: boolean;
    restrictedAt?: string;
    restrictionReason?: string;
    warningCount: number;
    lastWarningAt?: string;
}

export interface CreateReportRequest {
    reason: string;
    description: string;
}

export type NotificationChannel = 'EMAIL' | 'IN_APP';

export interface ResolveReportRequest {
    adminAction: string;
    actionDetails?: string;
    adminNotes?: string;
    notifyReporter?: boolean;
    reporterNotificationChannels?: NotificationChannel[];
    notifyReportedEntity?: boolean;
    reportedEntityNotificationChannels?: NotificationChannel[];
}

// User endpoints
export async function reportUser(userId: string, data: CreateReportRequest): Promise<{ message: string; reportId: string; status: string }> {
    return request(api.post(`/reports/user/${userId}`, data));
}

export async function reportStartup(startupId: string, data: CreateReportRequest): Promise<{ message: string; reportId: string; status: string }> {
    return request(api.post(`/reports/startup/${startupId}`, data));
}

export async function getMyReports(page: number = 0, size: number = 20): Promise<{ content: Report[]; totalElements: number; totalPages: number }> {
    return request(api.get('/reports/my-reports', { params: { page, size } }));
}

// Admin endpoints
export async function getAllReports(
    page: number = 0,
    size: number = 20,
    status?: string,
    entityType?: string
): Promise<{ content: Report[]; totalElements: number; totalPages: number }> {
    const params: any = { page, size };
    if (status) params.status = status;
    if (entityType) params.entityType = entityType;
    return request(api.get('/admin/reports', { params }));
}

export async function getReportDetails(reportId: string): Promise<Report> {
    return request(api.get(`/admin/reports/${reportId}`));
}

export async function updateReportStatus(reportId: string, status: string): Promise<Report> {
    return request(api.put(`/admin/reports/${reportId}/status`, null, { params: { status } }));
}

export async function resolveReport(reportId: string, data: ResolveReportRequest): Promise<{ message: string; report: Report }> {
    return request(api.post(`/admin/reports/${reportId}/resolve`, data));
}

export async function rejectReport(reportId: string, reason?: string): Promise<{ message: string; report: Report }> {
    return request(api.post(`/admin/reports/${reportId}/reject`, null, { params: { reason } }));
}

export async function warnReporter(reportId: string, warningMessage: string): Promise<{ message: string }> {
    return request(api.post(`/admin/reports/${reportId}/warn-reporter`, null, { params: { warningMessage } }));
}

export async function restrictReporter(reportId: string, reason: string): Promise<{ message: string }> {
    return request(api.post(`/admin/reports/${reportId}/restrict-reporter`, null, { params: { reason } }));
}

export async function getReporterStatistics(userId: string): Promise<ReporterStatistics> {
    return request(api.get(`/admin/reporters/${userId}/statistics`));
}

export async function getReportStats(): Promise<{
    total: number;
    pending: number;
    underReview: number;
    resolved: number;
    rejected: number;
}> {
    return request(api.get('/admin/reports/stats'));
}

export async function getReportsForEntity(entityType: string, entityId: string): Promise<Report[]> {
    return request(api.get(`/admin/reports/entity/${entityType}/${entityId}`));
}

export async function deleteReport(reportId: string): Promise<{ message: string }> {
    return request(api.delete(`/admin/reports/${reportId}`));
}

// ==================== Deals API ====================

export interface Deal {
    id: string;
    investorId: string;
    investorName: string;
    investorEmail: string;
    investorAvatar?: string;
    startupId: string;
    startupName: string;
    startupLogo?: string;
    amount: number;
    currency: string;
    status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
    dealType?: string;
    equityPercentage?: number;
    commissionPercentage?: number;
    notes?: string;
    dealDate: string;
    createdAt: string;
    updatedAt: string;
}

export interface DealStats {
    totalDeals: number;
    pendingDeals: number;
    completedDeals: number;
    cancelledDeals: number;
    totalCompletedAmount: number;
    totalCommissionRevenue: number;
}

export interface DealRequest {
    investorId: string;
    startupId: string;
    amount: number;
    currency?: string;
    status?: 'PENDING' | 'COMPLETED' | 'CANCELLED';
    dealType?: string;
    equityPercentage?: number;
    commissionPercentage?: number;
    notes?: string;
    dealDate?: string;
}

export interface DealFilterParams {
    query?: string;
    status?: string;
    dealType?: string;
}

export async function fetchAllDeals(
    page: number = 0,
    size: number = 20,
    query?: string
): Promise<{
    content: Deal[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
}> {
    const params: Record<string, any> = { page, size };
    if (query) params.query = query;
    return request(api.get('/admin/deals', { params }));
}

export async function fetchDealById(id: string): Promise<Deal> {
    return request(api.get(`/admin/deals/${id}`));
}

export async function fetchDealsForInvestor(investorId: string): Promise<Deal[]> {
    return request(api.get(`/admin/deals/investor/${investorId}`));
}

export async function fetchDealsForStartup(startupId: string): Promise<Deal[]> {
    return request(api.get(`/admin/deals/startup/${startupId}`));
}

export async function createDeal(data: DealRequest): Promise<Deal> {
    return request(api.post('/admin/deals', data));
}

export async function updateDeal(id: string, data: DealRequest): Promise<Deal> {
    return request(api.put(`/admin/deals/${id}`, data));
}

export async function deleteDeal(id: string): Promise<{ message: string }> {
    return request(api.delete(`/admin/deals/${id}`));
}

export async function fetchDealStats(): Promise<DealStats> {
    return request(api.get('/admin/deals/stats'));
}


/**
 * API service module for making HTTP requests to the backend.
 * Handles authentication tokens and provides typed API methods.
 */

import { Startup, StartupStats, User } from '../types';

const API_BASE_URL = '/api/v1';

/**
 * Get the authentication token from localStorage.
 */
function getAuthToken(): string | null {
    return localStorage.getItem('token');
}

/**
 * Create headers with authentication.
 */
function createHeaders(includeAuth: boolean = true): HeadersInit {
    const headers: HeadersInit = {
        'Content-Type': 'application/json',
    };

    if (includeAuth) {
        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }

    return headers;
}

/**
 * Handle API response and throw error if not ok.
 */
async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = 'An error occurred';
        try {
            const errorJson = JSON.parse(errorText);
            errorMessage = errorJson.message || errorJson.error || errorMessage;
        } catch {
            errorMessage = errorText || response.statusText;
        }
        throw new Error(errorMessage);
    }

    // Check for empty body (204 No Content or content-length 0)
    if (response.status === 204) {
        return undefined as unknown as T;
    }

    const text = await response.text();
    if (!text) {
        return undefined as unknown as T;
    }

    try {
        return JSON.parse(text);
    } catch {
        // Fallback if not JSON (though usually it should be)
        return text as unknown as T;
    }
}

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
    const response = await fetch(`${API_BASE_URL}/admin/stats`, {
        headers: createHeaders(),
    });
    return handleResponse<DashboardStats>(response);
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
    const response = await fetch(`${API_BASE_URL}/admin/financials/revenue-chart`, {
        headers: createHeaders(),
    });
    return handleResponse<RevenueDataPoint[]>(response);
}

export async function fetchSubscriptionStats(): Promise<SubscriptionStats> {
    const response = await fetch(`${API_BASE_URL}/admin/financials/subscriptions`, {
        headers: createHeaders(),
    });
    return handleResponse<SubscriptionStats>(response);
}

export async function fetchRecentPayments(limit: number = 10): Promise<Payment[]> {
    const response = await fetch(`${API_BASE_URL}/admin/financials/payments?limit=${limit}`, {
        headers: createHeaders(),
    });
    return handleResponse<Payment[]>(response);
}

export interface FinancialSummary {
    currentMonthRevenue: number;
    mrr: number;
    subscriptions: SubscriptionStats;
}

export async function fetchFinancialSummary(): Promise<FinancialSummary> {
    const response = await fetch(`${API_BASE_URL}/admin/financials/summary`, {
        headers: createHeaders(),
    });
    return handleResponse<FinancialSummary>(response);
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
    const response = await fetch(`${API_BASE_URL}/admin/investors/queue`, {
        headers: createHeaders(),
    });
    return handleResponse<InvestorVerification[]>(response);
}

export async function fetchInvestorStats(): Promise<InvestorStats> {
    const response = await fetch(`${API_BASE_URL}/admin/investors/stats`, {
        headers: createHeaders(),
    });
    return handleResponse<InvestorStats>(response);
}

export async function approveInvestorForPayment(id: string): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE_URL}/admin/investors/${id}/approve-verification`, {
        method: 'POST',
        headers: createHeaders(),
    });
    return handleResponse<{ message: string }>(response);
}

export async function rejectInvestorVerification(id: string, reason?: string): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE_URL}/admin/investors/${id}/reject-verification`, {
        method: 'POST',
        headers: createHeaders(),
        body: JSON.stringify({ reason }),
    });
    return handleResponse<{ message: string }>(response);
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
    const response = await fetch(`${API_BASE_URL}/admin/config`, {
        headers: createHeaders(),
    });
    return handleResponse<AppConfigItem[]>(response);
}

export async function fetchConfigsGrouped(): Promise<Record<string, AppConfigItem[]>> {
    const response = await fetch(`${API_BASE_URL}/admin/config/grouped`, {
        headers: createHeaders(),
    });
    return handleResponse<Record<string, AppConfigItem[]>>(response);
}

export async function updateConfig(key: string, value: string): Promise<AppConfigItem> {
    const response = await fetch(`${API_BASE_URL}/admin/config/${key}`, {
        method: 'PUT',
        headers: createHeaders(),
        body: JSON.stringify({ value }),
    });
    return handleResponse<AppConfigItem>(response);
}

export async function batchUpdateConfigs(updates: Record<string, string>): Promise<{ version: number }> {
    const response = await fetch(`${API_BASE_URL}/admin/config`, {
        method: 'PUT',
        headers: createHeaders(),
        body: JSON.stringify(updates),
    });
    return handleResponse<{ version: number }>(response);
}

export async function fetchConfigVersion(): Promise<{ version: number }> {
    const response = await fetch(`${API_BASE_URL}/admin/config/version`, {
        headers: createHeaders(),
    });
    return handleResponse<{ version: number }>(response);
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

    const response = await fetch(`${API_BASE_URL}/admin/startups/all?${params}`, {
        headers: createHeaders(),
    });
    return handleResponse(response);
}

export async function transferStartupOwnership(startupId: string, newOwnerId: string): Promise<Startup> {
    const response = await fetch(`${API_BASE_URL}/startups/${startupId}/transfer-ownership`, {
        method: 'PUT',
        headers: createHeaders(),
        body: JSON.stringify({ newOwnerId }),
    });
    return handleResponse<Startup>(response);
}

// StartupStats interface imported from types

export async function fetchStartupStats(): Promise<StartupStats> {
    const response = await fetch(`${API_BASE_URL}/admin/startups/stats-overview`, {
        headers: createHeaders(),
    });
    return handleResponse<StartupStats>(response);
}

export async function createStartup(data: Partial<Startup>): Promise<Startup> {
    const response = await fetch(`${API_BASE_URL}/startups`, {
        method: 'POST',
        headers: createHeaders(),
        body: JSON.stringify(data),
    });
    return handleResponse<Startup>(response);
}

export async function getStartupById(id: string): Promise<Startup> {
    const response = await fetch(`${API_BASE_URL}/startups/${id}`, {
        headers: createHeaders(),
    });
    return handleResponse<Startup>(response);
}

// ==================== User API ====================

// User interface imported from types

export async function searchUsers(query: string, role?: string, roleNegate?: boolean): Promise<{ content: User[] }> {
    let url = `${API_BASE_URL}/admin/users?query=${encodeURIComponent(query)}&size=10`;
    if (role) {
        url += `&role=${encodeURIComponent(role)}`;
    }
    if (roleNegate) {
        url += `&roleNegate=true`;
    }
    const response = await fetch(url, {
        headers: createHeaders(),
    });
    return handleResponse(response);
}

export async function updatePreferredCurrency(currency: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/users/me/preferred-currency`, {
        method: 'PUT',
        headers: createHeaders(),
        body: JSON.stringify({ currency }),
    });
    return handleResponse<void>(response);
}

export async function syncExchangeRates(): Promise<Record<string, string>> {
    const response = await fetch(`${API_BASE_URL}/admin/config/sync-rates`, {
        method: 'POST',
        headers: createHeaders(),
    });
    return handleResponse<Record<string, string>>(response);
}

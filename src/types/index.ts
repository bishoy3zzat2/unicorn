/**
 * Type definitions for Unicorn Admin Dashboard
 * Matches backend DTOs for type safety
 */

// Startup development stages
export type StartupStage = 'IDEA' | 'MVP' | 'SEED' | 'SERIES_A' | 'SERIES_B' | 'GROWTH';

// Startup status
export type StartupStatus = 'ACTIVE' | 'BANNED';

export type StartupRole = 'FOUNDER' | 'CO_FOUNDER' | 'CEO' | 'CTO' | 'COO' | 'CFO' | 'CMO' | 'CHIEF_PRODUCT_OFFICER' | 'OTHER';

// Startup Member interface
export interface StartupMember {
    id: string;
    userId: string;
    userName: string;
    userEmail: string;
    userAvatarUrl: string;
    role: string;
    joinedAt: string;
    leftAt?: string;
    isActive: boolean;
}

// Startup interface (matches backend StartupResponse DTO)
export interface Startup {
    id: string;
    name: string;
    tagline?: string;
    fullDescription?: string;
    industry: string;
    stage: StartupStage;
    fundingGoal: number;
    raisedAmount: number;
    websiteUrl?: string;
    logoUrl?: string;
    coverUrl?: string;
    pitchDeckUrl?: string;
    financialDocumentsUrl?: string;
    businessPlanUrl?: string;
    businessModelUrl?: string;
    facebookUrl?: string;
    instagramUrl?: string;
    twitterUrl?: string;
    status: StartupStatus;
    ownerId: string;
    ownerEmail: string;
    ownerRole?: StartupRole;
    createdAt: string;
    updatedAt: string;
    warningCount?: number;
    members: StartupMember[];
}

// Request to update startup status (admin)
export interface UpdateStartupStatusRequest {
    status: StartupStatus;
    rejectionReason?: string;
}

export interface StartupStats {
    total: number;
    active: number;
    banned: number;
    totalMembers: number;
}

export interface User {
    id: string;
    email: string;
    role: string;
    status: string;
    createdAt: string;
    lastLoginAt: string | null;
    firstName?: string;
    lastName?: string;
    avatarUrl?: string;
    username?: string;
    authProvider?: string;
    phoneNumber?: string;
    country?: string;
    preferredCurrency?: string;
}

// Deal status
export type DealStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';

// Deal type (investment round)
export type DealType =
    | 'SEED'
    | 'PRE_SEED'
    | 'SERIES_A'
    | 'SERIES_B'
    | 'SERIES_C'
    | 'BRIDGE'
    | 'CONVERTIBLE_NOTE'
    | 'SAFE'
    | 'EQUITY'
    | 'DEBT'
    | 'GRANT'
    | 'OTHER';

// Deal interface (matches backend DealResponse DTO)
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
    status: DealStatus;
    dealType?: DealType;
    equityPercentage?: number;
    notes?: string;
    dealDate: string;
    createdAt: string;
    updatedAt: string;
}

// Deal statistics
export interface DealStats {
    totalDeals: number;
    pendingDeals: number;
    completedDeals: number;
    cancelledDeals: number;
    totalCompletedAmount: number;
}

// Request to create or update a deal
export interface DealRequest {
    investorId: string;
    startupId: string;
    amount: number;
    currency?: string;
    status?: DealStatus;
    dealType?: DealType;
    equityPercentage?: number;
    notes?: string;
    dealDate?: string;
}


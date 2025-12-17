/**
 * Type definitions for Unicorn Admin Dashboard
 * Matches backend DTOs for type safety
 */

// Startup development stages
export type StartupStage = 'IDEA' | 'MVP' | 'SEED' | 'SERIES_A' | 'SERIES_B' | 'GROWTH';

// Startup status
export type StartupStatus = 'ACTIVE' | 'APPROVED';

export type StartupRole = 'FOUNDER' | 'CO_FOUNDER' | 'CEO' | 'CTO' | 'COO' | 'CFO' | 'CMO' | 'CHIEF_PRODUCT_OFFICER' | 'OTHER';

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
}

// Request to update startup status (admin)
export interface UpdateStartupStatusRequest {
    status: StartupStatus;
    rejectionReason?: string;
}

export interface StartupStats {
    total: number;
    active: number;
    pending: number;
    rejected: number;
}

export interface User {
    id: string;
    email: string;
    role: string;
    status: string;
    createdAt: string;
    lastLoginAt: string | null;
}

export interface KPIData {
    totalUsers: number
    activeStartups: number
    mrr: number
    activeInvestors: number
    pendingRequests: number
    totalFunding: number
    userGrowth: number
    startupGrowth: number
    mrrGrowth: number
    investorGrowth: number
    pendingGrowth: number
    fundingGrowth: number
}

export interface RevenueDataPoint {
    month: string
    revenue: number
}

export interface UserGrowthDataPoint {
    month: string
    users: number
}

export interface StartupStatusDataPoint {
    name: string
    value: number
    color: string
}

export interface StartupRequest {
    id: string
    startupName: string
    founderName: string
    dateSubmitted: string
    status: 'pending' | 'approved' | 'rejected'
}

export interface User {
    id: string
    name: string
    email: string
    type: 'investor' | 'startup'
    joinDate: string
}

export const kpiData: KPIData = {
    totalUsers: 12847,
    activeStartups: 342,
    mrr: 284500,
    activeInvestors: 156,
    pendingRequests: 48,
    totalFunding: 12500000,
    userGrowth: 12.5,
    startupGrowth: 8.3,
    mrrGrowth: 15.7,
    investorGrowth: 5.2,
    pendingGrowth: -2.4,
    fundingGrowth: 22.1,
}

export const revenueData: RevenueDataPoint[] = [
    { month: 'Jan', revenue: 185000 },
    { month: 'Feb', revenue: 195000 },
    { month: 'Mar', revenue: 210000 },
    { month: 'Apr', revenue: 225000 },
    { month: 'May', revenue: 238000 },
    { month: 'Jun', revenue: 245000 },
    { month: 'Jul', revenue: 252000 },
    { month: 'Aug', revenue: 260000 },
    { month: 'Sep', revenue: 268000 },
    { month: 'Oct', revenue: 275000 },
    { month: 'Nov', revenue: 281000 },
    { month: 'Dec', revenue: 284500 },
]

export const userGrowthData: UserGrowthDataPoint[] = [
    { month: 'Jan', users: 120 },
    { month: 'Feb', users: 250 },
    { month: 'Mar', users: 450 },
    { month: 'Apr', users: 890 },
    { month: 'May', users: 1560 },
    { month: 'Jun', users: 2800 },
    { month: 'Jul', users: 4100 },
    { month: 'Aug', users: 6200 },
    { month: 'Sep', users: 8500 },
    { month: 'Oct', users: 10200 },
    { month: 'Nov', users: 11800 },
    { month: 'Dec', users: 12847 },
]

export const startupStatusData: StartupStatusDataPoint[] = [
    { name: 'Active', value: 342, color: '#8b5cf6' }, // Violet-500
    { name: 'Pending', value: 48, color: '#f59e0b' }, // Amber-500
    { name: 'Rejected', value: 125, color: '#ef4444' }, // Red-500
]

export const securityStats = {
    totalTokens: 15420,
    activeTokens: 12847,
    expiredTokens: 2450,
    revokedTokens: 123,
    activeGrowth: 5.2,
    expiredGrowth: 1.1,
    revokedGrowth: -15.4, // Decreased revocations is good
}

export const startupRequests: StartupRequest[] = [
    {
        id: '1',
        startupName: 'TechVision AI',
        founderName: 'Sarah Johnson',
        dateSubmitted: '2024-12-08',
        status: 'pending',
    },
    {
        id: '2',
        startupName: 'GreenEnergy Solutions',
        founderName: 'Michael Chen',
        dateSubmitted: '2024-12-07',
        status: 'pending',
    },
    {
        id: '3',
        startupName: 'HealthTech Pro',
        founderName: 'Emily Rodriguez',
        dateSubmitted: '2024-12-06',
        status: 'pending',
    },
    {
        id: '4',
        startupName: 'FinanceFlow',
        founderName: 'David Park',
        dateSubmitted: '2024-12-05',
        status: 'pending',
    },
    {
        id: '5',
        startupName: 'EduLearn Platform',
        founderName: 'Jessica Williams',
        dateSubmitted: '2024-12-04',
        status: 'pending',
    },
    {
        id: '6',
        startupName: 'CloudSecure',
        founderName: 'Robert Martinez',
        dateSubmitted: '2024-12-03',
        status: 'pending',
    },
]

export const users: User[] = [
    {
        id: '1',
        name: 'John Anderson',
        email: 'john.anderson@example.com',
        type: 'investor',
        joinDate: '2024-01-15',
    },
    {
        id: '2',
        name: 'Maria Garcia',
        email: 'maria.garcia@example.com',
        type: 'investor',
        joinDate: '2024-02-20',
    },
    {
        id: '3',
        name: 'James Wilson',
        email: 'james.wilson@example.com',
        type: 'investor',
        joinDate: '2024-03-10',
    },
    {
        id: '4',
        name: 'TechVision AI',
        email: 'contact@techvision.com',
        type: 'startup',
        joinDate: '2024-04-05',
    },
    {
        id: '5',
        name: 'GreenEnergy Solutions',
        email: 'info@greenenergy.com',
        type: 'startup',
        joinDate: '2024-05-12',
    },
    {
        id: '6',
        name: 'Patricia Taylor',
        email: 'patricia.taylor@example.com',
        type: 'investor',
        joinDate: '2024-06-18',
    },
    {
        id: '7',
        name: 'HealthTech Pro',
        email: 'hello@healthtech.com',
        type: 'startup',
        joinDate: '2024-07-22',
    },
    {
        id: '8',
        name: 'Christopher Lee',
        email: 'chris.lee@example.com',
        type: 'investor',
        joinDate: '2024-08-30',
    },
    {
        id: '9',
        name: 'FinanceFlow',
        email: 'support@financeflow.com',
        type: 'startup',
        joinDate: '2024-09-14',
    },
    {
        id: '10',
        name: 'Amanda White',
        email: 'amanda.white@example.com',
        type: 'investor',
        joinDate: '2024-10-05',
    },
]

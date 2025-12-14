import { useEffect, useState } from 'react'
import { KPICard } from '../components/dashboard/KPICard'
import { RevenueChart } from '../components/dashboard/RevenueChart'
import { UserGrowthChart } from '../components/dashboard/UserGrowthChart'
import { StartupDistributionChart } from '../components/dashboard/StartupDistributionChart'
import { Users, Rocket, DollarSign, Briefcase, Clock, TrendingUp, AlertCircle, Loader2 } from 'lucide-react'
import { fetchDashboardStats, DashboardStats } from '../lib/api'
import { formatNumber, formatCurrency } from '../lib/utils'
import { Alert, AlertDescription } from '../components/ui/alert'

export function Overview() {
    const [stats, setStats] = useState<DashboardStats | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        async function loadStats() {
            try {
                setLoading(true)
                setError(null)
                const data = await fetchDashboardStats()
                setStats(data)
            } catch (err) {
                console.error('Failed to fetch dashboard stats:', err)
                setError(err instanceof Error ? err.message : 'Failed to load dashboard data')
            } finally {
                setLoading(false)
            }
        }

        loadStats()
    }, [])

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading dashboard...</span>
            </div>
        )
    }

    if (error) {
        return (
            <div className="space-y-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Dashboard Overview</h1>
                    <p className="text-muted-foreground mt-2">
                        Welcome back! Here's what's happening with your platform today.
                    </p>
                </div>
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>
                        {error}. Using fallback data.
                    </AlertDescription>
                </Alert>
                {/* Fallback to mock data display */}
                <FallbackDashboard />
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Dashboard Overview</h1>
                <p className="text-muted-foreground mt-2">
                    Welcome back! Here's what's happening with your platform today.
                </p>
            </div>

            {/* KPI Cards */}
            {stats && (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                    <KPICard
                        title="Total Users"
                        value={formatNumber(stats.totalUsers)}
                        icon={Users}
                        trend={stats.userGrowth}
                        iconColor="text-blue-500"
                    />
                    <KPICard
                        title="Active Startups"
                        value={formatNumber(stats.activeStartups)}
                        icon={Rocket}
                        trend={stats.startupGrowth}
                        iconColor="text-purple-500"
                    />
                    <KPICard
                        title="Monthly Recurring Revenue"
                        value={formatCurrency(stats.mrr)}
                        icon={DollarSign}
                        trend={stats.mrrGrowth}
                        iconColor="text-emerald-500"
                    />
                    <KPICard
                        title="Active Investors"
                        value={formatNumber(stats.activeInvestors)}
                        icon={Briefcase}
                        trend={stats.investorGrowth}
                        iconColor="text-orange-500"
                    />
                    <KPICard
                        title="Pending Verifications"
                        value={formatNumber(stats.pendingVerifications)}
                        icon={Clock}
                        trend={0}
                        iconColor="text-yellow-500"
                    />
                    <KPICard
                        title="Total Funding Raised"
                        value={formatCurrency(stats.totalFunding)}
                        icon={TrendingUp}
                        trend={0}
                        iconColor="text-indigo-500"
                    />
                </div>
            )}

            {/* Charts Section */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <div className="col-span-1 lg:col-span-2">
                    <RevenueChart />
                </div>
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <UserGrowthChart />
                <StartupDistributionChart />
            </div>
        </div>
    )
}

// Fallback dashboard with mock data
function FallbackDashboard() {
    const mockStats = {
        totalUsers: 12847,
        activeStartups: 342,
        mrr: 284500,
        activeInvestors: 156,
        pendingVerifications: 48,
        totalFunding: 12500000,
        userGrowth: 12.5,
        startupGrowth: 8.3,
        mrrGrowth: 15.7,
        investorGrowth: 5.2,
    }

    return (
        <>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <KPICard
                    title="Total Users"
                    value={formatNumber(mockStats.totalUsers)}
                    icon={Users}
                    trend={mockStats.userGrowth}
                    iconColor="text-blue-500"
                />
                <KPICard
                    title="Active Startups"
                    value={formatNumber(mockStats.activeStartups)}
                    icon={Rocket}
                    trend={mockStats.startupGrowth}
                    iconColor="text-purple-500"
                />
                <KPICard
                    title="Monthly Recurring Revenue"
                    value={formatCurrency(mockStats.mrr)}
                    icon={DollarSign}
                    trend={mockStats.mrrGrowth}
                    iconColor="text-emerald-500"
                />
                <KPICard
                    title="Active Investors"
                    value={formatNumber(mockStats.activeInvestors)}
                    icon={Briefcase}
                    trend={mockStats.investorGrowth}
                    iconColor="text-orange-500"
                />
                <KPICard
                    title="Pending Verifications"
                    value={formatNumber(mockStats.pendingVerifications)}
                    icon={Clock}
                    trend={0}
                    iconColor="text-yellow-500"
                />
                <KPICard
                    title="Total Funding Raised"
                    value={formatCurrency(mockStats.totalFunding)}
                    icon={TrendingUp}
                    trend={0}
                    iconColor="text-indigo-500"
                />
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <div className="col-span-1 lg:col-span-2">
                    <RevenueChart />
                </div>
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <UserGrowthChart />
                <StartupDistributionChart />
            </div>
        </>
    )
}

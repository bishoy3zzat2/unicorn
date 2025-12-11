import { KPICard } from '../components/dashboard/KPICard'
import { RevenueChart } from '../components/dashboard/RevenueChart'
import { UserGrowthChart } from '../components/dashboard/UserGrowthChart'
import { StartupDistributionChart } from '../components/dashboard/StartupDistributionChart'
import { Users, Rocket, DollarSign, Briefcase, Clock, TrendingUp } from 'lucide-react'
import { kpiData } from '../lib/mockData'
import { formatNumber, formatCurrency } from '../lib/utils'

export function Overview() {
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
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <KPICard
                    title="Total Users"
                    value={formatNumber(kpiData.totalUsers)}
                    icon={Users}
                    trend={kpiData.userGrowth}
                    iconColor="text-blue-500"
                />
                <KPICard
                    title="Active Startups"
                    value={formatNumber(kpiData.activeStartups)}
                    icon={Rocket}
                    trend={kpiData.startupGrowth}
                    iconColor="text-purple-500"
                />
                <KPICard
                    title="Monthly Recurring Revenue"
                    value={formatCurrency(kpiData.mrr)}
                    icon={DollarSign}
                    trend={kpiData.mrrGrowth}
                    iconColor="text-emerald-500"
                />
                <KPICard
                    title="Active Investors"
                    value={formatNumber(kpiData.activeInvestors)}
                    icon={Briefcase}
                    trend={kpiData.investorGrowth}
                    iconColor="text-orange-500"
                />
                <KPICard
                    title="Pending Requests"
                    value={formatNumber(kpiData.pendingRequests)}
                    icon={Clock}
                    trend={kpiData.pendingGrowth}
                    iconColor="text-yellow-500"
                />
                <KPICard
                    title="Total Funding Raised"
                    value={formatCurrency(kpiData.totalFunding)}
                    icon={TrendingUp}
                    trend={kpiData.fundingGrowth}
                    iconColor="text-indigo-500"
                />
            </div>

            {/* Charts Section */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <div className="col-span-1 lg:col-span-2">
                    <RevenueChart />
                </div>
                {/* This placeholder will be managed by grid flow, but let's be explicit if needed or just let it flow */}
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                <UserGrowthChart />
                <StartupDistributionChart />
            </div>
        </div>
    )
}

import { useEffect, useState } from 'react'
import { RevenueChart } from '../components/dashboard/RevenueChart'
import { UserGrowthChart } from '../components/dashboard/UserGrowthChart'
import { StartupDistributionChart } from '../components/dashboard/StartupDistributionChart'
import { Users, Rocket, DollarSign, Briefcase, Clock, TrendingUp, AlertCircle, Loader2, ChevronDown, ChevronUp } from 'lucide-react'
import { fetchDashboardStats, DashboardStats, fetchFinancialSummary, fetchDealStats, getReportStats, fetchSecurityStats } from '../lib/api'
import { formatNumber, formatCurrency } from '../lib/utils'
import { Alert, AlertDescription } from '../components/ui/alert'

export function Overview() {
    const [stats, setStats] = useState<DashboardStats | null>(null)
    const [financials, setFinancials] = useState<any | null>(null)
    const [dealStats, setDealStats] = useState<any | null>(null)
    const [reportStats, setReportStats] = useState<any | null>(null)
    const [securityStats, setSecurityStats] = useState<any | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [showAllStats, setShowAllStats] = useState(false)

    useEffect(() => {
        async function loadAllStats() {
            try {
                setLoading(true)
                setError(null)

                // Fetch all stats concurrently
                const [
                    dashboardData,
                    financialData,
                    dealData,
                    reportData,
                    securityData
                ] = await Promise.all([
                    fetchDashboardStats(),
                    fetchFinancialSummary(),
                    fetchDealStats(),
                    getReportStats(),
                    fetchSecurityStats()
                ])

                setStats(dashboardData)
                setFinancials(financialData)
                setDealStats(dealData)
                setReportStats(reportData)
                setSecurityStats(securityData)
            } catch (err) {
                console.error('Failed to fetch dashboard stats:', err)
                setError(err instanceof Error ? err.message : 'Failed to load dashboard data')
            } finally {
                setLoading(false)
            }
        }

        loadAllStats()
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
                    <Alert variant="destructive" className="mt-4">
                        <AlertCircle className="h-4 w-4" />
                        <AlertDescription>
                            {error}
                        </AlertDescription>
                    </Alert>
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-6">

            {/* KPI Grid - 5 columns for 15 cards */}
            {stats && financials && dealStats && reportStats && securityStats && (
                <div className="space-y-4">
                    <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">

                        {/* 1. Total Users */}
                        <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-4 text-white shadow-lg">
                            <Users className="h-6 w-6 mb-2 opacity-80" />
                            <div className="text-2xl font-bold">{formatNumber(stats.totalUsers)}</div>
                            <div className="text-blue-100 text-xs font-medium">Total Users</div>
                            <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                <span>With accounts</span>
                                <span className="bg-white/20 px-1.5 py-0.5 rounded">+{stats.userGrowth}%</span>
                            </div>
                        </div>

                        {/* 2. Active Startups */}
                        <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-violet-500 to-purple-600 p-4 text-white shadow-lg">
                            <Rocket className="h-6 w-6 mb-2 opacity-80" />
                            <div className="text-2xl font-bold">{formatNumber(stats.activeStartups)}</div>
                            <div className="text-violet-100 text-xs font-medium">Active Startups</div>
                            <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                <span>Verified entities</span>
                                <span className="bg-white/20 px-1.5 py-0.5 rounded">+{stats.startupGrowth}%</span>
                            </div>
                        </div>

                        {/* 3. Active Investors */}
                        <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 p-4 text-white shadow-lg">
                            <Briefcase className="h-6 w-6 mb-2 opacity-80" />
                            <div className="text-2xl font-bold">{formatNumber(stats.activeInvestors)}</div>
                            <div className="text-amber-100 text-xs font-medium">Active Investors</div>
                            <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                <span>Looking for deals</span>
                                <span className="bg-white/20 px-1.5 py-0.5 rounded">+{stats.investorGrowth}%</span>
                            </div>
                        </div>

                        {/* 4. MRR */}
                        <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 p-4 text-white shadow-lg">
                            <DollarSign className="h-6 w-6 mb-2 opacity-80" />
                            <div className="text-2xl font-bold">{formatCurrency(stats.mrr)}</div>
                            <div className="text-emerald-100 text-xs font-medium">Monthly Revenue</div>
                            <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                <span>Recurring</span>
                                <span className="bg-white/20 px-1.5 py-0.5 rounded">+{stats.mrrGrowth}%</span>
                            </div>
                        </div>

                        {/* 5. Total Funding */}
                        <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-indigo-500 to-blue-600 p-4 text-white shadow-lg">
                            <TrendingUp className="h-6 w-6 mb-2 opacity-80" />
                            <div className="text-2xl font-bold">{formatCurrency(stats.totalFunding)}</div>
                            <div className="text-indigo-100 text-xs font-medium">Total Funding</div>
                            <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                <span>Capital raised</span>
                                <span className="bg-white/20 px-1.5 py-0.5 rounded">Lifetime</span>
                            </div>
                        </div>

                        {showAllStats && (
                            <>
                                {/* 6. Pending Verifications */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-yellow-500 to-amber-500 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300">
                                    <Clock className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatNumber(stats.pendingVerifications)}</div>
                                    <div className="text-yellow-100 text-xs font-medium">Pending Verifications</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>Investor requests</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Action needed</span>
                                    </div>
                                </div>

                                {/* 7. Online Users - Security */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-cyan-500 to-blue-500 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <div className="absolute top-2 right-2 h-2 w-2 rounded-full bg-white animate-pulse" />
                                    <div className="h-6 w-6 mb-2 opacity-80 text-white font-mono text-xs border border-white/50 rounded flex items-center justify-center">LIVE</div>
                                    <div className="text-2xl font-bold">{formatNumber(securityStats.onlineUsers)}</div>
                                    <div className="text-cyan-100 text-xs font-medium">Online Users</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>{securityStats.activeSessions} active sessions</span>
                                    </div>
                                </div>

                                {/* 8. Pending Deals */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-pink-500 to-rose-500 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <Briefcase className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatNumber(dealStats.pendingDeals)}</div>
                                    <div className="text-pink-100 text-xs font-medium">Pending Deals</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>In negotiation</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Active</span>
                                    </div>
                                </div>

                                {/* 9. Completed Deals Count */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-teal-500 to-emerald-600 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <TrendingUp className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatNumber(dealStats.completedDeals)}</div>
                                    <div className="text-teal-100 text-xs font-medium">Deals Closed</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>Total value</span>
                                        <span className="truncate ml-1">{formatCurrency(dealStats.totalCompletedAmount)}</span>
                                    </div>
                                </div>

                                {/* 10. Commission Revenue */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-lime-500 to-green-600 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <DollarSign className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatCurrency(dealStats.totalCommissionRevenue)}</div>
                                    <div className="text-lime-100 text-xs font-medium">Commission Rev</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>From deals</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Success fees</span>
                                    </div>
                                </div>

                                {/* 11. ARPU */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-sky-500 to-indigo-600 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <Users className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatCurrency(financials.arpu)}</div>
                                    <div className="text-sky-100 text-xs font-medium">ARPU</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>Avg Rev Per User</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Monthly</span>
                                    </div>
                                </div>

                                {/* 12. Churn Rate */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-red-500 to-rose-600 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <TrendingUp className="h-6 w-6 mb-2 opacity-80 rotate-180" />
                                    <div className="text-2xl font-bold">{financials.churnRate}%</div>
                                    <div className="text-red-100 text-xs font-medium">Churn Rate</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>Attrition</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Monthly</span>
                                    </div>
                                </div>

                                {/* 13. Conversion Rate */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-fuchsia-500 to-pink-600 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <TrendingUp className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{financials.conversionRate}%</div>
                                    <div className="text-fuchsia-100 text-xs font-medium">Conversion Rate</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>Free to Paid</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Performance</span>
                                    </div>
                                </div>

                                {/* 14. Active Subscriptions */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-600 to-indigo-700 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <Briefcase className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatNumber(financials.activeSubscriptions)}</div>
                                    <div className="text-indigo-100 text-xs font-medium">Active Subs</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>Pro & Elite</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">All tiers</span>
                                    </div>
                                </div>

                                {/* 15. Pending Reports */}
                                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-orange-400 to-red-500 p-4 text-white shadow-lg animate-in fade-in zoom-in duration-300 slide-in-from-top-2">
                                    <AlertCircle className="h-6 w-6 mb-2 opacity-80" />
                                    <div className="text-2xl font-bold">{formatNumber(reportStats.pending)}</div>
                                    <div className="text-orange-100 text-xs font-medium">Pending Reports</div>
                                    <div className="mt-2 pt-2 border-t border-white/20 flex items-center justify-between text-[10px]">
                                        <span>User flagged</span>
                                        <span className="bg-white/20 px-1.5 py-0.5 rounded">Review ASAP</span>
                                    </div>
                                </div>
                            </>
                        )}

                    </div>

                    <div className="flex justify-center">
                        <button
                            onClick={() => setShowAllStats(!showAllStats)}
                            className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-full shadow-sm hover:shadow transition-all"
                        >
                            {showAllStats ? (
                                <>
                                    <ChevronUp className="h-4 w-4" />
                                    Show Less
                                </>
                            ) : (
                                <>
                                    <ChevronDown className="h-4 w-4" />
                                    Show More Stats
                                </>
                            )}
                        </button>
                    </div>
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

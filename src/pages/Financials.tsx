import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Button } from '../components/ui/button'
import {
    DollarSign,
    TrendingUp,
    CreditCard,
    Users,
    Loader2,
    AlertCircle,
    Crown,
    Sparkles,
    User,
    RefreshCcw,
    Percent,
    Target,
    Receipt,
    CheckCircle,
    XCircle,
    Clock,
    RotateCcw,
    Briefcase,
    PieChart as PieChartIcon,
    Search,
    Download,
    Filter,
    ChevronLeft,
    ChevronRight
} from 'lucide-react'
import { Alert, AlertDescription } from '../components/ui/alert'
import { Input } from '../components/ui/input'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '../components/ui/select'
import { KPICard } from '../components/dashboard/KPICard'
import {
    fetchFinancialSummary,
    fetchMonthlyRevenue,
    fetchDailyRevenue,
    fetchRecentPayments,
    fetchPaymentStatusBreakdown,
    FinancialSummary,
    RevenueDataPoint,
    DailyRevenuePoint,
    PaymentsPage
} from '../lib/api'
import { formatCurrency, formatDate, cn } from '../lib/utils'
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell,
    Legend,
    BarChart,
    Bar,
} from 'recharts'

// Color palette
const COLORS = {
    primary: '#8b5cf6',
    secondary: '#06b6d4',
    success: '#10b981',
    warning: '#f59e0b',
    danger: '#ef4444',
    muted: '#64748b',
    free: '#94a3b8',
    pro: '#8b5cf6',
    elite: '#f59e0b'
}

// Format currency with K/M for large amounts
const formatCompactCurrency = (amount: number, currency: string = 'USD') => {
    const currencySymbol = currency === 'USD' ? '$' : currency === 'EUR' ? '€' : currency === 'GBP' ? '£' : currency === 'EGP' ? 'E£' : '$'

    if (amount < 1000) {
        return `${currencySymbol}${amount.toFixed(0)}`
    } else if (amount < 1000000) {
        const value = amount / 1000
        return `${currencySymbol}${value % 1 === 0 ? value : value.toFixed(1)}K`
    } else if (amount < 1000000000) {
        const value = amount / 1000000
        return `${currencySymbol}${value % 1 === 0 ? value : value.toFixed(1)}M`
    } else {
        const value = amount / 1000000000
        return `${currencySymbol}${value % 1 === 0 ? value : value.toFixed(1)}B`
    }
}

export function Financials() {
    const [summary, setSummary] = useState<FinancialSummary | null>(null)
    const [monthlyRevenue, setMonthlyRevenue] = useState<RevenueDataPoint[]>([])
    const [dailyRevenue, setDailyRevenue] = useState<DailyRevenuePoint[]>([])
    const [payments, setPayments] = useState<PaymentsPage | null>(null)
    const [paymentBreakdown, setPaymentBreakdown] = useState<Record<string, number>>({})
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [paymentsPage, setPaymentsPage] = useState(0)
    const [paymentsFilter, setPaymentsFilter] = useState<string>('all')
    const [paymentsSearch, setPaymentsSearch] = useState('')
    const [paymentsPerPage] = useState(10)

    const loadData = async () => {
        try {
            setLoading(true)
            setError(null)

            const [summaryData, monthlyData, dailyData, paymentsData, breakdownData] = await Promise.all([
                fetchFinancialSummary(),
                fetchMonthlyRevenue(),
                fetchDailyRevenue(),
                fetchRecentPayments(0, 10),
                fetchPaymentStatusBreakdown()
            ])

            setSummary(summaryData)
            setMonthlyRevenue(monthlyData)
            setDailyRevenue(dailyData)
            setPayments(paymentsData)
            setPaymentBreakdown(breakdownData)
        } catch (err) {
            console.error('Failed to fetch financial data:', err)
            setError(err instanceof Error ? err.message : 'Failed to load financial data')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadData()
    }, [])

    const loadPayments = async (page: number = 0) => {
        try {
            const data = await fetchRecentPayments(page, paymentsPerPage)
            setPayments(data)
            setPaymentsPage(page)
        } catch (err) {
            console.error('Failed to load payments:', err)
        }
    }

    // Filter payments locally (for status filter and search)
    const filteredPayments = payments?.content?.filter(payment => {
        const matchesStatus = paymentsFilter === 'all' || payment.status === paymentsFilter
        const matchesSearch = paymentsSearch === '' ||
            payment.userEmail.toLowerCase().includes(paymentsSearch.toLowerCase()) ||
            payment.transactionId.toLowerCase().includes(paymentsSearch.toLowerCase())
        return matchesStatus && matchesSearch
    }) || []

    // Export payments to CSV
    const exportPayments = () => {
        if (!payments?.content) return

        const headers = ['Transaction ID', 'User Email', 'Amount', 'Currency', 'Status', 'Date']
        const csvData = payments.content.map(p => [
            p.transactionId,
            p.userEmail,
            p.amount.toString(),
            p.currency || 'USD',
            p.status,
            new Date(p.timestamp).toISOString()
        ])

        const csvContent = [
            headers.join(','),
            ...csvData.map(row => row.join(','))
        ].join('\n')

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = `payments_export_${new Date().toISOString().split('T')[0]}.csv`
        link.click()
    }

    // Subscription pie chart data
    const subscriptionPieData = summary ? [
        { name: 'Free', value: summary.freeUsers || 0, color: COLORS.free },
        { name: 'Pro', value: summary.proSubscribers || 0, color: COLORS.pro },
        { name: 'Elite', value: summary.eliteSubscribers || 0, color: COLORS.elite },
    ] : []

    // Payment status pie data
    const paymentStatusData = [
        { name: 'Completed', value: paymentBreakdown.COMPLETED || 0, color: COLORS.success },
        { name: 'Pending', value: paymentBreakdown.PENDING || 0, color: COLORS.warning },
        { name: 'Failed', value: paymentBreakdown.FAILED || 0, color: COLORS.danger },
        { name: 'Refunded', value: paymentBreakdown.REFUNDED || 0, color: COLORS.secondary },
    ].filter(d => d.value > 0)

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading financials...</span>
            </div>
        )
    }

    return (
        <div className="space-y-6 transition-colors duration-300">
            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            {/* Stats Overview - Row 1: Revenue Metrics */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">

                {/* Monthly Revenue */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <DollarSign className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactCurrency(summary?.currentMonthRevenue || 0)}</div>
                    <div className="text-emerald-100 text-sm">Monthly Revenue</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <TrendingUp className="h-4 w-4 opacity-70" />
                        <span className="text-sm">
                            {(summary?.revenueGrowthPercent || 0) > 0 ? '+' : ''}{(summary?.revenueGrowthPercent || 0).toFixed(1)}% growth
                        </span>
                    </div>
                </div>

                {/* MRR */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <TrendingUp className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactCurrency(summary?.mrr || 0)}</div>
                    <div className="text-blue-100 text-sm">MRR</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Monthly Recurring Revenue</span>
                    </div>
                </div>

                {/* ARR */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-purple-500 to-violet-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Target className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactCurrency(summary?.arr || 0)}</div>
                    <div className="text-purple-100 text-sm">ARR</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Annual Recurring Revenue</span>
                    </div>
                </div>

                {/* Lifetime Revenue */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <CreditCard className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactCurrency(summary?.totalLifetimeRevenue || 0)}</div>
                    <div className="text-amber-100 text-sm">Lifetime Revenue</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">All time total</span>
                    </div>
                </div>

            </div>

            {/* Stats Overview - Row 2: User & Conversion Metrics */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">

                {/* Active Subscribers */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-indigo-500 to-indigo-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Users className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{(summary?.activeSubscriptions || 0).toLocaleString()}</div>
                    <div className="text-indigo-100 text-sm">Active Subscribers</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Paid users</span>
                    </div>
                </div>

                {/* ARPU */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-teal-500 to-cyan-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <User className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactCurrency(summary?.arpu || 0)}</div>
                    <div className="text-teal-100 text-sm">ARPU</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Avg Revenue Per User</span>
                    </div>
                </div>

                {/* Conversion Rate */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-yellow-500 to-amber-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Percent className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{(summary?.conversionRate || 0).toFixed(1)}%</div>
                    <div className="text-yellow-100 text-sm">Conversion Rate</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Free → Paid</span>
                    </div>
                </div>

                {/* Commission Revenue */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-rose-500 to-red-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Briefcase className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactCurrency(summary?.totalCommissionRevenue || 0)}</div>
                    <div className="text-rose-100 text-sm">Commission Revenue</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">From <strong>{summary?.completedDeals || 0}</strong> deals</span>
                    </div>
                </div>

            </div>

            {/* Charts Row 1 */}
            <div className="grid gap-6 lg:grid-cols-3">
                {/* Revenue Trend Chart */}
                <Card className="lg:col-span-2 group relative overflow-hidden transition-all duration-300 hover:-translate-y-1 border-2 bg-gradient-to-br from-purple-500/5 via-violet-400/10 to-fuchsia-500/5 dark:from-purple-900/30 dark:via-violet-800/20 dark:to-fuchsia-900/30 border-purple-200/50 dark:border-purple-800/50 hover:border-purple-400/70 hover:shadow-xl">
                    {/* Dot Pattern Overlay */}
                    <div
                        className="absolute inset-0 opacity-[0.08] dark:opacity-[0.05] text-slate-900 dark:text-slate-100"
                        style={{
                            backgroundImage: `radial-gradient(circle, currentColor 1px, transparent 1px)`,
                            backgroundSize: '16px 16px'
                        }}
                    />
                    <CardHeader className="relative z-10">
                        <div className="flex items-center gap-3">
                            <div className="p-3 rounded-xl bg-gradient-to-br from-purple-500 to-violet-500 shadow-lg">
                                <TrendingUp className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <CardTitle className="text-lg font-bold">Revenue Trend</CardTitle>
                                <CardDescription>Monthly revenue over the last 12 months</CardDescription>
                            </div>
                        </div>
                    </CardHeader>
                    <CardContent className="relative z-10">
                        <div className="h-80">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={monthlyRevenue}>
                                    <defs>
                                        <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor={COLORS.primary} stopOpacity={0.3} />
                                            <stop offset="95%" stopColor={COLORS.primary} stopOpacity={0} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                                    <XAxis dataKey="month" className="text-xs" />
                                    <YAxis className="text-xs" tickFormatter={(v) => `$${v / 1000}k`} />
                                    <Tooltip
                                        formatter={(value: number) => [formatCurrency(value), 'Revenue']}
                                        contentStyle={{
                                            backgroundColor: 'hsl(var(--card))',
                                            border: '1px solid hsl(var(--border))',
                                            borderRadius: '8px'
                                        }}
                                    />
                                    <Area
                                        type="monotone"
                                        dataKey="revenue"
                                        stroke={COLORS.primary}
                                        strokeWidth={2}
                                        fillOpacity={1}
                                        fill="url(#colorRevenue)"
                                    />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>

                {/* Subscription Distribution */}
                <Card className="group relative overflow-hidden transition-all duration-300 hover:-translate-y-1 border-2 bg-gradient-to-br from-indigo-500/5 via-purple-400/10 to-blue-500/5 dark:from-indigo-900/30 dark:via-purple-800/20 dark:to-blue-900/30 border-indigo-200/50 dark:border-indigo-800/50 hover:border-indigo-400/70 hover:shadow-xl">
                    {/* Dot Pattern Overlay */}
                    <div
                        className="absolute inset-0 opacity-[0.08] dark:opacity-[0.05] text-slate-900 dark:text-slate-100"
                        style={{
                            backgroundImage: `radial-gradient(circle, currentColor 1px, transparent 1px)`,
                            backgroundSize: '16px 16px'
                        }}
                    />
                    <CardHeader className="relative z-10">
                        <div className="flex items-center gap-3">
                            <div className="p-3 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-lg">
                                <PieChartIcon className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <CardTitle className="text-lg font-bold">Subscription Plans</CardTitle>
                                <CardDescription>Distribution by plan type</CardDescription>
                            </div>
                        </div>
                    </CardHeader>
                    <CardContent className="relative z-10">
                        <div className="h-52">
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={subscriptionPieData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={45}
                                        outerRadius={65}
                                        paddingAngle={5}
                                        dataKey="value"
                                    >
                                        {subscriptionPieData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Pie>
                                    <Tooltip
                                        formatter={(value: number, name: string) => [value, name]}
                                        contentStyle={{
                                            backgroundColor: 'hsl(var(--card))',
                                            border: '1px solid hsl(var(--border))',
                                            borderRadius: '8px'
                                        }}
                                    />
                                    <Legend />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                        {/* Plan Stats */}
                        <div className="mt-4 space-y-2 pt-3 border-t border-current/10">
                            <div className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <User className="h-4 w-4 text-slate-400" />
                                    <span>Free</span>
                                </div>
                                <span className="font-bold">{summary?.freeUsers || 0}</span>
                            </div>
                            <div className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <Sparkles className="h-4 w-4 text-purple-500" />
                                    <span>Pro</span>
                                </div>
                                <span className="font-bold">{summary?.proSubscribers || 0}</span>
                            </div>
                            <div className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <Crown className="h-4 w-4 text-amber-500" />
                                    <span>Elite</span>
                                </div>
                                <span className="font-bold">{summary?.eliteSubscribers || 0}</span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Charts Row 2 */}
            <div className="grid gap-6 lg:grid-cols-2">
                {/* Daily Revenue Bar Chart */}
                <Card className="group relative overflow-hidden transition-all duration-300 hover:-translate-y-1 border-2 bg-gradient-to-br from-emerald-500/5 via-green-400/10 to-teal-500/5 dark:from-emerald-900/30 dark:via-green-800/20 dark:to-teal-900/30 border-emerald-200/50 dark:border-emerald-800/50 hover:border-emerald-400/70 hover:shadow-xl">
                    {/* Dot Pattern Overlay */}
                    <div
                        className="absolute inset-0 opacity-[0.08] dark:opacity-[0.05] text-slate-900 dark:text-slate-100"
                        style={{
                            backgroundImage: `radial-gradient(circle, currentColor 1px, transparent 1px)`,
                            backgroundSize: '16px 16px'
                        }}
                    />
                    <CardHeader className="relative z-10">
                        <div className="flex items-center gap-3">
                            <div className="p-3 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-500 shadow-lg">
                                <DollarSign className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <CardTitle className="text-lg font-bold">Daily Revenue</CardTitle>
                                <CardDescription>Last 30 days</CardDescription>
                            </div>
                        </div>
                    </CardHeader>
                    <CardContent className="relative z-10">
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={dailyRevenue}>
                                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                                    <XAxis dataKey="day" className="text-xs" />
                                    <YAxis className="text-xs" tickFormatter={(v) => `$${v}`} />
                                    <Tooltip
                                        formatter={(value: number) => [formatCurrency(value), 'Revenue']}
                                        labelFormatter={(label, payload) => {
                                            if (payload && payload[0]) {
                                                return payload[0].payload.date
                                            }
                                            return `Day ${label}`
                                        }}
                                        contentStyle={{
                                            backgroundColor: 'hsl(var(--card))',
                                            border: '1px solid hsl(var(--border))',
                                            borderRadius: '8px'
                                        }}
                                    />
                                    <Bar dataKey="revenue" fill={COLORS.success} radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>

                {/* Payment Status Breakdown */}
                <Card className="group relative overflow-hidden transition-all duration-300 hover:-translate-y-1 border-2 bg-gradient-to-br from-blue-500/5 via-blue-400/10 to-cyan-500/5 dark:from-blue-900/30 dark:via-blue-800/20 dark:to-cyan-900/30 border-blue-200/50 dark:border-blue-800/50 hover:border-blue-400/70 hover:shadow-xl">
                    {/* Dot Pattern Overlay */}
                    <div
                        className="absolute inset-0 opacity-[0.08] dark:opacity-[0.05] text-slate-900 dark:text-slate-100"
                        style={{
                            backgroundImage: `radial-gradient(circle, currentColor 1px, transparent 1px)`,
                            backgroundSize: '16px 16px'
                        }}
                    />
                    <CardHeader className="relative z-10">
                        <div className="flex items-center gap-3">
                            <div className="p-3 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 shadow-lg">
                                <Receipt className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <CardTitle className="text-lg font-bold">Payment Status</CardTitle>
                                <CardDescription>Breakdown by status</CardDescription>
                            </div>
                        </div>
                    </CardHeader>
                    <CardContent className="relative z-10">
                        <div className="h-52">
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={paymentStatusData}
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={70}
                                        dataKey="value"
                                        label={({ name, value }) => `${name}: ${value}`}
                                    >
                                        {paymentStatusData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Pie>
                                    <Tooltip
                                        contentStyle={{
                                            backgroundColor: 'hsl(var(--card))',
                                            border: '1px solid hsl(var(--border))',
                                            borderRadius: '8px'
                                        }}
                                    />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                        {/* Payment Stats Grid */}
                        <div className="mt-4 grid grid-cols-2 gap-3 pt-3 border-t border-current/10">
                            <PaymentStatBadge label="Completed" value={summary?.completedPayments || 0} icon={CheckCircle} color="text-emerald-500" />
                            <PaymentStatBadge label="Pending" value={summary?.pendingPayments || 0} icon={Clock} color="text-amber-500" />
                            <PaymentStatBadge label="Failed" value={summary?.failedPayments || 0} icon={XCircle} color="text-red-500" />
                            <PaymentStatBadge label="Refunded" value={summary?.refundedPayments || 0} icon={RotateCcw} color="text-cyan-500" />
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Recent Payments Table */}
            <Card className="group relative overflow-hidden transition-all duration-300 border-2 bg-gradient-to-br from-gray-500/5 via-slate-400/10 to-zinc-500/5 dark:from-gray-800/30 dark:via-slate-700/20 dark:to-zinc-800/30 border-gray-200/50 dark:border-gray-700/50 hover:border-gray-400/70">
                {/* Dot Pattern Overlay */}
                <div
                    className="absolute inset-0 opacity-[0.08] dark:opacity-[0.05] text-slate-900 dark:text-slate-100"
                    style={{
                        backgroundImage: `radial-gradient(circle, currentColor 1px, transparent 1px)`,
                        backgroundSize: '16px 16px'
                    }}
                />
                <CardHeader className="relative z-10">
                    <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                        <div className="flex items-center gap-3">
                            <div className="p-3 rounded-xl bg-gradient-to-br from-gray-500 to-slate-500 shadow-lg">
                                <CreditCard className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <CardTitle className="text-lg font-bold">Recent Payments</CardTitle>
                                <CardDescription>
                                    {payments ? `${payments.totalElements} total payments` : 'Loading...'}
                                </CardDescription>
                            </div>
                        </div>
                        <div className="flex flex-wrap items-center gap-2">
                            {/* Search */}
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Search payments..."
                                    value={paymentsSearch}
                                    onChange={(e) => setPaymentsSearch(e.target.value)}
                                    className="pl-9 w-48 bg-white/50 dark:bg-slate-800/50"
                                />
                            </div>
                            {/* Status Filter */}
                            <Select value={paymentsFilter} onValueChange={setPaymentsFilter}>
                                <SelectTrigger className="w-36 bg-white/50 dark:bg-slate-800/50">
                                    <Filter className="h-4 w-4 mr-2" />
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="all">All Status</SelectItem>
                                    <SelectItem value="COMPLETED">Completed</SelectItem>
                                    <SelectItem value="PENDING">Pending</SelectItem>
                                    <SelectItem value="FAILED">Failed</SelectItem>
                                    <SelectItem value="REFUNDED">Refunded</SelectItem>
                                </SelectContent>
                            </Select>
                            {/* Export */}
                            <Button onClick={exportPayments} variant="outline" size="sm" className="gap-2 bg-white/50 dark:bg-slate-800/50">
                                <Download className="h-4 w-4" />
                                Export
                            </Button>
                            {/* Refresh */}
                            <Button onClick={loadData} variant="outline" size="sm" className="gap-2 bg-white/50 dark:bg-slate-800/50">
                                <RefreshCcw className="h-4 w-4" />
                                Refresh
                            </Button>
                        </div>
                    </div>
                </CardHeader>
                <CardContent className="relative z-10">
                    <div className="relative overflow-x-auto rounded-xl border border-slate-200/50 dark:border-slate-700/50 bg-white/50 dark:bg-slate-900/50">
                        <table className="w-full text-sm">
                            <thead className="bg-slate-100/80 dark:bg-slate-800/80">
                                <tr className="border-b border-slate-200 dark:border-slate-700">
                                    <th className="text-left py-3 px-4 font-semibold text-slate-600 dark:text-slate-400">Transaction ID</th>
                                    <th className="text-left py-3 px-4 font-semibold text-slate-600 dark:text-slate-400">User</th>
                                    <th className="text-left py-3 px-4 font-semibold text-slate-600 dark:text-slate-400">Amount</th>
                                    <th className="text-left py-3 px-4 font-semibold text-slate-600 dark:text-slate-400">Currency</th>
                                    <th className="text-left py-3 px-4 font-semibold text-slate-600 dark:text-slate-400">Status</th>
                                    <th className="text-left py-3 px-4 font-semibold text-slate-600 dark:text-slate-400">Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredPayments.map((payment) => (
                                    <tr key={payment.transactionId} className="border-b border-slate-100 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                                        <td className="py-3 px-4 font-mono text-xs text-muted-foreground">
                                            {payment.transactionId.slice(0, 18)}...
                                        </td>
                                        <td className="py-3 px-4">{payment.userEmail}</td>
                                        <td className="py-3 px-4 font-bold text-emerald-600 dark:text-emerald-400">
                                            {formatCurrency(payment.amount)}
                                        </td>
                                        <td className="py-3 px-4">
                                            <span className="px-2 py-1 rounded-md bg-slate-100 dark:bg-slate-700 text-xs font-medium">
                                                {payment.currency || 'USD'}
                                            </span>
                                        </td>
                                        <td className="py-3 px-4">
                                            <PaymentStatusBadge status={payment.status} />
                                        </td>
                                        <td className="py-3 px-4 text-muted-foreground">
                                            {formatDate(payment.timestamp)}
                                        </td>
                                    </tr>
                                ))}
                                {filteredPayments.length === 0 && (
                                    <tr>
                                        <td colSpan={6} className="py-12 text-center text-muted-foreground">
                                            No payments found
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                    {/* Pagination Controls */}
                    {payments && payments.totalPages > 1 && (
                        <div className="flex items-center justify-between mt-4 px-2">
                            <span className="text-sm text-muted-foreground">
                                Page {paymentsPage + 1} of {payments.totalPages}
                            </span>
                            <div className="flex items-center gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => loadPayments(paymentsPage - 1)}
                                    disabled={paymentsPage === 0}
                                    className="gap-1"
                                >
                                    <ChevronLeft className="h-4 w-4" />
                                    Previous
                                </Button>
                                <div className="flex items-center gap-1">
                                    {Array.from({ length: Math.min(5, payments.totalPages) }, (_, i) => {
                                        const startPage = Math.max(0, Math.min(paymentsPage - 2, payments.totalPages - 5))
                                        const pageNum = startPage + i
                                        if (pageNum >= payments.totalPages) return null
                                        return (
                                            <Button
                                                key={pageNum}
                                                variant={pageNum === paymentsPage ? "default" : "outline"}
                                                size="sm"
                                                onClick={() => loadPayments(pageNum)}
                                                className="w-9 h-9 p-0"
                                            >
                                                {pageNum + 1}
                                            </Button>
                                        )
                                    })}
                                </div>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => loadPayments(paymentsPage + 1)}
                                    disabled={paymentsPage >= payments.totalPages - 1}
                                    className="gap-1"
                                >
                                    Next
                                    <ChevronRight className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    )
}

// Payment Status Badge
function PaymentStatusBadge({ status }: { status: string }) {
    const styles: Record<string, string> = {
        COMPLETED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
        PENDING: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
        FAILED: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
        REFUNDED: 'bg-cyan-100 text-cyan-700 dark:bg-cyan-900/30 dark:text-cyan-400',
    }

    return (
        <span className={cn("inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium", styles[status] || styles.PENDING)}>
            {status}
        </span>
    )
}

// Payment Stat Badge
function PaymentStatBadge({ label, value, icon: Icon, color }: {
    label: string
    value: number
    icon: React.ElementType
    color: string
}) {
    return (
        <div className="flex items-center gap-2 p-2.5 rounded-xl bg-white/60 dark:bg-slate-800/60 border border-current/10">
            <Icon className={cn("h-4 w-4", color)} />
            <div>
                <p className="text-xs text-muted-foreground">{label}</p>
                <p className="font-bold">{value}</p>
            </div>
        </div>
    )
}

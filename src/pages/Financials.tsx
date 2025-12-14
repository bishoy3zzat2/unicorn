import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import {
    DollarSign,
    TrendingUp,
    CreditCard,
    Users,
    Loader2,
    AlertCircle,
    Crown,
    Sparkles,
    User
} from 'lucide-react'
import { Alert, AlertDescription } from '../components/ui/alert'
import {
    fetchFinancialSummary,
    fetchRecentPayments,
    fetchRevenueChart,
    FinancialSummary,
    Payment,
    RevenueDataPoint
} from '../lib/api'
import { formatCurrency, formatDate } from '../lib/utils'
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
    Legend
} from 'recharts'

// Subscription plan colors
const PLAN_COLORS = {
    FREE: '#94a3b8',
    PRO: '#8b5cf6',
    ELITE: '#f59e0b'
}

export function Financials() {
    const [summary, setSummary] = useState<FinancialSummary | null>(null)
    const [payments, setPayments] = useState<Payment[]>([])
    const [revenueData, setRevenueData] = useState<RevenueDataPoint[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        async function loadData() {
            try {
                setLoading(true)
                setError(null)

                const [summaryData, paymentsData, revenueChartData] = await Promise.all([
                    fetchFinancialSummary(),
                    fetchRecentPayments(10),
                    fetchRevenueChart()
                ])

                setSummary(summaryData)
                setPayments(paymentsData)
                setRevenueData(revenueChartData)
            } catch (err) {
                console.error('Failed to fetch financial data:', err)
                setError(err instanceof Error ? err.message : 'Failed to load financial data')
                // Use mock data on error
                setRevenueData(mockRevenueData)
            } finally {
                setLoading(false)
            }
        }

        loadData()
    }, [])

    // Prepare subscription pie chart data
    const subscriptionPieData = summary ? [
        { name: 'Free', value: summary.subscriptions.byPlan?.FREE || 0, color: PLAN_COLORS.FREE },
        { name: 'Pro', value: summary.subscriptions.byPlan?.PRO || 0, color: PLAN_COLORS.PRO },
        { name: 'Elite', value: summary.subscriptions.byPlan?.ELITE || 0, color: PLAN_COLORS.ELITE },
    ] : []

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading financials...</span>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Financials</h1>
                <p className="text-muted-foreground mt-2">
                    Financial reports, revenue analytics, and subscription overview
                </p>
            </div>

            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>
                        {error}. Showing sample data.
                    </AlertDescription>
                </Alert>
            )}

            {/* KPI Cards */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Monthly Revenue</CardTitle>
                        <DollarSign className="h-4 w-4 text-emerald-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {formatCurrency(summary?.currentMonthRevenue || 0)}
                        </div>
                        <p className="text-xs text-muted-foreground">This month</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">MRR</CardTitle>
                        <TrendingUp className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {formatCurrency(summary?.mrr || 0)}
                        </div>
                        <p className="text-xs text-muted-foreground">Monthly recurring revenue</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Active Subscriptions</CardTitle>
                        <Users className="h-4 w-4 text-purple-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {summary?.subscriptions?.activeSubscriptions || 0}
                        </div>
                        <p className="text-xs text-muted-foreground">Paid subscribers</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Total Subscriptions</CardTitle>
                        <CreditCard className="h-4 w-4 text-orange-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {summary?.subscriptions?.totalSubscriptions || 0}
                        </div>
                        <p className="text-xs text-muted-foreground">All time</p>
                    </CardContent>
                </Card>
            </div>

            {/* Charts Row */}
            <div className="grid gap-6 lg:grid-cols-3">
                {/* Revenue Chart */}
                <Card className="lg:col-span-2">
                    <CardHeader>
                        <CardTitle>Revenue Trend</CardTitle>
                        <CardDescription>Monthly revenue over time</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="h-80">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={revenueData.length > 0 ? revenueData : mockRevenueData}>
                                    <defs>
                                        <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.3} />
                                            <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                                    <XAxis dataKey="month" className="text-xs" />
                                    <YAxis className="text-xs" tickFormatter={(value) => `$${value / 1000}k`} />
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
                                        stroke="#8b5cf6"
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
                <Card>
                    <CardHeader>
                        <CardTitle>Subscription Plans</CardTitle>
                        <CardDescription>Distribution by plan type</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={subscriptionPieData.length > 0 ? subscriptionPieData : mockSubscriptionData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={60}
                                        outerRadius={80}
                                        paddingAngle={5}
                                        dataKey="value"
                                    >
                                        {(subscriptionPieData.length > 0 ? subscriptionPieData : mockSubscriptionData).map((entry, index) => (
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
                        <div className="mt-4 space-y-2">
                            <div className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <User className="h-4 w-4 text-slate-400" />
                                    <span>Free</span>
                                </div>
                                <span className="font-medium">{summary?.subscriptions?.byPlan?.FREE || 0}</span>
                            </div>
                            <div className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <Sparkles className="h-4 w-4 text-purple-500" />
                                    <span>Pro</span>
                                </div>
                                <span className="font-medium">{summary?.subscriptions?.byPlan?.PRO || 0}</span>
                            </div>
                            <div className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <Crown className="h-4 w-4 text-amber-500" />
                                    <span>Elite</span>
                                </div>
                                <span className="font-medium">{summary?.subscriptions?.byPlan?.ELITE || 0}</span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Recent Payments */}
            <Card>
                <CardHeader>
                    <CardTitle>Recent Payments</CardTitle>
                    <CardDescription>Latest payment transactions</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="relative overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b">
                                    <th className="text-left py-3 px-4 font-medium">Transaction ID</th>
                                    <th className="text-left py-3 px-4 font-medium">User</th>
                                    <th className="text-left py-3 px-4 font-medium">Amount</th>
                                    <th className="text-left py-3 px-4 font-medium">Status</th>
                                    <th className="text-left py-3 px-4 font-medium">Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {(payments.length > 0 ? payments : mockPayments).map((payment) => (
                                    <tr key={payment.transactionId} className="border-b hover:bg-muted/50">
                                        <td className="py-3 px-4 font-mono text-xs">
                                            {payment.transactionId}
                                        </td>
                                        <td className="py-3 px-4">{payment.userEmail}</td>
                                        <td className="py-3 px-4 font-medium">
                                            {formatCurrency(payment.amount)} {payment.currency}
                                        </td>
                                        <td className="py-3 px-4">
                                            <PaymentStatusBadge status={payment.status} />
                                        </td>
                                        <td className="py-3 px-4 text-muted-foreground">
                                            {formatDate(payment.timestamp)}
                                        </td>
                                    </tr>
                                ))}
                                {payments.length === 0 && mockPayments.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="py-8 text-center text-muted-foreground">
                                            No payments found
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}

// Payment status badge component
function PaymentStatusBadge({ status }: { status: string }) {
    const statusStyles: Record<string, string> = {
        COMPLETED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
        PENDING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
        FAILED: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
        REFUNDED: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
    }

    return (
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusStyles[status] || statusStyles.PENDING}`}>
            {status}
        </span>
    )
}

// Mock data for fallback
const mockRevenueData: RevenueDataPoint[] = [
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

const mockSubscriptionData = [
    { name: 'Free', value: 350, color: PLAN_COLORS.FREE },
    { name: 'Pro', value: 120, color: PLAN_COLORS.PRO },
    { name: 'Elite', value: 28, color: PLAN_COLORS.ELITE },
]

const mockPayments: Payment[] = [
    {
        transactionId: 'TXN-1734123456-ABC123',
        userEmail: 'john@example.com',
        amount: 299,
        currency: 'EGP',
        status: 'COMPLETED',
        description: 'Pro Plan Subscription',
        paymentMethod: 'Card',
        timestamp: '2024-12-13T10:30:00Z'
    },
    {
        transactionId: 'TXN-1734123457-DEF456',
        userEmail: 'sarah@startup.com',
        amount: 799,
        currency: 'EGP',
        status: 'COMPLETED',
        description: 'Elite Plan Subscription',
        paymentMethod: 'Card',
        timestamp: '2024-12-12T15:45:00Z'
    },
    {
        transactionId: 'TXN-1734123458-GHI789',
        userEmail: 'mike@investor.com',
        amount: 299,
        currency: 'EGP',
        status: 'PENDING',
        description: 'Pro Plan Subscription',
        paymentMethod: 'Card',
        timestamp: '2024-12-11T09:20:00Z'
    },
]

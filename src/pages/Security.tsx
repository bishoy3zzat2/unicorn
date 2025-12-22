import { KPICard } from '../components/dashboard/KPICard'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card'
import { Shield, Key, AlertTriangle, CheckCircle, Lock, Monitor, Clock, RefreshCw, Laptop, History, Users } from 'lucide-react'
import { formatNumber } from '../lib/utils'
import { useAuth } from '../contexts/AuthContext'
import { jwtDecode } from 'jwt-decode'
import { useState, useEffect } from 'react'
import { fetchSecurityStats, SecurityStats } from '../lib/api'
import { toast } from 'sonner'
import {
    AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell, Legend
} from 'recharts'

export function Security() {
    const { token, refreshToken } = useAuth()
    const [tokenDetails, setTokenDetails] = useState<any>(null)
    const [refreshTokenDetails, setRefreshTokenDetails] = useState<any>(null)
    const [timeRemaining, setTimeRemaining] = useState<string>('')
    const [stats, setStats] = useState<SecurityStats | null>(null)
    const [loading, setLoading] = useState(true)

    // Brand Colors for Devices
    const DEVICE_COLORS: Record<string, string> = {
        Chrome: '#4285F4', // Google Blue
        Firefox: '#FF7139', // Firefox Orange
        Safari: '#00C7E6', // Safari Cyan
        Edge: '#0078D7', // Edge Blue
        Opera: '#FF1B2D', // Opera Red
        Android: '#3DDC84', // Android Green
        iOS: '#5856D6', // iOS Purple
        Other: '#6B7280', // Gray
    }
    const DEFAULT_COLORS = ['#8b5cf6', '#6366f1', '#ec4899', '#f43f5e', '#10b981', '#f59e0b']

    useEffect(() => {
        loadStats()
    }, [])

    const loadStats = async () => {
        try {
            setLoading(true)
            const data = await fetchSecurityStats()
            setStats(data)
        } catch (error) {
            console.error('Failed to load security stats', error)
            toast.error('Failed to load security statistics')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        // Token details decoding (existing logic)
        if (token) {
            try {
                const decoded: any = jwtDecode(token)
                setTokenDetails({
                    ...decoded,
                    raw: token
                })
            } catch (e) {
                console.error('Failed to decode token', e)
            }
        } else {
            setTokenDetails(null)
        }

        if (refreshToken) {
            setRefreshTokenDetails({
                token: refreshToken,
                exists: true
            })
        } else {
            setRefreshTokenDetails(null)
        }
    }, [token, refreshToken])

    // Countdown timer (existing logic)
    useEffect(() => {
        if (!token) {
            setTimeRemaining('')
            return
        }
        const calculateTimeRemaining = () => {
            try {
                const decoded: any = jwtDecode(token)
                const exp = decoded.exp * 1000
                const now = Date.now()
                const diff = exp - now
                if (diff > 0) {
                    const minutes = Math.floor((diff / 1000 / 60) % 60)
                    const seconds = Math.floor((diff / 1000) % 60)
                    const hours = Math.floor((diff / (1000 * 60 * 60)) % 24)
                    setTimeRemaining(`${hours}h ${minutes}m ${seconds}s`)
                } else {
                    setTimeRemaining('Expired')
                }
            } catch (e) {
                setTimeRemaining('Invalid Token')
            }
        }
        calculateTimeRemaining()
        const timer = setInterval(calculateTimeRemaining, 1000)
        return () => clearInterval(timer)
    }, [token])

    // Prepare chart data
    const deviceData = stats ? Object.entries(stats.deviceStats).map(([name, value]) => ({ name, value })) : []
    const trendData = stats ? Object.entries(stats.activityTrend)
        .sort((a, b) => a[0].localeCompare(b[0]))
        .map(([date, count]) => ({
            date: new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
            sessions: count
        })) : []

    if (loading) {
        return <div className="p-8 flex items-center justify-center">Loading security dashboard...</div>
    }

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent">
                    Security Center
                </h1>
                <p className="text-muted-foreground mt-2">
                    Real-time monitoring of authentication tokens, active sessions, and system security status.
                </p>
            </div>

            {/* General Token Statistics Section */}
            <section className="space-y-6">
                <div className="flex items-center gap-2 mb-4">
                    <Shield className="h-5 w-5 text-primary" />
                    <h2 className="text-xl font-semibold">Live System Overview</h2>
                </div>

                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    <KPICard
                        title="Total Issued Tokens"
                        value={stats ? formatNumber(stats.totalTokens) : '0'}
                        icon={Key}
                        iconColor="text-blue-500"
                        trend="Lifetime total"
                        tooltip="Total number of refresh tokens issued since system start. Indicates overall authentication volume."
                    />
                    <KPICard
                        title="Active Sessions"
                        value={stats ? formatNumber(stats.activeSessions) : '0'}
                        icon={CheckCircle}
                        iconColor="text-green-500"
                        trend="Online now"
                        tooltip="Total number of currently valid and active sessions across all users."
                    />
                    <KPICard
                        title="Expired Tokens"
                        value={stats ? formatNumber(stats.expiredTokens) : '0'}
                        icon={Clock}
                        iconColor="text-gray-500"
                        trend="Past sessions"
                        tooltip="Number of sessions that have expired naturally or were logged out."
                    />
                    <KPICard
                        title="Unique Online Users"
                        value={stats ? formatNumber(stats.onlineUsers) : '0'}
                        icon={Users}
                        iconColor="text-purple-500"
                        trend="Active users"
                        tooltip="Number of distinct users currently logged in with at least one active session."
                    />
                </div>

                {/* Charts Row */}
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
                    {/* Activity Trend Chart */}
                    <Card className="col-span-4 shadow-md hover:shadow-lg transition-shadow">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <History className="h-4 w-4 text-primary" />
                                Session Activity (Last 7 Days)
                            </CardTitle>
                            <CardDescription>Number of new sessions created daily</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="h-[300px]">
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={trendData}>
                                        <defs>
                                            <linearGradient id="colorSessions" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.8} />
                                                <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                                        <XAxis
                                            dataKey="date"
                                            stroke="#6B7280"
                                            fontSize={12}
                                            tickLine={false}
                                            axisLine={false}
                                            dy={10}
                                        />
                                        <YAxis
                                            stroke="#6B7280"
                                            fontSize={12}
                                            tickLine={false}
                                            axisLine={false}
                                        />
                                        <Tooltip
                                            contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                                            cursor={{ stroke: '#8b5cf6', strokeWidth: 2 }}
                                        />
                                        <Area
                                            type="monotone"
                                            dataKey="sessions"
                                            stroke="#8b5cf6"
                                            strokeWidth={3}
                                            fillOpacity={1}
                                            fill="url(#colorSessions)"
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </CardContent>
                    </Card>

                    {/* Device Distribution Chart */}
                    <Card className="col-span-3 shadow-md hover:shadow-lg transition-shadow">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Laptop className="h-4 w-4 text-blue-500" />
                                Active Devices
                            </CardTitle>
                            <CardDescription>Distribution by browser/OS</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div className="h-[300px] flex items-center justify-center">
                                {deviceData.length > 0 ? (
                                    <ResponsiveContainer width="100%" height="100%">
                                        <PieChart>
                                            <Pie
                                                data={deviceData}
                                                cx="50%"
                                                cy="50%"
                                                innerRadius={60}
                                                outerRadius={100}
                                                paddingAngle={2}
                                                dataKey="value"
                                            >
                                                {deviceData.map((entry, index) => (
                                                    <Cell
                                                        key={`cell-${index}`}
                                                        fill={DEVICE_COLORS[entry.name] || DEFAULT_COLORS[index % DEFAULT_COLORS.length]}
                                                    />
                                                ))}
                                            </Pie>
                                            <Tooltip
                                                formatter={(value: number) => [value, 'Sessions']}
                                                contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e2e8f0' }}
                                            />
                                            <Legend verticalAlign="bottom" height={36} />
                                        </PieChart>
                                    </ResponsiveContainer>
                                ) : (
                                    <div className="text-center text-muted-foreground">No active sessions to display</div>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            {/* Current Session Info Section */}
            <section className="space-y-4">
                <div className="flex items-center gap-2 mb-4">
                    <Lock className="h-5 w-5 text-primary" />
                    <h2 className="text-xl font-semibold">My Current Session Context</h2>
                </div>

                <div className="grid gap-6 md:grid-cols-2">
                    <Card className="md:col-span-1 border-purple-200 dark:border-purple-800/50 bg-white dark:bg-slate-900 shadow-sm">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Monitor className="h-5 w-5 text-purple-600" />
                                Current Access Token
                            </CardTitle>
                            <CardDescription>
                                Your JWT credential for API access
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {tokenDetails ? (
                                <>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div>
                                            <p className="text-xs font-medium text-muted-foreground uppercase">Issued</p>
                                            <p className="text-sm font-medium">{tokenDetails.iat ? new Date(tokenDetails.iat * 1000).toLocaleTimeString() : 'N/A'}</p>
                                        </div>
                                        <div>
                                            <p className="text-xs font-medium text-muted-foreground uppercase">Expires</p>
                                            <p className="text-sm font-medium">{tokenDetails.exp ? new Date(tokenDetails.exp * 1000).toLocaleTimeString() : 'N/A'}</p>
                                        </div>
                                    </div>

                                    <div className="bg-purple-50 dark:bg-purple-950/50 p-4 rounded-xl border border-purple-200 dark:border-purple-800/50 text-center shadow-sm">
                                        <p className="text-xs text-muted-foreground uppercase tracking-widest mb-1">Session Expires In</p>
                                        <p className="text-3xl font-mono font-bold text-primary tracking-tight">{timeRemaining}</p>
                                    </div>

                                    <div>
                                        <p className="text-xs font-medium text-muted-foreground mb-1 uppercase">User Subject</p>
                                        <p className="font-mono text-xs bg-slate-100 dark:bg-slate-800 p-2 rounded truncate border border-slate-200 dark:border-slate-700">{tokenDetails.sub}</p>
                                    </div>
                                </>
                            ) : (
                                <p className="text-sm text-muted-foreground">No active access token found.</p>
                            )}
                        </CardContent>
                    </Card>

                    <Card className="md:col-span-1 border-blue-200 dark:border-blue-800/50 bg-white dark:bg-slate-900 shadow-sm">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <RefreshCw className="h-5 w-5 text-blue-600" />
                                Refresh Token Status
                            </CardTitle>
                            <CardDescription>
                                Long-lived session credential
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {refreshTokenDetails ? (
                                <>
                                    <div className="flex items-center gap-2 bg-green-500/10 text-green-700 border border-green-200 px-4 py-3 rounded-xl w-full">
                                        <CheckCircle className="h-5 w-5" />
                                        <span className="text-sm font-semibold">Active & Valid</span>
                                    </div>

                                    <div>
                                        <p className="text-xs font-medium text-muted-foreground mb-1 uppercase">Token ID (Opaque)</p>
                                        <p className="font-mono text-xs bg-slate-100 dark:bg-slate-800 p-2 rounded break-all border border-slate-200 dark:border-slate-700 text-muted-foreground">
                                            {refreshTokenDetails.token}
                                        </p>
                                    </div>

                                    <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-xl p-4 flex items-start gap-3">
                                        <AlertTriangle className="h-5 w-5 text-yellow-600 mt-0.5" />
                                        <div>
                                            <h4 className="text-sm font-semibold text-yellow-700">Security Note</h4>
                                            <p className="text-xs text-muted-foreground mt-1">
                                                This token allows generating new access tokens without re-login. If compromised, revoke immediately.
                                            </p>
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <div className="flex items-center gap-2 bg-red-500/10 text-red-600 px-3 py-2 rounded-lg w-fit">
                                    <AlertTriangle className="h-4 w-4" />
                                    <span className="text-sm font-medium">Not Found</span>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            </section>
        </div>
    )
}

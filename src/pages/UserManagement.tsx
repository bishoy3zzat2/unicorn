import { UsersTable } from '../components/dashboard/UsersTable'
import { Users, Briefcase, Building2, UserCheck } from 'lucide-react'
import { useState, useEffect } from 'react'
import api from '../lib/axios'
import { formatNumber } from '../lib/utils'

interface UserStats {
    total: { value: number; newThisMonth: number }
    investors: { value: number; verifiedCount: number }
    startups: { value: number; totalRaised: number }
    active: { value: number; onlineNow: number }
}

export function UserManagement() {
    const [stats, setStats] = useState<UserStats>({
        total: { value: 0, newThisMonth: 0 },
        investors: { value: 0, verifiedCount: 0 },
        startups: { value: 0, totalRaised: 0 },
        active: { value: 0, onlineNow: 0 }
    })
    const [isLoading, setIsLoading] = useState(true)

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await api.get('/admin/users/stats')
                setStats(response.data)
            } catch (error) {
                console.error('Failed to fetch user stats', error)
            } finally {
                setIsLoading(false)
            }
        }
        fetchStats()
    }, [])

    return (
        <div className="space-y-6">


            {/* Statistics Cards */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">

                {/* Total Users */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Users className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{isLoading ? "..." : formatNumber(stats.total.value)}</div>
                    <div className="text-blue-100 text-sm">Total Users</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">
                            <strong className="text-white">+{stats.total.newThisMonth}</strong> new users this month
                        </span>
                    </div>
                </div>

                {/* Investors */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Briefcase className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{isLoading ? "..." : formatNumber(stats.investors.value)}</div>
                    <div className="text-emerald-100 text-sm">Investors</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <UserCheck className="h-4 w-4 opacity-70" />
                        <span className="text-sm">
                            <strong>{stats.investors.verifiedCount}</strong> verified
                            {stats.investors.value > 0 &&
                                <span className="opacity-80 ml-1">({Math.round((stats.investors.verifiedCount / stats.investors.value) * 100)}%)</span>
                            }
                        </span>
                    </div>
                </div>

                {/* Startups */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-purple-500 to-violet-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Building2 className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{isLoading ? "..." : formatNumber(stats.startups.value)}</div>
                    <div className="text-purple-100 text-sm">Startups</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">
                            Total Raised: <strong>${formatNumber(stats.startups.totalRaised)}</strong>
                        </span>
                    </div>
                </div>

                {/* Active Users */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-orange-500 to-amber-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <UserCheck className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{isLoading ? "..." : formatNumber(stats.active.value)}</div>
                    <div className="text-orange-100 text-sm">Active Users</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <div className="h-2 w-2 rounded-full bg-green-400 animate-pulse" />
                        <span className="text-sm">
                            <strong>{stats.active.onlineNow}</strong> online now
                        </span>
                    </div>
                </div>

            </div>

            {/* Users Table */}
            <UsersTable />
        </div>
    )
}

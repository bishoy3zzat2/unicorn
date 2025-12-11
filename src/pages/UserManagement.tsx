import { UsersTable } from '../components/dashboard/UsersTable'
import { KPICard } from '../components/dashboard/KPICard'
import { Users, Briefcase, Building2, UserCheck } from 'lucide-react'
import { useState, useEffect } from 'react'
import api from '../lib/axios'
import { formatNumber } from '../lib/utils'

interface UserStats {
    total: number
    investors: number
    startups: number
    active: number
}

export function UserManagement() {
    const [stats, setStats] = useState<UserStats>({
        total: 0,
        investors: 0,
        startups: 0,
        active: 0
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
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">User Management</h1>
                <p className="text-muted-foreground mt-2">
                    Manage investors and startups on the platform
                </p>
            </div>

            {/* Statistics Cards */}
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                <KPICard
                    title="Total Users"
                    value={isLoading ? "..." : formatNumber(stats.total)}
                    icon={Users}
                    iconColor="text-blue-500"
                />
                <KPICard
                    title="Investors"
                    value={isLoading ? "..." : formatNumber(stats.investors)}
                    icon={Briefcase}
                    iconColor="text-emerald-500"
                />
                <KPICard
                    title="Startups"
                    value={isLoading ? "..." : formatNumber(stats.startups)}
                    icon={Building2}
                    iconColor="text-purple-500"
                />
                <KPICard
                    title="Active Users"
                    value={isLoading ? "..." : formatNumber(stats.active)}
                    icon={UserCheck}
                    iconColor="text-orange-500"
                />
            </div>

            {/* Users Table */}
            <UsersTable />
        </div>
    )
}

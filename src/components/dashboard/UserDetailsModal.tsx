import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog'
import { Button } from '../ui/button'
import { Badge } from '../ui/badge'
import { Separator } from '../ui/separator'
import {
    Loader2, User, Mail, Phone, MapPin, Calendar, Shield,
    Building2, TrendingUp, CreditCard, Clock, AlertTriangle,
    Ban, CheckCircle, XCircle, DollarSign, Briefcase,
    Monitor, Globe
} from 'lucide-react'
import { formatDate } from '../../lib/utils'
import api from '../../lib/axios'
import { toast } from 'sonner'

interface UserDetailsModalProps {
    userId: string | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onAction?: () => void
}

interface UserDetails {
    id: string
    email: string
    username?: string
    firstName?: string
    lastName?: string
    displayName?: string
    phoneNumber?: string
    country?: string
    avatarUrl?: string
    role: string
    status: string
    authProvider: string
    createdAt: string
    updatedAt?: string
    lastLoginAt?: string
    passwordChangedAt?: string
    suspendedAt?: string
    suspendReason?: string
    suspendedUntil?: string
    suspensionType?: string
    deletedAt?: string
    deletionReason?: string
    warningCount: number
    startupCount: number
    hasInvestorProfile: boolean
    isInvestorVerified: boolean
    hasActiveSession?: boolean
    currentSubscription?: {
        plan: string
        status: string
        amount: number
        startDate?: string
        endDate?: string
    }
    investorInfo?: {
        id: string
        bio?: string
        investmentBudget?: number
        preferredIndustries?: string
        linkedInUrl?: string
        isVerified: boolean
        verifiedAt?: string
    }
    startups?: Array<{
        id: string
        name: string
        industry?: string
        stage?: string
        status?: string
        raisedAmount?: number
        createdAt?: string
    }>
    recentTransactions?: Array<{
        transactionId: string
        amount: number
        currency?: string
        status?: string
        description?: string
        paymentMethod?: string
        timestamp?: string
    }>
    moderationHistory?: Array<{
        id: string
        actionType: string
        reason?: string
        adminEmail?: string
        createdAt: string
        expiresAt?: string
        isActive: boolean
    }>
}

export function UserDetailsModal({ userId, open, onOpenChange, onAction }: UserDetailsModalProps) {
    const [loading, setLoading] = useState(false)
    const [userDetails, setUserDetails] = useState<UserDetails | null>(null)
    const [activeTab, setActiveTab] = useState<'info' | 'startups' | 'transactions' | 'history' | 'security'>('info')

    useEffect(() => {
        if (open && userId) {
            fetchUserDetails()
        }
    }, [open, userId])

    async function fetchUserDetails() {
        if (!userId) return
        setLoading(true)
        try {
            const response = await api.get(`/admin/users/${userId}/details`)
            setUserDetails(response.data)
        } catch (error) {
            console.error('Failed to fetch user details:', error)
            toast.error('Failed to load user details')
        } finally {
            setLoading(false)
        }
    }

    async function handleUnsuspend() {
        if (!userId) return
        try {
            await api.post(`/admin/users/${userId}/unsuspend`, {
                reason: 'Unsuspended by admin'
            })
            toast.success('User unsuspended successfully')
            fetchUserDetails()
            onAction?.()
        } catch (error) {
            console.error('Failed to unsuspend user:', error)
            toast.error('Failed to unsuspend user')
        }
    }



    const getStatusBadge = (status: string) => {
        const styles: Record<string, string> = {
            ACTIVE: 'bg-green-500/10 text-green-500 border-green-500/30',
            SUSPENDED: 'bg-orange-500/10 text-orange-500 border-orange-500/30',
            BANNED: 'bg-red-500/10 text-red-500 border-red-500/30',
            DELETED: 'bg-gray-500/10 text-gray-500 border-gray-500/30',
            PENDING: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/30',
        }
        return (
            <span className={`px-3 py-1 rounded-full text-sm font-semibold border ${styles[status] || styles.PENDING}`}>
                {status}
            </span>
        )
    }

    const getPlanBadge = (plan: string) => {
        const styles: Record<string, string> = {
            FREE: 'bg-gray-500/10 text-gray-500',
            PRO: 'bg-blue-500/10 text-blue-500',
            ELITE: 'bg-purple-500/10 text-purple-500',
        }
        return (
            <span className={`px-2 py-1 rounded text-xs font-semibold ${styles[plan] || styles.FREE}`}>
                {plan}
            </span>
        )
    }

    const tabs: Array<{ id: 'info' | 'startups' | 'transactions' | 'history' | 'security'; label: string; icon: React.ElementType; count?: number }> = [
        { id: 'info', label: 'Info', icon: User },
        { id: 'startups', label: 'Startups', icon: Building2, count: userDetails?.startups?.length },
        { id: 'transactions', label: 'Transactions', icon: CreditCard, count: userDetails?.recentTransactions?.length },
        { id: 'history', label: 'History', icon: Clock, count: userDetails?.moderationHistory?.length },
        { id: 'security', label: 'Security', icon: Shield },
    ]

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
                <DialogHeader>
                    <DialogTitle>User Details</DialogTitle>
                </DialogHeader>

                {loading ? (
                    <div className="flex items-center justify-center py-12">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                    </div>
                ) : !userDetails ? (
                    <div className="text-center py-8 text-muted-foreground">
                        User not found
                    </div>
                ) : (
                    <div className="flex flex-col flex-1 overflow-hidden">
                        {/* Header with basic info */}
                        <div className="flex items-start justify-between p-4 bg-muted/30 rounded-lg mb-4">
                            <div className="flex items-center gap-4">
                                {/* Avatar with status indicator */}
                                <div className="relative">
                                    <img
                                        src={userDetails.avatarUrl || `https://api.dicebear.com/7.x/initials/svg?seed=${userDetails.email}`}
                                        alt={userDetails.displayName || userDetails.email}
                                        className="h-16 w-16 rounded-full border-2 border-primary/20"
                                    />
                                    <div className={`absolute -bottom-0.5 -right-0.5 h-5 w-5 rounded-full border-2 border-background ${userDetails.hasActiveSession ? 'bg-green-500' : 'bg-gray-500'
                                        }`} />
                                </div>
                                <div>
                                    <h3 className="text-lg font-bold">
                                        {userDetails.displayName || userDetails.email.split('@')[0]}
                                    </h3>
                                    <p className="text-sm text-muted-foreground">{userDetails.email}</p>
                                    <div className="flex items-center gap-2 mt-1">
                                        <Badge variant="outline">{userDetails.role}</Badge>
                                        {getStatusBadge(userDetails.status)}
                                        {getPlanBadge(userDetails.currentSubscription?.plan || 'FREE')}
                                    </div>
                                </div>
                            </div>
                            {(userDetails.status === 'SUSPENDED' || userDetails.status === 'BANNED') && (
                                <Button variant="outline" onClick={handleUnsuspend} size="sm">
                                    <CheckCircle className="h-4 w-4 mr-2" />
                                    Unsuspend
                                </Button>
                            )}
                        </div>

                        {/* Tabs */}
                        <div className="flex border-b mb-4">
                            {tabs.map(tab => (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`flex items-center gap-2 px-4 py-2 text-sm font-medium border-b-2 transition-colors ${activeTab === tab.id
                                        ? 'border-primary text-primary'
                                        : 'border-transparent text-muted-foreground hover:text-foreground'
                                        }`}
                                >
                                    <tab.icon className="h-4 w-4" />
                                    {tab.label}
                                    {tab.count !== undefined && tab.count > 0 && (
                                        <span className="ml-1 px-1.5 py-0.5 text-xs bg-muted rounded-full">
                                            {tab.count}
                                        </span>
                                    )}
                                </button>
                            ))}
                        </div>

                        {/* Tab Content */}
                        <div className="flex-1 overflow-y-auto pr-2">
                            {/* Info Tab */}
                            {activeTab === 'info' && (
                                <div className="space-y-4">
                                    {/* Contact Info */}
                                    <div className="grid grid-cols-2 gap-4">
                                        <InfoRow icon={User} label="Username" value={userDetails.username || userDetails.email.split('@')[0]} />
                                        <InfoRow icon={Mail} label="Email" value={userDetails.email} />
                                        <InfoRow icon={User} label="First Name" value={userDetails.firstName || '-'} />
                                        <InfoRow icon={User} label="Last Name" value={userDetails.lastName || '-'} />
                                        <InfoRow icon={Phone} label="Phone" value={userDetails.phoneNumber || 'Not provided'} />
                                        <InfoRow icon={MapPin} label="Country" value={userDetails.country || 'Not specified'} />
                                        <InfoRow icon={Shield} label="Auth Provider" value={userDetails.authProvider} />
                                    </div>

                                    <Separator />

                                    {/* Timestamps */}
                                    <div className="grid grid-cols-2 gap-4">
                                        <InfoRow icon={Calendar} label="Joined" value={formatDate(userDetails.createdAt)} />
                                        <InfoRow icon={Clock} label="Last Login" value={userDetails.lastLoginAt ? formatDate(userDetails.lastLoginAt) : 'Never'} />
                                    </div>

                                    {/* Suspension Info */}
                                    {userDetails.status === 'SUSPENDED' && (
                                        <>
                                            <Separator />
                                            <div className="p-4 rounded-lg bg-orange-500/10 border border-orange-500/30">
                                                <h4 className="font-semibold text-orange-500 flex items-center gap-2">
                                                    <Ban className="h-4 w-4" />
                                                    Suspension Details
                                                </h4>
                                                <div className="mt-2 space-y-1 text-sm">
                                                    <p><strong>Type:</strong> {userDetails.suspensionType || 'Temporary'}</p>
                                                    <p><strong>Since:</strong> {userDetails.suspendedAt ? formatDate(userDetails.suspendedAt) : '-'}</p>
                                                    {userDetails.suspendedUntil && (
                                                        <p><strong>Until:</strong> {formatDate(userDetails.suspendedUntil)}</p>
                                                    )}
                                                    <p><strong>Reason:</strong> {userDetails.suspendReason || '-'}</p>
                                                </div>
                                            </div>
                                        </>
                                    )}

                                    {/* Investor Profile */}
                                    {userDetails.investorInfo && (
                                        <>
                                            <Separator />
                                            <div className="p-4 rounded-lg bg-emerald-500/10 border border-emerald-500/30">
                                                <h4 className="font-semibold text-emerald-500 flex items-center gap-2">
                                                    <TrendingUp className="h-4 w-4" />
                                                    Investor Profile
                                                    {userDetails.investorInfo.isVerified ? (
                                                        <CheckCircle className="h-4 w-4 text-green-500" />
                                                    ) : (
                                                        <XCircle className="h-4 w-4 text-red-500" />
                                                    )}
                                                </h4>
                                                <div className="mt-2 space-y-1 text-sm">
                                                    <p><strong>Budget:</strong> ${userDetails.investorInfo.investmentBudget?.toLocaleString() || 0}</p>
                                                    <p><strong>Industries:</strong> {userDetails.investorInfo.preferredIndustries || '-'}</p>
                                                    {userDetails.investorInfo.bio && (
                                                        <p><strong>Bio:</strong> {userDetails.investorInfo.bio}</p>
                                                    )}
                                                </div>
                                            </div>
                                        </>
                                    )}

                                    {/* Stats */}
                                    <div className="grid grid-cols-3 gap-4">
                                        <StatCard
                                            icon={Building2}
                                            label="Startups"
                                            value={userDetails.startupCount}
                                            color="purple"
                                        />
                                        <StatCard
                                            icon={AlertTriangle}
                                            label="Warnings"
                                            value={userDetails.warningCount}
                                            color="yellow"
                                        />
                                        <StatCard
                                            icon={DollarSign}
                                            label="Plan"
                                            value={userDetails.currentSubscription?.plan || 'FREE'}
                                            color="blue"
                                        />
                                    </div>
                                </div>
                            )}

                            {/* Startups Tab */}
                            {activeTab === 'startups' && (
                                <div className="space-y-3">
                                    {userDetails.startups && userDetails.startups.length > 0 ? (
                                        userDetails.startups.map(startup => (
                                            <div key={startup.id} className="p-4 rounded-lg border bg-card">
                                                <div className="flex items-start justify-between">
                                                    <div>
                                                        <h4 className="font-semibold flex items-center gap-2">
                                                            <Building2 className="h-4 w-4 text-purple-500" />
                                                            {startup.name}
                                                        </h4>
                                                        <p className="text-sm text-muted-foreground">{startup.industry}</p>
                                                    </div>
                                                    <Badge variant="outline">{startup.status}</Badge>
                                                </div>
                                                <div className="mt-2 flex items-center gap-4 text-xs text-muted-foreground">
                                                    <span>Stage: {startup.stage}</span>
                                                    <span>Raised: ${startup.raisedAmount?.toLocaleString() || 0}</span>
                                                    {startup.createdAt && <span>Created: {formatDate(startup.createdAt)}</span>}
                                                </div>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="text-center py-8 text-muted-foreground">
                                            <Briefcase className="h-12 w-12 mx-auto mb-2 opacity-50" />
                                            <p>No startups associated</p>
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* Transactions Tab */}
                            {activeTab === 'transactions' && (
                                <div className="space-y-3">
                                    {userDetails.recentTransactions && userDetails.recentTransactions.length > 0 ? (
                                        userDetails.recentTransactions.map(tx => (
                                            <div key={tx.transactionId} className="p-4 rounded-lg border bg-card">
                                                <div className="flex items-start justify-between">
                                                    <div>
                                                        <h4 className="font-semibold flex items-center gap-2">
                                                            <CreditCard className="h-4 w-4 text-blue-500" />
                                                            {tx.description || 'Payment'}
                                                        </h4>
                                                        <p className="text-sm text-muted-foreground">{tx.transactionId}</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="font-bold text-green-500">
                                                            ${tx.amount?.toLocaleString()} {tx.currency}
                                                        </p>
                                                        <Badge variant="outline" className="text-xs">{tx.status}</Badge>
                                                    </div>
                                                </div>
                                                <div className="mt-2 flex items-center gap-4 text-xs text-muted-foreground">
                                                    <span>Method: {tx.paymentMethod || '-'}</span>
                                                    {tx.timestamp && <span>{formatDate(tx.timestamp)}</span>}
                                                </div>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="text-center py-8 text-muted-foreground">
                                            <CreditCard className="h-12 w-12 mx-auto mb-2 opacity-50" />
                                            <p>No transactions found</p>
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* History Tab */}
                            {activeTab === 'history' && (
                                <div className="space-y-3">
                                    {userDetails.moderationHistory && userDetails.moderationHistory.length > 0 ? (
                                        userDetails.moderationHistory.map(log => (
                                            <div key={log.id} className="p-4 rounded-lg border bg-card">
                                                <div className="flex items-start justify-between">
                                                    <div>
                                                        <h4 className="font-semibold flex items-center gap-2">
                                                            <ActionIcon type={log.actionType} />
                                                            {log.actionType.replace('_', ' ')}
                                                        </h4>
                                                        <p className="text-sm text-muted-foreground">{log.reason}</p>
                                                    </div>
                                                    <Badge variant={log.isActive ? 'default' : 'outline'}>
                                                        {log.isActive ? 'Active' : 'Resolved'}
                                                    </Badge>
                                                </div>
                                                <div className="mt-2 flex items-center gap-4 text-xs text-muted-foreground">
                                                    <span>By: {log.adminEmail}</span>
                                                    <span>{formatDate(log.createdAt)}</span>
                                                </div>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="text-center py-8 text-muted-foreground">
                                            <Clock className="h-12 w-12 mx-auto mb-2 opacity-50" />
                                            <p>No moderation history</p>
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* Security Tab */}
                            {activeTab === 'security' && (
                                <SecurityTab userId={userId} />
                            )}
                        </div>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    )
}

// Helper Components
function InfoRow({ icon: Icon, label, value }: { icon: React.ElementType, label: string, value: string }) {
    return (
        <div className="flex items-center gap-3">
            <Icon className="h-4 w-4 text-muted-foreground" />
            <div>
                <p className="text-xs text-muted-foreground">{label}</p>
                <p className="text-sm font-medium">{value}</p>
            </div>
        </div>
    )
}

function StatCard({ icon: Icon, label, value, color }: { icon: React.ElementType, label: string, value: number | string, color: string }) {
    const colors: Record<string, string> = {
        purple: 'bg-purple-500/10 text-purple-500',
        yellow: 'bg-yellow-500/10 text-yellow-500',
        blue: 'bg-blue-500/10 text-blue-500',
        green: 'bg-green-500/10 text-green-500',
    }
    return (
        <div className={`p-4 rounded-lg ${colors[color]} text-center`}>
            <Icon className="h-6 w-6 mx-auto mb-1" />
            <p className="text-2xl font-bold">{value}</p>
            <p className="text-xs">{label}</p>
        </div>
    )
}

function ActionIcon({ type }: { type: string }) {
    const icons: Record<string, React.ReactNode> = {
        WARNING: <AlertTriangle className="h-4 w-4 text-yellow-500" />,
        SUSPENSION: <Ban className="h-4 w-4 text-orange-500" />,
        PERMANENT_BAN: <XCircle className="h-4 w-4 text-red-500" />,
        UNSUSPEND: <CheckCircle className="h-4 w-4 text-green-500" />,
        DELETE: <XCircle className="h-4 w-4 text-red-500" />,
    }
    return icons[type] || <Clock className="h-4 w-4 text-muted-foreground" />
}

function SecurityTab({ userId }: { userId: string | null }) {
    const [sessions, setSessions] = useState<any[]>([])
    const [loadingSessions, setLoadingSessions] = useState(false)

    const fetchSessions = async () => {
        if (!userId) return
        setLoadingSessions(true)
        try {
            const res = await api.get(`/admin/users/${userId}/sessions`)
            setSessions(res.data)
        } catch (err) {
            console.error("Failed to fetch sessions", err)
        } finally {
            setLoadingSessions(false)
        }
    }

    useEffect(() => {
        fetchSessions()
    }, [userId])

    const handleRevokeAll = async () => {
        if (!confirm("Are you sure you want to log out this user from ALL devices?")) return
        try {
            await api.delete(`/admin/users/${userId}/sessions`)
            toast.success("All sessions revoked")
            fetchSessions()
        } catch (err) {
            toast.error("Failed to revoke sessions")
        }
    }

    const handleRevokeSession = async (sessionId: number) => {
        try {
            await api.delete(`/admin/users/${userId}/sessions/${sessionId}`)
            toast.success("Session revoked")
            fetchSessions()
        } catch (err) {
            toast.error("Failed to revoke session")
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h3 className="text-lg font-medium">Active Sessions</h3>
                    <p className="text-sm text-muted-foreground">
                        Manage user's active login sessions and devices
                    </p>
                </div>
                <Button
                    variant="destructive"
                    size="sm"
                    onClick={handleRevokeAll}
                    disabled={sessions.length === 0}
                >
                    <Shield className="h-4 w-4 mr-2" />
                    Force Logout All
                </Button>
            </div>

            {loadingSessions ? (
                <div className="flex justify-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                </div>
            ) : sessions.length > 0 ? (
                <div className="border rounded-md divide-y">
                    {sessions.map((session) => (
                        <div key={session.id} className="p-4 flex items-center justify-between transition-colors hover:bg-muted/50">
                            <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                    <Monitor className="h-4 w-4 text-muted-foreground" />
                                    <span className="font-medium text-sm">{session.device}</span>
                                    {session.isCurrent && (
                                        <Badge variant="outline" className="text-xs bg-green-500/10 text-green-600 border-green-200">Current</Badge>
                                    )}
                                </div>
                                <div className="flex items-center gap-4 text-xs text-muted-foreground">
                                    <div className="flex items-center gap-1">
                                        <Globe className="h-3 w-3" />
                                        {session.ipAddress || 'Unknown IP'}
                                    </div>
                                    <div className="flex items-center gap-1">
                                        <Clock className="h-3 w-3" />
                                        Last active: {session.lastUsedAt ? formatDate(session.lastUsedAt) : 'N/A'}
                                    </div>
                                </div>
                            </div>
                            <Button
                                variant="ghost"
                                size="sm"
                                className="text-red-500 hover:text-red-600 hover:bg-red-500/10"
                                onClick={() => handleRevokeSession(session.id)}
                            >
                                Revoke
                            </Button>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="text-center py-8 text-muted-foreground bg-muted/20 rounded-lg">
                    <Shield className="h-8 w-8 mx-auto mb-2 opacity-50" />
                    <p>No active sessions found</p>
                </div>
            )}
        </div>
    )
}

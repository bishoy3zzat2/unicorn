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
    Monitor, Globe, AlertCircle, Eye
} from 'lucide-react'
import { formatDate } from '../../lib/utils'
import api from '../../lib/axios'
import { toast } from 'sonner'
import { useAuth } from '../../contexts/AuthContext'
import { getStartupById } from '../../lib/api'
import { StartupDetailsDialog } from './StartupDetailsDialog'
import { Startup } from '../../types'

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
    bio?: string
    linkedInUrl?: string
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
        preferredStage?: string
        linkedInUrl?: string
        isVerified: boolean
        verifiedAt?: string
        readyForPayment: boolean
    }
    startups?: Array<{
        id: string
        name: string
        industry?: string
        stage?: string
        role?: string
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
    const [selectedStartup, setSelectedStartup] = useState<Startup | null>(null)
    const [isStartupDetailsOpen, setIsStartupDetailsOpen] = useState(false)

    const { user: currentUser } = useAuth()
    const isSuperAdmin = currentUser?.role === 'SUPER_ADMIN'

    const canManageUser = userDetails ? (
        userDetails.role === 'SUPER_ADMIN' ? false :
            userDetails.role === 'ADMIN' ? isSuperAdmin :
                true
    ) : false

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
            const data = response.data as UserDetails
            console.log('User Details API Response:', data)
            console.log('hasActiveSession value:', data.hasActiveSession, 'type:', typeof data.hasActiveSession)
            console.log('investorInfo:', data.investorInfo)
            console.log('hasInvestorProfile:', data.hasInvestorProfile)
            console.log('role:', data.role)
            setUserDetails(data)
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

    const handleViewStartup = async (startupId: string) => {
        try {
            const startup = await getStartupById(startupId)
            setSelectedStartup(startup)
            setIsStartupDetailsOpen(true)
        } catch (error) {
            console.error('Failed to fetch startup details:', error)
            toast.error('Failed to load startup details')
        }
    }



    const getStatusBadge = (status: string) => {
        const styles: Record<string, string> = {
            ACTIVE: 'bg-green-500/10 text-green-500 border-green-500/30',
            APPROVED: 'bg-green-500/10 text-green-500 border-green-500/30',
            SUSPENDED: 'bg-orange-500/10 text-orange-500 border-orange-500/30',
            BANNED: 'bg-red-500/10 text-red-500 border-red-500/30',
            REJECTED: 'bg-red-500/10 text-red-500 border-red-500/30',
            DELETED: 'bg-gray-500/10 text-gray-500 border-gray-500/30',
            PENDING: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/30',
            PENDING_VERIFICATION: 'bg-blue-500/10 text-blue-500 border-blue-500/30',
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-semibold border ${styles[status] || styles.PENDING}`}>
                {status}
            </span>
        )
    }

    const getRoleBadge = (role: string) => {
        const styles: Record<string, string> = {
            ADMIN: 'bg-yellow-500/10 text-yellow-600 border-yellow-500/30',
            INVESTOR: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/30',
            STARTUP_OWNER: 'bg-purple-500/10 text-purple-600 border-purple-500/30',
            USER: 'bg-blue-500/10 text-blue-600 border-blue-500/30',
            SUPER_ADMIN: 'bg-purple-600/10 text-purple-700 border-purple-600/30', // Special style for Super Admin
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium border ${styles[role] || styles.USER}`}>
                {role.replace('_', ' ')}
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
                                    <div className={`absolute -bottom-0.5 -right-0.5 h-4 w-4 rounded-full border-2 border-background ring-1 ring-background ${userDetails.hasActiveSession ? 'bg-green-500' : 'bg-gray-300'
                                        }`} title={userDetails.hasActiveSession ? "Online" : "Offline"} />
                                </div>
                                <div>
                                    <h3 className="text-lg font-bold">
                                        {userDetails.displayName || userDetails.email.split('@')[0]}
                                    </h3>
                                    <p className="text-sm text-muted-foreground">{userDetails.email}</p>
                                    <div className="flex items-center gap-2 mt-1">
                                        {getRoleBadge(userDetails.role)}
                                        {getStatusBadge(userDetails.status)}
                                        {/* Plan for Non-Investors/Non-Admins, Verification for Investors */}
                                        {userDetails.role === 'INVESTOR' ? (
                                            userDetails.investorInfo?.isVerified ? (
                                                <Badge className="bg-emerald-500/10 text-emerald-600 hover:bg-emerald-500/20 border-emerald-200">
                                                    <CheckCircle className="h-3 w-3 mr-1" />
                                                    Verified Investor
                                                </Badge>
                                            ) : (
                                                <Badge variant="outline" className="text-muted-foreground border-dashed">
                                                    Not Verified
                                                </Badge>
                                            )
                                        ) : userDetails.role === 'ADMIN' ? (
                                            <Badge className="bg-purple-500/10 text-purple-600 hover:bg-purple-500/20 border-purple-200">
                                                <Shield className="h-3 w-3 mr-1" />
                                                Super Admin
                                            </Badge>
                                        ) : (
                                            getPlanBadge(userDetails.currentSubscription?.plan || 'FREE')
                                        )}
                                    </div>
                                </div>
                            </div>
                            {(userDetails.status === 'SUSPENDED' || userDetails.status === 'BANNED') && canManageUser && (
                                <Button variant="outline" onClick={handleUnsuspend} size="sm">
                                    <CheckCircle className="h-4 w-4 mr-2" />
                                    Unsuspend
                                </Button>
                            )}
                        </div>

                        {/* Tabs */}
                        <div className="flex border-b mb-4">
                            {tabs.filter(tab => {
                                // Filter out tabs based on role
                                if (userDetails.role === 'ADMIN' || userDetails.role === 'SUPER_ADMIN') {
                                    return ['info', 'history', 'security'].includes(tab.id)
                                }
                                return true
                            }).map(tab => {
                                // Rename Startups to Deals for Investors
                                if (tab.id === 'startups' && userDetails.role === 'INVESTOR') {
                                    return (
                                        <button
                                            key={tab.id}
                                            onClick={() => setActiveTab(tab.id)}
                                            className={`flex items-center gap-2 px-4 py-2 text-sm font-medium border-b-2 transition-colors ${activeTab === tab.id
                                                ? 'border-primary text-primary'
                                                : 'border-transparent text-muted-foreground hover:text-foreground'
                                                }`}
                                        >
                                            <TrendingUp className="h-4 w-4" /> {/* Use TrendingUp icon for Deals */}
                                            Deals
                                        </button>
                                    )
                                }
                                return (
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
                                )
                            })}
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
                                        {(userDetails.role !== 'ADMIN' && userDetails.role !== 'SUPER_ADMIN') && (
                                            <>
                                                <InfoRow icon={Phone} label="Phone" value={userDetails.phoneNumber || 'Not provided'} />
                                                <InfoRow icon={MapPin} label="Country" value={userDetails.country || 'Not specified'} />
                                                <InfoRow icon={Shield} label="Auth Provider" value={userDetails.authProvider} />
                                            </>
                                        )}
                                    </div>

                                    {/* Additional Info (Bio & LinkedIn) moved here for universal visibility - HIDDEN FOR ADMIN/SUPER_ADMIN */}
                                    {(userDetails.role !== 'ADMIN' && userDetails.role !== 'SUPER_ADMIN') && (
                                        <div className="grid grid-cols-1 gap-4 mt-4">
                                            <div className="flex items-center gap-3">
                                                <Globe className="h-4 w-4 text-muted-foreground" />
                                                <div>
                                                    <p className="text-xs text-muted-foreground">LinkedIn</p>
                                                    {userDetails.linkedInUrl ? (
                                                        <a
                                                            href={userDetails.linkedInUrl.startsWith('http') ? userDetails.linkedInUrl : `https://www.linkedin.com/in/${userDetails.linkedInUrl.replace(/^\/+/, '')}`}
                                                            target="_blank"
                                                            rel="noopener noreferrer"
                                                            className="text-sm font-medium text-blue-500 hover:underline"
                                                        >
                                                            {userDetails.linkedInUrl.replace(/^https?:\/\/(www\.)?linkedin\.com\/in\//, '').replace(/\/$/, '') || 'View Profile'}
                                                        </a>
                                                    ) : (
                                                        <p className="text-sm text-muted-foreground italic">Not provided</p>
                                                    )}
                                                </div>
                                            </div>

                                            <div className="flex items-start gap-3">
                                                <Briefcase className="h-4 w-4 text-muted-foreground mt-1" />
                                                <div>
                                                    <p className="text-xs text-muted-foreground">Bio</p>
                                                    <p className="text-sm text-foreground/80 whitespace-pre-wrap">
                                                        {userDetails.bio || <span className="text-muted-foreground italic">No bio provided</span>}
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    )}
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
                                    {userDetails.role === 'INVESTOR' && (
                                        <>
                                            <Separator />
                                            {userDetails.investorInfo ? (
                                                <div className="p-4 rounded-lg bg-emerald-500/10 border border-emerald-500/30">
                                                    <h4 className="font-semibold text-emerald-500 flex items-center gap-2">
                                                        <TrendingUp className="h-4 w-4" />
                                                        Investor Profile
                                                        {userDetails.investorInfo.isVerified ? (
                                                            <CheckCircle className="h-4 w-4 text-green-500" />
                                                        ) : (
                                                            <span className="text-muted-foreground text-xs font-normal border px-2 py-0.5 rounded-full">Not Verified</span>
                                                        )}
                                                    </h4>
                                                    <div className="mt-2 space-y-1 text-sm">
                                                        <p><strong>Investment Budget:</strong> ${userDetails.investorInfo.investmentBudget?.toLocaleString() || 0}</p>
                                                        <p><strong>Preferred Industries:</strong> {userDetails.investorInfo.preferredIndustries || '-'}</p>
                                                        <p><strong>Preferred Stage:</strong> {userDetails.investorInfo.preferredStage || 'All Stages'}</p>
                                                        <div className="flex items-center gap-2 mt-2 pt-2 border-t border-emerald-500/20">
                                                            <p><strong>Payment Status:</strong></p>
                                                            {userDetails.investorInfo.isVerified ? (
                                                                <Badge className="bg-green-500 text-white">Paid & Verified</Badge>
                                                            ) : userDetails.investorInfo.readyForPayment ? (
                                                                <Badge className="bg-blue-500 text-white">Ready for Payment</Badge>
                                                            ) : (
                                                                <div className="flex items-center gap-2">
                                                                    <Badge variant="outline" className="text-muted-foreground bg-gray-500/10">Pending Approval</Badge>
                                                                    <Button
                                                                        size="sm"
                                                                        variant="outline"
                                                                        className="h-6 text-xs border-emerald-500/50 text-emerald-600 hover:bg-emerald-500/10"
                                                                        onClick={async () => {
                                                                            if (!confirm('Approve this investor for payment?')) return;
                                                                            try {
                                                                                await api.put(`/admin/users/${userId}/approve-payment`);
                                                                                toast.success('Investor approved for payment');
                                                                                fetchUserDetails();
                                                                            } catch (e) {
                                                                                toast.error('Failed to approve payment');
                                                                            }
                                                                        }}
                                                                    >
                                                                        Approve
                                                                    </Button>
                                                                </div>
                                                            )}
                                                        </div>
                                                        {userDetails.investorInfo.verifiedAt && (
                                                            <p><strong>Verified Since:</strong> {formatDate(userDetails.investorInfo.verifiedAt)}</p>
                                                        )}
                                                    </div>
                                                </div>
                                            ) : (
                                                <div className="p-4 rounded-lg bg-yellow-500/10 border border-yellow-500/30">
                                                    <h4 className="font-semibold text-yellow-500 flex items-center gap-2">
                                                        <AlertCircle className="h-4 w-4" />
                                                        Investor Profile Incomplete
                                                    </h4>
                                                    <p className="mt-2 text-sm text-muted-foreground">
                                                        This user is registered as an investor but hasn't completed their investor profile yet.
                                                    </p>
                                                </div>
                                            )}
                                        </>
                                    )}

                                    {/* Stats */}
                                    <div className="grid grid-cols-3 gap-4">
                                        {(userDetails.role !== 'ADMIN' && userDetails.role !== 'SUPER_ADMIN') && (
                                            <StatCard
                                                icon={Building2}
                                                label={userDetails.role === 'INVESTOR' ? 'Deals (Mock)' : 'Startups'}
                                                value={userDetails.role === 'INVESTOR' ? 0 : userDetails.startupCount}
                                                color="purple"
                                            />
                                        )}
                                        {(userDetails.role === 'ADMIN' || userDetails.role === 'SUPER_ADMIN') ? (
                                            // Admin specific stats (focus on History/Security)
                                            <>
                                                <StatCard
                                                    icon={Clock}
                                                    label="Admin Actions"
                                                    value={userDetails.moderationHistory?.length || 0}
                                                    color="purple"
                                                />
                                                <StatCard
                                                    icon={Shield}
                                                    label="Active Sessions"
                                                    value={userDetails.hasActiveSession ? 'Active' : 'Offline'}
                                                    color="green"
                                                />
                                                <StatCard
                                                    icon={AlertTriangle}
                                                    label="Warnings Received"
                                                    value={userDetails.warningCount}
                                                    color="yellow"
                                                />
                                            </>
                                        ) : (
                                            <>
                                                <StatCard
                                                    icon={AlertTriangle}
                                                    label="Warnings"
                                                    value={userDetails.warningCount}
                                                    color="yellow"
                                                />
                                                {/* Hide Plan Stat for Investors */}
                                                {userDetails.role !== 'INVESTOR' && (
                                                    <StatCard
                                                        icon={DollarSign}
                                                        label="Plan"
                                                        value={userDetails.currentSubscription?.plan || 'FREE'}
                                                        color="blue"
                                                    />
                                                )}
                                            </>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Startups/Deals Tab */}
                            {activeTab === 'startups' && (
                                <div className="space-y-4">
                                    {userDetails.role === 'INVESTOR' ? (
                                        <div className="text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border border-dashed">
                                            <TrendingUp className="h-12 w-12 mx-auto mb-2 opacity-50 text-emerald-500" />
                                            <h4 className="text-lg font-semibold text-foreground">Deals Tracking</h4>
                                            <p className="text-sm">Investment deals history will be available here soon.</p>
                                            <Badge variant="outline" className="mt-2">Coming Soon</Badge>
                                        </div>
                                    ) : (
                                        userDetails.startups && userDetails.startups.length > 0 ? (
                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                {userDetails.startups.map(startup => (
                                                    <div
                                                        key={startup.id}
                                                        className="group relative flex flex-col justify-between p-4 rounded-xl border bg-card hover:shadow-md transition-all duration-200 hover:border-primary/20 cursor-pointer"
                                                        onClick={() => handleViewStartup(startup.id)}
                                                    >
                                                        <div className="flex items-start justify-between mb-3">
                                                            <div className="flex items-center gap-3">
                                                                <div className="h-10 w-10 rounded-lg bg-gradient-to-br from-purple-500/10 to-blue-500/10 flex items-center justify-center border border-purple-500/10 group-hover:border-purple-500/20 transition-colors">
                                                                    <Building2 className="h-5 w-5 text-purple-600" />
                                                                </div>
                                                                <div>
                                                                    <h4 className="font-bold text-base leading-tight group-hover:text-primary transition-colors line-clamp-1" title={startup.name}>
                                                                        {startup.name}
                                                                    </h4>
                                                                    <p className="text-xs text-muted-foreground">{startup.industry || 'Tech'}</p>
                                                                </div>
                                                            </div>
                                                            {getStatusBadge(startup.status || 'PENDING')}
                                                        </div>

                                                        <div className="space-y-3">
                                                            <div className="flex flex-wrap gap-2 text-xs">
                                                                <Badge variant="secondary" className="font-normal bg-secondary/50">
                                                                    {startup.stage || 'Idea'}
                                                                </Badge>
                                                                {startup.role && (
                                                                    <Badge variant="outline" className="font-medium bg-primary/5 text-primary border-primary/20">
                                                                        {startup.role.replace('_', ' ')}
                                                                    </Badge>
                                                                )}
                                                            </div>

                                                            <div className="pt-3 border-t grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                                                                <div className="flex flex-col">
                                                                    <span className="text-[10px] uppercase tracking-wider opacity-70">Raised</span>
                                                                    <span className="font-semibold text-foreground flex items-center gap-1">
                                                                        <DollarSign className="h-3 w-3" />
                                                                        {startup.raisedAmount?.toLocaleString() || 0}
                                                                    </span>
                                                                </div>
                                                                <div className="flex flex-col items-end">
                                                                    <span className="text-[10px] uppercase tracking-wider opacity-70">Created</span>
                                                                    <span className="font-medium text-foreground">
                                                                        {startup.createdAt ? new Date(startup.createdAt).toLocaleDateString() : '-'}
                                                                    </span>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        {/* Hover Action */}
                                                        <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                                            <div className="bg-background/80 backdrop-blur-sm rounded-full p-1.5 shadow-sm border">
                                                                <Eye className="h-3.5 w-3.5 text-primary" />
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <div className="text-center py-12 text-muted-foreground bg-muted/10 rounded-lg border border-dashed">
                                                <Briefcase className="h-10 w-10 mx-auto mb-3 opacity-30" />
                                                <p className="font-medium">No startups associated</p>
                                                <p className="text-xs opacity-70 mt-1">This user is not an owner or member of any startup.</p>
                                            </div>
                                        )
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
                                <SecurityTab userId={userId} canManageUser={canManageUser} />
                            )}
                        </div>
                    </div>
                )}
            </DialogContent>
            <StartupDetailsDialog
                open={isStartupDetailsOpen}
                onOpenChange={setIsStartupDetailsOpen}
                startup={selectedStartup}
            />
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

function SecurityTab({ userId, canManageUser }: { userId: string | null, canManageUser: boolean }) {
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
                    disabled={sessions.length === 0 || !canManageUser}
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
                                disabled={!canManageUser}
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

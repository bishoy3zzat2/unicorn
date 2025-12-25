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
    Monitor, Globe, AlertCircle, Eye, Trash2, MessageSquare
} from 'lucide-react'
import { formatDate, cn, formatCompactCurrency, formatTimeAgo } from '../../lib/utils'
import api from '../../lib/axios'
import { toast } from 'sonner'
import { useAuth } from '../../contexts/AuthContext'
import { getStartupById, deleteUserModerationLog, fetchDealsForInvestor, Deal } from '../../lib/api'
import { StartupDetailsDialog } from './StartupDetailsDialog'
import { Startup } from '../../types'
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '../ui/alert-dialog'

import { RotateCcw } from 'lucide-react'
import { SuspendUserDialog } from './SuspendUserDialog'
import { WarnUserDialog } from './WarnUserDialog'
import { DeleteUserDialog } from './DeleteUserDialog'
import { RestoreUserDialog } from './RestoreUserDialog'
import { UserStatusDialog } from './UserStatusDialog'
import { getUserChats, getChatMessages, ChatData, MessageData } from '../../api/adminChatApi'
import { ChatViewerDialog } from './ChatViewerDialog'

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
        verificationRequested?: boolean
        verificationNotes?: string
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
    const [activeTab, setActiveTab] = useState<'info' | 'startups' | 'transactions' | 'history' | 'security' | 'chats'>('info')
    const [selectedStartup, setSelectedStartup] = useState<Startup | null>(null)
    const [isStartupDetailsOpen, setIsStartupDetailsOpen] = useState(false)
    const [deleteLogId, setDeleteLogId] = useState<string | null>(null)
    const [showDeleteDialog, setShowDeleteDialog] = useState(false)
    const [investorDeals, setInvestorDeals] = useState<Deal[]>([])
    const [loadingDeals, setLoadingDeals] = useState(false)

    // Chat States
    const [userChats, setUserChats] = useState<ChatData[]>([])
    const [loadingChats, setLoadingChats] = useState(false)
    const [selectedChat, setSelectedChat] = useState<ChatData | null>(null)
    const [chatMessages, setChatMessages] = useState<MessageData[]>([])
    const [_loadingMessages, setLoadingMessages] = useState(false) // eslint-disable-line @typescript-eslint/no-unused-vars
    const [isChatViewerOpen, setIsChatViewerOpen] = useState(false)

    // Action Dialog States
    const [suspendDialogOpen, setSuspendDialogOpen] = useState(false)
    const [warnDialogOpen, setWarnDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
    const [restoreDialogOpen, setRestoreDialogOpen] = useState(false)
    const [statusChangeDialogOpen, setStatusChangeDialogOpen] = useState(false)
    const [revokeSubscriptionDialogOpen, setRevokeSubscriptionDialogOpen] = useState(false)
    const [revokingSubscription, setRevokingSubscription] = useState(false)

    const { user: currentUser } = useAuth()
    const isSuperAdmin = currentUser?.role === 'SUPER_ADMIN'



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
            setUserDetails(data)

            // Fetch deals if user is an investor
            if (data.role === 'INVESTOR') {
                setLoadingDeals(true)
                try {
                    const deals = await fetchDealsForInvestor(userId)
                    setInvestorDeals(deals)
                } catch (dealError) {
                    console.error('Failed to fetch investor deals:', dealError)
                } finally {
                    setLoadingDeals(false)
                }
            }
        } catch (error) {
            console.error('Failed to fetch user details:', error)
            toast.error('Failed to load user details')
        } finally {
            setLoading(false)
        }
    }

    // Fetch chats when user switches to chats tab
    useEffect(() => {
        if (activeTab === 'chats' && userId && open) {
            fetchUserChats()
        }
    }, [activeTab, userId, open])

    async function fetchUserChats() {
        if (!userId) return
        setLoadingChats(true)
        try {
            const chats = await getUserChats(userId)
            setUserChats(chats)
        } catch (error) {
            console.error('Failed to fetch user chats:', error)
            toast.error('Failed to load chats')
        } finally {
            setLoadingChats(false)
        }
    }

    async function handleViewChat(chat: ChatData) {
        setSelectedChat(chat)
        setLoadingMessages(true)
        setIsChatViewerOpen(true)
        try {
            const messages = await getChatMessages(chat.id)
            setChatMessages(messages)
        } catch (error) {
            console.error('Failed to fetch chat messages:', error)
            toast.error('Failed to load messages')
        } finally {
            setLoadingMessages(false)
        }
    }

    const handleActionComplete = () => {
        fetchUserDetails()
        if (onAction) onAction()
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

    async function handleRevokeSubscription() {
        if (!userId) return
        setRevokingSubscription(true)
        try {
            const response = await api.post(`/admin/subscriptions/${userId}/revoke`, {
                reason: 'Subscription revoked after Google Play refund'
            })
            const data = response.data as { success: boolean; message: string; previousPlan?: string }
            if (data.success) {
                toast.success(data.message)
                fetchUserDetails()
                onAction?.()
            } else {
                toast.error(data.message)
            }
        } catch (error) {
            console.error('Failed to revoke subscription:', error)
            toast.error('Failed to revoke subscription')
        } finally {
            setRevokingSubscription(false)
            setRevokeSubscriptionDialogOpen(false)
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

    const handleDeleteLog = (logId: string) => {
        setDeleteLogId(logId)
        setShowDeleteDialog(true)
    }

    const confirmDeleteLog = async () => {
        if (!deleteLogId) return
        try {
            await deleteUserModerationLog(deleteLogId)
            toast.success('Log entry deleted successfully')
            fetchUserDetails() // Reload user details to refresh logs
        } catch (error) {
            console.error('Failed to delete log:', error)
            toast.error('Failed to delete log entry')
        } finally {
            setShowDeleteDialog(false)
            setDeleteLogId(null)
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



    const tabs: Array<{ id: 'info' | 'startups' | 'transactions' | 'history' | 'security' | 'chats'; label: string; icon: React.ElementType; count?: number }> = [
        { id: 'info', label: 'Info', icon: User },
        { id: 'startups', label: 'Startups', icon: Building2, count: userDetails?.startups?.length },
        { id: 'transactions', label: 'Transactions', icon: CreditCard, count: userDetails?.recentTransactions?.length },
        { id: 'history', label: 'History', icon: Clock, count: userDetails?.moderationHistory?.length },
        { id: 'security', label: 'Security', icon: Shield },
        { id: 'chats', label: 'Chats', icon: MessageSquare, count: userChats.length },
    ]

    const canManageUser = userDetails ? (
        userDetails.role === 'SUPER_ADMIN' ? false :
            userDetails.role === 'ADMIN' ? isSuperAdmin :
                true
    ) : false
    const isSuspended = userDetails?.status === 'SUSPENDED' || userDetails?.status === 'BANNED'

    return (
        <>
            <Dialog open={open} onOpenChange={onOpenChange}>
                <DialogContent className="sm:max-w-4xl max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 bg-white dark:bg-slate-950 border-none shadow-2xl [&>button]:hidden">
                    {/* Gradient Header */}
                    <div className="bg-gradient-to-r from-slate-800 via-blue-900/50 to-indigo-900/50 dark:from-slate-900 dark:via-blue-950/80 dark:to-indigo-950/80 p-6 shrink-0 border-b border-slate-700/50">
                        <DialogHeader className="flex-row items-center justify-between space-y-0">
                            <div>
                                <DialogTitle className="text-xl text-white">User Details</DialogTitle>
                                <p className="text-sm text-white/70 mt-1">
                                    Manage user information, permissions, and security.
                                </p>
                            </div>
                            <div className="flex items-center gap-2">
                                <Button variant="ghost" size="icon" onClick={() => onOpenChange(false)} className="text-white/70 hover:text-white hover:bg-white/10">
                                    <XCircle className="h-5 w-5" />
                                </Button>
                            </div>
                        </DialogHeader>
                    </div>

                    {loading ? (
                        <div className="flex items-center justify-center py-12">
                            <Loader2 className="h-8 w-8 animate-spin text-primary" />
                        </div>
                    ) : !userDetails ? (
                        <div className="text-center py-12 text-muted-foreground">
                            User not found
                        </div>
                    ) : (
                        <div className="flex flex-col flex-1 overflow-y-auto bg-white dark:bg-slate-950">
                            {/* User Profile Hero Section */}
                            <div className="p-6 pb-0">
                                <div className="relative overflow-hidden rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
                                    <div className="absolute top-0 right-0 p-6 opacity-[0.03] pointer-events-none">
                                        <User className="w-64 h-64" />
                                    </div>

                                    <div className="relative z-10 flex flex-col md:flex-row gap-6 items-start">
                                        {/* Avatar & Status */}
                                        <div className="relative shrink-0">
                                            <div className="h-24 w-24 rounded-2xl overflow-hidden border-2 border-border shadow-sm bg-background">
                                                <img
                                                    src={userDetails.avatarUrl || "/avatars/avatar_1.png"}
                                                    alt={userDetails.email}
                                                    className="h-full w-full object-cover"
                                                />
                                            </div>
                                            <div className={cn(
                                                "absolute -bottom-2 -right-2 h-6 w-6 rounded-full border-4 border-background flex items-center justify-center shadow-sm",
                                                userDetails.hasActiveSession ? "bg-emerald-500" : "bg-slate-300"
                                            )} title={userDetails.hasActiveSession ? "Online" : "Offline"}>
                                                {userDetails.hasActiveSession && <div className="h-2 w-2 rounded-full bg-white animate-pulse" />}
                                            </div>
                                        </div>

                                        {/* Info & Actions */}
                                        <div className="flex-1 min-w-0 space-y-4">
                                            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                                                <div>
                                                    <h3 className="text-2xl font-bold tracking-tight text-foreground">
                                                        {userDetails.firstName ? `${userDetails.firstName} ${userDetails.lastName}` : userDetails.email.split('@')[0]}
                                                    </h3>
                                                    <div className="flex items-center gap-2 mt-1 text-muted-foreground">
                                                        <Mail className="h-3.5 w-3.5" />
                                                        <span className="text-sm">{userDetails.email}</span>
                                                    </div>
                                                    <div className="flex flex-wrap items-center gap-2 mt-3">
                                                        {getRoleBadge(userDetails.role)}
                                                        {getStatusBadge(userDetails.status)}
                                                        <div className="flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-muted text-xs font-mono text-muted-foreground border">
                                                            <span>ID:</span>
                                                            <span className="select-all">{userDetails.id}</span>
                                                            <button
                                                                onClick={() => {
                                                                    navigator.clipboard.writeText(userDetails.id);
                                                                    toast.success('ID copied');
                                                                }}
                                                                className="ml-1 hover:text-foreground"
                                                            >
                                                                <Monitor className="h-3 w-3" />
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>

                                                {/* Action Buttons Toolbar */}
                                                {canManageUser && (
                                                    <div className="flex items-center gap-1 bg-background p-1 rounded-lg border shadow-sm">
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            className="h-8 w-8 p-0 text-muted-foreground hover:text-foreground"
                                                            onClick={() => setStatusChangeDialogOpen(true)}
                                                            title="Change Status"
                                                        >
                                                            <Shield className="h-4 w-4" />
                                                        </Button>
                                                        <Separator orientation="vertical" className="h-4 mx-1" />

                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            className="h-8 w-8 p-0 text-amber-500 hover:text-amber-600 hover:bg-amber-500/10"
                                                            onClick={() => setWarnDialogOpen(true)}
                                                            title="Issue Warning"
                                                        >
                                                            <AlertTriangle className="h-4 w-4" />
                                                        </Button>

                                                        {(userDetails.status === 'SUSPENDED' || userDetails.status === 'BANNED') ? (
                                                            <Button
                                                                variant="ghost"
                                                                size="sm"
                                                                className="h-8 w-8 p-0 text-emerald-500 hover:text-emerald-600 hover:bg-emerald-500/10"
                                                                onClick={handleUnsuspend}
                                                                title="Unsuspend User"
                                                            >
                                                                <CheckCircle className="h-4 w-4" />
                                                            </Button>
                                                        ) : (
                                                            <Button
                                                                variant="ghost"
                                                                size="sm"
                                                                className="h-8 w-8 p-0 text-orange-500 hover:text-orange-600 hover:bg-orange-500/10"
                                                                onClick={() => setSuspendDialogOpen(true)}
                                                                disabled={isSuspended}
                                                                title="Suspend User"
                                                            >
                                                                <Ban className="h-4 w-4" />
                                                            </Button>
                                                        )}

                                                        {userDetails.status === 'DELETED' ? (
                                                            <Button
                                                                variant="ghost"
                                                                size="sm"
                                                                className="h-8 w-8 p-0 text-emerald-600 hover:text-emerald-700 hover:bg-emerald-500/10"
                                                                onClick={() => setRestoreDialogOpen(true)}
                                                                title="Restore User"
                                                            >
                                                                <RotateCcw className="h-4 w-4" />
                                                            </Button>
                                                        ) : (
                                                            <Button
                                                                variant="ghost"
                                                                size="sm"
                                                                className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-500/10"
                                                                onClick={() => setDeleteDialogOpen(true)}
                                                                title="Delete User"
                                                            >
                                                                <Trash2 className="h-4 w-4" />
                                                            </Button>
                                                        )}

                                                        {/* Revoke Subscription Button - Only show for PRO/ELITE users */}
                                                        {userDetails.currentSubscription &&
                                                            userDetails.currentSubscription.plan !== 'FREE' &&
                                                            userDetails.currentSubscription.status === 'ACTIVE' && (
                                                                <>
                                                                    <Separator orientation="vertical" className="h-4 mx-1" />
                                                                    <Button
                                                                        variant="ghost"
                                                                        size="sm"
                                                                        className="h-8 w-8 p-0 text-red-500 hover:text-red-600 hover:bg-red-500/10"
                                                                        onClick={() => setRevokeSubscriptionDialogOpen(true)}
                                                                        title="Revoke Subscription (After Refund)"
                                                                    >
                                                                        <DollarSign className="h-4 w-4" />
                                                                    </Button>
                                                                </>
                                                            )}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Tabs */}
                            <div className="sticky top-0 bg-white/95 dark:bg-slate-950/95 backdrop-blur-sm z-20 border-b border-slate-200 dark:border-slate-800 px-6 py-2">
                                <div className="flex items-center justify-start md:justify-center overflow-x-auto no-scrollbar gap-2">
                                    {tabs.filter(tab => {
                                        if (userDetails.role === 'ADMIN' || userDetails.role === 'SUPER_ADMIN') {
                                            return ['info', 'history', 'security'].includes(tab.id)
                                        }
                                        return true
                                    }).map(tab => {
                                        const isActive = activeTab === tab.id;
                                        const isDeals = tab.id === 'startups' && userDetails.role === 'INVESTOR';

                                        return (
                                            <button
                                                key={tab.id}
                                                onClick={() => setActiveTab(tab.id)}
                                                className={cn(
                                                    "flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-full transition-all whitespace-nowrap",
                                                    isActive
                                                        ? "bg-blue-500/10 text-blue-600 dark:text-blue-400 ring-1 ring-blue-500/20 shadow-sm"
                                                        : "text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-foreground"
                                                )}
                                            >
                                                {isDeals ? (
                                                    <TrendingUp className={cn("h-4 w-4", isActive ? "text-primary" : "text-muted-foreground")} />
                                                ) : (
                                                    <tab.icon className={cn("h-4 w-4", isActive ? "text-primary" : "text-muted-foreground")} />
                                                )}

                                                {isDeals ? "Deals" : tab.label}

                                                {tab.count !== undefined && tab.count > 0 && (
                                                    <span className={cn(
                                                        "ml-1.5 px-1.5 py-0.5 text-[10px] rounded-full",
                                                        isActive ? "bg-primary text-primary-foreground" : "bg-muted-foreground/20 text-muted-foreground"
                                                    )}>
                                                        {tab.count}
                                                    </span>
                                                )}
                                            </button>
                                        )
                                    })}
                                </div>
                            </div>

                            {/* Tab Content */}
                            <div className="pb-4 pt-4">
                                {/* Info Tab */}
                                {activeTab === 'info' && (
                                    <div className="space-y-4 px-8 pb-8">
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
                                                <InfoRow
                                                    icon={Globe}
                                                    label="LinkedIn"
                                                    value={
                                                        userDetails.linkedInUrl ? (
                                                            <a
                                                                href={userDetails.linkedInUrl.startsWith('http') ? userDetails.linkedInUrl : `https://www.linkedin.com/in/${userDetails.linkedInUrl.replace(/^\/+/, '')}`}
                                                                target="_blank"
                                                                rel="noopener noreferrer"
                                                                className="text-sm font-medium text-blue-500 hover:underline"
                                                            >
                                                                {userDetails.linkedInUrl.replace(/^https?:\/\/(www\.)?linkedin\.com\/in\//, '').replace(/\/$/, '') || 'View Profile'}
                                                            </a>
                                                        ) : 'Not provided'
                                                    }
                                                />

                                                <div className="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all duration-300 flex items-start gap-4">
                                                    <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-primary my-1">
                                                        <Briefcase className="h-5 w-5" />
                                                    </div>
                                                    <div className="flex-1">
                                                        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-1">Bio</p>
                                                        <p className="text-sm text-foreground/80 whitespace-pre-wrap leading-relaxed">
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
                                                    <div className={cn(
                                                        "p-4 rounded-lg border",
                                                        userDetails.investorInfo.isVerified ? "bg-emerald-500/10 border-emerald-500/30" :
                                                            (!userDetails.investorInfo.verificationRequested && userDetails.investorInfo.verificationNotes?.includes('Rejected')) ? "bg-red-500/10 border-red-500/30" :
                                                                "bg-blue-500/10 border-blue-500/30"
                                                    )}>
                                                        <h4 className={cn(
                                                            "font-semibold flex items-center gap-2",
                                                            userDetails.investorInfo.isVerified ? "text-emerald-500" :
                                                                (!userDetails.investorInfo.verificationRequested && userDetails.investorInfo.verificationNotes?.includes('Rejected')) ? "text-red-500" :
                                                                    "text-blue-500"
                                                        )}>
                                                            <TrendingUp className="h-4 w-4" />
                                                            Investor Profile
                                                            {userDetails.investorInfo.isVerified ? (
                                                                <CheckCircle className="h-4 w-4 text-green-500" />
                                                            ) : (!userDetails.investorInfo.verificationRequested && userDetails.investorInfo.verificationNotes?.includes('Rejected')) ? (
                                                                <XCircle className="h-4 w-4 text-red-500" />
                                                            ) : (
                                                                <span className="text-muted-foreground text-xs font-normal border px-2 py-0.5 rounded-full bg-white/20">
                                                                    {userDetails.investorInfo.verificationRequested ? 'Verification Pending' : 'Not Verified'}
                                                                </span>
                                                            )}
                                                        </h4>
                                                        <div className="mt-2 space-y-1 text-sm">

                                                            <p><strong>Investment Budget:</strong> {formatCompactCurrency(userDetails.investorInfo.investmentBudget || 0)}</p>
                                                            <p><strong>Preferred Industries:</strong> {userDetails.investorInfo.preferredIndustries || '-'}</p>
                                                            <p><strong>Preferred Stage:</strong> {userDetails.investorInfo.preferredStage || 'All Stages'}</p>

                                                            <div className="flex items-center gap-2 mt-2 pt-2 border-t border-current opacity-70">
                                                                <p><strong>Status:</strong></p>
                                                                {userDetails.investorInfo.isVerified ? (
                                                                    <Badge className="bg-green-500 text-white hover:bg-green-600">Verified</Badge>
                                                                ) : userDetails.investorInfo.readyForPayment ? (
                                                                    <Badge className="bg-blue-500 text-white hover:bg-blue-600">Ready for Payment</Badge>
                                                                ) : (!userDetails.investorInfo.verificationRequested && userDetails.investorInfo.verificationNotes?.includes('Rejected')) ? (
                                                                    <Badge variant="destructive">Rejected</Badge>
                                                                ) : userDetails.investorInfo.verificationRequested ? (
                                                                    <div className="flex items-center gap-2">
                                                                        <Badge variant="secondary" className="bg-amber-500/20 text-amber-700 hover:bg-amber-500/30">Pending Review</Badge>
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
                                                                ) : (
                                                                    <Badge variant="outline" className="text-muted-foreground">Not Applied</Badge>
                                                                )}

                                                                {!userDetails.investorInfo.isVerified && userDetails.investorInfo.readyForPayment && (
                                                                    <Button
                                                                        size="sm"
                                                                        variant="ghost"
                                                                        className="h-6 text-xs text-blue-600 hover:text-blue-700 hover:bg-blue-50 ml-auto"
                                                                        onClick={async () => {
                                                                            if (!confirm('Are you sure you want to MANUALLY mark this investor as VERIFIED? This bypasses the payment check.')) return;
                                                                            try {
                                                                                await api.post(`/admin/investors/${userDetails.investorInfo!.id}/complete-verification`);
                                                                                toast.success('Investor manually marked as VERIFIED');
                                                                                fetchUserDetails();
                                                                            } catch (e) {
                                                                                console.error(e);
                                                                                toast.error('Failed to verify investor');
                                                                            }
                                                                        }}
                                                                    >
                                                                        <CheckCircle className="h-3 w-3 mr-1" />
                                                                        Mark Verified
                                                                    </Button>
                                                                )}
                                                            </div>

                                                            {userDetails.investorInfo.verificationNotes && (
                                                                <div className="mt-3 pt-2 border-t border-current opacity-70">
                                                                    <p className="font-semibold text-xs uppercase opacity-70 mb-1">Verification History</p>
                                                                    <div className="text-xs bg-background/50 p-2 rounded border border-current opacity-80 whitespace-pre-wrap">
                                                                        {userDetails.investorInfo.verificationNotes}
                                                                    </div>
                                                                </div>
                                                            )}

                                                            {userDetails.investorInfo.verifiedAt && (
                                                                <p className="mt-2 text-xs opacity-70"><strong>Verified Since:</strong> {formatDate(userDetails.investorInfo.verifiedAt)}</p>
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
                                        {/* Stats (Vibrant & Contrast) */}
                                        <div className="grid grid-cols-2 lg:grid-cols-3 gap-4 px-6 mb-8 mt-6">
                                            {(userDetails.role !== 'ADMIN' && userDetails.role !== 'SUPER_ADMIN') && (
                                                <div className="flex flex-col items-center justify-center p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm hover:shadow-md transition-all group hover:-translate-y-1">
                                                    <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 shadow-lg shadow-blue-500/20 text-white flex items-center justify-center mb-3 group-hover:scale-110 transition-transform duration-300">
                                                        {userDetails.role === 'INVESTOR' ? <TrendingUp className="h-6 w-6" /> : <Building2 className="h-6 w-6" />}
                                                    </div>
                                                    <span className="text-3xl font-bold text-foreground tracking-tight">{userDetails.role === 'INVESTOR' ? 0 : userDetails.startupCount}</span>
                                                    <span className="text-xs font-bold text-muted-foreground uppercase tracking-widest mt-1">{userDetails.role === 'INVESTOR' ? 'Deals' : 'Startups'}</span>
                                                </div>
                                            )}

                                            <div className="flex flex-col items-center justify-center p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm hover:shadow-md transition-all group hover:-translate-y-1">
                                                <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-amber-500 to-amber-600 shadow-lg shadow-amber-500/20 text-white flex items-center justify-center mb-3 group-hover:scale-110 transition-transform duration-300">
                                                    <AlertTriangle className="h-6 w-6" />
                                                </div>
                                                <span className={cn("text-3xl font-bold tracking-tight", (userDetails.moderationHistory?.length || 0) > 0 ? "text-amber-600" : "text-foreground")}>
                                                    {userDetails.moderationHistory?.length || 0}
                                                </span>
                                                <span className="text-xs font-bold text-muted-foreground uppercase tracking-widest mt-1">Warnings</span>
                                            </div>

                                            {(userDetails.role !== 'ADMIN' && userDetails.role !== 'SUPER_ADMIN') && (
                                                <div className="flex flex-col items-center justify-center p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm hover:shadow-md transition-all group hover:-translate-y-1">
                                                    <div className={cn(
                                                        "h-12 w-12 rounded-xl shadow-lg text-white flex items-center justify-center mb-3 group-hover:scale-110 transition-transform duration-300",
                                                        userDetails.currentSubscription?.plan === 'ELITE'
                                                            ? "bg-gradient-to-br from-purple-500 to-purple-600 shadow-purple-500/20"
                                                            : userDetails.currentSubscription?.plan === 'PRO'
                                                                ? "bg-gradient-to-br from-blue-500 to-blue-600 shadow-blue-500/20"
                                                                : "bg-gradient-to-br from-emerald-500 to-emerald-600 shadow-emerald-500/20"
                                                    )}>
                                                        <CreditCard className="h-6 w-6" />
                                                    </div>
                                                    <span className={cn(
                                                        "text-xl font-bold tracking-tight",
                                                        userDetails.currentSubscription?.plan === 'ELITE' ? "text-purple-600" :
                                                            userDetails.currentSubscription?.plan === 'PRO' ? "text-blue-600" : "text-foreground"
                                                    )}>
                                                        {userDetails.currentSubscription?.plan || 'FREE'}
                                                    </span>
                                                    <span className="text-xs font-bold text-muted-foreground uppercase tracking-widest mt-1">Plan</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}

                                {/* Startups/Deals Tab */}
                                {activeTab === 'startups' && (
                                    <div className="space-y-4 px-8 pb-8">
                                        {userDetails.role === 'INVESTOR' ? (
                                            loadingDeals ? (
                                                <div className="flex items-center justify-center py-16">
                                                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                                                </div>
                                            ) : investorDeals.length > 0 ? (
                                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                    {investorDeals.map(deal => (
                                                        <div
                                                            key={deal.id}
                                                            className="group relative flex flex-col justify-between p-5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-lg hover:border-emerald-500/50 transition-all duration-300 overflow-hidden"
                                                        >
                                                            <div className="absolute top-0 left-0 w-1 h-full bg-gradient-to-b from-emerald-500 to-teal-500 opacity-0 group-hover:opacity-100 transition-opacity" />

                                                            <div className="flex items-start justify-between mb-4 pl-2">
                                                                <div className="flex items-center gap-3">
                                                                    <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-emerald-500/10 to-teal-500/10 flex items-center justify-center border border-emerald-500/10 group-hover:scale-105 transition-transform">
                                                                        {deal.startupLogo ? (
                                                                            <img src={deal.startupLogo} alt={deal.startupName} className="h-8 w-8 rounded-lg object-cover" />
                                                                        ) : (
                                                                            <Building2 className="h-6 w-6 text-emerald-600" />
                                                                        )}
                                                                    </div>
                                                                    <div>
                                                                        <h4 className="font-bold text-base leading-tight group-hover:text-primary transition-colors line-clamp-1" title={deal.startupName}>
                                                                            {deal.startupName}
                                                                        </h4>
                                                                        <p className="text-xs text-muted-foreground mt-0.5">{deal.dealType?.replace('_', ' ') || 'Investment'}</p>
                                                                    </div>
                                                                </div>
                                                                <span className={cn(
                                                                    "px-2 py-1 rounded-full text-xs font-semibold border",
                                                                    deal.status === 'COMPLETED' ? 'bg-green-500/10 text-green-500 border-green-500/30' :
                                                                        deal.status === 'CANCELLED' ? 'bg-red-500/10 text-red-500 border-red-500/30' :
                                                                            'bg-yellow-500/10 text-yellow-500 border-yellow-500/30'
                                                                )}>
                                                                    {deal.status}
                                                                </span>
                                                            </div>

                                                            <div className="space-y-3 pl-2">
                                                                <div className="pt-3 border-t grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                                                                    <div className="flex flex-col">
                                                                        <span className="text-[10px] uppercase tracking-wider opacity-70">Amount</span>
                                                                        <span className="font-semibold text-foreground flex items-center gap-1 text-sm">
                                                                            <DollarSign className="h-3.5 w-3.5 text-emerald-500" />
                                                                            {formatCompactCurrency(deal.amount)} {deal.currency}
                                                                        </span>
                                                                    </div>
                                                                    {deal.equityPercentage && (
                                                                        <div className="flex flex-col items-end">
                                                                            <span className="text-[10px] uppercase tracking-wider opacity-70">Equity</span>
                                                                            <span className="font-medium text-foreground text-sm">
                                                                                {deal.equityPercentage}%
                                                                            </span>
                                                                        </div>
                                                                    )}
                                                                </div>
                                                                <div className="flex items-center justify-between text-xs text-muted-foreground">
                                                                    <span>
                                                                        <Calendar className="h-3 w-3 inline mr-1" />
                                                                        {deal.dealDate ? new Date(deal.dealDate).toLocaleDateString() : '-'}
                                                                    </span>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            ) : (
                                                <div className="flex flex-col items-center justify-center py-16 text-muted-foreground bg-slate-50 dark:bg-slate-900/50 rounded-xl border border-dashed border-slate-200 dark:border-slate-800">
                                                    <div className="h-16 w-16 bg-emerald-500/10 rounded-full flex items-center justify-center mb-4">
                                                        <TrendingUp className="h-8 w-8 text-emerald-600" />
                                                    </div>
                                                    <h4 className="text-xl font-bold text-foreground mb-2">No Deals Yet</h4>
                                                    <p className="text-sm max-w-sm text-center">
                                                        This investor has no recorded investment deals.
                                                    </p>
                                                </div>
                                            )
                                        ) : (
                                            userDetails.startups && userDetails.startups.length > 0 ? (
                                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                    {userDetails.startups.map(startup => (
                                                        <div
                                                            key={startup.id}
                                                            className="group relative flex flex-col justify-between p-5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-lg hover:border-purple-500/50 transition-all duration-300 cursor-pointer overflow-hidden"
                                                            onClick={() => handleViewStartup(startup.id)}
                                                        >
                                                            <div className="absolute top-0 left-0 w-1 h-full bg-gradient-to-b from-purple-500 to-blue-500 opacity-0 group-hover:opacity-100 transition-opacity" />

                                                            <div className="flex items-start justify-between mb-4 pl-2">
                                                                <div className="flex items-center gap-3">
                                                                    <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-purple-500/10 to-blue-500/10 flex items-center justify-center border border-purple-500/10 group-hover:scale-105 transition-transform">
                                                                        <Building2 className="h-6 w-6 text-purple-600" />
                                                                    </div>
                                                                    <div>
                                                                        <h4 className="font-bold text-base leading-tight group-hover:text-primary transition-colors line-clamp-1" title={startup.name}>
                                                                            {startup.name}
                                                                        </h4>
                                                                        <p className="text-xs text-muted-foreground mt-0.5">{startup.industry || 'Tech'}</p>
                                                                    </div>
                                                                </div>
                                                                {getStatusBadge(startup.status || 'ACTIVE')}
                                                            </div>

                                                            <div className="space-y-3 pl-2">
                                                                <div className="flex flex-wrap gap-2 text-xs">
                                                                    <Badge variant="outline" className="bg-background/50 font-normal">
                                                                        {startup.stage || 'Idea'}
                                                                    </Badge>
                                                                    {startup.role && (
                                                                        <Badge className="bg-primary/5 text-primary border-primary/20 hover:bg-primary/10">
                                                                            {startup.role.replace('_', ' ')}
                                                                        </Badge>
                                                                    )}
                                                                </div>

                                                                <div className="pt-3 border-t grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                                                                    <div className="flex flex-col">
                                                                        <span className="text-[10px] uppercase tracking-wider opacity-70">Raised</span>
                                                                        <span className="font-semibold text-foreground flex items-center gap-1 text-sm">
                                                                            <DollarSign className="h-3.5 w-3.5 text-emerald-500" />
                                                                            {startup.raisedAmount?.toLocaleString() || 0}
                                                                        </span>
                                                                    </div>
                                                                    <div className="flex flex-col items-end">
                                                                        <span className="text-[10px] uppercase tracking-wider opacity-70">Created</span>
                                                                        <span className="font-medium text-foreground text-sm">
                                                                            {startup.createdAt ? new Date(startup.createdAt).toLocaleDateString() : '-'}
                                                                        </span>
                                                                    </div>
                                                                </div>
                                                            </div>

                                                            {/* Hover Action */}
                                                            <div className="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity transform translate-x-2 group-hover:translate-x-0">
                                                                <div className="bg-primary text-primary-foreground rounded-full p-2 shadow-md">
                                                                    <Eye className="h-4 w-4" />
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            ) : (
                                                <div className="flex flex-col items-center justify-center py-12 text-muted-foreground bg-slate-50 dark:bg-slate-900/50 rounded-xl border border-dashed border-slate-200 dark:border-slate-800">
                                                    <Briefcase className="h-12 w-12 mx-auto mb-3 opacity-20" />
                                                    <p className="font-medium">No startups associated</p>
                                                    <p className="text-xs opacity-70 mt-1 max-w-xs text-center">
                                                        This user is not currently listed as an owner or member of any startup.
                                                    </p>
                                                </div>
                                            )
                                        )}
                                    </div>
                                )}

                                {/* Transactions Tab */}
                                {activeTab === 'transactions' && (
                                    <div className="space-y-3 px-8 pb-8">
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
                                    <div className="space-y-4 px-8 pb-8">
                                        {userDetails.moderationHistory && userDetails.moderationHistory.length > 0 ? (
                                            <div className="relative border-l-2 border-muted ml-3 space-y-6 pl-6 pt-2">
                                                {userDetails.moderationHistory.map(log => (
                                                    <div key={log.id} className="relative group">
                                                        {/* Timeline Dot */}
                                                        <div className="absolute -left-[31px] top-4 h-4 w-4 rounded-full border-2 border-background bg-muted ring-2 ring-muted ring-offset-2 ring-offset-background group-hover:bg-primary transition-colors" />

                                                        <div className="p-5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all relative">
                                                            {/* Header */}
                                                            <div className="flex items-start justify-between mb-2">
                                                                <div className="flex items-center gap-2.5">
                                                                    <div className="p-1.5 rounded-lg bg-background border shadow-sm">
                                                                        <ActionIcon type={log.actionType} />
                                                                    </div>
                                                                    <h4 className="font-semibold text-foreground">
                                                                        {log.actionType.replace(/_/g, ' ')}
                                                                    </h4>
                                                                    <Badge variant={log.isActive ? 'default' : 'secondary'} className={cn(
                                                                        "text-[10px] px-1.5 py-0 h-5",
                                                                        log.isActive ? "bg-primary text-primary-foreground" : "text-muted-foreground bg-muted"
                                                                    )}>
                                                                        {log.isActive ? 'Active' : 'Resolved'}
                                                                    </Badge>
                                                                </div>

                                                                {/* Delete Action - Visible on hover only */}
                                                                <Button
                                                                    variant="ghost"
                                                                    size="icon"
                                                                    className="h-7 w-7 opacity-0 group-hover:opacity-100 transition-all text-muted-foreground hover:text-destructive hover:bg-destructive/10 -mt-1 -mr-1"
                                                                    onClick={() => handleDeleteLog(log.id)}
                                                                    title="Delete Entry"
                                                                >
                                                                    <Trash2 className="h-3.5 w-3.5" />
                                                                </Button>
                                                            </div>

                                                            {/* Content */}
                                                            <div className="space-y-2">
                                                                <p className="text-sm text-foreground/80 bg-slate-50 dark:bg-slate-800/50 p-2.5 rounded-md border border-dashed border-slate-200 dark:border-slate-700">
                                                                    {log.reason || <span className="text-muted-foreground italic">No reason provided</span>}
                                                                </p>
                                                                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground pt-1">
                                                                    <div className="flex items-center gap-1.5">
                                                                        <Shield className="h-3 w-3" />
                                                                        <span>{log.adminEmail}</span>
                                                                    </div>
                                                                    <div className="flex items-center gap-1.5">
                                                                        <Clock className="h-3 w-3" />
                                                                        <span>{formatDate(log.createdAt)}</span>
                                                                    </div>
                                                                    {log.expiresAt && log.isActive && (
                                                                        <div className="flex items-center gap-1.5 text-orange-500">
                                                                            <AlertCircle className="h-3 w-3" />
                                                                            <span>Expires: {formatDate(log.expiresAt)}</span>
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <div className="flex flex-col items-center justify-center py-16 text-muted-foreground bg-muted/10 rounded-xl border border-dashed">
                                                <Clock className="h-12 w-12 mx-auto mb-3 opacity-20" />
                                                <p className="font-medium">No moderation history</p>
                                                <p className="text-xs opacity-70 mt-1">This user has a clean record.</p>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {/* Security Tab */}
                                {activeTab === 'security' && (
                                    <div className="px-8 pb-8">
                                        <SecurityTab userId={userId} canManageUser={canManageUser} />
                                    </div>
                                )}

                                {/* Chats Tab */}
                                {activeTab === 'chats' && (
                                    <div className="space-y-4 px-8 pb-8">
                                        {loadingChats ? (
                                            <div className="flex items-center justify-center py-16">
                                                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                                            </div>
                                        ) : userChats.length > 0 ? (
                                            <div className="grid grid-cols-1 gap-4">
                                                {userChats.map((chat) => (
                                                    <div
                                                        key={chat.id}
                                                        className="group relative flex items-center justify-between p-5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-lg hover:border-primary/50 transition-all duration-300 cursor-pointer"
                                                        onClick={() => handleViewChat(chat)}
                                                    >
                                                        <div className="flex items-center gap-4">
                                                            <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-primary/10 to-primary/20 flex items-center justify-center border border-primary/10">
                                                                <MessageSquare className="h-6 w-6 text-primary" />
                                                            </div>
                                                            <div>
                                                                <h4 className="font-semibold text-base group-hover:text-primary transition-colors">
                                                                    {userDetails?.role === 'INVESTOR' ? chat.startupName : chat.investorName}
                                                                </h4>
                                                                <p className="text-xs text-muted-foreground mt-0.5">
                                                                    Last message: {formatTimeAgo(new Date(chat.lastMessageAt))}
                                                                </p>
                                                            </div>
                                                        </div>
                                                        <div className="flex items-center gap-2">
                                                            {chat.unreadCount > 0 && (
                                                                <span className="px-2 py-1 rounded-full text-xs font-semibold bg-primary text-primary-foreground">
                                                                    {chat.unreadCount}
                                                                </span>
                                                            )}
                                                            <span className={cn(
                                                                "px-2 py-1 rounded-full text-xs font-semibold border",
                                                                chat.status === 'ACTIVE' ? 'bg-green-500/10 text-green-500 border-green-500/30' :
                                                                    chat.status === 'BLOCKED' ? 'bg-red-500/10 text-red-500 border-red-500/30' :
                                                                        'bg-gray-500/10 text-gray-500 border-gray-500/30'
                                                            )}>
                                                                {chat.status}
                                                            </span>
                                                            <Eye className="h-4 w-4 text-muted-foreground group-hover:text-primary transition-colors" />
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <div className="flex flex-col items-center justify-center py-16 text-muted-foreground bg-muted/10 rounded-xl border border-dashed">
                                                <MessageSquare className="h-12 w-12 mx-auto mb-3 opacity-20" />
                                                <p className="font-medium">No chats found</p>
                                                <p className="text-xs opacity-70 mt-1">This user hasn't started any conversations yet.</p>
                                            </div>
                                        )}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </DialogContent>
            </Dialog>

            <StartupDetailsDialog
                open={isStartupDetailsOpen}
                onOpenChange={setIsStartupDetailsOpen}
                startup={selectedStartup}
            />

            <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Log Entry</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete this log entry? This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={confirmDeleteLog}
                            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                        >
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {userDetails && (
                <>
                    <SuspendUserDialog
                        open={suspendDialogOpen}
                        onOpenChange={setSuspendDialogOpen}
                        userId={userDetails.id}

                        onSuccess={handleActionComplete}
                    />

                    <WarnUserDialog
                        open={warnDialogOpen}
                        onOpenChange={setWarnDialogOpen}
                        userId={userDetails.id}

                        onSuccess={handleActionComplete}
                    />

                    <DeleteUserDialog
                        open={deleteDialogOpen}
                        onOpenChange={setDeleteDialogOpen}
                        userId={userDetails.id}

                        onSuccess={handleActionComplete}
                    />

                    <RestoreUserDialog
                        open={restoreDialogOpen}
                        onOpenChange={setRestoreDialogOpen}
                        userId={userDetails.id}

                        onSuccess={handleActionComplete}
                    />

                    <UserStatusDialog
                        open={statusChangeDialogOpen}
                        onOpenChange={setStatusChangeDialogOpen}
                        userId={userId}
                        currentStatus={userDetails?.status || ''}
                        onSuccess={handleActionComplete}
                    />

                    <ChatViewerDialog
                        open={isChatViewerOpen}
                        onOpenChange={setIsChatViewerOpen}
                        messages={chatMessages}
                        chatTitle={selectedChat ? `Chat with ${userDetails?.role === 'INVESTOR' ? selectedChat.startupName : selectedChat.investorName}` : 'Chat'}
                    />

                    {/* Revoke Subscription Confirmation Dialog */}
                    <AlertDialog open={revokeSubscriptionDialogOpen} onOpenChange={setRevokeSubscriptionDialogOpen}>
                        <AlertDialogContent className="bg-white dark:bg-slate-900 rounded-xl shadow-xl border-0">
                            <AlertDialogHeader>
                                <AlertDialogTitle className="text-red-600 flex items-center gap-2">
                                    <DollarSign className="h-5 w-5" />
                                    Revoke Subscription
                                </AlertDialogTitle>
                                <AlertDialogDescription className="text-slate-600 dark:text-slate-400">
                                    <span className="font-semibold text-red-500">Are you sure?</span> This will immediately downgrade the user to the <span className="font-semibold">Free</span> plan.
                                    <br /><br />
                                    <span className="text-amber-600 dark:text-amber-400 text-sm">
                                        ⚠️ Use this only after processing a refund in Google Play Console.
                                    </span>
                                </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                                <AlertDialogCancel disabled={revokingSubscription}>Cancel</AlertDialogCancel>
                                <AlertDialogAction
                                    onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                                        e.preventDefault()
                                        handleRevokeSubscription()
                                    }}
                                    disabled={revokingSubscription}
                                    className="bg-red-600 hover:bg-red-700 text-white"
                                >
                                    {revokingSubscription ? (
                                        <>
                                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                            Revoking...
                                        </>
                                    ) : (
                                        'Revoke Subscription'
                                    )}
                                </AlertDialogAction>
                            </AlertDialogFooter>
                        </AlertDialogContent>
                    </AlertDialog>
                </>
            )}
        </>
    )
}



function ActionIcon({ type }: { type: string }) {
    const icons: Record<string, React.ReactNode> = {
        WARNING: <AlertTriangle className="h-4 w-4 text-amber-500" />,
        SUSPENSION: <Ban className="h-4 w-4 text-orange-500" />,
        PERMANENT_BAN: <XCircle className="h-4 w-4 text-red-500" />,
        UNSUSPEND: <CheckCircle className="h-4 w-4 text-emerald-500" />,
        DELETE: <XCircle className="h-4 w-4 text-red-500" />,
        SUBSCRIPTION_REVOKED: <DollarSign className="h-4 w-4 text-red-500" />,
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

// Helper Components
function InfoRow({ icon: Icon, label, value, iconColor }: { icon: any, label: string, value: React.ReactNode, iconColor?: string }) {
    return (
        <div className="flex items-center gap-3 p-3 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:shadow-sm transition-colors">
            <div className={cn("flex flex-shrink-0 items-center justify-center h-8 w-8 rounded-full bg-background shadow-sm border border-border/50", iconColor ? iconColor.replace('text-', 'bg-').replace('500', '100') : "bg-muted/50")}>
                <Icon className={cn("h-4 w-4", iconColor || "text-muted-foreground")} />
            </div>
            <div className="flex flex-col min-w-0">
                <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground/80">{label}</span>
                <span className="text-sm font-medium truncate text-foreground">{value}</span>
            </div>
        </div>
    )
}

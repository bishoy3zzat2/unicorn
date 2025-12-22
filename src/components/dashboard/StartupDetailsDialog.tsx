
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogTitle,
} from "../ui/dialog"
import { Button } from "../ui/button"
import {
    Facebook, Instagram, Twitter, Globe, UserCog,
    FileText, FileSpreadsheet, FilePieChart,
    Building2, Calendar, Layers, Clock, TrendingUp, Target, Users,
    LogOut, Trash2, MoreVertical, UserPlus, Eye,
    Loader2, History, Shield, AlertTriangle, XCircle
} from "lucide-react"
import { formatDate, cn, formatCurrency } from "../../lib/utils"
import { Startup } from "../../types"
import { useAuth } from "../../contexts/AuthContext"
import { toast } from "sonner"
import { AddMemberDialog } from "./AddMemberDialog"
import { UserDetailsModal } from "./UserDetailsModal"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../ui/dropdown-menu"
import { removeStartupMember, unsignStartupMember, leaveStartup, unsignStartup, transferStartupOwnership, getStartupById } from "../../lib/api"
import { useState, MouseEvent, useEffect } from "react"
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "../ui/alert-dialog"
import { fetchStartupModerationLogs, deleteStartupModerationLog, StartupModerationLog } from "../../lib/api"
import { WarnStartupDialog, StartupStatusDialog, DeleteStartupDialog } from "./StartupActionDialogs"
import { Badge } from "../ui/badge"

interface StartupDetailsDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    startup?: Startup | null
    startupId?: string | null
    onTransfer?: (startup: Startup) => void
    onActionComplete?: () => void
}

export function StartupDetailsDialog({
    open,
    onOpenChange,
    startup: passedStartup,
    startupId,
    onTransfer,
    onActionComplete
}: StartupDetailsDialogProps) {
    const [fetchedStartup, setFetchedStartup] = useState<Startup | null>(null)
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (open && !passedStartup && startupId) {
            loadStartup()
        }
    }, [open, passedStartup, startupId])

    const loadStartup = async () => {
        if (!startupId) return
        setLoading(true)
        try {
            const data = await getStartupById(startupId)
            setFetchedStartup(data)
        } catch (error) {
            console.error(error)
            toast.error("Failed to load startup details")
        } finally {
            setLoading(false)
        }
    }

    const startup = passedStartup || fetchedStartup



    const { user } = useAuth()

    const [confirmDialog, setConfirmDialog] = useState<{
        open: boolean
        title: string
        description: string
        action: () => Promise<void>
        variant?: "default" | "destructive"
    }>({
        open: false,
        title: "",
        description: "",
        action: async () => { },
        variant: "default"
    })

    // Action Dialog States
    const [warnDialogOpen, setWarnDialogOpen] = useState(false)
    const [statusChangeDialogOpen, setStatusChangeDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)


    const [isAddMemberOpen, setIsAddMemberOpen] = useState(false)
    const [viewMemberId, setViewMemberId] = useState<string | null>(null)

    // Audit Logs State
    const [auditLogs, setAuditLogs] = useState<StartupModerationLog[]>([])
    const [loadingLogs, setLoadingLogs] = useState(false)
    const [activeTab, setActiveTab] = useState<'details' | 'history'>('details')

    if (loading) {
        return (
            <Dialog open={open} onOpenChange={onOpenChange}>
                <DialogContent>
                    <div className="flex justify-center items-center py-10">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                    </div>
                </DialogContent>
            </Dialog>
        )
    }

    if (!startup) return null

    const loadLogs = async () => {
        if (!startup) return
        try {
            setLoadingLogs(true)
            const logs = await fetchStartupModerationLogs(startup.id)
            setAuditLogs(logs)
        } catch (error) {
            console.error(error)
            toast.error("Failed to load audit logs")
        } finally {
            setLoadingLogs(false)
        }
    }

    // Reset tab when dialog opens
    if (open && activeTab === 'history' && !startup) {
        setActiveTab('details')
    }

    // Check membership status
    const currentMember = startup?.members?.find(m => m.userId === user?.id)
    const isMember = !!currentMember
    const isActiveMember = currentMember?.isActive

    const handleLeaveTeam = () => {
        if (!startup) return
        setConfirmDialog({
            open: true,
            title: "Leave Team",
            description: "Are you sure you want to leave this team? You will be marked as a past member.",
            variant: "destructive",
            action: async () => {
                try {
                    await leaveStartup(startup.id)
                    toast.success("You have left the team.")
                    onActionComplete?.()
                    onOpenChange(false)
                } catch (error) {
                    toast.error("Failed to leave team")
                    console.error(error)
                }
            }
        })
    }

    const handleUnsign = () => {
        if (!startup) return
        setConfirmDialog({
            open: true,
            title: "Unsign from Startup",
            description: "Are you sure you want to unsign? This will completely remove your membership record.",
            variant: "destructive",
            action: async () => {
                try {
                    await unsignStartup(startup.id)
                    toast.success("You have unsigned from the startup.")
                    onActionComplete?.()
                    onOpenChange(false)
                } catch (error) {
                    toast.error("Failed to unsign")
                    console.error(error)
                }
            }
        })
    }

    const handleRemoveMember = (memberId: string, memberName: string) => {
        if (!startup) return
        setConfirmDialog({
            open: true,
            title: "Remove Member",
            description: `Are you sure you want to remove ${memberName}? They will be marked as a past member.`,
            variant: "destructive",
            action: async () => {
                try {
                    await removeStartupMember(startup.id, memberId)
                    toast.success(`${memberName} removed from team.`)
                    onActionComplete?.()
                } catch (error) {
                    toast.error("Failed to remove member")
                    console.error(error)
                }
            }
        })
    }

    const handleDeleteLog = async (logId: string) => {
        if (!startup) return
        setConfirmDialog({
            open: true,
            title: "Delete Audit Log",
            description: "Are you sure you want to delete this log entry? This action cannot be undone.",
            variant: "destructive",
            action: async () => {
                try {
                    await deleteStartupModerationLog(logId)
                    toast.success("Log entry deleted successfully")
                    loadLogs() // Reload logs
                } catch (error) {
                    toast.error("Failed to delete log entry")
                    console.error(error)
                }
            }
        })
    }

    const handleUnsignMember = (memberId: string, memberName: string) => {
        if (!startup) return
        setConfirmDialog({
            open: true,
            title: "Delete Member",
            description: `Are you sure you want to delete ${memberName} from the team history? This cannot be undone.`,
            variant: "destructive",
            action: async () => {
                try {
                    await unsignStartupMember(startup.id, memberId)
                    toast.success(`${memberName} deleted from team history.`)
                    onActionComplete?.()
                } catch (error) {
                    toast.error("Failed to delete member")
                    console.error(error)
                }
            }
        })
    }

    const handleTransferOwnership = (memberUserId: string, memberName: string) => {
        if (!startup) return
        setConfirmDialog({
            open: true,
            title: "Transfer Ownership",
            description: `Are you sure you want to transfer ownership of ${startup.name} to ${memberName}? You will lose your status as the owner. This action cannot be undone by you.`,
            variant: "destructive",
            action: async () => {
                try {
                    await transferStartupOwnership(startup.id, memberUserId)
                    toast.success(`Ownership transferred to ${memberName} successfully.`)
                    onActionComplete?.()
                    onOpenChange(false)
                } catch (error) {
                    toast.error("Failed to transfer ownership")
                    console.error(error)
                }
            }
        })
    }

    const isAdminOrOwner = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN' || (startup?.ownerId === user?.id)





    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col p-0 border-0 gap-0 [&>button]:hidden">
                <div className="flex-1 overflow-y-auto custom-scrollbar">
                    {/* Hero Section */}
                    <div className="relative h-64 w-full bg-muted group">
                        {startup.coverUrl ? (
                            <img
                                src={startup.coverUrl}
                                alt="Cover"
                                className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                            />
                        ) : (
                            <div className="w-full h-full bg-gradient-to-r from-indigo-500/20 via-purple-500/20 to-pink-500/20 animate-gradle" />
                        )}
                        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/60 to-transparent" />

                        {/* Top Actions */}


                        <div className="absolute -bottom-12 left-8 flex items-end">
                            <div className="relative">
                                {startup.logoUrl ? (
                                    <div className="h-32 w-32 rounded-2xl overflow-hidden border-4 border-background shadow-xl bg-white relative z-10">
                                        <img
                                            src={startup.logoUrl}
                                            alt="Logo"
                                            className="h-full w-full object-cover"
                                        />
                                    </div>
                                ) : (
                                    <div className="h-32 w-32 rounded-2xl bg-background border-4 border-background shadow-xl flex items-center justify-center relative z-10">
                                        <div className="h-full w-full bg-gradient-to-br from-primary/10 to-primary/5 rounded-xl flex items-center justify-center">
                                            <Building2 className="h-12 w-12 text-primary/60" />
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                    {/* Top Actions */}
                    <div className="absolute top-4 right-4 flex gap-2 z-50">
                        <Button variant="ghost" size="icon" onClick={() => onOpenChange(false)} className="rounded-full bg-black/20 text-white hover:bg-black/40 backdrop-blur-md border-0">
                            <XCircle className="h-5 w-5" />
                        </Button>
                    </div>
                    <div className="px-8 pt-16 pb-8">
                        <div className="flex flex-col lg:flex-row justify-between items-start gap-6 mb-8">
                            <div className="space-y-2 flex-1">
                                <div className="flex items-center gap-3 flex-wrap">
                                    <DialogTitle className="text-3xl font-bold tracking-tight text-foreground">
                                        {startup.name}
                                    </DialogTitle>
                                    {startup.status && getStatusBadge(startup.status)}
                                </div>
                                <DialogDescription className="text-lg font-medium text-muted-foreground max-w-2xl leading-relaxed">
                                    {startup.tagline}
                                </DialogDescription>

                                <div className="flex items-center gap-4 pt-2">
                                    <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-muted/50 border hover:bg-muted transition-colors group/id cursor-pointer"
                                        onClick={() => {
                                            navigator.clipboard.writeText(startup.id);
                                            toast.success('Startup ID copied!');
                                        }}>
                                        <code className="text-xs font-mono text-muted-foreground group-hover/id:text-foreground transition-colors">
                                            {startup.id}
                                        </code>
                                        <div className="p-1 rounded-full bg-background shadow-sm opacity-0 group-hover/id:opacity-100 transition-opacity">
                                            <span className="text-[10px] block">📋</span>
                                        </div>
                                    </div>

                                    {/* Socials */}
                                    <div className="flex items-center gap-1">
                                        {[
                                            { icon: Facebook, url: startup.facebookUrl, color: 'text-blue-600', hover: 'hover:bg-blue-50' },
                                            { icon: Instagram, url: startup.instagramUrl, color: 'text-pink-600', hover: 'hover:bg-pink-50' },
                                            { icon: Twitter, url: startup.twitterUrl, color: 'text-sky-500', hover: 'hover:bg-sky-50' },
                                            { icon: Globe, url: startup.websiteUrl, color: 'text-emerald-600', hover: 'hover:bg-emerald-50' },
                                        ].map((social, idx) => (
                                            social.url && (
                                                <Button
                                                    key={idx}
                                                    variant="ghost"
                                                    size="icon"
                                                    className={cn("h-8 w-8 transition-transform hover:scale-110", social.color, social.hover)}
                                                    onClick={() => window.open(social.url, '_blank')}
                                                >
                                                    <social.icon className="h-4 w-4" />
                                                </Button>
                                            )
                                        ))}
                                    </div>
                                </div>
                            </div>

                            {/* Action Toolbar */}
                            <div className="flex flex-wrap items-center gap-2 p-1.5 rounded-xl border bg-card shadow-sm self-start lg:self-center">
                                {(user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN') && (
                                    <>
                                        <Button variant="ghost" size="sm" className="h-9 gap-2 text-amber-600 hover:text-amber-700 hover:bg-amber-50" onClick={() => setWarnDialogOpen(true)}>
                                            <AlertTriangle className="h-4 w-4" />
                                            <span className="sr-only lg:not-sr-only">Warn</span>
                                        </Button>
                                        <Button variant="ghost" size="sm" className="h-9 gap-2 text-blue-600 hover:text-blue-700 hover:bg-blue-50" onClick={() => setStatusChangeDialogOpen(true)}>
                                            <Shield className="h-4 w-4" />
                                            <span className="sr-only lg:not-sr-only">Status</span>
                                        </Button>
                                        <Button variant="ghost" size="sm" className="h-9 gap-2 text-red-600 hover:text-red-700 hover:bg-red-50" onClick={() => setDeleteDialogOpen(true)}>
                                            <Trash2 className="h-4 w-4" />
                                            <span className="sr-only lg:not-sr-only">Delete</span>
                                        </Button>
                                        <div className="w-px h-6 bg-border mx-1" />
                                    </>
                                )}

                                {onTransfer && (
                                    <Button size="sm" variant="ghost" onClick={() => onTransfer(startup)}>
                                        <UserCog className="h-4 w-4 mr-2" />
                                        Transfer
                                    </Button>
                                )}

                                {(isActiveMember || isMember) && (
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button size="sm" variant="outline" className="gap-2">
                                                <Users className="h-4 w-4" />
                                                Membership
                                            </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end">
                                            {isActiveMember && (
                                                <DropdownMenuItem onClick={handleLeaveTeam} className="text-red-600">
                                                    <LogOut className="mr-2 h-4 w-4" /> Leave Team
                                                </DropdownMenuItem>
                                            )}
                                            {isMember && (
                                                <DropdownMenuItem onClick={handleUnsign} className="text-destructive">
                                                    <Trash2 className="mr-2 h-4 w-4" /> Unsign Completely
                                                </DropdownMenuItem>
                                            )}
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                )}
                            </div>
                        </div>

                        {/* Tabs */}
                        <div className="flex items-center gap-1 p-1 bg-muted/50 rounded-lg w-fit">
                            <button
                                onClick={() => setActiveTab('details')}
                                className={cn(
                                    "px-4 py-2 text-sm font-medium rounded-md transition-all",
                                    activeTab === 'details'
                                        ? "bg-background text-foreground shadow-sm"
                                        : "text-muted-foreground hover:text-foreground hover:bg-background/50"
                                )}
                            >
                                Overview
                            </button>
                            {(user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN') && (
                                <button
                                    onClick={() => {
                                        setActiveTab('history');
                                        loadLogs();
                                    }}
                                    className={cn(
                                        "px-4 py-2 text-sm font-medium rounded-md transition-all flex items-center gap-2",
                                        activeTab === 'history'
                                            ? "bg-background text-foreground shadow-sm"
                                            : "text-muted-foreground hover:text-foreground hover:bg-background/50"
                                    )}
                                >
                                    <History className="h-4 w-4" />
                                    <span className="hidden sm:inline">Moderation History</span>
                                    <span className="sm:hidden">History</span>
                                </button>
                            )}
                        </div>
                    </div>

                    {activeTab === 'details' && (
                        <div className="space-y-8 px-8 pb-8">
                            {/* Metrics Grid */}
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div className="p-5 rounded-2xl bg-gradient-to-br from-card to-card/50 border shadow-sm relative overflow-hidden group hover:border-primary/20 transition-all">
                                    <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity">
                                        <Target className="h-24 w-24 -rotate-12" />
                                    </div>
                                    <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-1">Funding Goal</p>
                                    <p className="text-2xl font-bold tracking-tight text-foreground">{formatCurrency(startup.fundingGoal)}</p>
                                    <div className="mt-4 flex items-center gap-2 text-xs text-muted-foreground">
                                        <div className="h-1.5 w-16 rounded-full bg-muted overflow-hidden">
                                            <div className="h-full bg-primary/50 w-full" />
                                        </div>
                                        <span>Target</span>
                                    </div>
                                </div>

                                <div className="p-5 rounded-2xl bg-gradient-to-br from-emerald-50 to-emerald-100/50 dark:from-emerald-950/20 dark:to-emerald-900/10 border border-emerald-200/50 dark:border-emerald-800/50 relative overflow-hidden group">
                                    <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity">
                                        <TrendingUp className="h-24 w-24 -rotate-12 text-emerald-600" />
                                    </div>
                                    <div className="flex justify-between items-start mb-1 relative z-10">
                                        <p className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Raised So Far</p>
                                        <Badge variant="outline" className="bg-emerald-100/50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800 text-[10px] h-5">
                                            Success
                                        </Badge>
                                    </div>
                                    <p className="text-2xl font-bold tracking-tight text-emerald-700 dark:text-emerald-400 relative z-10">{formatCurrency(startup.raisedAmount)}</p>

                                    {/* Progress Bar */}
                                    <div className="relative z-10 mt-4">
                                        <div className="flex justify-between text-[10px] font-medium text-emerald-700/70 dark:text-emerald-400/70 mb-1.5">
                                            <span>Progress</span>
                                            <span>{Math.round(((startup.raisedAmount || 0) / (startup.fundingGoal || 1)) * 100)}%</span>
                                        </div>
                                        <div className="w-full bg-emerald-200/50 dark:bg-emerald-900/30 h-2 rounded-full overflow-hidden">
                                            <div
                                                className="bg-emerald-500 h-full rounded-full transition-all duration-1000 ease-out shadow-[0_0_10px_rgba(16,185,129,0.4)]"
                                                style={{ width: `${Math.min(((startup.raisedAmount || 0) / (startup.fundingGoal || 1)) * 100, 100)}%` }}
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="p-5 rounded-2xl bg-gradient-to-br from-card to-card/50 border shadow-sm relative overflow-hidden group hover:border-primary/20 transition-all">
                                    <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity">
                                        <Layers className="h-24 w-24 -rotate-12" />
                                    </div>
                                    <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider mb-2">Current Stage</p>
                                    <div className="relative z-10">{getStageBadge(startup.stage)}</div>
                                    <p className="text-xs text-muted-foreground mt-3 leading-relaxed max-w-[150px]">
                                        Startup is currently in the <span className="font-medium text-foreground lowercase">{startup.stage.replace(/_/g, ' ')}</span> phase.
                                    </p>
                                </div>
                            </div>

                            {/* Info Grid */}
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div className="group p-4 rounded-xl border bg-card/50 hover:bg-card hover:shadow-md transition-all duration-300 flex items-center gap-4">
                                    <div className="h-12 w-12 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-blue-600">
                                        <Building2 className="h-6 w-6" />
                                    </div>
                                    <div>
                                        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">Industry</p>
                                        <p className="font-semibold text-foreground">{startup.industry}</p>
                                    </div>
                                </div>

                                <div className="group p-4 rounded-xl border bg-card/50 hover:bg-card hover:shadow-md transition-all duration-300 flex items-center gap-4">
                                    <div className="h-12 w-12 rounded-xl bg-amber-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-amber-600">
                                        <Calendar className="h-6 w-6" />
                                    </div>
                                    <div>
                                        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">Founded</p>
                                        <p className="font-semibold text-foreground">{formatDate(startup.createdAt)}</p>
                                    </div>
                                </div>

                                <div className="group p-4 rounded-xl border bg-card/50 hover:bg-card hover:shadow-md transition-all duration-300 flex items-center gap-4">
                                    <div className="h-12 w-12 rounded-xl bg-cyan-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-cyan-600">
                                        <Clock className="h-6 w-6" />
                                    </div>
                                    <div>
                                        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">Last Sync</p>
                                        <p className="font-semibold text-foreground">{startup.updatedAt ? formatDate(startup.updatedAt) : 'N/A'}</p>
                                    </div>
                                </div>
                            </div>

                            {/* Team Members */}
                            {startup.members && startup.members.length > 0 && (
                                <div className="space-y-4">
                                    <div className="flex items-center justify-between">
                                        <h4 className="text-lg font-bold flex items-center gap-2">
                                            <Users className="h-5 w-5 text-primary" />
                                            Team Members
                                        </h4>
                                        {isAdminOrOwner && (
                                            <Button
                                                size="sm"
                                                variant="outline"
                                                className="h-8 gap-1.5"
                                                onClick={() => setIsAddMemberOpen(true)}
                                            >
                                                <UserPlus className="h-3.5 w-3.5" />
                                                Add Member
                                            </Button>
                                        )}
                                    </div>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                        {startup.members.map((member) => {
                                            const isOwner = member.userId === startup.ownerId;
                                            return (
                                                <div key={member.id} className={cn(
                                                    "group relative flex items-start gap-4 p-4 rounded-xl border transition-all duration-300 overflow-hidden",
                                                    isOwner
                                                        ? "border-amber-200/50 bg-gradient-to-br from-amber-50 to-white hover:border-amber-300 dark:from-amber-950/20 dark:to-card dark:border-amber-800/50"
                                                        : "border-border/50 bg-card hover:bg-muted/30 hover:shadow-sm"
                                                )}>
                                                    {isOwner && <div className="absolute top-0 right-0 w-16 h-16 bg-gradient-to-bl from-amber-400/20 to-transparent rounded-bl-full -mr-8 -mt-8" />}

                                                    {/* Avatar with Status Indicator */}
                                                    <div className="relative shrink-0">
                                                        <img
                                                            src={member.userAvatarUrl || `https://api.dicebear.com/7.x/initials/svg?seed=${member.userEmail}`}
                                                            alt={member.userName}
                                                            className="h-14 w-14 rounded-2xl object-cover border-2 border-background shadow-sm"
                                                        />
                                                        <div className={cn(
                                                            "absolute -bottom-1 -right-1 h-4 w-4 rounded-full border-[3px] border-background",
                                                            member.isActive ? "bg-emerald-500" : "bg-slate-400"
                                                        )} title={member.isActive ? "Active Member" : "Past Member"} />
                                                    </div>

                                                    {/* Info */}
                                                    <div className="flex-1 min-w-0 z-10">
                                                        <div className="flex items-start justify-between">
                                                            <div className="min-w-0 pr-2">
                                                                <div className="flex items-center gap-2">
                                                                    <p className="font-bold truncate leading-tight text-foreground">
                                                                        {member.userName}
                                                                    </p>
                                                                    {isOwner && (
                                                                        <span className="inline-flex items-center rounded-full bg-amber-100 dark:bg-amber-900/30 px-2 py-0.5 text-[10px] font-bold text-amber-700 dark:text-amber-400 shadow-sm whitespace-nowrap border border-amber-200 dark:border-amber-800/50">
                                                                            Owner
                                                                        </span>
                                                                    )}
                                                                </div>
                                                                <p className="text-xs text-muted-foreground mt-0.5 truncate font-medium">
                                                                    {member.userEmail}
                                                                </p>
                                                            </div>

                                                            {/* Actions */}
                                                            <div className="flex items-center gap-0.5 -mt-1 -mr-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                                                {(user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN') && (
                                                                    <Button
                                                                        variant="ghost"
                                                                        size="icon"
                                                                        className="h-7 w-7 text-muted-foreground hover:text-primary hover:bg-primary/10 rounded-full"
                                                                        onClick={() => setViewMemberId(member.userId)}
                                                                        title="View User Details"
                                                                    >
                                                                        <Eye className="h-3.5 w-3.5" />
                                                                    </Button>
                                                                )}

                                                                {isAdminOrOwner && member.userId !== user?.id && (
                                                                    <DropdownMenu>
                                                                        <DropdownMenuTrigger asChild>
                                                                            <Button variant="ghost" size="icon" className="h-7 w-7 text-muted-foreground hover:text-foreground hover:bg-muted rounded-full">
                                                                                <MoreVertical className="h-3.5 w-3.5" />
                                                                            </Button>
                                                                        </DropdownMenuTrigger>
                                                                        <DropdownMenuContent align="end" className="w-52">
                                                                            <DropdownMenuLabel>Member Actions</DropdownMenuLabel>
                                                                            <DropdownMenuSeparator />
                                                                            {member.isActive && (
                                                                                <>
                                                                                    <DropdownMenuItem
                                                                                        onClick={() => handleTransferOwnership(member.userId, member.userName)}
                                                                                        className="text-blue-600 focus:text-blue-700 focus:bg-blue-50"
                                                                                    >
                                                                                        <UserCog className="mr-2 h-4 w-4" />
                                                                                        Transfer Ownership
                                                                                    </DropdownMenuItem>
                                                                                    <DropdownMenuItem
                                                                                        onClick={() => handleRemoveMember(member.userId, member.userName)}
                                                                                        className="text-amber-600 focus:text-amber-700 focus:bg-amber-50"
                                                                                    >
                                                                                        <LogOut className="mr-2 h-4 w-4" />
                                                                                        Mark as Left
                                                                                    </DropdownMenuItem>
                                                                                </>
                                                                            )}
                                                                            <DropdownMenuItem
                                                                                onClick={() => handleUnsignMember(member.userId, member.userName)}
                                                                                className="text-red-600 focus:text-red-700 focus:bg-red-50"
                                                                            >
                                                                                <Trash2 className="mr-2 h-4 w-4" />
                                                                                Delete from History
                                                                            </DropdownMenuItem>
                                                                        </DropdownMenuContent>
                                                                    </DropdownMenu>
                                                                )}
                                                            </div>
                                                        </div>

                                                        <div className="mt-3 flex flex-wrap items-center gap-2">
                                                            {(() => {
                                                                const roleColors: Record<string, string> = {
                                                                    FOUNDER: "bg-indigo-50 text-indigo-700 border-indigo-200",
                                                                    CO_FOUNDER: "bg-indigo-50 text-indigo-600 border-indigo-200",
                                                                    CEO: "bg-purple-50 text-purple-700 border-purple-200",
                                                                    CTO: "bg-cyan-50 text-cyan-700 border-cyan-200",
                                                                    COO: "bg-blue-50 text-blue-700 border-blue-200",
                                                                    CFO: "bg-emerald-50 text-emerald-700 border-emerald-200",
                                                                    CMO: "bg-pink-50 text-pink-700 border-pink-200",
                                                                    CHIEF_PRODUCT_OFFICER: "bg-orange-50 text-orange-700 border-orange-200",
                                                                    DEVELOPER: "bg-slate-50 text-slate-700 border-slate-200",
                                                                    DESIGNER: "bg-rose-50 text-rose-700 border-rose-200",
                                                                    OTHER: "bg-gray-50 text-gray-700 border-gray-200"
                                                                };
                                                                const colorClass = roleColors[member.role] || "bg-gray-50 text-gray-700 border-gray-200";

                                                                return (
                                                                    <span className={cn("inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold border shadow-sm", colorClass)}>
                                                                        {member.role.replace(/_/g, " ")}
                                                                    </span>
                                                                )
                                                            })()}

                                                            <span className="text-[10px] text-muted-foreground pl-1 border-l border-border/50">
                                                                Joined {new Date(member.joinedAt).toLocaleDateString(undefined, { month: 'short', year: 'numeric' })}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            )
                                        })}
                                    </div>
                                </div>
                            )}

                            {/* Description */}
                            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                                <div className="lg:col-span-2 space-y-3">
                                    <h4 className="text-lg font-bold flex items-center gap-2">
                                        <FileText className="h-5 w-5 text-primary" />
                                        About Startup
                                    </h4>
                                    <div className="p-6 rounded-2xl bg-muted/30 border text-sm leading-relaxed whitespace-pre-wrap text-muted-foreground shadow-sm">
                                        {startup.fullDescription}
                                    </div>
                                </div>

                                {/* Documents Grid */}
                                <div className="space-y-3">
                                    <h4 className="text-lg font-bold flex items-center gap-2">
                                        <Target className="h-5 w-5 text-primary" />
                                        Documents
                                    </h4>
                                    <div className="flex flex-col gap-3">
                                        <Button
                                            variant="outline"
                                            className="w-full justify-start h-auto py-3 px-4 border-l-4 border-l-blue-500 hover:bg-blue-50/50 hover:border-l-blue-600 transition-all hover:translate-x-1"
                                            onClick={() => window.open(startup.pitchDeckUrl, '_blank')}
                                            disabled={!startup.pitchDeckUrl}
                                        >
                                            <div className="flex items-center gap-3 w-full">
                                                <div className="p-2 bg-blue-100 text-blue-600 rounded-lg">
                                                    <FileText className="h-4 w-4" />
                                                </div>
                                                <div className="flex flex-col items-start gap-0.5">
                                                    <span className="font-semibold text-foreground">Pitch Deck</span>
                                                    <span className="text-[10px] text-muted-foreground uppercase tracking-wider">Presentation</span>
                                                </div>
                                            </div>
                                        </Button>

                                        <Button
                                            variant="outline"
                                            className="w-full justify-start h-auto py-3 px-4 border-l-4 border-l-emerald-500 hover:bg-emerald-50/50 hover:border-l-emerald-600 transition-all hover:translate-x-1"
                                            onClick={() => window.open(startup.financialDocumentsUrl, '_blank')}
                                            disabled={!startup.financialDocumentsUrl}
                                        >
                                            <div className="flex items-center gap-3 w-full">
                                                <div className="p-2 bg-emerald-100 text-emerald-600 rounded-lg">
                                                    <FileSpreadsheet className="h-4 w-4" />
                                                </div>
                                                <div className="flex flex-col items-start gap-0.5">
                                                    <span className="font-semibold text-foreground">Financials</span>
                                                    <span className="text-[10px] text-muted-foreground uppercase tracking-wider">Spreadsheet</span>
                                                </div>
                                            </div>
                                        </Button>

                                        <Button
                                            variant="outline"
                                            className="w-full justify-start h-auto py-3 px-4 border-l-4 border-l-amber-500 hover:bg-amber-50/50 hover:border-l-amber-600 transition-all hover:translate-x-1"
                                            onClick={() => window.open(startup.businessPlanUrl, '_blank')}
                                            disabled={!startup.businessPlanUrl}
                                        >
                                            <div className="flex items-center gap-3 w-full">
                                                <div className="p-2 bg-amber-100 text-amber-600 rounded-lg">
                                                    <FileText className="h-4 w-4" />
                                                </div>
                                                <div className="flex flex-col items-start gap-0.5">
                                                    <span className="font-semibold text-foreground">Business Plan</span>
                                                    <span className="text-[10px] text-muted-foreground uppercase tracking-wider">Document</span>
                                                </div>
                                            </div>
                                        </Button>

                                        <Button
                                            variant="outline"
                                            className="w-full justify-start h-auto py-3 px-4 border-l-4 border-l-purple-500 hover:bg-purple-50/50 hover:border-l-purple-600 transition-all hover:translate-x-1"
                                            onClick={() => window.open(startup.businessModelUrl, '_blank')}
                                            disabled={!startup.businessModelUrl}
                                        >
                                            <div className="flex items-center gap-3 w-full">
                                                <div className="p-2 bg-purple-100 text-purple-600 rounded-lg">
                                                    <FilePieChart className="h-4 w-4" />
                                                </div>
                                                <div className="flex flex-col items-start gap-0.5">
                                                    <span className="font-semibold text-foreground">Model Canvas</span>
                                                    <span className="text-[10px] text-muted-foreground uppercase tracking-wider">Canvas</span>
                                                </div>
                                            </div>
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )} {/* Close details block */}

                    {activeTab === 'history' && (
                        <div className="px-8 pb-8 pt-4">
                            {loadingLogs ? (
                                <div className="flex flex-col items-center justify-center py-16">
                                    <Loader2 className="h-8 w-8 animate-spin text-primary mb-2" />
                                    <p className="text-muted-foreground">Loading audit logs...</p>
                                </div>
                            ) : !auditLogs || auditLogs.length === 0 ? (
                                <div className="flex flex-col items-center justify-center py-16 text-muted-foreground bg-muted/10 rounded-xl border border-dashed text-center">
                                    <History className="h-12 w-12 mx-auto mb-3 opacity-20" />
                                    <p className="font-medium">No moderation history is available.</p>
                                    <p className="text-xs opacity-70 mt-1">Actions taken by admins will appear here.</p>
                                </div>
                            ) : (
                                <div className="space-y-6 pl-2">
                                    <div className="relative border-l-2 border-muted ml-3 space-y-6 pl-8 pt-2">
                                        {auditLogs.map((log) => (
                                            <div key={log.id} className="relative group">
                                                {/* Timeline Dot */}
                                                <div className="absolute -left-[39px] top-4 h-4 w-4 rounded-full border-2 border-background bg-muted ring-2 ring-muted ring-offset-2 ring-offset-background group-hover:bg-primary transition-colors z-10" />

                                                <div className="p-5 rounded-xl border bg-card/60 hover:bg-card/100 hover:shadow-md transition-all relative">
                                                    {/* Header */}
                                                    <div className="flex items-start justify-between mb-3">
                                                        <div className="flex items-center gap-3">
                                                            <div className={cn(
                                                                "p-1.5 rounded-lg border shadow-sm",
                                                                log.actionType === 'WARNING' ? "bg-amber-100 text-amber-600 border-amber-200" :
                                                                    log.actionType === 'STATUS_CHANGE' ? "bg-blue-100 text-blue-600 border-blue-200" :
                                                                        log.actionType === 'DELETE' ? "bg-red-100 text-red-600 border-red-200" :
                                                                            "bg-gray-100 text-gray-600 border-gray-200"
                                                            )}>
                                                                {log.actionType === 'WARNING' ? <AlertTriangle className="h-4 w-4" /> :
                                                                    log.actionType === 'STATUS_CHANGE' ? <Shield className="h-4 w-4" /> :
                                                                        log.actionType === 'DELETE' ? <Trash2 className="h-4 w-4" /> :
                                                                            <History className="h-4 w-4" />}
                                                            </div>
                                                            <div>
                                                                <h4 className="font-bold text-foreground text-sm uppercase tracking-wide">
                                                                    {log.actionType.replace(/_/g, ' ')}
                                                                </h4>
                                                                <div className="flex items-center gap-2 mt-0.5">
                                                                    {log.newStatus && (
                                                                        <span className="text-xs font-semibold px-2 py-0.5 rounded-md bg-muted text-foreground border">
                                                                            {log.previousStatus} <span className="text-muted-foreground px-1">→</span> {log.newStatus}
                                                                        </span>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div className="flex items-center gap-2">
                                                            <span className="text-xs text-muted-foreground bg-muted/50 px-2 py-1 rounded-md border">
                                                                {formatDate(log.createdAt)}
                                                            </span>
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                className="h-7 w-7 opacity-0 group-hover:opacity-100 transition-all text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                                                                onClick={() => handleDeleteLog(log.id)}
                                                                title="Delete Entry"
                                                            >
                                                                <Trash2 className="h-3.5 w-3.5" />
                                                            </Button>
                                                        </div>
                                                    </div>

                                                    {/* Content */}
                                                    <div className="space-y-3 pl-[3.25rem]">
                                                        <div className="text-sm bg-muted/30 p-3 rounded-lg border border-dashed border-border/60">
                                                            <span className="text-xs font-bold text-muted-foreground uppercase tracking-wider block mb-1">Reason</span>
                                                            <p className="text-foreground/90">{log.reason || <span className="italic text-muted-foreground">No reason provided</span>}</p>
                                                        </div>
                                                        <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                                                            <UserCog className="h-3 w-3" />
                                                            <span>Action taken by:</span>
                                                            <span className="font-medium text-foreground">{log.adminEmail}</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                </div >

                <AlertDialog open={confirmDialog.open} onOpenChange={(open: boolean) => setConfirmDialog(prev => ({ ...prev, open }))}>
                    <AlertDialogContent>
                        <AlertDialogHeader>
                            <AlertDialogTitle>{confirmDialog.title}</AlertDialogTitle>
                            <AlertDialogDescription>
                                {confirmDialog.description}
                            </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                            <AlertDialogCancel>Cancel</AlertDialogCancel>
                            <AlertDialogAction
                                onClick={async (e: MouseEvent<HTMLButtonElement>) => {
                                    e.preventDefault()
                                    await confirmDialog.action()
                                    setConfirmDialog(prev => ({ ...prev, open: false }))
                                }}
                                className={confirmDialog.variant === "destructive" ? "bg-destructive text-destructive-foreground hover:bg-destructive/90" : ""}
                            >
                                Confirm
                            </AlertDialogAction>
                        </AlertDialogFooter>
                    </AlertDialogContent>
                </AlertDialog>

                {
                    isAddMemberOpen && (
                        <AddMemberDialog
                            open={isAddMemberOpen}
                            onOpenChange={setIsAddMemberOpen}
                            startupId={startup.id}
                            existingMemberIds={startup.members?.map(m => m.userId) || []}
                            onSuccess={() => {
                                setIsAddMemberOpen(false)
                                onActionComplete?.()
                                if (startupId) loadStartup()
                            }}
                        />
                    )
                }

                {/* Admin Action Dialogs */}
                {
                    startup && (
                        <>
                            <WarnStartupDialog
                                open={warnDialogOpen}
                                onOpenChange={setWarnDialogOpen}
                                startup={startup}
                                onSuccess={() => {
                                    if (startupId) loadStartup()
                                    onActionComplete?.()
                                }}
                            />
                            <StartupStatusDialog
                                open={statusChangeDialogOpen}
                                onOpenChange={setStatusChangeDialogOpen}
                                startup={startup}
                                onSuccess={() => {
                                    if (startupId) loadStartup()
                                    onActionComplete?.()
                                }}
                            />
                            <DeleteStartupDialog
                                open={deleteDialogOpen}
                                onOpenChange={setDeleteDialogOpen}
                                startup={startup}
                                onSuccess={() => {
                                    onOpenChange(false)
                                    onActionComplete?.()
                                }}
                            />
                        </>
                    )
                }
                <UserDetailsModal
                    open={!!viewMemberId}
                    onOpenChange={(open) => !open && setViewMemberId(null)}
                    userId={viewMemberId}
                />
            </DialogContent >
        </Dialog >
    )
}

function getStatusBadge(status: string) {
    const variants: Record<string, string> = {
        ACTIVE: "bg-emerald-100 text-emerald-700 border-emerald-200 hover:bg-emerald-200",
        APPROVED: "bg-blue-100 text-blue-700 border-blue-200 hover:bg-blue-200",
        PENDING: "bg-amber-100 text-amber-700 border-amber-200 hover:bg-amber-200",
        REJECTED: "bg-red-100 text-red-700 border-red-200 hover:bg-red-200",
        SUSPENDED: "bg-gray-100 text-gray-700 border-gray-200 hover:bg-gray-200",
    }
    return (
        <Badge variant="outline" className={cn("font-bold shadow-sm transition-colors cursor-default", variants[status] || "bg-gray-100 text-gray-600")}>
            {status}
        </Badge>
    )
}

function getStageBadge(stage: string) {
    const colors: Record<string, string> = {
        IDEA: "bg-slate-100 text-slate-700 border-slate-200",
        MVP: "bg-indigo-100 text-indigo-700 border-indigo-200",
        SEED: "bg-emerald-100 text-emerald-700 border-emerald-200",
        SERIES_A: "bg-blue-100 text-blue-700 border-blue-200",
        SERIES_B: "bg-purple-100 text-purple-700 border-purple-200",
        GROWTH: "bg-orange-100 text-orange-700 border-orange-200",
    }
    return (
        <Badge variant="outline" className={cn("font-bold shadow-sm", colors[stage] || "bg-gray-100 text-gray-600")}>
            {stage.replace(/_/g, ' ')}
        </Badge>
    )
}

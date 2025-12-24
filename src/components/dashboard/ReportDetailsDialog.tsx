import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
    DialogDescription,
} from "../ui/dialog"
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
import { Textarea } from "../ui/textarea"
import { Label } from "../ui/label"
import { Badge } from "../ui/badge"
import { Separator } from "../ui/separator"
import {
    Loader2,
    User,
    Building2,
    Flag,
    Eye,
    CheckCircle2,
    XCircle,
    Clock,
    Shield,
    AlertCircle,
    Copy,
    Calendar,
    MessageSquare,
    ExternalLink,
    AlertTriangle,
    ArrowRight,
    Trash2,
    Gavel,
    Ban,
} from 'lucide-react'
import { getReportDetails, Report, updateReportStatus, rejectReport } from '../../lib/api'
import { formatDate } from '../../lib/utils'
import { toast } from 'sonner'
import { Card, CardContent } from '../ui/card'
import { UserDetailsModal } from './UserDetailsModal'
import { StartupDetailsDialog } from './StartupDetailsDialog'
import { ReportResolutionDialog } from './ReportResolutionDialog'
import { Button } from '../ui/button'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../ui/dropdown-menu"
import { MoreVertical } from "lucide-react"
import { WarnStartupDialog, StartupStatusDialog, DeleteStartupDialog } from "./StartupActionDialogs"
import { Startup } from '../../types'
import { cn } from '../../lib/utils'

const REPORT_REASON_LABELS: Record<string, string> = {
    SPAM: 'Spam',
    HARASSMENT: 'Harassment',
    INAPPROPRIATE_CONTENT: 'Inappropriate Content',
    FRAUD: 'Fraud',
    DUPLICATE: 'Duplicate',
    COPYRIGHT: 'Copyright Infringement',
    IMPERSONATION: 'Impersonation',
    ADULT_CONTENT: 'Adult Content',
    VIOLENCE: 'Violence or Threats',
    HATE_SPEECH: 'Hate Speech',
    MISINFORMATION: 'Misinformation',
    OTHER: 'Other',
}

interface ReportDetailsDialogProps {
    reportId: string | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onReportUpdated?: () => void
}

export function ReportDetailsDialog({ reportId, open, onOpenChange, onReportUpdated }: ReportDetailsDialogProps) {
    const [report, setReport] = useState<Report | null>(null)
    const [loading, setLoading] = useState(false)
    const [viewUserId, setViewUserId] = useState<string | null>(null)
    const [viewStartupId, setViewStartupId] = useState<string | null>(null)

    // Action Dialog States for Startup
    const [warnStartupOpen, setWarnStartupOpen] = useState(false)
    const [statusStartupOpen, setStatusStartupOpen] = useState(false)
    const [deleteStartupOpen, setDeleteStartupOpen] = useState(false)

    // Resolution state
    const [resolveDialogOpen, setResolveDialogOpen] = useState(false)

    // Reject state
    const [rejectDialogOpen, setRejectDialogOpen] = useState(false)
    const [rejectReason, setRejectReason] = useState('')
    const [rejectLoading, setRejectLoading] = useState(false)

    useEffect(() => {
        if (open && reportId) {
            loadReport()
        }
    }, [open, reportId])

    const loadReport = async () => {
        if (!reportId) return

        setLoading(true)
        try {
            const data = await getReportDetails(reportId)
            setReport(data)

            // Auto-update status to UNDER_REVIEW if currently PENDING
            if (data.status === 'PENDING') {
                try {
                    await updateReportStatus(reportId, 'UNDER_REVIEW')
                    // Update local state without full reload
                    setReport({ ...data, status: 'UNDER_REVIEW' })
                    // Notify parent list to refresh
                    onReportUpdated?.()
                    toast.info('Report marked as Under Review')
                } catch (err) {
                    console.error('Failed to auto-update status', err)
                }
            }
        } catch (error: any) {
            console.error('Failed to load report:', error)
            toast.error('Failed to load report details')
        } finally {
            setLoading(false)
        }
    }

    const handleViewReported = () => {
        if (!report) return
        if (report.reportedEntityType === 'USER') {
            setViewUserId(report.reportedEntityId)
        } else if (report.reportedEntityType === 'STARTUP') {
            setViewStartupId(report.reportedEntityId)
        }
    }

    const getStatusConfig = (status: string) => {
        switch (status) {
            case 'PENDING': return {
                bg: "bg-amber-100 dark:bg-amber-900/30",
                text: "text-amber-700 dark:text-amber-400",
                border: "border-amber-200 dark:border-amber-800/50",
                icon: Clock,
                label: "Pending Review"
            }
            case 'UNDER_REVIEW': return {
                bg: "bg-blue-100 dark:bg-blue-900/30",
                text: "text-blue-700 dark:text-blue-400",
                border: "border-blue-200 dark:border-blue-800/50",
                icon: Eye,
                label: "Under Investigation"
            }
            case 'RESOLVED': return {
                bg: "bg-emerald-100 dark:bg-emerald-900/30",
                text: "text-emerald-700 dark:text-emerald-400",
                border: "border-emerald-200 dark:border-emerald-800/50",
                icon: CheckCircle2,
                label: "Resolved"
            }
            case 'REJECTED': return {
                bg: "bg-rose-100 dark:bg-rose-900/30",
                text: "text-rose-700 dark:text-rose-400",
                border: "border-rose-200 dark:border-rose-800/50",
                icon: XCircle,
                label: "Rejected"
            }
            default: return {
                bg: "bg-slate-100 dark:bg-slate-800",
                text: "text-slate-700 dark:text-slate-400",
                border: "border-slate-200 dark:border-slate-700",
                icon: AlertCircle,
                label: status
            }
        }
    }

    const CopyButton = ({ value }: { value: string }) => (
        <button
            onClick={(e) => {
                e.stopPropagation()
                navigator.clipboard.writeText(value)
                toast.success('ID copied!')
            }}
            className="p-1 hover:bg-muted rounded transition-all text-muted-foreground hover:text-foreground"
        >
            <Copy className="h-3 w-3" />
        </button>
    )

    if (!report && !loading) return null

    const statusConfig = report ? getStatusConfig(report.status) : null

    // Helper to create a temporary startup object for actions
    const tempStartup = report && report.reportedEntityType === 'STARTUP' ? {
        id: report.reportedEntityId,
        name: report.reportedEntityName || 'Startup',
        status: report.reportedEntityStatus || 'PENDING', // Default if missing
        // Other fields invalid but not needed for these specific dialogs
    } as Startup : null


    return (
        <>
            <Dialog open={open} onOpenChange={onOpenChange}>
                <DialogContent className="max-w-5xl p-0 gap-0 overflow-hidden bg-white dark:bg-slate-950 border-slate-200 dark:border-slate-800 sm:rounded-2xl max-h-[95vh] flex flex-col shadow-2xl [&>button]:hidden">
                    {/* Accessibility: visually hidden title and description */}
                    <DialogTitle className="sr-only">Report Details</DialogTitle>
                    <DialogDescription className="sr-only">View and manage report details, change status, and take actions</DialogDescription>

                    {loading ? (
                        <div className="flex flex-col items-center justify-center py-32 space-y-4">
                            <div className="relative">
                                <div className="absolute inset-0 bg-primary/20 blur-xl rounded-full" />
                                <Loader2 className="h-12 w-12 animate-spin text-primary relative z-10" />
                            </div>
                            <p className="text-muted-foreground font-medium animate-pulse">Retrieving report details...</p>
                        </div>
                    ) : report && statusConfig ? (
                        <>
                            {/* Header Section */}
                            <div className="bg-slate-50 dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 p-5 shrink-0">
                                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                                    <div className="space-y-1">
                                        <div className="flex items-center gap-3">
                                            <div className="p-2 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg text-indigo-600 dark:text-indigo-400">
                                                <Shield className="h-5 w-5" />
                                            </div>
                                            <div className="space-y-0.5">
                                                <h2 className="text-xl font-bold tracking-tight text-foreground flex items-center gap-2">
                                                    Report Details
                                                    <span className="text-muted-foreground font-normal text-base">#{report.id.substring(0, 8)}</span>
                                                </h2>
                                                <div className="flex items-center gap-3 text-xs text-muted-foreground">
                                                    <span className="flex items-center gap-1.5">
                                                        <Calendar className="h-3.5 w-3.5" />
                                                        {formatDate(report.createdAt)}
                                                    </span>
                                                    <Separator orientation="vertical" className="h-3 bg-border sm:block hidden" />
                                                    <div className="flex items-center gap-1.5 group cursor-pointer hover:text-foreground transition-colors" onClick={() => { navigator.clipboard.writeText(report.id); toast.success('Full ID Copied') }}>
                                                        <Copy className="h-3 w-3" />
                                                        <span className="font-mono opacity-80">Copy UUID</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Actions & Status */}
                                    <div className="flex items-center gap-3">
                                        <Badge variant="outline" className={cn("gap-1.5 py-1.5 px-3 text-sm font-medium border-transparent shadow-sm", statusConfig.bg, statusConfig.text)}>
                                            <statusConfig.icon className="h-4 w-4" />
                                            {statusConfig.label}
                                        </Badge>

                                        {(report.status !== 'RESOLVED' && report.status !== 'REJECTED') && (
                                            <div className="flex items-center gap-2">
                                                <DropdownMenu>
                                                    <DropdownMenuTrigger asChild>
                                                        <Button
                                                            variant="outline"
                                                            size="sm"
                                                            className="gap-2 h-9 bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-indigo-600 dark:hover:text-indigo-400 transition-all shadow-sm"
                                                        >
                                                            <MoreVertical className="h-4 w-4" />
                                                            Actions
                                                        </Button>
                                                    </DropdownMenuTrigger>
                                                    <DropdownMenuContent
                                                        align="end"
                                                        className="w-56 bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 shadow-xl rounded-xl p-1.5"
                                                    >
                                                        <DropdownMenuLabel className="text-xs font-semibold text-muted-foreground uppercase tracking-wider px-2 py-1.5">
                                                            Status Actions
                                                        </DropdownMenuLabel>

                                                        <DropdownMenuItem
                                                            className="cursor-pointer rounded-lg px-2 py-2 text-sm font-medium transition-colors hover:bg-slate-100 dark:hover:bg-slate-800 focus:bg-slate-100 dark:focus:bg-slate-800 text-slate-700 dark:text-slate-200"
                                                            onClick={async () => {
                                                                if (report.status === 'PENDING') return
                                                                try {
                                                                    await updateReportStatus(report.id, 'PENDING')
                                                                    setReport({ ...report, status: 'PENDING' })
                                                                    onReportUpdated?.()
                                                                    toast.success('Report marked as Pending')
                                                                } catch (err) {
                                                                    toast.error('Failed to update status')
                                                                }
                                                            }}
                                                            disabled={report.status === 'PENDING'}
                                                        >
                                                            <div className="flex items-center gap-2 w-full">
                                                                <div className="p-1.5 rounded-md bg-amber-50 dark:bg-amber-900/20 text-amber-600 dark:text-amber-400">
                                                                    <Clock className="h-4 w-4" />
                                                                </div>
                                                                <span>Mark as Pending</span>
                                                            </div>
                                                        </DropdownMenuItem>

                                                        <DropdownMenuItem
                                                            className="cursor-pointer rounded-lg px-2 py-2 text-sm font-medium transition-colors hover:bg-slate-100 dark:hover:bg-slate-800 focus:bg-slate-100 dark:focus:bg-slate-800 text-slate-700 dark:text-slate-200 mt-1"
                                                            onClick={async () => {
                                                                if (report.status === 'UNDER_REVIEW') return
                                                                try {
                                                                    await updateReportStatus(report.id, 'UNDER_REVIEW')
                                                                    setReport({ ...report, status: 'UNDER_REVIEW' })
                                                                    onReportUpdated?.()
                                                                    toast.success('Report marked as Under Review')
                                                                } catch (err) {
                                                                    toast.error('Failed to update status')
                                                                }
                                                            }}
                                                            disabled={report.status === 'UNDER_REVIEW'}
                                                        >
                                                            <div className="flex items-center gap-2 w-full">
                                                                <div className="p-1.5 rounded-md bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400">
                                                                    <Eye className="h-4 w-4" />
                                                                </div>
                                                                <span>Mark Under Review</span>
                                                            </div>
                                                        </DropdownMenuItem>

                                                        <DropdownMenuSeparator className="my-2 bg-slate-100 dark:bg-slate-800" />

                                                        <DropdownMenuLabel className="text-xs font-semibold text-muted-foreground uppercase tracking-wider px-2 py-1.5">
                                                            Resolution
                                                        </DropdownMenuLabel>

                                                        <DropdownMenuItem
                                                            onClick={() => setResolveDialogOpen(true)}
                                                            className="cursor-pointer rounded-lg px-2 py-2 text-sm font-medium transition-colors hover:bg-indigo-50 dark:hover:bg-indigo-900/20 focus:bg-indigo-50 dark:focus:bg-indigo-900/20 text-indigo-700 dark:text-indigo-300"
                                                        >
                                                            <div className="flex items-center gap-2 w-full">
                                                                <div className="p-1.5 rounded-md bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400">
                                                                    <Gavel className="h-4 w-4" />
                                                                </div>
                                                                <span>Resolve Report</span>
                                                            </div>
                                                        </DropdownMenuItem>

                                                        <DropdownMenuItem
                                                            onClick={() => setRejectDialogOpen(true)}
                                                            className="cursor-pointer rounded-lg px-2 py-2 text-sm font-medium transition-colors hover:bg-rose-50 dark:hover:bg-rose-900/20 focus:bg-rose-50 dark:focus:bg-rose-900/20 text-rose-700 dark:text-rose-300 mt-1"
                                                        >
                                                            <div className="flex items-center gap-2 w-full">
                                                                <div className="p-1.5 rounded-md bg-rose-100 dark:bg-rose-900/40 text-rose-600 dark:text-rose-400">
                                                                    <Ban className="h-4 w-4" />
                                                                </div>
                                                                <span>Reject Report</span>
                                                            </div>
                                                        </DropdownMenuItem>
                                                    </DropdownMenuContent>
                                                </DropdownMenu>
                                            </div>
                                        )}

                                        <div className="h-6 w-px bg-border mx-1" />

                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => onOpenChange(false)}
                                            className="rounded-full bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 transition-colors h-8 w-8"
                                        >
                                            <XCircle className="h-5 w-5 opacity-70" />
                                        </Button>
                                    </div>
                                </div>
                            </div>

                            <div className="flex-1 overflow-y-auto bg-white dark:bg-slate-950">
                                <div className="p-6 space-y-8">
                                    {/* ENTITIES ROW - SIDE BY SIDE */}
                                    <div className="grid md:grid-cols-2 gap-6">

                                        {/* REPORTER (LEFT) */}
                                        <Card className="group relative overflow-hidden bg-gradient-to-br from-indigo-50/50 to-background dark:from-indigo-950/10 dark:to-background border-indigo-100 dark:border-indigo-900/30 hover:shadow-md transition-all">
                                            <CardContent className="p-5">
                                                <div className="flex justify-between items-center mb-4 border-b border-indigo-100 dark:border-indigo-900/30 pb-3">
                                                    <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-indigo-600 dark:text-indigo-400">
                                                        <MessageSquare className="h-3.5 w-3.5" />
                                                        Reporter
                                                    </div>
                                                    <Button variant="ghost" size="sm" className="h-7 text-xs text-indigo-600 dark:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-900/20" onClick={() => setViewUserId(report.reporterId)}>
                                                        View Profile <ArrowRight className="h-3 w-3 ml-1" />
                                                    </Button>
                                                </div>

                                                <div className="flex items-center gap-4">
                                                    <div className="relative">
                                                        <div className="h-14 w-14 rounded-full bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0 ring-4 ring-background shadow-sm overflow-hidden">
                                                            {report.reporterImage ? (
                                                                <img src={report.reporterImage} alt="Reporter" className="h-full w-full object-cover" />
                                                            ) : (
                                                                <User className="h-7 w-7" />
                                                            )}
                                                        </div>
                                                        <div className="absolute -bottom-1 -right-1 bg-background rounded-full p-0.5">
                                                            <div className="bg-indigo-500 h-3 w-3 rounded-full border-2 border-background" />
                                                        </div>
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <p className="font-bold text-lg text-foreground truncate">{report.reporterName || 'Platform User'}</p>
                                                        <div className="flex items-center gap-2 mt-1">
                                                            <Badge variant="secondary" className="font-mono text-[10px] bg-indigo-50 text-indigo-700 dark:bg-indigo-900/20 dark:text-indigo-300 pointer-events-none">
                                                                ID: {report.reporterId.substring(0, 8)}...
                                                            </Badge>
                                                            <CopyButton value={report.reporterId} />
                                                        </div>
                                                    </div>
                                                </div>
                                            </CardContent>
                                        </Card>

                                        {/* TARGET (RIGHT) */}
                                        <Card className="group relative overflow-hidden bg-gradient-to-br from-orange-50/50 to-background dark:from-orange-950/10 dark:to-background border-orange-100 dark:border-orange-900/30 hover:shadow-md transition-all">
                                            <CardContent className="p-5">
                                                <div className="flex justify-between items-center mb-4 border-b border-orange-100 dark:border-orange-900/30 pb-3">
                                                    <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-orange-600 dark:text-orange-400">
                                                        <AlertTriangle className="h-3.5 w-3.5" />
                                                        Reported Entity
                                                    </div>
                                                    <div className="flex items-center gap-1">
                                                        {report.reportedEntityType === 'STARTUP' && (
                                                            <DropdownMenu>
                                                                <DropdownMenuTrigger asChild>
                                                                    <Button variant="ghost" size="icon" className="h-7 w-7 text-orange-600 hover:text-orange-700 hover:bg-orange-50 dark:hover:bg-orange-900/20">
                                                                        <MoreVertical className="h-3.5 w-3.5" />
                                                                    </Button>
                                                                </DropdownMenuTrigger>
                                                                <DropdownMenuContent align="end">
                                                                    <DropdownMenuLabel>Quick Actions</DropdownMenuLabel>
                                                                    <DropdownMenuItem onClick={() => setViewStartupId(report.reportedEntityId)}>
                                                                        <Eye className="mr-2 h-4 w-4" />
                                                                        Manage Startup
                                                                    </DropdownMenuItem>
                                                                    <DropdownMenuSeparator />
                                                                    <DropdownMenuItem onClick={() => setWarnStartupOpen(true)} className="text-amber-600">
                                                                        <AlertTriangle className="mr-2 h-4 w-4" />
                                                                        Issue Warning
                                                                    </DropdownMenuItem>
                                                                    <DropdownMenuItem onClick={() => setStatusStartupOpen(true)} className="text-indigo-600">
                                                                        <Shield className="mr-2 h-4 w-4" />
                                                                        Change Status
                                                                    </DropdownMenuItem>
                                                                    <DropdownMenuItem onClick={() => setDeleteStartupOpen(true)} className="text-red-600">
                                                                        <Trash2 className="mr-2 h-4 w-4" />
                                                                        Delete Startup
                                                                    </DropdownMenuItem>
                                                                </DropdownMenuContent>
                                                            </DropdownMenu>
                                                        )}
                                                        <Button variant="ghost" size="sm" className="h-7 text-xs text-orange-600 dark:text-orange-400 hover:bg-orange-50 dark:hover:bg-orange-900/20" onClick={handleViewReported}>
                                                            View Details <ExternalLink className="h-3 w-3 ml-1" />
                                                        </Button>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-4">
                                                    <div className="relative">
                                                        <div className="h-14 w-14 rounded-xl bg-orange-100 dark:bg-orange-900/40 text-orange-600 dark:text-orange-400 flex items-center justify-center shrink-0 ring-4 ring-background shadow-sm overflow-hidden">
                                                            {report.reportedEntityImage ? (
                                                                <img src={report.reportedEntityImage} alt="Entity" className="h-full w-full object-cover" />
                                                            ) : (
                                                                report.reportedEntityType === 'USER' ? <User className="h-7 w-7" /> : <Building2 className="h-7 w-7" />
                                                            )}
                                                        </div>
                                                        <div className="absolute -bottom-1 -right-1 bg-background rounded-full p-0.5">
                                                            <div className="bg-orange-500 h-3 w-3 rounded-full border-2 border-background" />
                                                        </div>
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <p className="font-bold text-lg text-foreground capitalize truncate">{report.reportedEntityName || `${report.reportedEntityType.toLowerCase()} Account`}</p>
                                                        <div className="flex items-center gap-2 mt-1">
                                                            <Badge variant="secondary" className="font-mono text-[10px] bg-orange-50 text-orange-700 dark:bg-orange-900/20 dark:text-orange-300 pointer-events-none">
                                                                ID: {report.reportedEntityId.substring(0, 8)}...
                                                            </Badge>
                                                            <CopyButton value={report.reportedEntityId} />
                                                        </div>
                                                    </div>
                                                </div>
                                            </CardContent>
                                        </Card>
                                    </div>

                                    {/* REPORT DETAILS CONTENT */}
                                    <div className="grid lg:grid-cols-3 gap-8">

                                        {/* MAIN CONTENT (Description) */}
                                        <div className="lg:col-span-2 space-y-6">
                                            <div className="space-y-3">
                                                <h3 className="text-sm font-semibold text-foreground flex items-center gap-2">
                                                    <div className="p-1.5 bg-background border rounded-md shadow-sm">
                                                        <Flag className="h-3.5 w-3.5 text-red-500" />
                                                    </div>
                                                    Report Description
                                                </h3>
                                                <div className="bg-white dark:bg-slate-900 p-6 rounded-xl border border-slate-200 dark:border-slate-800 text-sm leading-relaxed text-foreground shadow-sm min-h-[120px] relative">
                                                    {/* Quote Icon Background */}
                                                    <div className="absolute top-4 right-4 text-muted-foreground/10">
                                                        <MessageSquare className="h-12 w-12" />
                                                    </div>
                                                    <div className="relative z-10">
                                                        {report.description}
                                                    </div>
                                                </div>
                                            </div>

                                            {/* RESOLUTION SECTION (Conditional) */}
                                            {(report.status === 'RESOLVED' || report.status === 'REJECTED') && (
                                                <div className="space-y-3 pt-4 border-t border-dashed">
                                                    <h3 className="text-sm font-semibold text-foreground flex items-center gap-2">
                                                        <div className="p-1.5 bg-background border rounded-md shadow-sm">
                                                            <Shield className="h-3.5 w-3.5 text-emerald-600" />
                                                        </div>
                                                        Resolution Outcome
                                                    </h3>
                                                    <div className="bg-gradient-to-br from-emerald-50/50 to-background dark:from-emerald-950/10 dark:to-background border border-emerald-100 dark:border-emerald-900/30 rounded-xl overflow-hidden shadow-sm">
                                                        <div className="p-5 grid sm:grid-cols-2 gap-6">
                                                            <div className="space-y-1">
                                                                <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Action Taken</span>
                                                                <div className="flex items-center gap-2">
                                                                    <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800 px-3 py-1 text-sm">
                                                                        {report.adminAction || 'None'}
                                                                    </Badge>
                                                                </div>
                                                            </div>
                                                            <div className="space-y-1">
                                                                <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Resolved On</span>
                                                                <div className="text-sm font-medium flex items-center gap-1.5">
                                                                    <Calendar className="h-3.5 w-3.5 text-emerald-600" />
                                                                    {report.resolvedAt ? formatDate(report.resolvedAt) : 'N/A'}
                                                                </div>
                                                            </div>
                                                        </div>
                                                        {(report.adminNotes || report.actionDetails) && (
                                                            <div className="px-5 py-4 border-t border-emerald-100 dark:border-emerald-900/30 space-y-4 bg-emerald-50/30 dark:bg-emerald-950/5">
                                                                {report.actionDetails && (
                                                                    <div className="space-y-1.5">
                                                                        <span className="text-xs font-semibold text-emerald-700 dark:text-emerald-400">Public Details</span>
                                                                        <p className="text-sm text-foreground/80">{report.actionDetails}</p>
                                                                    </div>
                                                                )}
                                                                {report.adminNotes && (
                                                                    <div className="space-y-1.5">
                                                                        <span className="text-xs font-semibold text-emerald-700 dark:text-emerald-400">Internal Notes</span>
                                                                        <div className="bg-background/50 p-3 rounded-lg border border-emerald-100 dark:border-emerald-900/20 text-sm text-muted-foreground italic">
                                                                            "{report.adminNotes}"
                                                                        </div>
                                                                    </div>
                                                                )}
                                                            </div>
                                                        )}
                                                        <div className="px-5 py-3 border-t border-emerald-100 dark:border-emerald-900/30 flex items-center gap-3 text-xs bg-emerald-50/50 dark:bg-emerald-950/10">
                                                            <span className="font-semibold text-emerald-700 dark:text-emerald-400">Notifications:</span>
                                                            <div className="flex items-center gap-3">
                                                                {report.notifyReporter ? (
                                                                    <span className="flex items-center gap-1.5 text-emerald-600"><CheckCircle2 className="h-3 w-3" /> Reporter Notified</span>
                                                                ) : (
                                                                    <span className="flex items-center gap-1.5 text-muted-foreground"><XCircle className="h-3 w-3" /> Reporter Skipped</span>
                                                                )}
                                                                <span className="text-emerald-200">|</span>
                                                                {report.notifyReportedEntity ? (
                                                                    <span className="flex items-center gap-1.5 text-emerald-600"><CheckCircle2 className="h-3 w-3" /> Entity Notified</span>
                                                                ) : (
                                                                    <span className="flex items-center gap-1.5 text-muted-foreground"><XCircle className="h-3 w-3" /> Entity Skipped</span>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            )}
                                        </div>

                                        {/* SIDEBAR METADATA */}
                                        <div className="space-y-6">
                                            <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm p-0 overflow-hidden">
                                                <div className="bg-slate-50 dark:bg-slate-800/50 p-3 border-b border-slate-200 dark:border-slate-800">
                                                    <h4 className="text-xs font-bold text-foreground uppercase tracking-wider flex items-center gap-2">
                                                        <Eye className="h-3.5 w-3.5" />
                                                        Report Metadata
                                                    </h4>
                                                </div>
                                                <div className="p-4 space-y-4">
                                                    <div className="flex justify-between items-center text-sm">
                                                        <span className="text-muted-foreground">Type</span>
                                                        <Badge variant="outline" className="bg-background font-medium">{report.reportedEntityType}</Badge>
                                                    </div>
                                                    <div className="flex justify-between items-center text-sm">
                                                        <span className="text-muted-foreground">Reason</span>
                                                        <span className="font-medium text-foreground">{REPORT_REASON_LABELS[report.reason] || report.reason}</span>
                                                    </div>
                                                    <Separator className="bg-border/60" />
                                                    <div className="space-y-1.5">
                                                        <span className="text-xs text-muted-foreground uppercase tracking-wider">Submission Date</span>
                                                        <div className="flex items-center gap-2 text-sm font-medium text-foreground">
                                                            <Calendar className="h-4 w-4 text-indigo-500" />
                                                            {formatDate(report.createdAt)}
                                                        </div>
                                                    </div>
                                                    <div className="space-y-1.5">
                                                        <span className="text-xs text-muted-foreground uppercase tracking-wider">Last Update</span>
                                                        <div className="flex items-center gap-2 text-sm font-medium text-foreground">
                                                            <Clock className="h-4 w-4 text-orange-500" />
                                                            {formatDate(report.updatedAt)}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </>
                    ) : null}
                </DialogContent>
            </Dialog >

            <UserDetailsModal
                open={!!viewUserId}
                onOpenChange={(open: boolean) => !open && setViewUserId(null)}
                userId={viewUserId}
            />

            <StartupDetailsDialog
                open={!!viewStartupId}
                onOpenChange={(open) => !open && setViewStartupId(null)}
                startupId={viewStartupId}
                onActionComplete={() => {
                    // Refresh report if needed, though mostly unrelated unless status changed
                    if (reportId) loadReport()
                }}
            />

            {report && (
                <ReportResolutionDialog
                    open={resolveDialogOpen}
                    onOpenChange={setResolveDialogOpen}
                    report={report}
                    onSuccess={() => {
                        if (reportId) loadReport()
                        onReportUpdated?.()
                    }}
                />
            )}

            {/* Startup Action Dialogs */}
            {
                tempStartup && (
                    <>
                        <WarnStartupDialog
                            open={warnStartupOpen}
                            onOpenChange={setWarnStartupOpen}
                            startup={tempStartup!}
                            onSuccess={() => { if (reportId) loadReport() }}
                        />
                        <StartupStatusDialog
                            open={statusStartupOpen}
                            onOpenChange={setStatusStartupOpen}
                            startup={tempStartup!}
                            onSuccess={() => { if (reportId) loadReport() }}
                        />
                        <DeleteStartupDialog
                            open={deleteStartupOpen}
                            onOpenChange={setDeleteStartupOpen}
                            startup={tempStartup!}
                            onSuccess={() => {
                                onOpenChange(false) // Close report dialog if entity deleted? Maybe just refresh
                                if (reportId) loadReport()
                            }}
                        />
                    </>
                )
            }
            {/* Reject Report Dialog */}
            <AlertDialog open={rejectDialogOpen} onOpenChange={(open) => {
                setRejectDialogOpen(open)
                if (!open) setRejectReason('')
            }}>
                <AlertDialogContent className="max-w-md bg-white dark:bg-slate-950 border-rose-200 dark:border-rose-800/50">
                    <AlertDialogHeader>
                        <div className="mx-auto h-14 w-14 rounded-2xl bg-gradient-to-br from-rose-100 to-red-100 dark:from-rose-900/30 dark:to-red-900/30 shadow-lg flex items-center justify-center mb-3">
                            <Ban className="h-7 w-7 text-rose-600 dark:text-rose-400" />
                        </div>
                        <AlertDialogTitle className="text-xl font-bold text-center text-foreground">
                            Reject Report?
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-center text-muted-foreground">
                            This will mark the report as invalid or false. This action updates reporter statistics.
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <div className="space-y-3 py-4">
                        <div className="space-y-2">
                            <Label className="text-sm font-medium text-foreground">
                                Rejection Reason <span className="text-muted-foreground font-normal">(optional)</span>
                            </Label>
                            <Textarea
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                                placeholder="E.g., No evidence of violation found, Duplicate report, etc."
                                className="h-24 resize-none bg-slate-50 dark:bg-slate-900 border-2 border-rose-200 dark:border-rose-800/50 focus:border-rose-400 dark:focus:border-rose-600 rounded-lg placeholder:text-slate-400"
                            />
                        </div>
                    </div>

                    <AlertDialogFooter className="gap-3">
                        <AlertDialogCancel
                            className="flex-1 border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800"
                            disabled={rejectLoading}
                        >
                            Cancel
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={async (e: React.MouseEvent<HTMLButtonElement>) => {
                                e.preventDefault()
                                if (!report) return

                                setRejectLoading(true)
                                try {
                                    await rejectReport(report.id, rejectReason || undefined)
                                    toast.success('Report rejected successfully')
                                    setRejectDialogOpen(false)
                                    setRejectReason('')
                                    if (reportId) loadReport()
                                    onReportUpdated?.()
                                } catch (error: any) {
                                    toast.error(error.message || 'Failed to reject report')
                                } finally {
                                    setRejectLoading(false)
                                }
                            }}
                            disabled={rejectLoading}
                            className="flex-1 bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-700 hover:to-red-700 text-white shadow-lg shadow-rose-500/25 border-0"
                        >
                            {rejectLoading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Rejecting...
                                </>
                            ) : (
                                'Reject Report'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </>
    )
}
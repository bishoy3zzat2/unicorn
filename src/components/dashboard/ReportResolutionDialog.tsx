import { useState } from 'react'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "../ui/dialog"
import { Button } from "../ui/button"
import { Textarea } from "../ui/textarea"
import { Label } from "../ui/label"
import { Checkbox } from "../ui/checkbox"
import {
    Loader2,
    Mail,
    Bell,
    ShieldAlert,
    Trash2,
    Ban,
    UserX,
    CheckCircle2,
    AlertTriangle
} from "lucide-react"
import { toast } from "sonner"
import { resolveReport, Report, NotificationChannel } from "../../lib/api"
import { cn } from "../../lib/utils"

interface ReportResolutionDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    report: Report
    onSuccess: () => void
}

const ACTION_OPTIONS = [
    {
        value: 'NO_ACTION',
        label: 'Dismiss Report',
        description: 'No violation found. Clear report.',
        icon: CheckCircle2,
        color: 'text-slate-600 dark:text-slate-400',
        bg: 'bg-slate-100 dark:bg-slate-800',
        hover: 'hover:bg-slate-200 dark:hover:bg-slate-700',
        border: 'border-slate-200 dark:border-slate-700'
    },
    {
        value: 'WARNING',
        label: 'Issue Warning',
        description: 'Send a formal warning to the user.',
        icon: AlertTriangle,
        color: 'text-amber-600 dark:text-amber-400',
        bg: 'bg-amber-50 dark:bg-amber-950/30',
        hover: 'hover:bg-amber-100 dark:hover:bg-amber-900/50',
        border: 'border-amber-200 dark:border-amber-800'
    },
    {
        value: 'CONTENT_REMOVED',
        label: 'Remove Content',
        description: 'Delete the reported content.',
        icon: Trash2,
        color: 'text-orange-600 dark:text-orange-400',
        bg: 'bg-orange-50 dark:bg-orange-950/30',
        hover: 'hover:bg-orange-100 dark:hover:bg-orange-900/50',
        border: 'border-orange-200 dark:border-orange-800'
    },
    {
        value: 'ACCOUNT_SUSPENDED',
        label: 'Suspend Account',
        description: 'Temporarily restrict access.',
        icon: UserX,
        color: 'text-rose-600 dark:text-rose-400',
        bg: 'bg-rose-50 dark:bg-rose-950/30',
        hover: 'hover:bg-rose-100 dark:hover:bg-rose-900/50',
        border: 'border-rose-200 dark:border-rose-800'
    },
    {
        value: 'ACCOUNT_BANNED',
        label: 'Ban Account',
        description: 'Permanently block access.',
        icon: Ban,
        color: 'text-red-700 dark:text-red-500',
        bg: 'bg-red-50 dark:bg-red-950/30',
        hover: 'hover:bg-red-100 dark:hover:bg-red-900/50',
        border: 'border-red-200 dark:border-red-800'
    },
]

export function ReportResolutionDialog({ open, onOpenChange, report, onSuccess }: ReportResolutionDialogProps) {
    const [action, setAction] = useState<string>('WARNING')
    const [adminNotes, setAdminNotes] = useState('')
    const [resolutionMessage, setResolutionMessage] = useState('')

    // Notification States
    const [notifyReporter, setNotifyReporter] = useState(true)
    const [reporterChannels, setReporterChannels] = useState<NotificationChannel[]>(['IN_APP'])

    const [notifyReportedEntity, setNotifyReportedEntity] = useState(false)
    const [reportedEntityChannels, setReportedEntityChannels] = useState<NotificationChannel[]>(['IN_APP'])

    const [loading, setLoading] = useState(false)

    const handleSubmit = async () => {
        if (!action) {
            toast.error("Please select a resolution action")
            return
        }

        try {
            setLoading(true)
            await resolveReport(report.id, {
                adminAction: action,
                adminNotes: adminNotes,
                actionDetails: resolutionMessage,
                notifyReporter: notifyReporter,
                reporterNotificationChannels: notifyReporter ? reporterChannels : [],
                notifyReportedEntity: notifyReportedEntity,
                reportedEntityNotificationChannels: notifyReportedEntity ? reportedEntityChannels : []
            })
            toast.success("Report resolved successfully")
            onSuccess()
            onOpenChange(false)
        } catch (error) {
            toast.error("Failed to resolve report")
            console.error(error)
        } finally {
            setLoading(false)
        }
    }

    const toggleChannel = (
        channel: NotificationChannel,
        current: NotificationChannel[],
        set: (vals: NotificationChannel[]) => void
    ) => {
        if (current.includes(channel)) {
            set(current.filter(c => c !== channel))
        } else {
            set([...current, channel])
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-3xl max-h-[85vh] p-0 gap-0 overflow-hidden bg-white dark:bg-slate-950 border-none shadow-2xl flex flex-col">
                {/* Gradient Header */}
                <div className="bg-gradient-to-r from-slate-800 via-indigo-900/50 to-purple-900/50 dark:from-slate-900 dark:via-indigo-950/80 dark:to-purple-950/80 p-6 shrink-0 border-b border-slate-700/50">
                    <DialogHeader className="space-y-2">
                        <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white">
                            <div className="h-12 w-12 rounded-2xl bg-white/20 backdrop-blur-sm shadow-lg flex items-center justify-center">
                                <ShieldAlert className="h-6 w-6 text-white" />
                            </div>
                            Resolve Report
                        </DialogTitle>
                        <DialogDescription className="text-white/80">
                            Review case details and determine the final outcome.
                        </DialogDescription>
                    </DialogHeader>
                </div>

                {/* Scrollable Content Area */}
                <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-white dark:bg-slate-950">
                    {/* Action Selection Grid - Reduced Size */}
                    <div className="space-y-3">
                        <Label className="text-sm font-semibold text-foreground">Select Action</Label>
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
                            {ACTION_OPTIONS.map(opt => {
                                const isSelected = action === opt.value
                                return (
                                    <div
                                        key={opt.value}
                                        onClick={() => setAction(opt.value)}
                                        className={cn(
                                            "relative cursor-pointer rounded-lg border px-3 py-2.5 transition-all duration-200 flex flex-col items-start gap-1.5 h-full",
                                            isSelected
                                                ? cn(opt.bg, opt.border, "ring-1 ring-offset-0 ring-indigo-500 shadow-sm")
                                                : "bg-card border-border hover:shadow-sm opacity-80 hover:opacity-100 hover:bg-accent/50"
                                        )}
                                    >
                                        <div className="flex w-full items-start justify-between gap-2">
                                            <div className="flex items-center gap-2">
                                                <div className={cn("p-1.5 rounded-md bg-background/80 shadow-sm", opt.color)}>
                                                    <opt.icon className="h-3.5 w-3.5" />
                                                </div>
                                                <h4 className={cn("font-medium text-sm leading-none", isSelected ? "text-foreground" : "text-muted-foreground")}>
                                                    {opt.label}
                                                </h4>
                                            </div>
                                            {isSelected && (
                                                <CheckCircle2 className="h-4 w-4 text-indigo-600 dark:text-indigo-400" />
                                            )}
                                        </div>
                                        <p className="text-[11px] text-muted-foreground leading-tight pl-[30px]">
                                            {opt.description}
                                        </p>
                                    </div>
                                )
                            })}
                        </div>
                    </div>

                    <div className="grid gap-6 md:grid-cols-2">
                        {/* Public Message */}
                        <div className="space-y-2">
                            <Label className="flex items-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400">
                                <Mail className="h-3.5 w-3.5" />
                                Public Resolution Details
                            </Label>
                            <Textarea
                                value={resolutionMessage}
                                onChange={e => setResolutionMessage(e.target.value)}
                                placeholder="Explain the decision. This message will be included in notifications sent to users."
                                className="h-28 text-sm resize-none bg-white dark:bg-slate-900 border-2 border-indigo-200 dark:border-indigo-800/50 rounded-lg shadow-sm placeholder:text-slate-400"
                            />
                        </div>

                        {/* Private Notes */}
                        <div className="space-y-2">
                            <Label className="flex items-center gap-2 text-sm font-semibold text-slate-600 dark:text-slate-400">
                                <ShieldAlert className="h-3.5 w-3.5" />
                                Internal Admin Notes
                            </Label>
                            <Textarea
                                value={adminNotes}
                                onChange={e => setAdminNotes(e.target.value)}
                                placeholder="Private notes for team reference only. Not visible to users."
                                className="h-28 text-sm resize-none bg-amber-50 dark:bg-amber-950/30 border-2 border-amber-300 dark:border-amber-700/50 rounded-lg shadow-sm placeholder:text-amber-500/70"
                            />
                        </div>
                    </div>

                    {/* Notification Configuration */}
                    <div className="rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden bg-white dark:bg-slate-900">
                        <div className="bg-slate-50 dark:bg-slate-800/50 px-4 py-3 border-b border-slate-200 dark:border-slate-800">
                            <h4 className="font-semibold text-xs uppercase tracking-wider text-slate-600 dark:text-slate-400 flex items-center gap-2">
                                <Bell className="h-3.5 w-3.5" />
                                Notifications
                            </h4>
                        </div>

                        <div className="p-4 grid gap-4 grid-cols-1 md:grid-cols-2 divide-y md:divide-y-0 md:divide-x divide-border">
                            {/* Notify Reporter */}
                            <div className="space-y-3 md:pr-4">
                                <div className="flex items-center justify-between">
                                    <Label className="font-medium text-sm">Notify Reporter</Label>
                                    <Checkbox
                                        checked={notifyReporter}
                                        onCheckedChange={(c) => setNotifyReporter(!!c)}
                                        className="data-[state=checked]:bg-indigo-600 h-4 w-4"
                                    />
                                </div>
                                <p className="text-xs text-muted-foreground -mt-1 truncated">User: <span className="font-medium text-foreground">{report.reporterName || 'Anonymous'}</span></p>

                                <div className={cn("grid grid-cols-2 gap-2 transition-all duration-300", !notifyReporter && "opacity-50 pointer-events-none grayscale")}>
                                    <div
                                        className={cn("flex items-center gap-2 border rounded-md p-2 cursor-pointer transition-colors", reporterChannels.includes('IN_APP') ? "bg-indigo-50 border-indigo-200 dark:bg-indigo-900/20 dark:border-indigo-800" : "hover:bg-muted")}
                                        onClick={() => notifyReporter && toggleChannel('IN_APP', reporterChannels, setReporterChannels)}
                                    >
                                        <Bell className="h-3.5 w-3.5 text-indigo-500" />
                                        <span className="text-xs font-medium">In-App</span>
                                    </div>
                                    <div
                                        className={cn("flex items-center gap-2 border rounded-md p-2 cursor-pointer transition-colors", reporterChannels.includes('EMAIL') ? "bg-indigo-50 border-indigo-200 dark:bg-indigo-900/20 dark:border-indigo-800" : "hover:bg-muted")}
                                        onClick={() => notifyReporter && toggleChannel('EMAIL', reporterChannels, setReporterChannels)}
                                    >
                                        <Mail className="h-3.5 w-3.5 text-indigo-500" />
                                        <span className="text-xs font-medium">Email</span>
                                    </div>
                                </div>
                            </div>

                            {/* Notify Reported Entity */}
                            <div className="space-y-3 pt-4 md:pt-0 md:pl-4">
                                <div className="flex items-center justify-between">
                                    <Label className="font-medium text-sm text-foreground">Notify Reported Entity</Label>
                                    <Checkbox
                                        checked={notifyReportedEntity}
                                        onCheckedChange={(c) => setNotifyReportedEntity(!!c)}
                                        className="data-[state=checked]:bg-orange-600 h-4 w-4"
                                    />
                                </div>
                                <p className="text-xs text-muted-foreground -mt-1 truncated">Target: <span className="font-medium text-foreground">{report.reportedEntityName || 'Unknown ID'}</span></p>

                                <div className={cn("grid grid-cols-2 gap-2 transition-all duration-300", !notifyReportedEntity && "opacity-50 pointer-events-none grayscale")}>
                                    <div
                                        className={cn("flex items-center gap-2 border rounded-md p-2 cursor-pointer transition-colors", reportedEntityChannels.includes('IN_APP') ? "bg-orange-50 border-orange-200 dark:bg-orange-900/20 dark:border-orange-800" : "hover:bg-muted")}
                                        onClick={() => notifyReportedEntity && toggleChannel('IN_APP', reportedEntityChannels, setReportedEntityChannels)}
                                    >
                                        <Bell className="h-3.5 w-3.5 text-orange-500" />
                                        <span className="text-xs font-medium">In-App</span>
                                    </div>
                                    <div
                                        className={cn("flex items-center gap-2 border rounded-md p-2 cursor-pointer transition-colors", reportedEntityChannels.includes('EMAIL') ? "bg-orange-50 border-orange-200 dark:bg-orange-900/20 dark:border-orange-800" : "hover:bg-muted")}
                                        onClick={() => notifyReportedEntity && toggleChannel('EMAIL', reportedEntityChannels, setReportedEntityChannels)}
                                    >
                                        <Mail className="h-3.5 w-3.5 text-orange-500" />
                                        <span className="text-xs font-medium">Email</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end gap-3 p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50 shrink-0">
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white shadow-lg shadow-indigo-500/25"
                    >
                        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Confirm Resolution
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
}

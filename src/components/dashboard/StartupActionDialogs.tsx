import { useState, useEffect } from "react"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "../ui/dialog"
import { Button } from "../ui/button"
import { Textarea } from "../ui/textarea"
import { Label } from "../ui/label"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../ui/select"
import {
    AlertTriangle,
    Loader2,
    Megaphone,
    RefreshCcw,
    Trash2,
    CheckCircle2,
    PauseCircle,
    Ban
} from "lucide-react"
import { toast } from "sonner"
import { api } from "../../lib/api"
import { Startup } from "../../types"
import { cn } from "../../lib/utils"

interface ActionDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    startup: Startup
    onSuccess: () => void
}

const WARN_TEMPLATES = [
    "Violation of terms of service",
    "Suspicious activity detected",
    "Inappropriate content reported",
    "Please update your startup information",
]

export function WarnStartupDialog({ open, onOpenChange, startup, onSuccess }: ActionDialogProps) {
    const [message, setMessage] = useState("")
    const [loading, setLoading] = useState(false)

    const handleSubmit = async () => {
        if (!message.trim()) {
            toast.error("Please enter a warning message")
            return
        }

        try {
            setLoading(true)
            await api.post(`/admin/startups/${startup.id}/warn`, { message })
            toast.success("Warning sent successfully")
            onSuccess()
            onOpenChange(false)
            setMessage("")
        } catch (error) {
            toast.error("Failed to send warning")
            console.error(error)
        } finally {
            setLoading(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-xl p-0 gap-0 overflow-hidden bg-background">
                {/* Header */}
                <div className="bg-amber-50 dark:bg-amber-950/30 border-b border-amber-200 dark:border-amber-800/50 p-6">
                    <DialogHeader>
                        <DialogTitle className="text-xl font-bold flex items-center gap-2.5 text-amber-700 dark:text-amber-500">
                            <div className="p-2 bg-amber-100 dark:bg-amber-900/50 rounded-lg">
                                <Megaphone className="h-5 w-5" />
                            </div>
                            Issue Warning
                        </DialogTitle>
                        <DialogDescription className="text-amber-600/80 dark:text-amber-400/80 ml-11">
                            Send a formal warning to <span className="font-semibold text-foreground">{startup.name}</span>.
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <div className="p-6 space-y-5">
                    <div className="space-y-2">
                        <Label className="text-sm font-semibold">Quick Templates</Label>
                        <Select onValueChange={setMessage}>
                            <SelectTrigger className="bg-muted/30">
                                <SelectValue placeholder="Select a template..." />
                            </SelectTrigger>
                            <SelectContent>
                                {WARN_TEMPLATES.map((t) => (
                                    <SelectItem key={t} value={t}>
                                        {t}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="space-y-2">
                        <Label className="text-sm font-semibold">Custom Message</Label>
                        <Textarea
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            placeholder="Type your formal warning details here..."
                            rows={4}
                            className="bg-muted/30 resize-none focus-visible:ring-amber-500"
                        />
                    </div>
                </div>

                <DialogFooter className="p-4 bg-muted/10 border-t border-border">
                    <Button variant="ghost" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="bg-amber-600 hover:bg-amber-700 text-white"
                    >
                        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Send Warning
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

export function StartupStatusDialog({ open, onOpenChange, startup, onSuccess }: ActionDialogProps) {
    const [status, setStatus] = useState<string>(startup.status)
    const [reason, setReason] = useState("")
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (open) {
            setStatus(startup.status)
            setReason("")
        }
    }, [open, startup.status])

    const handleSubmit = async () => {
        if (status === 'BANNED' && !reason.trim()) {
            toast.error("Please provide a reason for this action")
            return
        }

        try {
            setLoading(true)
            await api.put(`/admin/startups/${startup.id}/status`, {
                status,
                reason: reason.trim() || undefined
            })
            toast.success(`Status updated to ${status}`)
            onSuccess()
            onOpenChange(false)
        } catch (error) {
            toast.error("Failed to update status")
            console.error(error)
        } finally {
            setLoading(false)
        }
    }

    const STATUS_OPTIONS = [
        {
            value: 'ACTIVE',
            label: 'Active',
            description: 'Startup is live and visible to everyone',
            Icon: CheckCircle2,
            bgClass: 'bg-emerald-50 dark:bg-emerald-950/30 border-emerald-300 dark:border-emerald-700',
            selectedBorder: 'border-emerald-500',
            textClass: 'text-emerald-700 dark:text-emerald-400',
            iconBg: 'bg-emerald-100 dark:bg-emerald-900/50'
        },
        {
            value: 'BANNED',
            label: 'Banned',
            description: 'Permanently banned from the platform',
            Icon: Ban,
            bgClass: 'bg-red-50 dark:bg-red-950/30 border-red-300 dark:border-red-700',
            selectedBorder: 'border-red-500',
            textClass: 'text-red-700 dark:text-red-400',
            iconBg: 'bg-red-100 dark:bg-red-900/50'
        }
    ]

    const currentStatusOption = STATUS_OPTIONS.find(s => s.value === status)

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-md p-0 gap-0 overflow-hidden bg-background border-0 shadow-2xl">
                {/* Header */}
                <div className="bg-gradient-to-br from-indigo-500 to-purple-600 p-6 text-white">
                    <DialogHeader>
                        <DialogTitle className="text-xl font-bold flex items-center gap-3">
                            <div className="p-2.5 bg-white/20 rounded-xl backdrop-blur-sm">
                                <RefreshCcw className="h-5 w-5" />
                            </div>
                            Update Status
                        </DialogTitle>
                        <DialogDescription className="text-white/80 ml-12 mt-1">
                            Change status for <span className="font-semibold text-white">{startup.name}</span>
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <div className="p-6 space-y-5">
                    {/* Status Selection Cards */}
                    <div className="space-y-3">
                        <Label className="text-xs font-bold text-muted-foreground uppercase tracking-wider">Select Status</Label>
                        <div className="grid gap-3">
                            {STATUS_OPTIONS.map((option) => (
                                <button
                                    key={option.value}
                                    type="button"
                                    onClick={() => setStatus(option.value)}
                                    className={cn(
                                        "w-full p-4 rounded-xl border-2 transition-all text-left flex items-center gap-4 group",
                                        status === option.value
                                            ? `${option.bgClass} ${option.selectedBorder} ring-2 ring-offset-2 ring-offset-background`
                                            : "bg-muted/30 border-transparent hover:border-muted-foreground/20 hover:bg-muted/50",
                                        status === option.value && option.value === 'ACTIVE' && 'ring-emerald-500/30',
                                        status === option.value && option.value === 'BANNED' && 'ring-red-500/30'
                                    )}
                                >
                                    <div className={cn(
                                        "h-11 w-11 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110",
                                        status === option.value ? option.iconBg : "bg-muted"
                                    )}>
                                        <option.Icon className={cn(
                                            "h-5 w-5",
                                            status === option.value ? option.textClass : "text-muted-foreground"
                                        )} />
                                    </div>
                                    <div className="flex-1">
                                        <p className={cn(
                                            "font-bold text-sm",
                                            status === option.value ? option.textClass : "text-foreground"
                                        )}>
                                            {option.label}
                                        </p>
                                        <p className="text-xs text-muted-foreground mt-0.5">{option.description}</p>
                                    </div>
                                    {status === option.value && (
                                        <div className={cn("h-6 w-6 rounded-full flex items-center justify-center", option.iconBg)}>
                                            <CheckCircle2 className={cn("h-4 w-4", option.textClass)} />
                                        </div>
                                    )}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Reason Field - Required for BANNED */}
                    {status === 'BANNED' && (
                        <div className="space-y-2 animate-in fade-in slide-in-from-top-2 duration-300">
                            <Label className="text-sm font-semibold flex items-center gap-2 text-red-600">
                                <AlertTriangle className="h-4 w-4" />
                                Reason Required
                            </Label>
                            <Textarea
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Explain why this startup is being banned..."
                                className="min-h-[100px] resize-none border-red-200 focus-visible:ring-red-500/30"
                            />
                        </div>
                    )}
                </div>

                <DialogFooter className="p-4 bg-muted/30 border-t flex items-center justify-between">
                    <Button variant="ghost" onClick={() => onOpenChange(false)} className="hover:bg-muted">
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={loading || status === startup.status || (status === 'BANNED' && !reason.trim())}
                        className={cn(
                            "shadow-lg transition-all font-semibold",
                            currentStatusOption?.value === 'ACTIVE' && "bg-emerald-600 hover:bg-emerald-700 text-white",
                            currentStatusOption?.value === 'BANNED' && "bg-red-600 hover:bg-red-700 text-white"
                        )}
                    >
                        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Update to {currentStatusOption?.label}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

export function DeleteStartupDialog({ open, onOpenChange, startup, onSuccess }: ActionDialogProps) {
    const [loading, setLoading] = useState(false)
    const [confirmName, setConfirmName] = useState("")

    const handleDelete = async () => {
        if (confirmName !== startup.name) return

        try {
            setLoading(true)
            await api.delete(`/admin/startups/${startup.id}`)
            toast.success("Startup deleted permanently")
            onSuccess()
            onOpenChange(false)
        } catch (error) {
            toast.error("Failed to delete startup")
            console.error(error)
        } finally {
            setLoading(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-md p-0 gap-0 overflow-hidden bg-background">
                <div className="bg-red-50 dark:bg-red-950/30 border-b border-red-100 dark:border-red-900/30 p-6">
                    <DialogHeader>
                        <DialogTitle className="text-xl font-bold flex items-center gap-2.5 text-red-600 dark:text-red-500">
                            <div className="p-2 bg-red-100 dark:bg-red-900/50 rounded-lg">
                                <Trash2 className="h-5 w-5" />
                            </div>
                            Delete Startup
                        </DialogTitle>
                        <DialogDescription className="text-red-600/80 dark:text-red-400/80 ml-11">
                            Permanent deletion process initiated.
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <div className="p-6 space-y-4">
                    <div className="bg-red-50 dark:bg-red-950/10 border border-red-100 dark:border-red-900/20 rounded-lg p-4 text-sm text-red-800 dark:text-red-300">
                        <p className="font-semibold flex items-center gap-2 mb-2">
                            <AlertTriangle className="h-4 w-4" />
                            Warning: Irreversible Action
                        </p>
                        <p>
                            This will permanently delete <span className="font-bold">{startup.name}</span> and remove all associated data, including team members and investments.
                        </p>
                    </div>

                    <div className="space-y-2">
                        <Label className="text-sm font-semibold">Type confirmation</Label>
                        <div className="relative">
                            <input
                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                value={confirmName}
                                onChange={(e) => setConfirmName(e.target.value)}
                                placeholder={startup.name}
                            />
                        </div>
                        <p className="text-xs text-muted-foreground">Type <strong>{startup.name}</strong> to confirm.</p>
                    </div>
                </div>

                <DialogFooter className="p-4 bg-muted/10 border-t border-border">
                    <Button variant="ghost" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button
                        variant="destructive"
                        onClick={handleDelete}
                        disabled={loading || confirmName !== startup.name}
                        className="bg-red-600 hover:bg-red-700"
                    >
                        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Delete Permanently
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

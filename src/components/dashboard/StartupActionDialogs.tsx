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
    Trash2
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
        try {
            setLoading(true)
            await api.put(`/admin/startups/${startup.id}/status`, {
                status,
                reason: reason.trim() || undefined
            })
            toast.success(`Startup status updated to ${status}`)
            onSuccess()
            onOpenChange(false)
        } catch (error) {
            toast.error("Failed to update status")
            console.error(error)
        } finally {
            setLoading(false)
        }
    }

    const getStatusColor = (st: string) => {
        switch (st) {
            case 'APPROVED': return 'text-green-600 bg-green-50 dark:bg-green-950/30'
            case 'PENDING': return 'text-yellow-600 bg-yellow-50 dark:bg-yellow-950/30'
            case 'SUSPENDED': return 'text-orange-600 bg-orange-50 dark:bg-orange-950/30'
            case 'BANNED': return 'text-red-600 bg-red-50 dark:bg-red-950/30'
            case 'REJECTED': return 'text-rose-600 bg-rose-50 dark:bg-rose-950/30'
            default: return 'text-slate-600 bg-slate-50 dark:bg-slate-950/30'
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-xl p-0 gap-0 overflow-hidden bg-background">
                {/* Header */}
                <div className="bg-slate-900/5 dark:bg-slate-900/50 border-b border-border p-6">
                    <DialogHeader>
                        <DialogTitle className="text-xl font-bold flex items-center gap-2.5">
                            <div className="p-2 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg text-indigo-600 dark:text-indigo-400">
                                <RefreshCcw className="h-5 w-5" />
                            </div>
                            Update Status
                        </DialogTitle>
                        <DialogDescription className="ml-11">
                            Change operational status for <span className="font-semibold text-foreground">{startup.name}</span>.
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <div className="p-6 space-y-6">
                    <div className="space-y-2">
                        <Label className="text-sm font-semibold">New Status</Label>
                        <Select value={status} onValueChange={setStatus}>
                            <SelectTrigger className={cn("h-11", getStatusColor(status))}>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="APPROVED" className="text-green-600 font-medium">Approved</SelectItem>
                                <SelectItem value="PENDING" className="text-yellow-600 font-medium">Pending</SelectItem>
                                <SelectItem value="SUSPENDED" className="text-orange-600 font-medium">Suspended</SelectItem>
                                <SelectItem value="BANNED" className="text-red-600 font-medium">Banned</SelectItem>
                                <SelectItem value="REJECTED" className="text-rose-600 font-medium">Rejected</SelectItem>
                                <SelectItem value="ARCHIVED" className="text-slate-500 font-medium">Archived</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    {(status === 'SUSPENDED' || status === 'BANNED' || status === 'REJECTED') && (
                        <div className="space-y-2 animate-in fade-in slide-in-from-top-2">
                            <Label className="text-sm font-semibold text-destructive">Reason for Action (Required)</Label>
                            <Textarea
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Please provide a clear reason for this administrative action..."
                                required
                                className="min-h-[100px] border-destructive/20 focus-visible:ring-destructive/30"
                            />
                        </div>
                    )}
                </div>

                <DialogFooter className="p-4 bg-muted/10 border-t border-border">
                    <Button variant="ghost" onClick={() => onOpenChange(false)}>Cancel</Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={loading || ((status === 'SUSPENDED' || status === 'BANNED' || status === 'REJECTED') && !reason.trim())}
                        className={status === 'APPROVED' ? "bg-green-600 hover:bg-green-700 text-white" : ""}
                    >
                        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Update Status
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

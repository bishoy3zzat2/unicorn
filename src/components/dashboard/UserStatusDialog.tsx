import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
} from '@/components/ui/dialog'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'
import api from '@/lib/axios'
import { Shield, Loader2, CheckCircle2, AlertOctagon, XCircle, UserCheck } from 'lucide-react'

interface UserStatusDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    userId: string | null
    currentStatus?: string
    onSuccess: () => void
}

export function UserStatusDialog({
    open,
    onOpenChange,
    userId,
    currentStatus = 'ACTIVE',
    onSuccess,
}: UserStatusDialogProps) {
    const [status, setStatus] = useState<string>(currentStatus)
    const [reason, setReason] = useState('')
    const [isLoading, setIsLoading] = useState(false)

    // Sync state with prop when dialog opens
    useEffect(() => {
        if (open) {
            setStatus(currentStatus)
            setReason('')
        }
    }, [open, currentStatus])

    const handleConfirm = async () => {
        if (!userId) return

        try {
            setIsLoading(true)
            await api.put(`/admin/users/${userId}/status`, {
                status,
                reason: reason || 'Manual status change by admin',
            })
            toast.success('User status updated successfully')
            onSuccess()
            onOpenChange(false)
        } catch (error) {
            console.error('Failed to update status:', error)
            toast.error('Failed to update status')
        } finally {
            setIsLoading(false)
        }
    }

    const getStatusIcon = (s: string) => {
        switch (s) {
            case 'ACTIVE': return CheckCircle2;
            case 'BANNED': return AlertOctagon;
            case 'DELETED': return XCircle;
            default: return UserCheck;
        }
    }

    const StatusIcon = getStatusIcon(status);

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md p-0 gap-0 overflow-hidden border-border bg-background shadow-lg sm:rounded-2xl">
                <div className="bg-indigo-50 dark:bg-indigo-900/20 border-b border-indigo-100 dark:border-indigo-800/50 p-6 flex flex-col items-center text-center">
                    <div className="h-12 w-12 rounded-full bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center mb-4 ring-4 ring-indigo-50 dark:ring-indigo-900/20">
                        <Shield className="h-6 w-6 text-indigo-600 dark:text-indigo-400" />
                    </div>
                    <DialogTitle className="text-xl font-bold text-indigo-950 dark:text-indigo-100">
                        Change User Status
                    </DialogTitle>
                    <p className="text-sm text-indigo-600/80 dark:text-indigo-300 mt-1 max-w-xs">
                        Update the account standing for this user. This will affect their access immediately.
                    </p>
                </div>

                <div className="p-6 space-y-5">
                    <div className="space-y-2">
                        <Label className="text-sm font-semibold text-foreground/80">New Status</Label>
                        <Select value={status} onValueChange={setStatus}>
                            <SelectTrigger className="h-11 bg-muted/30 border-muted-foreground/20 focus:ring-indigo-500/20">
                                <div className="flex items-center gap-2">
                                    <StatusIcon className="h-4 w-4 text-muted-foreground" />
                                    <SelectValue placeholder="Select status" />
                                </div>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ACTIVE" className="text-emerald-600 focus:text-emerald-700 focus:bg-emerald-50">
                                    <div className="flex items-center gap-2">
                                        <CheckCircle2 className="h-4 w-4" />
                                        <span>Active</span>
                                    </div>
                                </SelectItem>
                                <SelectItem value="PENDING_VERIFICATION" className="text-blue-600 focus:text-blue-700 focus:bg-blue-50">
                                    <div className="flex items-center gap-2">
                                        <UserCheck className="h-4 w-4" />
                                        <span>Pending Verification</span>
                                    </div>
                                </SelectItem>
                                <SelectItem value="BANNED" className="text-red-600 focus:text-red-700 focus:bg-red-50">
                                    <div className="flex items-center gap-2">
                                        <AlertOctagon className="h-4 w-4" />
                                        <span>Banned</span>
                                    </div>
                                </SelectItem>
                                <SelectItem value="DELETED" className="text-gray-600 focus:text-gray-700 focus:bg-gray-50">
                                    <div className="flex items-center gap-2">
                                        <XCircle className="h-4 w-4" />
                                        <span>Deleted</span>
                                    </div>
                                </SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="space-y-2">
                        <Label className="text-sm font-semibold text-foreground/80">Reason (Optional)</Label>
                        <Input
                            placeholder="Why are you changing this?"
                            value={reason}
                            onChange={e => setReason(e.target.value)}
                            className="h-11 bg-muted/30 border-muted-foreground/20 focus-visible:ring-indigo-500/20"
                        />
                        <p className="text-[11px] text-muted-foreground ml-1">
                            Use this to document the reason for status change.
                        </p>
                    </div>
                </div>

                <div className="bg-muted/30 p-4 flex items-center justify-end gap-3 border-t">
                    <Button variant="ghost" onClick={() => onOpenChange(false)} disabled={isLoading} className="hover:bg-muted/50">
                        Cancel
                    </Button>
                    <Button
                        onClick={handleConfirm}
                        disabled={isLoading}
                        className="bg-indigo-600 hover:bg-indigo-700 text-white shadow-md hover:shadow-lg transition-all"
                    >
                        {isLoading ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Updating...
                            </>
                        ) : (
                            'Update Status'
                        )}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
}

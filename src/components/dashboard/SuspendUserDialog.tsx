import { useState } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Loader2, Ban, Calendar, Check, AlertOctagon } from 'lucide-react'
import api from '@/lib/axios'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'

interface SuspendUserDialogProps {
    userId: string | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess?: () => void
}

// Quick duration options
const DURATION_OPTIONS = [
    { value: 1, label: '1 Day' },
    { value: 3, label: '3 Days' },
    { value: 7, label: '7 Days' },
    { value: 14, label: '14 Days' },
    { value: 30, label: '30 Days' },
    { value: 90, label: '90 Days' },
    { value: -1, label: 'Permanent Ban' },
]

// Suspension reason options (can select multiple)
const REASON_OPTIONS = [
    'Violation of community guidelines',
    'Inappropriate content or behavior',
    'Spam or promotional activity',
    'Misleading information',
    'Harassment or abusive behavior',
    'Fraudulent activity',
    'Multiple account violations',
    'Terms of service breach',
]

export function SuspendUserDialog({ userId, open, onOpenChange, onSuccess }: SuspendUserDialogProps) {
    const [selectedReasons, setSelectedReasons] = useState<string[]>([])
    const [customReason, setCustomReason] = useState('')
    const [selectedDuration, setSelectedDuration] = useState<number>(7)
    const [submitting, setSubmitting] = useState(false)

    function toggleReason(reason: string) {
        setSelectedReasons(prev =>
            prev.includes(reason)
                ? prev.filter(r => r !== reason)
                : [...prev, reason]
        )
    }

    function getFullReason(): string {
        const reasons = [...selectedReasons]
        if (customReason.trim()) {
            reasons.push(customReason.trim())
        }
        return reasons.join('; ')
    }

    async function handleSubmit() {
        const fullReason = getFullReason()
        if (!userId || !fullReason) {
            toast.error('Please select at least one reason for suspension')
            return
        }

        setSubmitting(true)
        try {
            const isPermanent = selectedDuration === -1

            await api.post(`/admin/users/${userId}/suspend`, {
                reason: fullReason,
                permanent: isPermanent,
                durationDays: isPermanent ? null : selectedDuration,
            })

            toast.success(isPermanent ? 'User permanently banned' : 'User suspended successfully')
            onOpenChange(false)
            onSuccess?.()

            // Reset form
            setSelectedReasons([])
            setCustomReason('')
            setSelectedDuration(7)
        } catch (error) {
            console.error('Failed to suspend user:', error)
            toast.error('Failed to suspend user')
        } finally {
            setSubmitting(false)
        }
    }

    const isPermanent = selectedDuration === -1

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-lg p-0 gap-0 overflow-hidden border-border bg-background shadow-lg sm:rounded-2xl max-h-[90vh] flex flex-col">
                <div className="bg-orange-50 dark:bg-orange-900/20 border-b border-orange-100 dark:border-orange-800/50 p-6 flex flex-col items-center text-center shrink-0">
                    <div className="h-12 w-12 rounded-full bg-orange-100 dark:bg-orange-900/50 flex items-center justify-center mb-4 ring-4 ring-orange-50 dark:ring-orange-900/20">
                        <Ban className="h-6 w-6 text-orange-600 dark:text-orange-400" />
                    </div>
                    <DialogTitle className="text-xl font-bold text-orange-950 dark:text-orange-100">
                        Suspend Account
                    </DialogTitle>
                    <p className="text-sm text-orange-800/80 dark:text-orange-300 mt-1 max-w-xs">
                        Temporarily or permanently restrict this user's access to the platform.
                    </p>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                    {/* Duration Selection */}
                    <div className="space-y-3">
                        <Label className="text-sm font-semibold text-foreground/80">Suspension Duration</Label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                            {DURATION_OPTIONS.map((option) => {
                                const isSelected = selectedDuration === option.value;
                                const isBan = option.value === -1;

                                return (
                                    <button
                                        key={option.value}
                                        type="button"
                                        onClick={() => setSelectedDuration(option.value)}
                                        className={cn(
                                            "flex items-center justify-center px-3 py-2.5 rounded-lg border text-sm font-medium transition-all relative",
                                            isSelected
                                                ? (isBan
                                                    ? "border-red-500 bg-red-500/10 text-red-600"
                                                    : "border-orange-500 bg-orange-500/10 text-orange-600")
                                                : "border-border hover:border-orange-300 hover:bg-orange-50",
                                            isBan && !isSelected && "hover:border-red-300 hover:bg-red-50 text-red-600/80 font-normal"
                                        )}
                                    >
                                        <span className={cn(isBan && "font-bold")}>{option.label}</span>
                                    </button>
                                )
                            })}
                        </div>

                        <div className={cn(
                            "rounded-lg p-3 text-sm flex items-start gap-2.5 mt-2",
                            isPermanent ? "bg-red-50 text-red-700 border border-red-100" : "bg-orange-50 text-orange-700 border border-orange-100"
                        )}>
                            {isPermanent ? (
                                <>
                                    <AlertOctagon className="h-4 w-4 shrink-0 mt-0.5" />
                                    <span><strong>Warning:</strong> This will permanently ban the user. They won't be able to log in or create new accounts with this email.</span>
                                </>
                            ) : (
                                <>
                                    <Calendar className="h-4 w-4 shrink-0 mt-0.5" />
                                    <span>Access will be restored automatically after <strong>{selectedDuration} days</strong>.</span>
                                </>
                            )}
                        </div>
                    </div>

                    {/* Reason Selection */}
                    <div className="space-y-3">
                        <Label className="text-sm font-semibold text-foreground/80">Reason(s) for Suspension *</Label>
                        <div className="grid grid-cols-1 gap-1.5">
                            {REASON_OPTIONS.map((reason) => {
                                const isSelected = selectedReasons.includes(reason);
                                return (
                                    <button
                                        key={reason}
                                        type="button"
                                        onClick={() => toggleReason(reason)}
                                        className={cn(
                                            "flex items-center justify-between px-3 py-2 rounded-lg border text-sm text-left transition-all",
                                            isSelected
                                                ? "border-orange-500 bg-orange-500/5 text-orange-700 font-medium"
                                                : "border-transparent bg-muted/30 hover:bg-muted text-muted-foreground hover:text-foreground"
                                        )}
                                    >
                                        <span>{reason}</span>
                                        {isSelected && <Check className="h-3.5 w-3.5 text-orange-500" />}
                                    </button>
                                )
                            })}
                        </div>
                    </div>

                    {/* Custom Reason */}
                    <div className="space-y-2">
                        <Label className="text-sm font-semibold text-foreground/80">Additional Details</Label>
                        <Textarea
                            placeholder="Add precise details regarding the violation..."
                            value={customReason}
                            onChange={(e) => setCustomReason(e.target.value)}
                            rows={3}
                            className="bg-muted/30 border-muted-foreground/20 focus-visible:ring-orange-500/20"
                        />
                    </div>
                </div>

                <div className="bg-muted/30 p-4 flex items-center justify-end gap-3 border-t mt-auto shrink-0">
                    <Button variant="ghost" onClick={() => onOpenChange(false)} className="hover:bg-muted/50">
                        Cancel
                    </Button>
                    <Button
                        variant={isPermanent ? 'destructive' : 'default'}
                        onClick={handleSubmit}
                        disabled={submitting || (selectedReasons.length === 0 && !customReason.trim())}
                        className={cn(
                            !isPermanent && "bg-orange-500 hover:bg-orange-600 text-white",
                            "shadow-md hover:shadow-lg transition-all"
                        )}
                    >
                        {submitting ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <Ban className="h-4 w-4 mr-2" />
                        )}
                        {isPermanent ? 'Permanently Ban User' : 'Suspend User'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
}

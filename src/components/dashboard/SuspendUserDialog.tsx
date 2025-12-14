import { useState } from 'react'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog'
import { Button } from '../ui/button'
import { Textarea } from '../ui/textarea'
import { Label } from '../ui/label'
import { Loader2, Ban, Calendar, Check } from 'lucide-react'
import api from '../../lib/axios'
import { toast } from 'sonner'

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
            <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <Ban className="h-5 w-5 text-orange-500" />
                        Suspend User
                    </DialogTitle>
                    <DialogDescription>
                        Suspend this user's account. They will not be able to access the platform during the suspension period.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-5 py-4">
                    {/* Duration Selection */}
                    <div className="space-y-3">
                        <Label className="text-base font-semibold">Suspension Duration</Label>
                        <div className="grid grid-cols-2 gap-2">
                            {DURATION_OPTIONS.map((option) => (
                                <button
                                    key={option.value}
                                    type="button"
                                    onClick={() => setSelectedDuration(option.value)}
                                    className={`flex items-center justify-between px-4 py-3 rounded-lg border-2 transition-all ${selectedDuration === option.value
                                            ? option.value === -1
                                                ? 'border-red-500 bg-red-500/10 text-red-500'
                                                : 'border-primary bg-primary/10 text-primary'
                                            : 'border-border hover:border-primary/50'
                                        }`}
                                >
                                    <span className={option.value === -1 ? 'font-semibold' : ''}>
                                        {option.label}
                                    </span>
                                    {selectedDuration === option.value && (
                                        <Check className="h-4 w-4" />
                                    )}
                                </button>
                            ))}
                        </div>

                        {!isPermanent && (
                            <p className="text-sm text-muted-foreground flex items-center gap-1">
                                <Calendar className="h-4 w-4" />
                                Suspension will end in {selectedDuration} day{selectedDuration > 1 ? 's' : ''}
                            </p>
                        )}

                        {isPermanent && (
                            <p className="text-sm text-red-500 font-medium">
                                ⚠️ This action will permanently ban the user from the platform
                            </p>
                        )}
                    </div>

                    {/* Reason Selection (Multiple) */}
                    <div className="space-y-3">
                        <Label className="text-base font-semibold">Reason(s) for Suspension *</Label>
                        <p className="text-sm text-muted-foreground">Select one or more reasons</p>
                        <div className="grid grid-cols-1 gap-2">
                            {REASON_OPTIONS.map((reason) => (
                                <button
                                    key={reason}
                                    type="button"
                                    onClick={() => toggleReason(reason)}
                                    className={`flex items-center justify-between px-4 py-2 rounded-lg border transition-all text-left ${selectedReasons.includes(reason)
                                            ? 'border-primary bg-primary/10 text-primary'
                                            : 'border-border hover:border-primary/50'
                                        }`}
                                >
                                    <span className="text-sm">{reason}</span>
                                    {selectedReasons.includes(reason) && (
                                        <Check className="h-4 w-4 flex-shrink-0" />
                                    )}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Custom Reason */}
                    <div className="space-y-2">
                        <Label htmlFor="custom-reason">Additional Details (optional)</Label>
                        <Textarea
                            id="custom-reason"
                            placeholder="Add additional context or custom reason..."
                            value={customReason}
                            onChange={(e) => setCustomReason(e.target.value)}
                            rows={3}
                        />
                    </div>

                    {/* Selected Reasons Summary */}
                    {(selectedReasons.length > 0 || customReason) && (
                        <div className="p-3 rounded-lg bg-muted/50 border">
                            <p className="text-xs font-medium text-muted-foreground mb-1">
                                Full suspension reason:
                            </p>
                            <p className="text-sm">{getFullReason()}</p>
                        </div>
                    )}
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant={isPermanent ? 'destructive' : 'default'}
                        onClick={handleSubmit}
                        disabled={submitting || (selectedReasons.length === 0 && !customReason.trim())}
                    >
                        {submitting ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <Ban className="h-4 w-4 mr-2" />
                        )}
                        {isPermanent ? 'Permanently Ban' : 'Suspend User'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

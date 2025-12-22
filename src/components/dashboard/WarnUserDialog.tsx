import { useState } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Loader2, AlertTriangle, MessageSquare } from 'lucide-react'
import api from '@/lib/axios'
import { toast } from 'sonner'

interface WarnUserDialogProps {
    userId: string | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess?: () => void
}

// Quick warning templates
const WARNING_TEMPLATES = [
    'Violation of community guidelines',
    'Inappropriate content or behavior',
    'Spam or promotional activity',
    'Misleading information',
    'Harassment or abusive behavior',
]

export function WarnUserDialog({ userId, open, onOpenChange, onSuccess }: WarnUserDialogProps) {
    const [reason, setReason] = useState('')
    const [submitting, setSubmitting] = useState(false)

    async function handleSubmit() {
        if (!userId || !reason.trim()) {
            toast.error('Please provide a warning message')
            return
        }

        setSubmitting(true)
        try {
            await api.post(`/admin/users/${userId}/warn`, {
                reason: reason.trim(),
            })

            toast.success('Warning issued successfully')
            onOpenChange(false)
            onSuccess?.()

            // Reset form
            setReason('')
        } catch (error) {
            console.error('Failed to warn user:', error)
            toast.error('Failed to issue warning')
        } finally {
            setSubmitting(false)
        }
    }

    function selectTemplate(template: string) {
        setReason(template)
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md p-0 gap-0 overflow-hidden border-border bg-background shadow-lg sm:rounded-2xl max-h-[90vh] flex flex-col">
                <div className="bg-amber-50 dark:bg-amber-900/20 border-b border-amber-100 dark:border-amber-800/50 p-6 flex flex-col items-center text-center">
                    <div className="h-12 w-12 rounded-full bg-amber-100 dark:bg-amber-900/50 flex items-center justify-center mb-4 ring-4 ring-amber-50 dark:ring-amber-900/20">
                        <AlertTriangle className="h-6 w-6 text-amber-600 dark:text-amber-400" />
                    </div>
                    <DialogTitle className="text-xl font-bold text-amber-950 dark:text-amber-100">
                        Issue Formal Warning
                    </DialogTitle>
                    <p className="text-sm text-amber-700/80 dark:text-amber-300 mt-1 max-w-xs">
                        This warning will be recorded in the user's moderation history and they will be notified.
                    </p>
                </div>

                <div className="p-6 space-y-5 overflow-y-auto">
                    {/* Quick Templates */}
                    <div className="space-y-3">
                        <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Quick Templates</Label>
                        <div className="flex flex-wrap gap-2">
                            {WARNING_TEMPLATES.map((template, idx) => (
                                <button
                                    key={idx}
                                    onClick={() => selectTemplate(template)}
                                    className="text-xs px-2.5 py-1.5 rounded-full border border-border bg-background hover:bg-amber-50 hover:text-amber-700 hover:border-amber-200 transition-colors"
                                >
                                    {template}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Warning Message */}
                    <div className="space-y-2">
                        <Label htmlFor="warning-reason" className="text-sm font-semibold text-foreground/80">Message Body *</Label>
                        <Textarea
                            id="warning-reason"
                            placeholder="Enter the specific warning details..."
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            rows={4}
                            className="bg-muted/30 border-muted-foreground/20 focus-visible:ring-amber-500/20 resize-none"
                        />
                        <div className="flex items-center gap-2 text-[11px] text-muted-foreground bg-muted/50 p-2 rounded-md">
                            <MessageSquare className="h-3.5 w-3.5" />
                            <span>This message will be sent to the user via email/notification.</span>
                        </div>
                    </div>
                </div>

                <div className="bg-muted/30 p-4 flex items-center justify-end gap-3 border-t">
                    <Button variant="ghost" onClick={() => onOpenChange(false)} disabled={submitting} className="hover:bg-muted/50">
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={submitting || !reason.trim()}
                        className="bg-amber-500 hover:bg-amber-600 text-black shadow-md hover:shadow-lg transition-all"
                    >
                        {submitting ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <AlertTriangle className="h-4 w-4 mr-2" />
                        )}
                        Issue Warning
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
}

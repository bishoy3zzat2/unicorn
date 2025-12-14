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
import { Loader2, AlertTriangle } from 'lucide-react'
import api from '../../lib/axios'
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
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <AlertTriangle className="h-5 w-5 text-yellow-500" />
                        Issue Warning
                    </DialogTitle>
                    <DialogDescription>
                        Issue a formal warning to this user. Warnings are recorded in their moderation history.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4 py-4">
                    {/* Quick Templates */}
                    <div className="space-y-2">
                        <Label>Quick Templates</Label>
                        <div className="flex flex-wrap gap-2">
                            {WARNING_TEMPLATES.map((template, idx) => (
                                <Button
                                    key={idx}
                                    variant="outline"
                                    size="sm"
                                    onClick={() => selectTemplate(template)}
                                    className="text-xs"
                                >
                                    {template}
                                </Button>
                            ))}
                        </div>
                    </div>

                    {/* Warning Message */}
                    <div className="space-y-2">
                        <Label htmlFor="warning-reason">Warning Message *</Label>
                        <Textarea
                            id="warning-reason"
                            placeholder="Enter the warning message..."
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            rows={4}
                        />
                        <p className="text-xs text-muted-foreground">
                            This message will be stored in the user's moderation history
                        </p>
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={submitting || !reason.trim()}
                        className="bg-yellow-500 hover:bg-yellow-600 text-black"
                    >
                        {submitting ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <AlertTriangle className="h-4 w-4 mr-2" />
                        )}
                        Issue Warning
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

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
import { Loader2, AlertTriangle, Skull } from 'lucide-react'
import api from '../../lib/axios'
import { toast } from 'sonner'

interface DeleteUserDialogProps {
    userId: string | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess?: () => void
}

export function DeleteUserDialog({ userId, open, onOpenChange, onSuccess }: DeleteUserDialogProps) {
    const [reason, setReason] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [confirmText, setConfirmText] = useState('')

    async function handleSubmit() {
        if (!userId) return

        if (confirmText !== 'DELETE') {
            toast.error('Please type DELETE to confirm')
            return
        }

        setSubmitting(true)
        try {
            await api.delete(`/admin/users/${userId}?hardDelete=true`, {
                data: { reason: reason.trim() || 'Deleted by admin' }
            })

            toast.success('User permanently deleted from database')
            onOpenChange(false)
            onSuccess?.()

            // Reset form
            setReason('')
            setConfirmText('')
        } catch (error) {
            console.error('Failed to delete user:', error)
            toast.error('Failed to delete user')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2 text-red-500">
                        <Skull className="h-5 w-5" />
                        Permanently Delete User
                    </DialogTitle>
                    <DialogDescription>
                        This will permanently delete the user from the database.
                    </DialogDescription>
                </DialogHeader>

                <div className="space-y-4 py-4">
                    {/* Critical Warning */}
                    <div className="p-4 rounded-lg bg-red-500/20 border-2 border-red-500">
                        <div className="flex items-start gap-3">
                            <AlertTriangle className="h-6 w-6 text-red-500 mt-0.5 flex-shrink-0" />
                            <div>
                                <p className="font-bold text-red-500 text-lg">⚠️ PERMANENT DELETION</p>
                                <p className="text-sm mt-2">This will:</p>
                                <ul className="text-sm mt-2 list-disc list-inside space-y-1">
                                    <li>Remove the user from the database</li>
                                    <li>Delete all moderation history</li>
                                    <li>Allow the email to be used for new registration</li>
                                </ul>
                                <p className="text-sm mt-3 font-bold text-red-600">
                                    ❌ This action CANNOT be undone!
                                </p>
                                <p className="text-xs mt-2 text-muted-foreground">
                                    💡 Tip: Use "Suspend → Permanent Ban" instead if you want to block the account.
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Reason */}
                    <div className="space-y-2">
                        <Label htmlFor="delete-reason">Reason for Deletion (for logs)</Label>
                        <Textarea
                            id="delete-reason"
                            placeholder="Enter the reason for deletion..."
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            rows={3}
                        />
                    </div>

                    {/* Confirmation */}
                    <div className="space-y-2">
                        <Label htmlFor="confirm-delete">
                            Type <span className="font-bold text-red-500 text-lg">DELETE</span> to confirm
                        </Label>
                        <input
                            id="confirm-delete"
                            type="text"
                            value={confirmText}
                            onChange={(e) => setConfirmText(e.target.value.toUpperCase())}
                            className="flex h-10 w-full rounded-md border-2 border-red-500/50 bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-2"
                            placeholder="Type DELETE"
                        />
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancel
                    </Button>
                    <Button
                        variant="destructive"
                        onClick={handleSubmit}
                        disabled={submitting || confirmText !== 'DELETE'}
                        className="bg-red-600 hover:bg-red-700"
                    >
                        {submitting ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <Skull className="h-4 w-4 mr-2" />
                        )}
                        Permanently Delete
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

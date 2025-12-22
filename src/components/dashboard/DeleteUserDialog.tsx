import { useState } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Loader2, AlertTriangle, Skull, Trash2 } from 'lucide-react'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import api from '@/lib/axios'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'

interface DeleteUserDialogProps {
    userId: string | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess?: () => void
}

export function DeleteUserDialog({ userId, open, onOpenChange, onSuccess }: DeleteUserDialogProps) {
    const [deleteType, setDeleteType] = useState<'SOFT' | 'HARD'>('SOFT')
    const [reason, setReason] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [confirmText, setConfirmText] = useState('')

    async function handleSubmit() {
        if (!userId) return

        if (deleteType === 'HARD' && confirmText !== 'DELETE') {
            toast.error('Please type DELETE to confirm')
            return
        }

        if (deleteType === 'SOFT' && !reason.trim()) {
            toast.error('Please provide a reason for soft deletion')
            return
        }

        setSubmitting(true)
        try {
            const isHardDelete = deleteType === 'HARD'
            await api.delete(`/admin/users/${userId}?hardDelete=${isHardDelete}`, {
                data: deleteType === 'SOFT' ? { reason: reason.trim() } : undefined
            })

            toast.success(deleteType === 'HARD' ? 'User permanently deleted' : 'User soft deleted successfully')
            onOpenChange(false)
            onSuccess?.()

            // Reset form
            setReason('')
            setConfirmText('')
            setDeleteType('SOFT')
        } catch (error) {
            console.error('Failed to delete user:', error)
            toast.error('Failed to delete user')
        } finally {
            setSubmitting(false)
        }
    }

    const isHard = deleteType === 'HARD'

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md p-0 gap-0 overflow-hidden border-border bg-background shadow-lg sm:rounded-2xl">
                <div className={cn(
                    "border-b p-6 flex flex-col items-center text-center transition-colors duration-300",
                    isHard
                        ? "bg-red-50 dark:bg-red-900/20 border-red-100 dark:border-red-800/50"
                        : "bg-orange-50 dark:bg-orange-900/20 border-orange-100 dark:border-orange-800/50"
                )}>
                    <div className={cn(
                        "h-12 w-12 rounded-full flex items-center justify-center mb-4 ring-4 transition-colors duration-300",
                        isHard
                            ? "bg-red-100 dark:bg-red-900/50 ring-red-50 dark:ring-red-900/20"
                            : "bg-orange-100 dark:bg-orange-900/50 ring-orange-50 dark:ring-orange-900/20"
                    )}>
                        {isHard ? (
                            <Skull className="h-6 w-6 text-red-600 dark:text-red-400" />
                        ) : (
                            <Trash2 className="h-6 w-6 text-orange-600 dark:text-orange-400" />
                        )}
                    </div>
                    <DialogTitle className={cn(
                        "text-xl font-bold transition-colors duration-300",
                        isHard ? "text-red-950 dark:text-red-100" : "text-orange-950 dark:text-orange-100"
                    )}>
                        {isHard ? 'Permanently Delete User' : 'Delete User Account'}
                    </DialogTitle>
                    <p className={cn(
                        "text-sm mt-1 max-w-xs transition-colors duration-300",
                        isHard ? "text-red-800/80 dark:text-red-300" : "text-orange-800/80 dark:text-orange-300"
                    )}>
                        {isHard
                            ? "This action is irreversible. All user data will be completely wiped."
                            : "User will be marked as deleted but data is retained for record keeping."
                        }
                    </p>
                </div>

                <div className="p-6 space-y-6">
                    {/* Delete Type Selection */}
                    <RadioGroup
                        value={deleteType}
                        onValueChange={(v) => setDeleteType(v as 'SOFT' | 'HARD')}
                        className="grid grid-cols-2 gap-4"
                    >
                        <div>
                            <RadioGroupItem value="SOFT" id="soft-delete" className="peer sr-only" />
                            <Label
                                htmlFor="soft-delete"
                                className="flex flex-col items-center justify-between rounded-xl border-2 border-muted bg-transparent p-4 hover:bg-muted/50 hover:border-muted-foreground/30 peer-data-[state=checked]:border-orange-500 peer-data-[state=checked]:bg-orange-500/5 transition-all cursor-pointer"
                            >
                                <Trash2 className="mb-2 h-6 w-6 text-orange-500" />
                                <div className="text-sm font-semibold text-center text-foreground">Soft Delete</div>
                                <div className="text-[10px] text-center text-muted-foreground mt-1">Recoverable</div>
                            </Label>
                        </div>
                        <div>
                            <RadioGroupItem value="HARD" id="hard-delete" className="peer sr-only" />
                            <Label
                                htmlFor="hard-delete"
                                className="flex flex-col items-center justify-between rounded-xl border-2 border-muted bg-transparent p-4 hover:bg-muted/50 hover:border-muted-foreground/30 peer-data-[state=checked]:border-red-500 peer-data-[state=checked]:bg-red-500/5 transition-all cursor-pointer"
                            >
                                <Skull className="mb-2 h-6 w-6 text-red-500" />
                                <div className="text-sm font-semibold text-center text-foreground">Hard Delete</div>
                                <div className="text-[10px] text-center text-muted-foreground mt-1">Irreversible</div>
                            </Label>
                        </div>
                    </RadioGroup>

                    {/* Dynamic Content based on Type */}
                    {deleteType === 'SOFT' ? (
                        <div className="space-y-2 animate-in fade-in slide-in-from-top-2">
                            <Label htmlFor="delete-reason" className="text-sm font-semibold text-foreground/80">Reason for Deletion (Required)</Label>
                            <Textarea
                                id="delete-reason"
                                placeholder="e.g. User requested deletion, Policy violation..."
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                rows={3}
                                className="bg-muted/30 border-muted-foreground/20 focus-visible:ring-orange-500/20"
                            />
                        </div>
                    ) : (
                        <div className="space-y-4 animate-in fade-in slide-in-from-top-2">
                            <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-red-700 dark:text-red-400 text-xs flex gap-3">
                                <AlertTriangle className="h-5 w-5 shrink-0" />
                                <div>
                                    <p className="font-bold mb-1">DANGER ZONE</p>
                                    <p>This will completely remove the user and all related data from the database. This cannot be undone.</p>
                                </div>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="confirm-delete" className="text-sm font-semibold">
                                    Type <span className="font-bold text-red-600 font-mono">DELETE</span> to confirm
                                </Label>
                                <input
                                    id="confirm-delete"
                                    type="text"
                                    value={confirmText}
                                    onChange={(e) => setConfirmText(e.target.value.toUpperCase())}
                                    className="flex h-11 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-2 tracking-widest font-mono"
                                    placeholder="DELETE"
                                />
                            </div>
                        </div>
                    )}
                </div>

                <div className="bg-muted/30 p-4 flex items-center justify-end gap-3 border-t">
                    <Button variant="ghost" onClick={() => onOpenChange(false)} className="hover:bg-muted/50">
                        Cancel
                    </Button>
                    <Button
                        variant={deleteType === 'HARD' ? "destructive" : "default"}
                        onClick={handleSubmit}
                        disabled={submitting || (deleteType === 'HARD' && confirmText !== 'DELETE') || (deleteType === 'SOFT' && !reason.trim())}
                        className={cn(
                            "shadow-md hover:shadow-lg transition-all",
                            deleteType === 'SOFT' && "bg-orange-500 hover:bg-orange-600 text-white"
                        )}
                    >
                        {submitting ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : deleteType === 'HARD' ? (
                            <Skull className="h-4 w-4 mr-2" />
                        ) : (
                            <Trash2 className="h-4 w-4 mr-2" />
                        )}
                        {deleteType === 'HARD' ? 'Permanently Delete' : 'Soft Delete'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
}

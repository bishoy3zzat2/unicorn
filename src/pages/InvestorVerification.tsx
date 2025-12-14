import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Button } from '../components/ui/button'
import {
    UserCheck,
    Loader2,
    AlertCircle,
    CheckCircle,
    XCircle,
    ExternalLink,
    Briefcase,
    Calendar,
    DollarSign,
    RefreshCcw
} from 'lucide-react'
import { Alert, AlertDescription } from '../components/ui/alert'
import {
    fetchVerificationQueue,
    approveInvestorForPayment,
    rejectInvestorVerification,
    InvestorVerification
} from '../lib/api'
import { formatDate, formatCurrency } from '../lib/utils'
import { toast } from 'sonner'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../components/ui/dialog'
import { Textarea } from '../components/ui/textarea'

export function InvestorVerificationPage() {
    const [queue, setQueue] = useState<InvestorVerification[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [processingId, setProcessingId] = useState<string | null>(null)
    const [rejectDialog, setRejectDialog] = useState<{ open: boolean; investor: InvestorVerification | null }>({
        open: false,
        investor: null
    })
    const [rejectReason, setRejectReason] = useState('')

    useEffect(() => {
        loadQueue()
    }, [])

    async function loadQueue() {
        try {
            setLoading(true)
            setError(null)
            const data = await fetchVerificationQueue()
            setQueue(data)
        } catch (err) {
            console.error('Failed to fetch verification queue:', err)
            setError(err instanceof Error ? err.message : 'Failed to load verification queue')
        } finally {
            setLoading(false)
        }
    }

    async function handleApprove(investor: InvestorVerification) {
        try {
            setProcessingId(investor.id)
            await approveInvestorForPayment(investor.id)

            toast.success('Investor approved for payment', {
                description: `A notification has been sent to ${investor.userEmail}`
            })

            // Remove from queue
            setQueue(prev => prev.filter(i => i.id !== investor.id))
        } catch (err) {
            console.error('Failed to approve investor:', err)
            toast.error('Approval failed', {
                description: err instanceof Error ? err.message : 'Failed to approve investor'
            })
        } finally {
            setProcessingId(null)
        }
    }

    async function handleReject() {
        if (!rejectDialog.investor) return

        try {
            setProcessingId(rejectDialog.investor.id)
            await rejectInvestorVerification(rejectDialog.investor.id, rejectReason)

            toast.success('Verification rejected', {
                description: `The investor has been notified.`
            })

            // Remove from queue
            setQueue(prev => prev.filter(i => i.id !== rejectDialog.investor?.id))
            setRejectDialog({ open: false, investor: null })
            setRejectReason('')
        } catch (err) {
            console.error('Failed to reject investor:', err)
            toast.error('Rejection failed', {
                description: err instanceof Error ? err.message : 'Failed to reject verification'
            })
        } finally {
            setProcessingId(null)
        }
    }

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading verification queue...</span>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Investor Verification</h1>
                    <p className="text-muted-foreground mt-2">
                        Review and approve investor verification requests
                    </p>
                </div>
                <Button variant="outline" size="sm" onClick={loadQueue}>
                    <RefreshCcw className="h-4 w-4 mr-2" />
                    Refresh
                </Button>
            </div>

            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            {/* Queue Stats */}
            <div className="grid gap-4 md:grid-cols-3">
                <Card>
                    <CardHeader className="pb-2">
                        <CardTitle className="text-sm font-medium">Pending Requests</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold">{queue.length}</div>
                        <p className="text-xs text-muted-foreground">Awaiting review</p>
                    </CardContent>
                </Card>
            </div>

            {/* Verification Queue */}
            {queue.length === 0 ? (
                <Card>
                    <CardContent className="flex flex-col items-center justify-center py-16">
                        <div className="p-4 rounded-full bg-emerald-100 dark:bg-emerald-900/30 mb-4">
                            <UserCheck className="h-12 w-12 text-emerald-600 dark:text-emerald-400" />
                        </div>
                        <h2 className="text-xl font-semibold mb-2">Queue is Empty</h2>
                        <p className="text-muted-foreground text-center max-w-md">
                            There are no pending investor verification requests at this time.
                        </p>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid gap-4">
                    {queue.map((investor) => (
                        <Card key={investor.id} className="hover:border-primary/50 transition-colors">
                            <CardHeader>
                                <div className="flex items-start justify-between">
                                    <div>
                                        <CardTitle className="text-lg">{investor.userEmail}</CardTitle>
                                        <CardDescription className="flex items-center gap-2 mt-1">
                                            <Calendar className="h-4 w-4" />
                                            Requested: {formatDate(investor.verificationRequestedAt)}
                                        </CardDescription>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setRejectDialog({ open: true, investor })}
                                            disabled={processingId === investor.id}
                                        >
                                            <XCircle className="h-4 w-4 mr-2 text-red-500" />
                                            Reject
                                        </Button>
                                        <Button
                                            size="sm"
                                            onClick={() => handleApprove(investor)}
                                            disabled={processingId === investor.id}
                                        >
                                            {processingId === investor.id ? (
                                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                            ) : (
                                                <CheckCircle className="h-4 w-4 mr-2" />
                                            )}
                                            Approve for Payment
                                        </Button>
                                    </div>
                                </div>
                            </CardHeader>
                            <CardContent>
                                <div className="grid gap-4 md:grid-cols-2">
                                    {/* Bio */}
                                    {investor.bio && (
                                        <div className="space-y-1">
                                            <p className="text-sm font-medium text-muted-foreground">Bio</p>
                                            <p className="text-sm">{investor.bio}</p>
                                        </div>
                                    )}

                                    {/* Investment Budget */}
                                    {investor.investmentBudget && (
                                        <div className="space-y-1">
                                            <p className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                                                <DollarSign className="h-4 w-4" />
                                                Investment Budget
                                            </p>
                                            <p className="text-sm font-semibold">
                                                {formatCurrency(investor.investmentBudget)}
                                            </p>
                                        </div>
                                    )}

                                    {/* Preferred Industries */}
                                    {investor.preferredIndustries && (
                                        <div className="space-y-1">
                                            <p className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                                                <Briefcase className="h-4 w-4" />
                                                Preferred Industries
                                            </p>
                                            <div className="flex flex-wrap gap-1">
                                                {investor.preferredIndustries.split(',').map((industry, idx) => (
                                                    <span
                                                        key={idx}
                                                        className="px-2 py-0.5 text-xs rounded-full bg-primary/10 text-primary"
                                                    >
                                                        {industry.trim()}
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    )}

                                    {/* LinkedIn */}
                                    {investor.linkedInUrl && (
                                        <div className="space-y-1">
                                            <p className="text-sm font-medium text-muted-foreground">LinkedIn</p>
                                            <a
                                                href={investor.linkedInUrl}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="text-sm text-primary hover:underline flex items-center gap-1"
                                            >
                                                View Profile
                                                <ExternalLink className="h-3 w-3" />
                                            </a>
                                        </div>
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}

            {/* Reject Dialog */}
            <Dialog open={rejectDialog.open} onOpenChange={(open: boolean) => {
                if (!open) {
                    setRejectDialog({ open: false, investor: null })
                    setRejectReason('')
                }
            }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Reject Verification Request</DialogTitle>
                        <DialogDescription>
                            Please provide a reason for rejecting this verification request.
                            The investor will be notified.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="py-4">
                        <p className="text-sm font-medium mb-2">
                            Investor: {rejectDialog.investor?.userEmail}
                        </p>
                        <Textarea
                            placeholder="Enter rejection reason..."
                            value={rejectReason}
                            onChange={(e) => setRejectReason(e.target.value)}
                            rows={4}
                        />
                    </div>
                    <DialogFooter>
                        <Button
                            variant="outline"
                            onClick={() => {
                                setRejectDialog({ open: false, investor: null })
                                setRejectReason('')
                            }}
                        >
                            Cancel
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={handleReject}
                            disabled={processingId !== null}
                        >
                            {processingId ? (
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            ) : (
                                <XCircle className="h-4 w-4 mr-2" />
                            )}
                            Reject
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    )
}

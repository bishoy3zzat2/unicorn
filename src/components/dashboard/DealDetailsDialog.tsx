import {
    Dialog,
    DialogContent,
} from '../ui/dialog'
import { Button } from '../ui/button'
import {
    Users,
    Building2,
    Calendar,
    Percent,
    Clock,
    CheckCircle2,
    XCircle,
    FileText,
    Trash2,
    TrendingUp,
    DollarSign,
    ArrowRight,
    Sparkles,
    XCircle as CloseIcon
} from 'lucide-react'
import { Deal } from '../../lib/api'
import { formatTimeAgo, cn } from '../../lib/utils'

interface DealDetailsDialogProps {
    deal: Deal | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onEdit?: () => void
    onDelete?: () => void
    onRefresh?: () => void
}

// Deal type config with colors
const DEAL_TYPE_CONFIG: Record<string, { label: string; color: string; bgColor: string }> = {
    SEED: { label: 'Seed', color: 'text-emerald-600', bgColor: 'bg-emerald-500/10' },
    PRE_SEED: { label: 'Pre-Seed', color: 'text-green-600', bgColor: 'bg-green-500/10' },
    SERIES_A: { label: 'Series A', color: 'text-blue-600', bgColor: 'bg-blue-500/10' },
    SERIES_B: { label: 'Series B', color: 'text-indigo-600', bgColor: 'bg-indigo-500/10' },
    SERIES_C: { label: 'Series C', color: 'text-violet-600', bgColor: 'bg-violet-500/10' },
    BRIDGE: { label: 'Bridge', color: 'text-amber-600', bgColor: 'bg-amber-500/10' },
    CONVERTIBLE_NOTE: { label: 'Convertible Note', color: 'text-cyan-600', bgColor: 'bg-cyan-500/10' },
    SAFE: { label: 'SAFE', color: 'text-teal-600', bgColor: 'bg-teal-500/10' },
    EQUITY: { label: 'Equity', color: 'text-purple-600', bgColor: 'bg-purple-500/10' },
    DEBT: { label: 'Debt', color: 'text-rose-600', bgColor: 'bg-rose-500/10' },
    GRANT: { label: 'Grant', color: 'text-pink-600', bgColor: 'bg-pink-500/10' },
    OTHER: { label: 'Other', color: 'text-slate-600', bgColor: 'bg-slate-500/10' },
}

// Status config
const STATUS_CONFIG: Record<string, { label: string; icon: any; color: string; bgColor: string; borderColor: string }> = {
    PENDING: {
        label: 'Pending',
        icon: Clock,
        color: 'text-yellow-600 dark:text-yellow-400',
        bgColor: 'bg-yellow-500/10',
        borderColor: 'border-yellow-500/30'
    },
    COMPLETED: {
        label: 'Completed',
        icon: CheckCircle2,
        color: 'text-green-600 dark:text-green-400',
        bgColor: 'bg-green-500/10',
        borderColor: 'border-green-500/30'
    },
    CANCELLED: {
        label: 'Cancelled',
        icon: XCircle,
        color: 'text-red-600 dark:text-red-400',
        bgColor: 'bg-red-500/10',
        borderColor: 'border-red-500/30'
    },
}

export function DealDetailsDialog({
    deal,
    open,
    onOpenChange,
    onDelete,
}: DealDetailsDialogProps) {
    if (!deal) return null

    const formatAmount = (amount: number, currency: string) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency || 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount)
    }

    const statusConfig = STATUS_CONFIG[deal.status] || STATUS_CONFIG.PENDING
    const StatusIcon = statusConfig.icon
    const dealTypeConfig = deal.dealType ? DEAL_TYPE_CONFIG[deal.dealType] : null

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 border-0 [&>button]:hidden">
                {/* Hero Header with Amount */}
                <div className="relative bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-600 px-5 py-4 text-white">
                    <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxjaXJjbGUgZmlsbD0iI2ZmZiIgb3BhY2l0eT0iLjEiIGN4PSIyMCIgY3k9IjIwIiByPSIxIi8+PC9nPjwvc3ZnPg==')] opacity-30"></div>

                    <div className="relative flex items-center justify-between">
                        {/* Left: Amount & Equity */}
                        <div className="flex items-center gap-4">
                            <div className="h-12 w-12 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
                                <DollarSign className="h-6 w-6" />
                            </div>
                            <div>
                                <div className="text-2xl font-bold tracking-tight">
                                    {formatAmount(deal.amount, deal.currency)}
                                </div>
                                {deal.equityPercentage && (
                                    <div className="text-sm text-white/80 flex items-center gap-1">
                                        <Percent className="h-3.5 w-3.5" />
                                        {deal.equityPercentage}% equity stake
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Right: Status & Close */}
                        <div className="flex items-center gap-3">
                            <div className={cn(
                                "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold",
                                "bg-white/20 backdrop-blur-sm text-white border border-white/30"
                            )}>
                                <StatusIcon className="h-3.5 w-3.5" />
                                {statusConfig.label}
                            </div>
                            <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => onOpenChange(false)}
                                className="rounded-full bg-white/10 hover:bg-white/20 text-white h-9 w-9 z-10"
                            >
                                <CloseIcon className="h-5 w-5" />
                            </Button>
                        </div>
                    </div>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6 space-y-5 bg-slate-50 dark:bg-slate-900/50">

                    {/* Deal Parties - Visual Connection */}
                    <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-700">
                        <div className="flex items-center gap-2 mb-5">
                            <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-indigo-500/10 to-purple-500/10 flex items-center justify-center">
                                <Sparkles className="h-4 w-4 text-indigo-600" />
                            </div>
                            <h3 className="font-semibold text-foreground">Deal Parties</h3>
                        </div>

                        <div className="flex items-center gap-4">
                            {/* Investor Card */}
                            <div className="flex-1 bg-gradient-to-br from-indigo-50 to-blue-50 dark:from-indigo-900/20 dark:to-blue-900/20 rounded-xl p-4 border border-indigo-100 dark:border-indigo-800/50">
                                <div className="text-[10px] text-indigo-600 dark:text-indigo-400 uppercase tracking-wider font-bold mb-3">Investor</div>
                                <div className="flex items-center gap-3">
                                    {deal.investorAvatar ? (
                                        <img
                                            src={deal.investorAvatar}
                                            alt={deal.investorName}
                                            className="h-12 w-12 rounded-full object-cover ring-2 ring-white dark:ring-slate-800 shadow-lg"
                                        />
                                    ) : (
                                        <div className="h-12 w-12 rounded-full bg-gradient-to-br from-indigo-500 to-blue-500 text-white flex items-center justify-center shadow-lg">
                                            <Users className="h-5 w-5" />
                                        </div>
                                    )}
                                    <div className="min-w-0">
                                        <div className="font-bold text-foreground truncate">
                                            {deal.investorName || 'Unknown'}
                                        </div>
                                        <div className="text-xs text-muted-foreground truncate">
                                            {deal.investorEmail}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Arrow */}
                            <div className="flex-shrink-0">
                                <div className="h-10 w-10 rounded-full bg-gradient-to-r from-emerald-500 to-teal-500 flex items-center justify-center shadow-lg">
                                    <ArrowRight className="h-5 w-5 text-white" />
                                </div>
                            </div>

                            {/* Startup Card */}
                            <div className="flex-1 bg-gradient-to-br from-orange-50 to-amber-50 dark:from-orange-900/20 dark:to-amber-900/20 rounded-xl p-4 border border-orange-100 dark:border-orange-800/50">
                                <div className="text-[10px] text-orange-600 dark:text-orange-400 uppercase tracking-wider font-bold mb-3">Startup</div>
                                <div className="flex items-center gap-3">
                                    {deal.startupLogo ? (
                                        <img
                                            src={deal.startupLogo}
                                            alt={deal.startupName}
                                            className="h-12 w-12 rounded-xl object-cover ring-2 ring-white dark:ring-slate-800 shadow-lg"
                                        />
                                    ) : (
                                        <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-orange-500 to-amber-500 text-white flex items-center justify-center shadow-lg">
                                            <Building2 className="h-5 w-5" />
                                        </div>
                                    )}
                                    <div className="min-w-0">
                                        <div className="font-bold text-foreground truncate">
                                            {deal.startupName || 'Unknown'}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Deal Details Grid */}
                    <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-700">
                        <div className="flex items-center gap-2 mb-4">
                            <div className="h-8 w-8 rounded-lg bg-blue-500/10 flex items-center justify-center">
                                <TrendingUp className="h-4 w-4 text-blue-600" />
                            </div>
                            <h3 className="font-semibold text-foreground">Deal Information</h3>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            {/* Deal Type */}
                            {dealTypeConfig && (
                                <div className="bg-slate-50 dark:bg-slate-900/50 rounded-xl p-4">
                                    <div className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold mb-2">Deal Type</div>
                                    <div className={cn(
                                        "inline-flex items-center gap-2 px-3 py-1.5 rounded-lg font-semibold text-sm",
                                        dealTypeConfig.bgColor, dealTypeConfig.color
                                    )}>
                                        <div className={cn("h-2 w-2 rounded-full", dealTypeConfig.color.replace('text-', 'bg-'))}></div>
                                        {dealTypeConfig.label}
                                    </div>
                                </div>
                            )}

                            {/* Deal Date */}
                            <div className="bg-slate-50 dark:bg-slate-900/50 rounded-xl p-4">
                                <div className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold mb-2 flex items-center gap-1.5">
                                    <Calendar className="h-3 w-3" />
                                    Deal Date
                                </div>
                                <div className="font-semibold text-foreground">
                                    {deal.dealDate ? new Date(deal.dealDate).toLocaleDateString('en-US', {
                                        year: 'numeric',
                                        month: 'short',
                                        day: 'numeric'
                                    }) : 'Not specified'}
                                </div>
                            </div>

                            {/* Status */}
                            <div className="bg-slate-50 dark:bg-slate-900/50 rounded-xl p-4">
                                <div className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold mb-2">Status</div>
                                <div className={cn(
                                    "inline-flex items-center gap-2 px-3 py-1.5 rounded-lg font-semibold text-sm border",
                                    statusConfig.bgColor, statusConfig.color, statusConfig.borderColor
                                )}>
                                    <StatusIcon className="h-4 w-4" />
                                    {statusConfig.label}
                                </div>
                            </div>

                            {/* Created */}
                            <div className="bg-slate-50 dark:bg-slate-900/50 rounded-xl p-4">
                                <div className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold mb-2 flex items-center gap-1.5">
                                    <Clock className="h-3 w-3" />
                                    Created
                                </div>
                                <div className="font-semibold text-muted-foreground">
                                    {formatTimeAgo(deal.createdAt)}
                                </div>
                            </div>

                            {/* Commission */}
                            {deal.commissionPercentage && (
                                <div className="bg-gradient-to-br from-violet-50 to-purple-50 dark:from-violet-900/20 dark:to-purple-900/20 rounded-xl p-4 border border-violet-100 dark:border-violet-800/50">
                                    <div className="text-[10px] text-violet-600 dark:text-violet-400 uppercase tracking-wider font-bold mb-2 flex items-center gap-1.5">
                                        <Percent className="h-3 w-3" />
                                        Our Commission
                                    </div>
                                    <div className="text-lg font-bold text-violet-700 dark:text-violet-300">
                                        {deal.commissionPercentage}%
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Notes */}
                    {deal.notes && (
                        <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-700">
                            <div className="flex items-center gap-2 mb-3">
                                <div className="h-8 w-8 rounded-lg bg-amber-500/10 flex items-center justify-center">
                                    <FileText className="h-4 w-4 text-amber-600" />
                                </div>
                                <h3 className="font-semibold text-foreground">Notes</h3>
                            </div>
                            <div className="bg-amber-50/50 dark:bg-amber-900/10 rounded-xl p-4 text-sm text-foreground border border-amber-100 dark:border-amber-800/30">
                                {deal.notes}
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer Actions */}
                <div className="flex justify-end gap-3 p-5 border-t bg-white dark:bg-slate-900">
                    <Button
                        variant="outline"
                        onClick={() => onOpenChange(false)}
                        className="px-6 rounded-xl"
                    >
                        Close
                    </Button>
                    {onDelete && (
                        <Button
                            variant="outline"
                            className="px-6 rounded-xl text-red-600 hover:text-red-700 border-red-200 hover:border-red-300 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-900/20"
                            onClick={onDelete}
                        >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Delete Deal
                        </Button>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    )
}

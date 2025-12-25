import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog'
import { Button } from '../ui/button'
import { Input } from '../ui/input'
import { Label } from '../ui/label'
import { Textarea } from '../ui/textarea'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '../ui/select'
import {
    Loader2, TrendingUp, DollarSign, Calendar, Percent, FileText,
    Sparkles, XCircle, Pencil, Info
} from 'lucide-react'
import { cn } from '../../lib/utils'
import { updateDeal, Deal, DealRequest } from '../../lib/api'
import { toast } from 'sonner'


interface EditDealDialogProps {
    deal: Deal | null
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess: () => void
}

// Deal type options
const DEAL_TYPES = [
    { value: 'SEED', label: 'Seed', color: 'bg-emerald-500' },
    { value: 'PRE_SEED', label: 'Pre-Seed', color: 'bg-green-500' },
    { value: 'SERIES_A', label: 'Series A', color: 'bg-blue-500' },
    { value: 'SERIES_B', label: 'Series B', color: 'bg-indigo-500' },
    { value: 'SERIES_C', label: 'Series C', color: 'bg-violet-500' },
    { value: 'BRIDGE', label: 'Bridge', color: 'bg-amber-500' },
    { value: 'CONVERTIBLE_NOTE', label: 'Convertible Note', color: 'bg-cyan-500' },
    { value: 'SAFE', label: 'SAFE', color: 'bg-teal-500' },
    { value: 'EQUITY', label: 'Equity', color: 'bg-purple-500' },
    { value: 'DEBT', label: 'Debt', color: 'bg-rose-500' },
    { value: 'GRANT', label: 'Grant', color: 'bg-pink-500' },
    { value: 'OTHER', label: 'Other', color: 'bg-slate-500' },
]

// Status options
const STATUS_OPTIONS = [
    { value: 'PENDING', label: 'Pending', color: 'bg-yellow-500', textColor: 'text-yellow-700 dark:text-yellow-400' },
    { value: 'COMPLETED', label: 'Completed', color: 'bg-green-500', textColor: 'text-green-700 dark:text-green-400' },
    { value: 'CANCELLED', label: 'Cancelled', color: 'bg-red-500', textColor: 'text-red-700 dark:text-red-400' },
]

// Currency options
const CURRENCIES = [
    { value: 'USD', symbol: '$', flag: '🇺🇸' },
    { value: 'EUR', symbol: '€', flag: '🇪🇺' },
    { value: 'GBP', symbol: '£', flag: '🇬🇧' },
    { value: 'EGP', symbol: 'E£', flag: '🇪🇬' },
    { value: 'SAR', symbol: '﷼', flag: '🇸🇦' },
    { value: 'AED', symbol: 'د.إ', flag: '🇦🇪' },
]

export function EditDealDialog({ deal, open, onOpenChange, onSuccess }: EditDealDialogProps) {
    const [loading, setLoading] = useState(false)

    // Form state
    const [amount, setAmount] = useState('')
    const [currency, setCurrency] = useState('USD')
    const [status, setStatus] = useState<'PENDING' | 'COMPLETED' | 'CANCELLED'>('PENDING')
    const [dealType, setDealType] = useState('')
    const [equityPercentage, setEquityPercentage] = useState('')
    const [commissionPercentage, setCommissionPercentage] = useState('')
    const [notes, setNotes] = useState('')
    const [dealDate, setDealDate] = useState('')

    // Initialize form when deal changes or dialog opens
    useEffect(() => {
        if (deal && open) {
            setAmount(deal.amount?.toString() || '')
            setCurrency(deal.currency || 'USD')
            setStatus(deal.status || 'PENDING')
            setDealType(deal.dealType || '')
            setEquityPercentage(deal.equityPercentage?.toString() || '')
            setCommissionPercentage(deal.commissionPercentage?.toString() || '')
            setNotes(deal.notes || '')

            // Format date for input: YYYY-MM-DD
            if (deal.dealDate) {
                try {
                    const date = new Date(deal.dealDate)
                    if (!isNaN(date.getTime())) {
                        setDealDate(date.toISOString().split('T')[0])
                    }
                } catch (e) {
                    console.error("Invalid deal date", e)
                }
            } else {
                setDealDate('')
            }
        }
    }, [deal, open])

    const handleSubmit = async () => {
        if (!deal) return

        // Validation
        if (!amount || parseFloat(amount) <= 0) {
            toast.error('Please enter a valid amount')
            return
        }

        setLoading(true)
        try {
            const request: DealRequest = {
                investorId: deal.investorId, // Keep original
                startupId: deal.startupId,   // Keep original
                amount: parseFloat(amount),
                currency,
                status,
                dealType: dealType || undefined,
                // Send undefined if empty or 0 - backend should clear the value
                equityPercentage: equityPercentage && parseFloat(equityPercentage) > 0
                    ? parseFloat(equityPercentage)
                    : undefined,
                commissionPercentage: commissionPercentage && parseFloat(commissionPercentage) > 0
                    ? parseFloat(commissionPercentage)
                    : undefined,
                notes: notes || undefined,
                dealDate: dealDate ? new Date(dealDate).toISOString() : undefined,
            }

            await updateDeal(deal.id, request)
            toast.success('Deal updated successfully')
            onOpenChange(false)
            onSuccess()
        } catch (error: any) {
            toast.error(error.message || 'Failed to update deal')
        } finally {
            setLoading(false)
        }
    }

    const selectedCurrency = CURRENCIES.find(c => c.value === currency)

    if (!deal) return null

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[650px] max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 border-0 [&>button]:hidden">
                {/* Hero Header */}
                <div className="relative bg-gradient-to-br from-indigo-900 via-slate-900 to-indigo-950 p-6 text-white">
                    <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxjaXJjbGUgZmlsbD0iI2ZmZiIgb3BhY2l0eT0iLjEiIGN4PSIyMCIgY3k9IjIwIiByPSIxIi8+PC9nPjwvc3ZnPg==')] opacity-30"></div>
                    <div className="relative flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <div className="h-14 w-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                                <Pencil className="h-7 w-7 text-white" />
                            </div>
                            <div>
                                <DialogHeader className="p-0 space-y-0">
                                    <DialogTitle className="text-2xl font-bold text-white">Edit Deal</DialogTitle>
                                </DialogHeader>
                                <p className="text-white/80 text-sm mt-1">
                                    {deal.investorName} • {deal.startupName}
                                </p>
                            </div>
                        </div>
                        <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => onOpenChange(false)}
                            className="rounded-full bg-white/10 hover:bg-white/20 text-white h-10 w-10"
                        >
                            <XCircle className="h-5 w-5" />
                        </Button>
                    </div>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-slate-50 dark:bg-slate-900/50">

                    {/* Read-only info banner */}
                    <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-100 dark:border-blue-900/30 rounded-xl p-4 flex gap-3 text-sm text-blue-700 dark:text-blue-300">
                        <Info className="h-5 w-5 shrink-0" />
                        <p>
                            You are editing an existing deal. The investor and startup are fixed.
                            Changing critical financial details may affect platform statistics.
                        </p>
                    </div>

                    {/* Financial Details Section */}
                    <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-700 space-y-5">
                        <div className="flex items-center gap-2 mb-1">
                            <div className="h-8 w-8 rounded-lg bg-emerald-500/10 flex items-center justify-center">
                                <DollarSign className="h-4 w-4 text-emerald-600" />
                            </div>
                            <h3 className="font-semibold text-foreground">Financial Details</h3>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            {/* Amount */}
                            <div className="space-y-2 md:col-span-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Investment Amount <span className="text-red-500">*</span>
                                </Label>
                                <div className="relative">
                                    <div className="absolute left-3 top-1/2 -translate-y-1/2 text-lg font-semibold text-muted-foreground">
                                        {selectedCurrency?.symbol}
                                    </div>
                                    <Input
                                        type="number"
                                        placeholder="1,000,000"
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
                                        min="0"
                                        step="1000"
                                        className="pl-10 h-12 text-lg font-semibold rounded-xl border-2 focus:border-emerald-500"
                                    />
                                </div>
                            </div>

                            {/* Currency */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Currency
                                </Label>
                                <Select value={currency} onValueChange={setCurrency}>
                                    <SelectTrigger className="h-12 rounded-xl border-2">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {CURRENCIES.map((c) => (
                                            <SelectItem key={c.value} value={c.value}>
                                                <span className="flex items-center gap-2">
                                                    <span>{c.flag}</span>
                                                    <span className="font-medium">{c.value}</span>
                                                </span>
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            {/* Equity Percentage */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                                    <Percent className="h-3.5 w-3.5" />
                                    Equity Stake
                                </Label>
                                <div className="relative">
                                    <Input
                                        type="number"
                                        placeholder="5.0"
                                        value={equityPercentage}
                                        onChange={(e) => setEquityPercentage(e.target.value)}
                                        min="0"
                                        max="100"
                                        step="0.1"
                                        className="pr-10 h-11 rounded-xl border-2"
                                    />
                                    <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground font-medium">
                                        %
                                    </div>
                                </div>
                            </div>

                            {/* Commission Percentage */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                                    <Percent className="h-3.5 w-3.5" />
                                    Our Commission
                                </Label>
                                <div className="relative">
                                    <Input
                                        type="number"
                                        placeholder="2.5"
                                        value={commissionPercentage}
                                        onChange={(e) => setCommissionPercentage(e.target.value)}
                                        min="0"
                                        max="100"
                                        step="0.1"
                                        className="pr-10 h-11 rounded-xl border-2"
                                    />
                                    <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground font-medium">
                                        %
                                    </div>
                                </div>
                            </div>

                            {/* Deal Date */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                                    <Calendar className="h-3.5 w-3.5" />
                                    Deal Date
                                </Label>
                                <Input
                                    type="date"
                                    value={dealDate}
                                    onChange={(e) => setDealDate(e.target.value)}
                                    className="h-11 rounded-xl border-2"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Deal Classification Section */}
                    <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-700 space-y-5">
                        <div className="flex items-center gap-2 mb-1">
                            <div className="h-8 w-8 rounded-lg bg-blue-500/10 flex items-center justify-center">
                                <TrendingUp className="h-4 w-4 text-blue-600" />
                            </div>
                            <h3 className="font-semibold text-foreground">Deal Classification</h3>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* Deal Type */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Deal Type
                                </Label>
                                <Select value={dealType} onValueChange={setDealType}>
                                    <SelectTrigger className="h-11 rounded-xl border-2">
                                        <SelectValue placeholder="Select round type" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {DEAL_TYPES.map((type) => (
                                            <SelectItem key={type.value} value={type.value}>
                                                <span className="flex items-center gap-2">
                                                    <span className={cn("h-2 w-2 rounded-full", type.color)}></span>
                                                    {type.label}
                                                </span>
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            {/* Status */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Status
                                </Label>
                                <Select value={status} onValueChange={(v) => setStatus(v as any)}>
                                    <SelectTrigger className="h-11 rounded-xl border-2">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {STATUS_OPTIONS.map((s) => (
                                            <SelectItem key={s.value} value={s.value}>
                                                <span className="flex items-center gap-2">
                                                    <span className={cn("h-2 w-2 rounded-full", s.color)}></span>
                                                    <span className={s.textColor}>{s.label}</span>
                                                </span>
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        {/* Notes */}
                        <div className="space-y-2">
                            <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
                                <FileText className="h-3.5 w-3.5" />
                                Notes
                            </Label>
                            <Textarea
                                placeholder="Additional notes about this investment deal..."
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                rows={3}
                                className="rounded-xl border-2 resize-none"
                            />
                        </div>
                    </div>
                </div>

                {/* Footer Actions */}
                <div className="flex justify-end gap-3 p-5 border-t bg-white dark:bg-slate-900">
                    <Button
                        variant="outline"
                        onClick={() => onOpenChange(false)}
                        disabled={loading}
                        className="px-6 rounded-xl"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="px-6 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-700 hover:to-indigo-800 text-white shadow-lg shadow-indigo-500/20"
                    >
                        {loading ? (
                            <>
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                Saving...
                            </>
                        ) : (
                            <>
                                <Sparkles className="h-4 w-4 mr-2" />
                                Save Changes
                            </>
                        )}
                    </Button>
                </div>
            </DialogContent>
        </Dialog >
    )
}

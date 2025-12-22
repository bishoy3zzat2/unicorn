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
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from '../ui/command'
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from '../ui/popover'
import {
    Loader2, Check, ChevronsUpDown, Users, Building2,
    TrendingUp, DollarSign, Calendar, Percent, FileText,
    Sparkles, XCircle
} from 'lucide-react'
import { cn } from '../../lib/utils'
import { createDeal, searchUsers, searchStartups, DealRequest } from '../../lib/api'
import { toast } from 'sonner'

interface CreateDealDialogProps {
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

interface Investor {
    id: string
    email: string
    firstName?: string
    lastName?: string
    avatarUrl?: string
}

interface Startup {
    id: string
    name: string
    logoUrl?: string
}

export function CreateDealDialog({ open, onOpenChange, onSuccess }: CreateDealDialogProps) {
    const [loading, setLoading] = useState(false)

    // Form state
    const [investorId, setInvestorId] = useState('')
    const [startupId, setStartupId] = useState('')
    const [amount, setAmount] = useState('')
    const [currency, setCurrency] = useState('USD')
    const [status, setStatus] = useState<'PENDING' | 'COMPLETED' | 'CANCELLED'>('PENDING')
    const [dealType, setDealType] = useState('')
    const [equityPercentage, setEquityPercentage] = useState('')
    const [commissionPercentage, setCommissionPercentage] = useState('')
    const [notes, setNotes] = useState('')
    const [dealDate, setDealDate] = useState('')

    // Investor search
    const [investorOpen, setInvestorOpen] = useState(false)
    const [investorSearch, setInvestorSearch] = useState('')
    const [investors, setInvestors] = useState<Investor[]>([])
    const [selectedInvestor, setSelectedInvestor] = useState<Investor | null>(null)
    const [searchingInvestors, setSearchingInvestors] = useState(false)

    // Startup search
    const [startupOpen, setStartupOpen] = useState(false)
    const [startupSearch, setStartupSearch] = useState('')
    const [startups, setStartups] = useState<Startup[]>([])
    const [selectedStartup, setSelectedStartup] = useState<Startup | null>(null)
    const [searchingStartups, setSearchingStartups] = useState(false)

    // Reset form on close
    useEffect(() => {
        if (!open) {
            setInvestorId('')
            setStartupId('')
            setAmount('')
            setCurrency('USD')
            setStatus('PENDING')
            setDealType('')
            setEquityPercentage('')
            setCommissionPercentage('')
            setNotes('')
            setDealDate('')
            setSelectedInvestor(null)
            setSelectedStartup(null)
            setInvestors([])
            setStartups([])
        }
    }, [open])

    // Search investors
    useEffect(() => {
        const searchInvestors = async () => {
            if (investorSearch.length < 2) {
                setInvestors([])
                return
            }

            setSearchingInvestors(true)
            try {
                const result = await searchUsers(investorSearch, 'INVESTOR')
                setInvestors(result.content as Investor[])
            } catch (error) {
                console.error('Failed to search investors:', error)
            } finally {
                setSearchingInvestors(false)
            }
        }

        const timer = setTimeout(searchInvestors, 300)
        return () => clearTimeout(timer)
    }, [investorSearch])

    // Search startups
    useEffect(() => {
        const searchStartupsFunc = async () => {
            if (startupSearch.length < 2) {
                setStartups([])
                return
            }

            setSearchingStartups(true)
            try {
                const result = await searchStartups(startupSearch)
                setStartups(result.content as Startup[])
            } catch (error) {
                console.error('Failed to search startups:', error)
            } finally {
                setSearchingStartups(false)
            }
        }

        const timer = setTimeout(searchStartupsFunc, 300)
        return () => clearTimeout(timer)
    }, [startupSearch])

    const handleSubmit = async () => {
        // Validation
        if (!investorId) {
            toast.error('Please select an investor')
            return
        }
        if (!startupId) {
            toast.error('Please select a startup')
            return
        }
        if (!amount || parseFloat(amount) <= 0) {
            toast.error('Please enter a valid amount')
            return
        }

        setLoading(true)
        try {
            const request: DealRequest = {
                investorId,
                startupId,
                amount: parseFloat(amount),
                currency,
                status,
                dealType: dealType || undefined,
                equityPercentage: equityPercentage ? parseFloat(equityPercentage) : undefined,
                commissionPercentage: commissionPercentage ? parseFloat(commissionPercentage) : undefined,
                notes: notes || undefined,
                dealDate: dealDate ? new Date(dealDate).toISOString() : undefined,
            }

            await createDeal(request)
            toast.success('Deal created successfully')
            onOpenChange(false)
            onSuccess()
        } catch (error: any) {
            toast.error(error.message || 'Failed to create deal')
        } finally {
            setLoading(false)
        }
    }

    const getInvestorDisplayName = (investor: Investor) => {
        if (investor.firstName || investor.lastName) {
            return `${investor.firstName || ''} ${investor.lastName || ''}`.trim()
        }
        return investor.email
    }

    const selectedCurrency = CURRENCIES.find(c => c.value === currency)

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[650px] max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 border-0 [&>button]:hidden">
                {/* Hero Header */}
                <div className="relative bg-gradient-to-br from-emerald-500 via-teal-500 to-cyan-600 p-6 text-white">
                    <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxjaXJjbGUgZmlsbD0iI2ZmZiIgb3BhY2l0eT0iLjEiIGN4PSIyMCIgY3k9IjIwIiByPSIxIi8+PC9nPjwvc3ZnPg==')] opacity-30"></div>
                    <div className="relative flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <div className="h-14 w-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                                <Sparkles className="h-7 w-7 text-white" />
                            </div>
                            <div>
                                <DialogHeader className="p-0 space-y-0">
                                    <DialogTitle className="text-2xl font-bold text-white">Create New Deal</DialogTitle>
                                </DialogHeader>
                                <p className="text-white/80 text-sm mt-1">Record a new investment transaction</p>
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

                    {/* Parties Section */}
                    <div className="bg-white dark:bg-slate-800/50 rounded-2xl p-5 shadow-sm border border-slate-200 dark:border-slate-700 space-y-5">
                        <div className="flex items-center gap-2 mb-1">
                            <div className="h-8 w-8 rounded-lg bg-indigo-500/10 flex items-center justify-center">
                                <Users className="h-4 w-4 text-indigo-600" />
                            </div>
                            <h3 className="font-semibold text-foreground">Deal Parties</h3>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            {/* Investor Selection */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Investor <span className="text-red-500">*</span>
                                </Label>
                                <Popover open={investorOpen} onOpenChange={setInvestorOpen}>
                                    <PopoverTrigger asChild>
                                        <Button
                                            variant="outline"
                                            role="combobox"
                                            aria-expanded={investorOpen}
                                            className={cn(
                                                "w-full justify-between h-12 px-4 rounded-xl border-2 transition-all",
                                                selectedInvestor
                                                    ? "border-indigo-200 bg-indigo-50/50 dark:border-indigo-800 dark:bg-indigo-900/20"
                                                    : "border-dashed border-slate-300 dark:border-slate-600 hover:border-indigo-400"
                                            )}
                                        >
                                            {selectedInvestor ? (
                                                <div className="flex items-center gap-3">
                                                    {selectedInvestor.avatarUrl ? (
                                                        <img
                                                            src={selectedInvestor.avatarUrl}
                                                            alt=""
                                                            className="h-7 w-7 rounded-full ring-2 ring-white"
                                                        />
                                                    ) : (
                                                        <div className="h-7 w-7 rounded-full bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center">
                                                            <Users className="h-3.5 w-3.5 text-indigo-600" />
                                                        </div>
                                                    )}
                                                    <span className="font-medium">{getInvestorDisplayName(selectedInvestor)}</span>
                                                </div>
                                            ) : (
                                                <span className="text-muted-foreground">Search investor...</span>
                                            )}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-[350px] p-0" align="start">
                                        <Command shouldFilter={false}>
                                            <CommandInput
                                                placeholder="Search by name or email..."
                                                value={investorSearch}
                                                onValueChange={setInvestorSearch}
                                            />
                                            <CommandList>
                                                {searchingInvestors ? (
                                                    <div className="flex items-center justify-center py-6">
                                                        <Loader2 className="h-5 w-5 animate-spin text-primary" />
                                                    </div>
                                                ) : investors.length === 0 ? (
                                                    <CommandEmpty>
                                                        {investorSearch.length < 2
                                                            ? 'Type at least 2 characters'
                                                            : 'No investors found'}
                                                    </CommandEmpty>
                                                ) : (
                                                    <CommandGroup>
                                                        {investors.map((investor) => (
                                                            <CommandItem
                                                                key={investor.id}
                                                                value={investor.id}
                                                                onSelect={() => {
                                                                    setSelectedInvestor(investor)
                                                                    setInvestorId(investor.id)
                                                                    setInvestorOpen(false)
                                                                }}
                                                                className="py-3"
                                                            >
                                                                <Check
                                                                    className={cn(
                                                                        "mr-2 h-4 w-4",
                                                                        investorId === investor.id ? "opacity-100 text-primary" : "opacity-0"
                                                                    )}
                                                                />
                                                                <div className="flex items-center gap-3">
                                                                    {investor.avatarUrl ? (
                                                                        <img
                                                                            src={investor.avatarUrl}
                                                                            alt=""
                                                                            className="h-8 w-8 rounded-full"
                                                                        />
                                                                    ) : (
                                                                        <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center">
                                                                            <Users className="h-4 w-4 text-indigo-600" />
                                                                        </div>
                                                                    )}
                                                                    <div className="flex flex-col">
                                                                        <span className="font-medium">
                                                                            {getInvestorDisplayName(investor)}
                                                                        </span>
                                                                        <span className="text-xs text-muted-foreground">
                                                                            {investor.email}
                                                                        </span>
                                                                    </div>
                                                                </div>
                                                            </CommandItem>
                                                        ))}
                                                    </CommandGroup>
                                                )}
                                            </CommandList>
                                        </Command>
                                    </PopoverContent>
                                </Popover>
                            </div>

                            {/* Startup Selection */}
                            <div className="space-y-2">
                                <Label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Startup <span className="text-red-500">*</span>
                                </Label>
                                <Popover open={startupOpen} onOpenChange={setStartupOpen}>
                                    <PopoverTrigger asChild>
                                        <Button
                                            variant="outline"
                                            role="combobox"
                                            aria-expanded={startupOpen}
                                            className={cn(
                                                "w-full justify-between h-12 px-4 rounded-xl border-2 transition-all",
                                                selectedStartup
                                                    ? "border-orange-200 bg-orange-50/50 dark:border-orange-800 dark:bg-orange-900/20"
                                                    : "border-dashed border-slate-300 dark:border-slate-600 hover:border-orange-400"
                                            )}
                                        >
                                            {selectedStartup ? (
                                                <div className="flex items-center gap-3">
                                                    {selectedStartup.logoUrl ? (
                                                        <img
                                                            src={selectedStartup.logoUrl}
                                                            alt=""
                                                            className="h-7 w-7 rounded-lg"
                                                        />
                                                    ) : (
                                                        <div className="h-7 w-7 rounded-lg bg-orange-100 dark:bg-orange-900/50 flex items-center justify-center">
                                                            <Building2 className="h-3.5 w-3.5 text-orange-600" />
                                                        </div>
                                                    )}
                                                    <span className="font-medium">{selectedStartup.name}</span>
                                                </div>
                                            ) : (
                                                <span className="text-muted-foreground">Search startup...</span>
                                            )}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-[350px] p-0" align="start">
                                        <Command shouldFilter={false}>
                                            <CommandInput
                                                placeholder="Search by name..."
                                                value={startupSearch}
                                                onValueChange={setStartupSearch}
                                            />
                                            <CommandList>
                                                {searchingStartups ? (
                                                    <div className="flex items-center justify-center py-6">
                                                        <Loader2 className="h-5 w-5 animate-spin text-primary" />
                                                    </div>
                                                ) : startups.length === 0 ? (
                                                    <CommandEmpty>
                                                        {startupSearch.length < 2
                                                            ? 'Type at least 2 characters'
                                                            : 'No startups found'}
                                                    </CommandEmpty>
                                                ) : (
                                                    <CommandGroup>
                                                        {startups.map((startup) => (
                                                            <CommandItem
                                                                key={startup.id}
                                                                value={startup.id}
                                                                onSelect={() => {
                                                                    setSelectedStartup(startup)
                                                                    setStartupId(startup.id)
                                                                    setStartupOpen(false)
                                                                }}
                                                                className="py-3"
                                                            >
                                                                <Check
                                                                    className={cn(
                                                                        "mr-2 h-4 w-4",
                                                                        startupId === startup.id ? "opacity-100 text-primary" : "opacity-0"
                                                                    )}
                                                                />
                                                                <div className="flex items-center gap-3">
                                                                    {startup.logoUrl ? (
                                                                        <img
                                                                            src={startup.logoUrl}
                                                                            alt=""
                                                                            className="h-8 w-8 rounded-lg object-cover"
                                                                        />
                                                                    ) : (
                                                                        <div className="h-8 w-8 rounded-lg bg-orange-100 flex items-center justify-center">
                                                                            <Building2 className="h-4 w-4 text-orange-600" />
                                                                        </div>
                                                                    )}
                                                                    <span className="font-medium">{startup.name}</span>
                                                                </div>
                                                            </CommandItem>
                                                        ))}
                                                    </CommandGroup>
                                                )}
                                            </CommandList>
                                        </Command>
                                    </PopoverContent>
                                </Popover>
                            </div>
                        </div>
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
                        className="px-6 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white shadow-lg shadow-emerald-500/25"
                    >
                        {loading ? (
                            <>
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                Creating...
                            </>
                        ) : (
                            <>
                                <Sparkles className="h-4 w-4 mr-2" />
                                Create Deal
                            </>
                        )}
                    </Button>
                </div>
            </DialogContent>
        </Dialog >
    )
}

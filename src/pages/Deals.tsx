import { useState, useEffect } from 'react'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "../components/ui/table"
import { Button } from "../components/ui/button"
import { Input } from "../components/ui/input"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../components/ui/select"
import { Badge } from "../components/ui/badge"
import {
    Loader2,
    RefreshCcw,
    Search,
    Briefcase,
    Clock,
    Eye,
    CheckCircle2,
    XCircle,
    Plus,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    TrendingUp,
    Users,
    Building2,
    Trash2,
    Pencil,
    Percent
} from 'lucide-react'
import {
    fetchAllDeals,
    fetchDealStats,
    deleteDeal,
    Deal,
    DealStats
} from '../lib/api'
import { formatTimeAgo } from '../lib/utils'
import { CreateDealDialog } from '../components/dashboard/CreateDealDialog'
import { EditDealDialog } from '../components/dashboard/EditDealDialog'
import { DealDetailsDialog } from '../components/dashboard/DealDetailsDialog'
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "../components/ui/alert-dialog"
import { toast } from 'sonner'

// Deal type labels
const DEAL_TYPE_LABELS: Record<string, string> = {
    SEED: 'Seed',
    PRE_SEED: 'Pre-Seed',
    SERIES_A: 'Series A',
    SERIES_B: 'Series B',
    SERIES_C: 'Series C',
    BRIDGE: 'Bridge',
    CONVERTIBLE_NOTE: 'Convertible Note',
    SAFE: 'SAFE',
    EQUITY: 'Equity',
    DEBT: 'Debt',
    GRANT: 'Grant',
    OTHER: 'Other',
}

export function Deals() {
    const [deals, setDeals] = useState<Deal[]>([])
    const [stats, setStats] = useState<DealStats>({
        totalDeals: 0,
        pendingDeals: 0,
        completedDeals: 0,
        cancelledDeals: 0,
        totalCompletedAmount: 0,
        totalCommissionRevenue: 0
    })
    const [loading, setLoading] = useState(true)
    const [searchQuery, setSearchQuery] = useState('')
    const [statusFilter, setStatusFilter] = useState<string>('ALL')

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [pageSize, setPageSize] = useState(20)

    // Dialogs
    const [createDialogOpen, setCreateDialogOpen] = useState(false)
    const [selectedDeal, setSelectedDeal] = useState<Deal | null>(null)
    const [detailsDialogOpen, setDetailsDialogOpen] = useState(false)
    const [editDialogOpen, setEditDialogOpen] = useState(false)
    const [dealToEdit, setDealToEdit] = useState<Deal | null>(null)

    // Delete confirmation
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
    const [dealToDelete, setDealToDelete] = useState<Deal | null>(null)
    const [deleting, setDeleting] = useState(false)

    // Load stats
    useEffect(() => {
        loadStats()
    }, [])

    // Load deals when filters change
    useEffect(() => {
        loadDeals()
    }, [currentPage, pageSize, statusFilter])
    const loadStats = async () => {
        try {
            const data = await fetchDealStats()
            setStats(data)
        } catch (error) {
            console.error('Failed to load stats:', error)
        }
    }

    const loadDeals = async () => {
        setLoading(true)
        try {
            const data = await fetchAllDeals(currentPage, pageSize, searchQuery || undefined)

            // Filter by status client-side if needed
            let filteredContent = data.content
            if (statusFilter !== 'ALL') {
                filteredContent = data.content.filter(d => d.status === statusFilter)
            }

            setDeals(filteredContent)
            setTotalPages(data.totalPages)
            setTotalElements(data.totalElements)
        } catch (error: any) {
            console.error('Failed to load deals:', error)
            toast.error('Failed to load deals')
        } finally {
            setLoading(false)
        }
    }

    const handleRefresh = () => {
        loadStats()
        loadDeals()
    }

    const handleSearch = () => {
        setCurrentPage(0)
        loadDeals()
    }

    const handleDeleteDeal = async () => {
        if (!dealToDelete) return

        setDeleting(true)
        try {
            await deleteDeal(dealToDelete.id)
            toast.success('Deal deleted successfully')
            handleRefresh()
        } catch (error: any) {
            toast.error(error.message || 'Failed to delete deal')
        } finally {
            setDeleting(false)
            setDeleteDialogOpen(false)
            setDealToDelete(null)
        }
    }

    const getStatusBadge = (status: string) => {
        const styles = {
            PENDING: "bg-yellow-500/15 text-yellow-700 dark:text-yellow-400 hover:bg-yellow-500/25 border-yellow-200 dark:border-yellow-900/50",
            COMPLETED: "bg-green-500/15 text-green-700 dark:text-green-400 hover:bg-green-500/25 border-green-200 dark:border-green-900/50",
            CANCELLED: "bg-red-500/15 text-red-700 dark:text-red-400 hover:bg-red-500/25 border-red-200 dark:border-red-900/50",
        }

        const icons = {
            PENDING: Clock,
            COMPLETED: CheckCircle2,
            CANCELLED: XCircle,
        }

        const Style = styles[status as keyof typeof styles] || "bg-muted text-muted-foreground"
        const Icon = icons[status as keyof typeof icons] || Clock

        return (
            <Badge variant="outline" className={`gap-1.5 py-1 px-2.5 transition-colors duration-200 ${Style}`}>
                <Icon className="h-3.5 w-3.5" />
                <span className="font-medium capitalize">{status.toLowerCase()}</span>
            </Badge>
        )
    }

    const formatAmount = (amount: number, currency: string) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency || 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount)
    }

    const formatCompactAmount = (amount: number, currency: string = 'USD') => {
        const currencySymbol = currency === 'USD' ? '$' : currency === 'EUR' ? '€' : currency === 'GBP' ? '£' : currency === 'EGP' ? 'E£' : '$'

        if (amount < 1000) {
            return `${currencySymbol}${amount}`
        } else if (amount < 1000000) {
            const value = amount / 1000
            return `${currencySymbol}${value % 1 === 0 ? value : value.toFixed(1)}K`
        } else if (amount < 1000000000) {
            const value = amount / 1000000
            return `${currencySymbol}${value % 1 === 0 ? value : value.toFixed(1)}M`
        } else {
            const value = amount / 1000000000
            return `${currencySymbol}${value % 1 === 0 ? value : value.toFixed(1)}B`
        }
    }

    // Client-side search filtering
    const filteredDeals = deals.filter(deal => {
        if (!searchQuery) return true
        const query = searchQuery.toLowerCase()
        return (
            deal.investorName?.toLowerCase().includes(query) ||
            deal.investorEmail?.toLowerCase().includes(query) ||
            deal.startupName?.toLowerCase().includes(query) ||
            deal.notes?.toLowerCase().includes(query)
        )
    })

    return (
        <div className="space-y-6 transition-colors duration-300">

            {/* Stats Overview - 6 Cards Grid */}
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">

                {/* Total Deals */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Briefcase className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.totalDeals.toLocaleString()}</div>
                    <div className="text-blue-100 text-sm">Total Deals</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <Briefcase className="h-4 w-4 opacity-70" />
                        <span className="text-sm">All recorded deals</span>
                    </div>
                </div>

                {/* Pending Deals */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Clock className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.pendingDeals.toLocaleString()}</div>
                    <div className="text-amber-100 text-sm">Pending Deals</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">
                            {stats.totalDeals > 0
                                ? <><strong>{Math.round((stats.pendingDeals / stats.totalDeals) * 100)}%</strong> of total</>
                                : 'No deals yet'}
                        </span>
                    </div>
                </div>

                {/* Completed Deals */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <CheckCircle2 className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.completedDeals.toLocaleString()}</div>
                    <div className="text-emerald-100 text-sm">Completed Deals</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">
                            {stats.totalDeals > 0
                                ? <><strong>{Math.round((stats.completedDeals / stats.totalDeals) * 100)}%</strong> success rate</>
                                : 'No completed deals'}
                        </span>
                    </div>
                </div>

                {/* Cancelled Deals */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-rose-500 to-red-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <XCircle className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.cancelledDeals.toLocaleString()}</div>
                    <div className="text-rose-100 text-sm">Cancelled Deals</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">
                            {stats.totalDeals > 0
                                ? <><strong>{Math.round((stats.cancelledDeals / stats.totalDeals) * 100)}%</strong> of total</>
                                : 'No cancellations'}
                        </span>
                    </div>
                </div>

                {/* Total Invested */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <TrendingUp className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactAmount(stats.totalCompletedAmount || 0, 'USD')}</div>
                    <div className="text-indigo-100 text-sm">Total Invested (USD)</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Total value of completed deals</span>
                    </div>
                </div>

                {/* Commission Revenue */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-teal-500 to-cyan-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Percent className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{formatCompactAmount(stats.totalCommissionRevenue || 0, 'USD')}</div>
                    <div className="text-teal-100 text-sm">Commission Revenue</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">Revenue from success fees</span>
                    </div>
                </div>

            </div>

            {/* Main Content Area */}
            <div className="space-y-4">
                {/* Toolbar */}
                <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800">
                    {/* Status Tabs */}
                    <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-lg overflow-x-auto max-w-full no-scrollbar">
                        {['ALL', 'PENDING', 'COMPLETED', 'CANCELLED'].map((status) => (
                            <button
                                key={status}
                                onClick={() => {
                                    setStatusFilter(status)
                                    setCurrentPage(0)
                                }}
                                className={`
                                    px-4 py-1.5 rounded-md text-sm font-medium transition-all duration-200
                                    ${statusFilter === status
                                        ? 'bg-white dark:bg-slate-700 text-foreground shadow-sm'
                                        : 'text-slate-500 dark:text-slate-400 hover:text-foreground hover:bg-white/50 dark:hover:bg-slate-700/50'}
                                `}
                            >
                                {status === 'ALL' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
                            </button>
                        ))}
                    </div>

                    <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                        <div className="relative flex-1 sm:w-64">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search investor, startup..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                                className="pl-9 bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
                            />
                        </div>

                        <Button
                            variant="outline"
                            size="icon"
                            onClick={handleRefresh}
                            className={`shrink-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700 ${loading ? 'animate-spin' : ''}`}
                        >
                            <RefreshCcw className="h-4 w-4" />
                        </Button>

                        <Button
                            onClick={() => setCreateDialogOpen(true)}
                            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md"
                        >
                            <Plus className="h-4 w-4 mr-2" />
                            New Deal
                        </Button>
                    </div>
                </div>

                {/* Table */}
                <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                    {loading && deals.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                            <Loader2 className="h-10 w-10 animate-spin mb-4" />
                            <p>Loading deals...</p>
                        </div>
                    ) : filteredDeals.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-center">
                            <div className="h-16 w-16 bg-muted rounded-full flex items-center justify-center mb-4">
                                <Briefcase className="h-8 w-8 text-muted-foreground" />
                            </div>
                            <h3 className="text-lg font-semibold text-foreground">No deals found</h3>
                            <p className="text-muted-foreground max-w-sm mx-auto mt-1">
                                No deals match your current filters. Try adjusting them or create a new deal.
                            </p>
                            <Button
                                className="mt-6"
                                onClick={() => setCreateDialogOpen(true)}
                            >
                                <Plus className="h-4 w-4 mr-2" />
                                Create First Deal
                            </Button>
                        </div>
                    ) : (
                        <>
                            <div className="overflow-x-auto">
                                <Table>
                                    <TableHeader className="bg-slate-50 dark:bg-slate-800/50">
                                        <TableRow className="hover:bg-transparent border-b-border">
                                            <TableHead className="font-semibold text-muted-foreground pl-6">Investor</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Startup</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Amount</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Type</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Status</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Date</TableHead>
                                            <TableHead className="text-right pr-6 font-semibold text-muted-foreground">Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {filteredDeals.map((deal) => (
                                            <TableRow
                                                key={deal.id}
                                                className="group cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800/50 transition-colors border-b-border"
                                                onClick={() => {
                                                    setSelectedDeal(deal)
                                                    setDetailsDialogOpen(true)
                                                }}
                                            >
                                                <TableCell className="pl-6 py-4">
                                                    <div className="flex items-center gap-3">
                                                        {deal.investorAvatar ? (
                                                            <img
                                                                src={deal.investorAvatar}
                                                                alt={deal.investorName}
                                                                className="h-9 w-9 rounded-full object-cover ring-2 ring-white dark:ring-slate-800"
                                                            />
                                                        ) : (
                                                            <div className="h-9 w-9 rounded-full bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 flex items-center justify-center ring-2 ring-white dark:ring-slate-800">
                                                                <Users className="h-4 w-4" />
                                                            </div>
                                                        )}
                                                        <div className="flex flex-col">
                                                            <span className="font-semibold text-foreground">
                                                                {deal.investorName || 'Unknown'}
                                                            </span>
                                                            <span className="text-xs text-muted-foreground">
                                                                {deal.investorEmail}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex items-center gap-3">
                                                        {deal.startupLogo ? (
                                                            <img
                                                                src={deal.startupLogo}
                                                                alt={deal.startupName}
                                                                className="h-8 w-8 rounded-lg object-cover"
                                                            />
                                                        ) : (
                                                            <div className="h-8 w-8 rounded-lg bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 flex items-center justify-center">
                                                                <Building2 className="h-4 w-4" />
                                                            </div>
                                                        )}
                                                        <span className="font-medium text-foreground">
                                                            {deal.startupName || 'Unknown'}
                                                        </span>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex flex-col">
                                                        <span className="font-semibold text-foreground">
                                                            {formatAmount(deal.amount, deal.currency)}
                                                        </span>
                                                        {deal.equityPercentage && (
                                                            <span className="text-xs text-muted-foreground">
                                                                {deal.equityPercentage}% equity
                                                            </span>
                                                        )}
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    {deal.dealType ? (
                                                        <Badge variant="secondary" className="font-medium">
                                                            {DEAL_TYPE_LABELS[deal.dealType] || deal.dealType}
                                                        </Badge>
                                                    ) : (
                                                        <span className="text-muted-foreground">-</span>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    {getStatusBadge(deal.status)}
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex items-center gap-1.5 text-muted-foreground">
                                                        <Clock className="h-3.5 w-3.5" />
                                                        <span className="text-sm">{formatTimeAgo(deal.dealDate)}</span>
                                                    </div>
                                                </TableCell>
                                                <TableCell className="text-right pr-6">
                                                    <div className="flex items-center justify-end gap-1">
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 transition-all"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                setSelectedDeal(deal)
                                                                setDetailsDialogOpen(true)
                                                            }}
                                                        >
                                                            <Eye className="h-4 w-4" />
                                                        </Button>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                setDealToEdit(deal)
                                                                setEditDialogOpen(true)
                                                            }}
                                                        >
                                                            <Pencil className="h-4 w-4" />
                                                        </Button>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                setDealToDelete(deal)
                                                                setDeleteDialogOpen(true)
                                                            }}
                                                        >
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </div>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>

                            {/* Pagination Footer */}
                            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 px-4 py-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50">
                                <div className="text-sm text-muted-foreground">
                                    Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} entries
                                </div>
                                <div className="flex flex-wrap items-center gap-4 lg:gap-6">
                                    <div className="flex items-center space-x-2">
                                        <p className="text-sm font-medium">Rows per page</p>
                                        <Select
                                            value={`${pageSize}`}
                                            onValueChange={(value) => {
                                                setPageSize(Number(value))
                                                setCurrentPage(0)
                                            }}
                                        >
                                            <SelectTrigger className="h-8 w-[70px]">
                                                <SelectValue placeholder={pageSize} />
                                            </SelectTrigger>
                                            <SelectContent side="top">
                                                {[10, 20, 50, 100].map((size) => (
                                                    <SelectItem key={size} value={`${size}`}>
                                                        {size}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="flex items-center space-x-2">
                                        <Button
                                            variant="outline"
                                            className="hidden h-8 w-8 p-0 lg:flex bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700"
                                            onClick={() => setCurrentPage(0)}
                                            disabled={currentPage === 0}
                                        >
                                            <span className="sr-only">Go to first page</span>
                                            <ChevronsLeft className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            className="h-8 w-8 p-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700"
                                            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                                            disabled={currentPage === 0}
                                        >
                                            <span className="sr-only">Go to previous page</span>
                                            <ChevronLeft className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            className="h-8 w-8 p-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700"
                                            onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                                            disabled={currentPage >= totalPages - 1}
                                        >
                                            <span className="sr-only">Go to next page</span>
                                            <ChevronRight className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            className="hidden h-8 w-8 p-0 lg:flex bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700"
                                            onClick={() => setCurrentPage(totalPages - 1)}
                                            disabled={currentPage >= totalPages - 1}
                                        >
                                            <span className="sr-only">Go to last page</span>
                                            <ChevronsRight className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Create Deal Dialog */}
            <CreateDealDialog
                open={createDialogOpen}
                onOpenChange={setCreateDialogOpen}
                onSuccess={handleRefresh}
            />

            {/* Edit Deal Dialog */}
            <EditDealDialog
                deal={dealToEdit}
                open={editDialogOpen}
                onOpenChange={setEditDialogOpen}
                onSuccess={handleRefresh}
            />

            {/* Deal Details Dialog */}
            {selectedDeal && (
                <DealDetailsDialog
                    deal={selectedDeal}
                    open={detailsDialogOpen}
                    onOpenChange={setDetailsDialogOpen}
                    onDelete={() => {
                        setDetailsDialogOpen(false)
                        setDealToDelete(selectedDeal)
                        setDeleteDialogOpen(true)
                    }}
                    onRefresh={handleRefresh}
                />
            )}

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Deal</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete this deal between{' '}
                            <strong>{dealToDelete?.investorName}</strong> and{' '}
                            <strong>{dealToDelete?.startupName}</strong>?
                            This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel disabled={deleting}>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={handleDeleteDeal}
                            disabled={deleting}
                            className="bg-red-600 hover:bg-red-700"
                        >
                            {deleting ? (
                                <>
                                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                    Deleting...
                                </>
                            ) : (
                                'Delete'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

        </div>
    )
}

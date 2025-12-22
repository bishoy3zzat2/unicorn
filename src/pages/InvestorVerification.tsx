import { useEffect, useState } from 'react'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '../components/ui/table'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '../components/ui/select'
import { Badge } from '../components/ui/badge'
import {
    Users,
    Loader2,
    AlertCircle,
    CheckCircle,
    XCircle,
    ExternalLink,
    RefreshCcw,
    Search,
    Clock,
    DollarSign,
    BadgeCheck,
    Eye,
    ChevronLeft,
    ChevronRight,
    ChevronsLeft,
    ChevronsRight,
    UserCheck,
    Briefcase,
    Mail,
} from 'lucide-react'
import { Alert, AlertDescription } from '../components/ui/alert'
import {
    fetchVerificationQueue,
    approveInvestorForPayment,
    rejectInvestorVerification,
    fetchInvestorStats,
    InvestorVerification,
    InvestorStats
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
import { KPICard } from '../components/dashboard/KPICard'
import { Avatar, AvatarFallback, AvatarImage } from '../components/ui/avatar'

export function InvestorVerificationPage() {
    // Data state
    const [queue, setQueue] = useState<InvestorVerification[]>([])
    const [stats, setStats] = useState<InvestorStats | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [processingId, setProcessingId] = useState<string | null>(null)

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0)
    const [pageSize, setPageSize] = useState(20)
    const [totalElements, setTotalElements] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

    // Search and filter state
    const [searchQuery, setSearchQuery] = useState('')
    const [debouncedQuery, setDebouncedQuery] = useState('')

    // Modal/Sheet state
    const [selectedInvestor, setSelectedInvestor] = useState<InvestorVerification | null>(null)
    const [detailsOpen, setDetailsOpen] = useState(false)
    const [rejectDialog, setRejectDialog] = useState<{ open: boolean; investor: InvestorVerification | null }>({
        open: false,
        investor: null
    })
    const [rejectReason, setRejectReason] = useState('')

    // Debounce search query
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedQuery(searchQuery)
            setCurrentPage(0)
        }, 500)
        return () => clearTimeout(handler)
    }, [searchQuery])

    // Load data when page, size, or query changes
    useEffect(() => {
        loadData()
    }, [currentPage, pageSize, debouncedQuery])

    // Initial stats load
    useEffect(() => {
        loadStats()
    }, [])

    async function loadStats() {
        try {
            const statsData = await fetchInvestorStats()
            setStats(statsData)
        } catch (err) {
            console.error('Failed to fetch stats:', err)
        }
    }

    async function loadData() {
        try {
            setLoading(true)
            setError(null)
            const response = await fetchVerificationQueue(currentPage, pageSize, debouncedQuery || undefined)
            setQueue(response.content || [])
            setTotalElements(response.totalElements || 0)
            setTotalPages(response.totalPages || 0)
        } catch (err) {
            console.error('Failed to fetch verification data:', err)
            setError(err instanceof Error ? err.message : 'Failed to load verification data')
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

            // Refresh data
            loadData()
            loadStats()
            setDetailsOpen(false)
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

            // Refresh data
            loadData()
            loadStats()
            setRejectDialog({ open: false, investor: null })
            setRejectReason('')
            setDetailsOpen(false)
        } catch (err) {
            console.error('Failed to reject investor:', err)
            toast.error('Rejection failed', {
                description: err instanceof Error ? err.message : 'Failed to reject verification'
            })
        } finally {
            setProcessingId(null)
        }
    }

    function handleRefresh() {
        loadData()
        loadStats()
    }

    function handleViewDetails(investor: InvestorVerification) {
        setSelectedInvestor(investor)
        setDetailsOpen(true)
    }

    function getInitials(investor: InvestorVerification): string {
        if (investor.userName) {
            return investor.userName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
        }
        return investor.userEmail.substring(0, 2).toUpperCase()
    }

    return (
        <div className="space-y-6 transition-colors duration-300">
            {/* Header Section */}
            <div className="flex flex-col gap-1">
                <h1 className="text-3xl font-bold tracking-tight text-foreground">Investor Verification</h1>
                <p className="text-muted-foreground text-lg">
                    Review and approve investor verification requests before payment processing.
                </p>
            </div>

            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            {/* Stats Overview */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <KPICard
                    title="Total Investors"
                    value={(stats?.totalInvestors || 0).toString()}
                    icon={Users}
                    iconColor="text-blue-600 dark:text-blue-400"
                />
                <KPICard
                    title="Verified"
                    value={(stats?.verifiedInvestors || 0).toString()}
                    icon={BadgeCheck}
                    iconColor="text-emerald-600 dark:text-emerald-400"
                />
                <KPICard
                    title="Pending Review"
                    value={(stats?.pendingVerifications || 0).toString()}
                    icon={Clock}
                    iconColor="text-yellow-600 dark:text-yellow-400"
                    trend={stats?.pendingVerifications && stats.pendingVerifications > 0 ? 'Requires Action' : undefined}
                />
                <KPICard
                    title="Total Capital"
                    value={formatCurrency(stats?.totalInvestmentBudget || 0)}
                    icon={DollarSign}
                    iconColor="text-indigo-600 dark:text-indigo-400"
                />
            </div>

            {/* Main Content Area */}
            <div className="space-y-4">
                {/* Toolbar */}
                <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800">
                    <div className="flex items-center gap-3">
                        <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
                            <UserCheck className="h-5 w-5 text-primary" />
                        </div>
                        <div>
                            <h2 className="text-lg font-bold tracking-tight">Verification Queue</h2>
                            <div className="flex items-center gap-2">
                                {loading && <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />}
                                <span className="text-xs text-muted-foreground font-medium">
                                    {totalElements} {totalElements === 1 ? 'request' : 'requests'} pending
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                        <div className="relative flex-1 sm:w-64">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search by email..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="pl-9 bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
                            />
                        </div>

                        <Button
                            variant="outline"
                            size="icon"
                            onClick={handleRefresh}
                            disabled={loading}
                            className={`shrink-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700 ${loading ? 'animate-spin' : ''}`}
                        >
                            <RefreshCcw className="h-4 w-4" />
                        </Button>
                    </div>
                </div>

                {/* Table */}
                <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                    {loading && (!queue || queue.length === 0) ? (
                        <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                            <Loader2 className="h-10 w-10 animate-spin mb-4" />
                            <p>Loading verification queue...</p>
                        </div>
                    ) : (!queue || queue.length === 0) ? (
                        <div className="flex flex-col items-center justify-center py-20 text-center">
                            <div className="h-16 w-16 bg-emerald-100 dark:bg-emerald-900/30 rounded-full flex items-center justify-center mb-4">
                                <UserCheck className="h-8 w-8 text-emerald-600 dark:text-emerald-400" />
                            </div>
                            <h3 className="text-lg font-semibold text-foreground">Queue is Empty</h3>
                            <p className="text-muted-foreground max-w-sm mx-auto mt-1">
                                There are no pending investor verification requests at this time.
                            </p>
                        </div>
                    ) : (
                        <>
                            <div className="overflow-x-auto">
                                <Table>
                                    <TableHeader className="bg-slate-50 dark:bg-slate-800/50">
                                        <TableRow className="hover:bg-transparent border-b-border">
                                            <TableHead className="font-semibold text-muted-foreground pl-6">Investor</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Budget</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Industries</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Requested</TableHead>
                                            <TableHead className="text-right pr-6 font-semibold text-muted-foreground">Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {queue.map((investor) => (
                                            <TableRow
                                                key={investor.id}
                                                className="group cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800/50 transition-colors border-b-border"
                                                onClick={() => handleViewDetails(investor)}
                                            >
                                                <TableCell className="pl-6 py-4">
                                                    <div className="flex items-center gap-3">
                                                        <Avatar className="h-10 w-10 border-2 border-white dark:border-slate-800 shadow-sm">
                                                            <AvatarImage src={investor.userAvatar || undefined} alt={investor.userName || investor.userEmail} />
                                                            <AvatarFallback className="bg-gradient-to-br from-indigo-500 to-purple-600 text-white font-semibold">
                                                                {getInitials(investor)}
                                                            </AvatarFallback>
                                                        </Avatar>
                                                        <div className="flex flex-col">
                                                            <span className="font-semibold text-foreground">
                                                                {investor.userName || investor.userEmail}
                                                            </span>
                                                            {investor.userName && (
                                                                <span className="text-xs text-muted-foreground">
                                                                    {investor.userEmail}
                                                                </span>
                                                            )}
                                                        </div>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                                                        {investor.investmentBudget ? formatCurrency(investor.investmentBudget) : '—'}
                                                    </span>
                                                </TableCell>
                                                <TableCell>
                                                    {investor.preferredIndustries ? (
                                                        <div className="flex flex-wrap gap-1 max-w-[200px]">
                                                            {investor.preferredIndustries.split(',').slice(0, 2).map((industry, idx) => (
                                                                <Badge key={idx} variant="secondary" className="text-xs">
                                                                    {industry.trim()}
                                                                </Badge>
                                                            ))}
                                                            {investor.preferredIndustries.split(',').length > 2 && (
                                                                <Badge variant="outline" className="text-xs">
                                                                    +{investor.preferredIndustries.split(',').length - 2}
                                                                </Badge>
                                                            )}
                                                        </div>
                                                    ) : (
                                                        <span className="text-muted-foreground">—</span>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex items-center gap-1.5 text-muted-foreground">
                                                        <Clock className="h-3.5 w-3.5" />
                                                        <span className="text-sm">{formatDate(investor.verificationRequestedAt)}</span>
                                                    </div>
                                                </TableCell>
                                                <TableCell className="text-right pr-6">
                                                    <div className="flex items-center justify-end gap-1" onClick={(e) => e.stopPropagation()}>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all"
                                                            onClick={() => handleViewDetails(investor)}
                                                        >
                                                            <Eye className="h-4 w-4" />
                                                        </Button>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all"
                                                            onClick={() => setRejectDialog({ open: true, investor })}
                                                            disabled={processingId === investor.id}
                                                        >
                                                            <XCircle className="h-4 w-4" />
                                                        </Button>
                                                        <Button
                                                            size="sm"
                                                            className="bg-emerald-600 hover:bg-emerald-700 text-white gap-1.5"
                                                            onClick={() => handleApprove(investor)}
                                                            disabled={processingId === investor.id}
                                                        >
                                                            {processingId === investor.id ? (
                                                                <Loader2 className="h-3.5 w-3.5 animate-spin" />
                                                            ) : (
                                                                <CheckCircle className="h-3.5 w-3.5" />
                                                            )}
                                                            Approve
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

            {/* Investor Details Dialog - Matching UserDetailsModal design */}
            <Dialog open={detailsOpen} onOpenChange={setDetailsOpen}>
                <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 bg-white dark:bg-slate-950 border-none shadow-2xl [&>button]:hidden">
                    {/* Gradient Header */}
                    <div className="bg-gradient-to-r from-slate-800 via-emerald-900/50 to-teal-900/50 dark:from-slate-900 dark:via-emerald-950/80 dark:to-teal-950/80 p-6 shrink-0 border-b border-slate-700/50">
                        <DialogHeader className="flex-row items-center justify-between space-y-0">
                            <div>
                                <DialogTitle className="text-xl text-white">Investor Verification</DialogTitle>
                                <p className="text-sm text-white/70 mt-1">
                                    Review investor profile and approve or reject verification request.
                                </p>
                            </div>
                            <div className="flex items-center gap-2">
                                <Button variant="ghost" size="icon" onClick={() => setDetailsOpen(false)} className="text-white/70 hover:text-white hover:bg-white/10">
                                    <XCircle className="h-5 w-5" />
                                </Button>
                            </div>
                        </DialogHeader>
                    </div>

                    {selectedInvestor && (
                        <div className="flex flex-col flex-1 overflow-y-auto bg-white dark:bg-slate-950">
                            {/* Investor Profile Hero Section */}
                            <div className="p-6 pb-0">
                                <div className="relative overflow-hidden rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 shadow-sm">
                                    <div className="absolute top-0 right-0 p-6 opacity-[0.03] pointer-events-none">
                                        <UserCheck className="w-40 h-40" />
                                    </div>

                                    <div className="relative z-10 flex flex-col md:flex-row gap-6 items-start">
                                        {/* Avatar */}
                                        <div className="relative shrink-0">
                                            <div className="h-20 w-20 rounded-2xl overflow-hidden border-2 border-border shadow-sm bg-background">
                                                {selectedInvestor.userAvatar ? (
                                                    <img
                                                        src={selectedInvestor.userAvatar}
                                                        alt={selectedInvestor.userName || selectedInvestor.userEmail}
                                                        className="h-full w-full object-cover"
                                                    />
                                                ) : (
                                                    <div className="h-full w-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white font-bold text-2xl">
                                                        {getInitials(selectedInvestor)}
                                                    </div>
                                                )}
                                            </div>
                                        </div>

                                        {/* Info */}
                                        <div className="flex-1 min-w-0 space-y-3">
                                            <div>
                                                <h3 className="text-2xl font-bold tracking-tight text-foreground">
                                                    {selectedInvestor.userName || selectedInvestor.userEmail.split('@')[0]}
                                                </h3>
                                                <div className="flex items-center gap-2 mt-1 text-muted-foreground">
                                                    <Mail className="h-3.5 w-3.5" />
                                                    <span className="text-sm">{selectedInvestor.userEmail}</span>
                                                </div>
                                            </div>
                                            <div className="flex flex-wrap items-center gap-2">
                                                <Badge className="bg-emerald-500/10 text-emerald-600 border-emerald-500/30">INVESTOR</Badge>
                                                <Badge variant="outline" className="bg-yellow-500/10 text-yellow-600 border-yellow-500/30">
                                                    <Clock className="h-3 w-3 mr-1" />
                                                    Pending Verification
                                                </Badge>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Details Content */}
                            <div className="p-6 space-y-6">
                                {/* Investment Budget Card */}
                                <div className="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all duration-300 flex items-start gap-4">
                                    <div className="h-10 w-10 rounded-lg bg-emerald-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-emerald-600">
                                        <DollarSign className="h-5 w-5" />
                                    </div>
                                    <div className="flex-1">
                                        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-1">Investment Budget</p>
                                        <p className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">
                                            {selectedInvestor.investmentBudget ? formatCurrency(selectedInvestor.investmentBudget) : 'Not specified'}
                                        </p>
                                    </div>
                                </div>

                                {/* Bio */}
                                {selectedInvestor.bio && (
                                    <div className="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all duration-300 flex items-start gap-4">
                                        <div className="h-10 w-10 rounded-lg bg-blue-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-blue-600">
                                            <Briefcase className="h-5 w-5" />
                                        </div>
                                        <div className="flex-1">
                                            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-1">Bio</p>
                                            <p className="text-sm text-foreground/80 whitespace-pre-wrap leading-relaxed">
                                                {selectedInvestor.bio}
                                            </p>
                                        </div>
                                    </div>
                                )}

                                {/* Preferred Industries */}
                                {selectedInvestor.preferredIndustries && (
                                    <div className="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all duration-300 flex items-start gap-4">
                                        <div className="h-10 w-10 rounded-lg bg-purple-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-purple-600">
                                            <Briefcase className="h-5 w-5" />
                                        </div>
                                        <div className="flex-1">
                                            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-2">Preferred Industries</p>
                                            <div className="flex flex-wrap gap-2">
                                                {selectedInvestor.preferredIndustries.split(',').map((industry, idx) => (
                                                    <Badge key={idx} variant="secondary" className="px-3 py-1">
                                                        {industry.trim()}
                                                    </Badge>
                                                ))}
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {/* LinkedIn & Request Date Grid */}
                                <div className="grid grid-cols-2 gap-4">
                                    {selectedInvestor.linkedInUrl && (
                                        <div className="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all duration-300 flex items-start gap-4">
                                            <div className="h-10 w-10 rounded-lg bg-blue-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-blue-600">
                                                <ExternalLink className="h-5 w-5" />
                                            </div>
                                            <div className="flex-1">
                                                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-1">LinkedIn</p>
                                                <a
                                                    href={selectedInvestor.linkedInUrl.startsWith('http') ? selectedInvestor.linkedInUrl : `https://www.linkedin.com/in/${selectedInvestor.linkedInUrl}`}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="text-sm font-medium text-blue-500 hover:underline inline-flex items-center gap-1"
                                                >
                                                    View Profile
                                                    <ExternalLink className="h-3 w-3" />
                                                </a>
                                            </div>
                                        </div>
                                    )}

                                    <div className="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:shadow-md transition-all duration-300 flex items-start gap-4">
                                        <div className="h-10 w-10 rounded-lg bg-amber-500/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform text-amber-600">
                                            <Clock className="h-5 w-5" />
                                        </div>
                                        <div className="flex-1">
                                            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-1">Request Date</p>
                                            <p className="text-sm font-medium text-foreground">{formatDate(selectedInvestor.verificationRequestedAt)}</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Action Buttons */}
                                <div className="flex gap-3 pt-4 border-t border-slate-200 dark:border-slate-800">
                                    <Button
                                        variant="outline"
                                        className="flex-1 gap-2 h-11 border-red-200 text-red-600 hover:bg-red-50 dark:border-red-900 dark:hover:bg-red-900/20"
                                        onClick={() => {
                                            setRejectDialog({ open: true, investor: selectedInvestor })
                                        }}
                                        disabled={processingId === selectedInvestor.id}
                                    >
                                        <XCircle className="h-4 w-4" />
                                        Reject Request
                                    </Button>
                                    <Button
                                        className="flex-1 gap-2 h-11 bg-emerald-600 hover:bg-emerald-700 text-white"
                                        onClick={() => handleApprove(selectedInvestor)}
                                        disabled={processingId === selectedInvestor.id}
                                    >
                                        {processingId === selectedInvestor.id ? (
                                            <Loader2 className="h-4 w-4 animate-spin" />
                                        ) : (
                                            <CheckCircle className="h-4 w-4" />
                                        )}
                                        Approve for Payment
                                    </Button>
                                </div>
                            </div>
                        </div>
                    )}
                </DialogContent>
            </Dialog>

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
                            Investor: {rejectDialog.investor?.userName || rejectDialog.investor?.userEmail}
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

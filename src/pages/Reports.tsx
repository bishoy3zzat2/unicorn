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
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "../components/ui/alert-dialog"
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
    User,
    Building2,
    Clock,
    Eye,
    CheckCircle2,
    XCircle,
    Flag,
    AlertCircle,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    Gavel,
    MessageSquare,
    Trash2,
} from 'lucide-react'
import { KPICard } from '../components/dashboard/KPICard'
import { getAllReports, getReportStats, deleteReport, Report } from '../lib/api'
import { formatTimeAgo } from '../lib/utils'
import { ReportDetailsDialog } from '../components/dashboard/ReportDetailsDialog'
import { ReportResolutionDialog } from '../components/dashboard/ReportResolutionDialog'
import { getChatReports, ChatReportData } from '../api/adminChatApi'

// Report reason labels
const REPORT_REASON_LABELS: Record<string, string> = {
    SPAM: 'Spam',
    HARASSMENT: 'Harassment',
    INAPPROPRIATE_CONTENT: 'Inappropriate Content',
    FRAUD: 'Fraud',
    DUPLICATE: 'Duplicate',
    COPYRIGHT: 'Copyright Infringement',
    IMPERSONATION: 'Impersonation',
    ADULT_CONTENT: 'Adult Content',
    VIOLENCE: 'Violence or Threats',
    HATE_SPEECH: 'Hate Speech',
    MISINFORMATION: 'Misinformation',
    OTHER: 'Other',
}

export function Reports() {
    const [reports, setReports] = useState<Report[]>([])
    const [stats, setStats] = useState({
        total: 0,
        pending: 0,
        underReview: 0,
        resolved: 0,
        rejected: 0
    })
    const [loading, setLoading] = useState(true)
    const [searchQuery, setSearchQuery] = useState('')
    const [statusFilter, setStatusFilter] = useState<string>('ALL')
    const [entityTypeFilter, setEntityTypeFilter] = useState<string>('ALL')

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [pageSize, setPageSize] = useState(20)

    // Selected report for details
    const [selectedReportId, setSelectedReportId] = useState<string | null>(null)

    // Quick Resolve State
    const [quickResolveReport, setQuickResolveReport] = useState<Report | null>(null)
    const [resolveDialogOpen, setResolveDialogOpen] = useState(false)

    // Delete State
    const [deleteReportId, setDeleteReportId] = useState<string | null>(null)
    const [deleteLoading, setDeleteLoading] = useState(false)

    // Load stats
    useEffect(() => {
        loadStats()
    }, [])

    // Load reports when filters change
    useEffect(() => {
        loadReports()
    }, [currentPage, pageSize, statusFilter, entityTypeFilter])

    const loadStats = async () => {
        try {
            const data = await getReportStats()
            setStats(data)
        } catch (error) {
            console.error('Failed to load stats:', error)
        }
    }

    const loadReports = async () => {
        setLoading(true)
        try {
            // Handle chat reports separately
            if (entityTypeFilter === 'CHAT_MESSAGE') {
                const status = statusFilter === 'ALL' ? undefined : statusFilter
                const chatReportsData = await getChatReports(
                    Number(currentPage),
                    Number(pageSize),
                    status
                )

                // Convert chat reports to Report format for display
                const convertedReports: Report[] = chatReportsData.content.map((cr: ChatReportData) => ({
                    id: cr.id,
                    reporterId: cr.reporterId,
                    reportedEntityId: cr.messageId,
                    reportedEntityType: 'CHAT_MESSAGE',
                    reason: cr.reason,
                    description: cr.messageContent, // Use messageContent as description
                    status: cr.status === 'ACTION_TAKEN' ? 'RESOLVED' : cr.status === 'REVIEWED' ? 'UNDER_REVIEW' : cr.status, // Map status
                    createdAt: cr.createdAt,
                    reviewedAt: cr.reviewedAt,
                    reviewedBy: cr.reviewedByName || undefined,
                    resolution: cr.status === 'ACTION_TAKEN' ? 'Message deleted' : cr.status === 'DISMISSED' ? 'Dismissed' : undefined,
                    notifyReporter: false,
                    reporterNotified: false,
                    updatedAt: cr.reviewedAt || cr.createdAt
                }))

                setReports(convertedReports)
                setTotalPages(chatReportsData.totalPages)
                setTotalElements(chatReportsData.totalElements)
            } else {
                // Regular user/startup reports
                const status = statusFilter === 'ALL' ? undefined : statusFilter
                const type = entityTypeFilter === 'ALL' ? undefined : entityTypeFilter
                const data = await getAllReports(currentPage, pageSize, status, type)

                setReports(data.content)
                setTotalPages(data.totalPages)
                setTotalElements(data.totalElements)
            }
        } catch (error: any) {
            console.error('Failed to load reports:', error)
        } finally {
            setLoading(false)
        }
    }

    const handleRefresh = () => {
        loadStats()
        loadReports()
    }

    const getStatusBadge = (status: string) => {
        const styles = {
            PENDING: "bg-yellow-500/15 text-yellow-700 dark:text-yellow-400 hover:bg-yellow-500/25 border-yellow-200 dark:border-yellow-900/50",
            UNDER_REVIEW: "bg-blue-500/15 text-blue-700 dark:text-blue-400 hover:bg-blue-500/25 border-blue-200 dark:border-blue-900/50",
            RESOLVED: "bg-green-500/15 text-green-700 dark:text-green-400 hover:bg-green-500/25 border-green-200 dark:border-green-900/50",
            REJECTED: "bg-red-500/15 text-red-700 dark:text-red-400 hover:bg-red-500/25 border-red-200 dark:border-red-900/50",
            DISMISSED: "bg-muted text-muted-foreground hover:bg-muted/80 border-border",
        }

        const icons = {
            PENDING: Clock,
            UNDER_REVIEW: Eye,
            RESOLVED: CheckCircle2,
            REJECTED: XCircle,
            DISMISSED: XCircle,
        }

        const Style = styles[status as keyof typeof styles] || "bg-muted text-muted-foreground"
        const Icon = icons[status as keyof typeof icons] || AlertCircle

        return (
            <Badge variant="outline" className={`gap-1.5 py-1 px-2.5 transition-colors duration-200 ${Style}`}>
                <Icon className="h-3.5 w-3.5" />
                <span className="font-medium capitalization">{status.replace('_', ' ').toLowerCase()}</span>
            </Badge>
        )
    }

    const getEntityTypeIcon = (type: string) => {
        if (type === 'USER') {
            return (
                <div className="h-8 w-8 rounded-full bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 flex items-center justify-center ring-2 ring-white dark:ring-background shadow-sm">
                    <User className="h-4 w-4" />
                </div>
            )
        } else if (type === 'CHAT_MESSAGE') {
            return (
                <div className="h-8 w-8 rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 flex items-center justify-center ring-2 ring-white dark:ring-background shadow-sm">
                    <MessageSquare className="h-4 w-4" />
                </div>
            )
        } else {
            return (
                <div className="h-8 w-8 rounded-full bg-orange-100 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 flex items-center justify-center ring-2 ring-white dark:ring-background shadow-sm">
                    <Building2 className="h-4 w-4" />
                </div>
            )
        }
    }

    // Client-side search filtering (since API search isn't implemented yet)
    const filteredReports = reports.filter(report => {
        if (!searchQuery) return true
        const query = searchQuery.toLowerCase()
        return (
            report.id.toLowerCase().includes(query) ||
            report.reporterId.toLowerCase().includes(query) ||
            report.reportedEntityId.toLowerCase().includes(query) ||
            report.description.toLowerCase().includes(query)
        )
    })

    return (
        <div className="space-y-6 transition-colors duration-300">


            {/* Stats Overview */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
                <KPICard
                    title="Total Reports"
                    value={stats.total.toString()}
                    icon={Flag}
                />
                <KPICard
                    title="Pending"
                    value={stats.pending.toString()}
                    icon={Clock}
                    iconColor="text-yellow-600 dark:text-yellow-400"
                    trend={stats.pending > 0 ? 'Requires Action' : undefined}
                />
                <KPICard
                    title="Under Review"
                    value={stats.underReview.toString()}
                    icon={Eye}
                    iconColor="text-blue-600 dark:text-blue-400"
                />
                <KPICard
                    title="Resolved"
                    value={stats.resolved.toString()}
                    icon={CheckCircle2}
                    iconColor="text-green-600 dark:text-green-400"
                />
                <KPICard
                    title="Rejected"
                    value={stats.rejected.toString()}
                    icon={XCircle}
                    iconColor="text-red-600 dark:text-red-400"
                />
            </div>

            {/* Main Content Area */}
            <div className="space-y-4">
                {/* Advanced Toolbar */}
                <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800">
                    {/* Status Tabs (Custom Implementation) */}
                    <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-lg overflow-x-auto max-w-full no-scrollbar">
                        {['ALL', 'PENDING', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED'].map((status) => (
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
                                {status === 'ALL' ? 'All' : status.replace('_', ' ')}
                            </button>
                        ))}
                    </div>

                    <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                        <Select value={entityTypeFilter} onValueChange={(value) => {
                            setEntityTypeFilter(value)
                            setCurrentPage(0)
                        }}>
                            <SelectTrigger className="w-full sm:w-[180px] bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                                <SelectValue placeholder="Entity Type" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">All Entities</SelectItem>
                                <SelectItem value="USER">User Reports</SelectItem>
                                <SelectItem value="STARTUP">Startup Reports</SelectItem>
                                <SelectItem value="CHAT_MESSAGE">Chat Reports</SelectItem>
                            </SelectContent>
                        </Select>

                        <div className="relative flex-1 sm:w-64">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search by ID or reason..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
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
                    </div>
                </div>

                {/* Modern Table */}
                <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                    {loading && reports.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                            <Loader2 className="h-10 w-10 animate-spin mb-4" />
                            <p>Loading reports...</p>
                        </div>
                    ) : filteredReports.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-center">
                            <div className="h-16 w-16 bg-muted rounded-full flex items-center justify-center mb-4">
                                <Flag className="h-8 w-8 text-muted-foreground" />
                            </div>
                            <h3 className="text-lg font-semibold text-foreground">No reports found</h3>
                            <p className="text-muted-foreground max-w-sm mx-auto mt-1">
                                No reports match your current filters. Try adjusting (or clearing) them.
                            </p>
                            <Button
                                variant="outline"
                                className="mt-6"
                                onClick={() => {
                                    setStatusFilter('ALL')
                                    setEntityTypeFilter('ALL')
                                    setSearchQuery('')
                                }}
                            >
                                Clear All Filters
                            </Button>
                        </div>
                    ) : (
                        <>
                            <div className="overflow-x-auto">
                                <Table>
                                    <TableHeader className="bg-slate-50 dark:bg-slate-800/50">
                                        <TableRow className="hover:bg-transparent border-b-border">
                                            <TableHead className="font-semibold text-muted-foreground pl-6">Entity</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Reason</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Status</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Submitted</TableHead>
                                            <TableHead className="text-right pr-6 font-semibold text-muted-foreground">Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {filteredReports.map((report) => (
                                            <TableRow
                                                key={report.id}
                                                className="group cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800/50 transition-colors border-b-border"
                                                onClick={() => setSelectedReportId(report.id)}
                                            >
                                                <TableCell className="pl-6 py-4">
                                                    <div className="flex items-center gap-3">
                                                        {getEntityTypeIcon(report.reportedEntityType)}
                                                        <div className="flex flex-col">
                                                            <span className="font-semibold text-foreground">
                                                                {report.reportedEntityType}
                                                            </span>
                                                            <span className="text-xs text-muted-foreground font-mono">
                                                                {report.reportedEntityId.substring(0, 8)}...
                                                            </span>
                                                        </div>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex flex-col gap-1">
                                                        <span className="font-medium text-foreground/90">
                                                            {REPORT_REASON_LABELS[report.reason] || report.reason}
                                                        </span>
                                                        <span className="text-xs text-muted-foreground line-clamp-1 max-w-[200px]">
                                                            {report.description}
                                                        </span>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    {getStatusBadge(report.status)}
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex items-center gap-1.5 text-muted-foreground">
                                                        <Clock className="h-3.5 w-3.5" />
                                                        <span className="text-sm">{formatTimeAgo(report.createdAt)}</span>
                                                    </div>
                                                </TableCell>
                                                <TableCell className="text-right pr-6">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-8 w-8 text-muted-foreground hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 transition-all"
                                                        onClick={(e) => {
                                                            e.stopPropagation()
                                                            setSelectedReportId(report.id)
                                                        }}
                                                        title="View Details"
                                                    >
                                                        <Eye className="h-4 w-4" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-8 w-8 text-muted-foreground hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-900/20 transition-all"
                                                        onClick={(e) => {
                                                            e.stopPropagation()
                                                            setDeleteReportId(report.id)
                                                        }}
                                                        title="Delete Report"
                                                    >
                                                        <Trash2 className="h-4 w-4" />
                                                    </Button>
                                                    {(report.status !== 'RESOLVED' && report.status !== 'REJECTED') && (
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-900/20 transition-all"
                                                            title="Quick Resolve"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                setQuickResolveReport(report)
                                                                setResolveDialogOpen(true)
                                                            }}
                                                        >
                                                            <Gavel className="h-4 w-4" />
                                                        </Button>
                                                    )}
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
                                                {[10, 20, 50, 100, 200, 500].map((size) => (
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

            {/* Report Details Dialog */}
            {selectedReportId && (
                <ReportDetailsDialog
                    reportId={selectedReportId}
                    open={!!selectedReportId}
                    onOpenChange={(open) => !open && setSelectedReportId(null)}
                    onReportUpdated={handleRefresh}
                />
            )}

            {/* Quick Resolve Dialog */}
            {quickResolveReport && (
                <ReportResolutionDialog
                    open={resolveDialogOpen}
                    onOpenChange={setResolveDialogOpen}
                    report={quickResolveReport}
                    onSuccess={() => {
                        setQuickResolveReport(null)
                        handleRefresh()
                        loadStats()
                    }}
                />
            )}

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={!!deleteReportId} onOpenChange={(open) => !open && setDeleteReportId(null)}>
                <AlertDialogContent className="max-w-md bg-white dark:bg-slate-950 border-rose-200 dark:border-rose-800/50">
                    <AlertDialogHeader>
                        <div className="mx-auto h-14 w-14 rounded-2xl bg-gradient-to-br from-rose-100 to-red-100 dark:from-rose-900/30 dark:to-red-900/30 shadow-lg flex items-center justify-center mb-3">
                            <Trash2 className="h-7 w-7 text-rose-600 dark:text-rose-400" />
                        </div>
                        <AlertDialogTitle className="text-xl font-bold text-center text-foreground">
                            Delete Report?
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-center text-muted-foreground">
                            This action cannot be undone. The report will be permanently deleted from the system.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter className="gap-3">
                        <AlertDialogCancel
                            className="flex-1 border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800"
                            disabled={deleteLoading}
                        >
                            Cancel
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={async (e: React.MouseEvent<HTMLButtonElement>) => {
                                e.preventDefault()
                                if (!deleteReportId) return

                                setDeleteLoading(true)
                                try {
                                    await deleteReport(deleteReportId)
                                    setDeleteReportId(null)
                                    handleRefresh()
                                    loadStats()
                                } catch (error: any) {
                                    console.error('Failed to delete report:', error)
                                } finally {
                                    setDeleteLoading(false)
                                }
                            }}
                            disabled={deleteLoading}
                            className="flex-1 bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-700 hover:to-red-700 text-white shadow-lg shadow-rose-500/25 border-0"
                        >
                            {deleteLoading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Deleting...
                                </>
                            ) : (
                                'Delete Report'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

        </div>
    )
}

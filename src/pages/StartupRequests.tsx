import { useState, useEffect } from 'react'
import {
    Loader2,
    RefreshCcw,
    AlertCircle,
    Building2,
    Globe,
    UserCog,
    Eye,
    Rocket,
    CheckCircle2,
    Clock,
    XCircle,

    Search,
    Download,
    UserPlus,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    Calendar,
} from "lucide-react"
import { toast } from "sonner"

import { Button } from "../components/ui/button"
import { Input } from "../components/ui/input"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../components/ui/select"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "../components/ui/dialog"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "../components/ui/table"
import { Alert, AlertDescription } from "../components/ui/alert"
import { Badge } from "../components/ui/badge"
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from "../components/ui/card"

import { Checkbox } from "../components/ui/checkbox"
import { Label } from "../components/ui/label"
import { RadioGroup, RadioGroupItem } from "../components/ui/radio-group"

import { StartupsFilters, StartupFilterState } from '../components/dashboard/StartupsFilters'
import { CreateStartupDialog } from '../components/dashboard/CreateStartupDialog'
import { StartupDetailsDialog } from '../components/dashboard/StartupDetailsDialog'
import { TransferStartupDialog } from '../components/dashboard/TransferStartupDialog'
import {
    fetchAllStartups,
    fetchStartupStats,
    getStartupById
} from '../lib/api'
import { Startup, StartupStats } from '../types'
import { cn, formatDate, formatTimeAgo, formatNumber } from '../lib/utils'
import { KPICard } from '../components/dashboard/KPICard'

import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "../components/ui/dropdown-menu"
import { MoreVertical, Shield, AlertTriangle, Trash2 } from "lucide-react"
import { WarnStartupDialog, StartupStatusDialog, DeleteStartupDialog } from '../components/dashboard/StartupActionDialogs'
import { useAuth } from '../contexts/AuthContext'

const EXPORTABLE_COLUMNS = [
    { id: 'id', label: 'ID' },
    { id: 'name', label: 'Name' },
    { id: 'industry', label: 'Industry' },
    { id: 'stage', label: 'Stage' },
    { id: 'fundingGoal', label: 'Funding Goal' },
    { id: 'raisedAmount', label: 'Raised Amount' },
    { id: 'status', label: 'Status' },
    { id: 'ownerEmail', label: 'Owner Email' },
    { id: 'createdAt', label: 'Created At' },
    { id: 'websiteUrl', label: 'Website URL' },
]

export function StartupRequests() {
    const [startups, setStartups] = useState<Startup[]>([])
    const [stats, setStats] = useState<StartupStats | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [page, setPage] = useState(0)
    const [pageSize, setPageSize] = useState(20)
    const [totalPages, setTotalPages] = useState(0)

    // Action Dialog States
    const [actionStartup, setActionStartup] = useState<Startup | null>(null)
    const [warnDialogOpen, setWarnDialogOpen] = useState(false)
    const [statusDialogOpen, setStatusDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

    const { user } = useAuth()
    const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'

    // Filter State
    const [filters, setFilters] = useState<StartupFilterState>({})
    const [appliedFilters, setAppliedFilters] = useState<StartupFilterState>({})

    // Transfer ownership state
    const [transferDialog, setTransferDialog] = useState<{ open: boolean; startup: Startup | null }>({
        open: false,
        startup: null
    })
    const [viewDialog, setViewDialog] = useState<{ open: boolean; startup: Startup | null }>({
        open: false,
        startup: null
    })
    const [createDialogOpen, setCreateDialogOpen] = useState(false)

    // Export State
    const [exportDialogOpen, setExportDialogOpen] = useState(false)
    const [exportScope, setExportScope] = useState<'current' | 'all'>('current')
    const [selectedColumns, setSelectedColumns] = useState<string[]>(
        EXPORTABLE_COLUMNS.map(col => col.id)
    )
    const [isExporting, setIsExporting] = useState(false)



    // Helper functions for Export Column Selection
    const handleSelectAllColumns = (checked: boolean) => {
        if (checked) {
            setSelectedColumns(EXPORTABLE_COLUMNS.map(col => col.id))
        } else {
            setSelectedColumns([])
        }
    }

    const handleColumnToggle = (columnId: string, checked: boolean) => {
        if (checked) {
            setSelectedColumns(prev => [...prev, columnId])
        } else {
            setSelectedColumns(prev => prev.filter(id => id !== columnId))
        }
    }

    // Quick Search State
    const [quickSearch, setQuickSearch] = useState('')

    useEffect(() => {
        loadData()
    }, [page, pageSize, appliedFilters])



    // Update filters when quick search changes changes (debounced)
    useEffect(() => {
        const timer = setTimeout(() => {
            if (quickSearch) {
                setFilters(prev => ({ ...prev, ownerEmail: quickSearch }))
                setAppliedFilters(prev => ({ ...prev, ownerEmail: quickSearch }))
            } else if (appliedFilters.ownerEmail) {
                const newFilters = { ...filters }
                delete newFilters.ownerEmail
                setFilters(newFilters)
                setAppliedFilters(newFilters)
            }
        }, 500)
        return () => clearTimeout(timer)
    }, [quickSearch])


    async function loadData() {
        try {
            setLoading(true)
            setError(null)
            const [startupsData, statsData] = await Promise.all([
                fetchAllStartups(page, pageSize, appliedFilters),
                fetchStartupStats()
            ])
            setStartups(startupsData.content)
            setTotalPages(startupsData.totalPages)
            setStats(statsData)
        } catch (err) {
            console.error('Failed to fetch data:', err)
            setError(err instanceof Error ? err.message : 'Failed to load data')
        } finally {
            setLoading(false)
        }
    }

    const handleApplyFilters = () => {
        setAppliedFilters({ ...filters })
        setPage(0)
    }

    const handleClearFilters = () => {
        setFilters({})
        setAppliedFilters({})
        setPage(0)
        setQuickSearch('')
    }

    const handleActionComplete = () => {
        // Refresh the list after moderation action
        loadData()
    }

    // Action Handlers
    const handleWarn = (e: React.MouseEvent, startup: Startup) => {
        e.stopPropagation()
        setActionStartup(startup)
        setWarnDialogOpen(true)
    }

    const handleStatusChange = (e: React.MouseEvent, startup: Startup) => {
        e.stopPropagation()
        setActionStartup(startup)
        setStatusDialogOpen(true)
    }

    const handleDelete = (e: React.MouseEvent, startup: Startup) => {
        e.stopPropagation()
        setActionStartup(startup)
        setDeleteDialogOpen(true)
    }

    // Export Logic
    const downloadCSV = (data: Startup[], filename: string) => {
        const activeColumns = EXPORTABLE_COLUMNS.filter(col => selectedColumns.includes(col.id))
        const headers = activeColumns.map(col => col.label)

        const csvContent = [
            headers.join(','),
            ...data.map(startup => {
                const row = activeColumns.map(col => {
                    const value = startup[col.id as keyof Startup]
                    return value === undefined || value === null ? '' : value
                })

                return row.map(field => {
                    const stringField = String(field)
                    if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
                        return `"${stringField.replace(/"/g, '""')}"`
                    }
                    return stringField
                }).join(',')
            })
        ].join('\n')

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        if (link.download !== undefined) {
            const url = URL.createObjectURL(blob)
            link.setAttribute('href', url)
            link.setAttribute('download', filename)
            link.style.visibility = 'hidden'
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
        }
    }

    const performExport = async () => {
        try {
            setIsExporting(true)
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-')

            if (exportScope === 'current') {
                downloadCSV(startups, `startups_current_page_${timestamp}.csv`)
                toast.success('Exported current page successfully')
            } else {
                toast.info('Fetching all matching records...')
                const response = await fetchAllStartups(0, 10000, appliedFilters)
                downloadCSV(response.content, `startups_all_filtered_${timestamp}.csv`)
                toast.success(`Exported ${response.content.length} records successfully`)
            }
            setExportDialogOpen(false)
        } catch (error) {
            console.error('Export failed:', error)
            toast.error('Failed to export data')
        } finally {
            setIsExporting(false)
        }
    }

    function getStatusBadge(status: string) {
        const styles: Record<string, string> = {
            APPROVED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
            ACTIVE: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
            PENDING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
            REJECTED: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
            ARCHIVED: 'bg-gray-100 text-gray-700 dark:bg-gray-900/30 dark:text-gray-400'
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[status] || styles.ARCHIVED}`}>
                {status}
            </span>
        )
    }

    function getStageBadge(stage: string) {
        const styles: Record<string, string> = {
            IDEA: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
            MVP: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
            GROWTH: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[stage] || styles.IDEA}`}>
                {stage}
            </span>
        )
    }

    if (loading && startups.length === 0) {
        return (
            <div className="flex items-center justify-center h-64">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading startups...</span>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Startups</h1>
                    <p className="text-muted-foreground mt-2">
                        Manage startups and ownership transfers
                    </p>
                </div>
            </div>

            {/* Stats Cards */}
            {stats && (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    <KPICard
                        title="Total Startups"
                        value={formatNumber(stats.total)}
                        icon={Rocket}
                        trend={0}
                        iconColor="text-blue-500"
                    />
                    <KPICard
                        title="Active Startups"
                        value={formatNumber(stats.active)}
                        icon={CheckCircle2}
                        trend={0}
                        iconColor="text-emerald-500"
                    />
                    <KPICard
                        title="Pending Review"
                        value={formatNumber(stats.pending)}
                        icon={Clock}
                        trend={0}
                        iconColor="text-yellow-500"
                    />
                    <KPICard
                        title="Rejected"
                        value={formatNumber(stats.rejected)}
                        icon={XCircle}
                        trend={0}
                        iconColor="text-red-500"
                    />
                </div>
            )}

            {/* Filter Component */}
            <StartupsFilters
                filters={filters}
                onFiltersChange={setFilters}
                onApply={handleApplyFilters}
                onClear={handleClearFilters}
            />

            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            {/* Startups Table */}
            <Card className="overflow-hidden">
                <CardHeader className="bg-white/50 dark:bg-slate-900/50 border-b border-border/50 pb-4">
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="flex items-center gap-3">
                            <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
                                <Building2 className="h-5 w-5 text-primary" />
                            </div>
                            <div>
                                <CardTitle>All Startups</CardTitle>
                                <div className="flex items-center gap-2">
                                    {loading && <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />}
                                    <span className="text-xs text-muted-foreground font-medium">
                                        {startups.length} {startups.length === 1 ? 'startup' : 'startups'} total
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-2">
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Search by email..."
                                    value={quickSearch}
                                    onChange={(e) => setQuickSearch(e.target.value)}
                                    className="pl-9 w-full sm:w-[250px] bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
                                />
                            </div>

                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    size="icon"
                                    onClick={loadData}
                                    disabled={loading}
                                    className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 hover:bg-slate-50"
                                    title="Refresh List"
                                >
                                    <RefreshCcw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                                </Button>

                                <Button
                                    variant="outline"
                                    onClick={() => setExportDialogOpen(true)}
                                    className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 hover:bg-slate-50 gap-2"
                                >
                                    <Download className="h-4 w-4" />
                                    <span className="hidden sm:inline">Export</span>
                                </Button>

                                <Button
                                    onClick={() => setCreateDialogOpen(true)}
                                    className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white shadow-lg shadow-indigo-500/20 gap-2"
                                >
                                    <UserPlus className="h-4 w-4" />
                                    <span className="hidden sm:inline">New Startup</span>
                                </Button>
                            </div>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    {startups.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-12">
                            <Rocket className="h-12 w-12 text-muted-foreground mb-4" />
                            <p className="text-muted-foreground">No startups found</p>
                        </div>
                    ) : (
                        <div className="rounded-md border mt-4">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Startup & Owner</TableHead>
                                        <TableHead>Industry</TableHead>
                                        <TableHead>Stage</TableHead>
                                        <TableHead>Funding Goal</TableHead>
                                        <TableHead>Status</TableHead>
                                        <TableHead>Created</TableHead>
                                        <TableHead className="text-right">Actions</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {startups.map((startup) => (
                                        <TableRow
                                            key={startup.id}
                                            className="group cursor-pointer hover:bg-muted/50 transition-colors"
                                            onClick={() => setViewDialog({ open: true, startup })}
                                        >
                                            <TableCell>
                                                <div className="flex items-center gap-3">
                                                    {startup.logoUrl ? (
                                                        <img
                                                            src={startup.logoUrl}
                                                            alt={startup.name}
                                                            className="h-10 w-10 rounded-full object-cover border"
                                                        />
                                                    ) : (
                                                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center border">
                                                            <Building2 className="h-5 w-5 text-primary" />
                                                        </div>
                                                    )}
                                                    <div>
                                                        <div className="flex items-center gap-2">
                                                            <p className="font-semibold text-sm">{startup.name}</p>
                                                            {(startup.warningCount || 0) > 0 && (
                                                                <div className="relative group">
                                                                    <AlertTriangle className="h-4 w-4 text-amber-500 fill-amber-100 dark:fill-amber-900/30" />
                                                                    <div className="absolute left-0 bottom-full mb-2 hidden group-hover:block w-32 p-2 bg-popover text-popover-foreground text-xs rounded shadow-lg border z-50">
                                                                        {startup.warningCount} Warnings Issued
                                                                    </div>
                                                                </div>
                                                            )}
                                                        </div>
                                                        <div className="flex items-center gap-1 text-xs text-muted-foreground">
                                                            <UserCog className="h-3 w-3" />
                                                            {startup.ownerEmail}
                                                        </div>
                                                        {filters.memberEmail && (
                                                            <div className="flex items-center gap-1 text-xs text-blue-500">
                                                                <UserCog className="h-3 w-3" />
                                                                Member: {filters.memberEmail}
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className={cn("font-normal", getIndustryStyle(startup.industry))}>
                                                    {startup.industry || 'General'}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                {getStageBadge(startup.stage)}
                                            </TableCell>
                                            <TableCell>
                                                <div className="font-medium">
                                                    {(startup.fundingGoal || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                {getStatusBadge(startup.status)}
                                            </TableCell>
                                            <TableCell className="text-muted-foreground text-xs">
                                                <div className="flex items-center gap-1.5" title={formatDate(startup.createdAt)}>
                                                    <Calendar className="h-3 w-3" />
                                                    <span>Created {formatTimeAgo(startup.createdAt)}</span>
                                                </div>
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <div className="flex items-center justify-end gap-1">
                                                    {startup.websiteUrl && (
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 hover:bg-muted"
                                                            asChild
                                                            title="Visit Website"
                                                        >
                                                            <a
                                                                href={startup.websiteUrl}
                                                                target="_blank"
                                                                rel="noopener noreferrer"
                                                                onClick={(e) => e.stopPropagation()}
                                                            >
                                                                <Globe className="h-4 w-4 text-indigo-500" />
                                                            </a>
                                                        </Button>
                                                    )}
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-8 w-8 hover:bg-muted"
                                                        onClick={(e) => {
                                                            e.stopPropagation()
                                                            setViewDialog({ open: true, startup })
                                                        }}
                                                        title="View Details"
                                                    >
                                                        <Eye className="h-4 w-4 text-blue-500" />
                                                    </Button>
                                                    <DropdownMenu>
                                                        <DropdownMenuTrigger asChild>
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                className="h-8 w-8 p-0"
                                                                onClick={(e) => e.stopPropagation()}
                                                            >
                                                                <span className="sr-only">Open menu</span>
                                                                <MoreVertical className="h-4 w-4" />
                                                            </Button>
                                                        </DropdownMenuTrigger>
                                                        <DropdownMenuContent align="end">
                                                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                                                            <DropdownMenuItem onClick={(e) => {
                                                                e.stopPropagation()
                                                                setTransferDialog({ open: true, startup })
                                                            }}>
                                                                <UserCog className="mr-2 h-4 w-4" />
                                                                Transfer Ownership
                                                            </DropdownMenuItem>


                                                            <DropdownMenuSeparator />
                                                            <DropdownMenuLabel>Moderation</DropdownMenuLabel>
                                                            <DropdownMenuItem onClick={(e) => handleStatusChange(e, startup)} disabled={!isAdmin}>
                                                                <Shield className="mr-2 h-4 w-4 text-blue-500" />
                                                                Change Status
                                                            </DropdownMenuItem>
                                                            <DropdownMenuItem onClick={(e) => handleWarn(e, startup)} disabled={!isAdmin}>
                                                                <AlertTriangle className="mr-2 h-4 w-4 text-yellow-500" />
                                                                Issue Warning
                                                            </DropdownMenuItem>
                                                            <DropdownMenuSeparator />
                                                            <DropdownMenuItem
                                                                onClick={(e) => handleDelete(e, startup)}
                                                                disabled={!isAdmin}
                                                                className="text-red-600 focus:text-red-600"
                                                            >
                                                                <Trash2 className="mr-2 h-4 w-4" />
                                                                Delete Permanently
                                                            </DropdownMenuItem>

                                                        </DropdownMenuContent>
                                                    </DropdownMenu>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    )}

                    {/* Pagination */}
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 px-2 py-4">
                        <div className="text-sm text-muted-foreground">
                            Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, stats?.total || 0)} of {stats?.total || 0} entries
                        </div>
                        <div className="flex flex-wrap items-center gap-4 lg:gap-6">
                            <div className="flex items-center space-x-2">
                                <p className="text-sm font-medium">Rows per page</p>
                                <Select
                                    value={`${pageSize}`}
                                    onValueChange={(value) => {
                                        setPageSize(Number(value))
                                        setPage(0)
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
                                    className="hidden h-8 w-8 p-0 lg:flex"
                                    onClick={() => setPage(0)}
                                    disabled={page === 0}
                                >
                                    <span className="sr-only">Go to first page</span>
                                    <ChevronsLeft className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="outline"
                                    className="h-8 w-8 p-0"
                                    onClick={() => setPage(p => Math.max(0, p - 1))}
                                    disabled={page === 0}
                                >
                                    <span className="sr-only">Go to previous page</span>
                                    <ChevronLeft className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="outline"
                                    className="h-8 w-8 p-0"
                                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                    disabled={page >= totalPages - 1}
                                >
                                    <span className="sr-only">Go to next page</span>
                                    <ChevronRight className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="outline"
                                    className="hidden h-8 w-8 p-0 lg:flex"
                                    onClick={() => setPage(totalPages - 1)}
                                    disabled={page >= totalPages - 1}
                                >
                                    <span className="sr-only">Go to last page</span>
                                    <ChevronsRight className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    </div>
                </CardContent >
            </Card >

            {/* View Startup Dialog */}

            < StartupDetailsDialog
                open={viewDialog.open}
                onOpenChange={(open) => setViewDialog(prev => ({ ...prev, open }))}
                startup={viewDialog.startup}
                onTransfer={(startup) => {
                    setTransferDialog({ open: true, startup });
                    setViewDialog(prev => ({ ...prev, open: false }));
                }}
                onActionComplete={async () => {
                    // 1. Refresh the main list
                    loadData();
                    // 2. Refresh the currently viewed startup details
                    if (viewDialog.startup) {
                        try {
                            const updatedStartup = await getStartupById(viewDialog.startup.id);
                            setViewDialog(prev => ({ ...prev, startup: updatedStartup }));
                        } catch (err) {
                            console.error("Failed to refresh startup details", err);
                        }
                    }
                }}
            />

            {/* Transfer Ownership Dialog */}
            <TransferStartupDialog
                open={transferDialog.open}
                onOpenChange={(open) => {
                    if (!open) setTransferDialog({ open: false, startup: null })
                }}
                startup={transferDialog.startup}
                onSuccess={loadData}
            />

            {/* Export Dialog */}
            <Dialog open={exportDialogOpen} onOpenChange={setExportDialogOpen}>
                <DialogContent className="sm:max-w-[500px] max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 border-none shadow-2xl bg-white dark:bg-slate-950">
                    {/* Gradient Header */}
                    <div className="bg-gradient-to-r from-slate-800 via-emerald-900/50 to-teal-900/50 dark:from-slate-900 dark:via-emerald-950/80 dark:to-teal-950/80 p-6 shrink-0 border-b border-slate-700/50">
                        <DialogHeader className="space-y-2">
                            <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white">
                                <div className="h-12 w-12 rounded-2xl bg-white/20 backdrop-blur-sm shadow-lg flex items-center justify-center">
                                    <Download className="h-6 w-6 text-white" />
                                </div>
                                Export Startups
                            </DialogTitle>
                            <DialogDescription className="text-white/80">
                                Choose the scope and columns to include in your CSV export.
                            </DialogDescription>
                        </DialogHeader>
                    </div>

                    <div className="overflow-y-auto flex-1 p-6 space-y-6">
                        {/* Scope Selection */}
                        <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-900/10 dark:to-indigo-900/10 border border-blue-100 dark:border-blue-800/30">
                            <h4 className="text-sm font-bold uppercase tracking-wider text-blue-700 dark:text-blue-400 flex items-center gap-2">
                                <Rocket className="h-4 w-4" />
                                Export Scope
                            </h4>
                            <RadioGroup
                                value={exportScope}
                                onValueChange={(v) => setExportScope(v as 'current' | 'all')}
                                className="grid grid-cols-2 gap-4"
                            >
                                <div className="flex items-center space-x-2 rounded-lg border border-blue-200 dark:border-blue-800/50 p-3 bg-white dark:bg-slate-900 hover:border-blue-400 dark:hover:border-blue-600 transition-colors cursor-pointer">
                                    <RadioGroupItem value="current" id="scope-current" />
                                    <Label htmlFor="scope-current" className="cursor-pointer">
                                        Current Page
                                        <span className="block text-xs text-slate-500 mt-1">
                                            Export only visible {startups.length} rows
                                        </span>
                                    </Label>
                                </div>
                                <div className="flex items-center space-x-2 rounded-lg border border-blue-200 dark:border-blue-800/50 p-3 bg-white dark:bg-slate-900 hover:border-blue-400 dark:hover:border-blue-600 transition-colors cursor-pointer">
                                    <RadioGroupItem value="all" id="scope-all" />
                                    <Label htmlFor="scope-all" className="cursor-pointer">
                                        All Matching
                                        <span className="block text-xs text-slate-500 mt-1">
                                            Fetch all matching rows via API
                                        </span>
                                    </Label>
                                </div>
                            </RadioGroup>
                        </div>

                        {/* Column Selection */}
                        <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-emerald-50 to-teal-50 dark:from-emerald-900/10 dark:to-teal-900/10 border border-emerald-100 dark:border-emerald-800/30">
                            <div className="flex items-center justify-between">
                                <h4 className="text-sm font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-400 flex items-center gap-2">
                                    <CheckCircle2 className="h-4 w-4" />
                                    Select Columns
                                </h4>
                                <div className="flex items-center space-x-2">
                                    <Checkbox
                                        id="select-all"
                                        checked={selectedColumns.length === EXPORTABLE_COLUMNS.length}
                                        onCheckedChange={handleSelectAllColumns}
                                    />
                                    <Label htmlFor="select-all" className="text-xs cursor-pointer text-emerald-700 dark:text-emerald-400">
                                        Select All
                                    </Label>
                                </div>
                            </div>
                            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 border border-emerald-200 dark:border-emerald-800/50 rounded-lg p-3 max-h-[250px] overflow-y-auto bg-white dark:bg-slate-900">
                                {EXPORTABLE_COLUMNS.map((col) => (
                                    <div key={col.id} className="flex items-center space-x-2">
                                        <Checkbox
                                            id={`col-${col.id}`}
                                            checked={selectedColumns.includes(col.id)}
                                            onCheckedChange={(checked) => handleColumnToggle(col.id, checked as boolean)}
                                        />
                                        <Label
                                            htmlFor={`col-${col.id}`}
                                            className="text-sm font-normal cursor-pointer truncate"
                                            title={col.label}
                                        >
                                            {col.label}
                                        </Label>
                                    </div>
                                ))}
                            </div>
                            <p className="text-xs text-emerald-600 dark:text-emerald-400 text-right font-medium">
                                {selectedColumns.length} columns selected
                            </p>
                        </div>
                    </div>

                    {/* Footer */}
                    <div className="flex items-center justify-end gap-3 p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50">
                        <Button variant="outline" onClick={() => setExportDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button
                            onClick={performExport}
                            disabled={isExporting || selectedColumns.length === 0}
                            className="bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white shadow-lg shadow-emerald-500/25"
                        >
                            {isExporting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            <Download className="mr-2 h-4 w-4" />
                            {isExporting ? 'Exporting...' : 'Export CSV'}
                        </Button>
                    </div>
                </DialogContent>
            </Dialog>


            <CreateStartupDialog
                open={createDialogOpen}
                onOpenChange={setCreateDialogOpen}
                onSuccess={() => {
                    loadData()
                    // Re-fetch stats to update potential counts
                    fetchStartupStats().then(setStats)
                }}
            />

            {/* Moderation Dialogs */}
            {
                actionStartup && (
                    <>
                        <WarnStartupDialog
                            open={warnDialogOpen}
                            onOpenChange={setWarnDialogOpen}
                            startup={actionStartup}
                            onSuccess={handleActionComplete}
                        />
                        <StartupStatusDialog
                            open={statusDialogOpen}
                            onOpenChange={setStatusDialogOpen}
                            startup={actionStartup}
                            onSuccess={handleActionComplete}
                        />
                        <DeleteStartupDialog
                            open={deleteDialogOpen}
                            onOpenChange={setDeleteDialogOpen}
                            startup={actionStartup}
                            onSuccess={handleActionComplete}
                        />
                    </>
                )
            }
        </div >
    )
}

function getIndustryStyle(industry: string | undefined | null) {
    const ind = (industry || 'General').toLowerCase()

    if (ind.includes('fin') || ind.includes('bank') || ind.includes('invest'))
        return "bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-300 dark:border-emerald-800"

    if (ind.includes('health') || ind.includes('med') || ind.includes('bio'))
        return "bg-rose-100 text-rose-800 border-rose-200 dark:bg-rose-900/30 dark:text-rose-300 dark:border-rose-800"

    if (ind.includes('ed') || ind.includes('learn') || ind.includes('school'))
        return "bg-amber-100 text-amber-800 border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800"

    if (ind.includes('tech') || ind.includes('soft') || ind.includes('app') || ind.includes('saas') || ind.includes('ai'))
        return "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800"

    if (ind.includes('comm') || ind.includes('retail') || ind.includes('market') || ind.includes('shop'))
        return "bg-purple-100 text-purple-800 border-purple-200 dark:bg-purple-900/30 dark:text-purple-300 dark:border-purple-800"

    if (ind.includes('green') || ind.includes('env') || ind.includes('sus'))
        return "bg-lime-100 text-lime-800 border-lime-200 dark:bg-lime-900/30 dark:text-lime-300 dark:border-lime-800"

    return "bg-slate-100 text-slate-800 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700"
}

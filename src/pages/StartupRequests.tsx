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
    DollarSign,
    Search,
    FileText,
    Download,
    UserPlus,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    Facebook,
    Instagram,
    Twitter,
    FileSpreadsheet,
    FilePieChart
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
    DialogFooter,
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
    CardDescription,
    CardHeader,
    CardTitle,
} from "../components/ui/card"

import { Checkbox } from "../components/ui/checkbox"
import { Label } from "../components/ui/label"
import { RadioGroup, RadioGroupItem } from "../components/ui/radio-group"

import { StartupsFilters, StartupFilterState } from '../components/dashboard/StartupsFilters'
import { CreateStartupDialog } from '../components/dashboard/CreateStartupDialog'
import {
    fetchAllStartups,
    fetchStartupStats,
    searchUsers,
    transferStartupOwnership
} from '../lib/api'
import { Startup, StartupStats, User } from '../types'
import { formatCurrency, formatDate, formatNumber } from '../lib/utils'
import { KPICard } from '../components/dashboard/KPICard'

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

    const [userSearchQuery, setUserSearchQuery] = useState('')
    const [searchResults, setSearchResults] = useState<User[]>([])
    const [selectedUser, setSelectedUser] = useState<User | null>(null)
    const [searchingUsers, setSearchingUsers] = useState(false)
    const [transferring, setTransferring] = useState(false)

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

    async function handleUserSearch(query: string) {
        setUserSearchQuery(query)
        if (query.length < 2) {
            setSearchResults([])
            return
        }

        try {
            setSearchingUsers(true)
            // Filter by USER role (excludes Admins & Investors)
            const data = await searchUsers(query, 'STARTUP_OWNER')

            // Exclude current owner
            const currentOwnerId = transferDialog.startup?.ownerId
            const filteredUsers = data.content.filter(u => u.id !== currentOwnerId)

            setSearchResults(filteredUsers)
        } catch (err) {
            console.error('Failed to search users:', err)
        } finally {
            setSearchingUsers(false)
        }
    }

    async function handleTransferOwnership() {
        if (!transferDialog.startup || !selectedUser) return

        try {
            setTransferring(true)
            await transferStartupOwnership(transferDialog.startup.id, selectedUser.id)

            toast.success('Ownership transferred', {
                description: `${transferDialog.startup.name} is now owned by ${selectedUser.email}`
            })

            // Refresh list
            loadData()
            closeTransferDialog()
        } catch (err) {
            console.error('Failed to transfer ownership:', err)
            toast.error('Transfer failed', {
                description: err instanceof Error ? err.message : 'Failed to transfer ownership'
            })
        } finally {
            setTransferring(false)
        }
    }

    function closeTransferDialog() {
        setTransferDialog({ open: false, startup: null })
        setUserSearchQuery('')
        setSearchResults([])
        setSelectedUser(null)
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
            <Card>
                <CardHeader>
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                        <div>
                            <CardTitle>All Startups</CardTitle>
                            <CardDescription>
                                {startups.length} startups found
                            </CardDescription>
                        </div>
                        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 w-full sm:w-auto">
                            <div className="relative w-full sm:w-64">
                                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Search owner email..."
                                    value={quickSearch}
                                    onChange={(e) => setQuickSearch(e.target.value)}
                                    className="pl-8"
                                />
                            </div>
                            <div className="flex items-center gap-2">
                                <Button variant="outline" size="sm" onClick={loadData} className="flex-1 sm:flex-none">
                                    <RefreshCcw className="h-4 w-4 mr-2" />
                                    Refresh
                                </Button>
                                <Button variant="outline" size="sm" onClick={() => setExportDialogOpen(true)} className="flex-1 sm:flex-none">
                                    <Download className="h-4 w-4 mr-2" />
                                    Export
                                </Button>
                            </div>

                            <Button size="sm" onClick={() => setCreateDialogOpen(true)} className="w-full sm:w-auto">
                                <UserPlus className="h-4 w-4 mr-2" />
                                Create Startup
                            </Button>
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
                        <div className="rounded-md border">
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
                                        <TableRow key={startup.id}>
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
                                                        <p className="font-semibold text-sm">{startup.name}</p>
                                                        <div className="flex items-center gap-1 text-xs text-muted-foreground">
                                                            <UserCog className="h-3 w-3" />
                                                            {startup.ownerEmail}
                                                        </div>
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className="font-normal">
                                                    {startup.industry || 'General'}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                {getStageBadge(startup.stage)}
                                            </TableCell>
                                            <TableCell>
                                                <div className="flex items-center gap-1 font-medium">
                                                    <DollarSign className="h-3 w-3 text-muted-foreground" />
                                                    {formatCurrency(startup.fundingGoal || 0)}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                {getStatusBadge(startup.status)}
                                            </TableCell>
                                            <TableCell className="text-muted-foreground text-xs">
                                                {formatDate(startup.createdAt)}
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <div className="flex items-center justify-end gap-1">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-8 w-8 text-blue-600 hover:text-blue-700 hover:bg-blue-100 dark:hover:bg-blue-900/50"
                                                        onClick={() => setViewDialog({ open: true, startup })}
                                                        title="View Details"
                                                    >
                                                        <Eye className="h-4 w-4" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-8 w-8 text-amber-600 hover:text-amber-700 hover:bg-amber-100 dark:hover:bg-amber-900/50"
                                                        onClick={() => setTransferDialog({ open: true, startup })}
                                                        title="Transfer Ownership"
                                                    >
                                                        <UserCog className="h-4 w-4" />
                                                    </Button>
                                                    {startup.websiteUrl && (
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-foreground"
                                                            asChild
                                                            title="Visit Website"
                                                        >
                                                            <a href={startup.websiteUrl} target="_blank" rel="noopener noreferrer">
                                                                <Globe className="h-4 w-4" />
                                                            </a>
                                                        </Button>
                                                    )}
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
                </CardContent>
            </Card>

            {/* View Startup Dialog */}
            <Dialog open={viewDialog.open} onOpenChange={(open) => setViewDialog(prev => ({ ...prev, open }))}>
                <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto p-0">
                    <div className="relative h-48 w-full bg-muted">
                        {viewDialog.startup?.coverUrl ? (
                            <img
                                src={viewDialog.startup.coverUrl}
                                alt="Cover"
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <div className="w-full h-full bg-gradient-to-r from-primary/10 to-primary/5" />
                        )}
                        <div className="absolute -bottom-10 left-8">
                            {viewDialog.startup?.logoUrl ? (
                                <img
                                    src={viewDialog.startup.logoUrl}
                                    alt="Logo"
                                    className="h-24 w-24 rounded-xl object-cover border-4 border-background shadow-lg bg-white"
                                />
                            ) : (
                                <div className="h-24 w-24 rounded-xl bg-background border-4 border-background shadow-lg flex items-center justify-center">
                                    <div className="h-full w-full bg-primary/10 rounded-lg flex items-center justify-center">
                                        <Building2 className="h-10 w-10 text-primary" />
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="px-8 pt-12 pb-8">
                        <div className="flex justify-between items-start mb-6">
                            <div>
                                <DialogTitle className="text-2xl font-bold flex items-center gap-2">
                                    {viewDialog.startup?.name}
                                    {viewDialog.startup?.status && getStatusBadge(viewDialog.startup.status)}
                                </DialogTitle>
                                <DialogDescription className="text-base mt-1">
                                    {viewDialog.startup?.tagline}
                                </DialogDescription>
                            </div>
                            <div className="flex gap-2">
                                <div className="flex items-center gap-1 mr-2 border-r pr-2">
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-8 w-8 text-blue-600 hover:text-blue-700 hover:bg-blue-100"
                                        onClick={() => window.open(viewDialog.startup?.facebookUrl, '_blank')}
                                        disabled={!viewDialog.startup?.facebookUrl}
                                        title={viewDialog.startup?.facebookUrl ? "Facebook" : "Facebook (Not Provided)"}
                                    >
                                        <Facebook className="h-4 w-4" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-8 w-8 text-pink-600 hover:text-pink-700 hover:bg-pink-100"
                                        onClick={() => window.open(viewDialog.startup?.instagramUrl, '_blank')}
                                        disabled={!viewDialog.startup?.instagramUrl}
                                        title={viewDialog.startup?.instagramUrl ? "Instagram" : "Instagram (Not Provided)"}
                                    >
                                        <Instagram className="h-4 w-4" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-8 w-8 text-sky-500 hover:text-sky-600 hover:bg-sky-100"
                                        onClick={() => window.open(viewDialog.startup?.twitterUrl, '_blank')}
                                        disabled={!viewDialog.startup?.twitterUrl}
                                        title={viewDialog.startup?.twitterUrl ? "X (Twitter)" : "X (Twitter) (Not Provided)"}
                                    >
                                        <Twitter className="h-4 w-4" />
                                    </Button>
                                </div>
                                {viewDialog.startup?.websiteUrl && (
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => window.open(viewDialog.startup?.websiteUrl, '_blank')}
                                    >
                                        <Globe className="h-4 w-4 mr-2" />
                                        Website
                                    </Button>
                                )}
                                <Button
                                    size="sm"
                                    onClick={() => {
                                        if (viewDialog.startup) {
                                            setTransferDialog({ open: true, startup: viewDialog.startup });
                                            setViewDialog(prev => ({ ...prev, open: false }));
                                        }
                                    }}
                                >
                                    <UserCog className="h-4 w-4 mr-2" />
                                    Transfer
                                </Button>
                            </div>
                        </div>

                        {viewDialog.startup && (
                            <div className="space-y-6">
                                {/* Metrics Grid */}
                                <div className="grid grid-cols-3 gap-4">
                                    <div className="p-4 rounded-lg bg-muted/50 border">
                                        <p className="text-xs font-medium text-muted-foreground uppercase">Funding Goal</p>
                                        <p className="text-xl font-bold mt-1">{formatCurrency(viewDialog.startup.fundingGoal || 0)}</p>
                                    </div>
                                    <div className="p-4 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
                                        <p className="text-xs font-medium text-emerald-600 uppercase">Raised</p>
                                        <p className="text-xl font-bold text-emerald-700 mt-1">{formatCurrency(viewDialog.startup.raisedAmount)}</p>
                                    </div>
                                    <div className="p-4 rounded-lg bg-muted/50 border">
                                        <p className="text-xs font-medium text-muted-foreground uppercase">Stage</p>
                                        <div className="mt-1">{getStageBadge(viewDialog.startup.stage)}</div>
                                    </div>
                                </div>

                                {/* Info Grid */}
                                <div className="grid grid-cols-2 gap-x-8 gap-y-4 text-sm">
                                    <div className="flex justify-between py-2 border-b">
                                        <span className="text-muted-foreground">Industry</span>
                                        <span className="font-medium">{viewDialog.startup.industry}</span>
                                    </div>
                                    <div className="flex justify-between py-2 border-b">
                                        <div>
                                            <p className="text-muted-foreground">Owner</p>
                                            <div className="flex items-center gap-2 mt-1">
                                                <p className="font-medium text-primary">{viewDialog.startup.ownerEmail}</p>
                                                {viewDialog.startup.ownerRole && (
                                                    <span className="inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
                                                        {viewDialog.startup.ownerRole.replace(/_/g, " ")}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex justify-between py-2 border-b">
                                        <span className="text-muted-foreground">Created</span>
                                        <span>{formatDate(viewDialog.startup.createdAt)}</span>
                                    </div>
                                </div>

                                {/* Description */}
                                <div>
                                    <h4 className="text-sm font-semibold mb-2 flex items-center gap-2">
                                        <FileText className="h-4 w-4" />
                                        About Request
                                    </h4>
                                    <div className="p-4 rounded-lg bg-muted/30 text-sm leading-relaxed whitespace-pre-wrap">
                                        {viewDialog.startup.fullDescription}
                                    </div>
                                </div>

                                {/* Documents Grid */}
                                <div className="grid grid-cols-2 gap-4 pt-2">
                                    <Button
                                        variant="secondary"
                                        className="w-full justify-start"
                                        onClick={() => window.open(viewDialog.startup?.pitchDeckUrl, '_blank')}
                                        disabled={!viewDialog.startup?.pitchDeckUrl}
                                    >
                                        <FileText className="h-4 w-4 mr-2 text-blue-500" />
                                        View Pitch Deck
                                    </Button>
                                    <Button
                                        variant="secondary"
                                        className="w-full justify-start"
                                        onClick={() => window.open(viewDialog.startup?.financialDocumentsUrl, '_blank')}
                                        disabled={!viewDialog.startup?.financialDocumentsUrl}
                                    >
                                        <FileSpreadsheet className="h-4 w-4 mr-2 text-emerald-500" />
                                        Financial Documents
                                    </Button>
                                    <Button
                                        variant="secondary"
                                        className="w-full justify-start"
                                        onClick={() => window.open(viewDialog.startup?.businessPlanUrl, '_blank')}
                                        disabled={!viewDialog.startup?.businessPlanUrl}
                                    >
                                        <FileText className="h-4 w-4 mr-2 text-amber-500" />
                                        Business Plan
                                    </Button>
                                    <Button
                                        variant="secondary"
                                        className="w-full justify-start"
                                        onClick={() => window.open(viewDialog.startup?.businessModelUrl, '_blank')}
                                        disabled={!viewDialog.startup?.businessModelUrl}
                                    >
                                        <FilePieChart className="h-4 w-4 mr-2 text-purple-500" />
                                        Business Model
                                    </Button>
                                </div>
                            </div>
                        )}
                    </div>
                </DialogContent>
            </Dialog>

            {/* Transfer Ownership Dialog */}
            <Dialog open={transferDialog.open} onOpenChange={(open: boolean) => {
                if (!open) closeTransferDialog()
            }}>
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle>Transfer Ownership</DialogTitle>
                        <DialogDescription>
                            Transfer {transferDialog.startup?.name} to a new owner.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4 py-4">
                        {/* Search Input */}
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search user by email..."
                                value={userSearchQuery}
                                onChange={(e) => handleUserSearch(e.target.value)}
                                className="pl-9"
                            />
                        </div>

                        {/* Search Results */}
                        {searchingUsers ? (
                            <div className="flex items-center justify-center py-4">
                                <Loader2 className="h-5 w-5 animate-spin" />
                            </div>
                        ) : searchResults.length > 0 ? (
                            <div className="border rounded-lg divide-y max-h-48 overflow-y-auto">
                                {searchResults.map((user) => (
                                    <button
                                        key={user.id}
                                        onClick={() => setSelectedUser(user)}
                                        className={`w-full text-left px-4 py-3 hover:bg-muted/50 transition-colors ${selectedUser?.id === user.id ? 'bg-primary/10' : ''
                                            }`}
                                    >
                                        <p className="font-medium">{user.email}</p>
                                        <p className="text-xs text-muted-foreground">
                                            {user.role} • Joined {formatDate(user.createdAt)}
                                        </p>
                                    </button>
                                ))}
                            </div>
                        ) : userSearchQuery.length >= 2 ? (
                            <p className="text-sm text-muted-foreground text-center py-4">
                                No users found
                            </p>
                        ) : null}

                        {/* Selected User */}
                        {selectedUser && (
                            <div className="p-3 border rounded-lg bg-muted/50">
                                <p className="text-sm font-medium">Selected new owner:</p>
                                <p className="text-sm text-primary">{selectedUser.email}</p>
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={closeTransferDialog}>
                            Cancel
                        </Button>
                        <Button
                            onClick={handleTransferOwnership}
                            disabled={!selectedUser || transferring}
                        >
                            {transferring ? (
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            ) : (
                                <UserCog className="h-4 w-4 mr-2" />
                            )}
                            Transfer Ownership
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Export Dialog */}
            <Dialog open={exportDialogOpen} onOpenChange={setExportDialogOpen}>
                <DialogContent className="sm:max-w-[425px] md:max-w-[600px] max-h-[90vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>Export Startups</DialogTitle>
                        <DialogDescription>
                            Choose the scope and columns to include in your CSV export.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-6 py-4">
                        {/* Scope Selection */}
                        <div className="space-y-3">
                            <h4 className="text-sm font-medium">Export Scope</h4>
                            <RadioGroup
                                value={exportScope}
                                onValueChange={(v) => setExportScope(v as 'current' | 'all')}
                                className="grid grid-cols-2 gap-4"
                            >
                                <div className="flex items-center space-x-2 rounded-md border p-3 hover:bg-accent/50">
                                    <RadioGroupItem value="current" id="scope-current" />
                                    <Label htmlFor="scope-current" className="cursor-pointer">
                                        Current Page
                                        <span className="block text-xs text-muted-foreground mt-1">
                                            Export only visible {startups.length} rows
                                        </span>
                                    </Label>
                                </div>
                                <div className="flex items-center space-x-2 rounded-md border p-3 hover:bg-accent/50">
                                    <RadioGroupItem value="all" id="scope-all" />
                                    <Label htmlFor="scope-all" className="cursor-pointer">
                                        All Matching
                                        <span className="block text-xs text-muted-foreground mt-1">
                                            Fetch all matching rows via API
                                        </span>
                                    </Label>
                                </div>
                            </RadioGroup>
                        </div>

                        {/* Column Selection */}
                        <div className="space-y-3">
                            <div className="flex items-center justify-between">
                                <h4 className="text-sm font-medium">Select Columns</h4>
                                <div className="flex items-center space-x-2">
                                    <Checkbox
                                        id="select-all"
                                        checked={selectedColumns.length === EXPORTABLE_COLUMNS.length}
                                        onCheckedChange={handleSelectAllColumns}
                                    />
                                    <Label htmlFor="select-all" className="text-xs cursor-pointer">
                                        Select All
                                    </Label>
                                </div>
                            </div>
                            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 border rounded-md p-3 max-h-[300px] overflow-y-auto">
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
                            <p className="text-xs text-muted-foreground text-right">
                                {selectedColumns.length} columns selected
                            </p>
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setExportDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={performExport} disabled={isExporting || selectedColumns.length === 0}>
                            {isExporting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            {isExporting ? 'Exporting...' : 'Export CSV'}
                        </Button>
                    </DialogFooter>
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
        </div >
    )
}

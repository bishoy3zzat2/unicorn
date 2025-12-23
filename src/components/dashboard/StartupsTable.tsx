import { Input } from '../ui/input'
import { Button } from '../ui/button'
import { useState, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table'
import { Startup } from '../../types'
import { formatDate } from '../../lib/utils'
import { StartupDetailsDialog } from './StartupDetailsDialog'
import { fetchAllStartups as fetchStartupsApi } from '../../lib/api'
import { toast } from 'sonner'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '../ui/dropdown-menu'
import {
    MoreVertical, Eye, AlertTriangle, Shield, Trash2, Loader2, RotateCcw,
    Download, Filter, Table as TableIcon, Database, Search, Building2
} from 'lucide-react'
import {
    Dialog, DialogContent, DialogDescription,
    DialogHeader, DialogTitle, DialogTrigger
} from '../ui/dialog'
import { Checkbox } from '../ui/checkbox'
import { RadioGroup, RadioGroupItem } from '../ui/radio-group'
import { WarnStartupDialog, StartupStatusDialog, DeleteStartupDialog } from './StartupActionDialogs'
import { useAuth } from '../../contexts/AuthContext'

const EXPORTABLE_COLUMNS = [
    { id: 'id', label: 'ID' },
    { id: 'name', label: 'Name' },
    { id: 'industry', label: 'Industry' },
    { id: 'fundingGoal', label: 'Funding Goal' },
    { id: 'ownerEmail', label: 'Owner Email' },
    { id: 'createdAt', label: 'Date Submitted' },
    { id: 'status', label: 'Status' },
]

export function StartupsTable() {
    const [startups, setStartups] = useState<Startup[]>([])
    const [loading, setLoading] = useState(true)
    const [selectedStartup, setSelectedStartup] = useState<Startup | null>(null)
    const [isModalOpen, setIsModalOpen] = useState(false)
    const [memberEmail, setMemberEmail] = useState('')

    // Action Dialog States
    const [actionStartup, setActionStartup] = useState<Startup | null>(null)
    const [warnDialogOpen, setWarnDialogOpen] = useState(false)
    const [statusDialogOpen, setStatusDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

    // Export State
    const [exportDialogOpen, setExportDialogOpen] = useState(false)
    const [exportScope, setExportScope] = useState<'current' | 'all'>('current')
    const [selectedColumns, setSelectedColumns] = useState<string[]>(
        EXPORTABLE_COLUMNS.map(col => col.id)
    )
    const [isExporting, setIsExporting] = useState(false)

    // Quick Status Filter
    const [statusFilter, setStatusFilter] = useState<string>('ALL')

    const { user } = useAuth()
    const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'

    const fetchAllStartups = async () => {
        setLoading(true)
        try {
            const filters: any = { memberEmail }
            if (statusFilter !== 'ALL') {
                filters.status = statusFilter
            }
            const data = await fetchStartupsApi(0, 100, filters)
            setStartups(data.content)
        } catch (error: any) {
            toast.error(error.message || 'Failed to load startups')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchAllStartups()
    }, [statusFilter])

    const handleRowClick = (startup: Startup) => {
        setSelectedStartup(startup)
        setIsModalOpen(true)
    }

    const handleActionComplete = () => {
        fetchAllStartups()
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

    const getStatusBadgeClass = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'bg-emerald-500/10 text-emerald-500 border-emerald-500/30'
            case 'BANNED': return 'bg-destructive/10 text-destructive border-destructive/30'
            default: return 'bg-slate-800 text-slate-400 border border-slate-700'
        }
    }

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
        }).format(amount)
    }

    // Loading State
    if (loading && startups.length === 0) {
        return (
            <div className="space-y-4">
                {/* Skeleton Toolbar */}
                <div className="bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 animate-pulse">
                    <div className="flex justify-between items-center">
                        <div className="flex gap-2">
                            {[1, 2, 3, 4, 5].map(i => (
                                <div key={i} className="h-8 w-20 bg-slate-200 dark:bg-slate-700 rounded-md" />
                            ))}
                        </div>
                        <div className="flex gap-2">
                            <div className="h-10 w-64 bg-slate-200 dark:bg-slate-700 rounded-md" />
                            <div className="h-10 w-10 bg-slate-200 dark:bg-slate-700 rounded-md" />
                        </div>
                    </div>
                </div>
                {/* Skeleton Table */}
                <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                    <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                        <Loader2 className="h-10 w-10 animate-spin mb-4" />
                        <p>Loading startups...</p>
                    </div>
                </div>
            </div>
        )
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

    const performExport = async () => {
        try {
            setIsExporting(true)
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-')

            if (exportScope === 'current') {
                downloadCSV(startups, `startups_current_page_${timestamp}.csv`)
                toast.success('Exported current page successfully')
            } else {
                toast.info('Fetching all matching records...')
                const data = await fetchStartupsApi(0, 1000, { memberEmail })
                downloadCSV(data.content, `startups_all_${timestamp}.csv`)
                toast.success(`Exported ${data.content.length} records successfully`)
            }
            setExportDialogOpen(false)
        } catch (error) {
            console.error('Export failed:', error)
            toast.error('Failed to export data')
        } finally {
            setIsExporting(false)
        }
    }

    return (
        <>
            <div className="space-y-4">
                {/* Toolbar Section */}
                <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800">
                    {/* Status Tabs */}
                    <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-lg overflow-x-auto max-w-full no-scrollbar">
                        {['ALL', 'ACTIVE', 'BANNED'].map((status) => (
                            <button
                                key={status}
                                onClick={() => setStatusFilter(status)}
                                className={`
                                    px-4 py-1.5 rounded-md text-sm font-medium transition-all duration-200 whitespace-nowrap
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
                                placeholder="Search by email..."
                                className="pl-9 bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
                                value={memberEmail}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setMemberEmail(e.target.value)}
                                onKeyDown={(e: React.KeyboardEvent<HTMLInputElement>) => e.key === 'Enter' && fetchAllStartups()}
                            />
                        </div>

                        <div className="flex gap-2">
                            <Button
                                variant="outline"
                                size="icon"
                                onClick={fetchAllStartups}
                                disabled={loading}
                                className="shrink-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700"
                                title="Refresh List"
                            >
                                <RotateCcw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                            </Button>

                            {/* Export Dropdown */}
                            <Dialog open={exportDialogOpen} onOpenChange={setExportDialogOpen}>
                                <DialogTrigger asChild>
                                    <Button variant="outline" className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 hover:bg-slate-50 gap-2">
                                        <Download className="h-4 w-4" />
                                        <span className="hidden sm:inline">Export</span>
                                    </Button>
                                </DialogTrigger>
                                <DialogContent className="sm:max-w-[500px] max-h-[85vh] overflow-hidden flex flex-col p-0 gap-0 border-none shadow-2xl bg-white dark:bg-slate-950">
                                    <div className="bg-gradient-to-r from-slate-800 via-blue-900/50 to-indigo-900/50 dark:from-slate-900 dark:via-blue-950/80 dark:to-indigo-950/80 p-6 border-b border-slate-700/50 shrink-0">
                                        <DialogHeader className="space-y-2">
                                            <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white">
                                                <div className="h-12 w-12 rounded-2xl bg-white/20 shadow-lg flex items-center justify-center backdrop-blur-sm">
                                                    <Download className="h-6 w-6 text-white" />
                                                </div>
                                                Export Startups
                                            </DialogTitle>
                                            <DialogDescription className="text-white/80">
                                                Download startup data as a CSV file. Select your scope and custom columns below.
                                            </DialogDescription>
                                        </DialogHeader>
                                    </div>

                                    <div className="overflow-y-auto p-6 space-y-8 custom-scrollbar">
                                        <div className="space-y-4">
                                            <h4 className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                                                <Filter className="h-4 w-4" />
                                                Export Scope
                                            </h4>
                                            <RadioGroup
                                                value={exportScope}
                                                onValueChange={(v) => setExportScope(v as 'current' | 'all')}
                                                className="grid grid-cols-1 sm:grid-cols-2 gap-4"
                                            >
                                                <label className={`
                                                    relative flex flex-col gap-3 p-4 rounded-xl border-2 cursor-pointer transition-all duration-200
                                                    hover:shadow-md
                                                    ${exportScope === 'current'
                                                        ? 'border-emerald-500 bg-emerald-50/50 dark:bg-emerald-900/10 dark:border-emerald-500/50'
                                                        : 'border-border bg-card hover:border-emerald-200 dark:hover:border-emerald-800'
                                                    }
                                                `}>
                                                    <div className="flex items-center justify-between">
                                                        <div className={`p-2 rounded-lg ${exportScope === 'current' ? 'bg-emerald-100 text-emerald-600 dark:bg-emerald-500/20' : 'bg-muted text-muted-foreground'}`}>
                                                            <TableIcon className="h-5 w-5" />
                                                        </div>
                                                        <RadioGroupItem value="current" id="scope-current" className="sr-only" />
                                                        {exportScope === 'current' && (
                                                            <div className="h-5 w-5 rounded-full bg-emerald-500 text-white flex items-center justify-center animate-in zoom-in">
                                                                <div className="h-2 w-2 rounded-full bg-white" />
                                                            </div>
                                                        )}
                                                    </div>
                                                    <div>
                                                        <span className={`font-bold block ${exportScope === 'current' ? 'text-emerald-700 dark:text-emerald-300' : 'text-foreground'}`}>Current Page</span>
                                                        <span className="text-xs text-muted-foreground mt-1 block">
                                                            Export only visible {startups.length} rows
                                                        </span>
                                                    </div>
                                                </label>

                                                <label className={`
                                                    relative flex flex-col gap-3 p-4 rounded-xl border-2 cursor-pointer transition-all duration-200
                                                    hover:shadow-md
                                                    ${exportScope === 'all'
                                                        ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-900/10 dark:border-indigo-500/50'
                                                        : 'border-border bg-card hover:border-indigo-200 dark:hover:border-indigo-800'
                                                    }
                                                `}>
                                                    <div className="flex items-center justify-between">
                                                        <div className={`p-2 rounded-lg ${exportScope === 'all' ? 'bg-indigo-100 text-indigo-600 dark:bg-indigo-500/20' : 'bg-muted text-muted-foreground'}`}>
                                                            <Database className="h-5 w-5" />
                                                        </div>
                                                        <RadioGroupItem value="all" id="scope-all" className="sr-only" />
                                                        {exportScope === 'all' && (
                                                            <div className="h-5 w-5 rounded-full bg-indigo-500 text-white flex items-center justify-center animate-in zoom-in">
                                                                <div className="h-2 w-2 rounded-full bg-white" />
                                                            </div>
                                                        )}
                                                    </div>
                                                    <div>
                                                        <span className={`font-bold block ${exportScope === 'all' ? 'text-indigo-700 dark:text-indigo-300' : 'text-foreground'}`}>All Matching</span>
                                                        <span className="text-xs text-muted-foreground mt-1 block">
                                                            All startups
                                                        </span>
                                                    </div>
                                                </label>
                                            </RadioGroup>
                                        </div>

                                        <div className="space-y-4">
                                            <div className="flex items-center justify-between">
                                                <h4 className="text-sm font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                                                    <TableIcon className="h-4 w-4" />
                                                    Columns
                                                </h4>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    className="h-8 text-xs font-medium text-blue-600 hover:text-blue-700 hover:bg-blue-50 dark:text-blue-400 dark:hover:bg-blue-900/20"
                                                    onClick={(e) => {
                                                        e.preventDefault();
                                                        handleSelectAllColumns(selectedColumns.length !== EXPORTABLE_COLUMNS.length);
                                                    }}
                                                >
                                                    {selectedColumns.length === EXPORTABLE_COLUMNS.length ? 'Deselect All' : 'Select All'}
                                                </Button>
                                            </div>
                                            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 border rounded-xl p-4 bg-slate-50/50 dark:bg-slate-900/50 max-h-[220px] overflow-y-auto custom-scrollbar">
                                                {EXPORTABLE_COLUMNS.map((col) => (
                                                    <div key={col.id} className="group flex items-center space-x-2.5 p-2 rounded-lg hover:bg-white dark:hover:bg-slate-800 border border-transparent hover:border-border transition-all">
                                                        <Checkbox
                                                            id={`col-${col.id}`}
                                                            checked={selectedColumns.includes(col.id)}
                                                            onCheckedChange={(checked) => handleColumnToggle(col.id, checked as boolean)}
                                                            className="data-[state=checked]:bg-blue-600 data-[state=checked]:border-blue-600"
                                                        />
                                                        <label
                                                            htmlFor={`col-${col.id}`}
                                                            className="text-sm text-muted-foreground group-hover:text-foreground cursor-pointer truncate flex-1 font-medium transition-colors"
                                                        >
                                                            {col.label}
                                                        </label>
                                                    </div>
                                                ))}
                                            </div>
                                            <p className="text-xs text-muted-foreground text-right px-1 flex justify-end gap-1">
                                                <span className="font-semibold text-foreground">{selectedColumns.length}</span> columns selected
                                            </p>
                                        </div>
                                    </div>

                                    <div className="border-t p-6 bg-muted/20 shrink-0 flex items-center justify-end gap-3">
                                        <Button variant="ghost" onClick={() => setExportDialogOpen(false)} className="hover:bg-muted font-medium">
                                            Cancel
                                        </Button>
                                        <Button
                                            onClick={performExport}
                                            disabled={isExporting || selectedColumns.length === 0}
                                            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md hover:shadow-lg transition-all gap-2 px-6"
                                        >
                                            {isExporting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
                                            {isExporting ? 'Exporting...' : 'Export CSV'}
                                        </Button>
                                    </div>
                                </DialogContent>
                            </Dialog>
                        </div>
                    </div>
                </div>

                {/* Table Section */}
                <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                    {startups.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-center">
                            <div className="h-16 w-16 bg-muted rounded-full flex items-center justify-center mb-4">
                                <Building2 className="h-8 w-8 text-muted-foreground" />
                            </div>
                            <h3 className="text-lg font-semibold text-foreground">No startups found</h3>
                            <p className="text-muted-foreground max-w-sm mx-auto mt-1">
                                No startups match your current filters. Try adjusting them.
                            </p>
                            <Button
                                variant="outline"
                                className="mt-6"
                                onClick={() => {
                                    setStatusFilter('ALL')
                                    setMemberEmail('')
                                }}
                            >
                                Clear All Filters
                            </Button>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <Table>
                                <TableHeader className="bg-slate-50 dark:bg-slate-800/50">
                                    <TableRow className="hover:bg-transparent border-b-border">
                                        <TableHead className="font-semibold text-muted-foreground pl-6">Startup Name</TableHead>
                                        <TableHead className="font-semibold text-muted-foreground">Industry</TableHead>
                                        <TableHead className="font-semibold text-muted-foreground">Funding Goal</TableHead>
                                        <TableHead className="font-semibold text-muted-foreground">Owner</TableHead>
                                        <TableHead className="font-semibold text-muted-foreground">Date Submitted</TableHead>
                                        <TableHead className="font-semibold text-muted-foreground">Status</TableHead>
                                        {isAdmin && <TableHead className="text-right pr-6 font-semibold text-muted-foreground">Actions</TableHead>}
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {startups.map((startup) => (
                                        <TableRow
                                            key={startup.id}
                                            onClick={() => handleRowClick(startup)}
                                            className="group cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800/50 transition-colors border-b-border"
                                        >
                                            <TableCell className="font-medium pl-6 py-4">{startup.name}</TableCell>
                                            <TableCell className="py-4">{startup.industry || '—'}</TableCell>
                                            <TableCell className="py-4">
                                                {startup.fundingGoal ? formatCurrency(startup.fundingGoal) : '—'}
                                            </TableCell>
                                            <TableCell className="py-4">{startup.ownerEmail}</TableCell>
                                            <TableCell className="py-4">{formatDate(startup.createdAt)}</TableCell>
                                            <TableCell className="py-4">
                                                <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium border ${getStatusBadgeClass(startup.status)}`}>
                                                    {startup.status}
                                                </span>
                                            </TableCell>
                                            {isAdmin && (
                                                <TableCell className="text-right pr-6">
                                                    <div className="flex items-center justify-end gap-1">
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 transition-all"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                handleRowClick(startup)
                                                            }}
                                                            title="View Details"
                                                        >
                                                            <Eye className="h-4 w-4" />
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
                                                                <DropdownMenuItem onClick={(e) => { e.stopPropagation(); handleRowClick(startup) }}>
                                                                    <Eye className="mr-2 h-4 w-4" />
                                                                    View Details
                                                                </DropdownMenuItem>
                                                                <DropdownMenuSeparator />
                                                                <DropdownMenuItem onClick={(e) => handleStatusChange(e, startup)}>
                                                                    <Shield className="mr-2 h-4 w-4 text-blue-500" />
                                                                    Change Status
                                                                </DropdownMenuItem>
                                                                <DropdownMenuItem onClick={(e) => handleWarn(e, startup)}>
                                                                    <AlertTriangle className="mr-2 h-4 w-4 text-yellow-500" />
                                                                    Issue Warning
                                                                </DropdownMenuItem>
                                                                <DropdownMenuSeparator />
                                                                <DropdownMenuItem
                                                                    onClick={(e) => handleDelete(e, startup)}
                                                                    className="text-red-600 focus:text-red-600"
                                                                >
                                                                    <Trash2 className="mr-2 h-4 w-4" />
                                                                    Delete Permanently
                                                                </DropdownMenuItem>
                                                            </DropdownMenuContent>
                                                        </DropdownMenu>
                                                    </div>
                                                </TableCell>
                                            )}
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    )}
                </div>
            </div>

            {/* Details Modal */}
            <StartupDetailsDialog
                startup={selectedStartup}
                open={isModalOpen}
                onOpenChange={(open) => {
                    setIsModalOpen(open)
                    if (!open) setSelectedStartup(null)
                }}
                onActionComplete={handleActionComplete}
            />

            {/* Action Dialogs */}
            {actionStartup && (
                <>
                    <WarnStartupDialog
                        startup={actionStartup}
                        open={warnDialogOpen}
                        onOpenChange={setWarnDialogOpen}
                        onSuccess={handleActionComplete}
                    />
                    <StartupStatusDialog
                        startup={actionStartup}
                        open={statusDialogOpen}
                        onOpenChange={setStatusDialogOpen}
                        onSuccess={handleActionComplete}
                    />
                    <DeleteStartupDialog
                        startup={actionStartup}
                        open={deleteDialogOpen}
                        onOpenChange={setDeleteDialogOpen}
                        onSuccess={handleActionComplete}
                    />
                </>
            )}
        </>
    )
}

import { Input } from '../ui/input'
import { Button } from '../ui/button'
import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader } from '../ui/card'
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

    const { user } = useAuth()
    const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'

    const fetchAllStartups = async () => {
        setLoading(true)
        try {
            const data = await fetchStartupsApi(0, 100, { memberEmail })
            setStartups(data.content)
        } catch (error: any) {
            toast.error(error.message || 'Failed to load startups')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchAllStartups()
    }, [])

    const handleRowClick = (startup: Startup) => {
        setSelectedStartup(startup)
        setIsModalOpen(true)
    }



    const handleActionComplete = () => {
        // Refresh the list after moderation action
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
            case 'APPROVED': return 'bg-green-500/10 text-green-500 border-green-500/30'
            case 'PENDING': return 'bg-yellow-500/10 text-yellow-500 border-yellow-500/30'
            case 'REJECTED': return 'bg-red-500/10 text-red-500 border-red-500/30'
            case 'SUSPENDED': return 'bg-orange-500/10 text-orange-500 border-orange-500/30'
            case 'BANNED': return 'bg-destructive/10 text-destructive border-destructive/30'
            case 'ARCHIVED': return 'bg-gray-500/10 text-gray-500 border-gray-500/30'
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

    if (loading) {
        return (
            <Card>
                <CardContent className="flex items-center justify-center py-16">
                    <div className="text-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
                        <p className="text-muted-foreground">Loading startups...</p>
                    </div>
                </CardContent>
            </Card>
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
                // NOTE: For now exporting current startups as 'all' since backend pagination might be complex to circumvent without a specific 'all' endpoint
                // Ideally this would fetch all pages. For now we use the current list or fetch a large page.
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
            <Card className="overflow-hidden">
                <CardHeader className="bg-slate-800/30 dark:bg-slate-800/50 border-b border-slate-700/50 pb-4">
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="flex items-center gap-3">
                            <div className="h-10 w-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
                                <Building2 className="h-5 w-5 text-indigo-400" />
                            </div>
                            <div>
                                <h2 className="text-lg font-bold tracking-tight text-white">All Startups</h2>
                                <div className="flex items-center gap-2">
                                    {loading && <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />}
                                    <span className="text-xs text-slate-400 font-medium">
                                        {startups.length} {startups.length === 1 ? 'startup' : 'startups'} total
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-2">
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                                <Input
                                    placeholder="Search by email..."
                                    className="pl-9 w-full sm:w-[250px] bg-slate-900/50 border-slate-700 text-white placeholder:text-slate-500 focus:border-indigo-500 focus:ring-indigo-500/20"
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
                                    className="bg-slate-900/50 border-slate-700 hover:bg-slate-800 hover:border-slate-600 text-slate-300"
                                    title="Refresh List"
                                >
                                    <RotateCcw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                                </Button>

                                {/* Export Dropdown */}
                                <Dialog open={exportDialogOpen} onOpenChange={setExportDialogOpen}>
                                    <DialogTrigger asChild>
                                        <Button variant="outline" className="bg-slate-900/50 border-slate-700 hover:bg-slate-800 hover:border-slate-600 text-slate-300 gap-2">
                                            <Download className="h-4 w-4" />
                                            <span className="hidden sm:inline">Export</span>
                                        </Button>
                                    </DialogTrigger>
                                    <DialogContent className="sm:max-w-[500px] max-h-[85vh] overflow-hidden flex flex-col p-0 gap-0 border-none shadow-2xl bg-white dark:bg-slate-950">
                                        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/10 dark:to-indigo-900/10 p-6 border-b border-border/50 shrink-0">
                                            <DialogHeader className="space-y-2">
                                                <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-primary">
                                                    <div className="h-12 w-12 rounded-2xl bg-white/80 dark:bg-white/10 shadow-sm flex items-center justify-center backdrop-blur-sm">
                                                        <Download className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                                                    </div>
                                                    Export Startups
                                                </DialogTitle>
                                                <DialogDescription className="text-base text-muted-foreground/80">
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
                                                            />
                                                            <label
                                                                htmlFor={`col-${col.id}`}
                                                                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer flex-1"
                                                            >
                                                                {col.label}
                                                            </label>
                                                        </div>
                                                    ))}
                                                </div>
                                            </div>
                                        </div>

                                        <div className="bg-muted/30 p-6 border-t border-border/50 flex items-center justify-end gap-3 shrink-0">
                                            <Button
                                                variant="outline"
                                                onClick={() => setExportDialogOpen(false)}
                                                disabled={isExporting}
                                            >
                                                Cancel
                                            </Button>
                                            <Button
                                                onClick={performExport}
                                                disabled={isExporting}
                                                className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-lg shadow-blue-500/20"
                                            >
                                                {isExporting ? (
                                                    <>
                                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                        Exporting...
                                                    </>
                                                ) : (
                                                    <>
                                                        <Download className="mr-2 h-4 w-4" />
                                                        Export Data
                                                    </>
                                                )}
                                            </Button>
                                        </div>
                                    </DialogContent>
                                </Dialog>
                            </div>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    {startups.length === 0 ? (
                        <div className="text-center py-8 text-muted-foreground">
                            No startups found
                        </div>
                    ) : (
                        <div className="rounded-md border mt-4">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Startup Name</TableHead>
                                        <TableHead>Industry</TableHead>
                                        <TableHead>Funding Goal</TableHead>
                                        <TableHead>Owner</TableHead>
                                        <TableHead>Date Submitted</TableHead>
                                        <TableHead>Status</TableHead>
                                        {isAdmin && <TableHead className="text-right">Actions</TableHead>}
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {startups.map((startup) => (
                                        <TableRow
                                            key={startup.id}
                                            onClick={() => handleRowClick(startup)}
                                            className="cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800/50 transition-colors"
                                        >
                                            <TableCell className="font-medium">{startup.name}</TableCell>
                                            <TableCell>{startup.industry || '—'}</TableCell>
                                            <TableCell>
                                                {startup.fundingGoal ? formatCurrency(startup.fundingGoal) : '—'}
                                            </TableCell>
                                            <TableCell>{startup.ownerEmail}</TableCell>
                                            <TableCell>{formatDate(startup.createdAt)}</TableCell>
                                            <TableCell>
                                                <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium border ${getStatusBadgeClass(startup.status)}`}>
                                                    {startup.status}
                                                </span>
                                            </TableCell>
                                            {isAdmin && (
                                                <TableCell className="text-right">
                                                    <div className="flex items-center justify-end gap-1">
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 hover:bg-slate-800"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                handleRowClick(startup)
                                                            }}
                                                            title="View Details"
                                                        >
                                                            <Eye className="h-4 w-4 text-blue-400" />
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
                </CardContent>
            </Card>

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

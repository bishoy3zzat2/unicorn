import { useState, useEffect, useMemo } from 'react'
import {
    useReactTable,
    getCoreRowModel,
    flexRender,
    ColumnDef,
    PaginationState,
} from '@tanstack/react-table'
import { Card, CardContent, CardHeader } from '../../components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'
import { Input } from '../../components/ui/input'
import { Button } from '../../components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select'
import { formatDate } from '../../lib/utils'
import {
    Building2, TrendingUp, User as UserIcon, Loader2, ChevronLeft,
    ChevronRight, ChevronsLeft, ChevronsRight, Search, Eye, Ban,
    AlertTriangle, Trash2, Shield, Clock, Download
} from 'lucide-react'
import { toast } from 'sonner'
import api from '../../lib/axios'
import { UserDetailsModal } from './UserDetailsModal'
import { SuspendUserDialog } from './SuspendUserDialog'
import { WarnUserDialog } from './WarnUserDialog'
import { DeleteUserDialog } from './DeleteUserDialog'
import { UserFilters, FilterState } from './UserFilters'

import {
    Dialog, DialogContent, DialogDescription,
    DialogFooter, DialogHeader, DialogTitle, DialogTrigger
} from '../../components/ui/dialog'
import { Label } from '../../components/ui/label'
import { Checkbox } from '../../components/ui/checkbox'
import { RadioGroup, RadioGroupItem } from '../../components/ui/radio-group'

// ... existing imports ...

interface UserData {
    id: string
    email: string
    username: string
    firstName: string | null
    lastName: string | null
    displayName: string | null
    role: string
    status: string
    authProvider: string
    phoneNumber: string | null
    country: string | null
    avatarUrl: string | null
    createdAt: string
    lastLoginAt?: string | null
    suspendedAt?: string | null
    suspendedUntil?: string | null
    suspensionType?: string | null
    suspendReason?: string | null
    hasInvestorProfile?: boolean
    hasStartups?: boolean
}

const EXPORTABLE_COLUMNS = [
    { id: 'id', label: 'ID' },
    { id: 'email', label: 'Email' },
    { id: 'username', label: 'Username' },
    { id: 'firstName', label: 'First Name' },
    { id: 'lastName', label: 'Last Name' },
    { id: 'displayName', label: 'Display Name' },
    { id: 'role', label: 'Role' },
    { id: 'status', label: 'Status' },
    { id: 'authProvider', label: 'Auth Provider' },
    { id: 'phoneNumber', label: 'Phone Number' },
    { id: 'country', label: 'Country' },
    { id: 'createdAt', label: 'Created At' },
    { id: 'lastLoginAt', label: 'Last Login' },
    { id: 'suspendedAt', label: 'Suspended At' },
    { id: 'suspendedUntil', label: 'Suspended Until' },
    { id: 'suspensionType', label: 'Suspension Type' },
    { id: 'suspendReason', label: 'Suspend Reason' },
    { id: 'hasInvestorProfile', label: 'Has Investor Profile' },
    { id: 'hasStartups', label: 'Has Startups' },
]

export function UsersTable() {
    // Pagination State
    const [{ pageIndex, pageSize }, setPagination] = useState<PaginationState>({
        pageIndex: 0,
        pageSize: 20,
    })

    const pagination = useMemo(
        () => ({
            pageIndex,
            pageSize,
        }),
        [pageIndex, pageSize]
    )

    // Data State
    const [data, setData] = useState<UserData[]>([])
    const [pageCount, setPageCount] = useState(-1)
    const [totalUsers, setTotalUsers] = useState(0)
    const [isLoading, setIsLoading] = useState(true)

    // Filter State
    const [searchQuery, setSearchQuery] = useState('')
    const [debouncedQuery, setDebouncedQuery] = useState('')
    const [filters, setFilters] = useState<FilterState>({})
    const [appliedFilters, setAppliedFilters] = useState<FilterState>({})

    // Modal States
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null)
    const [detailsModalOpen, setDetailsModalOpen] = useState(false)
    const [suspendDialogOpen, setSuspendDialogOpen] = useState(false)
    const [warnDialogOpen, setWarnDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

    // Export State
    const [exportDialogOpen, setExportDialogOpen] = useState(false)
    const [exportScope, setExportScope] = useState<'current' | 'all'>('current')
    const [selectedColumns, setSelectedColumns] = useState<string[]>(
        EXPORTABLE_COLUMNS.map(col => col.id)
    )
    const [isExporting, setIsExporting] = useState(false)

    // Debounce search query
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedQuery(searchQuery)
            setPagination(prev => ({ ...prev, pageIndex: 0 }))
        }, 500)
        return () => clearTimeout(handler)
    }, [searchQuery])

    const downloadCSV = (data: UserData[], filename: string) => {
        // Filter columns based on selection
        const activeColumns = EXPORTABLE_COLUMNS.filter(col => selectedColumns.includes(col.id))
        const headers = activeColumns.map(col => col.label)

        const csvContent = [
            headers.join(','),
            ...data.map(user => {
                const row = activeColumns.map(col => {
                    const value = user[col.id as keyof UserData]
                    return value === undefined || value === null ? '' : value
                })

                // Escape quotes and wrap in quotes if needed
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
                downloadCSV(data, `users_current_page_${timestamp}.csv`)
                toast.success('Exported current page successfully')
            } else {
                toast.info('Fetching all matching records...')
                let url = `/admin/users?page=0&size=10000`
                if (debouncedQuery) {
                    url += `&query=${encodeURIComponent(debouncedQuery)}`
                }
                url = buildFilterUrl(url, appliedFilters)

                const response = await api.get(url)
                const allData = response.data.content
                downloadCSV(allData, `users_all_filtered_${timestamp}.csv`)
                toast.success(`Exported ${allData.length} records successfully`)
            }
            setExportDialogOpen(false)
        } catch (error) {
            console.error('Export failed:', error)
            toast.error('Failed to export data')
        } finally {
            setIsExporting(false)
        }
    }

    // Build URL with filter parameters
    const buildFilterUrl = (baseUrl: string, filterState: FilterState): string => {
        const params = new URLSearchParams()

        // Text filters
        if (filterState.email) {
            params.append('email', filterState.email)
            if (filterState.emailNegate) params.append('emailNegate', 'true')
        }
        if (filterState.username) {
            params.append('username', filterState.username)
            if (filterState.usernameNegate) params.append('usernameNegate', 'true')
        }
        if (filterState.firstName) {
            params.append('firstName', filterState.firstName)
            if (filterState.firstNameNegate) params.append('firstNameNegate', 'true')
        }
        if (filterState.lastName) {
            params.append('lastName', filterState.lastName)
            if (filterState.lastNameNegate) params.append('lastNameNegate', 'true')
        }
        if (filterState.displayName) {
            params.append('displayName', filterState.displayName)
            if (filterState.displayNameNegate) params.append('displayNameNegate', 'true')
        }
        if (filterState.country) {
            params.append('country', filterState.country)
            if (filterState.countryNegate) params.append('countryNegate', 'true')
        }

        // Select filters
        if (filterState.role) {
            params.append('role', filterState.role)
            if (filterState.roleNegate) params.append('roleNegate', 'true')
        }
        if (filterState.status) {
            params.append('status', filterState.status)
            if (filterState.statusNegate) params.append('statusNegate', 'true')
        }
        if (filterState.authProvider) {
            params.append('authProvider', filterState.authProvider)
            if (filterState.authProviderNegate) params.append('authProviderNegate', 'true')
        }

        // Date filters
        if (filterState.createdAtFrom) {
            params.append('createdAtFrom', new Date(filterState.createdAtFrom).toISOString())
        }
        if (filterState.createdAtTo) {
            params.append('createdAtTo', new Date(filterState.createdAtTo).toISOString())
        }
        if (filterState.createdAtNegate) params.append('createdAtNegate', 'true')

        if (filterState.lastLoginFrom) {
            params.append('lastLoginFrom', new Date(filterState.lastLoginFrom).toISOString())
        }
        if (filterState.lastLoginTo) {
            params.append('lastLoginTo', new Date(filterState.lastLoginTo).toISOString())
        }
        if (filterState.lastLoginNegate) params.append('lastLoginNegate', 'true')

        // Boolean filters
        if (filterState.hasInvestorProfile !== undefined) {
            params.append('hasInvestorProfile', String(filterState.hasInvestorProfile))
            if (filterState.hasInvestorProfileNegate) params.append('hasInvestorProfileNegate', 'true')
        }
        if (filterState.hasStartups !== undefined) {
            params.append('hasStartups', String(filterState.hasStartups))
            if (filterState.hasStartupsNegate) params.append('hasStartupsNegate', 'true')
        }
        if (filterState.isSuspended !== undefined) {
            params.append('isSuspended', String(filterState.isSuspended))
            if (filterState.isSuspendedNegate) params.append('isSuspendedNegate', 'true')
        }

        // New filters
        if (filterState.minWarningCount !== undefined) {
            params.append('minWarningCount', String(filterState.minWarningCount))
            if (filterState.minWarningCountNegate) params.append('minWarningCountNegate', 'true')
        }
        if (filterState.hasActiveSession !== undefined) {
            params.append('hasActiveSession', String(filterState.hasActiveSession))
            if (filterState.hasActiveSessionNegate) params.append('hasActiveSessionNegate', 'true')
        }

        const queryString = params.toString()
        return queryString ? `${baseUrl}&${queryString}` : baseUrl
    }

    // Fetch Data
    const fetchData = async () => {
        setIsLoading(true)
        try {
            let url = `/admin/users?page=${pageIndex}&size=${pageSize}`
            if (debouncedQuery) {
                url += `&query=${encodeURIComponent(debouncedQuery)}`
            }

            // Add advanced filters
            url = buildFilterUrl(url, appliedFilters)

            const response = await api.get(url)
            const pageData = response.data

            setData(pageData.content)
            setPageCount(pageData.totalPages)
            setTotalUsers(pageData.totalElements)

        } catch (error) {
            console.error("Failed to fetch users", error)
            toast.error('Failed to load users')
        } finally {
            setIsLoading(false)
        }
    }

    // Apply filters handler
    const handleApplyFilters = () => {
        setAppliedFilters({ ...filters })
        setPagination(prev => ({ ...prev, pageIndex: 0 }))
    }

    // Clear filters handler
    const handleClearFilters = () => {
        setFilters({})
        setAppliedFilters({})
        setPagination(prev => ({ ...prev, pageIndex: 0 }))
    }

    useEffect(() => {
        fetchData()
    }, [pageIndex, pageSize, debouncedQuery, appliedFilters])

    const getIcon = (role: string) => {
        const r = role?.toLowerCase() || ''
        if (r === 'investor') return <TrendingUp className="h-4 w-4 text-emerald-500" />
        if (r === 'startup' || r === 'startup_owner') return <Building2 className="h-4 w-4 text-purple-500" />
        if (r === 'admin') return <Shield className="h-4 w-4 text-yellow-500" />
        return <UserIcon className="h-4 w-4 text-blue-500" />
    }

    const getStatusBadge = (status: string) => {
        const styles: Record<string, string> = {
            ACTIVE: 'bg-green-500/10 text-green-500 border-green-500/30',
            SUSPENDED: 'bg-orange-500/10 text-orange-500 border-orange-500/30',
            BANNED: 'bg-red-500/10 text-red-500 border-red-500/30',
            DELETED: 'bg-gray-500/10 text-gray-500 border-gray-500/30',
            PENDING: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/30',
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-semibold border ${styles[status] || styles.PENDING}`}>
                {status}
            </span>
        )
    }

    const getRoleBadge = (role: string) => {
        const styles: Record<string, string> = {
            ADMIN: 'bg-yellow-500/10 text-yellow-600 border-yellow-500/30',
            INVESTOR: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/30',
            STARTUP_OWNER: 'bg-purple-500/10 text-purple-600 border-purple-500/30',
            USER: 'bg-blue-500/10 text-blue-600 border-blue-500/30',
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium border ${styles[role] || styles.USER}`}>
                {role}
            </span>
        )
    }

    // Action Handlers
    const handleViewDetails = (userId: string) => {
        setSelectedUserId(userId)
        setDetailsModalOpen(true)
    }

    const handleSuspend = (userId: string) => {
        setSelectedUserId(userId)
        setSuspendDialogOpen(true)
    }

    const handleWarn = (userId: string) => {
        setSelectedUserId(userId)
        setWarnDialogOpen(true)
    }

    const handleDelete = (userId: string) => {
        setSelectedUserId(userId)
        setDeleteDialogOpen(true)
    }

    const handleActionComplete = () => {
        fetchData()
    }

    // Columns Definition with more details
    const columns = useMemo<ColumnDef<UserData>[]>(
        () => [
            {
                accessorKey: 'email',
                header: 'User',
                cell: ({ row }) => {
                    const user = row.original
                    return (
                        <div className="flex items-center gap-3">
                            <div className="h-9 w-9 rounded-full bg-primary/10 flex items-center justify-center">
                                {getIcon(user.role)}
                            </div>
                            <div>
                                <p className="font-medium">{user.email}</p>
                                <p className="text-xs text-muted-foreground">
                                    {user.authProvider || 'Email'} auth
                                </p>
                            </div>
                        </div>
                    )
                },
            },
            {
                accessorKey: 'role',
                header: 'Role',
                cell: ({ row }) => getRoleBadge(row.original.role),
            },
            {
                accessorKey: 'status',
                header: 'Status',
                cell: ({ row }) => {
                    const user = row.original
                    return (
                        <div className="space-y-1">
                            {getStatusBadge(user.status)}
                            {user.status === 'SUSPENDED' && user.suspendedUntil && (
                                <p className="text-xs text-muted-foreground flex items-center gap-1">
                                    <Clock className="h-3 w-3" />
                                    Until {formatDate(user.suspendedUntil)}
                                </p>
                            )}
                        </div>
                    )
                },
            },
            {
                accessorKey: 'lastLoginAt',
                header: 'Last Login',
                cell: ({ row }) => {
                    const lastLogin = row.original.lastLoginAt
                    return (
                        <span className="text-sm text-muted-foreground">
                            {lastLogin ? formatDate(lastLogin) : 'Never'}
                        </span>
                    )
                },
            },
            {
                accessorKey: 'createdAt',
                header: 'Joined',
                cell: info => (
                    <span className="text-sm text-muted-foreground">
                        {formatDate(info.getValue() as string)}
                    </span>
                ),
            },
            {
                id: 'actions',
                header: 'Actions',
                cell: ({ row }) => {
                    const user = row.original
                    const isAdmin = user.role === 'ADMIN'
                    const isSuspended = user.status === 'SUSPENDED' || user.status === 'BANNED'

                    return (
                        <div className="flex items-center gap-1">
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleViewDetails(user.id)}
                                title="View Details"
                            >
                                <Eye className="h-4 w-4" />
                            </Button>
                            {!isAdmin && (
                                <>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => handleWarn(user.id)}
                                        title="Issue Warning"
                                        className="text-yellow-500 hover:text-yellow-600 hover:bg-yellow-500/10"
                                    >
                                        <AlertTriangle className="h-4 w-4" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => handleSuspend(user.id)}
                                        title={isSuspended ? "Already Suspended" : "Suspend User"}
                                        className="text-orange-500 hover:text-orange-600 hover:bg-orange-500/10"
                                        disabled={isSuspended}
                                    >
                                        <Ban className="h-4 w-4" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => handleDelete(user.id)}
                                        title="Delete User"
                                        className="text-red-500 hover:text-red-600 hover:bg-red-500/10"
                                    >
                                        <Trash2 className="h-4 w-4" />
                                    </Button>
                                </>
                            )}
                        </div>
                    )
                },
            },
        ],
        []
    )

    const table = useReactTable({
        data,
        columns,
        pageCount,
        state: {
            pagination,
        },
        onPaginationChange: setPagination,
        getCoreRowModel: getCoreRowModel(),
        manualPagination: true,
    })

    return (
        <>
            {/* Advanced Filters Panel */}
            <UserFilters
                filters={filters}
                onFiltersChange={setFilters}
                onApply={handleApplyFilters}
                onClear={handleClearFilters}
            />

            <Card>
                <CardHeader>
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="flex items-center gap-2">
                            {isLoading && <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />}
                            <span className="text-sm text-muted-foreground">
                                {totalUsers} {totalUsers === 1 ? 'user' : 'users'} found
                            </span>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-2">
                            {/* Export Dropdown */}
                            <Dialog open={exportDialogOpen} onOpenChange={setExportDialogOpen}>
                                <DialogTrigger asChild>
                                    <Button variant="outline" className="gap-2">
                                        <Download className="h-4 w-4" />
                                        Export
                                    </Button>
                                </DialogTrigger>
                                <DialogContent className="sm:max-w-[425px] md:max-w-[600px] max-h-[90vh] overflow-y-auto">
                                    <DialogHeader>
                                        <DialogTitle>Export Users</DialogTitle>
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
                                                            Export only visible {data.length} rows
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

                            <div className="relative">
                                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Search by email..."
                                    className="pl-8 w-full sm:w-[250px]"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                />
                            </div>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="rounded-md border">
                        <Table>
                            <TableHeader>
                                {table.getHeaderGroups().map(headerGroup => (
                                    <TableRow key={headerGroup.id}>
                                        {headerGroup.headers.map(header => (
                                            <TableHead key={header.id}>
                                                {header.isPlaceholder
                                                    ? null
                                                    : flexRender(
                                                        header.column.columnDef.header,
                                                        header.getContext()
                                                    )}
                                            </TableHead>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableHeader>
                            <TableBody>
                                {isLoading ? (
                                    Array.from({ length: pageSize }).map((_, i) => (
                                        <TableRow key={i}>
                                            <TableCell colSpan={columns.length}><div className="h-12 bg-muted/20 rounded animate-pulse" /></TableCell>
                                        </TableRow>
                                    ))
                                ) : table.getRowModel().rows.length > 0 ? (
                                    table.getRowModel().rows.map(row => (
                                        <TableRow key={row.id} className="hover:bg-muted/50">
                                            {row.getVisibleCells().map(cell => (
                                                <TableCell key={cell.id}>
                                                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                                </TableCell>
                                            ))}
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={columns.length} className="h-24 text-center">
                                            No users found.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </div>

                    {/* Pagination Controls */}
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 px-2 py-4">
                        <div className="text-sm text-muted-foreground">
                            Page {table.getState().pagination.pageIndex + 1} of {table.getPageCount() || 1}
                        </div>
                        <div className="flex flex-wrap items-center gap-4 lg:gap-6">
                            <div className="flex items-center space-x-2">
                                <p className="text-sm font-medium">Rows per page</p>
                                <Select
                                    value={`${table.getState().pagination.pageSize}`}
                                    onValueChange={(value) => {
                                        table.setPageSize(Number(value))
                                    }}
                                >
                                    <SelectTrigger className="h-8 w-[70px]">
                                        <SelectValue placeholder={table.getState().pagination.pageSize} />
                                    </SelectTrigger>
                                    <SelectContent side="top">
                                        {[10, 20, 50, 100, 200, 500].map((pageSize) => (
                                            <SelectItem key={pageSize} value={`${pageSize}`}>
                                                {pageSize}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="flex items-center space-x-2">
                                <Button
                                    variant="outline"
                                    className="hidden h-8 w-8 p-0 lg:flex"
                                    onClick={() => table.setPageIndex(0)}
                                    disabled={!table.getCanPreviousPage()}
                                >
                                    <span className="sr-only">Go to first page</span>
                                    <ChevronsLeft className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="outline"
                                    className="h-8 w-8 p-0"
                                    onClick={() => table.previousPage()}
                                    disabled={!table.getCanPreviousPage()}
                                >
                                    <span className="sr-only">Go to previous page</span>
                                    <ChevronLeft className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="outline"
                                    className="h-8 w-8 p-0"
                                    onClick={() => table.nextPage()}
                                    disabled={!table.getCanNextPage()}
                                >
                                    <span className="sr-only">Go to next page</span>
                                    <ChevronRight className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="outline"
                                    className="hidden h-8 w-8 p-0 lg:flex"
                                    onClick={() => table.setPageIndex(table.getPageCount() - 1)}
                                    disabled={!table.getCanNextPage()}
                                >
                                    <span className="sr-only">Go to last page</span>
                                    <ChevronsRight className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Modals */}
            <UserDetailsModal
                userId={selectedUserId}
                open={detailsModalOpen}
                onOpenChange={setDetailsModalOpen}
                onAction={handleActionComplete}
            />

            <SuspendUserDialog
                userId={selectedUserId}
                open={suspendDialogOpen}
                onOpenChange={setSuspendDialogOpen}
                onSuccess={handleActionComplete}
            />

            <WarnUserDialog
                userId={selectedUserId}
                open={warnDialogOpen}
                onOpenChange={setWarnDialogOpen}
                onSuccess={handleActionComplete}
            />

            <DeleteUserDialog
                userId={selectedUserId}
                open={deleteDialogOpen}
                onOpenChange={setDeleteDialogOpen}
                onSuccess={handleActionComplete}
            />
        </>
    )
}

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
import { formatDate, formatTimeAgo } from '../../lib/utils'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '../../components/ui/dropdown-menu'
import {
    Building2, TrendingUp, User as UserIcon, Loader2, ChevronLeft,
    ChevronRight, ChevronsLeft, ChevronsRight, Search, Eye, Ban,
    AlertTriangle, Trash2, Shield, Clock, Download, RotateCcw, UserPlus, MoreVertical, Calendar,
    Filter, Table as TableIcon, Database, BadgeCheck
} from 'lucide-react'
import { toast } from 'sonner'
import api from '../../lib/axios'
import { UserDetailsModal } from './UserDetailsModal'
import { SuspendUserDialog } from './SuspendUserDialog'
import { WarnUserDialog } from './WarnUserDialog'
import { DeleteUserDialog } from './DeleteUserDialog'
import { RestoreUserDialog } from './RestoreUserDialog'
import { UserStatusDialog } from './UserStatusDialog'
import { AddAdminDialog } from './AddAdminDialog'
import { UserFilters, FilterState } from './UserFilters'

import {
    Dialog, DialogContent, DialogDescription,
    DialogFooter, DialogHeader, DialogTitle, DialogTrigger
} from '../../components/ui/dialog'
import { Label } from '../../components/ui/label'
import { Checkbox } from '../../components/ui/checkbox'
import { RadioGroup, RadioGroupItem } from '../../components/ui/radio-group'
import { useAuth } from '../../contexts/AuthContext'

// ... existing imports ...

interface UserData {
    id: string
    email: string
    username: string
    firstName: string | null
    lastName: string | null
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
    isInvestorVerified?: boolean
}

const EXPORTABLE_COLUMNS = [
    { id: 'id', label: 'ID' },
    { id: 'email', label: 'Email' },
    { id: 'username', label: 'Username' },
    { id: 'firstName', label: 'First Name' },
    { id: 'lastName', label: 'Last Name' },
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

    const { user: currentUser } = useAuth()
    const isSuperAdmin = currentUser?.role === 'SUPER_ADMIN'

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
    const [restoreDialogOpen, setRestoreDialogOpen] = useState(false)
    const [statusChangeDialogOpen, setStatusChangeDialogOpen] = useState(false)
    const [statusChangeInitialStatus, setStatusChangeInitialStatus] = useState<string>('ACTIVE')
    const [addAdminOpen, setAddAdminOpen] = useState(false)

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
        if (filterState.isMemberOfStartups !== undefined) {
            params.append('isMemberOfStartups', String(filterState.isMemberOfStartups))
            if (filterState.isMemberOfStartupsNegate) params.append('isMemberOfStartupsNegate', 'true')
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
        if (r === 'investor') return <TrendingUp className="h-4 w-4 text-emerald-600" />
        if (r === 'startup' || r === 'startup_owner') return <Building2 className="h-4 w-4 text-indigo-600" />
        if (r === 'admin') return <Shield className="h-4 w-4 text-amber-600" />
        if (r === 'super_admin') return <Shield className="h-4 w-4 text-rose-600" />
        return <UserIcon className="h-4 w-4 text-slate-600" />
    }

    const getStatusBadge = (status: string) => {
        const styles: Record<string, string> = {
            ACTIVE: 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20',
            SUSPENDED: 'bg-orange-500/10 text-orange-500 border-orange-500/20',
            BANNED: 'bg-red-500/10 text-red-500 border-red-500/20',
            DELETED: 'bg-slate-500/10 text-slate-500 border-slate-500/20',
            PENDING: 'bg-amber-500/10 text-amber-500 border-amber-500/20',
        }
        return (
            <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium border ${styles[status] || styles.PENDING}`}>
                {status}
            </span>
        )
    }

    const getRoleBadge = (role: string) => {
        const styles: Record<string, string> = {
            SUPER_ADMIN: 'bg-rose-500/10 text-rose-500 border-rose-500/20',
            ADMIN: 'bg-amber-500/10 text-amber-500 border-amber-500/20',
            INVESTOR: 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20',
            STARTUP_OWNER: 'bg-indigo-500/10 text-indigo-500 border-indigo-500/20',
            USER: 'bg-slate-500/10 text-slate-500 border-slate-500/20',
        }
        return (
            <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium border ${styles[role] || styles.USER}`}>
                {role.replace('_', ' ')}
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

    const handleRestore = (userId: string) => {
        setSelectedUserId(userId)
        setRestoreDialogOpen(true)
    }

    const handleStatusChange = (userId: string) => {
        // Find user to get current status
        const user = data.find(u => u.id === userId)
        if (user) {
            setStatusChangeInitialStatus(user.status)
        }
        setSelectedUserId(userId)
        setStatusChangeDialogOpen(true)
    }

    const handleActionComplete = () => {
        fetchData()
    }

    // Columns Definition with more details
    const columns = useMemo<ColumnDef<UserData>[]>(
        () => [
            {
                accessorKey: 'email',
                header: 'User & Account',
                cell: ({ row }) => {
                    const user = row.original
                    return (
                        <div className="flex items-center gap-3 py-1">
                            <div className={`h-10 w-10 rounded-xl flex items-center justify-center shadow-sm border ${user.role === 'INVESTOR' ? 'bg-emerald-50 border-emerald-100 dark:bg-emerald-900/20 dark:border-emerald-800' :
                                user.role === 'STARTUP_OWNER' ? 'bg-indigo-50 border-indigo-100 dark:bg-indigo-900/20 dark:border-indigo-800' :
                                    user.role === 'ADMIN' ? 'bg-amber-50 border-amber-100 dark:bg-amber-900/20 dark:border-amber-800' :
                                        user.role === 'SUPER_ADMIN' ? 'bg-rose-50 border-rose-100 dark:bg-rose-900/20 dark:border-rose-800' :
                                            'bg-slate-50 border-slate-100 dark:bg-slate-900/20 dark:border-slate-800'
                                }`}>
                                {getIcon(user.role)}
                            </div>
                            <div>
                                <div className="flex items-center gap-2">
                                    <p className="font-semibold text-sm text-foreground">{user.email}</p>
                                </div>
                                <div className="flex items-center gap-2 text-xs text-muted-foreground mt-0.5">
                                    <span className="capitalize">{user.authProvider?.toLowerCase() || 'email'}</span>
                                    <span>•</span>
                                    <span>{user.firstName ? `${user.firstName} ${user.lastName || ''}` : 'No Name'}</span>
                                    {user.isInvestorVerified && (
                                        <div className="flex items-center gap-1 text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/20 px-1.5 py-0.5 rounded-full" title="Verified Investor">
                                            <BadgeCheck className="h-3 w-3" />
                                            <span className="text-[10px] font-semibold">Verified</span>
                                        </div>
                                    )}
                                </div>
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
                        <div className="space-y-1.5">
                            {getStatusBadge(user.status)}
                            {user.status === 'SUSPENDED' && user.suspendedUntil && (
                                <p className="text-[10px] text-orange-600 dark:text-orange-400 flex items-center gap-1 bg-orange-50 dark:bg-orange-900/20 px-1.5 py-0.5 rounded w-fit">
                                    <Clock className="h-3 w-3" />
                                    Until {formatDate(user.suspendedUntil)}
                                </p>
                            )}
                        </div>
                    )
                },
            },
            {
                id: 'activity',
                header: 'Activity',
                cell: ({ row }) => {
                    const user = row.original
                    return (
                        <div className="flex flex-col gap-1.5 text-xs text-muted-foreground">
                            <div className="flex items-center gap-2">
                                <Calendar className="h-3 w-3 text-slate-400" />
                                <span>Joined {formatTimeAgo(user.createdAt)}</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Clock className="h-3 w-3 text-slate-400" />
                                <span className={user.lastLoginAt ? "text-emerald-600 dark:text-emerald-400 font-medium" : ""}>
                                    {user.lastLoginAt ? `Active ${formatTimeAgo(user.lastLoginAt)}` : 'Never logged in'}
                                </span>
                            </div>
                        </div>
                    )
                },
            },
            {
                id: 'actions',
                header: 'Actions',
                cell: ({ row }) => {
                    const user = row.original
                    const isAdmin = user.role === 'ADMIN'
                    const isTargetSuperAdmin = user.role === 'SUPER_ADMIN'
                    const isSuspended = user.status === 'SUSPENDED' || user.status === 'BANNED'

                    const canManage = (() => {
                        if (isTargetSuperAdmin) return false;
                        if (isAdmin) return isSuperAdmin;
                        return true;
                    })()

                    return (
                        <div className="flex items-center justify-end gap-1" onClick={(e) => e.stopPropagation()}>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8 text-muted-foreground hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/20"
                                onClick={() => handleViewDetails(user.id)}
                                title="View Details"
                            >
                                <Eye className="h-4 w-4 text-blue-400" />
                            </Button>

                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-8 w-8 p-0 text-muted-foreground hover:text-foreground"
                                        disabled={!canManage}
                                    >
                                        <span className="sr-only">Open menu</span>
                                        <MoreVertical className="h-4 w-4" />
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end" className="w-48">
                                    <DropdownMenuLabel>Manage User</DropdownMenuLabel>
                                    <DropdownMenuSeparator />

                                    <DropdownMenuItem onClick={() => handleStatusChange(user.id)}>
                                        <Shield className="mr-2 h-4 w-4 text-indigo-500" />
                                        Change Status
                                    </DropdownMenuItem>

                                    <DropdownMenuItem onClick={() => handleWarn(user.id)}>
                                        <AlertTriangle className="mr-2 h-4 w-4 text-amber-500" />
                                        Issue Warning
                                    </DropdownMenuItem>

                                    <DropdownMenuItem
                                        onClick={() => handleSuspend(user.id)}
                                        disabled={isSuspended}
                                        className="text-orange-600 focus:text-orange-700 focus:bg-orange-50 dark:focus:bg-orange-900/20"
                                    >
                                        <Ban className="mr-2 h-4 w-4" />
                                        {isSuspended ? "Already Suspended" : "Suspend User"}
                                    </DropdownMenuItem>

                                    {user.status === 'DELETED' && (
                                        <DropdownMenuItem onClick={() => handleRestore(user.id)}>
                                            <RotateCcw className="mr-2 h-4 w-4 text-emerald-500" />
                                            Restore User
                                        </DropdownMenuItem>
                                    )}

                                    <DropdownMenuSeparator />

                                    <DropdownMenuItem
                                        onClick={() => handleDelete(user.id)}
                                        className="text-red-600 focus:text-red-700 focus:bg-red-50 dark:focus:bg-red-900/20"
                                    >
                                        <Trash2 className="mr-2 h-4 w-4" />
                                        Delete User
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>

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
        <div className="space-y-6">
            <UserStatusDialog
                open={statusChangeDialogOpen}
                onOpenChange={setStatusChangeDialogOpen}
                userId={selectedUserId}
                currentStatus={statusChangeInitialStatus}
                onSuccess={handleActionComplete}
            />
            {/* Advanced Filters Panel */}
            <UserFilters
                filters={filters}
                onFiltersChange={setFilters}
                onApply={handleApplyFilters}
                onClear={handleClearFilters}
            />

            <Card>
                <CardHeader className="bg-white/50 dark:bg-slate-900/50 border-b border-border/50 pb-4">
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="flex items-center gap-3">
                            <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
                                <UserIcon className="h-5 w-5 text-primary" />
                            </div>
                            <div>
                                <h2 className="text-lg font-bold tracking-tight">Users Management</h2>
                                <div className="flex items-center gap-2">
                                    {isLoading && <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />}
                                    <span className="text-xs text-muted-foreground font-medium">
                                        {totalUsers} {totalUsers === 1 ? 'user' : 'users'} total
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-2">
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                                <Input
                                    placeholder="Search by email..."
                                    className="pl-9 w-full sm:w-[250px] bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                />
                            </div>

                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    size="icon"
                                    onClick={fetchData}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 hover:bg-slate-50"
                                    title="Refresh List"
                                >
                                    <RotateCcw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
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
                                                    Export Users
                                                </DialogTitle>
                                                <DialogDescription className="text-white/80">
                                                    Download user data as a CSV file. Select your scope and custom columns below.
                                                </DialogDescription>
                                            </DialogHeader>
                                        </div>

                                        <div className="overflow-y-auto p-6 space-y-8 custom-scrollbar">
                                            {/* Scope Selection */}
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
                                                                Export only visible {data.length} rows
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
                                                                All records matching filters
                                                            </span>
                                                        </div>
                                                    </label>
                                                </RadioGroup>
                                            </div>

                                            {/* Column Selection */}
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
                                                            <Label
                                                                htmlFor={`col-${col.id}`}
                                                                className="text-sm text-muted-foreground group-hover:text-foreground cursor-pointer truncate flex-1 font-medium transition-colors"
                                                                title={col.label}
                                                            >
                                                                {col.label}
                                                            </Label>
                                                        </div>
                                                    ))}
                                                </div>
                                                <p className="text-xs text-muted-foreground text-right px-1 flex justify-end gap-1">
                                                    <span className="font-semibold text-foreground">{selectedColumns.length}</span> columns selected
                                                </p>
                                            </div>
                                        </div>

                                        <DialogFooter className="border-t p-6 bg-muted/20 shrink-0">
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
                                        </DialogFooter>
                                    </DialogContent>
                                </Dialog>

                                <Button
                                    className="gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md transition-all hover:shadow-lg"
                                    onClick={() => setAddAdminOpen(true)}
                                    disabled={!isSuperAdmin}
                                    title={!isSuperAdmin ? "Only Super Admin can create new admins" : "Create New Admin"}
                                >
                                    <UserPlus className="h-4 w-4" />
                                    <span className="hidden sm:inline">New Admin</span>
                                </Button>
                            </div>
                        </div>
                    </div>
                </CardHeader>
                <CardContent>
                    <div className="rounded-md border mt-4">
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
                                            <TableCell colSpan={columns.length}><div className="h-10 bg-muted/20 rounded animate-pulse" /></TableCell>
                                        </TableRow>
                                    ))
                                ) : table.getRowModel().rows.length > 0 ? (
                                    table.getRowModel().rows.map(row => (
                                        <TableRow
                                            key={row.id}
                                            className="cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800/50 transition-colors"
                                            onClick={() => handleViewDetails(row.original.id)}
                                        >
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
                            Showing {table.getState().pagination.pageIndex * table.getState().pagination.pageSize + 1} to {Math.min((table.getState().pagination.pageIndex + 1) * table.getState().pagination.pageSize, totalUsers)} of {totalUsers} entries
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
                onSuccess={fetchData}
            />

            <AddAdminDialog
                open={addAdminOpen}
                onOpenChange={setAddAdminOpen}
                onSuccess={handleActionComplete}
            />

            <RestoreUserDialog
                open={restoreDialogOpen}
                onOpenChange={setRestoreDialogOpen}
                userId={selectedUserId}
                onSuccess={handleActionComplete}
            />
        </div>
    )
}

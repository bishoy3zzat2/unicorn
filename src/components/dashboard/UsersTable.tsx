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
import { Building2, TrendingUp, User as UserIcon, Loader2, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Search, Eye, Ban, AlertTriangle, Trash2, Shield, Clock } from 'lucide-react'
import { toast } from 'sonner'
import api from '../../lib/axios'
import { UserDetailsModal } from './UserDetailsModal'
import { SuspendUserDialog } from './SuspendUserDialog'
import { WarnUserDialog } from './WarnUserDialog'
import { DeleteUserDialog } from './DeleteUserDialog'

interface UserData {
    id: string
    email: string
    role: string
    status: string
    authProvider?: string
    createdAt: string
    lastLoginAt?: string | null
    suspendedAt?: string | null
    suspendedUntil?: string | null
}

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

    // Modal States
    const [selectedUserId, setSelectedUserId] = useState<string | null>(null)
    const [detailsModalOpen, setDetailsModalOpen] = useState(false)
    const [suspendDialogOpen, setSuspendDialogOpen] = useState(false)
    const [warnDialogOpen, setWarnDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

    // Debounce search query
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedQuery(searchQuery)
            setPagination(prev => ({ ...prev, pageIndex: 0 }))
        }, 500)
        return () => clearTimeout(handler)
    }, [searchQuery])

    // Fetch Data
    const fetchData = async () => {
        setIsLoading(true)
        try {
            let url = `/admin/users?page=${pageIndex}&size=${pageSize}`
            if (debouncedQuery) {
                url += `&query=${encodeURIComponent(debouncedQuery)}`
            }

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

    useEffect(() => {
        fetchData()
    }, [pageIndex, pageSize, debouncedQuery])

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

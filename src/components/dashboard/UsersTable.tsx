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
import { Building2, TrendingUp, User as UserIcon, Loader2, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Search } from 'lucide-react'
import { toast } from 'sonner'
import api from '../../lib/axios'

interface UserData {
    id: string
    email: string
    role: string
    status: string
    createdAt: string
}

export function UsersTable() {
    // Pagination State
    const [{ pageIndex, pageSize }, setPagination] = useState<PaginationState>({
        pageIndex: 0,
        pageSize: 20, // Default as requested
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

    // Debounce search query
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedQuery(searchQuery)
            setPagination(prev => ({ ...prev, pageIndex: 0 })) // Reset to first page on search
        }, 500)
        return () => clearTimeout(handler)
    }, [searchQuery])

    // Fetch Data
    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true)
            try {
                // Backend expects 0-indexed page
                let url = `/admin/users?page=${pageIndex}&size=${pageSize}`
                if (debouncedQuery) {
                    url += `&query=${encodeURIComponent(debouncedQuery)}`
                }

                const response = await api.get(url)

                // Spring Boot Page response structure:
                // { content: [], totalPages: number, totalElements: number, ... }
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

        fetchData()
    }, [pageIndex, pageSize, debouncedQuery])

    const getIcon = (role: string) => {
        const r = role.toLowerCase()
        if (r === 'investor') return <TrendingUp className="h-4 w-4 text-emerald-500" />
        if (r === 'startup') return <Building2 className="h-4 w-4 text-purple-500" />
        return <UserIcon className="h-4 w-4 text-blue-500" />
    }

    // Columns Definition
    const columns = useMemo<ColumnDef<UserData>[]>(
        () => [
            {
                accessorKey: 'email',
                header: 'Email',
                cell: info => <span className="font-medium">{info.getValue() as string}</span>,
            },
            {
                accessorKey: 'role',
                header: 'Role',
                cell: info => (
                    <div className="flex items-center gap-2">
                        {getIcon(info.getValue() as string)}
                        <span className="capitalize">{info.getValue() as string}</span>
                    </div>
                ),
            },
            {
                accessorKey: 'status',
                header: 'Status',
                cell: info => {
                    const status = info.getValue() as string
                    return (
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${status === 'ACTIVE' ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'
                            }`}>
                            {status}
                        </span>
                    )
                },
            },
            {
                accessorKey: 'createdAt',
                header: 'Join Date',
                cell: info => formatDate(info.getValue() as string),
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
                        {/* Search Input */}
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
                                        <TableCell colSpan={columns.length}><div className="h-8 bg-muted/20 rounded animate-pulse" /></TableCell>
                                    </TableRow>
                                ))
                            ) : table.getRowModel().rows.length > 0 ? (
                                table.getRowModel().rows.map(row => (
                                    <TableRow key={row.id}>
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
                                        No results.
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>

                {/* Pagination Controls */}
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 px-2 py-4">
                    <div className="text-sm text-muted-foreground">
                        Page {table.getState().pagination.pageIndex + 1} of {table.getPageCount()}
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
    )
}

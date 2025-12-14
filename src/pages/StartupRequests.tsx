import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import {
    Rocket,
    Loader2,
    AlertCircle,
    RefreshCcw,
    Search,
    UserCog,
    Building2,
    DollarSign
} from 'lucide-react'
import { Alert, AlertDescription } from '../components/ui/alert'
import {
    fetchAllStartups,
    transferStartupOwnership,
    searchUsers,
    Startup,
    User
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

export function StartupRequests() {
    const [startups, setStartups] = useState<Startup[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

    // Transfer ownership state
    const [transferDialog, setTransferDialog] = useState<{ open: boolean; startup: Startup | null }>({
        open: false,
        startup: null
    })
    const [userSearchQuery, setUserSearchQuery] = useState('')
    const [searchResults, setSearchResults] = useState<User[]>([])
    const [selectedUser, setSelectedUser] = useState<User | null>(null)
    const [searchingUsers, setSearchingUsers] = useState(false)
    const [transferring, setTransferring] = useState(false)

    useEffect(() => {
        loadStartups()
    }, [page])

    async function loadStartups() {
        try {
            setLoading(true)
            setError(null)
            const data = await fetchAllStartups(page, 20)
            setStartups(data.content)
            setTotalPages(data.totalPages)
        } catch (err) {
            console.error('Failed to fetch startups:', err)
            setError(err instanceof Error ? err.message : 'Failed to load startups')
        } finally {
            setLoading(false)
        }
    }

    async function handleUserSearch(query: string) {
        setUserSearchQuery(query)
        if (query.length < 2) {
            setSearchResults([])
            return
        }

        try {
            setSearchingUsers(true)
            const data = await searchUsers(query)
            setSearchResults(data.content)
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

            // Refresh startups list
            await loadStartups()
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

    function getStatusBadge(status: string) {
        const styles: Record<string, string> = {
            APPROVED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
            PENDING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400',
            REJECTED: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
        }
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium ${styles[status] || styles.PENDING}`}>
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
                <Button variant="outline" size="sm" onClick={loadStartups}>
                    <RefreshCcw className="h-4 w-4 mr-2" />
                    Refresh
                </Button>
            </div>

            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            {/* Startups Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Startups</CardTitle>
                    <CardDescription>
                        {startups.length} startups found
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {startups.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-12">
                            <Rocket className="h-12 w-12 text-muted-foreground mb-4" />
                            <p className="text-muted-foreground">No startups found</p>
                        </div>
                    ) : (
                        <div className="relative overflow-x-auto">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr className="border-b">
                                        <th className="text-left py-3 px-4 font-medium">Startup</th>
                                        <th className="text-left py-3 px-4 font-medium">Owner</th>
                                        <th className="text-left py-3 px-4 font-medium">Industry</th>
                                        <th className="text-left py-3 px-4 font-medium">Stage</th>
                                        <th className="text-left py-3 px-4 font-medium">Funding Goal</th>
                                        <th className="text-left py-3 px-4 font-medium">Status</th>
                                        <th className="text-left py-3 px-4 font-medium">Created</th>
                                        <th className="text-right py-3 px-4 font-medium">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {startups.map((startup) => (
                                        <tr key={startup.id} className="border-b hover:bg-muted/50">
                                            <td className="py-3 px-4">
                                                <div className="flex items-center gap-3">
                                                    {startup.logoUrl ? (
                                                        <img
                                                            src={startup.logoUrl}
                                                            alt={startup.name}
                                                            className="h-8 w-8 rounded-full object-cover"
                                                        />
                                                    ) : (
                                                        <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center">
                                                            <Building2 className="h-4 w-4 text-primary" />
                                                        </div>
                                                    )}
                                                    <div>
                                                        <p className="font-medium">{startup.name}</p>
                                                        {startup.tagline && (
                                                            <p className="text-xs text-muted-foreground truncate max-w-xs">
                                                                {startup.tagline}
                                                            </p>
                                                        )}
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground">
                                                {startup.ownerEmail}
                                            </td>
                                            <td className="py-3 px-4">
                                                {startup.industry || '-'}
                                            </td>
                                            <td className="py-3 px-4">
                                                {getStageBadge(startup.stage)}
                                            </td>
                                            <td className="py-3 px-4">
                                                <div className="flex items-center gap-1">
                                                    <DollarSign className="h-3 w-3 text-muted-foreground" />
                                                    {formatCurrency(startup.fundingGoal || 0)}
                                                </div>
                                            </td>
                                            <td className="py-3 px-4">
                                                {getStatusBadge(startup.status)}
                                            </td>
                                            <td className="py-3 px-4 text-muted-foreground">
                                                {formatDate(startup.createdAt)}
                                            </td>
                                            <td className="py-3 px-4 text-right">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => setTransferDialog({ open: true, startup })}
                                                >
                                                    <UserCog className="h-4 w-4 mr-1" />
                                                    Transfer
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex items-center justify-center gap-2 mt-4">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                            >
                                Previous
                            </Button>
                            <span className="text-sm text-muted-foreground">
                                Page {page + 1} of {totalPages}
                            </span>
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page >= totalPages - 1}
                            >
                                Next
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>

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
        </div>
    )
}

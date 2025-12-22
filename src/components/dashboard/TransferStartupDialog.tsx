import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Search, Loader2, UserCog, CheckCircle2, Building2 } from 'lucide-react'
import { toast } from 'sonner'
import { searchUsers, transferStartupOwnership } from '@/lib/api'
import { Startup, User } from '@/types'
import { formatDate } from '@/lib/utils'

interface TransferStartupDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    startup: Startup | null
    onSuccess: () => void
}

export function TransferStartupDialog({
    open,
    onOpenChange,
    startup,
    onSuccess
}: TransferStartupDialogProps) {
    const [userSearchQuery, setUserSearchQuery] = useState('')
    const [searchResults, setSearchResults] = useState<User[]>([])
    const [selectedUser, setSelectedUser] = useState<User | null>(null)
    const [searchingUsers, setSearchingUsers] = useState(false)
    const [transferring, setTransferring] = useState(false)

    // Reset state when dialog opens/closes
    useEffect(() => {
        if (!open) {
            setUserSearchQuery('')
            setSearchResults([])
            setSelectedUser(null)
        }
    }, [open])

    async function handleUserSearch(query: string) {
        setUserSearchQuery(query)
        if (query.length < 2) {
            setSearchResults([])
            return
        }

        try {
            setSearchingUsers(true)
            // Filter by USER role (excludes Admins & Investors) - looking for potential owners
            const data = await searchUsers(query, 'STARTUP_OWNER')

            // Exclude current owner
            const currentOwnerId = startup?.ownerId
            const filteredUsers = data.content.filter(u => u.id !== currentOwnerId)

            setSearchResults(filteredUsers)
        } catch (err) {
            console.error('Failed to search users:', err)
        } finally {
            setSearchingUsers(false)
        }
    }

    async function handleTransferOwnership() {
        if (!startup || !selectedUser) return

        try {
            setTransferring(true)
            await transferStartupOwnership(startup.id, selectedUser.id)

            toast.success('Ownership transferred', {
                description: `${startup.name} is now owned by ${selectedUser.email}`
            })

            onSuccess()
            onOpenChange(false)
        } catch (err) {
            console.error('Failed to transfer ownership:', err)
            toast.error('Transfer failed', {
                description: err instanceof Error ? err.message : 'Failed to transfer ownership'
            })
        } finally {
            setTransferring(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md p-0 gap-0 overflow-hidden border-border bg-background shadow-lg sm:rounded-2xl">
                <div className="bg-purple-50 dark:bg-purple-900/20 border-b border-purple-100 dark:border-purple-800/50 p-6 flex flex-col items-center text-center">
                    <div className="h-12 w-12 rounded-full bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center mb-4 ring-4 ring-purple-50 dark:ring-purple-900/20">
                        <UserCog className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                    </div>
                    <DialogTitle className="text-xl font-bold text-purple-950 dark:text-purple-100">
                        Transfer Ownership
                    </DialogTitle>
                    <p className="text-sm text-purple-700/80 dark:text-purple-300 mt-1 max-w-xs">
                        Select a new owner for <span className="font-semibold">{startup?.name}</span>. This action cannot be undone.
                    </p>
                </div>

                <div className="p-6 space-y-6">
                    {/* Current Owner Info (Optional context) */}
                    <div className="bg-muted/30 rounded-lg p-3 flex items-center gap-3 border border-border/50">
                        <div className="h-8 w-8 rounded-full bg-background flex items-center justify-center border shadow-sm">
                            <Building2 className="h-4 w-4 text-muted-foreground" />
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="text-xs text-muted-foreground">Current Owner</p>
                            <p className="text-sm font-medium truncate">{startup?.ownerEmail}</p>
                        </div>
                    </div>

                    <div className="space-y-3">
                        {/* Search Input */}
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search new owner by email..."
                                value={userSearchQuery}
                                onChange={(e) => handleUserSearch(e.target.value)}
                                className="pl-9 h-11 bg-muted/30 border-muted-foreground/20 focus-visible:ring-purple-500/20"
                            />
                        </div>

                        {/* Search Results / Selected User */}
                        <div className="min-h-[100px] border rounded-xl overflow-hidden bg-muted/10 relative">
                            {searchingUsers ? (
                                <div className="absolute inset-0 flex items-center justify-center bg-background/50">
                                    <Loader2 className="h-5 w-5 animate-spin text-purple-500" />
                                </div>
                            ) : selectedUser ? (
                                <div className="p-3 bg-purple-50/50 dark:bg-purple-900/10 h-full flex items-center justify-between group">
                                    <div className="flex items-center gap-3">
                                        <div className="h-8 w-8 rounded-full bg-purple-100 dark:bg-purple-900/50 flex items-center justify-center text-purple-600 font-bold border border-purple-200">
                                            {selectedUser.email.charAt(0).toUpperCase()}
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-foreground">{selectedUser.email}</p>
                                            <p className="text-xs text-muted-foreground">Selected as new owner</p>
                                        </div>
                                    </div>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="h-7 px-2 text-muted-foreground hover:text-destructive"
                                        onClick={() => setSelectedUser(null)}
                                    >
                                        Change
                                    </Button>
                                </div>
                            ) : searchResults.length > 0 ? (
                                <div className="divide-y max-h-[180px] overflow-y-auto">
                                    {searchResults.map((user) => (
                                        <button
                                            key={user.id}
                                            onClick={() => setSelectedUser(user)}
                                            className="w-full text-left px-4 py-3 hover:bg-purple-50 dark:hover:bg-purple-900/10 transition-colors flex items-center justify-between group"
                                        >
                                            <div>
                                                <p className="font-medium text-sm">{user.email}</p>
                                                <p className="text-[10px] text-muted-foreground uppercase tracking-wide">
                                                    {user.role} • Joined {formatDate(user.createdAt)}
                                                </p>
                                            </div>
                                            <CheckCircle2 className="h-4 w-4 text-purple-500 opacity-0 group-hover:opacity-100 transition-opacity" />
                                        </button>
                                    ))}
                                </div>
                            ) : userSearchQuery.length >= 2 ? (
                                <div className="flex flex-col items-center justify-center h-[100px] text-muted-foreground p-4 text-center">
                                    <p className="text-sm">No users found</p>
                                    <p className="text-xs opacity-70">Try a different email address</p>
                                </div>
                            ) : (
                                <div className="flex flex-col items-center justify-center h-[100px] text-muted-foreground p-4 text-center opacity-60">
                                    <UserCog className="h-8 w-8 mb-2 opacity-50" />
                                    <p className="text-xs">Search for a user to transfer ownership</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <div className="bg-muted/30 p-4 flex items-center justify-end gap-3 border-t">
                    <Button variant="ghost" onClick={() => onOpenChange(false)} className="hover:bg-muted/50">
                        Cancel
                    </Button>
                    <Button
                        onClick={handleTransferOwnership}
                        disabled={!selectedUser || transferring}
                        className="bg-purple-600 hover:bg-purple-700 text-white shadow-md hover:shadow-lg transition-all"
                    >
                        {transferring ? (
                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                            <UserCog className="h-4 w-4 mr-2" />
                        )}
                        Transfer Ownership
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
}

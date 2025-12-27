import { useState, useEffect } from 'react'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "../components/ui/table"
import { Button } from "../components/ui/button"
import { Input } from "../components/ui/input"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "../components/ui/dialog"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../components/ui/select"
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "../components/ui/alert-dialog"
import { Badge } from "../components/ui/badge"
import { Textarea } from "../components/ui/textarea"
import { Label } from "../components/ui/label"
import { Checkbox } from "../components/ui/checkbox"
import {
    Loader2,
    RefreshCcw,
    Search,
    Bell,
    BellRing,
    Clock,
    CheckCircle2,
    TrendingUp,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    Megaphone,
    Trash2,
    BarChart3,
    Smartphone,
    Users,
    User,
    Briefcase,
    Eye,
} from 'lucide-react'
import { formatTimeAgo } from '../lib/utils'
import {
    getNotificationStats,
    getAllNotifications,
    sendAnnouncement,
    cleanupOldNotifications,
    deleteNotification,
    getNotificationTypes,
    searchUsersForAnnouncement,
    NotificationStats,
    NotificationData,
    UserSearchResult,
    NOTIFICATION_TYPE_LABELS,
    getNotificationTypeColor,
    TargetAudience,
} from '../api/notificationApi'
import { toast } from 'sonner'

export function Notifications() {
    const [notifications, setNotifications] = useState<NotificationData[]>([])
    const [stats, setStats] = useState<NotificationStats>({
        total: 0,
        unread: 0,
        readCount: 0,
        readRate: 0,
        todayCount: 0,
        topNotificationType: 'NONE',
        topTypeCount: 0
    })
    const [loading, setLoading] = useState(true)
    const [searchQuery, setSearchQuery] = useState('')
    const [typeFilter, setTypeFilter] = useState<string>('ALL')
    const [readFilter, setReadFilter] = useState<string>('ALL')
    const [notificationTypes, setNotificationTypes] = useState<string[]>([])

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [pageSize, setPageSize] = useState(20)

    // Dialogs
    const [announcementOpen, setAnnouncementOpen] = useState(false)
    const [cleanupOpen, setCleanupOpen] = useState(false)
    const [deleteId, setDeleteId] = useState<string | null>(null)

    // Announcement form
    const [announcementTitle, setAnnouncementTitle] = useState('')
    const [announcementMessage, setAnnouncementMessage] = useState('')
    const [targetAudience, setTargetAudience] = useState<TargetAudience>('ALL_USERS')
    const [specificUserEmail, setSpecificUserEmail] = useState('')
    const [userSearchResults, setUserSearchResults] = useState<UserSearchResult[]>([])
    const [showUserDropdown, setShowUserDropdown] = useState(false)
    const [searchingUsers, setSearchingUsers] = useState(false)
    const [inAppChannel, setInAppChannel] = useState(true)
    const [pushChannel, setPushChannel] = useState(false)
    const [sendingAnnouncement, setSendingAnnouncement] = useState(false)

    // Cleanup
    const [cleanupDays, setCleanupDays] = useState(30)
    const [cleaningUp, setCleaningUp] = useState(false)

    // Deleting
    const [deleting, setDeleting] = useState(false)

    // Details modal
    const [selectedNotification, setSelectedNotification] = useState<NotificationData | null>(null)

    useEffect(() => {
        loadStats()
        loadNotificationTypes()
    }, [])

    useEffect(() => {
        loadNotifications()
    }, [currentPage, pageSize, typeFilter, readFilter])

    const loadStats = async () => {
        try {
            const data = await getNotificationStats()
            setStats(data)
        } catch (error) {
            console.error('Failed to load stats:', error)
        }
    }

    const loadNotificationTypes = async () => {
        try {
            const types = await getNotificationTypes()
            setNotificationTypes(types)
        } catch (error) {
            console.error('Failed to load notification types:', error)
        }
    }

    const loadNotifications = async () => {
        setLoading(true)
        try {
            const type = typeFilter === 'ALL' ? undefined : typeFilter
            const read = readFilter === 'ALL' ? undefined : readFilter === 'READ'
            const data = await getAllNotifications(currentPage, pageSize, type, read)
            setNotifications(data.content)
            setTotalPages(data.totalPages)
            setTotalElements(data.totalElements)
        } catch (error) {
            console.error('Failed to load notifications:', error)
        } finally {
            setLoading(false)
        }
    }

    // Debounced user search
    const handleUserSearch = async (query: string) => {
        setSpecificUserEmail(query)
        if (query.length < 2) {
            setUserSearchResults([])
            setShowUserDropdown(false)
            return
        }
        setSearchingUsers(true)
        try {
            const results = await searchUsersForAnnouncement(query)
            setUserSearchResults(results)
            setShowUserDropdown(results.length > 0)
        } catch {
            setUserSearchResults([])
        } finally {
            setSearchingUsers(false)
        }
    }

    const selectUser = (user: UserSearchResult) => {
        setSpecificUserEmail(user.email)
        setShowUserDropdown(false)
        setUserSearchResults([])
    }

    const handleRefresh = () => {
        loadStats()
        loadNotifications()
    }

    const handleSendAnnouncement = async () => {
        if (!announcementTitle.trim() || !announcementMessage.trim()) {
            toast.error('Please fill in title and message')
            return
        }

        // Validate specific user email if that audience is selected
        if (targetAudience === 'SPECIFIC_USER' && !specificUserEmail.trim()) {
            toast.error('Please enter the user email')
            return
        }

        const channels: ('IN_APP' | 'PUSH')[] = []
        if (inAppChannel) channels.push('IN_APP')
        if (pushChannel) channels.push('PUSH')

        if (channels.length === 0) {
            toast.error('Please select at least one channel')
            return
        }

        setSendingAnnouncement(true)
        try {
            const result = await sendAnnouncement({
                title: announcementTitle,
                message: announcementMessage,
                targetAudience,
                specificUserEmail: targetAudience === 'SPECIFIC_USER' ? specificUserEmail.trim() : undefined,
                channels,
            })
            toast.success(result.message || `Announcement sent to ${result.sentCount} users`)
            setAnnouncementOpen(false)
            setAnnouncementTitle('')
            setAnnouncementMessage('')
            setSpecificUserEmail('')
            setTargetAudience('ALL_USERS')
            handleRefresh()
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Failed to send announcement')
        } finally {
            setSendingAnnouncement(false)
        }
    }

    const handleCleanup = async () => {
        setCleaningUp(true)
        try {
            const result = await cleanupOldNotifications(cleanupDays)
            toast.success(`Deleted ${result.deletedCount} old notifications`)
            setCleanupOpen(false)
            handleRefresh()
        } catch (error) {
            toast.error('Failed to cleanup notifications')
        } finally {
            setCleaningUp(false)
        }
    }

    const handleDelete = async () => {
        if (!deleteId) return
        setDeleting(true)
        try {
            await deleteNotification(deleteId)
            toast.success('Notification deleted')
            setDeleteId(null)
            handleRefresh()
        } catch (error) {
            toast.error('Failed to delete notification')
        } finally {
            setDeleting(false)
        }
    }

    const getTypeBadge = (type: string) => {
        const label = NOTIFICATION_TYPE_LABELS[type] || type
        const colorClass = getNotificationTypeColor(type)
        return (
            <Badge variant="outline" className={`${colorClass} bg-current/10 border-current/20`}>
                {label}
            </Badge>
        )
    }

    const getReadBadge = (read: boolean) => {
        if (read) {
            return (
                <Badge variant="outline" className="bg-green-500/15 text-green-700 dark:text-green-400 border-green-200 dark:border-green-900/50">
                    <CheckCircle2 className="h-3 w-3 mr-1" />
                    Read
                </Badge>
            )
        }
        return (
            <Badge variant="outline" className="bg-yellow-500/15 text-yellow-700 dark:text-yellow-400 border-yellow-200 dark:border-yellow-900/50">
                <Clock className="h-3 w-3 mr-1" />
                Unread
            </Badge>
        )
    }

    // Client-side search filtering + exclude broadcasts from read/unread filters
    const filteredNotifications = notifications.filter(n => {
        // When filtering by READ or UNREAD, exclude broadcasts (they don't have read status)
        if (readFilter !== 'ALL' && n.isBroadcast) {
            return false
        }

        if (!searchQuery) return true
        const query = searchQuery.toLowerCase()
        return (
            n.title.toLowerCase().includes(query) ||
            n.message.toLowerCase().includes(query) ||
            n.type.toLowerCase().includes(query) ||
            (n.actorName && n.actorName.toLowerCase().includes(query))
        )
    })

    return (
        <div className="space-y-6 transition-colors duration-300">

            {/* Stats Overview - 4 Cards with Combined Info */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">

                {/* Total & Today */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Bell className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.total.toLocaleString()}</div>
                    <div className="text-blue-100 text-sm">Total Notifications</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <Clock className="h-4 w-4 opacity-70" />
                        <span className="text-sm"><strong>{stats.todayCount}</strong> today</span>
                    </div>
                </div>

                {/* Unread & Read Rate */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-amber-500 to-orange-500 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <BellRing className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.unread.toLocaleString()}</div>
                    <div className="text-amber-100 text-sm">Unread</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <CheckCircle2 className="h-4 w-4 opacity-70" />
                        <span className="text-sm"><strong>{stats.readRate.toFixed(0)}%</strong> read rate</span>
                    </div>
                </div>

                {/* Broadcasts */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-purple-500 to-indigo-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <Megaphone className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-3xl font-bold">{stats.readCount.toLocaleString()}</div>
                    <div className="text-purple-100 text-sm">Read</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <TrendingUp className="h-4 w-4 opacity-70" />
                        <span className="text-sm">of {stats.total} total</span>
                    </div>
                </div>

                {/* Top Type */}
                <div className="relative overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 p-5 text-white shadow-lg">
                    <div className="absolute top-0 right-0 -mt-4 -mr-4 h-24 w-24 rounded-full bg-white/10" />
                    <BarChart3 className="h-8 w-8 mb-3 opacity-80" />
                    <div className="text-xl font-bold truncate">
                        {stats.topNotificationType === 'NONE' || !stats.topNotificationType
                            ? 'No Data'
                            : (NOTIFICATION_TYPE_LABELS[stats.topNotificationType] || stats.topNotificationType)}
                    </div>
                    <div className="text-emerald-100 text-sm">Top Type</div>
                    <div className="mt-3 pt-3 border-t border-white/20 flex items-center gap-2">
                        <span className="text-sm">
                            {stats.topTypeCount > 0
                                ? <><strong>{stats.topTypeCount}</strong> notifications</>
                                : 'No notifications yet'}
                        </span>
                    </div>
                </div>

            </div>

            {/* Main Content Area - Redesigned */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">

                {/* Header Row */}
                <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                        <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-blue-500/25">
                            <Bell className="h-5 w-5 text-white" />
                        </div>
                        <div>
                            <h2 className="text-lg font-bold text-foreground">All Notifications</h2>
                            <p className="text-sm text-muted-foreground">{totalElements.toLocaleString()} total</p>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex items-center gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={handleRefresh}
                            className={`bg-white dark:bg-slate-800 ${loading ? 'animate-spin' : ''}`}
                        >
                            <RefreshCcw className="h-4 w-4" />
                        </Button>
                        <Button
                            size="sm"
                            onClick={() => setAnnouncementOpen(true)}
                            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-lg shadow-blue-500/25"
                        >
                            <Megaphone className="h-4 w-4 mr-1.5" />
                            Announce
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setCleanupOpen(true)}
                            className="text-rose-600 border-rose-200 hover:bg-rose-50 dark:border-rose-800 dark:hover:bg-rose-900/20"
                        >
                            <Trash2 className="h-4 w-4 mr-1.5" />
                            Cleanup
                        </Button>
                    </div>
                </div>

                {/* Filter Row */}
                <div className="px-6 py-3 bg-slate-50 dark:bg-slate-800/50 flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                    {/* Status Pills */}
                    <div className="flex items-center gap-1">
                        {['ALL', 'UNREAD', 'READ'].map((status) => (
                            <button
                                key={status}
                                onClick={() => {
                                    setReadFilter(status)
                                    setCurrentPage(0)
                                }}
                                className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${readFilter === status
                                    ? 'bg-blue-600 text-white shadow-sm'
                                    : 'bg-white dark:bg-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-600 border border-slate-200 dark:border-slate-600'
                                    }`}
                            >
                                {status === 'ALL' ? 'All' : status === 'UNREAD' ? 'Unread' : 'Read'}
                            </button>
                        ))}
                    </div>

                    <div className="hidden sm:block h-6 w-px bg-slate-200 dark:bg-slate-700" />

                    {/* Type Filter */}
                    <Select value={typeFilter} onValueChange={(value) => {
                        setTypeFilter(value)
                        setCurrentPage(0)
                    }}>
                        <SelectTrigger className="w-full sm:w-[160px] h-8 text-xs bg-white dark:bg-slate-700 border-slate-200 dark:border-slate-600">
                            <SelectValue placeholder="All Types" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">All Types</SelectItem>
                            {notificationTypes.map(type => (
                                <SelectItem key={type} value={type}>
                                    {NOTIFICATION_TYPE_LABELS[type] || type}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    {/* Search */}
                    <div className="relative flex-1 sm:max-w-xs">
                        <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
                        <Input
                            placeholder="Search..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-8 h-8 text-sm bg-white dark:bg-slate-700 border-slate-200 dark:border-slate-600"
                        />
                    </div>
                </div>

                {/* Table Content */}
                {loading && notifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                        <Loader2 className="h-10 w-10 animate-spin mb-4" />
                        <p>Loading notifications...</p>
                    </div>
                ) : filteredNotifications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-20 text-center">
                        <div className="h-14 w-14 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
                            <Bell className="h-7 w-7 text-muted-foreground" />
                        </div>
                        <h3 className="text-base font-semibold text-foreground">No notifications</h3>
                        <p className="text-sm text-muted-foreground max-w-xs mx-auto mt-1">
                            No notifications match your filters.
                        </p>
                        <Button
                            variant="outline"
                            size="sm"
                            className="mt-4"
                            onClick={() => {
                                setTypeFilter('ALL')
                                setReadFilter('ALL')
                                setSearchQuery('')
                            }}
                        >
                            Clear Filters
                        </Button>
                    </div>
                ) : (
                    <>
                        <div className="overflow-x-auto">
                            <Table>
                                <TableHeader>
                                    <TableRow className="hover:bg-transparent border-b border-slate-100 dark:border-slate-800">
                                        <TableHead className="font-semibold text-xs uppercase tracking-wider text-muted-foreground pl-6 py-3">Type</TableHead>
                                        <TableHead className="font-semibold text-xs uppercase tracking-wider text-muted-foreground py-3">Content</TableHead>
                                        <TableHead className="font-semibold text-xs uppercase tracking-wider text-muted-foreground py-3">Target</TableHead>
                                        <TableHead className="font-semibold text-xs uppercase tracking-wider text-muted-foreground py-3">Status</TableHead>
                                        <TableHead className="font-semibold text-xs uppercase tracking-wider text-muted-foreground py-3">Time</TableHead>
                                        <TableHead className="text-right pr-6 font-semibold text-xs uppercase tracking-wider text-muted-foreground py-3">Actions</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {filteredNotifications.map((notification) => (
                                        <TableRow
                                            key={notification.id}
                                            className="group hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors border-b border-slate-50 dark:border-slate-800/50 cursor-pointer"
                                            onClick={() => setSelectedNotification(notification)}
                                        >
                                            <TableCell className="pl-6 py-3">
                                                {getTypeBadge(notification.type)}
                                            </TableCell>
                                            <TableCell className="py-3">
                                                <div className="max-w-md">
                                                    <div className="font-medium text-sm text-foreground">
                                                        {notification.title}
                                                    </div>
                                                    <div className="text-xs text-muted-foreground line-clamp-1 mt-0.5">
                                                        {notification.message}
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell className="py-3">
                                                {notification.isBroadcast ? (
                                                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">
                                                        <Users className="h-3 w-3" />
                                                        {notification.targetAudience === 'ALL_USERS' && 'All Users'}
                                                        {notification.targetAudience === 'INVESTOR' && 'Investors'}
                                                        {notification.targetAudience === 'STARTUP_OWNER' && 'Owners'}
                                                        {!notification.targetAudience && 'Broadcast'}
                                                    </span>
                                                ) : (
                                                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300">
                                                        <User className="h-3 w-3" />
                                                        {notification.recipientEmail || 'Individual'}
                                                    </span>
                                                )}
                                            </TableCell>
                                            <TableCell className="py-3">
                                                {notification.isBroadcast ? (
                                                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300">
                                                        <Megaphone className="h-3 w-3" />
                                                        Broadcast
                                                    </span>
                                                ) : (
                                                    getReadBadge(notification.read)
                                                )}
                                            </TableCell>
                                            <TableCell className="py-3">
                                                <span className="text-xs text-muted-foreground">
                                                    {formatTimeAgo(notification.createdAt)}
                                                </span>
                                            </TableCell>
                                            <TableCell className="text-right pr-6 py-3" onClick={(e) => e.stopPropagation()}>
                                                <div className="flex items-center justify-end gap-1">
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-7 w-7 text-muted-foreground hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/20"
                                                        onClick={() => setSelectedNotification(notification)}
                                                    >
                                                        <Eye className="h-3.5 w-3.5" />
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        className="h-7 w-7 text-muted-foreground hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-900/20"
                                                        onClick={() => setDeleteId(notification.id)}
                                                    >
                                                        <Trash2 className="h-3.5 w-3.5" />
                                                    </Button>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>

                        {/* Pagination Footer */}
                        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 px-6 py-3 border-t border-slate-100 dark:border-slate-800">
                            <span className="text-xs text-muted-foreground">
                                {currentPage * pageSize + 1}-{Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements}
                            </span>
                            <div className="flex items-center gap-1">
                                <Select
                                    value={`${pageSize}`}
                                    onValueChange={(value) => {
                                        setPageSize(Number(value))
                                        setCurrentPage(0)
                                    }}
                                >
                                    <SelectTrigger className="h-7 w-16 text-xs">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent side="top">
                                        {[10, 20, 50, 100].map((size) => (
                                            <SelectItem key={size} value={`${size}`}>{size}</SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                <div className="flex items-center gap-0.5 ml-2">
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-7 w-7"
                                        onClick={() => setCurrentPage(0)}
                                        disabled={currentPage === 0}
                                    >
                                        <ChevronsLeft className="h-3.5 w-3.5" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-7 w-7"
                                        onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                                        disabled={currentPage === 0}
                                    >
                                        <ChevronLeft className="h-3.5 w-3.5" />
                                    </Button>
                                    <span className="text-xs px-2 text-muted-foreground">
                                        {currentPage + 1} / {totalPages || 1}
                                    </span>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-7 w-7"
                                        onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                                        disabled={currentPage >= totalPages - 1}
                                    >
                                        <ChevronRight className="h-3.5 w-3.5" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-7 w-7"
                                        onClick={() => setCurrentPage(totalPages - 1)}
                                        disabled={currentPage >= totalPages - 1}
                                    >
                                        <ChevronsRight className="h-3.5 w-3.5" />
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </div>

            {/* Send Announcement Dialog */}
            <Dialog open={announcementOpen} onOpenChange={(open) => {
                setAnnouncementOpen(open)
                if (!open) {
                    setShowUserDropdown(false)
                    setUserSearchResults([])
                }
            }}>
                <DialogContent className="sm:max-w-[480px] max-h-[85vh] overflow-y-auto bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-700 shadow-2xl ring-1 ring-black/5 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-slate-300 [&::-webkit-scrollbar-thumb]:rounded-full dark:[&::-webkit-scrollbar-thumb]:bg-slate-600">
                    {/* Header with gradient background */}
                    <div className="-m-6 mb-4 p-5 bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 rounded-t-lg">
                        <div className="flex items-center gap-3">
                            <div className="h-11 w-11 rounded-xl bg-white/20 backdrop-blur-sm shadow-lg flex items-center justify-center">
                                <Megaphone className="h-6 w-6 text-white" />
                            </div>
                            <div>
                                <h2 className="text-lg font-bold text-white">Send Announcement</h2>
                                <p className="text-blue-100 text-xs">Broadcast to your users</p>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-4">
                        {/* Title Input */}
                        <div className="space-y-1.5">
                            <Label htmlFor="title" className="text-sm font-semibold text-slate-700 dark:text-slate-300">Title</Label>
                            <Input
                                id="title"
                                placeholder="Enter announcement title..."
                                value={announcementTitle}
                                onChange={(e) => setAnnouncementTitle(e.target.value)}
                                className="bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        {/* Message Textarea */}
                        <div className="space-y-1.5">
                            <Label htmlFor="message" className="text-sm font-semibold text-slate-700 dark:text-slate-300">Message</Label>
                            <Textarea
                                id="message"
                                placeholder="Write your announcement message..."
                                value={announcementMessage}
                                onChange={(e) => setAnnouncementMessage(e.target.value)}
                                rows={3}
                                className="resize-none bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        {/* Target Audience */}
                        <div className="space-y-2">
                            <Label className="text-sm font-semibold text-slate-700 dark:text-slate-300">Target Audience</Label>
                            <div className="grid grid-cols-2 gap-2">
                                <button
                                    type="button"
                                    onClick={() => setTargetAudience('ALL_USERS')}
                                    className={`flex items-center gap-2 p-3 rounded-xl border-2 transition-all ${targetAudience === 'ALL_USERS'
                                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                                        : 'border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-700'
                                        }`}
                                >
                                    <Users className="h-5 w-5" />
                                    <span className="text-sm font-medium">All Users</span>
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setTargetAudience('INVESTORS_ONLY')}
                                    className={`flex items-center gap-2 p-3 rounded-xl border-2 transition-all ${targetAudience === 'INVESTORS_ONLY'
                                        ? 'border-emerald-500 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-300'
                                        : 'border-slate-200 dark:border-slate-700 hover:border-emerald-300 dark:hover:border-emerald-700'
                                        }`}
                                >
                                    <User className="h-5 w-5" />
                                    <span className="text-sm font-medium">Investors</span>
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setTargetAudience('STARTUP_OWNERS_ONLY')}
                                    className={`flex items-center gap-2 p-3 rounded-xl border-2 transition-all ${targetAudience === 'STARTUP_OWNERS_ONLY'
                                        ? 'border-purple-500 bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
                                        : 'border-slate-200 dark:border-slate-700 hover:border-purple-300 dark:hover:border-purple-700'
                                        }`}
                                >
                                    <Briefcase className="h-5 w-5" />
                                    <span className="text-sm font-medium">Owners</span>
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setTargetAudience('SPECIFIC_USER')}
                                    className={`flex items-center gap-2 p-3 rounded-xl border-2 transition-all ${targetAudience === 'SPECIFIC_USER'
                                        ? 'border-amber-500 bg-amber-50 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300'
                                        : 'border-slate-200 dark:border-slate-700 hover:border-amber-300 dark:hover:border-amber-700'
                                        }`}
                                >
                                    <Search className="h-5 w-5" />
                                    <span className="text-sm font-medium">Specific</span>
                                </button>
                            </div>

                            {/* Email input with search for specific user */}
                            {targetAudience === 'SPECIFIC_USER' && (
                                <div className="mt-2 relative">
                                    <div className="relative">
                                        <Input
                                            placeholder="Search by name or email..."
                                            value={specificUserEmail}
                                            onChange={(e) => handleUserSearch(e.target.value)}
                                            onFocus={() => userSearchResults.length > 0 && setShowUserDropdown(true)}
                                            className="bg-white dark:bg-slate-800 border-amber-300 dark:border-amber-700 focus:ring-2 focus:ring-amber-500 pr-8"
                                        />
                                        {searchingUsers && (
                                            <Loader2 className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 animate-spin text-amber-500" />
                                        )}
                                    </div>
                                    {/* Dropdown results */}
                                    {showUserDropdown && userSearchResults.length > 0 && (
                                        <div className="absolute z-50 w-full mt-1 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-lg max-h-48 overflow-y-auto">
                                            {userSearchResults.map((user) => (
                                                <button
                                                    key={user.id}
                                                    type="button"
                                                    onClick={() => selectUser(user)}
                                                    className="w-full flex items-center gap-3 p-2.5 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors text-left"
                                                >
                                                    <div className="h-8 w-8 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white text-xs font-bold">
                                                        {user.name?.charAt(0).toUpperCase() || user.email.charAt(0).toUpperCase()}
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <div className="text-sm font-medium truncate">{user.name}</div>
                                                        <div className="text-xs text-slate-500 truncate">{user.email}</div>
                                                    </div>
                                                </button>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Channels */}
                        <div className="flex items-center gap-6 py-2 px-3 bg-slate-100 dark:bg-slate-800 rounded-lg">
                            <div className="flex items-center space-x-2">
                                <Checkbox
                                    id="inapp"
                                    checked={inAppChannel}
                                    onCheckedChange={(checked) => setInAppChannel(checked as boolean)}
                                />
                                <label htmlFor="inapp" className="text-sm flex items-center gap-1.5 cursor-pointer font-medium">
                                    <Bell className="h-4 w-4 text-blue-600" />
                                    In-App
                                </label>
                            </div>
                            <div className="flex items-center space-x-2 opacity-50">
                                <Checkbox id="push" checked={pushChannel} disabled />
                                <label htmlFor="push" className="text-sm flex items-center gap-1.5 cursor-not-allowed">
                                    <Smartphone className="h-4 w-4" />
                                    Push (Soon)
                                </label>
                            </div>
                        </div>
                    </div>

                    <DialogFooter className="gap-2 pt-4 border-t border-slate-200 dark:border-slate-800 mt-4">
                        <Button
                            variant="outline"
                            onClick={() => setAnnouncementOpen(false)}
                            className="border-slate-300 dark:border-slate-600"
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={handleSendAnnouncement}
                            disabled={sendingAnnouncement}
                            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-lg shadow-blue-500/30"
                        >
                            {sendingAnnouncement ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Sending...
                                </>
                            ) : (
                                <>
                                    <Megaphone className="mr-2 h-4 w-4" />
                                    Send
                                </>
                            )}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Cleanup Dialog */}
            <AlertDialog open={cleanupOpen} onOpenChange={setCleanupOpen}>
                <AlertDialogContent className="max-w-md bg-white dark:bg-slate-950 border-rose-200 dark:border-rose-800/50">
                    <AlertDialogHeader>
                        <div className="mx-auto h-14 w-14 rounded-2xl bg-gradient-to-br from-rose-100 to-red-100 dark:from-rose-900/30 dark:to-red-900/30 shadow-lg flex items-center justify-center mb-3">
                            <Trash2 className="h-7 w-7 text-rose-600 dark:text-rose-400" />
                        </div>
                        <AlertDialogTitle className="text-xl font-bold text-center">
                            Cleanup Old Notifications
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-center">
                            Delete all read notifications older than the specified days.
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <div className="py-4">
                        <Label htmlFor="days">Days to keep</Label>
                        <Select value={`${cleanupDays}`} onValueChange={(v) => setCleanupDays(Number(v))}>
                            <SelectTrigger className="mt-2">
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="7">7 days</SelectItem>
                                <SelectItem value="14">14 days</SelectItem>
                                <SelectItem value="30">30 days</SelectItem>
                                <SelectItem value="60">60 days</SelectItem>
                                <SelectItem value="90">90 days</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <AlertDialogFooter className="gap-3">
                        <AlertDialogCancel disabled={cleaningUp}>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={(e) => {
                                e.preventDefault()
                                handleCleanup()
                            }}
                            disabled={cleaningUp}
                            className="bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-700 hover:to-red-700 text-white"
                        >
                            {cleaningUp ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Cleaning...
                                </>
                            ) : (
                                'Cleanup Now'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={!!deleteId} onOpenChange={(open) => !open && setDeleteId(null)}>
                <AlertDialogContent className="max-w-md bg-white dark:bg-slate-950 border-rose-200 dark:border-rose-800/50">
                    <AlertDialogHeader>
                        <div className="mx-auto h-14 w-14 rounded-2xl bg-gradient-to-br from-rose-100 to-red-100 dark:from-rose-900/30 dark:to-red-900/30 shadow-lg flex items-center justify-center mb-3">
                            <Trash2 className="h-7 w-7 text-rose-600 dark:text-rose-400" />
                        </div>
                        <AlertDialogTitle className="text-xl font-bold text-center">
                            Delete Notification?
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-center">
                            This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter className="gap-3">
                        <AlertDialogCancel disabled={deleting}>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={(e) => {
                                e.preventDefault()
                                handleDelete()
                            }}
                            disabled={deleting}
                            className="bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-700 hover:to-red-700 text-white"
                        >
                            {deleting ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Deleting...
                                </>
                            ) : (
                                'Delete'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* Notification Details Modal */}
            <Dialog open={!!selectedNotification} onOpenChange={(open) => !open && setSelectedNotification(null)}>
                <DialogContent className="sm:max-w-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700">
                    {selectedNotification && (
                        <>
                            <DialogHeader className="pb-4 border-b border-slate-100 dark:border-slate-800">
                                <div className="flex items-start gap-3">
                                    <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center">
                                        <Bell className="h-5 w-5 text-white" />
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <DialogTitle className="text-lg font-bold">
                                            {selectedNotification.title}
                                        </DialogTitle>
                                        <p className="text-xs text-muted-foreground mt-0.5">
                                            {getTypeBadge(selectedNotification.type)}
                                        </p>
                                    </div>
                                </div>
                            </DialogHeader>

                            <div className="py-4 space-y-4">
                                {/* Message */}
                                <div>
                                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Message</label>
                                    <p className="mt-1 text-sm text-foreground">{selectedNotification.message}</p>
                                </div>

                                {/* Target Info */}
                                <div>
                                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Target</label>
                                    <div className="mt-1">
                                        {selectedNotification.isBroadcast ? (
                                            <div className="flex items-center gap-2">
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">
                                                    <Users className="h-3.5 w-3.5" />
                                                    Broadcast
                                                </span>
                                                <span className="text-sm text-muted-foreground">
                                                    {selectedNotification.targetAudience === 'ALL_USERS' && '→ All Users'}
                                                    {selectedNotification.targetAudience === 'INVESTOR' && '→ Investors Only'}
                                                    {selectedNotification.targetAudience === 'STARTUP_OWNER' && '→ Startup Owners Only'}
                                                    {!selectedNotification.targetAudience && '→ Unknown Audience'}
                                                </span>
                                            </div>
                                        ) : (
                                            <div className="flex items-center gap-2">
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300">
                                                    <User className="h-3.5 w-3.5" />
                                                    Individual
                                                </span>
                                                <span className="text-sm text-muted-foreground">
                                                    → {selectedNotification.recipientName || selectedNotification.recipientEmail || 'Unknown'}
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {/* Status & Time */}
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Status</label>
                                        <div className="mt-1">
                                            {selectedNotification.isBroadcast ? (
                                                <span className="text-xs text-muted-foreground italic">
                                                    (Broadcasts don't track read status)
                                                </span>
                                            ) : (
                                                getReadBadge(selectedNotification.read)
                                            )}
                                        </div>
                                    </div>
                                    <div>
                                        <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Created</label>
                                        <p className="mt-1 text-sm text-foreground">{formatTimeAgo(selectedNotification.createdAt)}</p>
                                    </div>
                                </div>

                                {/* Actor Info */}
                                {selectedNotification.actorName && (
                                    <div>
                                        <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">By</label>
                                        <p className="mt-1 text-sm text-foreground">{selectedNotification.actorName}</p>
                                    </div>
                                )}

                                {/* Raw Data */}
                                {selectedNotification.data && Object.keys(selectedNotification.data).length > 0 && (
                                    <div>
                                        <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Additional Data</label>
                                        <pre className="mt-1 text-xs bg-slate-100 dark:bg-slate-800 p-2 rounded-lg overflow-x-auto">
                                            {JSON.stringify(selectedNotification.data, null, 2)}
                                        </pre>
                                    </div>
                                )}
                            </div>

                            <DialogFooter className="pt-4 border-t border-slate-100 dark:border-slate-800 gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    className="text-rose-600 border-rose-200 hover:bg-rose-50"
                                    onClick={() => {
                                        setDeleteId(selectedNotification.id)
                                        setSelectedNotification(null)
                                    }}
                                >
                                    <Trash2 className="h-4 w-4 mr-1.5" />
                                    Delete
                                </Button>
                                <Button
                                    size="sm"
                                    onClick={() => setSelectedNotification(null)}
                                >
                                    Close
                                </Button>
                            </DialogFooter>
                        </>
                    )}
                </DialogContent>
            </Dialog>

        </div>
    )
}

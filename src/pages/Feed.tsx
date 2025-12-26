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
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "../components/ui/alert-dialog"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../components/ui/select"
import { Badge } from "../components/ui/badge"
import {
    Loader2,
    RefreshCcw,
    Search,
    Eye,
    EyeOff,
    Trash2,
    Star,
    StarOff,
    Heart,
    MessageCircle,
    Share2,
    TrendingUp,
    Newspaper,
    ChevronsLeft,
    ChevronsRight,
    ChevronLeft,
    ChevronRight,
    Image,
    Undo2,
    RotateCcw,
} from 'lucide-react'
import { KPICard } from '../components/dashboard/KPICard'
import { formatTimeAgo } from '../lib/utils'
import { toast } from 'sonner'
import {
    getAdminFeedPosts,
    getFeedStats,
    hidePost,
    restorePost,
    deletePost,
    featurePost as featurePostApi,
    unfeaturePost,
    recalculateScores,
    PostData,
    FeedStats,
} from '../api/feedApi'
import { PostDetailsDialog } from '../components/dashboard/PostDetailsDialog'

// Plan badge colors
const PLAN_COLORS: Record<string, string> = {
    FREE: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
    PRO: 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/30 dark:text-indigo-400',
    ELITE: 'bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400',
}

export function Feed() {
    const [posts, setPosts] = useState<PostData[]>([])
    const [stats, setStats] = useState<FeedStats>({
        totalPosts: 0,
        activePosts: 0,
        hiddenPosts: 0,
        deletedPosts: 0,
        featuredPosts: 0,
        todayPosts: 0,
        avgEngagement: 0,
        totalLikes: 0,
        totalComments: 0,
        totalShares: 0,
    })
    const [loading, setLoading] = useState(true)
    const [searchQuery, setSearchQuery] = useState('')
    const [statusFilter, setStatusFilter] = useState<string>('ALL')

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [pageSize, setPageSize] = useState(20)

    // Selected post for details
    const [selectedPostId, setSelectedPostId] = useState<string | null>(null)

    // Action dialogs
    const [actionPost, setActionPost] = useState<PostData | null>(null)
    const [actionType, setActionType] = useState<'hide' | 'delete' | 'restore' | null>(null)
    const [actionLoading, setActionLoading] = useState(false)
    const [actionReason, setActionReason] = useState('')

    // Feature dialog
    const [featurePost, setFeaturePost] = useState<PostData | null>(null)
    const [featureDuration, setFeatureDuration] = useState<string>('indefinite')
    const [featureLoading, setFeatureLoading] = useState(false)

    // Load stats
    useEffect(() => {
        loadStats()
    }, [])

    // Load posts when filters change
    useEffect(() => {
        loadPosts()
    }, [currentPage, pageSize, statusFilter])

    const loadStats = async () => {
        try {
            const data = await getFeedStats()
            setStats(data)
        } catch (error) {
            console.error('Failed to load stats:', error)
        }
    }

    const loadPosts = async () => {
        setLoading(true)
        try {
            const status = statusFilter === 'ALL' ? undefined : statusFilter
            const data = await getAdminFeedPosts(currentPage, pageSize, status, searchQuery || undefined)
            setPosts(data.content)
            setTotalPages(data.totalPages)
            setTotalElements(data.totalElements)
        } catch (error: unknown) {
            console.error('Failed to load posts:', error)
            toast.error('Failed to load posts')
        } finally {
            setLoading(false)
        }
    }

    const handleRefresh = () => {
        loadStats()
        loadPosts()
    }

    const handleSearch = () => {
        setCurrentPage(0)
        loadPosts()
    }

    // Status badge rendering
    const getStatusBadge = (post: PostData) => {
        if (post.isFeatured) {
            return (
                <Badge className="gap-1 bg-amber-500/15 text-amber-700 dark:text-amber-400 border-amber-200 dark:border-amber-900/50">
                    <Star className="h-3 w-3" />
                    Featured
                </Badge>
            )
        }

        const styles: Record<string, string> = {
            ACTIVE: "bg-green-500/15 text-green-700 dark:text-green-400 border-green-200 dark:border-green-900/50",
            HIDDEN: "bg-orange-500/15 text-orange-700 dark:text-orange-400 border-orange-200 dark:border-orange-900/50",
            DELETED: "bg-red-500/15 text-red-700 dark:text-red-400 border-red-200 dark:border-red-900/50",
        }

        const icons: Record<string, React.ReactNode> = {
            ACTIVE: <Eye className="h-3 w-3" />,
            HIDDEN: <EyeOff className="h-3 w-3" />,
            DELETED: <Trash2 className="h-3 w-3" />,
        }

        return (
            <Badge variant="outline" className={`gap-1 ${styles[post.status]}`}>
                {icons[post.status]}
                {post.status.toLowerCase()}
            </Badge>
        )
    }

    // Handle moderation actions
    const handleAction = async () => {
        if (!actionPost || !actionType) return

        setActionLoading(true)
        try {
            switch (actionType) {
                case 'hide':
                    await hidePost(actionPost.id, actionReason || undefined)
                    toast.success('Post hidden successfully')
                    break
                case 'restore':
                    await restorePost(actionPost.id)
                    toast.success('Post restored successfully')
                    break
                case 'delete':
                    await deletePost(actionPost.id, actionReason || undefined)
                    toast.success('Post deleted successfully')
                    break
            }
            handleRefresh()
        } catch (error) {
            console.error('Action failed:', error)
            toast.error('Action failed')
        } finally {
            setActionLoading(false)
            setActionPost(null)
            setActionType(null)
            setActionReason('')
        }
    }

    // Handle feature toggle - opens dialog if featuring, unfeatured directly if unfeaturing
    const handleFeatureToggle = async (post: PostData) => {
        if (post.isFeatured) {
            // Unfeature directly
            try {
                await unfeaturePost(post.id)
                toast.success('Post unfeatured')
                handleRefresh()
            } catch (error) {
                console.error('Unfeature failed:', error)
                toast.error('Failed to unfeature post')
            }
        } else {
            // Open feature dialog to select duration
            setFeaturePost(post)
            setFeatureDuration('indefinite')
        }
    }

    // Handle feature with duration
    const handleFeatureWithDuration = async () => {
        if (!featurePost) return

        setFeatureLoading(true)
        try {
            const durationHours = featureDuration === 'indefinite' ? undefined : parseInt(featureDuration)
            await featurePostApi(featurePost.id, durationHours)

            const message = durationHours
                ? `Post featured for ${durationHours} hours`
                : 'Post featured indefinitely'
            toast.success(message)
            handleRefresh()
        } catch (error) {
            console.error('Feature failed:', error)
            toast.error('Failed to feature post')
        } finally {
            setFeatureLoading(false)
            setFeaturePost(null)
            setFeatureDuration('indefinite')
        }
    }

    // Handle score recalculation
    const handleRecalculateScores = async () => {
        try {
            await recalculateScores()
            toast.success('Score recalculation triggered')
            // Refresh data to show updated scores
            handleRefresh()
        } catch (error) {
            console.error('Recalculation failed:', error)
            toast.error('Failed to trigger recalculation')
        }
    }

    return (
        <div className="space-y-6 transition-colors duration-300">
            {/* Stats Overview */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
                <KPICard
                    title="Total Posts"
                    value={stats.totalPosts.toString()}
                    icon={Newspaper}
                />
                <KPICard
                    title="Active"
                    value={stats.activePosts.toString()}
                    icon={Eye}
                    iconColor="text-green-600 dark:text-green-400"
                />
                <KPICard
                    title="Hidden"
                    value={stats.hiddenPosts.toString()}
                    icon={EyeOff}
                    iconColor="text-orange-600 dark:text-orange-400"
                />
                <KPICard
                    title="Featured"
                    value={stats.featuredPosts.toString()}
                    icon={Star}
                    iconColor="text-amber-600 dark:text-amber-400"
                />
                <KPICard
                    title="Today"
                    value={stats.todayPosts.toString()}
                    icon={TrendingUp}
                    iconColor="text-blue-600 dark:text-blue-400"
                />
            </div>

            {/* Main Content Area */}
            <div className="space-y-4">
                {/* Advanced Toolbar */}
                <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center bg-white dark:bg-slate-900 p-4 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800">
                    {/* Status Tabs */}
                    <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-lg overflow-x-auto max-w-full no-scrollbar">
                        {['ALL', 'ACTIVE', 'HIDDEN', 'DELETED'].map((status) => (
                            <button
                                key={status}
                                onClick={() => {
                                    setStatusFilter(status)
                                    setCurrentPage(0)
                                }}
                                className={`
                                    px-4 py-1.5 rounded-md text-sm font-medium transition-all duration-200
                                    ${statusFilter === status
                                        ? 'bg-white dark:bg-slate-700 text-foreground shadow-sm'
                                        : 'text-slate-500 dark:text-slate-400 hover:text-foreground hover:bg-white/50 dark:hover:bg-slate-700/50'}
                                `}
                            >
                                {status === 'ALL' ? 'All' : status.toLowerCase()}
                            </button>
                        ))}
                    </div>

                    <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                        <div className="relative flex-1 sm:w-64">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search posts..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                                className="pl-9 bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800"
                            />
                        </div>

                        <Button
                            variant="outline"
                            size="icon"
                            onClick={handleRecalculateScores}
                            className="shrink-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700"
                            title="Recalculate Scores"
                        >
                            <RotateCcw className="h-4 w-4" />
                        </Button>

                        <Button
                            variant="outline"
                            size="icon"
                            onClick={handleRefresh}
                            className={`shrink-0 bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 ${loading ? 'animate-spin' : ''}`}
                        >
                            <RefreshCcw className="h-4 w-4" />
                        </Button>
                    </div>
                </div>

                {/* Posts Table */}
                <div className="bg-white dark:bg-slate-900 rounded-xl shadow-sm border border-slate-200 dark:border-slate-800 overflow-hidden">
                    {loading && posts.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                            <Loader2 className="h-10 w-10 animate-spin mb-4" />
                            <p>Loading posts...</p>
                        </div>
                    ) : posts.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-center">
                            <div className="h-16 w-16 bg-muted rounded-full flex items-center justify-center mb-4">
                                <Newspaper className="h-8 w-8 text-muted-foreground" />
                            </div>
                            <h3 className="text-lg font-semibold text-foreground">No posts found</h3>
                            <p className="text-muted-foreground max-w-sm mx-auto mt-1">
                                No posts match your current filters.
                            </p>
                        </div>
                    ) : (
                        <>
                            <div className="overflow-x-auto">
                                <Table>
                                    <TableHeader className="bg-slate-50 dark:bg-slate-800/50">
                                        <TableRow className="hover:bg-transparent border-b-border">
                                            <TableHead className="font-semibold text-muted-foreground pl-6">Author</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground">Content</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground text-center">Engagement</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground text-center">Score</TableHead>
                                            <TableHead className="font-semibold text-muted-foreground text-center">Status</TableHead>
                                            <TableHead className="text-right pr-6 font-semibold text-muted-foreground">Actions</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {posts.map((post) => (
                                            <TableRow
                                                key={post.id}
                                                className="cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors border-b-border"
                                                onClick={() => setSelectedPostId(post.id)}
                                            >
                                                {/* Author */}
                                                <TableCell className="pl-6 py-3">
                                                    <div className="flex items-center gap-3">
                                                        <div className="h-10 w-10 rounded-full bg-gradient-to-br from-indigo-400 to-purple-400 flex items-center justify-center text-white font-medium text-sm shrink-0 overflow-hidden">
                                                            {post.authorAvatarUrl ? (
                                                                <img src={post.authorAvatarUrl} alt="" className="h-full w-full object-cover" />
                                                            ) : (
                                                                post.authorName?.charAt(0)?.toUpperCase() || 'U'
                                                            )}
                                                        </div>
                                                        <div className="flex flex-col">
                                                            <span className="font-medium text-foreground text-sm">{post.authorName}</span>
                                                            <div className="flex items-center gap-1.5">
                                                                <Badge className={`text-[10px] px-1.5 py-0 h-4 ${PLAN_COLORS[post.authorPlan] || PLAN_COLORS.FREE}`}>
                                                                    {post.authorPlan}
                                                                </Badge>
                                                                <span className="text-[10px] text-muted-foreground">{formatTimeAgo(post.createdAt)}</span>
                                                            </div>
                                                            {post.contextualTitle && (
                                                                <span className="text-[11px] text-muted-foreground mt-0.5">{post.contextualTitle}</span>
                                                            )}
                                                        </div>
                                                    </div>
                                                </TableCell>

                                                {/* Content */}
                                                <TableCell>
                                                    <div className="flex flex-col gap-1 max-w-[280px]">
                                                        <p className="text-sm text-foreground line-clamp-2">{post.content}</p>
                                                        <div className="flex items-center gap-1.5">
                                                            {post.mediaUrl && (
                                                                <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4 gap-0.5">
                                                                    <Image className="h-2.5 w-2.5" />
                                                                    Media
                                                                </Badge>
                                                            )}
                                                            {post.isEdited && (
                                                                <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4 text-muted-foreground">
                                                                    Edited
                                                                </Badge>
                                                            )}
                                                        </div>
                                                    </div>
                                                </TableCell>

                                                {/* Engagement */}
                                                <TableCell className="text-center">
                                                    <div className="flex items-center justify-center gap-4 text-sm">
                                                        <div className="flex flex-col items-center">
                                                            <Heart className="h-4 w-4 text-rose-400 mb-0.5" />
                                                            <span className="text-foreground font-medium">{post.likeCount}</span>
                                                        </div>
                                                        <div className="flex flex-col items-center">
                                                            <MessageCircle className="h-4 w-4 text-blue-400 mb-0.5" />
                                                            <span className="text-foreground font-medium">{post.commentCount}</span>
                                                        </div>
                                                        <div className="flex flex-col items-center">
                                                            <Share2 className="h-4 w-4 text-green-400 mb-0.5" />
                                                            <span className="text-foreground font-medium">{post.shareCount}</span>
                                                        </div>
                                                    </div>
                                                </TableCell>

                                                {/* Score */}
                                                <TableCell className="text-center">
                                                    <div className="flex flex-col items-center">
                                                        <span className="font-mono font-semibold text-foreground text-base">
                                                            {post.rankingScore?.toFixed(1) || '0.0'}
                                                        </span>
                                                        <span className="text-[10px] text-muted-foreground">
                                                            ×{post.subscriptionMultiplier?.toFixed(1) || '1.0'}
                                                        </span>
                                                    </div>
                                                </TableCell>

                                                {/* Status */}
                                                <TableCell className="text-center">
                                                    {getStatusBadge(post)}
                                                </TableCell>

                                                {/* Actions - Always visible */}
                                                <TableCell className="text-right pr-6">
                                                    <div className="flex items-center justify-end gap-1">
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className="h-8 w-8 text-muted-foreground hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-900/20"
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                setSelectedPostId(post.id)
                                                            }}
                                                            title="View Details"
                                                        >
                                                            <Eye className="h-4 w-4" />
                                                        </Button>

                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            className={`h-8 w-8 ${post.isFeatured ? 'text-amber-500' : 'text-muted-foreground'} hover:text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-900/20`}
                                                            onClick={(e) => {
                                                                e.stopPropagation()
                                                                handleFeatureToggle(post)
                                                            }}
                                                            title={post.isFeatured ? 'Unfeature' : 'Feature'}
                                                        >
                                                            {post.isFeatured ? <StarOff className="h-4 w-4" /> : <Star className="h-4 w-4" />}
                                                        </Button>

                                                        {post.status === 'HIDDEN' ? (
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                className="h-8 w-8 text-muted-foreground hover:text-green-600 hover:bg-green-50 dark:hover:bg-green-900/20"
                                                                onClick={(e) => {
                                                                    e.stopPropagation()
                                                                    setActionPost(post)
                                                                    setActionType('restore')
                                                                }}
                                                                title="Restore"
                                                            >
                                                                <Undo2 className="h-4 w-4" />
                                                            </Button>
                                                        ) : post.status === 'ACTIVE' && (
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                className="h-8 w-8 text-muted-foreground hover:text-orange-600 hover:bg-orange-50 dark:hover:bg-orange-900/20"
                                                                onClick={(e) => {
                                                                    e.stopPropagation()
                                                                    setActionPost(post)
                                                                    setActionType('hide')
                                                                }}
                                                                title="Hide"
                                                            >
                                                                <EyeOff className="h-4 w-4" />
                                                            </Button>
                                                        )}

                                                        {post.status !== 'DELETED' && (
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                className="h-8 w-8 text-muted-foreground hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-900/20"
                                                                onClick={(e) => {
                                                                    e.stopPropagation()
                                                                    setActionPost(post)
                                                                    setActionType('delete')
                                                                }}
                                                                title="Delete"
                                                            >
                                                                <Trash2 className="h-4 w-4" />
                                                            </Button>
                                                        )}
                                                    </div>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>

                            {/* Pagination Footer */}
                            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 px-4 py-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50">
                                <div className="text-sm text-muted-foreground">
                                    Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} posts
                                </div>
                                <div className="flex flex-wrap items-center gap-4 lg:gap-6">
                                    <div className="flex items-center space-x-2">
                                        <p className="text-sm font-medium">Rows per page</p>
                                        <Select
                                            value={`${pageSize}`}
                                            onValueChange={(value) => {
                                                setPageSize(Number(value))
                                                setCurrentPage(0)
                                            }}
                                        >
                                            <SelectTrigger className="h-8 w-[70px]">
                                                <SelectValue placeholder={pageSize} />
                                            </SelectTrigger>
                                            <SelectContent side="top">
                                                {[10, 20, 50, 100].map((size) => (
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
                                            onClick={() => setCurrentPage(0)}
                                            disabled={currentPage === 0}
                                        >
                                            <ChevronsLeft className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            className="h-8 w-8 p-0"
                                            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                                            disabled={currentPage === 0}
                                        >
                                            <ChevronLeft className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            className="h-8 w-8 p-0"
                                            onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                                            disabled={currentPage >= totalPages - 1}
                                        >
                                            <ChevronRight className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            className="hidden h-8 w-8 p-0 lg:flex"
                                            onClick={() => setCurrentPage(totalPages - 1)}
                                            disabled={currentPage >= totalPages - 1}
                                        >
                                            <ChevronsRight className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Post Details Dialog */}
            {selectedPostId && (
                <PostDetailsDialog
                    postId={selectedPostId}
                    open={!!selectedPostId}
                    onOpenChange={(open) => !open && setSelectedPostId(null)}
                    onPostUpdated={handleRefresh}
                />
            )}

            {/* Action Confirmation Dialog */}
            <AlertDialog open={!!actionPost && !!actionType} onOpenChange={(open: boolean) => { if (!open) { setActionPost(null); setActionType(null); setActionReason(''); } }}>
                <AlertDialogContent className="max-w-md">
                    <AlertDialogHeader>
                        <div className={`mx-auto h-14 w-14 rounded-2xl shadow-lg flex items-center justify-center mb-3 ${actionType === 'delete' ? 'bg-gradient-to-br from-rose-100 to-red-100 dark:from-rose-900/30 dark:to-red-900/30' :
                            actionType === 'hide' ? 'bg-gradient-to-br from-orange-100 to-amber-100 dark:from-orange-900/30 dark:to-amber-900/30' :
                                'bg-gradient-to-br from-green-100 to-emerald-100 dark:from-green-900/30 dark:to-emerald-900/30'
                            }`}>
                            {actionType === 'delete' && <Trash2 className="h-7 w-7 text-rose-600 dark:text-rose-400" />}
                            {actionType === 'hide' && <EyeOff className="h-7 w-7 text-orange-600 dark:text-orange-400" />}
                            {actionType === 'restore' && <Undo2 className="h-7 w-7 text-green-600 dark:text-green-400" />}
                        </div>
                        <AlertDialogTitle className="text-xl font-bold text-center">
                            {actionType === 'delete' && 'Delete Post?'}
                            {actionType === 'hide' && 'Hide Post?'}
                            {actionType === 'restore' && 'Restore Post?'}
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-center">
                            {actionType === 'delete' && 'This post will be soft-deleted and hidden from the feed.'}
                            {actionType === 'hide' && 'This post will be hidden from the feed but can be restored later.'}
                            {actionType === 'restore' && 'This post will be restored and visible in the feed again.'}
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    {(actionType === 'hide' || actionType === 'delete') && (
                        <div className="py-2">
                            <Input
                                placeholder="Reason (optional)"
                                value={actionReason}
                                onChange={(e) => setActionReason(e.target.value)}
                            />
                        </div>
                    )}

                    <AlertDialogFooter className="gap-3">
                        <AlertDialogCancel disabled={actionLoading}>
                            Cancel
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                                e.preventDefault()
                                handleAction()
                            }}
                            disabled={actionLoading}
                            className={`${actionType === 'delete' ? 'bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-700 hover:to-red-700' :
                                actionType === 'hide' ? 'bg-gradient-to-r from-orange-600 to-amber-600 hover:from-orange-700 hover:to-amber-700' :
                                    'bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700'
                                } text-white shadow-lg border-0`}
                        >
                            {actionLoading ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                                actionType === 'delete' ? 'Delete' :
                                    actionType === 'hide' ? 'Hide' :
                                        'Restore'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* Feature Duration Dialog */}
            <AlertDialog open={!!featurePost} onOpenChange={(open: boolean) => { if (!open) { setFeaturePost(null); setFeatureDuration('indefinite'); } }}>
                <AlertDialogContent className="max-w-md">
                    <AlertDialogHeader>
                        <div className="mx-auto h-14 w-14 rounded-2xl shadow-lg flex items-center justify-center mb-3 bg-gradient-to-br from-amber-100 to-yellow-100 dark:from-amber-900/30 dark:to-yellow-900/30">
                            <Star className="h-7 w-7 text-amber-600 dark:text-amber-400" />
                        </div>
                        <AlertDialogTitle className="text-xl font-bold text-center">
                            Feature Post
                        </AlertDialogTitle>
                        <AlertDialogDescription className="text-center">
                            Featured posts appear at the top of the feed. Choose how long to feature this post.
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <div className="py-4 space-y-3">
                        <label className="text-sm font-medium text-foreground">Duration</label>
                        <Select value={featureDuration} onValueChange={setFeatureDuration}>
                            <SelectTrigger>
                                <SelectValue placeholder="Select duration" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="indefinite">♾️ Indefinitely (until manually removed)</SelectItem>
                                <SelectItem value="1">⏱️ 1 hour</SelectItem>
                                <SelectItem value="2">⏱️ 2 hours</SelectItem>
                                <SelectItem value="6">⏱️ 6 hours</SelectItem>
                                <SelectItem value="12">⏱️ 12 hours</SelectItem>
                                <SelectItem value="24">📅 1 day</SelectItem>
                                <SelectItem value="48">📅 2 days</SelectItem>
                                <SelectItem value="72">📅 3 days</SelectItem>
                                <SelectItem value="168">📅 1 week</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <AlertDialogFooter className="gap-3">
                        <AlertDialogCancel disabled={featureLoading}>
                            Cancel
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                                e.preventDefault()
                                handleFeatureWithDuration()
                            }}
                            disabled={featureLoading}
                            className="bg-gradient-to-r from-amber-500 to-yellow-500 hover:from-amber-600 hover:to-yellow-600 text-white shadow-lg border-0"
                        >
                            {featureLoading ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                                <>
                                    <Star className="h-4 w-4 mr-1.5" />
                                    Feature Post
                                </>
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    )
}

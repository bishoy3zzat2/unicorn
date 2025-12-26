import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogTitle,
    DialogDescription,
} from '../ui/dialog'
import { Button } from '../ui/button'
import { Badge } from '../ui/badge'
import { Separator } from '../ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs'
import {
    Eye,
    EyeOff,
    Trash2,
    Star,
    StarOff,
    Heart,
    MessageCircle,
    Share2,
    TrendingUp,
    Image,
    Pencil,
    Crown,
    Shield,
    Loader2,
    XCircle,
    Calculator,
    Zap,
    Timer,
    ChevronDown,
    ChevronUp,
    Calendar,
    Reply,
} from 'lucide-react'
import { formatTimeAgo } from '../../lib/utils'
import { toast } from 'sonner'
import {
    getPostById,
    hidePost,
    restorePost,
    deletePost,
    featurePost as featurePostApi,
    unfeaturePost,
    getPostLikes,
    getPostComments,
    getPostShares,
    PostData,
    EngagementUser,
    CommentWithReplies,
} from '../../api/feedApi'

interface PostDetailsDialogProps {
    postId: string
    open: boolean
    onOpenChange: (open: boolean) => void
    onPostUpdated?: () => void
}

const PLAN_COLORS: Record<string, string> = {
    FREE: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
    PRO: 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/30 dark:text-indigo-400',
    ELITE: 'bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400',
}

const STATUS_CONFIG = {
    ACTIVE: { bg: 'bg-emerald-100 dark:bg-emerald-900/30', text: 'text-emerald-700 dark:text-emerald-400', label: 'Active' },
    HIDDEN: { bg: 'bg-orange-100 dark:bg-orange-900/30', text: 'text-orange-700 dark:text-orange-400', label: 'Hidden' },
    DELETED: { bg: 'bg-red-100 dark:bg-red-900/30', text: 'text-red-700 dark:text-red-400', label: 'Deleted' },
}

export function PostDetailsDialog({
    postId,
    open,
    onOpenChange,
    onPostUpdated,
}: PostDetailsDialogProps) {
    const [post, setPost] = useState<PostData | null>(null)
    const [loading, setLoading] = useState(true)
    const [actionLoading, setActionLoading] = useState(false)
    const [activeTab, setActiveTab] = useState('overview')

    // Engagement data
    const [likes, setLikes] = useState<EngagementUser[]>([])
    const [likesTotal, setLikesTotal] = useState(0)
    const [likesPage, setLikesPage] = useState(0)
    const [likesLoading, setLikesLoading] = useState(false)

    const [comments, setComments] = useState<CommentWithReplies[]>([])
    const [commentsTotal, setCommentsTotal] = useState(0)
    const [commentsPage, setCommentsPage] = useState(0)
    const [commentsLoading, setCommentsLoading] = useState(false)

    const [shares, setShares] = useState<EngagementUser[]>([])
    const [sharesTotal, setSharesTotal] = useState(0)
    const [sharesPage, setSharesPage] = useState(0)
    const [sharesLoading, setSharesLoading] = useState(false)

    useEffect(() => {
        if (open && postId) {
            loadPost()
            setActiveTab('overview')
        }
    }, [open, postId])

    useEffect(() => {
        if (open && postId && activeTab === 'likes') {
            loadLikes()
        }
    }, [open, postId, activeTab, likesPage])

    useEffect(() => {
        if (open && postId && activeTab === 'comments') {
            loadComments()
        }
    }, [open, postId, activeTab, commentsPage])

    useEffect(() => {
        if (open && postId && activeTab === 'shares') {
            loadShares()
        }
    }, [open, postId, activeTab, sharesPage])

    const loadPost = async () => {
        setLoading(true)
        try {
            const data = await getPostById(postId)
            setPost(data)
        } catch (error) {
            console.error('Failed to load post:', error)
            toast.error('Failed to load post details')
        } finally {
            setLoading(false)
        }
    }

    const loadLikes = async () => {
        setLikesLoading(true)
        try {
            const data = await getPostLikes(postId, likesPage, 10)
            if (likesPage === 0) {
                setLikes(data.content)
            } else {
                setLikes(prev => [...prev, ...data.content])
            }
            setLikesTotal(data.totalElements)
        } catch (error) {
            console.error('Failed to load likes:', error)
        } finally {
            setLikesLoading(false)
        }
    }

    const loadComments = async () => {
        setCommentsLoading(true)
        try {
            const data = await getPostComments(postId, commentsPage, 10)
            if (commentsPage === 0) {
                setComments(data.content)
            } else {
                setComments(prev => [...prev, ...data.content])
            }
            setCommentsTotal(data.totalElements)
        } catch (error) {
            console.error('Failed to load comments:', error)
        } finally {
            setCommentsLoading(false)
        }
    }

    const loadShares = async () => {
        setSharesLoading(true)
        try {
            const data = await getPostShares(postId, sharesPage, 10)
            if (sharesPage === 0) {
                setShares(data.content)
            } else {
                setShares(prev => [...prev, ...data.content])
            }
            setSharesTotal(data.totalElements)
        } catch (error) {
            console.error('Failed to load shares:', error)
        } finally {
            setSharesLoading(false)
        }
    }

    const handleAction = async (action: 'hide' | 'restore' | 'delete' | 'feature' | 'unfeature') => {
        if (!post) return

        setActionLoading(true)
        try {
            switch (action) {
                case 'hide':
                    await hidePost(post.id)
                    toast.success('Post hidden')
                    break
                case 'restore':
                    await restorePost(post.id)
                    toast.success('Post restored')
                    break
                case 'delete':
                    await deletePost(post.id)
                    toast.success('Post deleted')
                    break
                case 'feature':
                    await featurePostApi(post.id)
                    toast.success('Post featured')
                    break
                case 'unfeature':
                    await unfeaturePost(post.id)
                    toast.success('Post unfeatured')
                    break
            }
            loadPost()
            onPostUpdated?.()
        } catch (error) {
            console.error('Action failed:', error)
            toast.error('Action failed')
        } finally {
            setActionLoading(false)
        }
    }

    const statusConfig = post ? STATUS_CONFIG[post.status] : null

    // User Avatar component
    const UserAvatar = ({ user, size = 'md' }: { user: { userName: string; userAvatarUrl?: string; userPlan: string }; size?: 'sm' | 'md' }) => {
        const sizeClasses = size === 'sm' ? 'h-8 w-8 text-xs' : 'h-10 w-10 text-sm'
        return (
            <div className={`${sizeClasses} rounded-full bg-gradient-to-br from-indigo-400 to-purple-400 flex items-center justify-center text-white font-semibold ring-2 ring-white dark:ring-slate-700 shadow-md overflow-hidden`}>
                {user.userAvatarUrl ? (
                    <img src={user.userAvatarUrl} alt={user.userName} className="h-full w-full object-cover" />
                ) : (
                    user.userName?.charAt(0)?.toUpperCase() || 'U'
                )}
            </div>
        )
    }

    // Comment component with replies
    const CommentItem = ({ comment, isReply = false }: { comment: CommentWithReplies; isReply?: boolean }) => {
        const [showReplies, setShowReplies] = useState(false)

        return (
            <div className={`${isReply ? 'ml-8 border-l-2 border-slate-200 dark:border-slate-700 pl-4' : ''}`}>
                <div className="flex gap-3 py-3">
                    <div className="h-8 w-8 rounded-full bg-gradient-to-br from-indigo-400 to-purple-400 flex items-center justify-center text-white text-xs font-semibold ring-2 ring-white dark:ring-slate-700 shadow overflow-hidden shrink-0">
                        {comment.authorAvatarUrl ? (
                            <img src={comment.authorAvatarUrl} alt={comment.authorName} className="h-full w-full object-cover" />
                        ) : (
                            comment.authorName?.charAt(0)?.toUpperCase() || 'U'
                        )}
                    </div>
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-medium text-sm text-foreground">{comment.authorName}</span>
                            <Badge className={`text-[10px] px-1.5 py-0 ${PLAN_COLORS[comment.authorPlan]}`}>
                                {comment.authorPlan}
                            </Badge>
                            <span className="text-xs text-muted-foreground">{formatTimeAgo(comment.createdAt)}</span>
                        </div>
                        <p className="text-sm text-foreground/80 mt-1">{comment.content}</p>

                        {comment.replyCount > 0 && !isReply && (
                            <button
                                onClick={() => setShowReplies(!showReplies)}
                                className="flex items-center gap-1 text-xs text-indigo-600 dark:text-indigo-400 mt-2 hover:underline"
                            >
                                <Reply className="h-3 w-3" />
                                {showReplies ? 'Hide' : 'Show'} {comment.replyCount} {comment.replyCount === 1 ? 'reply' : 'replies'}
                                {showReplies ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
                            </button>
                        )}
                    </div>
                </div>

                {showReplies && comment.replies && (
                    <div className="space-y-0">
                        {comment.replies.map(reply => (
                            <CommentItem key={reply.id} comment={reply} isReply />
                        ))}
                    </div>
                )}
            </div>
        )
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-4xl p-0 gap-0 overflow-hidden bg-white dark:bg-slate-950 border-slate-200 dark:border-slate-800 sm:rounded-2xl max-h-[90vh] flex flex-col shadow-2xl">
                <DialogTitle className="sr-only">Post Details</DialogTitle>
                <DialogDescription className="sr-only">View post content, engagement, and moderation options</DialogDescription>

                {loading ? (
                    <div className="flex flex-col items-center justify-center py-24 space-y-4">
                        <div className="relative">
                            <div className="absolute inset-0 bg-indigo-500/20 blur-xl rounded-full" />
                            <Loader2 className="h-12 w-12 animate-spin text-indigo-600 relative z-10" />
                        </div>
                        <p className="text-muted-foreground font-medium">Loading post details...</p>
                    </div>
                ) : post ? (
                    <>
                        {/* Header */}
                        <div className="bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50 dark:from-indigo-950/30 dark:via-purple-950/30 dark:to-pink-950/30 border-b border-slate-200 dark:border-slate-800 p-5 shrink-0">
                            <div className="flex items-center justify-between gap-4">
                                <div className="flex items-center gap-4">
                                    <div className="h-14 w-14 rounded-full bg-gradient-to-br from-indigo-400 to-purple-400 flex items-center justify-center text-white font-bold text-lg ring-4 ring-white dark:ring-slate-800 shadow-lg overflow-hidden">
                                        {post.authorAvatarUrl ? (
                                            <img src={post.authorAvatarUrl} alt={post.authorName} className="h-full w-full object-cover" />
                                        ) : (
                                            post.authorName?.charAt(0)?.toUpperCase() || 'U'
                                        )}
                                    </div>
                                    <div>
                                        <div className="flex items-center gap-2">
                                            <h2 className="text-lg font-bold text-foreground">{post.authorName}</h2>
                                            {post.authorIsVerified && <Shield className="h-4 w-4 text-blue-500" />}
                                            <Badge className={`${PLAN_COLORS[post.authorPlan]} text-xs`}>
                                                {post.authorPlan === 'ELITE' && <Crown className="h-3 w-3 mr-1" />}
                                                {post.authorPlan}
                                            </Badge>
                                        </div>
                                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                            <span>@{post.authorUsername}</span>
                                            {post.contextualTitle && (
                                                <>
                                                    <span>•</span>
                                                    <span>{post.contextualTitle}</span>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                <div className="flex items-center gap-2">
                                    {statusConfig && (
                                        <Badge className={`${statusConfig.bg} ${statusConfig.text} gap-1`}>
                                            {post.isFeatured && <Star className="h-3 w-3" />}
                                            {statusConfig.label}
                                        </Badge>
                                    )}
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        onClick={() => onOpenChange(false)}
                                        className="rounded-full h-8 w-8"
                                    >
                                        <XCircle className="h-5 w-5 opacity-70" />
                                    </Button>
                                </div>
                            </div>
                        </div>

                        {/* Tabs */}
                        <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col overflow-hidden">
                            <div className="border-b border-slate-200 dark:border-slate-800 px-5 bg-slate-50/50 dark:bg-slate-900/50">
                                <TabsList className="h-12 bg-transparent gap-4">
                                    <TabsTrigger value="overview" className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 data-[state=active]:shadow-sm rounded-lg px-4">
                                        <TrendingUp className="h-4 w-4 mr-2" />
                                        Overview
                                    </TabsTrigger>
                                    <TabsTrigger value="likes" className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 data-[state=active]:shadow-sm rounded-lg px-4">
                                        <Heart className="h-4 w-4 mr-2 text-rose-500" />
                                        Likes
                                        <Badge variant="secondary" className="ml-2 h-5 px-1.5 text-xs">{post.likeCount}</Badge>
                                    </TabsTrigger>
                                    <TabsTrigger value="comments" className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 data-[state=active]:shadow-sm rounded-lg px-4">
                                        <MessageCircle className="h-4 w-4 mr-2 text-blue-500" />
                                        Comments
                                        <Badge variant="secondary" className="ml-2 h-5 px-1.5 text-xs">{post.commentCount}</Badge>
                                    </TabsTrigger>
                                    <TabsTrigger value="shares" className="data-[state=active]:bg-white dark:data-[state=active]:bg-slate-800 data-[state=active]:shadow-sm rounded-lg px-4">
                                        <Share2 className="h-4 w-4 mr-2 text-green-500" />
                                        Shares
                                        <Badge variant="secondary" className="ml-2 h-5 px-1.5 text-xs">{post.shareCount}</Badge>
                                    </TabsTrigger>
                                </TabsList>
                            </div>

                            <div className="flex-1 overflow-y-auto">
                                {/* Overview Tab */}
                                <TabsContent value="overview" className="m-0 p-6 space-y-6">
                                    {/* Content */}
                                    <div className="space-y-3">
                                        <h4 className="font-semibold text-sm text-muted-foreground uppercase tracking-wider">Content</h4>
                                        <div className="bg-slate-50 dark:bg-slate-900 p-4 rounded-xl border">
                                            <p className="whitespace-pre-wrap text-foreground">{post.content}</p>
                                        </div>
                                        {post.mediaUrl && (
                                            <div className="relative rounded-xl overflow-hidden border">
                                                <img src={post.mediaUrl} alt="Post media" className="w-full max-h-80 object-cover" />
                                                <Badge className="absolute top-2 right-2 gap-1 bg-black/50">
                                                    <Image className="h-3 w-3" />
                                                    Media
                                                </Badge>
                                            </div>
                                        )}
                                        {post.isEdited && (
                                            <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                                <Pencil className="h-4 w-4" />
                                                Edited {post.editCount} time(s)
                                            </div>
                                        )}
                                    </div>

                                    <Separator />

                                    {/* Engagement Stats */}
                                    <div className="grid grid-cols-3 gap-4">
                                        <div className="flex flex-col items-center p-4 bg-gradient-to-br from-rose-50 to-pink-50 dark:from-rose-900/20 dark:to-pink-900/20 rounded-xl border border-rose-100 dark:border-rose-800/30">
                                            <Heart className="h-6 w-6 text-rose-500 mb-1" />
                                            <span className="text-2xl font-bold">{post.likeCount}</span>
                                            <span className="text-xs text-muted-foreground">Likes</span>
                                        </div>
                                        <div className="flex flex-col items-center p-4 bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-xl border border-blue-100 dark:border-blue-800/30">
                                            <MessageCircle className="h-6 w-6 text-blue-500 mb-1" />
                                            <span className="text-2xl font-bold">{post.commentCount}</span>
                                            <span className="text-xs text-muted-foreground">Comments</span>
                                        </div>
                                        <div className="flex flex-col items-center p-4 bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 rounded-xl border border-green-100 dark:border-green-800/30">
                                            <Share2 className="h-6 w-6 text-green-500 mb-1" />
                                            <span className="text-2xl font-bold">{post.shareCount}</span>
                                            <span className="text-xs text-muted-foreground">Shares</span>
                                        </div>
                                    </div>

                                    <Separator />

                                    {/* Ranking Algorithm */}
                                    <div className="space-y-3">
                                        <h4 className="font-semibold text-sm text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                                            <Calculator className="h-4 w-4" />
                                            Ranking Algorithm
                                        </h4>
                                        <div className="grid grid-cols-2 gap-3">
                                            <div className="flex items-center justify-between p-3 bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 rounded-lg border border-indigo-100 dark:border-indigo-800/30">
                                                <span className="text-sm text-muted-foreground flex items-center gap-2">
                                                    <TrendingUp className="h-4 w-4 text-indigo-600" />
                                                    Ranking Score
                                                </span>
                                                <span className="font-mono font-bold text-lg text-indigo-600">{post.rankingScore?.toFixed(2) || '0.00'}</span>
                                            </div>
                                            <div className="flex items-center justify-between p-3 bg-gradient-to-r from-amber-50 to-yellow-50 dark:from-amber-900/20 dark:to-yellow-900/20 rounded-lg border border-amber-100 dark:border-amber-800/30">
                                                <span className="text-sm text-muted-foreground flex items-center gap-2">
                                                    <Zap className="h-4 w-4 text-amber-600" />
                                                    Plan Boost
                                                </span>
                                                <span className="font-mono font-bold text-amber-600">{post.subscriptionMultiplier?.toFixed(1) || '1.0'}×</span>
                                            </div>
                                            <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
                                                <span className="text-sm text-muted-foreground flex items-center gap-2">
                                                    <Calendar className="h-4 w-4" />
                                                    Created
                                                </span>
                                                <span className="text-sm">{formatTimeAgo(post.createdAt)}</span>
                                            </div>
                                            <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
                                                <span className="text-sm text-muted-foreground flex items-center gap-2">
                                                    <Timer className="h-4 w-4" />
                                                    Score Updated
                                                </span>
                                                <span className="text-sm">{post.scoreCalculatedAt ? formatTimeAgo(post.scoreCalculatedAt) : 'N/A'}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <Separator />

                                    {/* Actions */}
                                    <div className="flex flex-wrap gap-2">
                                        {post.isFeatured ? (
                                            <Button variant="outline" onClick={() => handleAction('unfeature')} disabled={actionLoading} className="gap-2">
                                                <StarOff className="h-4 w-4" />
                                                Unfeature
                                            </Button>
                                        ) : (
                                            <Button variant="outline" onClick={() => handleAction('feature')} disabled={actionLoading} className="gap-2 text-amber-600 border-amber-300 hover:bg-amber-50">
                                                <Star className="h-4 w-4" />
                                                Feature
                                            </Button>
                                        )}

                                        {post.status === 'HIDDEN' ? (
                                            <Button variant="outline" onClick={() => handleAction('restore')} disabled={actionLoading} className="gap-2 text-green-600 border-green-300 hover:bg-green-50">
                                                <Eye className="h-4 w-4" />
                                                Restore
                                            </Button>
                                        ) : post.status === 'ACTIVE' && (
                                            <Button variant="outline" onClick={() => handleAction('hide')} disabled={actionLoading} className="gap-2 text-orange-600 border-orange-300 hover:bg-orange-50">
                                                <EyeOff className="h-4 w-4" />
                                                Hide
                                            </Button>
                                        )}

                                        {post.status !== 'DELETED' && (
                                            <Button variant="outline" onClick={() => handleAction('delete')} disabled={actionLoading} className="gap-2 text-rose-600 border-rose-300 hover:bg-rose-50">
                                                <Trash2 className="h-4 w-4" />
                                                Delete
                                            </Button>
                                        )}

                                        {actionLoading && <Loader2 className="h-4 w-4 animate-spin ml-2" />}
                                    </div>
                                </TabsContent>

                                {/* Likes Tab */}
                                <TabsContent value="likes" className="m-0 p-6">
                                    {likesLoading && likes.length === 0 ? (
                                        <div className="flex items-center justify-center py-12">
                                            <Loader2 className="h-8 w-8 animate-spin text-rose-500" />
                                        </div>
                                    ) : likes.length === 0 ? (
                                        <div className="text-center py-12 text-muted-foreground">
                                            <Heart className="h-12 w-12 mx-auto mb-3 opacity-30" />
                                            <p>No likes yet</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            <p className="text-sm text-muted-foreground mb-4">
                                                Showing {likes.length} of {likesTotal} likes
                                            </p>
                                            {likes.map((user, i) => (
                                                <div key={i} className="flex items-center gap-3 p-3 bg-slate-50 dark:bg-slate-800/50 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
                                                    <UserAvatar user={user} />
                                                    <div className="flex-1 min-w-0">
                                                        <div className="flex items-center gap-2">
                                                            <span className="font-medium truncate">{user.userName}</span>
                                                            <Badge className={`text-[10px] px-1.5 py-0 ${PLAN_COLORS[user.userPlan]}`}>
                                                                {user.userPlan}
                                                            </Badge>
                                                        </div>
                                                        <span className="text-xs text-muted-foreground">
                                                            Liked {formatTimeAgo(user.engagedAt)}
                                                        </span>
                                                    </div>
                                                    <Heart className="h-4 w-4 text-rose-500 fill-rose-500" />
                                                </div>
                                            ))}
                                            {likes.length < likesTotal && (
                                                <Button
                                                    variant="outline"
                                                    onClick={() => setLikesPage(prev => prev + 1)}
                                                    disabled={likesLoading}
                                                    className="w-full mt-4"
                                                >
                                                    {likesLoading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                                                    Load More
                                                </Button>
                                            )}
                                        </div>
                                    )}
                                </TabsContent>

                                {/* Comments Tab */}
                                <TabsContent value="comments" className="m-0 p-6">
                                    {commentsLoading && comments.length === 0 ? (
                                        <div className="flex items-center justify-center py-12">
                                            <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
                                        </div>
                                    ) : comments.length === 0 ? (
                                        <div className="text-center py-12 text-muted-foreground">
                                            <MessageCircle className="h-12 w-12 mx-auto mb-3 opacity-30" />
                                            <p>No comments yet</p>
                                        </div>
                                    ) : (
                                        <div>
                                            <p className="text-sm text-muted-foreground mb-4">
                                                Showing {comments.length} of {commentsTotal} top-level comments
                                            </p>
                                            <div className="divide-y divide-slate-200 dark:divide-slate-700">
                                                {comments.map(comment => (
                                                    <CommentItem key={comment.id} comment={comment} />
                                                ))}
                                            </div>
                                            {comments.length < commentsTotal && (
                                                <Button
                                                    variant="outline"
                                                    onClick={() => setCommentsPage(prev => prev + 1)}
                                                    disabled={commentsLoading}
                                                    className="w-full mt-4"
                                                >
                                                    {commentsLoading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                                                    Load More
                                                </Button>
                                            )}
                                        </div>
                                    )}
                                </TabsContent>

                                {/* Shares Tab */}
                                <TabsContent value="shares" className="m-0 p-6">
                                    {sharesLoading && shares.length === 0 ? (
                                        <div className="flex items-center justify-center py-12">
                                            <Loader2 className="h-8 w-8 animate-spin text-green-500" />
                                        </div>
                                    ) : shares.length === 0 ? (
                                        <div className="text-center py-12 text-muted-foreground">
                                            <Share2 className="h-12 w-12 mx-auto mb-3 opacity-30" />
                                            <p>No shares yet</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            <p className="text-sm text-muted-foreground mb-4">
                                                Showing {shares.length} of {sharesTotal} shares
                                            </p>
                                            {shares.map((user, i) => (
                                                <div key={i} className="flex items-center gap-3 p-3 bg-slate-50 dark:bg-slate-800/50 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
                                                    <UserAvatar user={user} />
                                                    <div className="flex-1 min-w-0">
                                                        <div className="flex items-center gap-2">
                                                            <span className="font-medium truncate">{user.userName}</span>
                                                            <Badge className={`text-[10px] px-1.5 py-0 ${PLAN_COLORS[user.userPlan]}`}>
                                                                {user.userPlan}
                                                            </Badge>
                                                        </div>
                                                        <span className="text-xs text-muted-foreground">
                                                            Shared {formatTimeAgo(user.engagedAt)}
                                                        </span>
                                                    </div>
                                                    <Share2 className="h-4 w-4 text-green-500" />
                                                </div>
                                            ))}
                                            {shares.length < sharesTotal && (
                                                <Button
                                                    variant="outline"
                                                    onClick={() => setSharesPage(prev => prev + 1)}
                                                    disabled={sharesLoading}
                                                    className="w-full mt-4"
                                                >
                                                    {sharesLoading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                                                    Load More
                                                </Button>
                                            )}
                                        </div>
                                    )}
                                </TabsContent>
                            </div>
                        </Tabs>
                    </>
                ) : (
                    <div className="flex flex-col items-center justify-center py-24 text-muted-foreground">
                        <XCircle className="h-12 w-12 mb-2 opacity-30" />
                        <p>Post not found</p>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    )
}

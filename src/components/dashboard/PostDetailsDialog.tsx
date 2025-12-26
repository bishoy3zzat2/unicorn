import { useState, useEffect } from 'react'
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog'
import { Button } from '../ui/button'
import { Badge } from '../ui/badge'
import { Separator } from '../ui/separator'
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
    X,
    Calculator,
    Zap,
    Timer,
    AlertTriangle,
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
    PostData,
} from '../../api/feedApi'

interface PostDetailsDialogProps {
    postId: string
    open: boolean
    onOpenChange: (open: boolean) => void
    onPostUpdated?: () => void
}

// Plan badge colors
const PLAN_COLORS: Record<string, string> = {
    FREE: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
    PRO: 'bg-indigo-100 text-indigo-600 dark:bg-indigo-900/30 dark:text-indigo-400',
    ELITE: 'bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400',
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

    useEffect(() => {
        if (open && postId) {
            loadPost()
        }
    }, [open, postId])

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
                    await featurePostApi(post.id) // No duration - indefinite
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

    const formatDate = (dateString?: string) => {
        if (!dateString) return 'N/A'
        return new Date(dateString).toLocaleString()
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <TrendingUp className="h-5 w-5" />
                        Post Details
                    </DialogTitle>
                </DialogHeader>

                {loading ? (
                    <div className="flex items-center justify-center py-12">
                        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                    </div>
                ) : post ? (
                    <div className="space-y-6">
                        {/* Author Section */}
                        <div className="flex items-center gap-4 p-4 bg-slate-50 dark:bg-slate-800/50 rounded-xl">
                            <div className="h-14 w-14 rounded-full bg-gradient-to-br from-indigo-400 to-purple-400 flex items-center justify-center text-white font-bold text-lg ring-2 ring-white dark:ring-slate-700 shadow-md overflow-hidden">
                                {post.authorAvatarUrl ? (
                                    <img src={post.authorAvatarUrl} alt={post.authorName} className="h-full w-full object-cover" />
                                ) : (
                                    post.authorName?.charAt(0)?.toUpperCase() || 'U'
                                )}
                            </div>
                            <div className="flex-1">
                                <div className="flex items-center gap-2">
                                    <h3 className="font-semibold text-lg">{post.authorName}</h3>
                                    {post.authorIsVerified && <Shield className="h-4 w-4 text-blue-500" />}
                                </div>
                                <div className="flex items-center gap-2 mt-1">
                                    <Badge className={`${PLAN_COLORS[post.authorPlan]}`}>
                                        {post.authorPlan === 'ELITE' && <Crown className="h-3 w-3 mr-1" />}
                                        {post.authorPlan}
                                    </Badge>
                                    <span className="text-sm text-muted-foreground">@{post.authorUsername}</span>
                                    {post.contextualTitle && (
                                        <span className="text-sm text-muted-foreground">• {post.contextualTitle}</span>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Content Section */}
                        <div className="space-y-3">
                            <h4 className="font-semibold">Content</h4>
                            <div className="p-4 bg-white dark:bg-slate-900 border rounded-lg">
                                <p className="whitespace-pre-wrap">{post.content}</p>
                            </div>

                            {post.mediaUrl && (
                                <div className="relative rounded-lg overflow-hidden border">
                                    <img
                                        src={post.mediaUrl}
                                        alt="Post media"
                                        className="w-full max-h-96 object-cover"
                                    />
                                    <Badge className="absolute top-2 right-2 gap-1">
                                        <Image className="h-3 w-3" />
                                        Media
                                    </Badge>
                                </div>
                            )}

                            {post.isEdited && (
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <Pencil className="h-4 w-4" />
                                    Edited {post.editCount} time(s) • Last edited {formatTimeAgo(post.lastEditedAt || '')}
                                </div>
                            )}
                        </div>

                        <Separator />

                        {/* Engagement Stats */}
                        <div className="space-y-3">
                            <h4 className="font-semibold">Engagement</h4>
                            <div className="grid grid-cols-3 gap-4">
                                <div className="flex flex-col items-center p-4 bg-rose-50 dark:bg-rose-900/20 rounded-lg">
                                    <Heart className="h-6 w-6 text-rose-500 mb-1" />
                                    <span className="text-2xl font-bold">{post.likeCount}</span>
                                    <span className="text-xs text-muted-foreground">Likes</span>
                                </div>
                                <div className="flex flex-col items-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                                    <MessageCircle className="h-6 w-6 text-blue-500 mb-1" />
                                    <span className="text-2xl font-bold">{post.commentCount}</span>
                                    <span className="text-xs text-muted-foreground">Comments</span>
                                </div>
                                <div className="flex flex-col items-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                                    <Share2 className="h-6 w-6 text-green-500 mb-1" />
                                    <span className="text-2xl font-bold">{post.shareCount}</span>
                                    <span className="text-xs text-muted-foreground">Shares</span>
                                </div>
                            </div>
                        </div>

                        <Separator />

                        {/* Ranking Score Breakdown */}
                        <div className="space-y-3">
                            <h4 className="font-semibold flex items-center gap-2">
                                <Calculator className="h-4 w-4" />
                                Ranking Algorithm
                            </h4>
                            <div className="grid grid-cols-2 gap-3">
                                <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
                                    <span className="text-sm text-muted-foreground flex items-center gap-2">
                                        <TrendingUp className="h-4 w-4" />
                                        Final Score
                                    </span>
                                    <span className="font-mono font-bold text-lg">{post.rankingScore?.toFixed(4) || '0.0000'}</span>
                                </div>
                                <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
                                    <span className="text-sm text-muted-foreground flex items-center gap-2">
                                        <Zap className="h-4 w-4" />
                                        Plan Boost
                                    </span>
                                    <span className="font-mono font-bold">{post.subscriptionMultiplier?.toFixed(1) || '1.0'}×</span>
                                </div>
                                <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
                                    <span className="text-sm text-muted-foreground flex items-center gap-2">
                                        <Timer className="h-4 w-4" />
                                        Last Calculated
                                    </span>
                                    <span className="text-sm">{formatTimeAgo(post.scoreCalculatedAt || '')}</span>
                                </div>
                                <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
                                    <span className="text-sm text-muted-foreground flex items-center gap-2">
                                        <AlertTriangle className="h-4 w-4" />
                                        Edit Penalty
                                    </span>
                                    <span className="font-mono">{post.isEdited ? `${post.editCount} × 0.1` : 'None'}</span>
                                </div>
                            </div>
                        </div>

                        <Separator />

                        {/* Status & Moderation */}
                        <div className="space-y-3">
                            <h4 className="font-semibold">Status & Timeline</h4>
                            <div className="grid grid-cols-2 gap-3 text-sm">
                                <div className="flex justify-between p-2">
                                    <span className="text-muted-foreground">Status</span>
                                    <Badge className={
                                        post.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                                            post.status === 'HIDDEN' ? 'bg-orange-100 text-orange-700' :
                                                'bg-red-100 text-red-700'
                                    }>
                                        {post.isFeatured && <Star className="h-3 w-3 mr-1" />}
                                        {post.status}
                                    </Badge>
                                </div>
                                <div className="flex justify-between p-2">
                                    <span className="text-muted-foreground">Created</span>
                                    <span>{formatDate(post.createdAt)}</span>
                                </div>
                                {post.moderatedAt && (
                                    <>
                                        <div className="flex justify-between p-2">
                                            <span className="text-muted-foreground">Moderated At</span>
                                            <span>{formatDate(post.moderatedAt)}</span>
                                        </div>
                                        <div className="flex justify-between p-2">
                                            <span className="text-muted-foreground">Reason</span>
                                            <span>{post.moderationReason || 'N/A'}</span>
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>

                        <Separator />

                        {/* Actions */}
                        <div className="flex flex-wrap gap-2">
                            {post.isFeatured ? (
                                <Button
                                    variant="outline"
                                    onClick={() => handleAction('unfeature')}
                                    disabled={actionLoading}
                                    className="gap-2"
                                >
                                    <StarOff className="h-4 w-4" />
                                    Unfeature
                                </Button>
                            ) : (
                                <Button
                                    variant="outline"
                                    onClick={() => handleAction('feature')}
                                    disabled={actionLoading}
                                    className="gap-2 text-amber-600 border-amber-300 hover:bg-amber-50"
                                >
                                    <Star className="h-4 w-4" />
                                    Feature
                                </Button>
                            )}

                            {post.status === 'HIDDEN' ? (
                                <Button
                                    variant="outline"
                                    onClick={() => handleAction('restore')}
                                    disabled={actionLoading}
                                    className="gap-2 text-green-600 border-green-300 hover:bg-green-50"
                                >
                                    <Eye className="h-4 w-4" />
                                    Restore
                                </Button>
                            ) : post.status === 'ACTIVE' && (
                                <Button
                                    variant="outline"
                                    onClick={() => handleAction('hide')}
                                    disabled={actionLoading}
                                    className="gap-2 text-orange-600 border-orange-300 hover:bg-orange-50"
                                >
                                    <EyeOff className="h-4 w-4" />
                                    Hide
                                </Button>
                            )}

                            {post.status !== 'DELETED' && (
                                <Button
                                    variant="outline"
                                    onClick={() => handleAction('delete')}
                                    disabled={actionLoading}
                                    className="gap-2 text-rose-600 border-rose-300 hover:bg-rose-50"
                                >
                                    <Trash2 className="h-4 w-4" />
                                    Delete
                                </Button>
                            )}

                            {actionLoading && <Loader2 className="h-4 w-4 animate-spin ml-2" />}
                        </div>
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
                        <X className="h-12 w-12 mb-2" />
                        <p>Post not found</p>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    )
}

import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog';
import { MessageData } from '../../api/adminChatApi';
import { formatTimeAgo } from '../../lib/utils';
import { MessageSquare, User, Clock, Eye, Trash2, CheckCheck } from 'lucide-react';
import { cn } from '../../lib/utils';

interface ChatViewerDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    messages: MessageData[];
    chatTitle: string;
}

/**
 * Read-only chat viewer for admin dashboard.
 * Displays all messages in a chat conversation with a modern, premium design.
 */
export function ChatViewerDialog({
    open,
    onOpenChange,
    messages,
    chatTitle,
}: ChatViewerDialogProps) {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-2xl max-h-[85vh] p-0 overflow-hidden bg-gradient-to-b from-slate-50 to-white dark:from-slate-900 dark:to-slate-950 border-slate-200 dark:border-slate-800">
                {/* Header */}
                <DialogHeader className="p-5 pb-4 bg-gradient-to-r from-primary/10 via-primary/5 to-transparent border-b border-slate-200 dark:border-slate-800">
                    <div className="flex items-center gap-3">
                        <div className="h-11 w-11 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-lg shadow-primary/20">
                            <MessageSquare className="h-5 w-5 text-white" />
                        </div>
                        <div>
                            <DialogTitle className="text-lg font-bold text-slate-900 dark:text-white">{chatTitle}</DialogTitle>
                            <p className="text-xs text-muted-foreground mt-0.5 flex items-center gap-1.5">
                                <Eye className="h-3 w-3" />
                                Admin View • Read Only
                            </p>
                        </div>
                    </div>
                </DialogHeader>

                {/* Messages Container */}
                <div className="h-[55vh] overflow-y-auto px-5 py-4 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-slate-100/50 via-transparent to-transparent dark:from-slate-800/30">
                    <div className="space-y-5">
                        {messages.length === 0 ? (
                            <div className="flex flex-col items-center justify-center py-16 text-muted-foreground">
                                <div className="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-4">
                                    <MessageSquare className="h-8 w-8 opacity-30" />
                                </div>
                                <p className="font-medium">No messages yet</p>
                                <p className="text-xs opacity-70 mt-1">This conversation is empty.</p>
                            </div>
                        ) : (
                            messages.map((message, index) => {
                                const isEven = index % 2 === 0;
                                return (
                                    <div
                                        key={message.id}
                                        className={cn(
                                            "flex gap-3 group transition-all duration-200",
                                            message.isDeleted && "opacity-60"
                                        )}
                                    >
                                        {/* Avatar */}
                                        {message.senderAvatarUrl ? (
                                            <img
                                                src={message.senderAvatarUrl}
                                                alt={message.senderName}
                                                className="h-9 w-9 rounded-full flex-shrink-0 object-cover shadow-sm ring-2 ring-white dark:ring-slate-800"
                                            />
                                        ) : (
                                            <div className={cn(
                                                "h-9 w-9 rounded-full flex-shrink-0 flex items-center justify-center text-sm font-semibold shadow-sm",
                                                isEven
                                                    ? "bg-gradient-to-br from-violet-500 to-purple-600 text-white"
                                                    : "bg-gradient-to-br from-emerald-500 to-teal-600 text-white"
                                            )}>
                                                {message.senderName?.charAt(0)?.toUpperCase() || <User className="h-4 w-4" />}
                                            </div>
                                        )}

                                        {/* Message Content */}
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                                                <span className="font-semibold text-sm text-slate-800 dark:text-slate-200">
                                                    {message.senderName}
                                                </span>
                                                <span className="text-[10px] text-muted-foreground flex items-center gap-1">
                                                    <Clock className="h-2.5 w-2.5" />
                                                    {formatTimeAgo(new Date(message.createdAt))}
                                                </span>
                                                {message.isDeleted && (
                                                    <span className="text-[10px] bg-red-500/10 text-red-500 px-2 py-0.5 rounded-full font-medium flex items-center gap-1">
                                                        <Trash2 className="h-2.5 w-2.5" />
                                                        Deleted
                                                    </span>
                                                )}
                                            </div>
                                            <div
                                                className={cn(
                                                    "px-4 py-3 rounded-2xl rounded-tl-sm max-w-[90%] shadow-sm transition-shadow group-hover:shadow-md",
                                                    message.isDeleted
                                                        ? "bg-slate-200/70 dark:bg-slate-700/50 italic text-muted-foreground"
                                                        : isEven
                                                            ? "bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700"
                                                            : "bg-primary/5 dark:bg-primary/10 border border-primary/10"
                                                )}
                                            >
                                                <p className={cn(
                                                    "text-sm whitespace-pre-wrap break-words",
                                                    message.isDeleted ? "italic" : ""
                                                )}>
                                                    {message.isDeleted ? "This message has been deleted." : message.content}
                                                </p>
                                            </div>
                                            {message.isRead && message.readAt && !message.isDeleted && (
                                                <span className="text-[10px] text-muted-foreground mt-1.5 flex items-center gap-1">
                                                    <CheckCheck className="h-3 w-3 text-primary" />
                                                    Read {formatTimeAgo(new Date(message.readAt))}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>

                {/* Footer */}
                <div className="px-5 py-3 border-t border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-900/80 backdrop-blur-sm">
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                        <div className="flex items-center gap-2">
                            <MessageSquare className="h-3.5 w-3.5" />
                            <span className="font-medium">{messages.length}</span>
                            <span>message{messages.length !== 1 ? 's' : ''}</span>
                        </div>
                        <div className="flex items-center gap-1.5 text-primary/70">
                            <Eye className="h-3 w-3" />
                            <span className="font-medium">Admin Read-Only View</span>
                        </div>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
}


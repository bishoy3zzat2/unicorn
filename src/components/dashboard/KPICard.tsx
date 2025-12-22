import { Card, CardContent } from '../../components/ui/card'
import { LucideIcon, Info } from 'lucide-react'
import { cn } from '../../lib/utils'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../../components/ui/tooltip'

interface KPICardProps {
    title: string
    value: string
    icon: LucideIcon
    trend?: number | string
    iconColor?: string
    details?: React.ReactNode
    tooltip?: string
}

export function KPICard({ title, value, icon: Icon, trend, iconColor = "text-primary", details, tooltip }: KPICardProps) {
    const isNumberTrend = typeof trend === 'number';
    const isPositive = isNumberTrend ? (trend as number) > 0 : false

    // Dynamic gradient and icon background based on color
    const getGradientClass = (colorClass: string) => {
        if (colorClass.includes('blue')) return 'from-blue-500/5 via-blue-400/10 to-cyan-500/5 dark:from-blue-900/30 dark:via-blue-800/20 dark:to-cyan-900/30'
        if (colorClass.includes('green')) return 'from-green-500/5 via-emerald-400/10 to-teal-500/5 dark:from-green-900/30 dark:via-emerald-800/20 dark:to-teal-900/30'
        if (colorClass.includes('emerald')) return 'from-emerald-500/5 via-green-400/10 to-teal-500/5 dark:from-emerald-900/30 dark:via-green-800/20 dark:to-teal-900/30'
        if (colorClass.includes('purple')) return 'from-purple-500/5 via-violet-400/10 to-fuchsia-500/5 dark:from-purple-900/30 dark:via-violet-800/20 dark:to-fuchsia-900/30'
        if (colorClass.includes('orange')) return 'from-orange-500/5 via-amber-400/10 to-yellow-500/5 dark:from-orange-900/30 dark:via-amber-800/20 dark:to-yellow-900/30'
        if (colorClass.includes('yellow')) return 'from-yellow-500/5 via-amber-400/10 to-orange-500/5 dark:from-yellow-900/30 dark:via-amber-800/20 dark:to-orange-900/30'
        if (colorClass.includes('indigo')) return 'from-indigo-500/5 via-purple-400/10 to-blue-500/5 dark:from-indigo-900/30 dark:via-purple-800/20 dark:to-blue-900/30'
        if (colorClass.includes('red')) return 'from-red-500/5 via-rose-400/10 to-pink-500/5 dark:from-red-900/30 dark:via-rose-800/20 dark:to-pink-900/30'
        if (colorClass.includes('gray')) return 'from-gray-500/5 via-slate-400/10 to-zinc-500/5 dark:from-gray-800/30 dark:via-slate-700/20 dark:to-zinc-800/30'
        return 'from-primary/5 via-primary/10 to-primary/5 dark:from-primary/20 dark:via-primary/10 dark:to-primary/20'
    }

    const getIconGradient = (colorClass: string) => {
        if (colorClass.includes('blue')) return 'from-blue-500 to-cyan-500'
        if (colorClass.includes('green')) return 'from-green-500 to-emerald-500'
        if (colorClass.includes('emerald')) return 'from-emerald-500 to-teal-500'
        if (colorClass.includes('purple')) return 'from-purple-500 to-violet-500'
        if (colorClass.includes('orange')) return 'from-orange-500 to-amber-500'
        if (colorClass.includes('yellow')) return 'from-yellow-500 to-amber-500'
        if (colorClass.includes('indigo')) return 'from-indigo-500 to-purple-500'
        if (colorClass.includes('red')) return 'from-red-500 to-rose-500'
        if (colorClass.includes('gray')) return 'from-gray-500 to-slate-500'
        return 'from-primary to-primary/80'
    }

    const getBorderColor = (colorClass: string) => {
        if (colorClass.includes('blue')) return 'border-blue-200/50 dark:border-blue-800/50 hover:border-blue-400/70'
        if (colorClass.includes('green')) return 'border-green-200/50 dark:border-green-800/50 hover:border-green-400/70'
        if (colorClass.includes('emerald')) return 'border-emerald-200/50 dark:border-emerald-800/50 hover:border-emerald-400/70'
        if (colorClass.includes('purple')) return 'border-purple-200/50 dark:border-purple-800/50 hover:border-purple-400/70'
        if (colorClass.includes('orange')) return 'border-orange-200/50 dark:border-orange-800/50 hover:border-orange-400/70'
        if (colorClass.includes('yellow')) return 'border-yellow-200/50 dark:border-yellow-800/50 hover:border-yellow-400/70'
        if (colorClass.includes('indigo')) return 'border-indigo-200/50 dark:border-indigo-800/50 hover:border-indigo-400/70'
        if (colorClass.includes('red')) return 'border-red-200/50 dark:border-red-800/50 hover:border-red-400/70'
        if (colorClass.includes('gray')) return 'border-gray-200/50 dark:border-gray-700/50 hover:border-gray-400/70'
        return 'border-primary/20 hover:border-primary/50'
    }

    return (
        <Card className={cn(
            "group relative overflow-hidden transition-all duration-300 hover:-translate-y-1 border-2",
            "bg-gradient-to-br",
            getGradientClass(iconColor),
            getBorderColor(iconColor),
            "hover:shadow-xl dark:hover:shadow-2xl dark:hover:shadow-black/20"
        )}>
            {/* Dot Pattern Overlay */}
            <div
                className="absolute inset-0 opacity-[0.08] dark:opacity-[0.05] text-slate-900 dark:text-slate-100"
                style={{
                    backgroundImage: `radial-gradient(circle, currentColor 1px, transparent 1px)`,
                    backgroundSize: '16px 16px'
                }}
            />

            {/* Decorative Gradient Orb */}
            <div className={cn(
                "absolute -top-12 -right-12 w-32 h-32 rounded-full blur-3xl opacity-20 group-hover:opacity-30 transition-opacity",
                `bg-gradient-to-br ${getIconGradient(iconColor)}`
            )} />

            <CardContent className="p-6 relative z-10">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex-1">
                        <div className="flex items-center gap-1.5 mb-1">
                            <p className="text-sm font-medium text-muted-foreground">{title}</p>
                            {tooltip && (
                                <TooltipProvider>
                                    <Tooltip>
                                        <TooltipTrigger>
                                            <Info className="h-3.5 w-3.5 text-muted-foreground/70 hover:text-primary cursor-help" />
                                        </TooltipTrigger>
                                        <TooltipContent>
                                            <p className="max-w-[200px] text-xs">{tooltip}</p>
                                        </TooltipContent>
                                    </Tooltip>
                                </TooltipProvider>
                            )}
                        </div>
                        <h3 className="text-3xl font-bold bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text">{value}</h3>
                    </div>
                    <div className={cn(
                        "p-4 rounded-2xl transition-all duration-300 group-hover:scale-110 shadow-lg",
                        `bg-gradient-to-br ${getIconGradient(iconColor)}`
                    )}>
                        <Icon className="h-8 w-8 text-white" />
                    </div>
                </div>

                {/* Details Section */}
                {(details || trend !== undefined) && (
                    <div className="pt-3 border-t border-current/10 mt-2">
                        {trend !== undefined && (
                            <div className="flex items-center gap-1 mb-1">
                                {isNumberTrend ? (
                                    <>
                                        <span
                                            className={cn(
                                                "text-sm font-semibold",
                                                isPositive ? "text-emerald-500" : "text-red-500"
                                            )}
                                        >
                                            {isPositive ? "↑" : "↓"} {Math.abs(trend as number)}%
                                        </span>
                                        <span className="text-xs text-muted-foreground">vs last month</span>
                                    </>
                                ) : (
                                    <span className="text-xs text-muted-foreground">{trend}</span>
                                )}
                            </div>
                        )}
                        {details}
                    </div>
                )}
            </CardContent>
        </Card>
    )
}


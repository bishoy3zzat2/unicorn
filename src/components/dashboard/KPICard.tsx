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

    const getBackgroundClass = (colorClass: string) => {
        if (colorClass.includes('blue')) return 'bg-blue-500/10'
        if (colorClass.includes('green')) return 'bg-green-500/10'
        if (colorClass.includes('emerald')) return 'bg-emerald-500/10'
        if (colorClass.includes('purple')) return 'bg-purple-500/10'
        if (colorClass.includes('orange')) return 'bg-orange-500/10'
        if (colorClass.includes('yellow')) return 'bg-yellow-500/10'
        if (colorClass.includes('indigo')) return 'bg-indigo-500/10'
        if (colorClass.includes('red')) return 'bg-red-500/10'
        if (colorClass.includes('gray')) return 'bg-gray-500/10'
        return 'bg-primary/10'
    }

    return (
        <Card className="group hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border-2 hover:border-primary/50">
            <CardContent className="p-6">
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
                        <h3 className="text-3xl font-bold">{value}</h3>
                    </div>
                    <div className={cn(
                        "p-4 rounded-2xl transition-all duration-300 group-hover:scale-105",
                        getBackgroundClass(iconColor),
                        iconColor
                    )}>
                        <Icon className="h-8 w-8" />
                    </div>
                </div>

                {/* Details Section */}
                {(details || trend !== undefined) && (
                    <div className="pt-2 border-t mt-2">
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

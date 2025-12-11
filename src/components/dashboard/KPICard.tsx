import { Card, CardContent } from '../../components/ui/card'
import { LucideIcon } from 'lucide-react'
import { cn } from '../../lib/utils'

interface KPICardProps {
    title: string
    value: string
    icon: LucideIcon
    trend?: number
    iconColor?: string
}

export function KPICard({ title, value, icon: Icon, trend, iconColor = "text-primary" }: KPICardProps) {
    const isPositive = trend ? trend > 0 : false

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
                <div className="flex items-center justify-between">
                    <div className="flex-1">
                        <p className="text-sm font-medium text-muted-foreground mb-1">{title}</p>
                        <h3 className="text-3xl font-bold mb-2">{value}</h3>
                        {trend !== undefined && (
                            <div className="flex items-center gap-1">
                                <span
                                    className={cn(
                                        "text-sm font-semibold",
                                        isPositive ? "text-emerald-500" : "text-red-500"
                                    )}
                                >
                                    {isPositive ? "↑" : "↓"} {Math.abs(trend)}%
                                </span>
                                <span className="text-xs text-muted-foreground">vs last month</span>
                            </div>
                        )}
                    </div>
                    <div className={cn(
                        "p-4 rounded-2xl transition-all duration-300 group-hover:scale-105",
                        getBackgroundClass(iconColor),
                        iconColor
                    )}>
                        <Icon className="h-8 w-8" />
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}

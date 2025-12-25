import { useState } from 'react'
import { Button } from '../ui/button'
import { Input } from '../ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select'
import { Card, CardContent } from '../ui/card'
import { Label } from '../ui/label'
import { Switch } from '../ui/switch'
import {
    Filter, ChevronDown, ChevronUp, RotateCcw,
    Building2, Activity, DollarSign, Calendar, Layers, Mail, UserCog
} from 'lucide-react'
import { cn } from '../../lib/utils'

export interface StartupFilterState {
    // Text filters
    name?: string
    nameNegate?: boolean
    industry?: string
    industryNegate?: boolean

    ownerEmail?: string
    ownerEmailNegate?: boolean
    memberEmail?: string
    memberEmailNegate?: boolean

    // Select filters
    stage?: string
    stageNegate?: boolean
    status?: string
    statusNegate?: boolean

    // Numeric Range filters
    fundingGoalMin?: number
    fundingGoalMax?: number
    fundingGoalNegate?: boolean
    raisedAmountMin?: number
    raisedAmountMax?: number
    raisedAmountNegate?: boolean

    // Date filters
    createdAtFrom?: string
    createdAtTo?: string
    createdAtNegate?: boolean
}

interface StartupFiltersProps {
    filters: StartupFilterState
    onFiltersChange: (filters: StartupFilterState) => void
    onApply: () => void
    onClear: () => void
}

const STAGES = ['IDEA', 'MVP', 'GROWTH', 'SCALING', 'MATURE'] as const
const STATUSES = ['ACTIVE', 'BANNED'] as const

// Helper component for filter row with negate toggle - matching UserFilters design
const FilterRow = ({
    label,
    icon: Icon,
    children,
    negateValue,
    onNegateChange,
    hasValue
}: {
    label: string
    icon: React.ElementType
    children: React.ReactNode
    negateValue?: boolean
    onNegateChange: (checked: boolean) => void
    hasValue?: boolean
}) => (
    <div className={cn(
        "group space-y-2 p-3 rounded-xl border transition-all duration-200",
        // Default state
        !hasValue && "border-transparent hover:border-slate-200 dark:hover:border-slate-800 hover:bg-slate-50/50 dark:hover:bg-slate-800/10",
        // Active state (Green border)
        hasValue && !negateValue && "bg-slate-50/80 dark:bg-slate-900/50 border-emerald-500/50 shadow-sm shadow-emerald-500/10",
        // Negated state (Red border)
        hasValue && negateValue && "bg-slate-50/80 dark:bg-slate-900/50 border-red-500/50 shadow-sm shadow-red-500/10"
    )}>
        <div className="flex items-center justify-between">
            <Label className={cn(
                "flex items-center gap-2 text-sm font-medium transition-colors",
                hasValue && !negateValue ? "text-emerald-700 dark:text-emerald-400" :
                    hasValue && negateValue ? "text-red-700 dark:text-red-400" :
                        "text-slate-700 dark:text-slate-300"
            )}>
                <div className={cn(
                    "p-1.5 rounded-lg transition-colors",
                    hasValue && !negateValue ? "bg-emerald-100 text-emerald-600 dark:bg-emerald-500/20" :
                        hasValue && negateValue ? "bg-red-100 text-red-600 dark:bg-red-500/20" :
                            "bg-slate-100 dark:bg-slate-800 text-slate-500"
                )}>
                    <Icon className="h-4 w-4" />
                </div>
                {label}
            </Label>
            <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <span className={cn(
                    "text-[10px] uppercase font-bold tracking-wider",
                    negateValue ? "text-red-500" : "text-muted-foreground"
                )}>Exclude</span>
                <Switch
                    checked={negateValue || false}
                    onCheckedChange={onNegateChange}
                    className="scale-75 data-[state=checked]:bg-red-500"
                />
            </div>
        </div>
        <div className="transition-all duration-200 relative">
            {children}
        </div>
    </div>
)

export function StartupsFilters({ filters, onFiltersChange, onApply, onClear }: StartupFiltersProps) {
    const [expanded, setExpanded] = useState(false)

    const updateFilter = <K extends keyof StartupFilterState>(key: K, value: StartupFilterState[K]) => {
        onFiltersChange({ ...filters, [key]: value })
    }

    const activeFilterCount = Object.entries(filters).filter(([key, value]) => {
        if (key.endsWith('Negate')) return false
        return value !== undefined && value !== '' && value !== null
    }).length

    return (
        <Card className="mb-6 border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden transition-all duration-300 hover:shadow-md">
            <div
                className={cn(
                    "py-4 px-6 cursor-pointer transition-all duration-300 flex items-center justify-between select-none relative overflow-hidden",
                    expanded
                        ? "bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 text-white"
                        : "bg-white dark:bg-slate-950 hover:bg-slate-50 dark:hover:bg-slate-900"
                )}
                onClick={() => setExpanded(!expanded)}
            >
                {/* Background Pattern for expanded state */}
                {expanded && (
                    <div className="absolute inset-0 opacity-10 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-indigo-400 to-cyan-400 pointer-events-none" />
                )}

                <div className="flex items-center gap-3 relative z-10">
                    <div className={cn(
                        "h-10 w-10 rounded-xl flex items-center justify-center transition-all duration-300 shadow-sm",
                        expanded
                            ? "bg-white/10 text-white backdrop-blur-sm"
                            : "bg-primary/10 text-primary"
                    )}>
                        <Filter className="h-5 w-5" />
                    </div>
                    <div>
                        <h3 className={cn("text-base font-bold tracking-tight", expanded ? "text-white" : "text-foreground")}>
                            Advanced Filtering
                        </h3>
                        <p className={cn("text-xs font-medium", expanded ? "text-white/70" : "text-muted-foreground")}>
                            {activeFilterCount === 0
                                ? "Refine your startup search"
                                : `${activeFilterCount} active filters applied`
                            }
                        </p>
                    </div>
                </div>

                <div className="flex items-center gap-3 relative z-10">
                    {activeFilterCount > 0 && (
                        <span className={cn(
                            "px-3 py-1 rounded-full text-xs font-bold shadow-sm border",
                            expanded
                                ? "bg-white/20 text-white border-white/10 backdrop-blur-md"
                                : "bg-primary/10 text-primary border-primary/20"
                        )}>
                            {activeFilterCount} Active
                        </span>
                    )}
                    <div className={cn(
                        "p-2 rounded-lg transition-colors duration-200",
                        expanded ? "bg-white/10 text-white hover:bg-white/20" : "bg-slate-100 dark:bg-slate-800 text-muted-foreground"
                    )}>
                        {expanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                    </div>
                </div>
            </div>

            {expanded && (
                <div className="animate-in slide-in-from-top-4 duration-300 ease-out">
                    <CardContent className="p-6 bg-slate-50/50 dark:bg-slate-900/50 space-y-8">
                        {/* Text Filters Section */}
                        <div className="space-y-4">
                            <h4 className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-muted-foreground/80 pl-1">
                                <span className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                                Startup Information
                            </h4>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 bg-white dark:bg-slate-950 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
                                <FilterRow
                                    label="Startup Name"
                                    icon={Building2}
                                    negateValue={filters.nameNegate}
                                    onNegateChange={(checked) => updateFilter('nameNegate', checked)}
                                    hasValue={!!filters.name}
                                >
                                    <Input
                                        placeholder="Search name..."
                                        value={filters.name || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('name', e.target.value || undefined)}
                                        className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                    />
                                </FilterRow>

                                <FilterRow
                                    label="Industry"
                                    icon={Layers}
                                    negateValue={filters.industryNegate}
                                    onNegateChange={(checked) => updateFilter('industryNegate', checked)}
                                    hasValue={!!filters.industry}
                                >
                                    <Input
                                        placeholder="Search industry..."
                                        value={filters.industry || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('industry', e.target.value || undefined)}
                                        className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                    />
                                </FilterRow>

                                <FilterRow
                                    label="Owner Email"
                                    icon={Mail}
                                    negateValue={filters.ownerEmailNegate}
                                    onNegateChange={(checked) => updateFilter('ownerEmailNegate', checked)}
                                    hasValue={!!filters.ownerEmail}
                                >
                                    <Input
                                        placeholder="Search owner email..."
                                        value={filters.ownerEmail || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('ownerEmail', e.target.value || undefined)}
                                        className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                    />
                                </FilterRow>

                                <FilterRow
                                    label="Member Email"
                                    icon={UserCog}
                                    negateValue={filters.memberEmailNegate}
                                    onNegateChange={(checked) => updateFilter('memberEmailNegate', checked)}
                                    hasValue={!!filters.memberEmail}
                                >
                                    <Input
                                        placeholder="Search member email..."
                                        value={filters.memberEmail || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('memberEmail', e.target.value || undefined)}
                                        className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                    />
                                </FilterRow>
                            </div>
                        </div>

                        {/* Select Filters Section */}
                        <div className="space-y-4">
                            <h4 className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-muted-foreground/80 pl-1">
                                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500" />
                                Status & Stage
                            </h4>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-white dark:bg-slate-950 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
                                <FilterRow
                                    label="Stage"
                                    icon={Activity}
                                    negateValue={filters.stageNegate}
                                    onNegateChange={(checked) => updateFilter('stageNegate', checked)}
                                    hasValue={!!filters.stage}
                                >
                                    <Select
                                        value={filters.stage || '__ALL__'}
                                        onValueChange={(value: string) => updateFilter('stage', value === '__ALL__' ? undefined : value)}
                                    >
                                        <SelectTrigger className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                                            <SelectValue placeholder="All stages" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="__ALL__">All stages</SelectItem>
                                            {STAGES.map(stage => (
                                                <SelectItem key={stage} value={stage}>{stage}</SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </FilterRow>

                                <FilterRow
                                    label="Status"
                                    icon={Activity}
                                    negateValue={filters.statusNegate}
                                    onNegateChange={(checked) => updateFilter('statusNegate', checked)}
                                    hasValue={!!filters.status}
                                >
                                    <Select
                                        value={filters.status || '__ALL__'}
                                        onValueChange={(value: string) => updateFilter('status', value === '__ALL__' ? undefined : value)}
                                    >
                                        <SelectTrigger className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                                            <SelectValue placeholder="All statuses" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="__ALL__">All statuses</SelectItem>
                                            {STATUSES.map(status => (
                                                <SelectItem key={status} value={status}>{status}</SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </FilterRow>
                            </div>
                        </div>

                        {/* Numeric Range Filters */}
                        <div className="space-y-4">
                            <h4 className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-muted-foreground/80 pl-1">
                                <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
                                Financials
                            </h4>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-white dark:bg-slate-950 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
                                <FilterRow
                                    label="Funding Goal Range"
                                    icon={DollarSign}
                                    negateValue={filters.fundingGoalNegate}
                                    onNegateChange={(checked) => updateFilter('fundingGoalNegate', checked)}
                                    hasValue={filters.fundingGoalMin !== undefined || filters.fundingGoalMax !== undefined}
                                >
                                    <div className="flex gap-2">
                                        <Input
                                            type="number"
                                            placeholder="Min"
                                            value={filters.fundingGoalMin || ''}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('fundingGoalMin', e.target.value ? Number(e.target.value) : undefined)}
                                            className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                        />
                                        <div className="flex items-center text-muted-foreground">-</div>
                                        <Input
                                            type="number"
                                            placeholder="Max"
                                            value={filters.fundingGoalMax || ''}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('fundingGoalMax', e.target.value ? Number(e.target.value) : undefined)}
                                            className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                        />
                                    </div>
                                </FilterRow>

                                <FilterRow
                                    label="Raised Amount Range"
                                    icon={DollarSign}
                                    negateValue={filters.raisedAmountNegate}
                                    onNegateChange={(checked) => updateFilter('raisedAmountNegate', checked)}
                                    hasValue={filters.raisedAmountMin !== undefined || filters.raisedAmountMax !== undefined}
                                >
                                    <div className="flex gap-2">
                                        <Input
                                            type="number"
                                            placeholder="Min"
                                            value={filters.raisedAmountMin || ''}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('raisedAmountMin', e.target.value ? Number(e.target.value) : undefined)}
                                            className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                        />
                                        <div className="flex items-center text-muted-foreground">-</div>
                                        <Input
                                            type="number"
                                            placeholder="Max"
                                            value={filters.raisedAmountMax || ''}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('raisedAmountMax', e.target.value ? Number(e.target.value) : undefined)}
                                            className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 focus:bg-white dark:focus:bg-slate-950 transition-colors"
                                        />
                                    </div>
                                </FilterRow>
                            </div>
                        </div>

                        {/* Date Filters Section */}
                        <div className="space-y-4">
                            <h4 className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-muted-foreground/80 pl-1">
                                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                                Timeline
                            </h4>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-white dark:bg-slate-950 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
                                <FilterRow
                                    label="Created At"
                                    icon={Calendar}
                                    negateValue={filters.createdAtNegate}
                                    onNegateChange={(checked) => updateFilter('createdAtNegate', checked)}
                                    hasValue={!!(filters.createdAtFrom || filters.createdAtTo)}
                                >
                                    <div className="flex gap-2">
                                        <Input
                                            type="date"
                                            placeholder="From"
                                            value={filters.createdAtFrom || ''}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('createdAtFrom', e.target.value || undefined)}
                                            className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 dark:text-white dark:[color-scheme:dark]"
                                        />
                                        <div className="flex items-center text-muted-foreground">-</div>
                                        <Input
                                            type="date"
                                            placeholder="To"
                                            value={filters.createdAtTo || ''}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('createdAtTo', e.target.value || undefined)}
                                            className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-800 dark:text-white dark:[color-scheme:dark]"
                                        />
                                    </div>
                                </FilterRow>
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div className="flex justify-end gap-3 pt-6 border-t border-slate-200 dark:border-slate-800">
                            <Button
                                variant="outline"
                                onClick={onClear}
                                className="group gap-2 border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400"
                            >
                                <RotateCcw className="h-4 w-4 group-hover:-rotate-180 transition-transform duration-500" />
                                Clear All
                            </Button>
                            <Button
                                onClick={onApply}
                                className="gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md hover:shadow-lg transition-all hover:scale-[1.02]"
                            >
                                <Filter className="h-4 w-4" />
                                Apply Filters
                            </Button>
                        </div>
                    </CardContent>
                </div>
            )}
        </Card>
    )
}

import { useState } from 'react'
import { Button } from '../ui/button'
import { Input } from '../ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select'
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card'
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
const STATUSES = ['PENDING', 'APPROVED', 'REJECTED', 'ACTIVE', 'ARCHIVED'] as const

// Helper component for filter row with negate toggle
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
    <div className="space-y-2">
        <div className="flex items-center justify-between">
            <Label className="flex items-center gap-2 text-sm">
                <Icon className="h-4 w-4 text-muted-foreground" />
                {label}
            </Label>
            <div className="flex items-center gap-2">
                <span className="text-xs text-muted-foreground">Exclude</span>
                <Switch
                    checked={negateValue || false}
                    onCheckedChange={onNegateChange}
                    className="scale-75"
                />
            </div>
        </div>
        <div className={cn(
            "transition-all duration-200",
            hasValue && negateValue && "ring-2 ring-red-500/50 rounded-md",
            hasValue && !negateValue && "ring-2 ring-green-500/50 rounded-md"
        )}>
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
        <Card className="mb-4">
            <CardHeader
                className="py-3 px-4 cursor-pointer hover:bg-muted/50 transition-colors border-b border-border/40"
                onClick={() => setExpanded(!expanded)}
            >
                <div className="flex items-center justify-between">
                    <CardTitle className="text-sm font-medium flex items-center gap-2">
                        <Filter className="h-4 w-4" />
                        Advanced Filters
                        {activeFilterCount > 0 && (
                            <span className="ml-2 px-2 py-0.5 text-xs bg-primary text-primary-foreground rounded-full">
                                {activeFilterCount} active
                            </span>
                        )}
                    </CardTitle>
                    {expanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                </div>
            </CardHeader>

            {expanded && (
                <CardContent className="pt-6 pb-4 space-y-6">
                    {/* Text Filters Section */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Text Filters
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
                                />
                            </FilterRow>
                        </div>
                    </div>

                    {/* Select Filters Section */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Status & Stage
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                                    <SelectTrigger>
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
                                    <SelectTrigger>
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
                        <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Financials
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                                    />
                                    <Input
                                        type="number"
                                        placeholder="Max"
                                        value={filters.fundingGoalMax || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('fundingGoalMax', e.target.value ? Number(e.target.value) : undefined)}
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
                                    />
                                    <Input
                                        type="number"
                                        placeholder="Max"
                                        value={filters.raisedAmountMax || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('raisedAmountMax', e.target.value ? Number(e.target.value) : undefined)}
                                    />
                                </div>
                            </FilterRow>
                        </div>
                    </div>

                    {/* Date Filters Section */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Date Ranges
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                                        className="dark:text-white dark:[color-scheme:dark]"
                                    />
                                    <Input
                                        type="date"
                                        placeholder="To"
                                        value={filters.createdAtTo || ''}
                                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('createdAtTo', e.target.value || undefined)}
                                        className="dark:text-white dark:[color-scheme:dark]"
                                    />
                                </div>
                            </FilterRow>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex justify-end gap-2 pt-2 border-t">
                        <Button variant="outline" onClick={onClear} className="gap-2">
                            <RotateCcw className="h-4 w-4" />
                            Clear All
                        </Button>
                        <Button onClick={onApply} className="gap-2">
                            <Filter className="h-4 w-4" />
                            Apply Filters
                        </Button>
                    </div>
                </CardContent>
            )}
        </Card>
    )
}

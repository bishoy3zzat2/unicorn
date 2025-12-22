import { useState } from 'react'
import { Button } from '../ui/button'
import { Input } from '../ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select'
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card'
import { Label } from '../ui/label'
import { Switch } from '../ui/switch'
import {
    Filter, ChevronDown, ChevronUp, RotateCcw,
    Ban, User, Mail, Globe, Shield, Calendar, Briefcase
} from 'lucide-react'
import { cn } from '../../lib/utils'

export interface FilterState {
    // Text filters
    email?: string
    emailNegate?: boolean
    username?: string
    usernameNegate?: boolean
    firstName?: string
    firstNameNegate?: boolean
    lastName?: string
    lastNameNegate?: boolean
    country?: string
    countryNegate?: boolean

    // Select filters
    role?: string
    roleNegate?: boolean
    status?: string
    statusNegate?: boolean
    authProvider?: string
    authProviderNegate?: boolean

    // Date filters
    createdAtFrom?: string
    createdAtTo?: string
    createdAtNegate?: boolean
    lastLoginFrom?: string
    lastLoginTo?: string
    lastLoginNegate?: boolean

    // Boolean filters
    hasInvestorProfile?: boolean
    hasInvestorProfileNegate?: boolean
    hasStartups?: boolean
    hasStartupsNegate?: boolean
    isSuspended?: boolean
    isSuspendedNegate?: boolean

    // New additions
    minWarningCount?: number
    minWarningCountNegate?: boolean
    hasActiveSession?: boolean
    hasActiveSessionNegate?: boolean

    isMemberOfStartups?: boolean
    isMemberOfStartupsNegate?: boolean
}

interface UserFiltersProps {
    filters: FilterState
    onFiltersChange: (filters: FilterState) => void
    onApply: () => void
    onClear: () => void
}

const ROLES = ['ADMIN', 'INVESTOR', 'STARTUP_OWNER'] as const
const STATUSES = ['ACTIVE', 'SUSPENDED', 'BANNED', 'DELETED', 'PENDING_VERIFICATION'] as const
const AUTH_PROVIDERS = ['LOCAL', 'GOOGLE'] as const

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

export function UserFilters({ filters, onFiltersChange, onApply, onClear }: UserFiltersProps) {
    const [expanded, setExpanded] = useState(false)

    const updateFilter = <K extends keyof FilterState>(key: K, value: FilterState[K]) => {
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
                    <CardTitle className="text-sm font-semibold flex items-center gap-2 text-primary">
                        <Filter className="h-4 w-4" />
                        Advanced Filters
                        {activeFilterCount > 0 && (
                            <span className="ml-2 px-2 py-0.5 text-[10px] font-bold bg-primary text-primary-foreground rounded-full shadow-sm">
                                {activeFilterCount}
                            </span>
                        )}
                    </CardTitle>
                    <div className="flex items-center gap-2">
                        {activeFilterCount > 0 && expanded && (
                            <span className="text-xs text-muted-foreground mr-2 font-normal">
                                Click to collapse
                            </span>
                        )}
                        {expanded ? <ChevronUp className="h-4 w-4 text-muted-foreground" /> : <ChevronDown className="h-4 w-4 text-muted-foreground" />}
                    </div>
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
                                label="Email"
                                icon={Mail}
                                negateValue={filters.emailNegate}
                                onNegateChange={(checked) => updateFilter('emailNegate', checked)}
                                hasValue={!!filters.email}
                            >
                                <Input
                                    placeholder="Search email..."
                                    value={filters.email || ''}
                                    onChange={(e) => updateFilter('email', e.target.value || undefined)}
                                />
                            </FilterRow>

                            <FilterRow
                                label="Username"
                                icon={User}
                                negateValue={filters.usernameNegate}
                                onNegateChange={(checked) => updateFilter('usernameNegate', checked)}
                                hasValue={!!filters.username}
                            >
                                <Input
                                    placeholder="Search username..."
                                    value={filters.username || ''}
                                    onChange={(e) => updateFilter('username', e.target.value || undefined)}
                                />
                            </FilterRow>

                            <FilterRow
                                label="First Name"
                                icon={User}
                                negateValue={filters.firstNameNegate}
                                onNegateChange={(checked) => updateFilter('firstNameNegate', checked)}
                                hasValue={!!filters.firstName}
                            >
                                <Input
                                    placeholder="Search first name..."
                                    value={filters.firstName || ''}
                                    onChange={(e) => updateFilter('firstName', e.target.value || undefined)}
                                />
                            </FilterRow>

                            <FilterRow
                                label="Last Name"
                                icon={User}
                                negateValue={filters.lastNameNegate}
                                onNegateChange={(checked) => updateFilter('lastNameNegate', checked)}
                                hasValue={!!filters.lastName}
                            >
                                <Input
                                    placeholder="Search last name..."
                                    value={filters.lastName || ''}
                                    onChange={(e) => updateFilter('lastName', e.target.value || undefined)}
                                />
                            </FilterRow>

                            <FilterRow
                                label="Country"
                                icon={Globe}
                                negateValue={filters.countryNegate}
                                onNegateChange={(checked) => updateFilter('countryNegate', checked)}
                                hasValue={!!filters.country}
                            >
                                <Input
                                    placeholder="Search country..."
                                    value={filters.country || ''}
                                    onChange={(e) => updateFilter('country', e.target.value || undefined)}
                                />
                            </FilterRow>
                        </div>
                    </div>

                    {/* Select Filters Section */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            Role & Status
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <FilterRow
                                label="Role"
                                icon={Shield}
                                negateValue={filters.roleNegate}
                                onNegateChange={(checked) => updateFilter('roleNegate', checked)}
                                hasValue={!!filters.role}
                            >
                                <Select
                                    value={filters.role || '__ALL__'}
                                    onValueChange={(value) => updateFilter('role', value === '__ALL__' ? undefined : value)}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="All roles" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ALL__">All roles</SelectItem>
                                        {ROLES.map(role => (
                                            <SelectItem key={role} value={role}>{role}</SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </FilterRow>

                            <FilterRow
                                label="Status"
                                icon={Ban}
                                negateValue={filters.statusNegate}
                                onNegateChange={(checked) => updateFilter('statusNegate', checked)}
                                hasValue={!!filters.status}
                            >
                                <Select
                                    value={filters.status || '__ALL__'}
                                    onValueChange={(value) => updateFilter('status', value === '__ALL__' ? undefined : value)}
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

                            <FilterRow
                                label="Auth Provider"
                                icon={Shield}
                                negateValue={filters.authProviderNegate}
                                onNegateChange={(checked) => updateFilter('authProviderNegate', checked)}
                                hasValue={!!filters.authProvider}
                            >
                                <Select
                                    value={filters.authProvider || '__ALL__'}
                                    onValueChange={(value) => updateFilter('authProvider', value === '__ALL__' ? undefined : value)}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="All providers" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ALL__">All providers</SelectItem>
                                        {AUTH_PROVIDERS.map(provider => (
                                            <SelectItem key={provider} value={provider}>{provider}</SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
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
                                        onChange={(e) => updateFilter('createdAtFrom', e.target.value || undefined)}
                                        className="dark:text-white dark:[color-scheme:dark]"
                                    />
                                    <Input
                                        type="date"
                                        placeholder="To"
                                        value={filters.createdAtTo || ''}
                                        onChange={(e) => updateFilter('createdAtTo', e.target.value || undefined)}
                                        className="dark:text-white dark:[color-scheme:dark]"
                                    />
                                </div>
                            </FilterRow>

                            <FilterRow
                                label="Last Login"
                                icon={Calendar}
                                negateValue={filters.lastLoginNegate}
                                onNegateChange={(checked) => updateFilter('lastLoginNegate', checked)}
                                hasValue={!!(filters.lastLoginFrom || filters.lastLoginTo)}
                            >
                                <div className="flex gap-2">
                                    <Input
                                        type="date"
                                        placeholder="From"
                                        value={filters.lastLoginFrom || ''}
                                        onChange={(e) => updateFilter('lastLoginFrom', e.target.value || undefined)}
                                        className="dark:text-white dark:[color-scheme:dark]"
                                    />
                                    <Input
                                        type="date"
                                        placeholder="To"
                                        value={filters.lastLoginTo || ''}
                                        onChange={(e) => updateFilter('lastLoginTo', e.target.value || undefined)}
                                        className="dark:text-white dark:[color-scheme:dark]"
                                    />
                                </div>
                            </FilterRow>
                        </div>
                    </div>

                    {/* Boolean Filters Section */}
                    <div className="space-y-4">
                        <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                            User Properties
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div className="flex items-center justify-between p-3 border rounded-lg">
                                <div className="flex items-center gap-2">
                                    <User className="h-4 w-4 text-muted-foreground" />
                                    <span className="text-sm">Has Investor Profile</span>
                                </div>
                                <Select
                                    value={filters.hasInvestorProfile === undefined ? '__ANY__' : String(filters.hasInvestorProfile)}
                                    onValueChange={(value) => updateFilter('hasInvestorProfile', value === '__ANY__' ? undefined : value === 'true')}
                                >
                                    <SelectTrigger className="w-24">
                                        <SelectValue placeholder="Any" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ANY__">Any</SelectItem>
                                        <SelectItem value="true">Yes</SelectItem>
                                        <SelectItem value="false">No</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="flex items-center justify-between p-3 border rounded-lg">
                                <div className="flex items-center gap-2">
                                    <Briefcase className="h-4 w-4 text-muted-foreground" />
                                    <span className="text-sm">Has Startups</span>
                                </div>
                                <Select
                                    value={filters.hasStartups === undefined ? '__ANY__' : String(filters.hasStartups)}
                                    onValueChange={(value) => updateFilter('hasStartups', value === '__ANY__' ? undefined : value === 'true')}
                                >
                                    <SelectTrigger className="w-24">
                                        <SelectValue placeholder="Any" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ANY__">Any</SelectItem>
                                        <SelectItem value="true">Yes</SelectItem>
                                        <SelectItem value="false">No</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="flex items-center justify-between p-3 border rounded-lg">
                                <div className="flex items-center gap-2">
                                    <Briefcase className="h-4 w-4 text-muted-foreground" />
                                    <span className="text-sm">Is Member of Startups (Joined)</span>
                                </div>
                                <Select
                                    value={filters.isMemberOfStartups === undefined ? '__ANY__' : String(filters.isMemberOfStartups)}
                                    onValueChange={(value) => updateFilter('isMemberOfStartups', value === '__ANY__' ? undefined : value === 'true')}
                                >
                                    <SelectTrigger className="w-24">
                                        <SelectValue placeholder="Any" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ANY__">Any</SelectItem>
                                        <SelectItem value="true">Yes</SelectItem>
                                        <SelectItem value="false">No</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="flex items-center justify-between p-3 border rounded-lg">
                                <div className="flex items-center gap-2">
                                    <Ban className="h-4 w-4 text-muted-foreground" />
                                    <span className="text-sm">Is Suspended</span>
                                </div>
                                <Select
                                    value={filters.isSuspended === undefined ? '__ANY__' : String(filters.isSuspended)}
                                    onValueChange={(value) => updateFilter('isSuspended', value === '__ANY__' ? undefined : value === 'true')}
                                >
                                    <SelectTrigger className="w-24">
                                        <SelectValue placeholder="Any" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ANY__">Any</SelectItem>
                                        <SelectItem value="true">Yes</SelectItem>
                                        <SelectItem value="false">No</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="flex items-center justify-between p-3 border rounded-lg">
                                <div className="flex items-center gap-2">
                                    <Shield className="h-4 w-4 text-muted-foreground" />
                                    <span className="text-sm">Has Active Session</span>
                                </div>
                                <Select
                                    value={filters.hasActiveSession === undefined ? '__ANY__' : String(filters.hasActiveSession)}
                                    onValueChange={(value) => updateFilter('hasActiveSession', value === '__ANY__' ? undefined : value === 'true')}
                                >
                                    <SelectTrigger className="w-24">
                                        <SelectValue placeholder="Any" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="__ANY__">Any</SelectItem>
                                        <SelectItem value="true">Yes</SelectItem>
                                        <SelectItem value="false">No</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        {/* Numeric Filters */}
                        <div className="space-y-4 pt-4 border-t">
                            <h4 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                                Metrics
                            </h4>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <FilterRow
                                    label="Min Warnings"
                                    icon={Shield}
                                    negateValue={filters.minWarningCountNegate}
                                    onNegateChange={(checked) => updateFilter('minWarningCountNegate', checked)}
                                    hasValue={filters.minWarningCount !== undefined}
                                >
                                    <Input
                                        type="number"
                                        min="0"
                                        placeholder="0"
                                        value={filters.minWarningCount || ''}
                                        onChange={(e) => updateFilter('minWarningCount', e.target.value ? parseInt(e.target.value) : undefined)}
                                    />
                                </FilterRow>
                            </div>
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

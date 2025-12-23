import { useState, useEffect } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { toast } from "sonner"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "../ui/dialog"
import { Button } from "../ui/button"
import { Input } from "../ui/input"
import { Label } from "../ui/label"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../ui/select"
import { searchUsers, addStartupMember } from "../../lib/api"
import { User } from "../../types"
import { User as UserIcon, Loader2, Calendar, ShieldCheck, UserCheck, UserX } from "lucide-react"

const addMemberSchema = z.object({
    role: z.string().min(1, "Role is required"),
    joinedAt: z.string().min(1, "Joined Date is required"),
    leftAt: z.string().optional(),
})

type AddMemberFormValues = z.infer<typeof addMemberSchema>

interface AddMemberDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    startupId: string
    existingMemberIds: string[]
    onSuccess: () => void
}

export function AddMemberDialog({
    open,
    onOpenChange,
    startupId,
    existingMemberIds,
    onSuccess,
}: AddMemberDialogProps) {
    const [isLoading, setIsLoading] = useState(false)
    const [searchQuery, setSearchQuery] = useState("")
    const [searchResults, setSearchResults] = useState<User[]>([])
    const [isSearching, setIsSearching] = useState(false)
    const [selectedUser, setSelectedUser] = useState<User | null>(null)
    const [isActive, setIsActive] = useState(true)

    const form = useForm<AddMemberFormValues>({
        resolver: zodResolver(addMemberSchema),
        defaultValues: {
            role: "DEVELOPER",
            joinedAt: new Date().toISOString().split('T')[0],
            leftAt: "",
        },
    })

    // Search users with debounce
    useEffect(() => {
        const timer = setTimeout(() => {
            if (searchQuery.trim().length >= 2) {
                performSearch(searchQuery)
            } else {
                setSearchResults([])
            }
        }, 300)

        return () => clearTimeout(timer)
    }, [searchQuery])

    const performSearch = async (query: string) => {
        setIsSearching(true)
        try {
            // Filter by STARTUP_OWNER role as requested (User referred to "Business Owner")
            // Also enforce ACTIVE status
            const response = await searchUsers(query, "STARTUP_OWNER", undefined, "ACTIVE")
            // Filter out existing members
            const filteredUsers = response.content.filter(user => !existingMemberIds.includes(user.id))
            setSearchResults(filteredUsers)
        } catch (error) {
            console.error("Failed to search users", error)
        } finally {
            setIsSearching(false)
        }
    }

    const handleSelectUser = (user: User) => {
        setSelectedUser(user)
        setSearchQuery("")
        setSearchResults([])
    }

    const clearSelectedUser = () => {
        setSelectedUser(null)
    }

    const onSubmit = async (values: AddMemberFormValues) => {
        if (!selectedUser) {
            toast.error("Please select a user")
            return
        }

        setIsLoading(true)
        try {
            // Logic to handle "today" as "now"
            const today = new Date().toISOString().split('T')[0]
            let joinedAtISO = values.joinedAt

            if (values.joinedAt === today) {
                joinedAtISO = new Date().toISOString()
            } else {
                // If picking a past date, simplistic approach: use T00:00:00 or similar
                // But generally new Date(dateString) defaults to UTC midnight or local depending on parsing
                // To be safe and consistent with previous logic:
                joinedAtISO = new Date(values.joinedAt).toISOString()
            }

            // Handle leftAt date
            let leftAtISO: string | null = null
            if (!isActive && form.getValues("leftAt")) {
                const leftAtValue = form.getValues("leftAt")
                if (leftAtValue) {
                    leftAtISO = new Date(leftAtValue).toISOString()
                }
            }

            await addStartupMember(
                startupId,
                selectedUser.id,
                values.role,
                joinedAtISO,
                leftAtISO
            )

            toast.success("Member added successfully")
            onSuccess()
            onOpenChange(false)

            // Reset form/state
            form.reset()
            setSelectedUser(null)
            setSearchQuery("")
            setIsActive(true)

        } catch (error) {
            console.error("Failed to add member", error)
            toast.error("Failed to add member")
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px] max-h-[85vh] overflow-hidden flex flex-col p-0 gap-0 bg-white dark:bg-slate-950 border-none shadow-2xl">
                {/* Gradient Header */}
                <div className="bg-gradient-to-r from-slate-800 via-purple-900/50 to-indigo-900/50 dark:from-slate-900 dark:via-purple-950/80 dark:to-indigo-950/80 p-6 shrink-0 border-b border-slate-700/50">
                    <DialogHeader className="space-y-2">
                        <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white">
                            <div className="h-12 w-12 rounded-2xl bg-white/20 backdrop-blur-sm shadow-lg flex items-center justify-center">
                                <UserIcon className="h-6 w-6 text-white" />
                            </div>
                            Add Team Member
                        </DialogTitle>
                        <DialogDescription className="text-white/80">
                            Search for a user and assign them a role in the startup.
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col flex-1 overflow-hidden">
                    <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-white dark:bg-slate-950">
                        {/* User Selection */}
                        <div className="space-y-3 relative">
                            <Label className="flex items-center gap-2 text-sm font-semibold text-purple-600 dark:text-purple-400">
                                <UserIcon className="h-4 w-4" />
                                Select User *
                            </Label>

                            {selectedUser ? (
                                <div className="flex items-center justify-between p-4 border-2 border-purple-200 dark:border-purple-800/50 rounded-xl bg-purple-50 dark:bg-purple-950/30">
                                    <div className="flex items-center gap-3 overflow-hidden">
                                        {selectedUser.avatarUrl ? (
                                            <img src={selectedUser.avatarUrl} alt={selectedUser.email} className="h-10 w-10 rounded-full object-cover ring-2 ring-purple-500/30" />
                                        ) : (
                                            <div className="h-10 w-10 rounded-full bg-gradient-to-br from-purple-500 to-indigo-500 flex items-center justify-center shadow-lg">
                                                <span className="text-sm font-bold text-white">
                                                    {(selectedUser.firstName && selectedUser.lastName)
                                                        ? `${selectedUser.firstName[0]}${selectedUser.lastName[0]}`
                                                        : selectedUser.email.substring(0, 2).toUpperCase()}
                                                </span>
                                            </div>
                                        )}
                                        <div className="min-w-0">
                                            <p className="text-sm font-semibold truncate text-foreground">
                                                {selectedUser.firstName ? `${selectedUser.firstName} ${selectedUser.lastName}` : selectedUser.email.split('@')[0]}
                                            </p>
                                            <p className="text-xs text-muted-foreground truncate">{selectedUser.email}</p>
                                        </div>
                                    </div>
                                    <Button type="button" variant="ghost" size="sm" onClick={clearSelectedUser} className="h-8 w-8 p-0 text-purple-600 hover:bg-purple-100 dark:hover:bg-purple-900/30">
                                        &times;
                                    </Button>
                                </div>
                            ) : (
                                <div className="relative">
                                    <Input
                                        placeholder="Search by name or email..."
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                        className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-lg h-11 pl-4 pr-10"
                                    />
                                    {isSearching && (
                                        <div className="absolute right-3 top-3">
                                            <Loader2 className="h-5 w-5 animate-spin text-purple-500" />
                                        </div>
                                    )}
                                    {searchResults.length > 0 && (
                                        <div className="absolute z-50 w-full mt-2 bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-xl shadow-xl max-h-[200px] overflow-auto">
                                            {searchResults.map(user => (
                                                <div
                                                    key={user.id}
                                                    className="p-3 hover:bg-purple-50 dark:hover:bg-purple-900/20 cursor-pointer text-sm flex items-center gap-3 border-b border-slate-100 dark:border-slate-800 last:border-0 transition-colors"
                                                    onClick={() => handleSelectUser(user)}
                                                >
                                                    {user.avatarUrl ? (
                                                        <img src={user.avatarUrl} alt="" className="h-9 w-9 rounded-full object-cover" />
                                                    ) : (
                                                        <div className="h-9 w-9 rounded-full bg-gradient-to-br from-slate-400 to-slate-500 flex items-center justify-center shrink-0">
                                                            <UserIcon className="h-4 w-4 text-white" />
                                                        </div>
                                                    )}
                                                    <div className="min-w-0">
                                                        <div className="font-medium truncate text-foreground">
                                                            {user.firstName ? `${user.firstName} ${user.lastName}` : user.email}
                                                        </div>
                                                        <div className="text-xs text-muted-foreground truncate">{user.email}</div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            {/* Role Select */}
                            <div className="space-y-2">
                                <Label htmlFor="role" className="flex items-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400">
                                    <ShieldCheck className="h-4 w-4" />
                                    Role *
                                </Label>
                                <Select
                                    onValueChange={(value) => form.setValue("role", value)}
                                    defaultValue={form.getValues("role")}
                                >
                                    <SelectTrigger className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-lg h-11">
                                        <SelectValue placeholder="Select role" />
                                    </SelectTrigger>
                                    <SelectContent className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
                                        <SelectItem value="FOUNDER">Founder</SelectItem>
                                        <SelectItem value="CO_FOUNDER">Co-Founder</SelectItem>
                                        <SelectItem value="CEO">CEO</SelectItem>
                                        <SelectItem value="CTO">CTO</SelectItem>
                                        <SelectItem value="COO">COO</SelectItem>
                                        <SelectItem value="CFO">CFO</SelectItem>
                                        <SelectItem value="CMO">CMO</SelectItem>
                                        <SelectItem value="CHIEF_PRODUCT_OFFICER">CPO</SelectItem>
                                        <SelectItem value="DEVELOPER">Developer</SelectItem>
                                        <SelectItem value="DESIGNER">Designer</SelectItem>
                                        <SelectItem value="OTHER">Other</SelectItem>
                                    </SelectContent>
                                </Select>
                                {form.formState.errors.role && (
                                    <p className="text-sm text-destructive">{form.formState.errors.role.message}</p>
                                )}
                            </div>

                            {/* Joined Date */}
                            <div className="space-y-2">
                                <Label htmlFor="joinedAt" className="flex items-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400">
                                    <Calendar className="h-4 w-4" />
                                    Joined Date *
                                </Label>
                                <Input
                                    id="joinedAt"
                                    type="date"
                                    {...form.register("joinedAt")}
                                    className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-lg h-11"
                                />
                                {form.formState.errors.joinedAt && (
                                    <p className="text-sm text-destructive">{form.formState.errors.joinedAt.message}</p>
                                )}
                            </div>
                        </div>

                        {/* Active Status Toggle */}
                        <div className="space-y-3">
                            <Label className="flex items-center gap-2 text-sm font-semibold text-slate-600 dark:text-slate-400">
                                {isActive ? <UserCheck className="h-4 w-4 text-emerald-500" /> : <UserX className="h-4 w-4 text-slate-400" />}
                                Member Status
                            </Label>
                            <div className="flex items-center gap-4">
                                <button
                                    type="button"
                                    onClick={() => setIsActive(true)}
                                    className={`flex-1 p-3 rounded-xl border-2 transition-all ${isActive
                                        ? "border-emerald-500 bg-emerald-50 dark:bg-emerald-950/30"
                                        : "border-slate-200 dark:border-slate-700 hover:border-slate-300"
                                        }`}
                                >
                                    <div className="flex items-center justify-center gap-2">
                                        <UserCheck className={`h-5 w-5 ${isActive ? "text-emerald-600" : "text-slate-400"}`} />
                                        <span className={`font-medium ${isActive ? "text-emerald-700 dark:text-emerald-400" : "text-slate-500"}`}>Active</span>
                                    </div>
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setIsActive(false)}
                                    className={`flex-1 p-3 rounded-xl border-2 transition-all ${!isActive
                                        ? "border-slate-500 bg-slate-100 dark:bg-slate-800"
                                        : "border-slate-200 dark:border-slate-700 hover:border-slate-300"
                                        }`}
                                >
                                    <div className="flex items-center justify-center gap-2">
                                        <UserX className={`h-5 w-5 ${!isActive ? "text-slate-600" : "text-slate-400"}`} />
                                        <span className={`font-medium ${!isActive ? "text-slate-700 dark:text-slate-300" : "text-slate-500"}`}>Left</span>
                                    </div>
                                </button>
                            </div>
                        </div>

                        {/* Left Date - Only show when NOT active */}
                        {!isActive && (
                            <div className="space-y-2 animate-in fade-in slide-in-from-top-2">
                                <Label htmlFor="leftAt" className="flex items-center gap-2 text-sm font-semibold text-red-600 dark:text-red-400">
                                    <Calendar className="h-4 w-4" />
                                    Left Date *
                                </Label>
                                <Input
                                    id="leftAt"
                                    type="date"
                                    {...form.register("leftAt")}
                                    className="bg-white dark:bg-slate-900 border-2 border-red-200 dark:border-red-800/50 rounded-lg h-11 focus:ring-red-500"
                                />
                            </div>
                        )}
                    </div>

                    {/* Footer */}
                    <div className="flex items-center justify-end gap-3 p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50 shrink-0">
                        <Button type="button" variant="outline" onClick={() => onOpenChange(false)} className="bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-700">
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            disabled={isLoading || !selectedUser}
                            className="bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white shadow-lg shadow-purple-500/25"
                        >
                            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Add Member
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    )
}

import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"
import { toast } from "sonner"


import { Button } from "../ui/button"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "../ui/dialog"
import { Input } from "../ui/input"
import { Label } from "../ui/label"
import { Textarea } from "../ui/textarea"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "../ui/select"
import { createStartup, searchUsers, searchStartups, addStartupMember } from "../../lib/api"
import { StartupRole, StartupStage, User, Startup } from "../../types"
import { User as UserIcon, Loader2, Upload, Building2, DollarSign, Globe, FileText, Link, Facebook, Instagram, Twitter, Users, PlusCircle, Search } from "lucide-react"


const INDUSTRY_OPTIONS = [
    "Fintech",
    "Health Tech",
    "Ed Tech",
    "E-commerce",
    "SaaS",
    "AI & ML",
    "Blockchain",
    "Clean Tech",
    "Agri Tech",
    "Real Estate",
    "Entertainment",
    "Logistics",
    "Manufacturing",
    "Bio Tech",
    "Cybersecurity",
    "Other"
];

const startupSchema = z.object({
    name: z.string().min(2, "Name must be at least 2 characters"),
    tagline: z.string().max(80, "Tagline must be less than 80 characters").optional(),
    fullDescription: z.string().max(200, "Description must be less than 200 characters").optional(),
    industry: z.string().min(2, "Industry is required"),
    stage: z.string().min(1, "Stage is required"),
    fundingGoal: z.string().refine((val) => !isNaN(Number(val)) && Number(val) > 0, {
        message: "Funding goal must be a positive number",
    }),
    websiteUrl: z.string().optional().or(z.literal("")),
    logoUrl: z.string().optional().or(z.literal("")),
    coverUrl: z.string().optional().or(z.literal("")),
    facebookUrl: z.string().optional().or(z.literal("")),
    instagramUrl: z.string().optional().or(z.literal("")),
    twitterUrl: z.string().optional().or(z.literal("")),
    pitchDeckUrl: z.string().optional().or(z.literal("")),
    financialDocumentsUrl: z.string().optional().or(z.literal("")),
    businessPlanUrl: z.string().optional().or(z.literal("")),
    businessModelUrl: z.string().optional().or(z.literal("")),
    ownerId: z.string().min(1, "Owner is required"),
    ownerRole: z.string().min(1, "Owner Role is required"),
})

type StartupFormValues = z.infer<typeof startupSchema>

interface CreateStartupDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess: () => void
}

export function CreateStartupDialog({
    open,
    onOpenChange,
    onSuccess,
}: CreateStartupDialogProps) {
    const [isLoading, setIsLoading] = useState(false)

    const form = useForm<StartupFormValues>({
        resolver: zodResolver(startupSchema),
        defaultValues: {
            name: "",
            tagline: "",
            fullDescription: "",
            industry: "",
            stage: "SEED",
            fundingGoal: "",
            websiteUrl: "",
            logoUrl: "",
            coverUrl: "",
            facebookUrl: "",
            instagramUrl: "",
            twitterUrl: "",
            pitchDeckUrl: "",
            financialDocumentsUrl: "",
            businessPlanUrl: "",
            businessModelUrl: "",
            ownerId: "",
            ownerRole: "FOUNDER",
        },
    })

    // Mode Toggle
    const [mode, setMode] = useState<'CREATE' | 'ADD_MEMBER'>('CREATE')

    // Member Form State
    const [memberStartup, setMemberStartup] = useState<Startup | null>(null)
    const [memberUser, setMemberUser] = useState<User | null>(null)
    const [memberRole, setMemberRole] = useState<StartupRole>('OTHER')
    const [memberJoinedAt, setMemberJoinedAt] = useState<string>(new Date().toISOString().split('T')[0])
    const [memberIsActive, setMemberIsActive] = useState(true)
    const [memberLeftAt, setMemberLeftAt] = useState<string>('')

    // Member Search State
    const [startupSearchQuery, setStartupSearchQuery] = useState("")
    const [startupSearchResults, setStartupSearchResults] = useState<Startup[]>([])
    const [isSearchingStartups, setIsSearchingStartups] = useState(false)

    const handleSearchStartups = async (query: string) => {
        setStartupSearchQuery(query)
        if (query.length < 2) {
            setStartupSearchResults([])
            return
        }
        try {
            setIsSearchingStartups(true)
            const data = await searchStartups(query)
            setStartupSearchResults(data.content || [])
        } catch (error) {
            console.error(error)
        } finally {
            setIsSearchingStartups(false)
        }
    }

    const selectStartup = (startup: Startup) => {
        setMemberStartup(startup)
        setStartupSearchQuery("")
        setStartupSearchResults([])
    }

    const handleAddMember = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!memberStartup || !memberUser) {
            toast.error("Please select both a startup and a user")
            return
        }
        try {
            setIsLoading(true)
            // If the selected date is today, use the current time
            // Otherwise default to start of day (or end of day for leftAt?)
            // Actually, backend now expects LocalDateTime.
            // Let's assume if user picks today, they mean "now".
            // If they pick a past date, we can default to noon or something, or just use the date string + T00:00:00.
            // But to fix the "static 2:00 AM" issue, specifically for "today", we should send current time.

            const today = new Date().toISOString().split('T')[0]

            let joinedAtISO = memberJoinedAt
            if (memberJoinedAt === today) {
                joinedAtISO = new Date().toISOString()
            } else {
                joinedAtISO = new Date(memberJoinedAt).toISOString()
            }

            let leftAtISO: string | null = null
            if (!memberIsActive && memberLeftAt) {
                if (memberLeftAt === today) {
                    leftAtISO = new Date().toISOString()
                } else {
                    leftAtISO = new Date(memberLeftAt).toISOString()
                }
            }

            await addStartupMember(
                memberStartup.id,
                memberUser.id,
                memberRole,
                joinedAtISO,
                leftAtISO
            )
            toast.success("Member added successfully")
            onSuccess()
            onOpenChange(false)
            // Reset
            setMemberStartup(null)
            setMemberUser(null)
            setMemberRole('OTHER')
            setMemberIsActive(true)
            setMemberLeftAt('')
        } catch (error) {
            toast.error("Failed to add member")
            console.error(error)
        } finally {
            setIsLoading(false)
        }
    }

    // User Search State
    const [searchQuery, setSearchQuery] = useState("")
    const [searchResults, setSearchResults] = useState<User[]>([])
    const [isSearching, setIsSearching] = useState(false)
    const [selectedOwner, setSelectedOwner] = useState<User | null>(null)

    const handleSearchUsers = async (query: string) => {
        setSearchQuery(query)
        if (query.length < 2) {
            setSearchResults([])
            return
        }

        try {
            setIsSearching(true)
            // Filter by STARTUP_OWNER role as requested
            // Enforce ACTIVE status
            const data = await searchUsers(query, 'STARTUP_OWNER', undefined, 'ACTIVE')

            let users = data.content || []

            // If a startup is selected (in ADD_MEMBER mode), filter out existing members
            if (memberStartup && memberStartup.members) {
                const existingMemberIds = memberStartup.members.map(m => m.userId)
                users = users.filter(u => !existingMemberIds.includes(u.id))
            }

            setSearchResults(users)
        } catch (error) {
            console.error(error)
        } finally {
            setIsSearching(false)
        }
    }

    const selectOwner = (user: User) => {
        setSelectedOwner(user)
        form.setValue("ownerId", user.id)
        setSearchResults([])
        setSearchQuery("")
    }

    const clearOwner = () => {
        setSelectedOwner(null)
        form.setValue("ownerId", "")
    }

    const onSubmit = async (data: StartupFormValues) => {
        try {
            setIsLoading(true)
            await createStartup({
                ...data,
                fundingGoal: Number(data.fundingGoal),
                raisedAmount: 0,
                stage: data.stage as StartupStage,
                ownerRole: data.ownerRole as StartupRole,
                status: 'ACTIVE', // Startups are active immediately
            })
            toast.success("Startup created successfully")
            onSuccess()
            onOpenChange(false)
            form.reset()
        } catch (error) {
            toast.error("Failed to create startup")
            console.error(error)
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-3xl max-h-[90vh] overflow-hidden flex flex-col p-0 gap-0 border-none shadow-2xl bg-white dark:bg-slate-950">
                {/* Gradient Header */}
                <div className="bg-gradient-to-r from-slate-800 via-indigo-900/50 to-purple-900/50 dark:from-slate-900 dark:via-indigo-950/80 dark:to-purple-950/80 p-6 shrink-0 border-b border-slate-700/50">
                    <DialogHeader className="space-y-2">
                        <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white">
                            <div className="h-12 w-12 rounded-2xl bg-white/20 backdrop-blur-sm shadow-lg flex items-center justify-center">
                                {mode === 'CREATE' ? (
                                    <Building2 className="h-6 w-6 text-white" />
                                ) : (
                                    <Users className="h-6 w-6 text-white" />
                                )}
                            </div>
                            {mode === 'CREATE' ? 'Create New Startup' : 'Add Team Member'}
                        </DialogTitle>
                        <DialogDescription className="text-white/80">
                            {mode === 'CREATE'
                                ? 'Launch a new startup on the platform with all the details.'
                                : 'Add an existing user to a startup team with a specific role.'}
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <div className="overflow-y-auto flex-1 p-6">
                    {/* Mode Toggle */}
                    <div className="grid grid-cols-2 bg-slate-100 dark:bg-slate-800/50 p-1.5 rounded-xl mb-6">
                        <button
                            onClick={() => setMode('CREATE')}
                            className={`flex items-center justify-center gap-2 py-2.5 text-sm font-semibold rounded-lg transition-all duration-200 ${mode === 'CREATE'
                                ? 'bg-white dark:bg-slate-700 shadow-md text-indigo-600 dark:text-indigo-400'
                                : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-300'
                                }`}
                        >
                            <PlusCircle className="h-4 w-4" />
                            Create Startup
                        </button>
                        <button
                            onClick={() => setMode('ADD_MEMBER')}
                            className={`flex items-center justify-center gap-2 py-2.5 text-sm font-semibold rounded-lg transition-all duration-200 ${mode === 'ADD_MEMBER'
                                ? 'bg-white dark:bg-slate-700 shadow-md text-purple-600 dark:text-purple-400'
                                : 'text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-300'
                                }`}
                        >
                            <Users className="h-4 w-4" />
                            Add Member
                        </button>
                    </div>

                    {mode === 'CREATE' ? (
                        <form onSubmit={form.handleSubmit(onSubmit, (errors) => {
                            console.error('Form validation errors:', errors)
                            const firstError = Object.values(errors)[0]
                            if (firstError?.message) {
                                toast.error(String(firstError.message))
                            } else {
                                toast.error('Please fill all required fields')
                            }
                        })} className="space-y-6">
                            {/* Basic Info Section */}
                            <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-900/10 dark:to-indigo-900/10 border border-blue-100 dark:border-blue-800/30">
                                <h3 className="text-sm font-bold uppercase tracking-wider text-blue-700 dark:text-blue-400 flex items-center gap-2">
                                    <Building2 className="h-4 w-4" />
                                    Basic Information
                                </h3>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="name" className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                                            Startup Name *
                                        </Label>
                                        <Input id="name" {...form.register("name")} placeholder="Acme Inc." className="bg-white dark:bg-slate-900" />
                                        {form.formState.errors.name && (
                                            <p className="text-sm text-red-500">{form.formState.errors.name.message}</p>
                                        )}
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="industry" className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                                            Industry *
                                        </Label>
                                        <Select
                                            onValueChange={(value) => form.setValue("industry", value)}
                                            defaultValue={form.getValues("industry")}
                                        >
                                            <SelectTrigger className="bg-white dark:bg-slate-900">
                                                <SelectValue placeholder="Select industry" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {INDUSTRY_OPTIONS.map((industry) => (
                                                    <SelectItem key={industry} value={industry}>
                                                        {industry}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                        {form.formState.errors.industry && (
                                            <p className="text-sm text-red-500">{form.formState.errors.industry.message}</p>
                                        )}
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="tagline" className="text-slate-700 dark:text-slate-300">
                                        Tagline
                                    </Label>
                                    <Input id="tagline" {...form.register("tagline")} placeholder="Innovating the future..." className="bg-white dark:bg-slate-900" />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="description" className="text-slate-700 dark:text-slate-300">
                                        Full Description
                                    </Label>
                                    <Textarea id="description" {...form.register("fullDescription")} className="min-h-[100px] bg-white dark:bg-slate-900" placeholder="Tell us about your startup..." />
                                </div>
                            </div>

                            {/* Owner & Stage Section */}
                            <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-purple-50 to-pink-50 dark:from-purple-900/10 dark:to-pink-900/10 border border-purple-100 dark:border-purple-800/30">
                                <h3 className="text-sm font-bold uppercase tracking-wider text-purple-700 dark:text-purple-400 flex items-center gap-2">
                                    <UserIcon className="h-4 w-4" />
                                    Owner & Funding
                                </h3>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2 relative">
                                        <Label className="text-slate-700 dark:text-slate-300">
                                            Startup Owner *
                                        </Label>
                                        {selectedOwner ? (
                                            <div className="flex items-center justify-between p-2.5 border rounded-lg bg-white dark:bg-slate-900">
                                                <div className="flex items-center gap-2 overflow-hidden">
                                                    <div className="h-7 w-7 rounded-full bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center">
                                                        <UserIcon className="h-3.5 w-3.5 text-purple-600 dark:text-purple-400" />
                                                    </div>
                                                    <span className="text-sm truncate">{selectedOwner.email}</span>
                                                </div>
                                                <Button variant="ghost" size="sm" onClick={clearOwner} className="h-6 w-6 p-0 hover:bg-red-100 hover:text-red-600">
                                                    &times;
                                                </Button>
                                            </div>
                                        ) : (
                                            <div>
                                                <Input
                                                    placeholder="Search user by email..."
                                                    value={searchQuery}
                                                    onChange={(e) => handleSearchUsers(e.target.value)}
                                                    className="bg-white dark:bg-slate-900"
                                                />
                                                {searchResults.length > 0 && (
                                                    <div className="absolute z-10 w-full mt-1 bg-white dark:bg-slate-900 border rounded-lg shadow-lg max-h-[200px] overflow-auto">
                                                        {searchResults.map(user => (
                                                            <div
                                                                key={user.id}
                                                                className="p-2.5 hover:bg-purple-50 dark:hover:bg-purple-900/20 cursor-pointer text-sm flex items-center gap-2"
                                                                onClick={() => selectOwner(user)}
                                                            >
                                                                <UserIcon className="h-4 w-4 text-purple-500" />
                                                                <span>{user.email}</span>
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                                {isSearching && (
                                                    <div className="absolute right-2 top-9">
                                                        <Loader2 className="h-4 w-4 animate-spin text-purple-500" />
                                                    </div>
                                                )}
                                                {!selectedOwner && form.formState.errors.ownerId && (
                                                    <p className="text-[10px] text-red-500 mt-1">{form.formState.errors.ownerId.message}</p>
                                                )}
                                            </div>
                                        )}
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="role" className="text-slate-700 dark:text-slate-300">
                                            Owner Role *
                                        </Label>
                                        <Select
                                            onValueChange={(value) => form.setValue("ownerRole", value)}
                                            defaultValue={form.getValues("ownerRole")}
                                        >
                                            <SelectTrigger className="bg-white dark:bg-slate-900">
                                                <SelectValue placeholder="Select role" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="FOUNDER">Founder</SelectItem>
                                                <SelectItem value="CO_FOUNDER">Co-Founder</SelectItem>
                                                <SelectItem value="CEO">CEO</SelectItem>
                                                <SelectItem value="CTO">CTO</SelectItem>
                                                <SelectItem value="COO">COO</SelectItem>
                                                <SelectItem value="CFO">CFO</SelectItem>
                                                <SelectItem value="CMO">CMO</SelectItem>
                                                <SelectItem value="CHIEF_PRODUCT_OFFICER">CPO</SelectItem>
                                                <SelectItem value="OTHER">Other</SelectItem>
                                            </SelectContent>
                                        </Select>
                                        {form.formState.errors.ownerRole && (
                                            <p className="text-sm text-red-500">{form.formState.errors.ownerRole.message}</p>
                                        )}
                                    </div>
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="stage" className="text-slate-700 dark:text-slate-300">
                                            Stage *
                                        </Label>
                                        <Select
                                            onValueChange={(value) => form.setValue("stage", value)}
                                            defaultValue={form.getValues("stage")}
                                        >
                                            <SelectTrigger className="bg-white dark:bg-slate-900">
                                                <SelectValue placeholder="Select stage" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="IDEA">Idea</SelectItem>
                                                <SelectItem value="MVP">MVP</SelectItem>
                                                <SelectItem value="SEED">Seed</SelectItem>
                                                <SelectItem value="SERIES_A">Series A</SelectItem>
                                                <SelectItem value="SERIES_B">Series B</SelectItem>
                                                <SelectItem value="GROWTH">Growth</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="fundingGoal" className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                                            <DollarSign className="h-4 w-4 text-emerald-500" />
                                            Funding Goal ($) *
                                        </Label>
                                        <Input id="fundingGoal" type="number" {...form.register("fundingGoal")} placeholder="1,000,000" className="bg-white dark:bg-slate-900" />
                                        {form.formState.errors.fundingGoal && (
                                            <p className="text-sm text-red-500">{form.formState.errors.fundingGoal.message}</p>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Media & Links Section */}
                            <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-emerald-50 to-teal-50 dark:from-emerald-900/10 dark:to-teal-900/10 border border-emerald-100 dark:border-emerald-800/30">
                                <h3 className="text-sm font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-400 flex items-center gap-2">
                                    <Globe className="h-4 w-4" />
                                    Media & Links
                                </h3>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="website" className="text-slate-700 dark:text-slate-300">Website URL</Label>
                                        <Input id="website" {...form.register("websiteUrl")} placeholder="https://example.com" className="bg-white dark:bg-slate-900" />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="logo" className="text-slate-700 dark:text-slate-300">Logo URL</Label>
                                        <div className="flex gap-2">
                                            <Input id="logo" {...form.register("logoUrl")} placeholder="https://..." className="bg-white dark:bg-slate-900" />
                                            <Button type="button" variant="outline" size="icon" title="Upload Logo" className="shrink-0">
                                                <Upload className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="space-y-2 col-span-2">
                                        <Label htmlFor="cover" className="text-slate-700 dark:text-slate-300">Cover Image URL</Label>
                                        <div className="flex gap-2">
                                            <Input id="cover" {...form.register("coverUrl")} placeholder="https://..." className="bg-white dark:bg-slate-900" />
                                            <Button type="button" variant="outline" size="icon" title="Upload Cover" className="shrink-0">
                                                <Upload className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Social Media Section */}
                            <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-rose-50 to-orange-50 dark:from-rose-900/10 dark:to-orange-900/10 border border-rose-100 dark:border-rose-800/30">
                                <h3 className="text-sm font-bold uppercase tracking-wider text-rose-700 dark:text-rose-400 flex items-center gap-2">
                                    <Link className="h-4 w-4" />
                                    Social Media
                                </h3>
                                <div className="grid grid-cols-3 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="facebook" className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                                            <Facebook className="h-4 w-4 text-blue-600" /> Facebook
                                        </Label>
                                        <Input id="facebook" {...form.register("facebookUrl")} placeholder="https://facebook.com/..." className="bg-white dark:bg-slate-900" />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="instagram" className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                                            <Instagram className="h-4 w-4 text-pink-600" /> Instagram
                                        </Label>
                                        <Input id="instagram" {...form.register("instagramUrl")} placeholder="https://instagram.com/..." className="bg-white dark:bg-slate-900" />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="twitter" className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                                            <Twitter className="h-4 w-4 text-sky-500" /> X (Twitter)
                                        </Label>
                                        <Input id="twitter" {...form.register("twitterUrl")} placeholder="https://x.com/..." className="bg-white dark:bg-slate-900" />
                                    </div>
                                </div>
                            </div>

                            {/* Documents Section */}
                            <div className="space-y-4 p-4 rounded-xl bg-gradient-to-br from-amber-50 to-yellow-50 dark:from-amber-900/10 dark:to-yellow-900/10 border border-amber-100 dark:border-amber-800/30">
                                <h3 className="text-sm font-bold uppercase tracking-wider text-amber-700 dark:text-amber-400 flex items-center gap-2">
                                    <FileText className="h-4 w-4" />
                                    Documents
                                </h3>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="pitchDeck" className="text-slate-700 dark:text-slate-300">Pitch Deck URL</Label>
                                        <div className="flex gap-2">
                                            <Input id="pitchDeck" {...form.register("pitchDeckUrl")} placeholder="https://..." className="bg-white dark:bg-slate-900" />
                                            <Button type="button" variant="outline" size="icon" title="Upload Pitch Deck" className="shrink-0">
                                                <Upload className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="financials" className="text-slate-700 dark:text-slate-300">Financial Documents URL</Label>
                                        <div className="flex gap-2">
                                            <Input id="financials" {...form.register("financialDocumentsUrl")} placeholder="https://..." className="bg-white dark:bg-slate-900" />
                                            <Button type="button" variant="outline" size="icon" title="Upload Financials" className="shrink-0">
                                                <Upload className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="businessPlan" className="text-slate-700 dark:text-slate-300">Business Plan URL</Label>
                                        <div className="flex gap-2">
                                            <Input id="businessPlan" {...form.register("businessPlanUrl")} placeholder="https://..." className="bg-white dark:bg-slate-900" />
                                            <Button type="button" variant="outline" size="icon" title="Upload Business Plan" className="shrink-0">
                                                <Upload className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="businessModel" className="text-slate-700 dark:text-slate-300">Business Model URL</Label>
                                        <div className="flex gap-2">
                                            <Input id="businessModel" {...form.register("businessModelUrl")} placeholder="https://..." className="bg-white dark:bg-slate-900" />
                                            <Button type="button" variant="outline" size="icon" title="Upload Business Model" className="shrink-0">
                                                <Upload className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Footer */}
                            <div className="flex items-center justify-end gap-3 pt-4 border-t">
                                <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                                    Cancel
                                </Button>
                                <Button type="submit" disabled={isLoading} className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white shadow-lg shadow-indigo-500/25">
                                    {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                    <Building2 className="mr-2 h-4 w-4" />
                                    Create Startup
                                </Button>
                            </div>
                        </form>
                    ) : (
                        <div className="space-y-6 py-4">
                            {/* Add Member Form */}
                            <div className="grid gap-6">
                                {/* Startup Selection */}
                                <div className="space-y-2 relative">
                                    <Label className="flex items-center gap-2">
                                        <Building2 className="h-4 w-4 text-muted-foreground" />
                                        Select Startup
                                    </Label>
                                    {memberStartup ? (
                                        <div className="flex items-center justify-between p-3 border rounded-lg bg-background">
                                            <div className="flex items-center gap-3">
                                                {memberStartup.logoUrl ? (
                                                    <img
                                                        src={memberStartup.logoUrl}
                                                        alt={memberStartup.name}
                                                        className="h-10 w-10 rounded-md object-cover border"
                                                    />
                                                ) : (
                                                    <div className="h-10 w-10 rounded-md bg-blue-100 flex items-center justify-center text-blue-700 font-bold border border-blue-200">
                                                        {memberStartup.name.substring(0, 2).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <p className="font-medium text-sm">{memberStartup.name}</p>
                                                    <p className="text-xs text-muted-foreground">{memberStartup.industry} • {memberStartup.stage}</p>
                                                </div>
                                            </div>
                                            <Button variant="ghost" size="sm" onClick={() => setMemberStartup(null)}>Change</Button>
                                        </div>
                                    ) : (
                                        <div className="relative">
                                            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                            <Input
                                                placeholder="Search startup..."
                                                className="pl-9 bg-white dark:bg-slate-900"
                                                value={startupSearchQuery}
                                                onChange={(e) => handleSearchStartups(e.target.value)}
                                            />
                                            {isSearchingStartups && (
                                                <div className="absolute right-2 top-2">
                                                    <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                                                </div>
                                            )}
                                            {startupSearchResults.length > 0 && (
                                                <div className="absolute z-10 w-full mt-1 bg-popover border rounded-md shadow-lg max-h-[200px] overflow-auto">
                                                    {startupSearchResults.map(s => (
                                                        <div
                                                            key={s.id}
                                                            className="p-3 hover:bg-accent cursor-pointer flex items-center gap-3 border-b last:border-0"
                                                            onClick={() => selectStartup(s)}
                                                        >
                                                            {/* Startup Logo/Icon */}
                                                            {s.logoUrl ? (
                                                                <img
                                                                    src={s.logoUrl}
                                                                    alt={s.name}
                                                                    className="h-9 w-9 rounded-md object-cover border"
                                                                />
                                                            ) : (
                                                                <div className="h-9 w-9 rounded-md bg-primary/10 flex items-center justify-center shrink-0 border border-primary/20">
                                                                    <Building2 className="h-5 w-5 text-primary" />
                                                                </div>
                                                            )}

                                                            {/* Startup Details */}
                                                            <div className="min-w-0 flex-1">
                                                                <p className="font-medium text-sm truncate">{s.name}</p>
                                                                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                                                                    <span className="truncate">{s.industry}</span>
                                                                    <span className="w-1 h-1 rounded-full bg-muted-foreground/50" />
                                                                    <span className="truncate">{s.stage}</span>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>

                                {/* User Selection */}
                                <div className="space-y-2 relative">
                                    <Label className="flex items-center gap-2">
                                        <UserIcon className="h-4 w-4 text-muted-foreground" />
                                        Select User
                                    </Label>
                                    {memberUser ? (
                                        <div className="flex items-center justify-between p-3 border rounded-lg bg-background">
                                            <div className="flex items-center gap-3">
                                                <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center">
                                                    <UserIcon className="h-4 w-4 text-primary" />
                                                </div>
                                                <div>
                                                    <p className="font-medium text-sm">{memberUser.email}</p>
                                                </div>
                                            </div>
                                            <Button variant="ghost" size="sm" onClick={() => setMemberUser(null)}>Change</Button>
                                        </div>
                                    ) : (
                                        <div className="relative">
                                            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                            <Input
                                                placeholder="Search user email..."
                                                className="pl-9 bg-white dark:bg-slate-900"
                                                value={searchQuery}
                                                onChange={(e) => handleSearchUsers(e.target.value)}
                                            />
                                            {searchResults.length > 0 && (
                                                <div className="absolute z-10 w-full mt-1 bg-popover border rounded-md shadow-lg max-h-[200px] overflow-auto">
                                                    {searchResults.map(u => (
                                                        <div
                                                            key={u.id}
                                                            className="p-3 hover:bg-accent cursor-pointer text-sm"
                                                            onClick={() => {
                                                                setMemberUser(u)
                                                                setSearchQuery("")
                                                                setSearchResults([])
                                                            }}
                                                        >
                                                            {u.email}
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label>Role</Label>
                                        <Select
                                            value={memberRole}
                                            onValueChange={(val) => setMemberRole(val as StartupRole)}
                                        >
                                            <SelectTrigger className="bg-white dark:bg-slate-900">
                                                <SelectValue />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="FOUNDER">Founder</SelectItem>
                                                <SelectItem value="CO_FOUNDER">Co-Founder</SelectItem>
                                                <SelectItem value="CEO">CEO</SelectItem>
                                                <SelectItem value="CTO">CTO</SelectItem>
                                                <SelectItem value="COO">COO</SelectItem>
                                                <SelectItem value="CFO">CFO</SelectItem>
                                                <SelectItem value="CMO">CMO</SelectItem>
                                                <SelectItem value="CHIEF_PRODUCT_OFFICER">CPO</SelectItem>
                                                <SelectItem value="OTHER">Other</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Joined At</Label>
                                        <Input
                                            type="date"
                                            value={memberJoinedAt}
                                            onChange={(e) => setMemberJoinedAt(e.target.value)}
                                            className="block bg-white dark:bg-slate-900 dark:[color-scheme:dark]"
                                        />
                                    </div>
                                </div>

                                <div className="space-y-4 border-t pt-4">
                                    <div className="flex items-center space-x-2">
                                        <input
                                            type="checkbox"
                                            id="isActive"
                                            checked={memberIsActive}
                                            onChange={(e) => {
                                                setMemberIsActive(e.target.checked)
                                                if (e.target.checked) setMemberLeftAt('')
                                            }}
                                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                                        />
                                        <Label htmlFor="isActive">Currently Active Member</Label>
                                    </div>

                                    {!memberIsActive && (
                                        <div className="space-y-2">
                                            <Label>Left At</Label>
                                            <Input
                                                type="date"
                                                value={memberLeftAt}
                                                onChange={(e) => setMemberLeftAt(e.target.value)}
                                                max={new Date().toISOString().split('T')[0]}
                                                className="block bg-white dark:bg-slate-900 dark:[color-scheme:dark]"
                                            />
                                        </div>
                                    )}
                                </div>

                                <DialogFooter>
                                    <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                                        Cancel
                                    </Button>
                                    <Button onClick={handleAddMember} disabled={isLoading || !memberStartup || !memberUser}>
                                        {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                        Add Member
                                    </Button>
                                </DialogFooter>
                            </div>
                        </div>
                    )}
                </div>
            </DialogContent>
        </Dialog>
    )
}

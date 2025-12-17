import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogTitle,
} from "../ui/dialog"
import { Button } from "../ui/button"
import {
    Facebook, Instagram, Twitter, Globe, UserCog,
    FileText, FileSpreadsheet, FilePieChart,
    Building2
} from "lucide-react"
import { formatDate } from "../../lib/utils"
import { Startup } from "../../types"

interface StartupDetailsDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    startup: Startup | null
    onTransfer?: (startup: Startup) => void
}

export function StartupDetailsDialog({
    open,
    onOpenChange,
    startup,
    onTransfer
}: StartupDetailsDialogProps) {
    if (!startup) return null

    const formatCurrency = (amount?: number) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            maximumFractionDigits: 0,
        }).format(amount || 0)
    }

    const getStatusBadge = (status: string) => {
        const styles: Record<string, string> = {
            PENDING: 'bg-yellow-500/10 text-yellow-600 border-yellow-200',
            APPROVED: 'bg-green-500/10 text-green-600 border-green-200',
            REJECTED: 'bg-red-500/10 text-red-600 border-red-200',
        }
        return (
            <span className={`px-2 py-0.5 rounded-full text-xs font-medium border ${styles[status] || 'bg-gray-100 text-gray-800'}`}>
                {status}
            </span>
        )
    }

    const getStageBadge = (stage: string) => {
        return (
            <div className="flex items-center gap-2">
                <div className={`h-2 w-2 rounded-full ${['IDEA', 'MVP'].includes(stage) ? 'bg-blue-500' :
                    ['SEED', 'SERIES_A'].includes(stage) ? 'bg-purple-500' :
                        'bg-orange-500'
                    }`} />
                <span className="font-medium">{stage.replace(/_/g, ' ')}</span>
            </div>
        )
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto p-0">
                <div className="relative h-48 w-full bg-muted">
                    {startup.coverUrl ? (
                        <img
                            src={startup.coverUrl}
                            alt="Cover"
                            className="w-full h-full object-cover"
                        />
                    ) : (
                        <div className="w-full h-full bg-gradient-to-r from-primary/10 to-primary/5" />
                    )}
                    <div className="absolute -bottom-10 left-8">
                        {startup.logoUrl ? (
                            <img
                                src={startup.logoUrl}
                                alt="Logo"
                                className="h-24 w-24 rounded-xl object-cover border-4 border-background shadow-lg bg-white"
                            />
                        ) : (
                            <div className="h-24 w-24 rounded-xl bg-background border-4 border-background shadow-lg flex items-center justify-center">
                                <div className="h-full w-full bg-primary/10 rounded-lg flex items-center justify-center">
                                    <Building2 className="h-10 w-10 text-primary" />
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                <div className="px-8 pt-12 pb-8">
                    <div className="flex justify-between items-start mb-6">
                        <div>
                            <DialogTitle className="text-2xl font-bold flex items-center gap-2">
                                {startup.name}
                                {startup.status && getStatusBadge(startup.status)}
                            </DialogTitle>
                            <DialogDescription className="text-base mt-1">
                                {startup.tagline}
                            </DialogDescription>
                        </div>
                        <div className="flex gap-2">
                            <div className="flex items-center gap-1 mr-2 border-r pr-2">
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 text-blue-600 hover:text-blue-700 hover:bg-blue-100"
                                    onClick={() => window.open(startup.facebookUrl, '_blank')}
                                    disabled={!startup.facebookUrl}
                                    title={startup.facebookUrl ? "Facebook" : "Facebook (Not Provided)"}
                                >
                                    <Facebook className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 text-pink-600 hover:text-pink-700 hover:bg-pink-100"
                                    onClick={() => window.open(startup.instagramUrl, '_blank')}
                                    disabled={!startup.instagramUrl}
                                    title={startup.instagramUrl ? "Instagram" : "Instagram (Not Provided)"}
                                >
                                    <Instagram className="h-4 w-4" />
                                </Button>
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 text-sky-500 hover:text-sky-600 hover:bg-sky-100"
                                    onClick={() => window.open(startup.twitterUrl, '_blank')}
                                    disabled={!startup.twitterUrl}
                                    title={startup.twitterUrl ? "X (Twitter)" : "X (Twitter) (Not Provided)"}
                                >
                                    <Twitter className="h-4 w-4" />
                                </Button>
                            </div>
                            {startup.websiteUrl && (
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => window.open(startup.websiteUrl, '_blank')}
                                >
                                    <Globe className="h-4 w-4 mr-2" />
                                    Website
                                </Button>
                            )}
                            {onTransfer && (
                                <Button
                                    size="sm"
                                    onClick={() => onTransfer(startup)}
                                >
                                    <UserCog className="h-4 w-4 mr-2" />
                                    Transfer
                                </Button>
                            )}
                        </div>
                    </div>

                    <div className="space-y-6">
                        {/* Metrics Grid */}
                        <div className="grid grid-cols-3 gap-4">
                            <div className="p-4 rounded-lg bg-muted/50 border">
                                <p className="text-xs font-medium text-muted-foreground uppercase">Funding Goal</p>
                                <p className="text-xl font-bold mt-1">{formatCurrency(startup.fundingGoal)}</p>
                            </div>
                            <div className="p-4 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
                                <p className="text-xs font-medium text-emerald-600 uppercase">Raised</p>
                                <p className="text-xl font-bold text-emerald-700 mt-1">{formatCurrency(startup.raisedAmount)}</p>
                            </div>
                            <div className="p-4 rounded-lg bg-muted/50 border">
                                <p className="text-xs font-medium text-muted-foreground uppercase">Stage</p>
                                <div className="mt-1">{getStageBadge(startup.stage)}</div>
                            </div>
                        </div>

                        {/* Info Grid */}
                        <div className="grid grid-cols-2 gap-x-8 gap-y-4 text-sm">
                            <div className="flex justify-between py-2 border-b">
                                <span className="text-muted-foreground">Industry</span>
                                <span className="font-medium">{startup.industry}</span>
                            </div>
                            <div className="flex justify-between py-2 border-b">
                                <div>
                                    <p className="text-muted-foreground">Owner</p>
                                    <div className="flex items-center gap-2 mt-1">
                                        <p className="font-medium text-primary">{startup.ownerEmail}</p>
                                        {startup.ownerRole && (
                                            <span className="inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
                                                {startup.ownerRole.replace(/_/g, " ")}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            </div>
                            <div className="flex justify-between py-2 border-b">
                                <span className="text-muted-foreground">Created</span>
                                <span>{formatDate(startup.createdAt)}</span>
                            </div>
                        </div>

                        {/* Description */}
                        <div>
                            <h4 className="text-sm font-semibold mb-2 flex items-center gap-2">
                                <FileText className="h-4 w-4" />
                                About Request
                            </h4>
                            <div className="p-4 rounded-lg bg-muted/30 text-sm leading-relaxed whitespace-pre-wrap">
                                {startup.fullDescription}
                            </div>
                        </div>

                        {/* Documents Grid */}
                        <div className="grid grid-cols-2 gap-4 pt-2">
                            <Button
                                variant="secondary"
                                className="w-full justify-start"
                                onClick={() => window.open(startup.pitchDeckUrl, '_blank')}
                                disabled={!startup.pitchDeckUrl}
                            >
                                <FileText className="h-4 w-4 mr-2 text-blue-500" />
                                View Pitch Deck
                            </Button>
                            <Button
                                variant="secondary"
                                className="w-full justify-start"
                                onClick={() => window.open(startup.financialDocumentsUrl, '_blank')}
                                disabled={!startup.financialDocumentsUrl}
                            >
                                <FileSpreadsheet className="h-4 w-4 mr-2 text-emerald-500" />
                                Financial Documents
                            </Button>
                            <Button
                                variant="secondary"
                                className="w-full justify-start"
                                onClick={() => window.open(startup.businessPlanUrl, '_blank')}
                                disabled={!startup.businessPlanUrl}
                            >
                                <FileText className="h-4 w-4 mr-2 text-amber-500" />
                                Business Plan
                            </Button>
                            <Button
                                variant="secondary"
                                className="w-full justify-start"
                                onClick={() => window.open(startup.businessModelUrl, '_blank')}
                                disabled={!startup.businessModelUrl}
                            >
                                <FilePieChart className="h-4 w-4 mr-2 text-purple-500" />
                                Business Model
                            </Button>
                        </div>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    )
}

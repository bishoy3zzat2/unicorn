import { useState } from 'react';
import { Startup, StartupStatus } from '../../types';
import { Button } from '../ui/button';
import { ExternalLink, Trash2, DollarSign } from 'lucide-react';
import { toast } from 'sonner';
import { deleteStartup } from '../../lib/api';

interface StartupDetailsModalProps {
    startup: Startup | null;
    isOpen: boolean;
    onClose: () => void;
    onActionComplete: () => void;
}

export function StartupDetailsModal({ startup, isOpen, onClose, onActionComplete }: StartupDetailsModalProps) {
    const [isDeleting, setIsDeleting] = useState(false);

    if (!isOpen || !startup) return null;

    const handleDelete = async () => {
        if (!confirm(`Are you sure you want to permanently delete "${startup.name}"? This action cannot be undone and will remove all related data.`)) {
            return;
        }

        setIsDeleting(true);
        try {
            await deleteStartup(startup.id);

            toast.success('Startup deleted permanently');
            onActionComplete();
            onClose();
        } catch (error: any) {
            toast.error(error.message || 'Failed to delete startup');
        } finally {
            setIsDeleting(false);
        }
    };

    const getStatusBadgeClass = (status: StartupStatus) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-950/50 text-green-400 border-green-900';
            default:
                return 'bg-slate-800 text-slate-400 border-slate-700';
        }
    };

    const getStageBadgeClass = (stage: string) => {
        switch (stage) {
            case 'IDEA':
                return 'bg-purple-950/50 text-purple-400 border-purple-900';
            case 'MVP':
                return 'bg-blue-950/50 text-blue-400 border-blue-900';
            case 'GROWTH':
                return 'bg-orange-950/50 text-orange-400 border-orange-900';
            case 'SCALING':
                return 'bg-pink-950/50 text-pink-400 border-pink-900';
            default:
                return 'bg-slate-800 text-slate-400 border-slate-700';
        }
    };

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
        }).format(amount);
    };

    const calculateProgress = (raised: number, goal: number) => {
        if (!goal || goal === 0) return 0;
        return Math.min((raised / goal) * 100, 100);
    };

    return (
        <>
            {/* Backdrop */}
            <div
                className="fixed inset-0 bg-black/50 z-50"
                onClick={onClose}
            />

            {/* Sheet sliding from right */}
            <div className="fixed right-0 top-0 h-full w-full max-w-2xl bg-slate-900 border-l border-slate-800 z-50 shadow-2xl overflow-y-auto">
                {/* Header */}
                <div className="sticky top-0 bg-slate-900 border-b border-slate-800 p-6 z-10">
                    <div className="flex justify-between items-start">
                        <div className="flex-1">
                            <h2 className="text-2xl font-bold mb-2">{startup.name}</h2>
                            {startup.tagline && (
                                <p className="text-slate-400 mb-3">{startup.tagline}</p>
                            )}
                            <div className="flex gap-2">
                                <span className={`px-2 py-1 text-xs rounded-md border ${getStatusBadgeClass(startup.status)}`}>
                                    {startup.status}
                                </span>
                                <span className={`px-2 py-1 text-xs rounded-md border ${getStageBadgeClass(startup.stage)}`}>
                                    {startup.stage}
                                </span>
                            </div>
                        </div>
                        <Button variant="ghost" size="sm" onClick={onClose}>
                            ✕
                        </Button>
                    </div>
                </div>

                {/* Content */}
                <div className="p-6 space-y-6">
                    {/* Description */}
                    {startup.fullDescription && (
                        <div>
                            <h3 className="text-lg font-semibold mb-2">Description</h3>
                            <p className="text-slate-300 leading-relaxed">{startup.fullDescription}</p>
                        </div>
                    )}

                    {/* Details Grid */}
                    <div className="grid grid-cols-2 gap-4">
                        {startup.industry && (
                            <div>
                                <p className="text-sm text-slate-400">Industry</p>
                                <p className="font-medium">{startup.industry}</p>
                            </div>
                        )}
                        <div>
                            <p className="text-sm text-slate-400">Owner</p>
                            <p className="font-medium">{startup.ownerEmail}</p>
                        </div>
                        {startup.websiteUrl && (
                            <div>
                                <p className="text-sm text-slate-400">Website</p>
                                <a
                                    href={startup.websiteUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-purple-400 hover:text-purple-300 flex items-center gap-1"
                                >
                                    Visit Site <ExternalLink className="h-3 w-3" />
                                </a>
                            </div>
                        )}
                        <div>
                            <p className="text-sm text-slate-400">Created</p>
                            <p className="font-medium">{new Date(startup.createdAt).toLocaleDateString()}</p>
                        </div>
                    </div>

                    {/* Funding Information */}
                    {startup.fundingGoal && (
                        <div className="bg-slate-800/50 rounded-lg p-4">
                            <h3 className="text-lg font-semibold mb-3 flex items-center gap-2">
                                <DollarSign className="h-5 w-5 text-green-400" />
                                Funding
                            </h3>
                            <div className="space-y-2">
                                <div className="flex justify-between text-sm">
                                    <span className="text-slate-400">Goal</span>
                                    <span className="font-semibold">{formatCurrency(startup.fundingGoal)}</span>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-slate-400">Raised</span>
                                    <span className="font-semibold">{formatCurrency(startup.raisedAmount)}</span>
                                </div>
                                <div className="w-full bg-slate-700 rounded-full h-2 mt-2">
                                    <div
                                        className="bg-gradient-to-r from-green-600 to-green-500 h-2 rounded-full transition-all"
                                        style={{ width: `${calculateProgress(startup.raisedAmount, startup.fundingGoal)}%` }}
                                    />
                                </div>
                                <div className="text-xs text-slate-400 text-center mt-1">
                                    {calculateProgress(startup.raisedAmount, startup.fundingGoal).toFixed(1)}% funded
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Documents */}
                    {startup.pitchDeckUrl && (
                        <div>
                            <h3 className="text-lg font-semibold mb-2">Documents</h3>
                            <Button
                                variant="outline"
                                onClick={() => window.open(startup.pitchDeckUrl, '_blank')}
                                className="w-full"
                            >
                                <ExternalLink className="h-4 w-4 mr-2" />
                                View Pitch Deck
                            </Button>
                        </div>
                    )}
                </div>

                {/* Actions Footer */}
                <div className="sticky bottom-0 bg-slate-900 border-t border-slate-800 p-6">
                    <Button
                        onClick={handleDelete}
                        disabled={isDeleting}
                        variant="destructive"
                        className="w-full"
                    >
                        <Trash2 className="h-4 w-4 mr-2" />
                        {isDeleting ? 'Deleting...' : 'Delete Startup Permanently'}
                    </Button>
                    <p className="text-xs text-slate-400 text-center mt-2">
                        This will permanently remove the startup and all related data
                    </p>
                </div>
            </div>
        </>
    );
}

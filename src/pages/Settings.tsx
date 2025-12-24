import { useState, useEffect } from 'react';
import { AppConfigItem, fetchConfigsGrouped, batchUpdateConfigs, updatePreferredCurrency, syncExchangeRates } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import {
    Loader2, Save, RefreshCw, Settings2, Globe, DollarSign,
    Palette, CreditCard, Percent, Sparkles, ChevronRight
} from 'lucide-react';
import { Switch } from '@/components/ui/switch';
import { useAuth } from '@/contexts/AuthContext';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { cn } from '@/lib/utils';

const CURRENCIES = ['USD', 'SAR', 'AED', 'EGP', 'QAR', 'KWD', 'BHD', 'OMR', 'JOD', 'LBP', 'MAD'];

// Category icons and colors for visual distinction
const CATEGORY_CONFIG: Record<string, { icon: React.ElementType; color: string; gradient: string; bgColor: string }> = {
    preferences: { icon: Palette, color: 'text-indigo-500', gradient: 'from-indigo-500 to-purple-500', bgColor: 'bg-indigo-50 dark:bg-indigo-950/30' },
    pricing: { icon: DollarSign, color: 'text-emerald-500', gradient: 'from-emerald-500 to-teal-500', bgColor: 'bg-emerald-50 dark:bg-emerald-950/30' },
    fees: { icon: Percent, color: 'text-amber-500', gradient: 'from-amber-500 to-orange-500', bgColor: 'bg-amber-50 dark:bg-amber-950/30' },
    exchange_rates: { icon: Globe, color: 'text-blue-500', gradient: 'from-blue-500 to-cyan-500', bgColor: 'bg-blue-50 dark:bg-blue-950/30' },
    plans: { icon: CreditCard, color: 'text-purple-500', gradient: 'from-purple-500 to-pink-500', bgColor: 'bg-purple-50 dark:bg-purple-950/30' },
};

export function Settings() {
    const { user, updateUser } = useAuth();
    const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN';

    const [groupedConfigs, setGroupedConfigs] = useState<Record<string, AppConfigItem[]>>({});
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [syncing, setSyncing] = useState(false);
    const [activeTab, setActiveTab] = useState<string>('preferences');

    // Edited values state for Admin Configs
    const [edits, setEdits] = useState<Record<string, string>>({});

    // User Preferences State
    const [currency, setCurrency] = useState(user?.preferredCurrency || 'USD');

    useEffect(() => {
        if (isAdmin) {
            loadConfigs();
        } else {
            setLoading(false);
        }
    }, [isAdmin]);

    // Update local currency state if user object updates
    useEffect(() => {
        if (user?.preferredCurrency) {
            setCurrency(user.preferredCurrency);
        }
    }, [user?.preferredCurrency]);

    const loadConfigs = async () => {
        try {
            setLoading(true);
            const data = await fetchConfigsGrouped();
            setGroupedConfigs(data);
        } catch (error) {
            console.error(error);
            toast.error("Error", {
                description: "Failed to load system settings.",
            });
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (key: string, value: string) => {
        setEdits(prev => ({ ...prev, [key]: value }));
    };

    const handleSaveConfigs = async () => {
        try {
            setSaving(true);
            await batchUpdateConfigs(edits);
            toast.success("Success", {
                description: "System settings updated successfully.",
            });
            setEdits({});
            loadConfigs();
        } catch (error) {
            console.error(error);
            toast.error("Error", {
                description: "Failed to save system settings.",
            });
        } finally {
            setSaving(false);
        }
    };

    const handleSavePreferences = async () => {
        try {
            setSaving(true);
            await updatePreferredCurrency(currency);
            updateUser({ preferredCurrency: currency });
            toast.success("Success", {
                description: "Preferences updated successfully.",
            });
        } catch (error) {
            console.error(error);
            toast.error("Error", {
                description: "Failed to update preferences.",
            });
        } finally {
            setSaving(false);
        }
    };

    const handleSyncRates = async () => {
        try {
            setSyncing(true);
            const rates = await syncExchangeRates();
            toast.success("Rates Synced", {
                description: `Updated ${Object.keys(rates).length} exchange rates.`,
            });
            // Reload configs to show new values
            loadConfigs();
        } catch (error) {
            console.error(error);
            toast.error("Sync Failed", {
                description: "Could not fetch live rates.",
            });
        } finally {
            setSyncing(false);
        }
    };

    const categories = Object.keys(groupedConfigs);
    const allTabs = ['preferences', ...categories];

    const getCategoryConfig = (category: string) => {
        return CATEGORY_CONFIG[category] || {
            icon: Settings2,
            color: 'text-slate-500',
            gradient: 'from-slate-500 to-gray-500',
            bgColor: 'bg-slate-50 dark:bg-slate-950/30'
        };
    };

    if (loading) {
        return (
            <div className="flex h-full items-center justify-center min-h-[400px]">
                <div className="flex flex-col items-center gap-4">
                    <div className="relative">
                        <div className="h-16 w-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/20">
                            <Loader2 className="h-8 w-8 animate-spin text-white" />
                        </div>
                    </div>
                    <p className="text-sm text-muted-foreground font-medium">Loading settings...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 pb-20">
            {/* Modern Header */}
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-600 p-8 shadow-xl">
                <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxnIGZpbGw9IiNmZmZmZmYiIGZpbGwtb3BhY2l0eT0iMC4xIj48cGF0aCBkPSJNMzYgMzRoLTJWMjhoLTR2Nmgtc3YtNGgtNHY0aC00di0yaDJ2LTRoLTJ2LTRoNHYyaDR2LTJoNHYtMmgydjRoMnYtMmgydjZoLTJ2MnoiLz48L2c+PC9nPjwvc3ZnPg==')] opacity-30" />
                <div className="relative z-10">
                    <div className="flex items-center gap-4">
                        <div className="h-14 w-14 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                            <Settings2 className="h-7 w-7 text-white" />
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold text-white tracking-tight">Settings</h1>
                            <p className="text-white/80 mt-1">
                                {isAdmin ? 'Manage system configuration and preferences' : 'Customize your experience'}
                            </p>
                        </div>
                    </div>
                </div>
                <div className="absolute -bottom-8 -right-8 h-40 w-40 rounded-full bg-white/10 blur-2xl" />
                <div className="absolute -top-8 -left-8 h-32 w-32 rounded-full bg-white/10 blur-2xl" />
            </div>

            {/* Modern Tab Navigation */}
            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-2 shadow-sm">
                <div className="flex flex-wrap gap-2">
                    {allTabs.map(tab => {
                        const config = getCategoryConfig(tab);
                        const Icon = config.icon;
                        const isActive = activeTab === tab;

                        return (
                            <button
                                key={tab}
                                onClick={() => setActiveTab(tab)}
                                className={cn(
                                    "flex items-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-200",
                                    isActive
                                        ? `bg-gradient-to-r ${config.gradient} text-white shadow-lg shadow-${config.color}/20`
                                        : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-foreground"
                                )}
                            >
                                <Icon className={cn("h-4 w-4", isActive ? "text-white" : config.color)} />
                                <span className="capitalize">{tab.replace(/_/g, ' ')}</span>
                            </button>
                        );
                    })}
                </div>
            </div>

            {/* Tab Content */}
            <div className="space-y-6">
                {activeTab === "preferences" ? (
                    /* Preferences Tab */
                    <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                        {/* Section Header */}
                        <div className="border-b border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/50 px-6 py-4">
                            <div className="flex items-center gap-3">
                                <div className={cn("h-10 w-10 rounded-xl flex items-center justify-center", getCategoryConfig('preferences').bgColor)}>
                                    <Palette className={cn("h-5 w-5", getCategoryConfig('preferences').color)} />
                                </div>
                                <div>
                                    <h2 className="text-lg font-semibold text-foreground">Display Preferences</h2>
                                    <p className="text-sm text-muted-foreground">Customize how you view the application</p>
                                </div>
                            </div>
                        </div>

                        {/* Content */}
                        <div className="p-6 space-y-6">
                            {/* Currency Setting */}
                            <div className="group flex items-start gap-6 p-5 rounded-xl border border-slate-200 dark:border-slate-800 hover:border-indigo-200 dark:hover:border-indigo-800/50 hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-all">
                                <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/20 shrink-0 group-hover:scale-105 transition-transform">
                                    <DollarSign className="h-6 w-6 text-white" />
                                </div>
                                <div className="flex-1 space-y-4">
                                    <div>
                                        <Label htmlFor="currency" className="text-base font-semibold text-foreground">
                                            Preferred Currency
                                        </Label>
                                        <p className="text-sm text-muted-foreground mt-1">
                                            All prices are stored in USD but will be displayed in your selected currency
                                        </p>
                                    </div>
                                    <Select value={currency} onValueChange={setCurrency}>
                                        <SelectTrigger id="currency" className="max-w-xs bg-white dark:bg-slate-950 border-slate-200 dark:border-slate-800">
                                            <SelectValue placeholder="Select currency" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {CURRENCIES.map(c => (
                                                <SelectItem key={c} value={c}>
                                                    <span className="font-medium">{c}</span>
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>
                                <ChevronRight className="h-5 w-5 text-muted-foreground/50 self-center" />
                            </div>

                            {/* Save Button */}
                            <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-slate-800">
                                <Button
                                    onClick={handleSavePreferences}
                                    disabled={saving || currency === user?.preferredCurrency}
                                    className="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white shadow-lg shadow-indigo-500/30 gap-2"
                                >
                                    {saving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                                    Save Preferences
                                </Button>
                            </div>
                        </div>
                    </div>
                ) : (
                    /* Admin Config Tabs */
                    <div className="space-y-6">
                        {/* Sync Button for Exchange Rates */}
                        {activeTab === 'exchange_rates' && (
                            <div className="flex justify-end">
                                <Button
                                    variant="outline"
                                    onClick={handleSyncRates}
                                    disabled={syncing}
                                    className="bg-white dark:bg-slate-900 border-blue-200 dark:border-blue-800 text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-950/30 shadow-sm gap-2"
                                >
                                    <RefreshCw className={cn("h-4 w-4", syncing && 'animate-spin')} />
                                    <Globe className="h-4 w-4" />
                                    Sync Live Rates
                                </Button>
                            </div>
                        )}

                        {/* Config Cards */}
                        <div className="grid gap-4">
                            {groupedConfigs[activeTab]?.map((config) => {
                                const categoryConfig = getCategoryConfig(activeTab);

                                return (
                                    <div
                                        key={config.key}
                                        className="group bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md hover:border-slate-300 dark:hover:border-slate-700 transition-all overflow-hidden"
                                    >
                                        {/* Config Header */}
                                        <div className="px-6 py-4 flex items-start gap-4">
                                            <div className={cn(
                                                "h-11 w-11 rounded-xl flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform",
                                                categoryConfig.bgColor
                                            )}>
                                                <Sparkles className={cn("h-5 w-5", categoryConfig.color)} />
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <h3 className="text-base font-semibold text-foreground">
                                                    {config.key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                                                </h3>
                                                <p className="text-sm text-muted-foreground mt-0.5 line-clamp-2">
                                                    {config.description}
                                                </p>
                                            </div>
                                        </div>

                                        {/* Config Input */}
                                        <div className="px-6 pb-5">
                                            <div className="flex items-center gap-4 p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
                                                <Label htmlFor={config.key} className="text-sm font-medium text-muted-foreground whitespace-nowrap">
                                                    Value <span className="text-xs opacity-70">({config.valueType})</span>
                                                </Label>
                                                <div className="flex-1">
                                                    {config.valueType === 'BOOLEAN' ? (
                                                        <div className="flex items-center gap-3">
                                                            <Switch
                                                                id={config.key}
                                                                checked={edits[config.key] !== undefined ? edits[config.key] === 'true' : config.value === 'true'}
                                                                onCheckedChange={(checked) => handleInputChange(config.key, String(checked))}
                                                                className="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-emerald-500 data-[state=checked]:to-teal-500"
                                                            />
                                                            <span className="text-sm font-medium text-foreground">
                                                                {(edits[config.key] !== undefined ? edits[config.key] === 'true' : config.value === 'true') ? 'Enabled' : 'Disabled'}
                                                            </span>
                                                        </div>
                                                    ) : (
                                                        <Input
                                                            id={config.key}
                                                            type={config.valueType === 'NUMBER' ? 'number' : 'text'}
                                                            value={edits[config.key] !== undefined ? edits[config.key] : config.value}
                                                            onChange={(e) => handleInputChange(config.key, e.target.value)}
                                                            className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
                                                        />
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}
            </div>

            {/* Floating Save Button for Admin Configs */}
            {activeTab !== "preferences" && Object.keys(edits).length > 0 && (
                <div className="fixed bottom-6 right-6 z-50">
                    <Button
                        onClick={handleSaveConfigs}
                        disabled={saving}
                        size="lg"
                        className="bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 hover:from-indigo-600 hover:via-purple-600 hover:to-pink-600 text-white shadow-2xl shadow-indigo-500/40 gap-2 px-6 ring-4 ring-white dark:ring-slate-900"
                    >
                        {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                        Save {Object.keys(edits).length} Change{Object.keys(edits).length > 1 ? 's' : ''}
                    </Button>
                </div>
            )}
        </div>
    );
}

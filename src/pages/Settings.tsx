import { useState, useEffect } from 'react';
import { AppConfigItem, fetchConfigsGrouped, batchUpdateConfigs, updatePreferredCurrency, syncExchangeRates } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import {
    Loader2, Save, RefreshCw, Settings2, Globe, DollarSign,
    Palette, Percent, Sparkles, Scale, ChevronRight, Menu, X,
    Rss, Bell, CreditCard, Layers, Shield, Crown, Target,
    MessageSquare, Gauge
} from 'lucide-react';
import { Switch } from '@/components/ui/switch';
import { useAuth } from '@/contexts/AuthContext';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { cn } from '@/lib/utils';

const CURRENCIES = ['USD', 'SAR', 'AED', 'EGP', 'QAR', 'KWD', 'BHD', 'OMR', 'JOD', 'LBP', 'MAD'];

// Category configuration with icons, colors, and descriptions
const CATEGORY_CONFIG: Record<string, {
    icon: React.ElementType;
    color: string;
    gradient: string;
    bgColor: string;
    lightBg: string;
    description: string;
}> = {
    preferences: {
        icon: Palette,
        color: 'text-indigo-500',
        gradient: 'from-indigo-500 to-purple-500',
        bgColor: 'bg-indigo-50 dark:bg-indigo-950/30',
        lightBg: 'bg-indigo-500/10',
        description: 'Display and personalization options'
    },
    fees: {
        icon: Percent,
        color: 'text-amber-500',
        gradient: 'from-amber-500 to-orange-500',
        bgColor: 'bg-amber-50 dark:bg-amber-950/30',
        lightBg: 'bg-amber-500/10',
        description: 'Platform fees and commissions'
    },
    exchange_rates: {
        icon: Globe,
        color: 'text-blue-500',
        gradient: 'from-blue-500 to-cyan-500',
        bgColor: 'bg-blue-50 dark:bg-blue-950/30',
        lightBg: 'bg-blue-500/10',
        description: 'Currency conversion rates'
    },
    verification: {
        icon: Sparkles,
        color: 'text-emerald-500',
        gradient: 'from-emerald-500 to-teal-500',
        bgColor: 'bg-emerald-50 dark:bg-emerald-950/30',
        lightBg: 'bg-emerald-500/10',
        description: 'Verification requirements and settings'
    },
    system: {
        icon: Gauge,
        color: 'text-violet-500',
        gradient: 'from-violet-500 to-purple-600',
        bgColor: 'bg-violet-50 dark:bg-violet-950/30',
        lightBg: 'bg-violet-500/10',
        description: 'Core system configuration'
    },
    limits_general: {
        icon: Scale,
        color: 'text-rose-500',
        gradient: 'from-rose-500 to-red-500',
        bgColor: 'bg-rose-50 dark:bg-rose-950/30',
        lightBg: 'bg-rose-500/10',
        description: 'Content and usage limits'
    },
    limits_plans: {
        icon: Crown,
        color: 'text-yellow-500',
        gradient: 'from-yellow-500 to-amber-500',
        bgColor: 'bg-yellow-50 dark:bg-yellow-950/30',
        lightBg: 'bg-yellow-500/10',
        description: 'Plan limits and quotas'
    },
    feed: {
        icon: Rss,
        color: 'text-orange-500',
        gradient: 'from-orange-500 to-red-500',
        bgColor: 'bg-orange-50 dark:bg-orange-950/30',
        lightBg: 'bg-orange-500/10',
        description: 'Feed algorithm settings'
    },
    nudge: {
        icon: Bell,
        color: 'text-pink-500',
        gradient: 'from-pink-500 to-rose-500',
        bgColor: 'bg-pink-50 dark:bg-pink-950/30',
        lightBg: 'bg-pink-500/10',
        description: 'Notification nudges'
    },
    limits: {
        icon: Layers,
        color: 'text-cyan-500',
        gradient: 'from-cyan-500 to-blue-500',
        bgColor: 'bg-cyan-50 dark:bg-cyan-950/30',
        lightBg: 'bg-cyan-500/10',
        description: 'System limits'
    },
    subscription: {
        icon: CreditCard,
        color: 'text-green-500',
        gradient: 'from-green-500 to-emerald-500',
        bgColor: 'bg-green-50 dark:bg-green-950/30',
        lightBg: 'bg-green-500/10',
        description: 'Subscription settings'
    },
    notifications: {
        icon: MessageSquare,
        color: 'text-sky-500',
        gradient: 'from-sky-500 to-blue-500',
        bgColor: 'bg-sky-50 dark:bg-sky-950/30',
        lightBg: 'bg-sky-500/10',
        description: 'Notification preferences'
    },
    matching: {
        icon: Target,
        color: 'text-fuchsia-500',
        gradient: 'from-fuchsia-500 to-purple-500',
        bgColor: 'bg-fuchsia-50 dark:bg-fuchsia-950/30',
        lightBg: 'bg-fuchsia-500/10',
        description: 'Matching algorithm settings'
    },
    security: {
        icon: Shield,
        color: 'text-red-500',
        gradient: 'from-red-500 to-rose-600',
        bgColor: 'bg-red-50 dark:bg-red-950/30',
        lightBg: 'bg-red-500/10',
        description: 'Security configurations'
    },
};

export function Settings() {
    const { user, updateUser } = useAuth();
    const isAdmin = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN';

    const [groupedConfigs, setGroupedConfigs] = useState<Record<string, AppConfigItem[]>>({});
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [syncing, setSyncing] = useState(false);
    const [activeTab, setActiveTab] = useState<string>('preferences');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

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

    // Filter out pricing and android_subscriptions - managed via Google Play Console
    const categories = Object.keys(groupedConfigs).filter(cat =>
        cat !== 'pricing' && cat !== 'android_subscriptions'
    );
    const allTabs = ['preferences', ...categories];

    const getCategoryConfig = (category: string) => {
        return CATEGORY_CONFIG[category] || {
            icon: Settings2,
            color: 'text-slate-500',
            gradient: 'from-slate-500 to-gray-500',
            bgColor: 'bg-slate-50 dark:bg-slate-950/30',
            lightBg: 'bg-slate-500/10',
            description: 'Configuration settings'
        };
    };

    const handleTabChange = (tab: string) => {
        setActiveTab(tab);
        setMobileMenuOpen(false);
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

    // Sidebar Navigation Item Component
    const SidebarItem = ({ tab }: { tab: string }) => {
        const config = getCategoryConfig(tab);
        const Icon = config.icon;
        const isActive = activeTab === tab;
        const configCount = tab === 'preferences' ? 1 : (groupedConfigs[tab]?.length || 0);

        return (
            <button
                onClick={() => handleTabChange(tab)}
                className={cn(
                    "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-left transition-all duration-200 group",
                    isActive
                        ? `bg-gradient-to-r ${config.gradient} text-white shadow-lg`
                        : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800/50"
                )}
            >
                <div className={cn(
                    "h-9 w-9 rounded-lg flex items-center justify-center shrink-0 transition-all",
                    isActive
                        ? "bg-white/20"
                        : config.bgColor
                )}>
                    <Icon className={cn("h-5 w-5", isActive ? "text-white" : config.color)} />
                </div>
                <div className="flex-1 min-w-0">
                    <p className={cn(
                        "font-medium capitalize truncate text-sm",
                        isActive ? "text-white" : "text-foreground"
                    )}>
                        {tab.replace(/_/g, ' ')}
                    </p>
                    <p className={cn(
                        "text-xs truncate",
                        isActive ? "text-white/70" : "text-muted-foreground"
                    )}>
                        {configCount} {configCount === 1 ? 'setting' : 'settings'}
                    </p>
                </div>
                <ChevronRight className={cn(
                    "h-4 w-4 shrink-0 transition-transform",
                    isActive ? "text-white/70" : "text-muted-foreground/50 group-hover:translate-x-0.5"
                )} />
            </button>
        );
    };

    return (
        <div className="space-y-6 pb-20">
            {/* Mobile Header - Only visible on mobile */}
            <div className="lg:hidden relative overflow-hidden rounded-2xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 p-5 shadow-lg">
                {/* Background Pattern */}
                <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxnIGZpbGw9IiNmZmZmZmYiIGZpbGwtb3BhY2l0eT0iMC4xIj48cGF0aCBkPSJNMzYgMzRoLTJWMjhoLTR2Nmgtc3YtNGgtNHY0aC00di0yaDJ2LTRoLTJ2LTRoNHYyaDR2LTJoNHYtMmgydjRoMnYtMmgydjZoLTJ2MnoiLz48L2c+PC9nPjwvc3ZnPg==')] opacity-20" />

                {/* Decorative blurs */}
                <div className="absolute -bottom-4 -right-4 h-20 w-20 rounded-full bg-white/10 blur-xl" />
                <div className="absolute -top-4 -left-4 h-16 w-16 rounded-full bg-white/10 blur-xl" />

                <div className="relative z-10 flex items-center justify-between">
                    {/* Left - Icon and Title */}
                    <div className="flex items-center gap-3">
                        <div className="h-11 w-11 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center shadow-lg">
                            <Settings2 className="h-5 w-5 text-white" />
                        </div>
                        <div>
                            <h1 className="text-lg font-bold text-white">Settings</h1>
                            <p className="text-xs text-white/70 capitalize">{activeTab.replace(/_/g, ' ')}</p>
                        </div>
                    </div>

                    {/* Right - Menu Toggle Button */}
                    <button
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        className={cn(
                            "h-11 w-11 rounded-xl flex items-center justify-center transition-all duration-200",
                            mobileMenuOpen
                                ? "bg-white/30 backdrop-blur-sm shadow-lg"
                                : "bg-white/20 backdrop-blur-sm hover:bg-white/30"
                        )}
                    >
                        {mobileMenuOpen ? (
                            <X className="h-5 w-5 text-white" />
                        ) : (
                            <Menu className="h-5 w-5 text-white" />
                        )}
                    </button>
                </div>
            </div>

            {/* Mobile Navigation Dropdown */}
            {mobileMenuOpen && (
                <div className="lg:hidden bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-lg p-3 space-y-1 animate-in slide-in-from-top-2 duration-200">
                    {allTabs.map(tab => (
                        <SidebarItem key={tab} tab={tab} />
                    ))}
                </div>
            )}

            {/* Main Content with Sidebar */}
            <div className="flex flex-col lg:flex-row gap-6">
                {/* Sidebar Navigation - Desktop */}
                <div className="hidden lg:block w-72 shrink-0">
                    <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm p-3 sticky top-6 space-y-1">
                        <div className="px-4 py-2 mb-2">
                            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                                Categories
                            </p>
                        </div>
                        {allTabs.map(tab => (
                            <SidebarItem key={tab} tab={tab} />
                        ))}
                    </div>
                </div>

                {/* Content Area */}
                <div className="flex-1 min-w-0">
                    {activeTab === "preferences" ? (
                        /* Preferences Tab */
                        <div className="space-y-6">
                            {/* Section Card */}
                            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                                {/* Section Header */}
                                <div className="border-b border-slate-200 dark:border-slate-800 bg-gradient-to-r from-slate-50 to-white dark:from-slate-800/50 dark:to-slate-900 px-6 py-5">
                                    <div className="flex items-center gap-4">
                                        <div className={cn("h-12 w-12 rounded-xl flex items-center justify-center shadow-sm", getCategoryConfig('preferences').bgColor)}>
                                            <Palette className={cn("h-6 w-6", getCategoryConfig('preferences').color)} />
                                        </div>
                                        <div>
                                            <h2 className="text-xl font-bold text-foreground">Display Preferences</h2>
                                            <p className="text-sm text-muted-foreground mt-0.5">Customize how you view the application</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Content */}
                                <div className="p-6 space-y-6">
                                    {/* Currency Setting */}
                                    <div className="group flex flex-col sm:flex-row sm:items-center gap-4 p-5 rounded-xl border border-slate-200 dark:border-slate-800 hover:border-indigo-200 dark:hover:border-indigo-800/50 hover:bg-gradient-to-r hover:from-indigo-50/50 hover:to-transparent dark:hover:from-indigo-950/20 transition-all">
                                        <div className="h-14 w-14 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/20 shrink-0 group-hover:scale-105 transition-transform">
                                            <DollarSign className="h-7 w-7 text-white" />
                                        </div>
                                        <div className="flex-1 space-y-3">
                                            <div>
                                                <Label htmlFor="currency" className="text-base font-semibold text-foreground">
                                                    Preferred Currency
                                                </Label>
                                                <p className="text-sm text-muted-foreground mt-0.5">
                                                    All prices are stored in USD but displayed in your selected currency
                                                </p>
                                            </div>
                                            <Select value={currency} onValueChange={setCurrency}>
                                                <SelectTrigger id="currency" className="max-w-[200px] bg-white dark:bg-slate-950 border-slate-200 dark:border-slate-800">
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
                        </div>
                    ) : (
                        /* Admin Config Tabs */
                        <div className="space-y-6">
                            {/* Category Header Card */}
                            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
                                <div className="bg-gradient-to-r from-slate-50 to-white dark:from-slate-800/50 dark:to-slate-900 px-6 py-5">
                                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                                        <div className="flex items-center gap-4">
                                            <div className={cn("h-12 w-12 rounded-xl flex items-center justify-center shadow-sm", getCategoryConfig(activeTab).bgColor)}>
                                                {(() => {
                                                    const Icon = getCategoryConfig(activeTab).icon;
                                                    return <Icon className={cn("h-6 w-6", getCategoryConfig(activeTab).color)} />;
                                                })()}
                                            </div>
                                            <div>
                                                <h2 className="text-xl font-bold text-foreground capitalize">
                                                    {activeTab.replace(/_/g, ' ')}
                                                </h2>
                                                <p className="text-sm text-muted-foreground mt-0.5">
                                                    {getCategoryConfig(activeTab).description}
                                                </p>
                                            </div>
                                        </div>

                                        {/* Sync Button for Exchange Rates */}
                                        {activeTab === 'exchange_rates' && (
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
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Config Cards Grid */}
                            <div className="grid gap-4 md:grid-cols-2">
                                {groupedConfigs[activeTab]?.map((config) => {
                                    const categoryConfig = getCategoryConfig(activeTab);
                                    const Icon = categoryConfig.icon;

                                    return (
                                        <div
                                            key={config.key}
                                            className="group bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md hover:border-slate-300 dark:hover:border-slate-700 transition-all overflow-hidden"
                                        >
                                            {/* Config Card Content */}
                                            <div className="p-5 space-y-4">
                                                {/* Header */}
                                                <div className="flex items-start gap-3">
                                                    <div className={cn(
                                                        "h-10 w-10 rounded-xl flex items-center justify-center shrink-0 transition-transform group-hover:scale-105",
                                                        categoryConfig.bgColor
                                                    )}>
                                                        <Icon className={cn("h-5 w-5", categoryConfig.color)} />
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <h3 className="text-sm font-semibold text-foreground line-clamp-1">
                                                            {config.key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                                                        </h3>
                                                        <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">
                                                            {config.description}
                                                        </p>
                                                    </div>
                                                </div>

                                                {/* Input Area */}
                                                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
                                                    <div className="flex items-center justify-between gap-3">
                                                        <Label htmlFor={config.key} className="text-xs font-medium text-muted-foreground shrink-0">
                                                            Value <span className="opacity-70">({config.valueType})</span>
                                                        </Label>
                                                        {config.valueType === 'BOOLEAN' ? (
                                                            <div className="flex items-center gap-2">
                                                                <Switch
                                                                    id={config.key}
                                                                    checked={edits[config.key] !== undefined ? edits[config.key] === 'true' : config.value === 'true'}
                                                                    onCheckedChange={(checked) => handleInputChange(config.key, String(checked))}
                                                                    className="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-emerald-500 data-[state=checked]:to-teal-500"
                                                                />
                                                                <span className="text-xs font-medium text-foreground min-w-[60px]">
                                                                    {(edits[config.key] !== undefined ? edits[config.key] === 'true' : config.value === 'true') ? 'Enabled' : 'Disabled'}
                                                                </span>
                                                            </div>
                                                        ) : (
                                                            <Input
                                                                id={config.key}
                                                                type={config.valueType === 'NUMBER' ? 'number' : 'text'}
                                                                value={edits[config.key] !== undefined ? edits[config.key] : config.value}
                                                                onChange={(e) => handleInputChange(config.key, e.target.value)}
                                                                className="flex-1 max-w-[200px] h-8 text-sm bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
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
            </div>

            {/* Floating Save Button for Admin Configs */}
            {activeTab !== "preferences" && Object.keys(edits).length > 0 && (
                <div className="fixed bottom-6 right-6 z-50 animate-in slide-in-from-bottom-4 duration-300">
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

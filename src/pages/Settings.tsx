import { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import {
    Settings as SettingsIcon,
    Loader2,
    AlertCircle,
    Save,
    RefreshCcw,
    DollarSign,
    Gauge,
    Cog,
    CheckCircle2
} from 'lucide-react'
import { Alert, AlertDescription } from '../components/ui/alert'
import {
    fetchConfigsGrouped,
    updateConfig,
    fetchConfigVersion,
    AppConfigItem
} from '../lib/api'
import { toast } from 'sonner'

// Category icons and labels
const CATEGORY_CONFIG: Record<string, { icon: React.ElementType; label: string; description: string }> = {
    pricing: {
        icon: DollarSign,
        label: 'Pricing',
        description: 'Subscription plan prices and payment settings'
    },
    limits: {
        icon: Gauge,
        label: 'Limits',
        description: 'Content length limits and usage restrictions'
    },
    system: {
        icon: Cog,
        label: 'System',
        description: 'System-level configuration and versioning'
    },
    general: {
        icon: SettingsIcon,
        label: 'General',
        description: 'General application settings'
    },
}

export function Settings() {
    const [configsByCategory, setConfigsByCategory] = useState<Record<string, AppConfigItem[]>>({})
    const [editedValues, setEditedValues] = useState<Record<string, string>>({})
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState<string | null>(null)
    const [error, setError] = useState<string | null>(null)
    const [version, setVersion] = useState<number>(0)

    useEffect(() => {
        loadConfigs()
    }, [])

    async function loadConfigs() {
        try {
            setLoading(true)
            setError(null)

            const [configs, versionData] = await Promise.all([
                fetchConfigsGrouped(),
                fetchConfigVersion()
            ])

            setConfigsByCategory(configs)
            setVersion(versionData.version)

            // Initialize edited values with current values
            const initialValues: Record<string, string> = {}
            Object.values(configs).flat().forEach(config => {
                initialValues[config.key] = config.value
            })
            setEditedValues(initialValues)
        } catch (err) {
            console.error('Failed to fetch configs:', err)
            setError(err instanceof Error ? err.message : 'Failed to load configuration')
        } finally {
            setLoading(false)
        }
    }

    async function handleSave(key: string) {
        try {
            setSaving(key)
            await updateConfig(key, editedValues[key])

            // Refresh version
            const versionData = await fetchConfigVersion()
            setVersion(versionData.version)

            toast.success('Configuration updated', {
                description: `${key} has been updated successfully.`
            })
        } catch (err) {
            console.error('Failed to update config:', err)
            toast.error('Update failed', {
                description: err instanceof Error ? err.message : 'Failed to update configuration'
            })
        } finally {
            setSaving(null)
        }
    }

    function handleValueChange(key: string, value: string) {
        setEditedValues(prev => ({
            ...prev,
            [key]: value
        }))
    }

    function hasChanges(key: string, originalValue: string): boolean {
        return editedValues[key] !== originalValue
    }

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <span className="ml-2 text-muted-foreground">Loading settings...</span>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
                    <p className="text-muted-foreground mt-2">
                        Configure dynamic application settings and pricing
                    </p>
                </div>
                <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-primary/10 text-primary">
                        <CheckCircle2 className="h-4 w-4" />
                        <span className="text-sm font-medium">Version {version}</span>
                    </div>
                    <Button variant="outline" size="sm" onClick={loadConfigs}>
                        <RefreshCcw className="h-4 w-4 mr-2" />
                        Refresh
                    </Button>
                </div>
            </div>

            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            {/* Config Categories */}
            <div className="space-y-6">
                {Object.entries(configsByCategory).length === 0 ? (
                    <Card>
                        <CardContent className="flex flex-col items-center justify-center py-16">
                            <SettingsIcon className="h-16 w-16 text-muted-foreground mb-4" />
                            <h2 className="text-xl font-semibold mb-2">No Configuration Found</h2>
                            <p className="text-muted-foreground text-center max-w-md">
                                No configuration entries found. Default configurations will be created on first backend startup.
                            </p>
                        </CardContent>
                    </Card>
                ) : (
                    Object.entries(configsByCategory).map(([category, configs]) => {
                        const categoryConfig = CATEGORY_CONFIG[category] || CATEGORY_CONFIG.general
                        const CategoryIcon = categoryConfig.icon

                        return (
                            <Card key={category}>
                                <CardHeader>
                                    <div className="flex items-center gap-3">
                                        <div className="p-2 rounded-lg bg-primary/10">
                                            <CategoryIcon className="h-5 w-5 text-primary" />
                                        </div>
                                        <div>
                                            <CardTitle>{categoryConfig.label}</CardTitle>
                                            <CardDescription>{categoryConfig.description}</CardDescription>
                                        </div>
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    <div className="space-y-4">
                                        {configs.map((config) => (
                                            <div
                                                key={config.key}
                                                className="flex items-start justify-between gap-4 p-4 rounded-lg border bg-card hover:bg-muted/50 transition-colors"
                                            >
                                                <div className="flex-1 min-w-0">
                                                    <div className="flex items-center gap-2">
                                                        <span className="font-medium text-sm">
                                                            {formatConfigKey(config.key)}
                                                        </span>
                                                        <span className="text-xs px-2 py-0.5 rounded bg-muted text-muted-foreground">
                                                            {config.valueType || 'STRING'}
                                                        </span>
                                                    </div>
                                                    {config.description && (
                                                        <p className="text-sm text-muted-foreground mt-1">
                                                            {config.description}
                                                        </p>
                                                    )}
                                                </div>
                                                <div className="flex items-center gap-2">
                                                    <Input
                                                        type={config.valueType === 'NUMBER' ? 'number' : 'text'}
                                                        value={editedValues[config.key] || ''}
                                                        onChange={(e) => handleValueChange(config.key, e.target.value)}
                                                        className="w-40"
                                                    />
                                                    <Button
                                                        size="sm"
                                                        onClick={() => handleSave(config.key)}
                                                        disabled={!hasChanges(config.key, config.value) || saving === config.key}
                                                    >
                                                        {saving === config.key ? (
                                                            <Loader2 className="h-4 w-4 animate-spin" />
                                                        ) : (
                                                            <Save className="h-4 w-4" />
                                                        )}
                                                    </Button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        )
                    })
                )}
            </div>

            {/* Info Card */}
            <Card className="border-dashed">
                <CardContent className="py-6">
                    <div className="flex items-start gap-4">
                        <div className="p-2 rounded-lg bg-blue-100 dark:bg-blue-900/30">
                            <AlertCircle className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                        </div>
                        <div>
                            <h3 className="font-medium">About Configuration</h3>
                            <p className="text-sm text-muted-foreground mt-1">
                                Changes to configuration are immediately reflected in the backend.
                                The mobile app will sync these settings on next launch when it detects
                                a version change. The current version number is automatically incremented
                                whenever any configuration is updated.
                            </p>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}

// Format config key for display
function formatConfigKey(key: string): string {
    return key
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ')
}

import { KPICard } from '../components/dashboard/KPICard'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card'
import { Shield, Key, AlertTriangle, CheckCircle, Lock, Monitor, Clock, RefreshCw } from 'lucide-react'
import { securityStats } from '../lib/mockData'
import { formatNumber } from '../lib/utils'
// import { useAuth } from '../contexts/AuthContext'
import { jwtDecode } from 'jwt-decode'
import { useState, useEffect } from 'react'

export function Security() {
    // const { user } = useAuth()
    const [tokenDetails, setTokenDetails] = useState<any>(null)
    const [refreshTokenDetails, setRefreshTokenDetails] = useState<any>(null)
    const [timeRemaining, setTimeRemaining] = useState<string>('')

    useEffect(() => {
        const token = localStorage.getItem('token')
        const refreshToken = localStorage.getItem('refreshToken')

        if (token) {
            try {
                const decoded: any = jwtDecode(token)
                setTokenDetails({
                    ...decoded,
                    raw: token
                })
            } catch (e) {
                console.error('Failed to decode token', e)
            }
        }

        if (refreshToken) {
            // Refresh tokens are opaque strings in our backend (UUIDs), not JWTs
            // So we can't decode them, but we can show their presence
            setRefreshTokenDetails({
                token: refreshToken,
                exists: true
            })
        }

        // Countdown timer
        const timer = setInterval(() => {
            if (token) {
                try {
                    const decoded: any = jwtDecode(token)
                    const exp = decoded.exp * 1000
                    const now = Date.now()
                    const diff = exp - now

                    if (diff > 0) {
                        const minutes = Math.floor((diff / 1000 / 60) % 60)
                        const seconds = Math.floor((diff / 1000) % 60)
                        const hours = Math.floor((diff / (1000 * 60 * 60)) % 24)

                        setTimeRemaining(`${hours}h ${minutes}m ${seconds}s`)
                    } else {
                        setTimeRemaining('Expired')
                    }
                } catch (e) {
                    setTimeRemaining('Invalid Token')
                }
            }
        }, 1000)

        return () => clearInterval(timer)
    }, [])

    return (
        <div className="space-y-8">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Security Center</h1>
                <p className="text-muted-foreground mt-2">
                    Monitor authentication tokens and system security status.
                </p>
            </div>

            {/* General Token Statistics Section */}
            <section className="space-y-4">
                <div className="flex items-center gap-2 mb-4">
                    <Shield className="h-5 w-5 text-primary" />
                    <h2 className="text-xl font-semibold">Token Statistics</h2>
                </div>

                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
                    <KPICard
                        title="Total Issued Tokens"
                        value={formatNumber(securityStats.totalTokens)}
                        icon={Key}
                        iconColor="text-blue-500"
                    />
                    <KPICard
                        title="Active Sessions"
                        value={formatNumber(securityStats.activeTokens)}
                        icon={CheckCircle}
                        trend={securityStats.activeGrowth}
                        iconColor="text-green-500"
                    />
                    <KPICard
                        title="Expired Tokens"
                        value={formatNumber(securityStats.expiredTokens)}
                        icon={Clock}
                        trend={securityStats.expiredGrowth}
                        iconColor="text-gray-500"
                    />
                    <KPICard
                        title="Revoked Tokens"
                        value={formatNumber(securityStats.revokedTokens)}
                        icon={AlertTriangle}
                        trend={securityStats.revokedGrowth}
                        iconColor="text-red-500"
                    />
                </div>
            </section>

            {/* Current Session Info Section */}
            <section className="space-y-4">
                <div className="flex items-center gap-2 mb-4">
                    <Lock className="h-5 w-5 text-primary" />
                    <h2 className="text-xl font-semibold">My Current Session</h2>
                </div>

                <div className="grid gap-6 md:grid-cols-2">
                    <Card className="md:col-span-1">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Monitor className="h-5 w-5 text-purple-500" />
                                Access Token (JWT)
                            </CardTitle>
                            <CardDescription>
                                Your key to accessing protected resources.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {tokenDetails ? (
                                <>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div>
                                            <p className="text-sm font-medium text-muted-foreground">Issued At</p>
                                            <p className="text-sm">{tokenDetails.iat ? new Date(tokenDetails.iat * 1000).toLocaleTimeString() : 'N/A'}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-muted-foreground">Expires At</p>
                                            <p className="text-sm">{tokenDetails.exp ? new Date(tokenDetails.exp * 1000).toLocaleTimeString() : 'N/A'}</p>
                                        </div>
                                    </div>

                                    <div className="bg-primary/5 p-4 rounded-lg text-center">
                                        <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Time Remaining</p>
                                        <p className="text-2xl font-mono font-bold text-primary">{timeRemaining}</p>
                                    </div>

                                    <div>
                                        <p className="text-sm font-medium text-muted-foreground mb-1">Subject (Username)</p>
                                        <p className="font-mono text-sm bg-accent/50 p-2 rounded truncate">{tokenDetails.sub}</p>
                                    </div>

                                    <div>
                                        <p className="text-sm font-medium text-muted-foreground mb-1">Raw Token (Truncated)</p>
                                        <p className="font-mono text-xs bg-accent/50 p-2 rounded break-all text-muted-foreground">
                                            {tokenDetails.raw?.substring(0, 20)}...{tokenDetails.raw?.substring(tokenDetails.raw.length - 20)}
                                        </p>
                                    </div>
                                </>
                            ) : (
                                <p className="text-sm text-muted-foreground">No active access token found.</p>
                            )}
                        </CardContent>
                    </Card>

                    <Card className="md:col-span-1">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <RefreshCw className="h-5 w-5 text-blue-500" />
                                Refresh Token
                            </CardTitle>
                            <CardDescription>
                                Used to obtain new access tokens without logging in.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {refreshTokenDetails ? (
                                <>
                                    <div className="flex items-center gap-2 bg-green-500/10 text-green-600 px-3 py-2 rounded-lg w-fit">
                                        <CheckCircle className="h-4 w-4" />
                                        <span className="text-sm font-medium">Active & Present</span>
                                    </div>

                                    <div>
                                        <p className="text-sm font-medium text-muted-foreground mb-1">Token Identifier</p>
                                        <p className="font-mono text-xs bg-accent/50 p-2 rounded break-all text-muted-foreground">
                                            {refreshTokenDetails.token}
                                        </p>
                                    </div>

                                    <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-lg p-4 flex items-start gap-3">
                                        <AlertTriangle className="h-5 w-5 text-yellow-600 mt-0.5" />
                                        <div>
                                            <h4 className="text-sm font-semibold text-yellow-600">Opaque System Token</h4>
                                            <p className="text-xs text-muted-foreground mt-1">
                                                This is a secure, opaque token handled by the backend. It has a longer lifespan than the access token.
                                            </p>
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <div className="flex items-center gap-2 bg-red-500/10 text-red-600 px-3 py-2 rounded-lg w-fit">
                                    <AlertTriangle className="h-4 w-4" />
                                    <span className="text-sm font-medium">Not Found</span>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            </section>
        </div>
    )
}

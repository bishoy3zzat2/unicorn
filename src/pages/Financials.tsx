import { Card, CardContent } from '../components/ui/card'
import { BarChart3 } from 'lucide-react'

export function Financials() {
    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Financials</h1>
                <p className="text-muted-foreground mt-2">
                    Financial reports and analytics
                </p>
            </div>

            {/* Coming Soon Card */}
            <Card>
                <CardContent className="flex flex-col items-center justify-center py-16">
                    <div className="p-6 rounded-full bg-primary/10 mb-4">
                        <BarChart3 className="h-16 w-16 text-primary" />
                    </div>
                    <h2 className="text-2xl font-bold mb-2">Coming Soon</h2>
                    <p className="text-muted-foreground text-center max-w-md">
                        Financial reports, analytics, and detailed revenue breakdowns will be available here.
                    </p>
                </CardContent>
            </Card>
        </div>
    )
}

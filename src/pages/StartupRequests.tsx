import { StartupsTable } from '../components/dashboard/StartupsTable'

export function StartupRequests() {
    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Startup Requests</h1>
                <p className="text-muted-foreground mt-2">
                    Review and manage pending startup applications
                </p>
            </div>

            {/* Startups Table */}
            <StartupsTable />
        </div>
    )
}

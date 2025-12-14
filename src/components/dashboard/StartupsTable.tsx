import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table'
import { Startup } from '../../types'
import { formatDate } from '../../lib/utils'
import { StartupReviewModal } from '../admin/StartupReviewModal'
import { useAuth } from '../../contexts/AuthContext'
import { toast } from 'sonner'

export function StartupsTable() {
    const [startups, setStartups] = useState<Startup[]>([])
    const [loading, setLoading] = useState(true)
    const [selectedStartup, setSelectedStartup] = useState<Startup | null>(null)
    const [isModalOpen, setIsModalOpen] = useState(false)
    const { token } = useAuth()

    const fetchPendingStartups = async () => {
        setLoading(true)
        try {
            const response = await fetch('/api/v1/admin/startups?status=PENDING', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            })

            if (!response.ok) {
                throw new Error('Failed to fetch startups')
            }

            const data = await response.json()
            setStartups(data)
        } catch (error: any) {
            toast.error(error.message || 'Failed to load startups')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchPendingStartups()
    }, [])

    const handleRowClick = (startup: Startup) => {
        setSelectedStartup(startup)
        setIsModalOpen(true)
    }

    const handleModalClose = () => {
        setIsModalOpen(false)
        setSelectedStartup(null)
    }

    const handleStatusUpdated = () => {
        // Refresh the list after approve/reject
        fetchPendingStartups()
    }

    const getStatusBadgeClass = (status: string) => {
        switch (status) {
            case 'APPROVED':
                return 'bg-green-950/50 text-green-400 border border-green-900'
            case 'PENDING':
                return 'bg-yellow-950/50 text-yellow-400 border border-yellow-900'
            case 'REJECTED':
                return 'bg-red-950/50 text-red-400 border border-red-900'
            default:
                return 'bg-slate-800 text-slate-400 border border-slate-700'
        }
    }

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
        }).format(amount)
    }

    if (loading) {
        return (
            <Card>
                <CardContent className="flex items-center justify-center py-16">
                    <div className="text-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
                        <p className="text-muted-foreground">Loading pending startups...</p>
                    </div>
                </CardContent>
            </Card>
        )
    }

    return (
        <>
            <Card>
                <CardHeader>
                    <CardTitle>Pending Startup Requests</CardTitle>
                    <CardDescription>
                        {startups.length} startup{startups.length !== 1 ? 's' : ''} awaiting approval
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {startups.length === 0 ? (
                        <div className="text-center py-8 text-muted-foreground">
                            No pending startup requests
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Startup Name</TableHead>
                                    <TableHead>Industry</TableHead>
                                    <TableHead>Funding Goal</TableHead>
                                    <TableHead>Owner</TableHead>
                                    <TableHead>Date Submitted</TableHead>
                                    <TableHead>Status</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {startups.map((startup) => (
                                    <TableRow
                                        key={startup.id}
                                        onClick={() => handleRowClick(startup)}
                                        className="cursor-pointer hover:bg-slate-800/50 transition-colors"
                                    >
                                        <TableCell className="font-medium">{startup.name}</TableCell>
                                        <TableCell>{startup.industry || '—'}</TableCell>
                                        <TableCell>
                                            {startup.fundingGoal ? formatCurrency(startup.fundingGoal) : '—'}
                                        </TableCell>
                                        <TableCell>{startup.ownerEmail}</TableCell>
                                        <TableCell>{formatDate(startup.createdAt)}</TableCell>
                                        <TableCell>
                                            <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium ${getStatusBadgeClass(startup.status)}`}>
                                                {startup.status}
                                            </span>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>

            {/* Review Modal */}
            <StartupReviewModal
                startup={selectedStartup}
                isOpen={isModalOpen}
                onClose={handleModalClose}
                onStatusUpdated={handleStatusUpdated}
            />
        </>
    )
}

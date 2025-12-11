import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table'
import { Button } from '../../components/ui/button'
import { startupRequests, StartupRequest } from '../../lib/mockData'
import { formatDate } from '../../lib/utils'
import { CheckCircle2, XCircle } from 'lucide-react'

export function StartupsTable() {
    const [requests, setRequests] = useState<StartupRequest[]>(startupRequests)

    const handleApprove = (id: string) => {
        setRequests((prev) =>
            prev.map((req) => (req.id === id ? { ...req, status: 'approved' as const } : req))
        )
        console.log(`Approved startup request: ${id}`)
    }

    const handleReject = (id: string) => {
        setRequests((prev) =>
            prev.map((req) => (req.id === id ? { ...req, status: 'rejected' as const } : req))
        )
        console.log(`Rejected startup request: ${id}`)
    }

    const pendingRequests = requests.filter((req) => req.status === 'pending')

    return (
        <Card>
            <CardHeader>
                <CardTitle>Pending Startup Requests</CardTitle>
                <CardDescription>
                    {pendingRequests.length} startup{pendingRequests.length !== 1 ? 's' : ''} awaiting approval
                </CardDescription>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Startup Name</TableHead>
                            <TableHead>Founder</TableHead>
                            <TableHead>Date Submitted</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {requests.map((request) => (
                            <TableRow key={request.id}>
                                <TableCell className="font-medium">{request.startupName}</TableCell>
                                <TableCell>{request.founderName}</TableCell>
                                <TableCell>{formatDate(request.dateSubmitted)}</TableCell>
                                <TableCell>
                                    <span
                                        className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${request.status === 'pending'
                                            ? 'bg-yellow-500/10 text-yellow-500'
                                            : request.status === 'approved'
                                                ? 'bg-emerald-500/10 text-emerald-500'
                                                : 'bg-red-500/10 text-red-500'
                                            }`}
                                    >
                                        {request.status === 'approved' && <CheckCircle2 className="h-3 w-3" />}
                                        {request.status === 'rejected' && <XCircle className="h-3 w-3" />}
                                        {request.status.charAt(0).toUpperCase() + request.status.slice(1)}
                                    </span>
                                </TableCell>
                                <TableCell className="text-right">
                                    <div className="flex gap-2 justify-end">
                                        {request.status === 'pending' && (
                                            <>
                                                <Button
                                                    variant="success"
                                                    size="sm"
                                                    onClick={() => handleApprove(request.id)}
                                                >
                                                    Approve
                                                </Button>
                                                <Button
                                                    variant="danger"
                                                    size="sm"
                                                    onClick={() => handleReject(request.id)}
                                                >
                                                    Reject
                                                </Button>
                                            </>
                                        )}
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    )
}

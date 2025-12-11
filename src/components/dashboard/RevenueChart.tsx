import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { revenueData } from '../../lib/mockData'
import { formatCurrency } from '../../lib/utils'

export function RevenueChart() {
    return (
        <Card>
            <CardHeader>
                <CardTitle>Monthly Recurring Revenue</CardTitle>
                <CardDescription>Revenue trend over the past 12 months</CardDescription>
            </CardHeader>
            <CardContent>
                <ResponsiveContainer width="100%" height={350}>
                    <LineChart data={revenueData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                        <defs>
                            <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="hsl(262, 83%, 58%)" stopOpacity={0.3} />
                                <stop offset="95%" stopColor="hsl(262, 83%, 58%)" stopOpacity={0} />
                            </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                        <XAxis
                            dataKey="month"
                            className="text-xs"
                            tick={{ fill: 'hsl(var(--muted-foreground))' }}
                        />
                        <YAxis
                            className="text-xs"
                            tick={{ fill: 'hsl(var(--muted-foreground))' }}
                            tickFormatter={(value) => `$${value / 1000}k`}
                        />
                        <Tooltip
                            contentStyle={{
                                backgroundColor: 'hsl(var(--card))',
                                border: '1px solid hsl(var(--border))',
                                borderRadius: '8px',
                            }}
                            formatter={(value: number) => [formatCurrency(value), 'Revenue']}
                        />
                        <Line
                            type="monotone"
                            dataKey="revenue"
                            stroke="hsl(262, 83%, 58%)"
                            strokeWidth={3}
                            fill="url(#colorRevenue)"
                            dot={{ fill: 'hsl(262, 83%, 58%)', strokeWidth: 2, r: 5 }}
                            activeDot={{ r: 7 }}
                        />
                    </LineChart>
                </ResponsiveContainer>
            </CardContent>
        </Card>
    )
}

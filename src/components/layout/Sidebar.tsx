import { Link, useLocation } from 'react-router-dom'
import { LayoutDashboard, Users, DollarSign, Settings, Menu, X, LogOut, Shield, Rocket, UserCheck } from 'lucide-react'
import { cn } from '../../lib/utils'
import { useState } from 'react'
import { useAuth } from '../../contexts/AuthContext'

const menuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'User Management', path: '/users', icon: Users },
    { name: 'Startups', path: '/startups', icon: Rocket },
    { name: 'Investor Verification', path: '/verification', icon: UserCheck },
    { name: 'Financials', path: '/financials', icon: DollarSign },
    { name: 'Security', path: '/security', icon: Shield },
    { name: 'Settings', path: '/settings', icon: Settings },
]

export function Sidebar() {
    const location = useLocation()
    const [isMobileOpen, setIsMobileOpen] = useState(false)
    const { logout } = useAuth()

    return (
        <>
            {/* Mobile menu button */}
            <button
                onClick={() => setIsMobileOpen(!isMobileOpen)}
                className="lg:hidden fixed top-4 left-4 z-50 p-2 rounded-lg bg-card border border-border"
            >
                {isMobileOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>

            {/* Backdrop for mobile */}
            {isMobileOpen && (
                <div
                    className="lg:hidden fixed inset-0 bg-black/50 z-30"
                    onClick={() => setIsMobileOpen(false)}
                />
            )}

            {/* Sidebar */}
            <aside
                className={cn(
                    "fixed left-0 top-0 h-screen w-64 glass border-r border-border transition-transform duration-300 ease-in-out z-40",
                    isMobileOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
                )}
            >
                <div className="flex flex-col h-full">
                    {/* Logo */}
                    <div className="p-6 border-b border-border">
                        <h1 className="text-2xl font-bold bg-gradient-to-r from-purple-600 via-purple-500 to-pink-500 bg-clip-text text-transparent">
                            🦄 Unicorn
                        </h1>
                    </div>

                    {/* Navigation */}
                    <nav className="flex-1 p-4 space-y-2">
                        {menuItems.map((item) => {
                            const Icon = item.icon
                            const isActive = location.pathname === item.path

                            return (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    onClick={() => setIsMobileOpen(false)}
                                    className={cn(
                                        "flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200",
                                        isActive
                                            ? "bg-gradient-to-r from-purple-600 to-purple-500 text-white shadow-lg shadow-purple-500/30"
                                            : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                                    )}
                                >
                                    <Icon className="h-5 w-5" />
                                    <span className="font-medium">{item.name}</span>
                                </Link>
                            )
                        })}
                    </nav>

                    {/* Footer */}
                    <div className="p-4 border-t border-border space-y-4">
                        <button
                            onClick={logout}
                            className="flex items-center gap-3 px-4 py-3 w-full rounded-lg text-muted-foreground hover:bg-red-950/30 hover:text-red-400 transition-all duration-200"
                        >
                            <LogOut className="h-5 w-5" />
                            <span className="font-medium">Logout</span>
                        </button>

                        <div className="text-xs text-muted-foreground text-center">
                            v1.0.0 • © 2024 Unicorn
                        </div>
                    </div>
                </div>
            </aside>
        </>
    )
}

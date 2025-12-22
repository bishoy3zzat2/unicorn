import { Link, useLocation } from 'react-router-dom'
import {
    LayoutDashboard, Users, DollarSign, Settings,
    Menu, X, Shield, Rocket, UserCheck, Flag, Banknote
} from 'lucide-react'
import { cn } from '../../lib/utils'
import { useState } from 'react'

const mainMenuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Startups', path: '/startups', icon: Rocket },
    { name: 'Reports', path: '/reports', icon: Flag },
]

const financeMenuItems = [
    { name: 'Financials', path: '/financials', icon: DollarSign },
    { name: 'Investor Verification', path: '/verification', icon: UserCheck },
    { name: 'Deals', path: '/deals', icon: Banknote },
]

const systemMenuItems = [
    { name: 'User Management', path: '/users', icon: Users },
    { name: 'Security', path: '/security', icon: Shield },
    { name: 'Settings', path: '/settings', icon: Settings },
]

export function Sidebar() {
    const location = useLocation()
    const [isMobileOpen, setIsMobileOpen] = useState(false)

    const NavItem = ({ item }: { item: any }) => {
        const Icon = item.icon
        const isActive = location.pathname === item.path

        return (
            <Link
                to={item.path}
                onClick={() => setIsMobileOpen(false)}
                className={cn(
                    "group relative flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 outline-none",
                    isActive
                        ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/20"
                        : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-100/50 dark:hover:bg-slate-800/50"
                )}
            >
                <Icon className={cn(
                    "h-5 w-5 transition-transform duration-200",
                    isActive ? "text-white" : "text-slate-400 group-hover:text-blue-600 group-hover:scale-110"
                )} />
                <span className={cn(
                    "font-medium tracking-tight text-sm",
                    isActive && "font-semibold"
                )}>
                    {item.name}
                </span>

                {isActive && (
                    <div className="absolute right-2 h-1.5 w-1.5 rounded-full bg-white/50 animate-pulse shadow-sm" />
                )}
            </Link>
        )
    }

    const SectionLabel = ({ label }: { label: string }) => (
        <h3 className="px-4 text-[10px] font-bold text-slate-400/80 uppercase tracking-widest mb-1 mt-6 first:mt-2">
            {label}
        </h3>
    )

    return (
        <>
            {/* Mobile menu button */}
            <button
                onClick={() => setIsMobileOpen(!isMobileOpen)}
                className="lg:hidden fixed top-7 left-4 z-50 p-2.5 rounded-xl bg-white/80 dark:bg-slate-900/80 border border-slate-200 dark:border-slate-800 backdrop-blur-md shadow-sm"
            >
                {isMobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>

            {/* Backdrop for mobile */}
            {isMobileOpen && (
                <div
                    className="lg:hidden fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-30 animate-in fade-in"
                    onClick={() => setIsMobileOpen(false)}
                />
            )}

            {/* Sidebar */}
            <aside
                className={cn(
                    "fixed left-0 top-0 h-[100dvh] w-[280px] border-r border-slate-200/60 dark:border-slate-800/60 transition-transform duration-300 ease-in-out z-40 bg-white/80 dark:bg-slate-950/80 backdrop-blur-2xl shadow-2xl lg:shadow-none overflow-hidden",
                    isMobileOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
                )}
            >
                <div className="flex flex-col h-full">
                    {/* Logo Section */}
                    <div className="min-h-[80px] lg:h-20 flex items-center px-6 pt-24 pb-6 lg:py-0 border-b border-slate-100/10 dark:border-slate-800/10 bg-gradient-to-b from-white/50 to-transparent dark:from-slate-900/50">
                        <Link to="/" className="flex items-center gap-3 group">
                            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-xl shadow-lg shadow-blue-500/20 group-hover:shadow-blue-500/30 group-hover:scale-105 transition-all duration-300 ring-2 ring-white/20">
                                🦄
                            </div>
                            <div className="flex flex-col">
                                <span className="font-bold text-lg bg-clip-text text-transparent bg-gradient-to-r from-slate-900 via-slate-700 to-slate-900 dark:from-white dark:via-slate-200 dark:to-white">
                                    Unicorn
                                </span>
                                <span className="text-[10px] uppercase tracking-wider font-semibold text-slate-400">
                                    Admin Console
                                </span>
                            </div>
                        </Link>
                    </div>

                    {/* Navigation */}
                    <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto custom-scrollbar">
                        <SectionLabel label="Overview" />
                        {mainMenuItems.map((item) => <NavItem key={item.path} item={item} />)}

                        <SectionLabel label="Finance & Legal" />
                        {financeMenuItems.map((item) => <NavItem key={item.path} item={item} />)}

                        <SectionLabel label="Management" />
                        {systemMenuItems.map((item) => <NavItem key={item.path} item={item} />)}
                    </nav>

                    {/* Footer */}
                    <div className="p-4 border-t border-slate-200/50 dark:border-slate-800/50 bg-slate-50/50 dark:bg-slate-900/30">
                        <div className="text-[10px] text-slate-400 text-center font-mono uppercase tracking-widest">
                            v1.0.0 • Build 2024.12
                        </div>
                    </div>
                </div>
            </aside>
        </>
    )
}

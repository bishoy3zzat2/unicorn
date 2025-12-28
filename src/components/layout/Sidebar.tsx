import { Link, useLocation } from 'react-router-dom'
import {
    LayoutDashboard, Users, DollarSign, Settings,
    Menu, X, Shield, Rocket, UserCheck, Flag, Banknote, Newspaper, Bell
} from 'lucide-react'
import { cn } from '../../lib/utils'
import { useState } from 'react'

const mainMenuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard, color: 'text-blue-500' },
    { name: 'Startups', path: '/startups', icon: Rocket, color: 'text-purple-500' },
    { name: 'Feed', path: '/feed', icon: Newspaper, color: 'text-pink-500' },
    { name: 'Reports', path: '/reports', icon: Flag, color: 'text-orange-500' },
    { name: 'Notifications', path: '/notifications', icon: Bell, color: 'text-yellow-500' },
]

const financeMenuItems = [
    { name: 'Financials', path: '/financials', icon: DollarSign, color: 'text-emerald-500' },
    { name: 'Investor Verification', path: '/verification', icon: UserCheck, color: 'text-cyan-500' },
    { name: 'Deals', path: '/deals', icon: Banknote, color: 'text-lime-500' },
]

const systemMenuItems = [
    { name: 'User Management', path: '/users', icon: Users, color: 'text-indigo-500' },
    { name: 'Security', path: '/security', icon: Shield, color: 'text-teal-500' },
    { name: 'Settings', path: '/settings', icon: Settings, color: 'text-slate-500' },
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
                    "group relative flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 outline-none mb-1",
                    isActive
                        ? "bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-white shadow-sm ring-1 ring-slate-200 dark:ring-slate-700"
                        : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800/50"
                )}
            >
                <div className={cn(
                    "p-1.5 rounded-md transition-colors duration-200",
                    isActive ? "bg-white dark:bg-slate-700 shadow-sm" : "bg-slate-100/50 dark:bg-slate-800/50 group-hover:bg-white dark:group-hover:bg-slate-700"
                )}>
                    <Icon className={cn(
                        "h-4 w-4 transition-transform duration-200",
                        isActive ? item.color : "text-slate-400 group-hover:scale-110",
                        !isActive && "group-hover:" + item.color
                    )} />
                </div>

                <span className={cn(
                    "font-medium tracking-tight text-sm flex-1",
                    isActive && "font-semibold"
                )}>
                    {item.name}
                </span>

                {isActive && (
                    <div className={cn("h-1.5 w-1.5 rounded-full animate-pulse shadow-sm", item.color.replace('text-', 'bg-'))} />
                )}
            </Link>
        )
    }

    const SectionLabel = ({ label }: { label: string }) => (
        <div className="flex items-center px-4 mt-6 mb-2 group">
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800 group-hover:bg-slate-300 dark:group-hover:bg-slate-700 transition-colors" />
            <span className="px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest whitespace-nowrap">
                {label}
            </span>
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800 group-hover:bg-slate-300 dark:group-hover:bg-slate-700 transition-colors" />
        </div>
    )

    return (
        <>
            {/* Mobile menu button */}
            <button
                onClick={() => setIsMobileOpen(!isMobileOpen)}
                className={cn(
                    "lg:hidden fixed top-[2rem] left-[1.5rem] z-50 h-10 w-10 rounded-xl flex items-center justify-center transition-all duration-200",
                    isMobileOpen
                        ? "bg-gradient-to-br from-red-500 to-rose-600 text-white shadow-md shadow-red-500/20"
                        : "text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white"
                )}
            >
                {isMobileOpen ? (
                    <X className="h-5 w-5" />
                ) : (
                    <Menu className="h-5 w-5" />
                )}
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
                    "fixed left-0 top-0 h-[100dvh] w-[280px] border-r border-slate-200/60 dark:border-slate-800/60 transition-transform duration-300 ease-in-out z-40 bg-white/95 dark:bg-slate-950/95 backdrop-blur-2xl shadow-2xl lg:shadow-none overflow-hidden flex flex-col",
                    isMobileOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
                )}
            >
                {/* Logo Section */}
                <div className="flex-none min-h-[80px] lg:h-20 flex items-center pl-20 pr-6 pt-7 pb-6 lg:px-6 lg:py-0 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50">
                    <Link to="/" className="flex items-center gap-3 group w-full">
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
                <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto sidebar-scrollbar">
                    <SectionLabel label="Overview" />
                    {mainMenuItems.map((item) => <NavItem key={item.path} item={item} />)}

                    <SectionLabel label="Finance" />
                    {financeMenuItems.map((item) => <NavItem key={item.path} item={item} />)}

                    <SectionLabel label="System" />
                    {systemMenuItems.map((item) => <NavItem key={item.path} item={item} />)}

                    <div className="h-10" /> {/* Spacer */}
                </nav>

                {/* Footer */}
                <div className="flex-none p-4 border-t border-slate-200/50 dark:border-slate-800/50 bg-slate-50/80 dark:bg-slate-900/50 backdrop-blur-sm">
                    <div className="flex items-center justify-between text-[10px] text-slate-400 font-medium px-2">
                        <span>v1.0.0</span>
                        <span>Build 2024.12</span>
                    </div>
                </div>
            </aside>

            <style>{`
                .sidebar-scrollbar::-webkit-scrollbar {
                    width: 5px;
                }
                .sidebar-scrollbar::-webkit-scrollbar-track {
                    background: transparent;
                }
                .sidebar-scrollbar::-webkit-scrollbar-thumb {
                    background: rgba(148, 163, 184, 0.2);
                    border-radius: 10px;
                }
                .sidebar-scrollbar::-webkit-scrollbar-thumb:hover {
                    background: rgba(148, 163, 184, 0.4);
                }
                .dark .sidebar-scrollbar::-webkit-scrollbar-thumb {
                    background: rgba(71, 85, 105, 0.2);
                }
                .dark .sidebar-scrollbar::-webkit-scrollbar-thumb:hover {
                    background: rgba(71, 85, 105, 0.4);
                }
            `}</style>
        </>
    )
}

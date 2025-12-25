import { Moon, Sun, ChevronRight, LogOut } from 'lucide-react'
import { useTheme } from '../../contexts/ThemeContext'
import { useAuth } from '../../contexts/AuthContext'
import { useLocation, Link } from 'react-router-dom'
import { Button } from '../ui/button'
import { Avatar, AvatarFallback, AvatarImage } from '../ui/avatar'

export function Header() {
    const { theme, toggleTheme } = useTheme()
    const { user, logout } = useAuth()
    const location = useLocation()

    // Generate breadcrumbs from path
    const pathSegments = location.pathname.split('/').filter(Boolean)
    const breadcrumbs = pathSegments.map((segment, index) => {
        const path = `/${pathSegments.slice(0, index + 1).join('/')}`
        const name = segment.charAt(0).toUpperCase() + segment.slice(1).replace('-', ' ')
        return { name, path }
    })

    return (
        <header className="fixed top-4 right-0 left-0 lg:left-[300px] z-30 px-3 lg:px-6 transition-all duration-300">
            <div className="flex items-center justify-between px-5 py-3 rounded-2xl bg-gradient-to-br from-slate-50/95 via-white/95 to-slate-100/95 dark:from-slate-800/95 dark:via-slate-900/95 dark:to-slate-800/95 backdrop-blur-2xl border-2 border-slate-200 dark:border-slate-700 shadow-lg shadow-slate-300/30 dark:shadow-black/40 transition-all duration-300">
                {/* Left Section: Breadcrumbs */}
                <div className="flex items-center gap-3 pl-10 lg:pl-0">
                    <Link
                        to="/"
                        className="text-sm font-medium text-slate-500 hover:text-blue-600 dark:text-slate-400 dark:hover:text-blue-400 transition-colors"
                    >
                        Dashboard
                    </Link>
                    {breadcrumbs.map((crumb) => (
                        <div key={crumb.path} className="flex items-center gap-3">
                            <ChevronRight className="h-4 w-4 text-slate-300 dark:text-slate-600" />
                            <Link
                                to={crumb.path}
                                className="text-sm font-semibold text-slate-800 dark:text-slate-100 hover:text-blue-600 dark:hover:text-blue-400 transition-colors capitalize px-3 py-1 rounded-lg bg-slate-100/80 dark:bg-slate-700/50"
                            >
                                {crumb.name}
                            </Link>
                        </div>
                    ))}
                </div>

                {/* Right Section */}
                <div className="flex items-center gap-2 lg:gap-3">
                    {/* Theme Toggle */}
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={toggleTheme}
                        className="h-9 w-9 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500"
                    >
                        {theme === 'dark' ? <Moon className="h-4 w-4" /> : <Sun className="h-4 w-4" />}
                    </Button>

                    <div className="h-6 w-px bg-slate-200 dark:bg-slate-800 mx-1 hidden sm:block" />

                    {/* User Profile Content (Responsive) */}
                    <div className="flex items-center gap-3 pl-1.5 pr-1.5 md:pl-2 md:pr-4 py-1.5 rounded-xl border border-transparent hover:border-slate-200 dark:hover:border-slate-800 hover:bg-slate-50/50 dark:hover:bg-slate-900/50 transition-all duration-200">
                        <Avatar className="h-8 w-8 md:h-9 md:w-9 border-2 border-white dark:border-slate-800 shadow-sm">
                            <AvatarImage src={user?.avatarUrl} />
                            <AvatarFallback className="bg-gradient-to-br from-blue-500 to-indigo-600 text-xs text-white">
                                {user?.email?.[0]?.toUpperCase()}
                            </AvatarFallback>
                        </Avatar>

                        {/* Hide text details on mobile */}
                        <div className="hidden md:flex flex-col items-start min-w-[100px]">
                            <span className="text-sm font-semibold leading-none text-slate-900 dark:text-slate-100 mb-1">
                                {user?.email}
                            </span>
                            <div className="flex items-center gap-1.5">
                                <span className="h-1.5 w-1.5 rounded-full bg-blue-500 animate-pulse" />
                                <span className="text-[10px] text-slate-500 capitalize leading-none font-medium">
                                    {user?.role === 'SUPER_ADMIN' ? 'Super Admin' : user?.role === 'ADMIN' ? 'Admin' : 'User'}
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Separate Logout Button */}
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={logout}
                        className="h-9 w-9 rounded-xl text-red-500 hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-950/30 transition-colors"
                        title="Log out"
                    >
                        <LogOut className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </header>
    )
}

import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Header } from './Header'

export function Layout({ children }: { children?: React.ReactNode }) {
    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-950 dark:to-slate-900 transition-colors duration-300">
            <Sidebar />
            <div className="lg:ml-[300px] min-h-screen flex flex-col overflow-x-hidden">
                <Header />
                <main className="flex-1 px-3 lg:px-6 pb-6 pt-28 lg:pt-32 space-y-6">
                    {children || <Outlet />}
                </main>
            </div>
        </div>
    )
}

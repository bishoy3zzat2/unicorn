import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Header } from './Header'

export function Layout({ children }: { children?: React.ReactNode }) {
    return (
        <div className="min-h-screen bg-background">
            <Sidebar />
            <div className="lg:ml-64">
                <Header />
                <main className="p-6">
                    {children || <Outlet />}
                </main>
            </div>
        </div>
    )
}

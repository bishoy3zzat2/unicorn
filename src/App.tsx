import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { ThemeProvider } from './contexts/ThemeContext'
import { AuthProvider } from './contexts/AuthContext'
import { Layout } from './components/layout/Layout'
import { Overview } from './pages/Overview'
import { StartupRequests } from './pages/StartupRequests'
import { UserManagement } from './pages/UserManagement'
import { Financials } from './pages/Financials'
import { Settings } from './pages/Settings'
import { Security } from './pages/Security'
import { InvestorVerificationPage } from './pages/InvestorVerification'
import { Reports } from './pages/Reports'
import { Deals } from './pages/Deals'
import Login from './pages/Login'
import { ProtectedRoute } from './components/ProtectedRoute'

import { Toaster } from 'sonner'

import { useTheme } from './contexts/ThemeContext'

function AppToaster() {
    const { theme } = useTheme()
    return <Toaster position="bottom-right" theme={theme} richColors closeButton />
}

function App() {
    return (
        <ThemeProvider>
            <AuthProvider>
                <BrowserRouter>
                    <AppToaster />
                    <Routes>
                        <Route path="/login" element={<Login />} />

                        <Route element={<ProtectedRoute />}>
                            <Route element={<Layout />}>
                                <Route path="/" element={<Overview />} />
                                <Route path="/startups" element={<StartupRequests />} />
                                <Route path="/users" element={<UserManagement />} />
                                <Route path="/verification" element={<InvestorVerificationPage />} />
                                <Route path="/financials" element={<Financials />} />
                                <Route path="/security" element={<Security />} />
                                <Route path="/reports" element={<Reports />} />
                                <Route path="/deals" element={<Deals />} />
                                <Route path="/settings" element={<Settings />} />
                            </Route>
                        </Route>
                    </Routes>
                </BrowserRouter>
            </AuthProvider>
        </ThemeProvider>
    )
}

export default App

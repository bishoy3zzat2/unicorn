import { Moon, Sun, User } from 'lucide-react'
import { useTheme } from '../../contexts/ThemeContext'
import { Switch } from '../../components/ui/switch'

export function Header() {
    const { theme, toggleTheme } = useTheme()

    return (
        <header className="sticky top-0 z-20 glass border-b border-border">
            <div className="flex items-center justify-between px-4 py-3 lg:px-6 lg:py-4">
                <div className="flex-1" />

                {/* Right Section */}
                <div className="flex items-center gap-2 lg:gap-4 ml-4">
                    {/* Dark Mode Toggle */}
                    <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-accent/50">
                        <Sun className="h-4 w-4 text-muted-foreground" />
                        <Switch
                            checked={theme === 'dark'}
                            onCheckedChange={toggleTheme}
                        />
                        <Moon className="h-4 w-4 text-muted-foreground" />
                    </div>

                    {/* User Profile */}
                    <button className="flex items-center gap-2 px-2 py-1.5 lg:px-3 lg:py-2 rounded-lg hover:bg-accent transition-colors">
                        <div className="h-8 w-8 rounded-full bg-primary flex items-center justify-center">
                            <User className="h-4 w-4 text-primary-foreground" />
                        </div>
                        <div className="hidden md:block text-left">
                            <p className="text-sm font-medium">Admin User</p>
                            <p className="text-xs text-muted-foreground">admin@unicorn.com</p>
                        </div>
                    </button>
                </div>
            </div>
        </header>
    )
}

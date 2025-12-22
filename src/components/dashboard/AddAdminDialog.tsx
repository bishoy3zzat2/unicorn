import { useState } from 'react'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '../ui/dialog'
import { Button } from '../ui/button'
import { Input } from '../ui/input'
import { Label } from '../ui/label'
import { Loader2, ShieldCheck, UserPlus, Mail, Lock, User as UserIcon } from 'lucide-react'
import api from '../../lib/axios'
import { toast } from 'sonner'
import { z } from 'zod'

interface AddAdminDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    onSuccess?: () => void
}

const adminSchema = z.object({
    firstName: z.string().min(2, "First name must be at least 2 characters"),
    lastName: z.string().min(2, "Last name must be at least 2 characters"),
    username: z.string().min(3, "Username must be at least 3 characters").regex(/^[a-z0-9-_]+$/, "Username can only contain lowercase letters, numbers, dashes, and underscores"),
    email: z.string().email("Invalid email address"),
    password: z.string().min(6, "Password must be at least 6 characters"),
})

export function AddAdminDialog({ open, onOpenChange, onSuccess }: AddAdminDialogProps) {
    const [isLoading, setIsLoading] = useState(false)
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        password: '',
    })

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData(prev => ({ ...prev, [e.target.id]: e.target.value }))
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setIsLoading(true)

        try {
            // Validate
            const validatedData = adminSchema.parse(formData)

            await api.post('/admin/users', validatedData)

            toast.success('Admin user created successfully')
            onOpenChange(false)
            onSuccess?.()

            // Reset form
            setFormData({
                firstName: '',
                lastName: '',
                username: '',
                email: '',
                password: '',
            })
        } catch (error: any) {
            if (error instanceof z.ZodError) {
                toast.error(error.issues[0].message)
            } else {
                console.error('Failed to create admin:', error)
                const message = error.response?.data || error.message || 'Failed to create admin user'
                toast.error(typeof message === 'string' ? message : 'Failed to create admin user')
            }
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[500px] p-0 gap-0 border-none shadow-2xl bg-white dark:bg-slate-950 overflow-hidden">
                <div className="bg-gradient-to-r from-slate-800 via-blue-900/50 to-indigo-900/50 dark:from-slate-900 dark:via-blue-950/80 dark:to-indigo-950/80 p-6 shrink-0 border-b border-slate-700/50">
                    <DialogHeader className="space-y-2">
                        <DialogTitle className="flex items-center gap-3 text-2xl font-bold tracking-tight text-white">
                            <div className="h-10 w-10 rounded-xl bg-white/20 flex items-center justify-center backdrop-blur-sm shadow-inner">
                                <ShieldCheck className="h-6 w-6 text-white" />
                            </div>
                            Add New Admin
                        </DialogTitle>
                        <DialogDescription className="text-blue-100 text-base">
                            Create a new administrator account with full access.
                        </DialogDescription>
                    </DialogHeader>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">
                    <div className="grid grid-cols-2 gap-5">
                        <div className="space-y-2">
                            <Label htmlFor="firstName" className="font-semibold text-foreground/80">First Name</Label>
                            <div className="relative">
                                <UserIcon className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                                <Input
                                    id="firstName"
                                    placeholder="John"
                                    className="pl-9 bg-slate-50 dark:bg-slate-900/50 border-slate-200 dark:border-slate-800 focus-visible:ring-blue-500 transition-all"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="lastName" className="font-semibold text-foreground/80">Last Name</Label>
                            <div className="relative">
                                <UserIcon className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                                <Input
                                    id="lastName"
                                    placeholder="Doe"
                                    className="pl-9 bg-slate-50 dark:bg-slate-900/50 border-slate-200 dark:border-slate-800 focus-visible:ring-blue-500 transition-all"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="username" className="font-semibold text-foreground/80">Username</Label>
                        <div className="relative">
                            <ShieldCheck className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                                id="username"
                                placeholder="johndoe"
                                className="pl-9 bg-slate-50 dark:bg-slate-900/50 border-slate-200 dark:border-slate-800 focus-visible:ring-blue-500 transition-all"
                                value={formData.username}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="email" className="font-semibold text-foreground/80">Email Address</Label>
                        <div className="relative">
                            <Mail className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                                id="email"
                                type="email"
                                placeholder="john.doe@unicorn.com"
                                className="pl-9 bg-slate-50 dark:bg-slate-900/50 border-slate-200 dark:border-slate-800 focus-visible:ring-blue-500 transition-all"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="password" className="font-semibold text-foreground/80">Password</Label>
                        <div className="relative">
                            <Lock className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                                id="password"
                                type="password"
                                placeholder="••••••••"
                                className="pl-9 bg-slate-50 dark:bg-slate-900/50 border-slate-200 dark:border-slate-800 focus-visible:ring-blue-500 transition-all"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <DialogFooter className="pt-2 gap-2">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => onOpenChange(false)}
                            className="bg-transparent hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 border-transparent"
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            disabled={isLoading}
                            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md hover:shadow-lg transition-all"
                        >
                            {isLoading ? (
                                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            ) : (
                                <UserPlus className="h-4 w-4 mr-2" />
                            )}
                            Create Admin Account
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    )
}

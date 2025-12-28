import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    server: {
        host: '0.0.0.0',
        port: 5173,
        strictPort: false,
        allowedHosts: ['053a14cf1317c545-41-46-6-196.serveousercontent.com'], // Allow ngrok domain
        proxy: {
            '/api': {
                target: 'http://localhost:9090',
                changeOrigin: true,
                secure: false,
            },
        },
    },
})

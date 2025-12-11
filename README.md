# Unicorn Admin Dashboard

A modern, production-ready Admin Dashboard built with React, Vite, Tailwind CSS, Shadcn/UI, and Recharts.

## Features

- 🌓 **Dark Mode Support** - Toggle between light and dark themes
- 📊 **Interactive Charts** - Revenue visualization with Recharts
- 📱 **Fully Responsive** - Works seamlessly on mobile, tablet, and desktop
- ✨ **Premium UI/UX** - Glassmorphism effects and smooth animations
- 🎯 **Mock Data** - Pre-loaded with sample data for immediate testing

## Tech Stack

- **React 18** - Modern UI framework
- **TypeScript** - Type-safe development
- **Vite** - Lightning-fast build tool
- **Tailwind CSS v3** - Utility-first CSS framework
- **Shadcn/UI** - High-quality component library
- **Recharts** - Powerful charting library
- **React Router v6** - Client-side routing
- **Lucide React** - Beautiful icon library

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

The application will be available at `http://localhost:5173`

## Project Structure

```
src/
├── components/
│   ├── dashboard/      # Dashboard-specific components
│   ├── layout/         # Layout components (Sidebar, Header)
│   └── ui/            # Shadcn/UI base components
├── contexts/          # React contexts (Theme)
├── lib/              # Utilities and mock data
├── pages/            # Page components
├── App.tsx           # Main app component
├── main.tsx          # Entry point
└── index.css         # Global styles
```

## Pages

- **Dashboard Overview** - KPI cards and revenue chart
- **Startup Requests** - Manage pending startup applications
- **User Management** - View and filter investors and startups
- **Financials** - Coming soon
- **Settings** - Coming soon

## Backend Integration

This frontend is ready to connect to a Spring Boot backend. To integrate:

1. Update the mock data imports to API calls
2. Configure your API base URL
3. Add authentication/authorization
4. Handle loading and error states

## License

MIT

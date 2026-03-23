import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from '@/context/AuthContext'
import { AdminLayout } from '@/components/layout/AdminLayout'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { LoginPage } from '@/pages/LoginPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { VenueListPage } from '@/pages/venues/VenueListPage'
import { VenueCreatePage } from '@/pages/venues/VenueCreatePage'
import { VenueEditPage } from '@/pages/venues/VenueEditPage'
import { CoachListPage } from '@/pages/coaches/CoachListPage'
import { CoachCreatePage } from '@/pages/coaches/CoachCreatePage'
import { CoachEditPage } from '@/pages/coaches/CoachEditPage'
import { StripeConnectPage } from '@/pages/StripeConnectPage'
import { EarningsPage } from '@/pages/EarningsPage'
import { TimeSlotPage } from '@/pages/TimeSlotPage'
import type { ReactNode } from 'react'

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, backendUser, loading } = useAuth()
  if (loading) return <LoadingSpinner />
  if (!user || !backendUser) return <Navigate to="/login" replace />
  return <>{children}</>
}

function AppRoutes() {
  const { user, loading } = useAuth()

  if (loading) return <LoadingSpinner />

  return (
    <Routes>
      <Route
        path="/login"
        element={user ? <Navigate to="/" replace /> : <LoginPage />}
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="venues" element={<VenueListPage />} />
        <Route path="venues/new" element={<VenueCreatePage />} />
        <Route path="venues/:id/edit" element={<VenueEditPage />} />
        <Route path="coaches" element={<CoachListPage />} />
        <Route path="coaches/new" element={<CoachCreatePage />} />
        <Route path="coaches/:id/edit" element={<CoachEditPage />} />
        <Route path="stripe" element={<StripeConnectPage />} />
        <Route path="earnings" element={<EarningsPage />} />
        <Route path="time-slots" element={<TimeSlotPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}

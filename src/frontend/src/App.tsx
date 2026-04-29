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
// v2-practical-ux: parallel "Option 2" pages (master pages above remain Option 1)
import { WeeklyCalendarPage } from '@/pages/v2/WeeklyCalendarPage'
import { PlayHomePage } from '@/pages/v2/PlayHomePage'
// Partner reservation detail (landing page for partner-approval emails)
import { PartnerReservationDetailPage } from '@/pages/partner/PartnerReservationDetailPage'
// Owner-only admin dashboard (gated below by AdminRoute)
import { AdminOverviewPage } from '@/pages/admin/AdminOverviewPage'
import { AdminListingsPage } from '@/pages/admin/AdminListingsPage'
import { AdminListingDetailPage } from '@/pages/admin/AdminListingDetailPage'
import type { ReactNode } from 'react'

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, backendUser, loading } = useAuth()
  if (loading) return <LoadingSpinner />
  if (!user || !backendUser) return <Navigate to="/login" replace />
  return <>{children}</>
}

// Owner-only gate. Layered inside ProtectedRoute, so it assumes the user is
// already authenticated; just enforces role=admin. Non-admins get bounced home.
function AdminRoute({ children }: { children: ReactNode }) {
  const { isAdmin } = useAuth()
  if (!isAdmin) return <Navigate to="/" replace />
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
        {/* v2-practical-ux: Option 2 redesign routes (parallel to master pages above) */}
        <Route path="v2/calendar" element={<WeeklyCalendarPage />} />
        <Route path="v2/play" element={<PlayHomePage />} />
        {/* Partner approval flow: landing page from email links. Login is enforced by ProtectedRoute. */}
        <Route path="partner/reservations/:id" element={<PartnerReservationDetailPage />} />
        {/* Owner-only admin dashboard (admins review listings + see platform metrics). */}
        <Route path="admin" element={<AdminRoute><AdminOverviewPage /></AdminRoute>} />
        <Route path="admin/listings" element={<AdminRoute><AdminListingsPage /></AdminRoute>} />
        <Route
          path="admin/listings/:type/:id"
          element={<AdminRoute><AdminListingDetailPage /></AdminRoute>}
        />
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

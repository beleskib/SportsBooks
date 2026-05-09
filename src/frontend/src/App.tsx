import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from '@/context/AuthContext'
import { AdminLayout } from '@/components/layout/AdminLayout'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { ErrorBoundary } from '@/components/ErrorBoundary'
import { LoginPage } from '@/pages/LoginPage'
import type { ReactNode } from 'react'

const DashboardPage = lazy(() => import('@/pages/DashboardPage').then(m => ({ default: m.DashboardPage })))
const VenueListPage = lazy(() => import('@/pages/venues/VenueListPage').then(m => ({ default: m.VenueListPage })))
const VenueCreatePage = lazy(() => import('@/pages/venues/VenueCreatePage').then(m => ({ default: m.VenueCreatePage })))
const VenueEditPage = lazy(() => import('@/pages/venues/VenueEditPage').then(m => ({ default: m.VenueEditPage })))
const CoachListPage = lazy(() => import('@/pages/coaches/CoachListPage').then(m => ({ default: m.CoachListPage })))
const CoachCreatePage = lazy(() => import('@/pages/coaches/CoachCreatePage').then(m => ({ default: m.CoachCreatePage })))
const CoachEditPage = lazy(() => import('@/pages/coaches/CoachEditPage').then(m => ({ default: m.CoachEditPage })))
const StripeConnectPage = lazy(() => import('@/pages/StripeConnectPage').then(m => ({ default: m.StripeConnectPage })))
const EarningsPage = lazy(() => import('@/pages/EarningsPage').then(m => ({ default: m.EarningsPage })))
const TimeSlotPage = lazy(() => import('@/pages/TimeSlotPage').then(m => ({ default: m.TimeSlotPage })))
// v2-practical-ux: parallel "Option 2" pages (master pages above remain Option 1)
const WeeklyCalendarPage = lazy(() => import('@/pages/v2/WeeklyCalendarPage').then(m => ({ default: m.WeeklyCalendarPage })))
const PlayHomePage = lazy(() => import('@/pages/v2/PlayHomePage').then(m => ({ default: m.PlayHomePage })))
const SettingsPage = lazy(() => import('@/pages/v2/SettingsPage').then(m => ({ default: m.SettingsPage })))
// Partner pages
const PartnerReservationDetailPage = lazy(() => import('@/pages/partner/PartnerReservationDetailPage').then(m => ({ default: m.PartnerReservationDetailPage })))
const PartnerReservationsPage = lazy(() => import('@/pages/partner/PartnerReservationsPage').then(m => ({ default: m.PartnerReservationsPage })))
const PartnerAnalyticsPage = lazy(() => import('@/pages/partner/PartnerAnalyticsPage').then(m => ({ default: m.PartnerAnalyticsPage })))
// Owner-only admin dashboard (gated below by AdminRoute)
const AdminOverviewPage = lazy(() => import('@/pages/admin/AdminOverviewPage').then(m => ({ default: m.AdminOverviewPage })))
const AdminListingsPage = lazy(() => import('@/pages/admin/AdminListingsPage').then(m => ({ default: m.AdminListingsPage })))
const AdminListingDetailPage = lazy(() => import('@/pages/admin/AdminListingDetailPage').then(m => ({ default: m.AdminListingDetailPage })))
const InboxPage = lazy(() => import('@/pages/InboxPage').then(m => ({ default: m.InboxPage })))

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, backendUser, loading } = useAuth()
  if (loading) return <LoadingSpinner />
  if (!user || !backendUser) return <Navigate to="/login" replace />
  return <>{children}</>
}

// Owner-only gate. Layered inside ProtectedRoute, so it assumes the user is
// already authenticated; just enforces role=admin. Non-admins get bounced home.
function AdminRoute({ children }: { children: ReactNode }) {
  const { isAdmin, loading } = useAuth()
  if (loading) return <LoadingSpinner />
  if (!isAdmin) return <Navigate to="/" replace />
  return <>{children}</>
}

function AppRoutes() {
  const { user, loading } = useAuth()

  if (loading) return <LoadingSpinner />

  return (
    <Suspense fallback={<LoadingSpinner />}>
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
          <Route path="reservations" element={<PartnerReservationsPage />} />
          <Route path="analytics" element={<PartnerAnalyticsPage />} />
          <Route path="earnings" element={<EarningsPage />} />
          <Route path="time-slots" element={<TimeSlotPage />} />
          {/* v2-practical-ux: Option 2 redesign routes (parallel to master pages above) */}
          <Route path="v2/calendar" element={<WeeklyCalendarPage />} />
          <Route path="v2/play" element={<PlayHomePage />} />
          <Route path="settings" element={<SettingsPage />} />
          {/* DM inbox */}
          <Route path="inbox" element={<InboxPage />} />
          <Route path="inbox/:friendId" element={<InboxPage />} />
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
    </Suspense>
  )
}

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  )
}

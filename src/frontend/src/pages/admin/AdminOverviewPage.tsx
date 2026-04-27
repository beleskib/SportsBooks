// Owner-only landing page at /admin.
// Shows platform-wide metrics + a "needs your attention" panel pointing
// at the partner approval queue.
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Users as UsersIcon,
  UserCheck,
  Building2,
  GraduationCap,
  CalendarDays,
  TrendingUp,
  AlertCircle,
  ArrowRight,
} from 'lucide-react'
import { adminApi, type PlatformOverview } from '@/api/admin'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

interface MetricCardProps {
  icon: React.ComponentType<{ className?: string }>
  label: string
  value: number
  hint?: string
  emphasis?: 'normal' | 'warn'
}

function MetricCard({ icon: Icon, label, value, hint, emphasis = 'normal' }: MetricCardProps) {
  const ring = emphasis === 'warn' && value > 0 ? 'ring-2 ring-yellow-300/60' : ''
  return (
    <div className={`rounded-xl border border-gray-200 bg-white p-5 shadow-sm ${ring}`}>
      <div className="flex items-center gap-3">
        <div className="rounded-lg bg-gray-900 p-2">
          <Icon className="h-5 w-5 text-yellow-300" />
        </div>
        <div className="text-sm font-medium text-gray-500">{label}</div>
      </div>
      <div className="mt-3 text-3xl font-semibold tabular-nums text-gray-900">
        {value.toLocaleString()}
      </div>
      {hint && <div className="mt-1 text-xs text-gray-500">{hint}</div>}
    </div>
  )
}

export function AdminOverviewPage() {
  const [overview, setOverview] = useState<PlatformOverview | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    adminApi
      .getOverview()
      .then((res) => setOverview(res.data))
      .catch((err) => setError(err?.error?.message ?? 'Could not load overview'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <LoadingSpinner />
  if (error) return <div className="p-8 text-red-600">{error}</div>
  if (!overview) return null

  const hasPending = overview.pendingPartners > 0

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Owner overview</h1>
        <p className="mt-1 text-sm text-gray-500">Platform-wide health at a glance.</p>
      </div>

      {hasPending && (
        <Link
          to="/admin/partners"
          className="flex items-center justify-between rounded-xl border border-yellow-300 bg-yellow-50 p-4 hover:bg-yellow-100 transition"
        >
          <div className="flex items-center gap-3">
            <AlertCircle className="h-5 w-5 text-yellow-700" />
            <div>
              <div className="text-sm font-semibold text-yellow-900">
                {overview.pendingPartners} partner{overview.pendingPartners === 1 ? '' : 's'} awaiting approval
              </div>
              <div className="text-xs text-yellow-800">Review and approve to let them list venues / coaches.</div>
            </div>
          </div>
          <ArrowRight className="h-5 w-5 text-yellow-700" />
        </Link>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <MetricCard
          icon={UsersIcon}
          label="Total users"
          value={overview.totalUsers}
          hint={`${overview.totalPlayers.toLocaleString()} players · ${overview.totalPartners.toLocaleString()} partners`}
        />
        <MetricCard
          icon={UserCheck}
          label="Pending partners"
          value={overview.pendingPartners}
          emphasis="warn"
          hint="Awaiting your approval"
        />
        <MetricCard
          icon={Building2}
          label="Venues listed"
          value={overview.totalVenues}
        />
        <MetricCard
          icon={GraduationCap}
          label="Coaches listed"
          value={overview.totalCoaches}
        />
        <MetricCard
          icon={CalendarDays}
          label="Bookings (30d)"
          value={overview.bookingsLast30d}
          hint={`${overview.totalBookings.toLocaleString()} all-time`}
        />
        <MetricCard
          icon={TrendingUp}
          label="Pending bookings"
          value={overview.pendingBookings}
          hint="Awaiting partner response"
        />
      </div>
    </div>
  )
}

export default AdminOverviewPage

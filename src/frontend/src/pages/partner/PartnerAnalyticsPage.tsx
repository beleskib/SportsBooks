import { useEffect, useState } from 'react'
import {
  BarChart3,
  TrendingUp,
  CalendarCheck,
  Star,
  Clock,
  CheckCircle,
  XCircle,
  ArrowUpRight,
  DollarSign,
} from 'lucide-react'
import { dashboardApi, type PartnerStats } from '@/api/dashboard'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

const STATUS_COLORS: Record<string, { bg: string; text: string; icon: typeof Clock }> = {
  pending: { bg: 'bg-yellow-100', text: 'text-yellow-800', icon: Clock },
  confirmed: { bg: 'bg-blue-100', text: 'text-blue-800', icon: CheckCircle },
  completed: { bg: 'bg-green-100', text: 'text-green-800', icon: CheckCircle },
  cancelled: { bg: 'bg-red-100', text: 'text-red-800', icon: XCircle },
}

export function PartnerAnalyticsPage() {
  const [stats, setStats] = useState<PartnerStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await dashboardApi.getPartnerStats()
        setStats(res.data)
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'Failed to load analytics'
        setError(message)
      } finally {
        setLoading(false)
      }
    }
    fetchStats()
  }, [])

  if (loading) return <LoadingSpinner />
  if (error) return <div className="rounded-lg bg-red-50 p-4 text-sm text-red-700">{error}</div>
  if (!stats) return null

  const maxMonthlyRevenue = Math.max(...(stats.revenueByMonth?.map(m => m.revenue) ?? [0]), 1)

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Analytics</h2>
        <p className="mt-1 text-sm text-gray-500">Overview of your business performance</p>
      </div>

      {/* KPI Cards */}
      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          label="Total Bookings"
          value={String(stats.totalBookings)}
          icon={CalendarCheck}
          iconBg="bg-blue-100"
          iconColor="text-blue-600"
        />
        <KpiCard
          label="Total Revenue"
          value={`${stats.totalRevenue.toFixed(0)} ден`}
          icon={DollarSign}
          iconBg="bg-green-100"
          iconColor="text-green-600"
        />
        <KpiCard
          label="Avg Rating"
          value={stats.totalReviews > 0 ? `${stats.avgRating.toFixed(1)} ★` : 'No reviews'}
          subtext={stats.totalReviews > 0 ? `from ${stats.totalReviews} review${stats.totalReviews !== 1 ? 's' : ''}` : undefined}
          icon={Star}
          iconBg="bg-yellow-100"
          iconColor="text-yellow-600"
        />
        <KpiCard
          label="Upcoming"
          value={String(stats.upcomingBookings)}
          subtext="scheduled bookings"
          icon={ArrowUpRight}
          iconBg="bg-purple-100"
          iconColor="text-purple-600"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Bookings by Status */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <h3 className="mb-4 flex items-center gap-2 text-sm font-semibold text-gray-900">
            <BarChart3 className="h-4 w-4 text-gray-400" />
            Bookings by Status
          </h3>
          {stats.bookingsByStatus.length === 0 ? (
            <p className="text-sm text-gray-400">No booking data yet</p>
          ) : (
            <div className="space-y-3">
              {stats.bookingsByStatus.map(item => {
                const config = STATUS_COLORS[item.status] ?? { bg: 'bg-gray-100', text: 'text-gray-800', icon: Clock }
                const percentage = stats.totalBookings > 0 ? (item.count / stats.totalBookings) * 100 : 0
                return (
                  <div key={item.status}>
                    <div className="mb-1 flex items-center justify-between">
                      <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${config.bg} ${config.text}`}>
                        {item.status}
                      </span>
                      <span className="text-sm font-medium text-gray-700">{item.count}</span>
                    </div>
                    <div className="h-2 w-full rounded-full bg-gray-100">
                      <div
                        className={`h-2 rounded-full ${config.bg.replace('100', '400')}`}
                        style={{ width: `${percentage}%`, backgroundColor: getBarColor(item.status) }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          )}

          {/* Summary */}
          <div className="mt-6 flex items-center justify-between border-t border-gray-100 pt-4">
            <span className="text-xs text-gray-500">Confirmed bookings</span>
            <span className="text-sm font-bold text-gray-900">{stats.confirmedBookings}</span>
          </div>
        </div>

        {/* Monthly Revenue Chart */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <h3 className="mb-4 flex items-center gap-2 text-sm font-semibold text-gray-900">
            <TrendingUp className="h-4 w-4 text-gray-400" />
            Monthly Revenue
          </h3>
          {(!stats.revenueByMonth || stats.revenueByMonth.length === 0) ? (
            <p className="text-sm text-gray-400">No revenue data yet</p>
          ) : (
            <div className="space-y-2">
              {stats.revenueByMonth.slice(-6).map(item => {
                const barWidth = (item.revenue / maxMonthlyRevenue) * 100
                return (
                  <div key={item.month} className="flex items-center gap-3">
                    <span className="w-20 flex-shrink-0 text-xs text-gray-500">
                      {formatMonthLabel(item.month)}
                    </span>
                    <div className="flex-1">
                      <div className="h-6 w-full rounded bg-gray-50">
                        <div
                          className="flex h-6 items-center rounded bg-emerald-500 px-2"
                          style={{ width: `${Math.max(barWidth, 5)}%` }}
                        >
                          {barWidth > 25 && (
                            <span className="text-xs font-medium text-white">
                              {item.revenue.toFixed(0)} ден
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                    {barWidth <= 25 && (
                      <span className="text-xs font-medium text-gray-600">
                        {item.revenue.toFixed(0)} ден
                      </span>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function KpiCard({
  label,
  value,
  subtext,
  icon: Icon,
  iconBg,
  iconColor,
}: {
  label: string
  value: string
  subtext?: string
  icon: typeof Clock
  iconBg: string
  iconColor: string
}) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wide text-gray-500">{label}</p>
          <p className="mt-2 text-2xl font-bold text-gray-900">{value}</p>
          {subtext && <p className="mt-0.5 text-xs text-gray-400">{subtext}</p>}
        </div>
        <div className={`rounded-lg p-2.5 ${iconBg}`}>
          <Icon className={`h-5 w-5 ${iconColor}`} />
        </div>
      </div>
    </div>
  )
}

function getBarColor(status: string): string {
  switch (status) {
    case 'pending': return '#f59e0b'
    case 'confirmed': return '#3b82f6'
    case 'completed': return '#22c55e'
    case 'cancelled': return '#ef4444'
    default: return '#9ca3af'
  }
}

function formatMonthLabel(month: string): string {
  // Expects "YYYY-MM" or similar
  try {
    const [y, m] = month.split('-')
    const dt = new Date(Number(y), Number(m) - 1)
    return dt.toLocaleDateString('en-US', { month: 'short', year: '2-digit' })
  } catch {
    return month
  }
}

export default PartnerAnalyticsPage

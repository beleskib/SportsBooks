import { useEffect, useState } from 'react'
import {
  DollarSign,
  TrendingUp,
  Calendar,
  ArrowUpRight,
  ArrowDownRight,
  CreditCard,
  ExternalLink,
} from 'lucide-react'
import { paymentApi, type PaymentWithBooking } from '@/api/payments'
import { stripeConnectApi, type StripeAccountStatusResponse } from '@/api/stripeConnect'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { useAuth } from '@/context/AuthContext'
import { Link } from 'react-router-dom'

export function EarningsPage() {
  const { isAdmin } = useAuth()
  const [payments, setPayments] = useState<PaymentWithBooking[]>([])
  const [stripeStatus, setStripeStatus] = useState<StripeAccountStatusResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [paymentsRes, statusRes] = await Promise.all([
          paymentApi.getMyPayments(),
          stripeConnectApi.getStatus(),
        ])
        setPayments(paymentsRes.data)
        setStripeStatus(statusRes.data)
      } catch (err: unknown) {
        setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load earnings')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  if (loading) return <LoadingSpinner />
  if (error) return <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{error}</div>

  const completedPayments = payments.filter((p) => p.status === 'completed')
  const totalRevenue = completedPayments.reduce((sum, p) => sum + p.amount, 0)
  const totalPlatformFees = completedPayments.reduce((sum, p) => sum + (p.platformFeeAmount ?? 0), 0)
  const totalPayout = totalRevenue - totalPlatformFees

  // Monthly breakdown
  const monthlyMap = new Map<string, { revenue: number; fees: number; count: number }>()
  for (const p of completedPayments) {
    const date = new Date(p.paidAt ?? p.createdAt)
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    const existing = monthlyMap.get(key) ?? { revenue: 0, fees: 0, count: 0 }
    monthlyMap.set(key, {
      revenue: existing.revenue + p.amount,
      fees: existing.fees + (p.platformFeeAmount ?? 0),
      count: existing.count + 1,
    })
  }
  const monthlyData = Array.from(monthlyMap.entries())
    .sort((a, b) => b[0].localeCompare(a[0]))

  const isConnected = stripeStatus?.onboardingStatus === 'complete' && stripeStatus?.payoutsEnabled

  const handleOpenDashboard = async () => {
    try {
      const res = await stripeConnectApi.getDashboardLink()
      window.open(res.data.dashboardUrl, '_blank')
    } catch {
      // Silently fail — user can retry
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">{isAdmin ? 'Platform Revenue' : 'Earnings'}</h2>
        {!isAdmin && isConnected && (
          <button
            onClick={handleOpenDashboard}
            className="flex items-center gap-2 rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-800"
          >
            <ExternalLink className="h-4 w-4" />
            Stripe Dashboard
          </button>
        )}
      </div>

      {/* Stripe Connection Banner — partners only */}
      {!isAdmin && !isConnected && (
        <div className="mb-6 flex items-center justify-between rounded-lg bg-amber-50 p-4">
          <div className="flex items-center gap-3">
            <CreditCard className="h-5 w-5 text-amber-600" />
            <div>
              <p className="text-sm font-medium text-amber-800">Stripe not connected</p>
              <p className="text-sm text-amber-700">Set up payouts to start receiving money from bookings.</p>
            </div>
          </div>
          <Link
            to="/stripe"
            className="rounded-lg bg-amber-600 px-4 py-2 text-sm font-medium text-white hover:bg-amber-700"
          >
            Connect Now
          </Link>
        </div>
      )}

      {/* Revenue Cards */}
      <div className="mb-8 grid grid-cols-1 gap-6 sm:grid-cols-3">
        <div className="rounded-lg bg-white p-6 shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">{isAdmin ? 'Total Platform Revenue' : 'Total Revenue'}</p>
              <p className="mt-1 text-3xl font-bold text-gray-900">${totalRevenue.toFixed(2)}</p>
            </div>
            <div className="rounded-lg bg-green-100 p-3">
              <DollarSign className="h-6 w-6 text-green-600" />
            </div>
          </div>
          <p className="mt-2 text-xs text-gray-500">
            From {completedPayments.length} completed booking{completedPayments.length !== 1 ? 's' : ''}
          </p>
        </div>

        <div className="rounded-lg bg-white p-6 shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">{isAdmin ? 'Platform Fees Collected (10%)' : 'Platform Fees (10%)'}</p>
              <p className="mt-1 text-3xl font-bold text-red-600">-${totalPlatformFees.toFixed(2)}</p>
            </div>
            <div className="rounded-lg bg-red-100 p-3">
              <ArrowDownRight className="h-6 w-6 text-red-600" />
            </div>
          </div>
          <p className="mt-2 text-xs text-gray-500">SportsBooks commission</p>
        </div>

        <div className="rounded-lg bg-white p-6 shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500">{isAdmin ? 'Partner Payouts' : 'Your Payout'}</p>
              <p className="mt-1 text-3xl font-bold text-blue-600">${totalPayout.toFixed(2)}</p>
            </div>
            <div className="rounded-lg bg-blue-100 p-3">
              <TrendingUp className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <p className="mt-2 text-xs text-gray-500">Transferred to your Stripe account</p>
        </div>
      </div>

      {/* Monthly Breakdown */}
      {monthlyData.length > 0 && (
        <div className="mb-8 rounded-lg bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-gray-900">Monthly Breakdown</h3>
          <table className="min-w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="pb-2 text-left text-xs font-medium uppercase text-gray-500">Month</th>
                <th className="pb-2 text-right text-xs font-medium uppercase text-gray-500">Bookings</th>
                <th className="pb-2 text-right text-xs font-medium uppercase text-gray-500">Revenue</th>
                <th className="pb-2 text-right text-xs font-medium uppercase text-gray-500">Fees</th>
                <th className="pb-2 text-right text-xs font-medium uppercase text-gray-500">Payout</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {monthlyData.map(([month, data]) => (
                <tr key={month}>
                  <td className="py-3 text-sm font-medium text-gray-900">
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-gray-400" />
                      {formatMonth(month)}
                    </div>
                  </td>
                  <td className="py-3 text-right text-sm text-gray-600">{data.count}</td>
                  <td className="py-3 text-right text-sm text-gray-900">${data.revenue.toFixed(2)}</td>
                  <td className="py-3 text-right text-sm text-red-600">-${data.fees.toFixed(2)}</td>
                  <td className="py-3 text-right text-sm font-semibold text-green-600">
                    ${(data.revenue - data.fees).toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Recent Transactions */}
      <div className="rounded-lg bg-white p-6 shadow">
        <h3 className="mb-4 text-lg font-semibold text-gray-900">Recent Transactions</h3>
        {payments.length === 0 ? (
          <p className="text-sm text-gray-500">No transactions yet. Earnings will appear here once players book your venues or coaching sessions.</p>
        ) : (
          <div className="space-y-3">
            {payments.slice(0, 20).map((payment) => (
              <div
                key={payment.id}
                className="flex items-center justify-between rounded-md border border-gray-100 p-3"
              >
                <div className="flex items-center gap-3">
                  <div
                    className={`rounded-full p-2 ${
                      payment.status === 'completed'
                        ? 'bg-green-100'
                        : payment.status === 'pending'
                          ? 'bg-yellow-100'
                          : payment.status === 'refunded'
                            ? 'bg-purple-100'
                            : 'bg-red-100'
                    }`}
                  >
                    {payment.status === 'completed' ? (
                      <ArrowUpRight className="h-4 w-4 text-green-600" />
                    ) : (
                      <ArrowDownRight className="h-4 w-4 text-red-600" />
                    )}
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {payment.venueName ?? payment.coachName ?? `Booking #${payment.bookingId}`}
                    </p>
                    <p className="text-xs text-gray-500">
                      {payment.slotDate && payment.startTime
                        ? `${payment.slotDate} at ${payment.startTime}`
                        : new Date(payment.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold text-gray-900">${payment.amount.toFixed(2)}</p>
                  <PaymentStatusBadge status={payment.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function PaymentStatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    completed: 'bg-green-100 text-green-700',
    pending: 'bg-yellow-100 text-yellow-700',
    failed: 'bg-red-100 text-red-700',
    refunded: 'bg-purple-100 text-purple-700',
  }

  return (
    <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${styles[status] ?? 'bg-gray-100 text-gray-700'}`}>
      {status}
    </span>
  )
}

function formatMonth(key: string): string {
  const [year, month] = key.split('-')
  const date = new Date(Number(year), Number(month) - 1)
  return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
}

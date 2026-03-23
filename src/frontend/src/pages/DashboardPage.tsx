import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  MapPin,
  Users,
  TrendingUp,
  Activity,
  CalendarCheck,
  Star,
  CreditCard,
  Plus,
} from 'lucide-react'
import { venueApi } from '@/api/venues'
import { coachApi } from '@/api/coaches'
import { bookingApi, type BookingCount } from '@/api/bookings'
import { stripeConnectApi, type StripeAccountStatusResponse } from '@/api/stripeConnect'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { useAuth } from '@/context/AuthContext'
import { SPORT_TYPE_LABELS, PartnerType } from '@/types'
import type { Venue, Coach, SportType } from '@/types'

// ─── Admin Dashboard ─────────────────────────────────────────────────
function AdminDashboard() {
  const [venues, setVenues] = useState<Venue[]>([])
  const [coaches, setCoaches] = useState<Coach[]>([])
  const [venueBookings, setVenueBookings] = useState<Map<number, BookingCount>>(new Map())
  const [coachBookings, setCoachBookings] = useState<Map<number, BookingCount>>(new Map())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [venuesRes, coachesRes, countsRes] = await Promise.all([
          venueApi.getAll(),
          coachApi.getAll(),
          bookingApi.getCounts(),
        ])
        const venuesList: Venue[] = venuesRes.data
        const coachesList: Coach[] = coachesRes.data
        const counts = countsRes.data

        const vMap = new Map<number, BookingCount>()
        let totalVB = 0
        for (const c of counts.venues) {
          vMap.set(c.entityId, c)
          totalVB += c.totalBookings
        }
        const cMap = new Map<number, BookingCount>()
        let totalCB = 0
        for (const c of counts.coaches) {
          cMap.set(c.entityId, c)
          totalCB += c.totalBookings
        }

        setVenues(venuesList)
        setCoaches(coachesList)
        setVenueBookings(vMap)
        setCoachBookings(cMap)
      } catch (err: unknown) {
        setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load stats')
      } finally {
        setLoading(false)
      }
    }
    fetchStats()
  }, [])

  if (loading) return <LoadingSpinner />
  if (error) return <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{error}</div>

  const totalVenueBookings = Array.from(venueBookings.values()).reduce((s, c) => s + c.totalBookings, 0)
  const totalCoachBookings = Array.from(coachBookings.values()).reduce((s, c) => s + c.totalBookings, 0)

  const cards = [
    { label: 'Total Venues', value: venues.length, icon: MapPin, color: 'bg-blue-500', link: '/venues' },
    { label: 'Active Venues', value: venues.filter((v) => v.isActive).length, icon: Activity, color: 'bg-green-500', link: '/venues' },
    { label: 'Total Coaches', value: coaches.length, icon: Users, color: 'bg-purple-500', link: '/coaches' },
    { label: 'Active Coaches', value: coaches.filter((c) => c.isActive).length, icon: TrendingUp, color: 'bg-orange-500', link: '/coaches' },
    { label: 'Venue Bookings', value: totalVenueBookings, icon: CalendarCheck, color: 'bg-indigo-500', link: '/venues' },
    { label: 'Coach Bookings', value: totalCoachBookings, icon: Star, color: 'bg-pink-500', link: '/coaches' },
  ]

  const topVenues = [...venues]
    .map((v) => ({ ...v, bookings: venueBookings.get(v.id)?.totalBookings ?? 0 }))
    .sort((a, b) => b.bookings - a.bookings)
    .slice(0, 5)

  const topCoaches = [...coaches]
    .map((c) => ({ ...c, bookings: coachBookings.get(c.id)?.totalBookings ?? 0 }))
    .sort((a, b) => b.bookings - a.bookings)
    .slice(0, 5)

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900">Platform Overview</h2>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map((card) => (
          <Link
            key={card.label}
            to={card.link}
            className="rounded-lg bg-white p-6 shadow transition-shadow hover:shadow-md"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-500">{card.label}</p>
                <p className="mt-1 text-3xl font-bold text-gray-900">{card.value}</p>
              </div>
              <div className={`rounded-lg ${card.color} p-3`}>
                <card.icon className="h-6 w-6 text-white" />
              </div>
            </div>
          </Link>
        ))}
      </div>

      <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-lg bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-gray-900">Most Booked Venues</h3>
          {topVenues.length === 0 ? (
            <p className="text-sm text-gray-500">No booking data yet</p>
          ) : (
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="pb-2 text-left text-xs font-medium uppercase text-gray-500">Venue</th>
                  <th className="pb-2 text-left text-xs font-medium uppercase text-gray-500">Sport</th>
                  <th className="pb-2 text-right text-xs font-medium uppercase text-gray-500">Bookings</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {topVenues.map((v) => (
                  <tr key={v.id}>
                    <td className="py-2 text-sm font-medium text-gray-900">
                      <Link to={`/venues/${v.id}/edit`} className="hover:text-blue-600">{v.name}</Link>
                    </td>
                    <td className="py-2 text-sm text-gray-600">
                      {SPORT_TYPE_LABELS[v.sportType as SportType] || v.sportType}
                    </td>
                    <td className="py-2 text-right text-sm font-semibold text-gray-900">{v.bookings}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="rounded-lg bg-white p-6 shadow">
          <h3 className="mb-4 text-lg font-semibold text-gray-900">Most Booked Coaches</h3>
          {topCoaches.length === 0 ? (
            <p className="text-sm text-gray-500">No booking data yet</p>
          ) : (
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="pb-2 text-left text-xs font-medium uppercase text-gray-500">Coach</th>
                  <th className="pb-2 text-left text-xs font-medium uppercase text-gray-500">Sport</th>
                  <th className="pb-2 text-right text-xs font-medium uppercase text-gray-500">Bookings</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {topCoaches.map((c) => (
                  <tr key={c.id}>
                    <td className="py-2 text-sm font-medium text-gray-900">
                      <Link to={`/coaches/${c.id}/edit`} className="hover:text-blue-600">{c.name}</Link>
                    </td>
                    <td className="py-2 text-sm text-gray-600">
                      {SPORT_TYPE_LABELS[c.sportType as SportType] || c.sportType}
                    </td>
                    <td className="py-2 text-right text-sm font-semibold text-gray-900">{c.bookings}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}

// ─── Partner Dashboard ───────────────────────────────────────────────
function PartnerDashboard() {
  const { partnerType } = useAuth()
  const [venues, setVenues] = useState<Venue[]>([])
  const [coach, setCoach] = useState<Coach | null>(null)
  const [stripeStatus, setStripeStatus] = useState<StripeAccountStatusResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const promises: Promise<unknown>[] = [stripeConnectApi.getStatus()]

        if (partnerType === PartnerType.VENUE_OWNER) {
          promises.push(venueApi.getMine())
        } else if (partnerType === PartnerType.COACH) {
          promises.push(coachApi.getMine())
        }

        const results = await Promise.all(promises)
        setStripeStatus((results[0] as { data: StripeAccountStatusResponse }).data)

        if (partnerType === PartnerType.VENUE_OWNER && results[1]) {
          setVenues((results[1] as { data: Venue[] }).data)
        } else if (partnerType === PartnerType.COACH && results[1]) {
          setCoach((results[1] as { data: Coach | null }).data)
        }
      } catch (err: unknown) {
        setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load dashboard')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [partnerType])

  if (loading) return <LoadingSpinner />
  if (error) return <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{error}</div>

  const isStripeConnected = stripeStatus?.onboardingStatus === 'complete' && stripeStatus.payoutsEnabled

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900">Dashboard</h2>

      {/* Stripe connection banner */}
      {!isStripeConnected && (
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

      {/* Stats for venue owner */}
      {partnerType === PartnerType.VENUE_OWNER && (
        <>
          <div className="mb-6 grid grid-cols-1 gap-6 sm:grid-cols-3">
            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">My Venues</p>
                  <p className="mt-1 text-3xl font-bold text-gray-900">{venues.length}</p>
                </div>
                <div className="rounded-lg bg-blue-500 p-3">
                  <MapPin className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>
            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">Active Venues</p>
                  <p className="mt-1 text-3xl font-bold text-gray-900">{venues.filter((v) => v.isActive).length}</p>
                </div>
                <div className="rounded-lg bg-green-500 p-3">
                  <Activity className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>
            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">Stripe Status</p>
                  <p className="mt-1 text-lg font-bold text-gray-900">
                    {isStripeConnected ? 'Connected' : 'Not Connected'}
                  </p>
                </div>
                <div className={`rounded-lg p-3 ${isStripeConnected ? 'bg-green-500' : 'bg-gray-400'}`}>
                  <CreditCard className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>
          </div>

          {venues.length === 0 ? (
            <div className="rounded-lg bg-white p-8 text-center shadow">
              <MapPin className="mx-auto h-12 w-12 text-gray-300" />
              <h3 className="mt-4 text-lg font-medium text-gray-900">No venues yet</h3>
              <p className="mt-2 text-sm text-gray-500">Add your first venue to start receiving bookings.</p>
              <Link
                to="/venues/new"
                className="mt-4 inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                <Plus className="h-4 w-4" />
                Add Venue
              </Link>
            </div>
          ) : (
            <div className="rounded-lg bg-white p-6 shadow">
              <h3 className="mb-4 text-lg font-semibold text-gray-900">My Venues</h3>
              <div className="space-y-3">
                {venues.map((v) => (
                  <Link
                    key={v.id}
                    to={`/venues/${v.id}/edit`}
                    className="flex items-center justify-between rounded-md border border-gray-100 p-3 hover:bg-gray-50"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-900">{v.name}</p>
                      <p className="text-xs text-gray-500">
                        {SPORT_TYPE_LABELS[v.sportType as SportType] || v.sportType} &middot; ${v.pricePerHour}/hr
                      </p>
                    </div>
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${v.isActive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                      {v.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {/* Stats for coach */}
      {partnerType === PartnerType.COACH && (
        <>
          <div className="mb-6 grid grid-cols-1 gap-6 sm:grid-cols-2">
            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">Profile Status</p>
                  <p className="mt-1 text-lg font-bold text-gray-900">
                    {coach ? (coach.isActive ? 'Active' : 'Inactive') : 'Not Created'}
                  </p>
                </div>
                <div className={`rounded-lg p-3 ${coach?.isActive ? 'bg-green-500' : 'bg-gray-400'}`}>
                  <Users className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>
            <div className="rounded-lg bg-white p-6 shadow">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">Stripe Status</p>
                  <p className="mt-1 text-lg font-bold text-gray-900">
                    {isStripeConnected ? 'Connected' : 'Not Connected'}
                  </p>
                </div>
                <div className={`rounded-lg p-3 ${isStripeConnected ? 'bg-green-500' : 'bg-gray-400'}`}>
                  <CreditCard className="h-6 w-6 text-white" />
                </div>
              </div>
            </div>
          </div>

          {!coach ? (
            <div className="rounded-lg bg-white p-8 text-center shadow">
              <Users className="mx-auto h-12 w-12 text-gray-300" />
              <h3 className="mt-4 text-lg font-medium text-gray-900">No coach profile yet</h3>
              <p className="mt-2 text-sm text-gray-500">Create your coach profile to start receiving bookings.</p>
              <Link
                to="/coaches/new"
                className="mt-4 inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                <Plus className="h-4 w-4" />
                Create Profile
              </Link>
            </div>
          ) : (
            <div className="rounded-lg bg-white p-6 shadow">
              <h3 className="mb-4 text-lg font-semibold text-gray-900">My Coach Profile</h3>
              <Link
                to={`/coaches/${coach.id}/edit`}
                className="flex items-center justify-between rounded-md border border-gray-100 p-4 hover:bg-gray-50"
              >
                <div>
                  <p className="font-medium text-gray-900">{coach.name}</p>
                  <p className="text-sm text-gray-500">
                    {SPORT_TYPE_LABELS[coach.sportType as SportType] || coach.sportType}
                    {coach.specialization && ` \u00b7 ${coach.specialization}`}
                    {' \u00b7 '}${coach.pricePerHour}/hr
                  </p>
                </div>
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${coach.isActive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                  {coach.isActive ? 'Active' : 'Inactive'}
                </span>
              </Link>
            </div>
          )}
        </>
      )}
    </div>
  )
}

// ─── Main export ─────────────────────────────────────────────────────
export function DashboardPage() {
  const { isAdmin } = useAuth()
  return isAdmin ? <AdminDashboard /> : <PartnerDashboard />
}

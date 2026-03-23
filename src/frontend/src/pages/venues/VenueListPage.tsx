import { useEffect, useState } from 'react'
import { VenueList } from '@/components/venues/VenueList'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { venueApi } from '@/api/venues'
import { bookingApi, type BookingCount } from '@/api/bookings'
import { useAuth } from '@/context/AuthContext'
import type { Venue } from '@/types'

export function VenueListPage() {
  const { isAdmin } = useAuth()
  const [venues, setVenues] = useState<Venue[]>([])
  const [bookingCounts, setBookingCounts] = useState<Map<number, BookingCount>>(new Map())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [deleting, setDeleting] = useState<number | null>(null)

  const fetchData = async () => {
    try {
      setLoading(true)
      setError(null)
      const [venuesRes, countsRes] = await Promise.all([
        isAdmin ? venueApi.getAll() : venueApi.getMine(),
        bookingApi.getCounts(),
      ])
      setVenues(venuesRes.data)
      const map = new Map<number, BookingCount>()
      for (const c of countsRes.data.venues) {
        map.set(c.entityId, c)
      }
      setBookingCounts(map)
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load venues')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [isAdmin])

  const handleDelete = async (id: number) => {
    try {
      setDeleting(id)
      await venueApi.delete(id)
      await fetchData()
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message || 'Failed to delete venue')
    } finally {
      setDeleting(null)
    }
  }

  if (loading) return <LoadingSpinner />

  if (error) {
    return (
      <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">
        {error}
        <button onClick={fetchData} className="ml-2 font-medium underline">Retry</button>
      </div>
    )
  }

  return <VenueList venues={venues} onDelete={handleDelete} deleting={deleting} bookingCounts={bookingCounts} showOwner={isAdmin} />
}

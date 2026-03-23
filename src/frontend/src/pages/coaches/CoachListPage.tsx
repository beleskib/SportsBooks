import { useEffect, useState } from 'react'
import { CoachList } from '@/components/coaches/CoachList'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { coachApi } from '@/api/coaches'
import { bookingApi, type BookingCount } from '@/api/bookings'
import { useAuth } from '@/context/AuthContext'
import type { Coach } from '@/types'

export function CoachListPage() {
  const { isAdmin } = useAuth()
  const [coaches, setCoaches] = useState<Coach[]>([])
  const [bookingCounts, setBookingCounts] = useState<Map<number, BookingCount>>(new Map())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [deleting, setDeleting] = useState<number | null>(null)

  const fetchData = async () => {
    try {
      setLoading(true)
      setError(null)
      const [coachesRes, countsRes] = await Promise.all([
        isAdmin ? coachApi.getAll() : coachApi.getMine().then((res) => ({ data: res.data ? [res.data] : [] })),
        bookingApi.getCounts(),
      ])
      setCoaches(coachesRes.data as Coach[])
      const map = new Map<number, BookingCount>()
      for (const c of countsRes.data.coaches) {
        map.set(c.entityId, c)
      }
      setBookingCounts(map)
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load coaches')
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
      await coachApi.delete(id)
      await fetchData()
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message || 'Failed to delete coach')
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

  return <CoachList coaches={coaches} onDelete={handleDelete} deleting={deleting} bookingCounts={bookingCounts} showOwner={isAdmin} />
}

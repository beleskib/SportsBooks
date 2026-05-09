// Landing page for the partner-approval email link.
// Route: /partner/reservations/:id
// Already login-gated by the parent ProtectedRoute in App.tsx.
//
// Shows the booking details (player, slot, price) and Approve / Decline buttons.
// On success, swaps the action area for a confirmation pill.

import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { bookingApi, type ReservationDetail } from '@/api/bookings'

type ResolutionState =
  | { kind: 'idle' }
  | { kind: 'pending' }
  | { kind: 'approved' }
  | { kind: 'declined' }
  | { kind: 'error'; message: string }

function formatSlot(reservation: ReservationDetail): string {
  const slot = reservation.timeSlot
  if (!slot) return 'Date TBC'
  const [y, m, d] = slot.slotDate.split('-').map(Number)
  const dt = new Date(Date.UTC(y, m - 1, d))
  const datePart = dt.toLocaleDateString('en-GB', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC',
  })
  return `${datePart} · ${slot.startTime?.substring(0, 5) ?? ''}–${slot.endTime?.substring(0, 5) ?? ''}`
}

export function PartnerReservationDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [reservation, setReservation] = useState<ReservationDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [resolution, setResolution] = useState<ResolutionState>({ kind: 'idle' })

  useEffect(() => {
    if (!id) return
    let cancelled = false
    bookingApi
      .getById(Number(id))
      .then((res) => {
        if (cancelled) return
        setReservation(res.data ?? null)
        setLoading(false)
      })
      .catch((err) => {
        if (cancelled) return
        setLoadError(err?.error?.message ?? 'Could not load reservation')
        setLoading(false)
      })
    return () => { cancelled = true }
  }, [id])

  const respond = async (action: 'approve' | 'decline') => {
    if (!id) return
    setResolution({ kind: 'pending' })
    try {
      const fn = action === 'approve' ? bookingApi.approve : bookingApi.decline
      await fn(Number(id))
      setResolution({ kind: action === 'approve' ? 'approved' : 'declined' })
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Action failed'
      setResolution({ kind: 'error', message })
    }
  }

  if (loading) return <div className="p-8 text-gray-600">Loading reservation…</div>
  if (loadError) return <div className="p-8 text-red-600">{loadError}</div>
  if (!reservation) return <div className="p-8 text-gray-600">Not found.</div>

  const entityName = reservation.venue?.name ?? reservation.coach?.name ?? 'Booking'
  const slotWhen = formatSlot(reservation)
  const isAlreadyResolved = reservation.status !== 'pending' && resolution.kind === 'idle'

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-semibold text-gray-900">Reservation #{reservation.id}</h1>
      <p className="mt-1 text-sm text-gray-500">
        Status: <span className="font-medium text-gray-900">{reservation.status}</span>
      </p>

      <div className="mt-6 rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
        <div className="text-lg font-semibold text-gray-900">{entityName}</div>
        <div className="mt-1 text-sm text-gray-600">{slotWhen}</div>
        {reservation.venue?.address && (
          <div className="mt-1 text-sm text-gray-500">{reservation.venue.address}</div>
        )}

        <div className="mt-5 flex items-center justify-between border-t border-gray-100 pt-4">
          <div>
            <div className="text-xs uppercase tracking-wide text-gray-400">Player</div>
            <div className="text-sm font-medium text-gray-900">
              {reservation.playerName ?? reservation.playerEmail ?? `User #${reservation.playerId}`}
            </div>
          </div>
          <div className="text-right">
            <div className="text-xs uppercase tracking-wide text-gray-400">Total</div>
            <div className="text-sm font-medium text-gray-900">{reservation.totalPrice.toFixed(2)} ден</div>
          </div>
        </div>

        {reservation.notes && (
          <div className="mt-4 rounded-lg bg-gray-50 p-3 text-sm text-gray-700">
            <span className="font-medium text-gray-500">Note from player:</span> {reservation.notes}
          </div>
        )}

        <div className="mt-6">
          {isAlreadyResolved && (
            <div className="rounded-lg bg-gray-100 px-4 py-3 text-sm text-gray-700">
              This reservation has already been <span className="font-semibold">{reservation.status}</span>.
            </div>
          )}

          {!isAlreadyResolved && resolution.kind === 'idle' && (
            <div className="flex gap-3">
              <button
                onClick={() => respond('approve')}
                className="flex-1 rounded-lg bg-gray-900 px-4 py-2.5 text-sm font-semibold text-yellow-300 hover:bg-gray-800"
              >
                Approve
              </button>
              <button
                onClick={() => respond('decline')}
                className="flex-1 rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 hover:bg-gray-50"
              >
                Decline
              </button>
            </div>
          )}

          {resolution.kind === 'pending' && (
            <div className="text-sm text-gray-500">Submitting…</div>
          )}
          {resolution.kind === 'approved' && (
            <div className="rounded-lg bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
              Approved ✓ — the player has been notified and will receive a receipt by email.
            </div>
          )}
          {resolution.kind === 'declined' && (
            <div className="rounded-lg bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
              Declined — the player has been notified.
            </div>
          )}
          {resolution.kind === 'error' && (
            <div className="rounded-lg bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {resolution.message}
            </div>
          )}
        </div>
      </div>

      <Link to="/" className="mt-6 inline-block text-sm text-gray-500 hover:text-gray-700">
        ← Back to dashboard
      </Link>
    </div>
  )
}

export default PartnerReservationDetailPage

import { useEffect, useState } from 'react'
import { Calendar, Plus, Trash2, Clock, AlertCircle } from 'lucide-react'
import { venueApi } from '@/api/venues'
import { coachApi } from '@/api/coaches'
import { timeSlotApi } from '@/api/timeSlots'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { useAuth } from '@/context/AuthContext'
import { PartnerType } from '@/types'
import type { Venue, Coach, TimeSlot } from '@/types'

export function TimeSlotPage() {
  const { isAdmin, partnerType } = useAuth()

  const [venues, setVenues] = useState<Venue[]>([])
  const [coach, setCoach] = useState<Coach | null>(null)
  const [selectedEntityId, setSelectedEntityId] = useState<number | null>(null)
  const [entityType, setEntityType] = useState<'venue' | 'coach'>('venue')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [slots, setSlots] = useState<TimeSlot[]>([])
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [deletingSlot, setDeletingSlot] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Set default date range: today → 7 days from now
  useEffect(() => {
    const today = new Date()
    const nextWeek = new Date()
    nextWeek.setDate(today.getDate() + 7)
    setDateFrom(today.toISOString().split('T')[0])
    setDateTo(nextWeek.toISOString().split('T')[0])
  }, [])

  // Load venues or coach profile
  useEffect(() => {
    const fetchEntities = async () => {
      try {
        if (isAdmin || partnerType === PartnerType.VENUE_OWNER) {
          const res = isAdmin ? await venueApi.getAll() : await venueApi.getMine()
          setVenues(res.data)
          if (res.data.length > 0) {
            setSelectedEntityId(res.data[0].id)
            setEntityType('venue')
          }
        }
        if (isAdmin || partnerType === PartnerType.COACH) {
          const res = await coachApi.getMine()
          if (res.data) {
            setCoach(res.data)
            if (partnerType === PartnerType.COACH) {
              setSelectedEntityId(res.data.id)
              setEntityType('coach')
            }
          }
        }
      } catch (err: unknown) {
        setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load data')
      } finally {
        setLoading(false)
      }
    }
    fetchEntities()
  }, [isAdmin, partnerType])

  // Load slots when entity or date range changes
  useEffect(() => {
    if (!selectedEntityId || !dateFrom || !dateTo) return
    const fetchSlots = async () => {
      try {
        const res = entityType === 'venue'
          ? await timeSlotApi.getVenueSlots(selectedEntityId, dateFrom, dateTo)
          : await timeSlotApi.getCoachSlots(selectedEntityId, dateFrom, dateTo)
        setSlots(res.data)
      } catch {
        setSlots([])
      }
    }
    fetchSlots()
  }, [selectedEntityId, entityType, dateFrom, dateTo])

  const handleGenerate = async () => {
    if (!selectedEntityId || !dateFrom || !dateTo) return
    setGenerating(true)
    setError(null)
    setSuccessMsg(null)
    try {
      const data = entityType === 'venue'
        ? { venueId: selectedEntityId, dateFrom, dateTo }
        : { coachId: selectedEntityId, dateFrom, dateTo }
      const res = await timeSlotApi.generate(data)
      setSuccessMsg(`Generated ${res.data.length} time slots (09:00-22:00) for the selected date range.`)
      // Refresh slots
      const slotsRes = entityType === 'venue'
        ? await timeSlotApi.getVenueSlots(selectedEntityId, dateFrom, dateTo)
        : await timeSlotApi.getCoachSlots(selectedEntityId, dateFrom, dateTo)
      setSlots(slotsRes.data)
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message || 'Failed to generate time slots')
    } finally {
      setGenerating(false)
    }
  }

  const handleDeleteSlot = async (slotId: number) => {
    setDeletingSlot(slotId)
    try {
      await timeSlotApi.deleteSlot(slotId)
      setSlots((prev) => prev.filter((s) => s.id !== slotId))
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message || 'Failed to delete time slot')
    } finally {
      setDeletingSlot(null)
    }
  }

  if (loading) return <LoadingSpinner />

  // Group slots by date
  const slotsByDate = new Map<string, TimeSlot[]>()
  for (const slot of slots) {
    const existing = slotsByDate.get(slot.slotDate) ?? []
    existing.push(slot)
    slotsByDate.set(slot.slotDate, existing)
  }
  const sortedDates = Array.from(slotsByDate.keys()).sort()

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900">Time Slots</h2>

      {error && (
        <div className="mb-4 flex items-center gap-2 rounded-md bg-red-50 p-4 text-sm text-red-700">
          <AlertCircle className="h-4 w-4" />
          {error}
          <button onClick={() => setError(null)} className="ml-auto font-medium underline">Dismiss</button>
        </div>
      )}
      {successMsg && (
        <div className="mb-4 rounded-md bg-green-50 p-4 text-sm text-green-700">
          {successMsg}
          <button onClick={() => setSuccessMsg(null)} className="ml-2 font-medium underline">Dismiss</button>
        </div>
      )}

      {/* Controls */}
      <div className="mb-6 rounded-lg bg-white p-6 shadow">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Entity selector */}
          {partnerType === PartnerType.VENUE_OWNER && venues.length > 0 && (
            <div>
              <label className="block text-sm font-medium text-gray-700">Venue</label>
              <select
                value={selectedEntityId ?? ''}
                onChange={(e) => {
                  setSelectedEntityId(Number(e.target.value))
                  setEntityType('venue')
                }}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                {venues.map((v) => (
                  <option key={v.id} value={v.id}>{v.name}</option>
                ))}
              </select>
            </div>
          )}

          {partnerType === PartnerType.COACH && coach && (
            <div>
              <label className="block text-sm font-medium text-gray-700">Coach Profile</label>
              <input
                type="text"
                readOnly
                value={coach.name}
                className="mt-1 block w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-gray-600"
              />
            </div>
          )}

          {isAdmin && (
            <div>
              <label className="block text-sm font-medium text-gray-700">Entity</label>
              <select
                value={`${entityType}-${selectedEntityId}`}
                onChange={(e) => {
                  const [type, id] = e.target.value.split('-')
                  setEntityType(type as 'venue' | 'coach')
                  setSelectedEntityId(Number(id))
                }}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                {venues.map((v) => (
                  <option key={`venue-${v.id}`} value={`venue-${v.id}`}>Venue: {v.name}</option>
                ))}
                {coach && <option value={`coach-${coach.id}`}>Coach: {coach.name}</option>}
              </select>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700">Date From</label>
            <input
              type="date"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Date To</label>
            <input
              type="date"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
          <div className="flex items-end">
            <button
              onClick={handleGenerate}
              disabled={generating || !selectedEntityId || !dateFrom || !dateTo}
              className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
            >
              <Plus className="h-4 w-4" />
              {generating ? 'Generating...' : 'Generate Slots'}
            </button>
          </div>
        </div>
        <p className="mt-3 text-xs text-gray-500">
          Generates hourly time slots from 09:00 to 22:00 for each day in the range. Existing slots are not duplicated.
        </p>
      </div>

      {/* Slot display */}
      {slots.length === 0 ? (
        <div className="rounded-lg bg-white p-8 text-center text-gray-500 shadow">
          <Calendar className="mx-auto h-12 w-12 text-gray-300" />
          <p className="mt-4">No time slots found for the selected range. Generate slots to get started.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {sortedDates.map((date) => {
            const daySlots = slotsByDate.get(date) ?? []
            const dateObj = new Date(date + 'T00:00:00')
            const dayLabel = dateObj.toLocaleDateString('en-US', {
              weekday: 'long',
              month: 'short',
              day: 'numeric',
              year: 'numeric',
            })
            return (
              <div key={date} className="rounded-lg bg-white p-6 shadow">
                <h3 className="mb-4 flex items-center gap-2 text-sm font-semibold text-gray-900">
                  <Calendar className="h-4 w-4 text-gray-400" />
                  {dayLabel}
                  <span className="ml-auto text-xs font-normal text-gray-500">
                    {daySlots.length} slot{daySlots.length !== 1 ? 's' : ''}
                  </span>
                </h3>
                <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
                  {daySlots
                    .sort((a, b) => a.startTime.localeCompare(b.startTime))
                    .map((slot) => (
                      <div
                        key={slot.id}
                        className={`flex items-center justify-between rounded-md border p-2 text-sm ${
                          slot.isAvailable
                            ? 'border-green-200 bg-green-50'
                            : 'border-red-200 bg-red-50'
                        }`}
                      >
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3 text-gray-400" />
                          <span className="font-medium">
                            {slot.startTime.slice(0, 5)}
                          </span>
                        </div>
                        {slot.isAvailable ? (
                          <button
                            onClick={() => handleDeleteSlot(slot.id)}
                            disabled={deletingSlot === slot.id}
                            className="rounded p-0.5 text-gray-400 hover:bg-red-100 hover:text-red-600"
                            title="Delete slot"
                          >
                            <Trash2 className="h-3 w-3" />
                          </button>
                        ) : (
                          <span className="text-xs text-red-600">Booked</span>
                        )}
                      </div>
                    ))}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

import { useEffect, useMemo, useState, useCallback } from 'react'
import { Calendar, ChevronLeft, ChevronRight, Loader2, AlertCircle } from 'lucide-react'
import { venueApi } from '@/api/venues'
import { coachApi } from '@/api/coaches'
import { timeSlotApi } from '@/api/timeSlots'
import { useAuth } from '@/context/AuthContext'
import { PartnerType } from '@/types'
import type { Venue, Coach, TimeSlot } from '@/types'

// ============================================================
// v2-practical-ux: Weekly Calendar Grid for Partners
// One-screen view of the upcoming week with drag-to-block slots.
// Replaces the form-heavy generate/list flow on master.
// ============================================================

const HOURS = Array.from({ length: 14 }, (_, i) => 9 + i) // 9:00 .. 22:00
const DAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

function startOfWeek(d: Date): Date {
  const date = new Date(d)
  const day = date.getDay() // 0 = Sun
  const diff = day === 0 ? -6 : 1 - day // shift to Monday
  date.setDate(date.getDate() + diff)
  date.setHours(0, 0, 0, 0)
  return date
}

function isoDate(d: Date): string {
  return d.toISOString().split('T')[0]
}

function addDays(d: Date, n: number): Date {
  const next = new Date(d)
  next.setDate(d.getDate() + n)
  return next
}

type CellState = 'empty' | 'available' | 'booked'

interface CellInfo {
  date: string
  hour: number
  state: CellState
  slot: TimeSlot | null
}

export function WeeklyCalendarPage() {
  const { isAdmin, partnerType } = useAuth()

  const [venues, setVenues] = useState<Venue[]>([])
  const [coach, setCoach] = useState<Coach | null>(null)
  const [selectedEntityId, setSelectedEntityId] = useState<number | null>(null)
  const [entityType, setEntityType] = useState<'venue' | 'coach'>('venue')
  const [weekStart, setWeekStart] = useState<Date>(startOfWeek(new Date()))
  const [slots, setSlots] = useState<TimeSlot[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Drag state
  const [dragStart, setDragStart] = useState<{ date: string; hour: number } | null>(null)
  const [dragEnd, setDragEnd] = useState<{ date: string; hour: number } | null>(null)
  const [dragMode, setDragMode] = useState<'add' | 'remove' | null>(null)

  // ----- Load entities (venues for owner, coach profile for coach) -----
  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        if (isAdmin || partnerType === PartnerType.VENUE_OWNER) {
          const res = isAdmin ? await venueApi.getAll() : await venueApi.getMine()
          if (cancelled) return
          setVenues(res.data)
          if (res.data.length > 0) {
            setSelectedEntityId(res.data[0].id)
            setEntityType('venue')
          }
        }
        if (isAdmin || partnerType === PartnerType.COACH) {
          const res = await coachApi.getMine()
          if (cancelled) return
          if (res.data) {
            setCoach(res.data)
            if (partnerType === PartnerType.COACH) {
              setSelectedEntityId(res.data.id)
              setEntityType('coach')
            }
          }
        }
      } catch (err: unknown) {
        if (!cancelled) {
          setError((err as { error?: { message?: string } })?.error?.message ?? 'Failed to load')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [isAdmin, partnerType])

  // ----- Load slots when entity or week changes -----
  const loadSlots = useCallback(async () => {
    if (!selectedEntityId) return
    const from = isoDate(weekStart)
    const to = isoDate(addDays(weekStart, 6))
    try {
      const res =
        entityType === 'venue'
          ? await timeSlotApi.getVenueSlots(selectedEntityId, from, to)
          : await timeSlotApi.getCoachSlots(selectedEntityId, from, to)
      setSlots(res.data)
    } catch {
      setSlots([])
    }
  }, [selectedEntityId, entityType, weekStart])

  useEffect(() => {
    loadSlots()
  }, [loadSlots])

  // ----- Build the day x hour grid -----
  const grid = useMemo<CellInfo[][]>(() => {
    const slotMap = new Map<string, TimeSlot>()
    for (const s of slots) {
      const hour = parseInt(s.startTime.split(':')[0], 10)
      slotMap.set(`${s.slotDate}|${hour}`, s)
    }
    return DAY_LABELS.map((_, dIdx) => {
      const date = isoDate(addDays(weekStart, dIdx))
      return HOURS.map(hour => {
        const slot = slotMap.get(`${date}|${hour}`) ?? null
        const state: CellState = !slot
          ? 'empty'
          : slot.isAvailable
            ? 'available'
            : 'booked'
        return { date, hour, state, slot }
      })
    })
  }, [slots, weekStart])

  // ----- Drag selection helpers -----
  const cellsInDrag = useMemo(() => {
    if (!dragStart || !dragEnd) return new Set<string>()
    // Constrain selection to a single day (vertical drag)
    if (dragStart.date !== dragEnd.date) {
      return new Set<string>([`${dragStart.date}|${dragStart.hour}`])
    }
    const min = Math.min(dragStart.hour, dragEnd.hour)
    const max = Math.max(dragStart.hour, dragEnd.hour)
    const out = new Set<string>()
    for (let h = min; h <= max; h++) out.add(`${dragStart.date}|${h}`)
    return out
  }, [dragStart, dragEnd])

  const onCellMouseDown = (cell: CellInfo) => {
    if (cell.state === 'booked') return // can't touch booked slots
    setDragStart({ date: cell.date, hour: cell.hour })
    setDragEnd({ date: cell.date, hour: cell.hour })
    setDragMode(cell.state === 'available' ? 'remove' : 'add')
  }

  const onCellMouseEnter = (cell: CellInfo) => {
    if (!dragStart) return
    setDragEnd({ date: cell.date, hour: cell.hour })
  }

  const onMouseUp = async () => {
    if (!dragStart || !dragEnd || !dragMode || !selectedEntityId) {
      setDragStart(null)
      setDragEnd(null)
      setDragMode(null)
      return
    }

    const min = Math.min(dragStart.hour, dragEnd.hour)
    const max = Math.max(dragStart.hour, dragEnd.hour)
    const date = dragStart.date
    setBusy(true)
    setError(null)

    try {
      if (dragMode === 'add') {
        // Bulk-generate hourly slots for the selected range
        await timeSlotApi.generate({
          venueId: entityType === 'venue' ? selectedEntityId : undefined,
          coachId: entityType === 'coach' ? selectedEntityId : undefined,
          dateFrom: date,
          dateTo: date,
          startHour: min,
          endHour: max + 1, // generate API treats end as exclusive
        })
      } else {
        // Remove available slots in the range, one at a time
        const toRemove = grid
          .flat()
          .filter(c => c.date === date && c.hour >= min && c.hour <= max && c.slot && c.slot.isAvailable)
        for (const c of toRemove) {
          if (c.slot) await timeSlotApi.deleteSlot(c.slot.id)
        }
      }
      await loadSlots()
    } catch (err: unknown) {
      setError((err as { error?: { message?: string } })?.error?.message ?? 'Failed to update slots')
    } finally {
      setBusy(false)
      setDragStart(null)
      setDragEnd(null)
      setDragMode(null)
    }
  }

  // Cancel drag if user releases outside grid
  useEffect(() => {
    const cleanup = () => {
      if (dragStart && dragEnd) onMouseUp()
    }
    window.addEventListener('mouseup', cleanup)
    return () => window.removeEventListener('mouseup', cleanup)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dragStart, dragEnd])

  // ----- Entity selector source -----
  const entityOptions = useMemo(() => {
    if (entityType === 'venue') return venues.map(v => ({ id: v.id, label: v.name }))
    if (coach) return [{ id: coach.id, label: coach.name }]
    return []
  }, [venues, coach, entityType])

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-gray-400" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Calendar className="h-6 w-6" />
            Weekly Calendar
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Click + drag to add available hours. Drag an existing block to remove it.
          </p>
        </div>
      </div>

      {error && (
        <div className="flex items-start gap-2 rounded-md bg-red-50 p-3 text-sm text-red-700">
          <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-wrap items-center gap-3">
        {(isAdmin || (venues.length > 0 && coach)) && (
          <select
            className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
            value={entityType}
            onChange={e => {
              const t = e.target.value as 'venue' | 'coach'
              setEntityType(t)
              if (t === 'venue' && venues[0]) setSelectedEntityId(venues[0].id)
              if (t === 'coach' && coach) setSelectedEntityId(coach.id)
            }}
          >
            {(isAdmin || venues.length > 0) && <option value="venue">Venue</option>}
            {(isAdmin || coach) && <option value="coach">Coach</option>}
          </select>
        )}
        {entityOptions.length > 1 && (
          <select
            className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
            value={selectedEntityId ?? ''}
            onChange={e => setSelectedEntityId(Number(e.target.value))}
          >
            {entityOptions.map(o => (
              <option key={o.id} value={o.id}>
                {o.label}
              </option>
            ))}
          </select>
        )}

        <div className="ml-auto flex items-center gap-2">
          <button
            className="rounded-md border border-gray-300 bg-white p-2 hover:bg-gray-50"
            onClick={() => setWeekStart(addDays(weekStart, -7))}
            aria-label="Previous week"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
          <button
            className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium hover:bg-gray-50"
            onClick={() => setWeekStart(startOfWeek(new Date()))}
          >
            Today
          </button>
          <button
            className="rounded-md border border-gray-300 bg-white p-2 hover:bg-gray-50"
            onClick={() => setWeekStart(addDays(weekStart, 7))}
            aria-label="Next week"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      </div>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white shadow-sm">
        <table className="w-full select-none border-collapse text-sm">
          <thead>
            <tr>
              <th className="border-b border-r border-gray-200 bg-gray-50 px-3 py-2 text-left font-medium text-gray-600 w-16">
                Hour
              </th>
              {DAY_LABELS.map((label, idx) => {
                const date = addDays(weekStart, idx)
                const isToday = isoDate(date) === isoDate(new Date())
                return (
                  <th
                    key={label}
                    className={`border-b border-r border-gray-200 px-3 py-2 text-center font-medium ${
                      isToday ? 'bg-blue-50 text-blue-700' : 'bg-gray-50 text-gray-600'
                    }`}
                  >
                    <div>{label}</div>
                    <div className="text-xs font-normal text-gray-500">
                      {date.getDate()}/{date.getMonth() + 1}
                    </div>
                  </th>
                )
              })}
            </tr>
          </thead>
          <tbody>
            {HOURS.map((hour, hIdx) => (
              <tr key={hour}>
                <td className="border-r border-b border-gray-200 bg-gray-50 px-3 py-2 text-xs font-medium text-gray-500">
                  {String(hour).padStart(2, '0')}:00
                </td>
                {grid.map((dayCells, dIdx) => {
                  const cell = dayCells[hIdx]
                  const inDrag = cellsInDrag.has(`${cell.date}|${cell.hour}`)
                  const baseClasses = 'border-r border-b border-gray-200 h-10 cursor-pointer transition-colors'
                  let stateClasses = ''
                  if (cell.state === 'booked') {
                    stateClasses = 'bg-amber-100 cursor-not-allowed'
                  } else if (cell.state === 'available') {
                    stateClasses = inDrag && dragMode === 'remove' ? 'bg-red-200' : 'bg-emerald-100 hover:bg-emerald-200'
                  } else {
                    stateClasses = inDrag && dragMode === 'add' ? 'bg-blue-200' : 'hover:bg-blue-50'
                  }
                  return (
                    <td
                      key={dIdx}
                      className={`${baseClasses} ${stateClasses}`}
                      onMouseDown={() => onCellMouseDown(cell)}
                      onMouseEnter={() => onCellMouseEnter(cell)}
                      title={
                        cell.state === 'booked'
                          ? 'Booked — cannot edit'
                          : cell.state === 'available'
                            ? 'Available · drag to remove'
                            : 'Click + drag to mark available'
                      }
                    />
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex flex-wrap items-center gap-4 text-xs text-gray-600">
        <div className="flex items-center gap-1.5">
          <span className="inline-block h-3 w-3 rounded-sm bg-emerald-100 border border-emerald-200" />
          Available
        </div>
        <div className="flex items-center gap-1.5">
          <span className="inline-block h-3 w-3 rounded-sm bg-amber-100 border border-amber-200" />
          Booked
        </div>
        <div className="flex items-center gap-1.5">
          <span className="inline-block h-3 w-3 rounded-sm bg-white border border-gray-200" />
          Empty
        </div>
        {busy && (
          <div className="flex items-center gap-1.5 ml-auto text-blue-600">
            <Loader2 className="h-3 w-3 animate-spin" />
            Saving…
          </div>
        )}
      </div>
    </div>
  )
}

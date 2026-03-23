import { useState } from 'react'
import { Calendar, Clock, ChevronDown, ChevronUp } from 'lucide-react'

export interface AvailabilityConfig {
  enabled: boolean
  dateFrom: string
  dateTo: string
  startHour: number
  endHour: number
  daysOfWeek: number[] // 0=Sun, 1=Mon, ..., 6=Sat
}

interface AvailabilitySetupProps {
  value: AvailabilityConfig
  onChange: (config: AvailabilityConfig) => void
}

const DAY_LABELS = [
  { key: 1, short: 'Mon', full: 'Monday' },
  { key: 2, short: 'Tue', full: 'Tuesday' },
  { key: 3, short: 'Wed', full: 'Wednesday' },
  { key: 4, short: 'Thu', full: 'Thursday' },
  { key: 5, short: 'Fri', full: 'Friday' },
  { key: 6, short: 'Sat', full: 'Saturday' },
  { key: 0, short: 'Sun', full: 'Sunday' },
]

const HOURS = Array.from({ length: 24 }, (_, i) => i)

function formatHour(h: number): string {
  return `${h.toString().padStart(2, '0')}:00`
}

export function AvailabilitySetup({ value, onChange }: AvailabilitySetupProps) {
  const [expanded, setExpanded] = useState(value.enabled)

  const update = (partial: Partial<AvailabilityConfig>) => {
    onChange({ ...value, ...partial })
  }

  const toggleDay = (day: number) => {
    const current = value.daysOfWeek
    const next = current.includes(day)
      ? current.filter((d) => d !== day)
      : [...current, day]
    update({ daysOfWeek: next })
  }

  const selectWeekdays = () => update({ daysOfWeek: [1, 2, 3, 4, 5] })
  const selectAll = () => update({ daysOfWeek: [0, 1, 2, 3, 4, 5, 6] })

  const totalHours = value.endHour - value.startHour
  const totalDays = value.daysOfWeek.length
  const dateFromObj = new Date(value.dateFrom + 'T00:00:00')
  const dateToObj = new Date(value.dateTo + 'T00:00:00')
  const rangeDays = Math.max(0, Math.ceil((dateToObj.getTime() - dateFromObj.getTime()) / (1000 * 60 * 60 * 24)) + 1)

  return (
    <div className="md:col-span-2">
      <div className="rounded-lg border border-blue-200 bg-blue-50/50">
        {/* Header - Toggle */}
        <button
          type="button"
          onClick={() => {
            const next = !expanded
            setExpanded(next)
            update({ enabled: next })
          }}
          className="flex w-full items-center justify-between px-4 py-3 text-left"
        >
          <div className="flex items-center gap-2">
            <Calendar className="h-5 w-5 text-blue-600" />
            <span className="font-semibold text-gray-900">Availability Setup</span>
            <span className="text-xs text-gray-500">(optional — set your schedule now or later)</span>
          </div>
          {expanded ? (
            <ChevronUp className="h-5 w-5 text-gray-400" />
          ) : (
            <ChevronDown className="h-5 w-5 text-gray-400" />
          )}
        </button>

        {expanded && (
          <div className="border-t border-blue-200 px-4 pb-4 pt-3">
            {/* Operating Hours */}
            <div className="mb-4">
              <label className="mb-2 flex items-center gap-1 text-sm font-medium text-gray-700">
                <Clock className="h-4 w-4" />
                Operating Hours
              </label>
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2">
                  <select
                    value={value.startHour}
                    onChange={(e) => update({ startHour: Number(e.target.value) })}
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  >
                    {HOURS.filter((h) => h < value.endHour).map((h) => (
                      <option key={h} value={h}>{formatHour(h)}</option>
                    ))}
                  </select>
                  <span className="text-sm text-gray-500">to</span>
                  <select
                    value={value.endHour}
                    onChange={(e) => update({ endHour: Number(e.target.value) })}
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  >
                    {HOURS.filter((h) => h > value.startHour).map((h) => (
                      <option key={h} value={h}>{formatHour(h)}</option>
                    ))}
                  </select>
                </div>
                <span className="text-xs text-gray-500">
                  ({totalHours} hour{totalHours !== 1 ? 's' : ''} / day)
                </span>
              </div>
            </div>

            {/* Days of Week */}
            <div className="mb-4">
              <label className="mb-2 block text-sm font-medium text-gray-700">Available Days</label>
              <div className="flex flex-wrap gap-2">
                {DAY_LABELS.map(({ key, short }) => {
                  const active = value.daysOfWeek.includes(key)
                  return (
                    <button
                      key={key}
                      type="button"
                      onClick={() => toggleDay(key)}
                      className={`rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
                        active
                          ? 'bg-blue-600 text-white shadow-sm'
                          : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {short}
                    </button>
                  )
                })}
              </div>
              <div className="mt-2 flex gap-2">
                <button
                  type="button"
                  onClick={selectWeekdays}
                  className="text-xs text-blue-600 hover:text-blue-800 hover:underline"
                >
                  Weekdays only
                </button>
                <span className="text-xs text-gray-300">|</span>
                <button
                  type="button"
                  onClick={selectAll}
                  className="text-xs text-blue-600 hover:text-blue-800 hover:underline"
                >
                  Every day
                </button>
              </div>
            </div>

            {/* Date Range */}
            <div className="mb-3">
              <label className="mb-2 block text-sm font-medium text-gray-700">Generate Slots For</label>
              <div className="flex items-center gap-3">
                <div>
                  <input
                    type="date"
                    value={value.dateFrom}
                    onChange={(e) => update({ dateFrom: e.target.value })}
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  />
                </div>
                <span className="text-sm text-gray-500">to</span>
                <div>
                  <input
                    type="date"
                    value={value.dateTo}
                    onChange={(e) => update({ dateTo: e.target.value })}
                    className="rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                  />
                </div>
              </div>
            </div>

            {/* Summary */}
            <div className="rounded-md bg-white/70 px-3 py-2 text-xs text-gray-600">
              Will generate <strong>{totalHours}</strong> hourly slots per day, on <strong>{totalDays}</strong> day{totalDays !== 1 ? 's' : ''} per week,
              across <strong>{rangeDays}</strong> days — approximately <strong>{Math.round(rangeDays * (totalDays / 7) * totalHours)}</strong> total slots.
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

/** Returns default availability config with sensible defaults */
export function getDefaultAvailability(): AvailabilityConfig {
  const today = new Date()
  const plus30 = new Date()
  plus30.setDate(today.getDate() + 30)

  return {
    enabled: false,
    dateFrom: today.toISOString().split('T')[0],
    dateTo: plus30.toISOString().split('T')[0],
    startHour: 9,
    endHour: 22,
    daysOfWeek: [1, 2, 3, 4, 5, 6, 0], // All days
  }
}

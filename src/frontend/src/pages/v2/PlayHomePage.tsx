import { useEffect, useMemo, useState } from 'react'
import {
  Search,
  MapPin,
  Clock,
  Users,
  Repeat,
  Star,
  Loader2,
  AlertCircle,
  Sparkles,
} from 'lucide-react'
import { v2Api } from '@/api/v2'
import type { HomeFeedResponse, PlaySearchResponse, PlaySuggestion } from '@/api/v2'

// ------------------------------------------------------------
// Canonical match-filter skill levels. Mirrors
// src/shared/types/skillLevel.ts — keep in sync.
// DB stores INTEGER (historically 1-5); legacy value 5 is
// collapsed into "Competitive" so old rows still render.
// ------------------------------------------------------------
const SKILL_LEVEL_META = [
  { numeric: 1, label: 'beginner', displayName: 'Beginner' },
  { numeric: 2, label: 'intermediate', displayName: 'Intermediate' },
  { numeric: 3, label: 'advanced', displayName: 'Advanced' },
  { numeric: 4, label: 'competitive', displayName: 'Competitive' },
] as const

function skillLevelDisplayName(n: number | null | undefined): string {
  if (n == null) return '—'
  if (n <= 1) return 'Beginner'
  if (n === 2) return 'Intermediate'
  if (n === 3) return 'Advanced'
  return 'Competitive' // 4 or legacy 5
}

function skillLevelRangeDisplay(
  min: number | null | undefined,
  max: number | null | undefined,
): string | null {
  const lo = skillLevelDisplayName(min)
  const hi = skillLevelDisplayName(max)
  if (lo === '—' && hi === '—') return null
  if (lo === hi) return lo
  if (lo === '—') return `Up to ${hi}`
  if (hi === '—') return `${lo}+`
  return `${lo} – ${hi}`
}

// ============================================================
// v2-practical-ux: "When + Where first" home page
// 1) Greeting bar + reliability badge
// 2) Time/location pickers ABOVE everything (primary CTA: search)
// 3) One-tap rebook strip
// 4) Suggested play cards
// 5) Friends available
// ============================================================

const SPORTS = [
  { value: '', label: 'Any sport' },
  { value: 'tennis', label: 'Tennis' },
  { value: 'paddle', label: 'Paddle' },
  { value: 'football', label: 'Football' },
  { value: 'basketball', label: 'Basketball' },
  { value: 'volleyball', label: 'Volleyball' },
  { value: 'badminton', label: 'Badminton' },
]

function isoLocal(d: Date): string {
  // input[type=datetime-local] needs YYYY-MM-DDTHH:mm without seconds/zone
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function fmtTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch {
    return iso
  }
}

function fmtDate(iso: string): string {
  try {
    return new Date(iso).toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' })
  } catch {
    return iso
  }
}

export function PlayHomePage() {
  const [feed, setFeed] = useState<HomeFeedResponse | null>(null)
  const [loadingFeed, setLoadingFeed] = useState(true)
  const [feedError, setFeedError] = useState<string | null>(null)

  // Search state
  const now = useMemo(() => new Date(), [])
  const tonight = useMemo(() => {
    const t = new Date(now)
    t.setHours(19, 0, 0, 0)
    if (t < now) t.setDate(t.getDate() + 1)
    return t
  }, [now])
  const tomorrowEnd = useMemo(() => {
    const t = new Date(tonight)
    t.setHours(23, 0, 0, 0)
    return t
  }, [tonight])

  const [from, setFrom] = useState(isoLocal(tonight))
  const [to, setTo] = useState(isoLocal(tomorrowEnd))
  const [sportType, setSportType] = useState('')
  const [skillMin, setSkillMin] = useState<number | ''>('')
  const [skillMax, setSkillMax] = useState<number | ''>('')

  const [results, setResults] = useState<PlaySearchResponse | null>(null)
  const [searching, setSearching] = useState(false)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [hasSearched, setHasSearched] = useState(false)

  // Initial home feed load
  useEffect(() => {
    let cancelled = false
    v2Api
      .getHomeFeed()
      .then(res => {
        if (!cancelled) setFeed(res.data)
      })
      .catch(err => {
        if (!cancelled) setFeedError(err?.error?.message ?? 'Failed to load feed')
      })
      .finally(() => {
        if (!cancelled) setLoadingFeed(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const onSearch = async (e?: React.FormEvent) => {
    e?.preventDefault()
    setSearching(true)
    setSearchError(null)
    setHasSearched(true)
    try {
      const res = await v2Api.searchPlay({
        from: new Date(from).toISOString(),
        to: new Date(to).toISOString(),
        sportType: sportType || undefined,
        skillLevelMin: skillMin === '' ? undefined : Number(skillMin),
        skillLevelMax: skillMax === '' ? undefined : Number(skillMax),
      })
      setResults(res.data)
    } catch (err: unknown) {
      setSearchError((err as { error?: { message?: string } })?.error?.message ?? 'Search failed')
    } finally {
      setSearching(false)
    }
  }

  const onRebook = async (bookingId: number) => {
    // Default: rebook for the same hour tomorrow
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    const slotDate = tomorrow.toISOString().split('T')[0]
    const startTime = '19:00'
    try {
      await v2Api.rebook(bookingId, slotDate, startTime)
      // Optimistic refresh
      const fresh = await v2Api.getHomeFeed()
      setFeed(fresh.data)
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message ?? 'Rebook failed'
      alert(msg)
    }
  }

  return (
    <div className="space-y-6">
      {/* Greeting */}
      {feed?.greeting && (
        <div className="flex items-center justify-between rounded-lg bg-gradient-to-r from-indigo-600 to-purple-600 p-5 text-white shadow-sm">
          <div>
            <h1 className="text-2xl font-bold">
              Hey {feed.greeting.displayName ?? 'there'} 👋
            </h1>
            <p className="mt-1 text-sm text-indigo-100">
              Find a game, book a court, or invite friends
            </p>
          </div>
          <div className="text-right">
            <div className="flex items-center justify-end gap-1 text-sm font-medium">
              <Star className="h-4 w-4 fill-current" />
              {(feed.greeting.reliabilityScore * 100).toFixed(0)}%
            </div>
            <div className="text-xs text-indigo-200">
              reliability · {feed.greeting.totalAttended} games
            </div>
          </div>
        </div>
      )}

      {/* When + Where first — the primary CTA */}
      <form
        onSubmit={onSearch}
        className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm space-y-4"
      >
        <div className="flex items-center gap-2 text-gray-700 font-medium">
          <Search className="h-5 w-5" />
          <span>I want to play…</span>
        </div>

        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">From</label>
            <input
              type="datetime-local"
              value={from}
              onChange={e => setFrom(e.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">To</label>
            <input
              type="datetime-local"
              value={to}
              onChange={e => setTo(e.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Sport</label>
            <select
              value={sportType}
              onChange={e => setSportType(e.target.value)}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm bg-white"
            >
              {SPORTS.map(s => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Skill from</label>
            <select
              value={skillMin}
              onChange={e => setSkillMin(e.target.value === '' ? '' : Number(e.target.value))}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm bg-white"
            >
              <option value="">Any level</option>
              {SKILL_LEVEL_META.map(m => (
                <option key={m.label} value={m.numeric}>{m.displayName}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Skill to</label>
            <select
              value={skillMax}
              onChange={e => setSkillMax(e.target.value === '' ? '' : Number(e.target.value))}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm bg-white"
            >
              <option value="">Any level</option>
              {SKILL_LEVEL_META.map(m => (
                <option key={m.label} value={m.numeric}>{m.displayName}</option>
              ))}
            </select>
          </div>
        </div>

        <button
          type="submit"
          disabled={searching}
          className="inline-flex items-center justify-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
        >
          {searching ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
          Search
        </button>
      </form>

      {/* Search results */}
      {searchError && (
        <div className="flex items-start gap-2 rounded-md bg-red-50 p-3 text-sm text-red-700">
          <AlertCircle className="h-4 w-4 mt-0.5" />
          <span>{searchError}</span>
        </div>
      )}

      {hasSearched && results && (
        <section>
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">Available now</h2>
            <div className="flex items-center gap-2 text-xs text-gray-600">
              <Chip>{results.counts.lobbies} lobbies</Chip>
              <Chip>{results.counts.openSlots} open courts</Chip>
              <Chip>{results.counts.availablePlayers} players</Chip>
            </div>
          </div>
          {results.results.length === 0 ? (
            <p className="rounded-md bg-gray-50 p-4 text-sm text-gray-500">
              Nothing found for this window. Try a wider time range or another sport.
            </p>
          ) : (
            <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
              {results.results.map(r => (
                <PlayCard key={`${r.type}-${r.id}`} item={r} />
              ))}
            </div>
          )}
        </section>
      )}

      {/* One-tap rebook */}
      {!hasSearched && feed && feed.recentBookings.length > 0 && (
        <section>
          <h2 className="mb-3 flex items-center gap-2 text-lg font-semibold text-gray-900">
            <Repeat className="h-5 w-5" />
            Rebook in one tap
          </h2>
          <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
            {feed.recentBookings.map(b => (
              <RebookCard key={b.bookingId} booking={b} onRebook={onRebook} />
            ))}
          </div>
        </section>
      )}

      {/* Suggested play */}
      {!hasSearched && feed && feed.suggestedPlay.length > 0 && (
        <section>
          <h2 className="mb-3 flex items-center gap-2 text-lg font-semibold text-gray-900">
            <Sparkles className="h-5 w-5" />
            Suggested for you
          </h2>
          <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
            {feed.suggestedPlay.map(s => (
              <PlayCard key={`sugg-${s.id}`} item={s} />
            ))}
          </div>
        </section>
      )}

      {/* Friends available */}
      {!hasSearched && feed && feed.friendsAvailable.length > 0 && (
        <section>
          <h2 className="mb-3 flex items-center gap-2 text-lg font-semibold text-gray-900">
            <Users className="h-5 w-5" />
            Friends available now
          </h2>
          <div className="flex flex-wrap gap-3">
            {feed.friendsAvailable.map(f => (
              <div
                key={f.userId}
                className="flex items-center gap-3 rounded-full border border-gray-200 bg-white px-3 py-1.5 text-sm shadow-sm"
              >
                {f.photoUrl ? (
                  <img src={f.photoUrl} alt="" className="h-7 w-7 rounded-full object-cover" />
                ) : (
                  <div className="h-7 w-7 rounded-full bg-indigo-100 text-indigo-700 grid place-items-center text-xs font-semibold">
                    {(f.displayName ?? '?').slice(0, 1).toUpperCase()}
                  </div>
                )}
                <div className="leading-tight">
                  <div className="font-medium text-gray-900">{f.displayName ?? 'Player'}</div>
                  <div className="text-xs text-gray-500">{f.sportType}</div>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Loading + error states */}
      {loadingFeed && (
        <div className="flex h-32 items-center justify-center">
          <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
        </div>
      )}
      {feedError && (
        <div className="flex items-start gap-2 rounded-md bg-red-50 p-3 text-sm text-red-700">
          <AlertCircle className="h-4 w-4 mt-0.5" />
          <span>{feedError}</span>
        </div>
      )}
    </div>
  )
}

// ============================================================
// Sub-components
// ============================================================

function Chip({ children }: { children: React.ReactNode }) {
  return (
    <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-700">
      {children}
    </span>
  )
}

function PlayCard({ item }: { item: PlaySuggestion }) {
  const seatLabel = item.maxPlayers > 1 ? `${item.currentPlayers}/${item.maxPlayers}` : null
  const skillRange = item.skillLevelMin && item.skillLevelMax
    ? skillLevelRangeDisplay(item.skillLevelMin, item.skillLevelMax)
    : null
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-2">
        <div>
          <div className="flex items-center gap-1 text-xs text-gray-500 capitalize">
            {item.type === 'lobby' ? '🏟️ Lobby' : item.type === 'open_slot' ? '📅 Open court' : '🎯 Match'}
            {' · '}
            {item.sportType}
          </div>
          <h3 className="mt-1 font-semibold text-gray-900 text-sm">{item.title}</h3>
        </div>
        {item.price !== null && (
          <div className="text-right">
            <div className="text-sm font-semibold text-gray-900">€{item.price.toFixed(0)}</div>
          </div>
        )}
      </div>
      <div className="mt-3 space-y-1 text-xs text-gray-600">
        <div className="flex items-center gap-1">
          <Clock className="h-3 w-3" />
          {fmtDate(item.startAt)} · {fmtTime(item.startAt)}
        </div>
        {item.venueName && (
          <div className="flex items-center gap-1">
            <MapPin className="h-3 w-3" />
            {item.venueName}
            {item.distanceKm !== null && ` · ${item.distanceKm.toFixed(1)} km`}
          </div>
        )}
        {(seatLabel || skillRange) && (
          <div className="flex items-center gap-2 pt-1">
            {seatLabel && <Chip>{seatLabel} players</Chip>}
            {skillRange && <Chip>{skillRange}</Chip>}
          </div>
        )}
      </div>
      <button className="mt-3 w-full rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700">
        {item.type === 'lobby' ? 'Join lobby' : 'Book this slot'}
      </button>
    </div>
  )
}

function RebookCard({
  booking,
  onRebook,
}: {
  booking: { bookingId: number; venueName: string | null; coachName: string | null; sportType: string; lastSlotStart: string; price: number; timesBooked: number }
  onRebook: (id: number) => void
}) {
  const target = booking.venueName ?? booking.coachName ?? 'Booking'
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <div className="text-xs text-gray-500 capitalize">{booking.sportType}</div>
          <h3 className="mt-1 font-semibold text-gray-900 text-sm">{target}</h3>
          <div className="mt-1 text-xs text-gray-600">
            {booking.lastSlotStart || '—'} · €{booking.price.toFixed(0)}
          </div>
          {booking.timesBooked > 1 && (
            <div className="mt-1 text-xs text-indigo-600">
              You've booked here {booking.timesBooked}×
            </div>
          )}
        </div>
        <button
          onClick={() => onRebook(booking.bookingId)}
          className="inline-flex items-center gap-1 rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700"
        >
          <Repeat className="h-3 w-3" />
          Rebook
        </button>
      </div>
    </div>
  )
}

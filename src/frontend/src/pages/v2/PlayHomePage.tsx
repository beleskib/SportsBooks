import { useEffect, useMemo, useState } from 'react'
import {
  Search,
  MapPin,
  Clock,
  Users,
  Repeat,
  Loader2,
  AlertCircle,
  Sparkles,
  ShieldCheck,
  ChevronRight,
} from 'lucide-react'
import { v2Api } from '@/api/v2'
import type { HomeFeedResponse, PlaySearchResponse, PlaySuggestion } from '@/api/v2'

// ------------------------------------------------------------
// v2-practical-ux: "When + Where first" Play home page.
//
// Visual direction: calm / confident / premium.
//   - Navy 900 + US-Open-gold accent (yellow-300 on gray-900).
//   - No gradients, no emoji prefixes.
//   - Generous whitespace; subtle 1px borders; tabular numbers.
//   - Type hierarchy is restrained: one bold headline, everything else
//     in regular weight. Restraint is the brand.
//
// Behavior is unchanged from the previous mock — this is purely a
// visual refresh, so the API contract stays identical.
// ------------------------------------------------------------

// Canonical match-filter skill levels. Mirrors src/shared/types/skillLevel.ts.
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
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function fmtTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch { return iso }
}

function fmtDate(iso: string): string {
  try {
    return new Date(iso).toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' })
  } catch { return iso }
}

// Type guard for the {kind:'lobby'|'open_slot'|'match'} discriminator.
function typeLabel(t: string): string {
  if (t === 'lobby') return 'Lobby'
  if (t === 'open_slot') return 'Open court'
  return 'Match'
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

  useEffect(() => {
    let cancelled = false
    v2Api.getHomeFeed()
      .then(res => { if (!cancelled) setFeed(res.data) })
      .catch(err => { if (!cancelled) setFeedError(err?.error?.message ?? 'Failed to load feed') })
      .finally(() => { if (!cancelled) setLoadingFeed(false) })
    return () => { cancelled = true }
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
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    const slotDate = tomorrow.toISOString().split('T')[0]
    const startTime = '19:00'
    try {
      await v2Api.rebook(bookingId, slotDate, startTime)
      const fresh = await v2Api.getHomeFeed()
      setFeed(fresh.data)
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message ?? 'Rebook failed'
      alert(msg)
    }
  }

  return (
    <div className="mx-auto max-w-5xl space-y-10">
      {/* ─── Greeting (calm header, not a banner) ────────────────── */}
      {feed?.greeting && (
        <header className="flex items-end justify-between">
          <div>
            <p className="text-sm text-gray-500">Welcome back</p>
            <h1 className="mt-1 text-3xl font-semibold tracking-tight text-gray-900">
              {feed.greeting.displayName ?? 'Hello'}
            </h1>
          </div>
          {/* Reliability pill — discreet, dark, gold accent. */}
          <div className="inline-flex items-center gap-2 rounded-full bg-gray-900 px-3 py-1.5 text-xs font-medium text-yellow-300">
            <ShieldCheck className="h-3.5 w-3.5" />
            <span className="tabular-nums">
              {(feed.greeting.reliabilityScore * 100).toFixed(0)}%
            </span>
            <span className="text-gray-300">reliability</span>
            <span className="text-gray-500">·</span>
            <span className="text-gray-300">{feed.greeting.totalAttended} games</span>
          </div>
        </header>
      )}

      {/* ─── Primary CTA: "I want to play..." ───────────────────── */}
      <form
        onSubmit={onSearch}
        className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm"
      >
        <div className="flex items-baseline justify-between">
          <h2 className="text-lg font-semibold text-gray-900">I want to play…</h2>
          <p className="text-xs text-gray-500">Pick a window — we'll show what's open.</p>
        </div>

        <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="From">
            <input
              type="datetime-local"
              value={from}
              onChange={e => setFrom(e.target.value)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-gray-900 focus:outline-none"
            />
          </Field>
          <Field label="To">
            <input
              type="datetime-local"
              value={to}
              onChange={e => setTo(e.target.value)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-gray-900 focus:outline-none"
            />
          </Field>
        </div>

        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
          <Field label="Sport">
            <select
              value={sportType}
              onChange={e => setSportType(e.target.value)}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-gray-900 focus:outline-none"
            >
              {SPORTS.map(s => (
                <option key={s.value} value={s.value}>{s.label}</option>
              ))}
            </select>
          </Field>
          <Field label="Skill from">
            <select
              value={skillMin}
              onChange={e => setSkillMin(e.target.value === '' ? '' : Number(e.target.value))}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-gray-900 focus:outline-none"
            >
              <option value="">Any level</option>
              {SKILL_LEVEL_META.map(m => (
                <option key={m.label} value={m.numeric}>{m.displayName}</option>
              ))}
            </select>
          </Field>
          <Field label="Skill to">
            <select
              value={skillMax}
              onChange={e => setSkillMax(e.target.value === '' ? '' : Number(e.target.value))}
              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-gray-900 focus:outline-none"
            >
              <option value="">Any level</option>
              {SKILL_LEVEL_META.map(m => (
                <option key={m.label} value={m.numeric}>{m.displayName}</option>
              ))}
            </select>
          </Field>
        </div>

        <button
          type="submit"
          disabled={searching}
          className="mt-6 inline-flex items-center justify-center gap-2 rounded-lg bg-gray-900 px-5 py-2.5 text-sm font-semibold text-yellow-300 hover:bg-gray-800 disabled:opacity-60"
        >
          {searching ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
          {searching ? 'Searching…' : 'Show me what\u2019s open'}
        </button>
      </form>

      {/* ─── Results ────────────────────────────────────────────── */}
      {searchError && (
        <div className="flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
          <AlertCircle className="mt-0.5 h-4 w-4" />
          <span>{searchError}</span>
        </div>
      )}

      {hasSearched && results && (
        <Section
          title="Available now"
          aside={
            <div className="flex items-center gap-2 text-xs text-gray-500 tabular-nums">
              <Quiet>{results.counts.lobbies} lobbies</Quiet>
              <Quiet>{results.counts.openSlots} courts</Quiet>
              <Quiet>{results.counts.availablePlayers} players</Quiet>
            </div>
          }
        >
          {results.results.length === 0 ? (
            <EmptyState>
              Nothing for that window. Try a wider time range or another sport.
            </EmptyState>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {results.results.map(r => (
                <PlayCard key={`${r.type}-${r.id}`} item={r} />
              ))}
            </div>
          )}
        </Section>
      )}

      {/* ─── Rebook strip ────────────────────────────────────────── */}
      {!hasSearched && feed && feed.recentBookings.length > 0 && (
        <Section title="Rebook in one tap" icon={Repeat}>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {feed.recentBookings.map(b => (
              <RebookCard key={b.bookingId} booking={b} onRebook={onRebook} />
            ))}
          </div>
        </Section>
      )}

      {/* ─── Suggested ──────────────────────────────────────────── */}
      {!hasSearched && feed && feed.suggestedPlay.length > 0 && (
        <Section title="Suggested for you" icon={Sparkles}>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {feed.suggestedPlay.map(s => (
              <PlayCard key={`sugg-${s.id}`} item={s} />
            ))}
          </div>
        </Section>
      )}

      {/* ─── Friends available ──────────────────────────────────── */}
      {!hasSearched && feed && feed.friendsAvailable.length > 0 && (
        <Section title="Friends free now" icon={Users}>
          <div className="flex flex-wrap gap-2">
            {feed.friendsAvailable.map(f => (
              <div
                key={f.userId}
                className="inline-flex items-center gap-2 rounded-full border border-gray-200 bg-white px-3 py-1.5 text-sm shadow-sm"
              >
                {f.photoUrl ? (
                  <img src={f.photoUrl} alt="" className="h-6 w-6 rounded-full object-cover" />
                ) : (
                  <div className="grid h-6 w-6 place-items-center rounded-full bg-gray-900 text-[10px] font-semibold text-yellow-300">
                    {(f.displayName ?? '?').slice(0, 1).toUpperCase()}
                  </div>
                )}
                <span className="font-medium text-gray-900">{f.displayName ?? 'Player'}</span>
                <span className="text-xs text-gray-500 capitalize">· {f.sportType}</span>
              </div>
            ))}
          </div>
        </Section>
      )}

      {/* ─── Loading / error ────────────────────────────────────── */}
      {loadingFeed && (
        <div className="flex h-32 items-center justify-center">
          <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
        </div>
      )}
      {feedError && (
        <div className="flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
          <AlertCircle className="mt-0.5 h-4 w-4" />
          <span>{feedError}</span>
        </div>
      )}
    </div>
  )
}

// ============================================================
// Sub-components — kept private to this file.
// ============================================================

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-[11px] font-semibold uppercase tracking-wider text-gray-500">
        {label}
      </span>
      {children}
    </label>
  )
}

function Section({
  title,
  icon: Icon,
  aside,
  children,
}: {
  title: string
  icon?: React.ComponentType<{ className?: string }>
  aside?: React.ReactNode
  children: React.ReactNode
}) {
  return (
    <section>
      <div className="mb-4 flex items-end justify-between">
        <h2 className="inline-flex items-center gap-2 text-lg font-semibold text-gray-900">
          {Icon && <Icon className="h-4 w-4 text-gray-500" />}
          {title}
        </h2>
        {aside}
      </div>
      {children}
    </section>
  )
}

function Quiet({ children }: { children: React.ReactNode }) {
  return (
    <span className="inline-flex items-center rounded-full border border-gray-200 bg-white px-2.5 py-0.5">
      {children}
    </span>
  )
}

function EmptyState({ children }: { children: React.ReactNode }) {
  return (
    <div className="rounded-xl border border-dashed border-gray-300 bg-white px-6 py-10 text-center text-sm text-gray-500">
      {children}
    </div>
  )
}

function PlayCard({ item }: { item: PlaySuggestion }) {
  const seatLabel = item.maxPlayers > 1 ? `${item.currentPlayers}/${item.maxPlayers}` : null
  const skillRange = item.skillLevelMin && item.skillLevelMax
    ? skillLevelRangeDisplay(item.skillLevelMin, item.skillLevelMax)
    : null
  return (
    <article className="group flex flex-col rounded-2xl border border-gray-200 bg-white p-5 shadow-sm transition hover:border-gray-900">
      <div className="flex items-start justify-between gap-2">
        <div>
          <div className="text-[11px] font-semibold uppercase tracking-wider text-gray-500">
            {typeLabel(item.type)} · <span className="capitalize">{item.sportType}</span>
          </div>
          <h3 className="mt-2 text-base font-semibold leading-tight text-gray-900">
            {item.title}
          </h3>
        </div>
        {item.price !== null && (
          <div className="shrink-0 text-right">
            <div className="text-base font-semibold tabular-nums text-gray-900">
              €{item.price.toFixed(0)}
            </div>
          </div>
        )}
      </div>

      <div className="mt-4 space-y-1.5 text-sm text-gray-600">
        <div className="flex items-center gap-2">
          <Clock className="h-3.5 w-3.5 text-gray-400" />
          <span>{fmtDate(item.startAt)} · {fmtTime(item.startAt)}</span>
        </div>
        {item.venueName && (
          <div className="flex items-center gap-2">
            <MapPin className="h-3.5 w-3.5 text-gray-400" />
            <span>
              {item.venueName}
              {item.distanceKm !== null && (
                <span className="text-gray-400 tabular-nums"> · {item.distanceKm.toFixed(1)} km</span>
              )}
            </span>
          </div>
        )}
      </div>

      {(seatLabel || skillRange) && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {seatLabel && <Quiet>{seatLabel} players</Quiet>}
          {skillRange && <Quiet>{skillRange}</Quiet>}
        </div>
      )}

      <button className="mt-5 inline-flex items-center justify-center gap-1.5 rounded-lg bg-gray-900 px-4 py-2 text-sm font-semibold text-yellow-300 transition hover:bg-gray-800 group-hover:bg-gray-800">
        {item.type === 'lobby' ? 'Join lobby' : 'Book this slot'}
        <ChevronRight className="h-4 w-4" />
      </button>
    </article>
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
    <article className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
      <div className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 capitalize">
        {booking.sportType}
      </div>
      <h3 className="mt-2 text-base font-semibold leading-tight text-gray-900">{target}</h3>
      <div className="mt-1 text-sm text-gray-600 tabular-nums">
        {booking.lastSlotStart || '—'} · €{booking.price.toFixed(0)}
      </div>
      {booking.timesBooked > 1 && (
        <div className="mt-2 inline-flex items-center gap-1 rounded-full bg-gray-900 px-2.5 py-0.5 text-[11px] font-medium text-yellow-300">
          You've booked here {booking.timesBooked}×
        </div>
      )}
      <button
        onClick={() => onRebook(booking.bookingId)}
        className="mt-4 inline-flex w-full items-center justify-center gap-1.5 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-semibold text-gray-900 transition hover:border-gray-900"
      >
        <Repeat className="h-4 w-4" />
        Rebook tomorrow
      </button>
    </article>
  )
}

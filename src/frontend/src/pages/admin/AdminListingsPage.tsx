// Admin listing approval queue at /admin/listings.
// Lists every venue + coach in `pending` status. The admin clicks through
// to AdminListingDetailPage to review the full submission and decide.
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Building2, GraduationCap, Clock, ArrowRight, ClipboardCheck } from 'lucide-react'
import { adminApi, type PendingListing } from '@/api/admin'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

function relativeTime(iso: string): string {
  const ms = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(ms / 60_000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}d ago`
  return new Date(iso).toLocaleDateString()
}

function ListingRow({ listing }: { listing: PendingListing }) {
  const Icon = listing.type === 'venue' ? Building2 : GraduationCap
  const ownerLabel = listing.ownerDisplayName ?? listing.ownerEmail
  const cityBit = listing.city ? ` · ${listing.city}` : ''
  return (
    <Link
      to={`/admin/listings/${listing.type}/${listing.id}`}
      className="flex items-center gap-4 rounded-xl border border-gray-200 bg-white p-4 hover:border-gray-900 transition shadow-sm"
    >
      {listing.primaryImageUrl ? (
        <img
          src={listing.primaryImageUrl}
          alt=""
          className="h-16 w-16 flex-shrink-0 rounded-lg object-cover"
        />
      ) : (
        <div className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-lg bg-gray-900">
          <Icon className="h-7 w-7 text-yellow-300" />
        </div>
      )}
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <span className="rounded-full bg-gray-900 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-yellow-300">
            {listing.type}
          </span>
          <span className="text-base font-semibold text-gray-900 truncate">{listing.name}</span>
        </div>
        <div className="mt-0.5 truncate text-sm text-gray-500">
          {listing.sportType}
          {cityBit} · {listing.pricePerHour.toLocaleString()} ден/h
        </div>
        <div className="mt-1 flex items-center gap-3 text-xs text-gray-500">
          <span className="truncate">by {ownerLabel}</span>
          <span className="inline-flex items-center gap-1">
            <Clock className="h-3 w-3" />
            {relativeTime(listing.createdAt)}
          </span>
        </div>
      </div>
      <ArrowRight className="h-5 w-5 flex-shrink-0 text-gray-400" />
    </Link>
  )
}

export function AdminListingsPage() {
  const [listings, setListings] = useState<PendingListing[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    adminApi
      .getPendingListings()
      .then((res) => setListings(res.data ?? []))
      .catch((err) => setError(err?.error?.message ?? 'Could not load listings'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Listing approvals</h1>
        <p className="mt-1 text-sm text-gray-500">
          Review each venue or coach before it goes live. Open one to see full details, then approve or send back with feedback.
        </p>
      </div>

      {loading && <LoadingSpinner />}
      {error && <div className="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{error}</div>}

      {!loading && !error && listings.length === 0 && (
        <div className="rounded-xl border border-dashed border-gray-300 bg-white p-12 text-center">
          <ClipboardCheck className="mx-auto h-10 w-10 text-gray-300" />
          <div className="mt-3 text-sm font-medium text-gray-700">Nothing waiting on you.</div>
          <div className="mt-1 text-xs text-gray-500">All submitted listings have been reviewed.</div>
        </div>
      )}

      <div className="space-y-3">
        {listings.map((l) => (
          <ListingRow key={`${l.type}-${l.id}`} listing={l} />
        ))}
      </div>
    </div>
  )
}

export default AdminListingsPage

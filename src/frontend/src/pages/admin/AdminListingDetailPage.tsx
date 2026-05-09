// Full listing review page at /admin/listings/:type/:id.
// Shows everything an admin needs to verify a venue/coach is real:
// photos, address (with map link), price, equipment/certifications,
// owner contact details. Approve or reject + reason.
import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import {
  ArrowLeft,
  Mail,
  Phone,
  MapPin,
  CalendarDays,
  Building2,
  GraduationCap,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react'
import { adminApi, type ListingDetail, type ListingType } from '@/api/admin'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

function StatusPill({ status }: { status: ListingDetail['approvalStatus'] }) {
  const config = {
    pending: { label: 'Pending review', cls: 'bg-yellow-50 text-yellow-800 ring-yellow-200' },
    approved: { label: 'Approved · live', cls: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
    rejected: { label: 'Sent back for changes', cls: 'bg-rose-50 text-rose-700 ring-rose-200' },
  }[status]
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ring-1 ring-inset ${config.cls}`}>
      {status === 'approved' && <CheckCircle2 className="h-3.5 w-3.5" />}
      {status === 'pending' && <AlertCircle className="h-3.5 w-3.5" />}
      {config.label}
    </span>
  )
}

function mapsUrl(detail: ListingDetail): string | null {
  const q = [detail.address, detail.city, detail.country].filter(Boolean).join(', ')
  if (!q) return null
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(q)}`
}

export function AdminListingDetailPage() {
  const { type, id } = useParams<{ type: ListingType; id: string }>()
  const navigate = useNavigate()
  const [detail, setDetail] = useState<ListingDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionPending, setActionPending] = useState(false)
  const [showReject, setShowReject] = useState(false)
  const [reason, setReason] = useState('')

  useEffect(() => {
    if (!type || !id) return
    adminApi
      .getListingDetail(type, Number(id))
      .then((res) => setDetail(res.data))
      .catch((err) => setError(err?.error?.message ?? 'Could not load listing'))
      .finally(() => setLoading(false))
  }, [type, id])

  if (loading) return <LoadingSpinner />
  if (error) return <div className="p-8 text-red-600">{error}</div>
  if (!detail || !type) return null

  const Icon = detail.type === 'venue' ? Building2 : GraduationCap

  const handleApprove = async () => {
    setActionPending(true)
    try {
      await adminApi.approveListing(detail.type, detail.id)
      navigate('/admin/listings')
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Could not approve'
      setError(msg)
    } finally {
      setActionPending(false)
    }
  }

  const handleReject = async () => {
    if (!reason.trim()) return
    setActionPending(true)
    try {
      await adminApi.rejectListing(detail.type, detail.id, reason.trim())
      navigate('/admin/listings')
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Could not reject'
      setError(msg)
      setActionPending(false)
    }
  }

  return (
    <div className="space-y-6">
      <Link to="/admin/listings" className="inline-flex items-center gap-1 text-sm text-gray-600 hover:text-gray-900">
        <ArrowLeft className="h-4 w-4" />
        Back to queue
      </Link>

      {/* Header */}
      <div className="flex items-start justify-between gap-4">
        <div className="flex items-start gap-4">
          <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-lg bg-gray-900">
            <Icon className="h-6 w-6 text-yellow-300" />
          </div>
          <div>
            <div className="text-xs font-semibold uppercase tracking-wide text-gray-500">{detail.type}</div>
            <h1 className="text-2xl font-semibold text-gray-900">{detail.name}</h1>
            <div className="mt-1 text-sm text-gray-500">
              {detail.sportType} · {detail.pricePerHour.toLocaleString()} ден/hr
            </div>
          </div>
        </div>
        <StatusPill status={detail.approvalStatus} />
      </div>

      {/* Photos */}
      {detail.imageUrls.length > 0 && (
        <div>
          <SectionLabel>Photos ({detail.imageUrls.length})</SectionLabel>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
            {detail.imageUrls.map((url, i) => (
              <a key={i} href={url} target="_blank" rel="noreferrer" className="block overflow-hidden rounded-lg border border-gray-200">
                <img src={url} alt="" className="h-32 w-full object-cover" />
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Description */}
      {detail.description && (
        <div>
          <SectionLabel>Description</SectionLabel>
          <p className="rounded-xl border border-gray-200 bg-white p-4 text-sm text-gray-700 whitespace-pre-wrap">
            {detail.description}
          </p>
        </div>
      )}

      {/* Location */}
      <div>
        <SectionLabel>Location</SectionLabel>
        <div className="rounded-xl border border-gray-200 bg-white p-4 text-sm text-gray-700">
          <div className="flex items-start gap-2">
            <MapPin className="mt-0.5 h-4 w-4 flex-shrink-0 text-gray-400" />
            <div>
              <div>{detail.address ?? <span className="italic text-gray-400">No address</span>}</div>
              {(detail.city || detail.country) && (
                <div className="text-gray-500">
                  {[detail.city, detail.country].filter(Boolean).join(', ')}
                </div>
              )}
              {mapsUrl(detail) && (
                <a
                  href={mapsUrl(detail)!}
                  target="_blank"
                  rel="noreferrer"
                  className="mt-1 inline-block text-xs font-medium text-gray-900 underline"
                >
                  Open in Google Maps
                </a>
              )}
            </div>
          </div>
          {(detail.phoneNumber || detail.email) && (
            <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-2">
              {detail.phoneNumber && (
                <div className="inline-flex items-center gap-2"><Phone className="h-4 w-4 text-gray-400" />{detail.phoneNumber}</div>
              )}
              {detail.email && (
                <div className="inline-flex items-center gap-2"><Mail className="h-4 w-4 text-gray-400" />{detail.email}</div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Type-specific */}
      {detail.type === 'venue' && detail.equipment && detail.equipment.length > 0 && (
        <div>
          <SectionLabel>Equipment ({detail.equipment.length})</SectionLabel>
          <ul className="rounded-xl border border-gray-200 bg-white divide-y divide-gray-100">
            {detail.equipment.map((e, i) => (
              <li key={i} className="flex items-center justify-between p-3 text-sm">
                <div>
                  <div className="font-medium text-gray-900">{e.name}</div>
                  {e.description && <div className="text-xs text-gray-500">{e.description}</div>}
                </div>
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${e.isIncluded ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-600'}`}>
                  {e.isIncluded ? 'Included' : 'Extra'}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {detail.type === 'coach' && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {(detail.specialization || detail.experienceYears != null) && (
            <div className="rounded-xl border border-gray-200 bg-white p-4 text-sm">
              <SectionLabel>Specialization</SectionLabel>
              <div className="text-gray-900">{detail.specialization ?? '—'}</div>
              {detail.experienceYears != null && (
                <div className="mt-1 text-gray-500">{detail.experienceYears} years experience</div>
              )}
            </div>
          )}
          {detail.certifications && detail.certifications.length > 0 && (
            <div className="rounded-xl border border-gray-200 bg-white p-4 text-sm">
              <SectionLabel>Certifications</SectionLabel>
              <ul className="space-y-1">
                {detail.certifications.map((c, i) => (
                  <li key={i}>
                    <div className="font-medium text-gray-900">{c.name}</div>
                    <div className="text-xs text-gray-500">
                      {[c.issuingBody, c.yearObtained].filter(Boolean).join(' · ')}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Owner */}
      <div>
        <SectionLabel>Submitted by</SectionLabel>
        <div className="rounded-xl border border-gray-200 bg-white p-4">
          <div className="flex items-start gap-3">
            {detail.owner.photoUrl ? (
              <img src={detail.owner.photoUrl} alt="" className="h-10 w-10 rounded-full object-cover" />
            ) : (
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-900 text-yellow-300 text-sm font-semibold">
                {(detail.owner.displayName ?? detail.owner.email).slice(0, 1).toUpperCase()}
              </div>
            )}
            <div className="text-sm">
              <div className="font-semibold text-gray-900">{detail.owner.displayName ?? detail.owner.email}</div>
              <div className="text-gray-500">{detail.owner.email}</div>
              {detail.owner.phoneNumber && <div className="text-gray-500">{detail.owner.phoneNumber}</div>}
              {detail.owner.bio && <div className="mt-2 text-gray-700">{detail.owner.bio}</div>}
              <div className="mt-2 inline-flex items-center gap-1 text-xs text-gray-400">
                <CalendarDays className="h-3 w-3" />
                Joined {new Date(detail.owner.createdAt).toLocaleDateString()}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Previous decision (if any) */}
      {detail.approvalStatus === 'rejected' && detail.approvalRejectionReason && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          <div className="font-medium mb-1">Previous review note:</div>
          <div>{detail.approvalRejectionReason}</div>
        </div>
      )}

      {/* Decision controls — only when not yet approved */}
      {detail.approvalStatus !== 'approved' && !showReject && (
        <div className="flex gap-3 border-t border-gray-200 pt-4">
          <button
            onClick={handleApprove}
            disabled={actionPending}
            className="flex-1 rounded-lg bg-gray-900 px-4 py-3 text-sm font-semibold text-yellow-300 hover:bg-gray-800 disabled:opacity-50"
          >
            {actionPending ? 'Working…' : 'Approve and publish'}
          </button>
          <button
            onClick={() => setShowReject(true)}
            disabled={actionPending}
            className="flex-1 rounded-lg border border-gray-300 bg-white px-4 py-3 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            Send back for changes
          </button>
        </div>
      )}

      {showReject && (
        <div className="space-y-2 border-t border-gray-200 pt-4">
          <label className="block text-sm font-medium text-gray-900">
            What needs to change?
          </label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={4}
            placeholder="The partner will see this in their dashboard and an email."
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-gray-900 focus:outline-none"
          />
          <div className="flex gap-2">
            <button
              onClick={handleReject}
              disabled={actionPending || !reason.trim()}
              className="flex-1 rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-700 disabled:opacity-50"
            >
              {actionPending ? 'Sending…' : 'Send changes request'}
            </button>
            <button
              onClick={() => { setShowReject(false); setReason('') }}
              disabled={actionPending}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return <div className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-500">{children}</div>
}

export default AdminListingDetailPage

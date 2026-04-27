// Admin partner approval queue at /admin/partners.
// Two tabs: pending (default) and all. Approve flips them live; reject
// keeps them in queue with a reason note attached.
import { useEffect, useState } from 'react'
import { CheckCircle2, XCircle, Mail, Phone, Building2, GraduationCap, Clock } from 'lucide-react'
import { adminApi, type PartnerSummary } from '@/api/admin'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

type Tab = 'pending' | 'all'

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

function PartnerCard({
  partner,
  onApprove,
  onReject,
  pendingAction,
}: {
  partner: PartnerSummary
  onApprove: (id: number) => Promise<void>
  onReject: (id: number, reason: string) => Promise<void>
  pendingAction: boolean
}) {
  const [showReject, setShowReject] = useState(false)
  const [reason, setReason] = useState('')
  const isApproved = !!partner.approvedAt
  const partnerLabel = partner.partnerType === 'venue_owner'
    ? 'Venue owner'
    : partner.partnerType === 'coach'
      ? 'Coach'
      : 'Partner'

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div className="flex items-start gap-4">
          {partner.photoUrl ? (
            <img
              src={partner.photoUrl}
              alt=""
              className="h-12 w-12 rounded-full object-cover"
            />
          ) : (
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-900 text-yellow-300 text-sm font-semibold">
              {(partner.displayName ?? partner.email).slice(0, 1).toUpperCase()}
            </div>
          )}
          <div>
            <div className="text-base font-semibold text-gray-900">
              {partner.displayName ?? partner.email}
            </div>
            <div className="mt-0.5 inline-flex items-center gap-2 text-xs text-gray-500">
              <span className="rounded-full bg-gray-100 px-2 py-0.5 font-medium text-gray-700">
                {partnerLabel}
              </span>
              <span className="inline-flex items-center gap-1">
                <Clock className="h-3 w-3" />
                Joined {relativeTime(partner.createdAt)}
              </span>
            </div>
          </div>
        </div>
        {isApproved && (
          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
            <CheckCircle2 className="h-3.5 w-3.5" />
            Approved
          </span>
        )}
      </div>

      <div className="mt-4 grid grid-cols-1 gap-2 text-sm text-gray-700 sm:grid-cols-2">
        <div className="inline-flex items-center gap-2">
          <Mail className="h-4 w-4 text-gray-400" />
          {partner.email}
        </div>
        {partner.phoneNumber && (
          <div className="inline-flex items-center gap-2">
            <Phone className="h-4 w-4 text-gray-400" />
            {partner.phoneNumber}
          </div>
        )}
        <div className="inline-flex items-center gap-2">
          <Building2 className="h-4 w-4 text-gray-400" />
          {partner.venueCount} venue{partner.venueCount === 1 ? '' : 's'}
        </div>
        <div className="inline-flex items-center gap-2">
          <GraduationCap className="h-4 w-4 text-gray-400" />
          {partner.coachCount} coach{partner.coachCount === 1 ? '' : 'es'}
        </div>
      </div>

      {partner.bio && (
        <div className="mt-3 rounded-lg bg-gray-50 p-3 text-sm text-gray-700">{partner.bio}</div>
      )}

      {partner.rejectionReason && !isApproved && (
        <div className="mt-3 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">
          <span className="font-medium">Previously asked to revise:</span> {partner.rejectionReason}
        </div>
      )}

      {!isApproved && !showReject && (
        <div className="mt-4 flex gap-2">
          <button
            disabled={pendingAction}
            onClick={() => onApprove(partner.id)}
            className="flex-1 rounded-lg bg-gray-900 px-4 py-2 text-sm font-semibold text-yellow-300 hover:bg-gray-800 disabled:opacity-50"
          >
            {pendingAction ? 'Working…' : 'Approve'}
          </button>
          <button
            disabled={pendingAction}
            onClick={() => setShowReject(true)}
            className="flex-1 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            Ask for changes
          </button>
        </div>
      )}

      {!isApproved && showReject && (
        <div className="mt-4 space-y-2">
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="What needs to change before we can approve them? (sent to the partner)"
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-gray-900 focus:outline-none"
          />
          <div className="flex gap-2">
            <button
              disabled={pendingAction || !reason.trim()}
              onClick={async () => {
                await onReject(partner.id, reason.trim())
                setShowReject(false)
                setReason('')
              }}
              className="flex-1 rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-700 disabled:opacity-50"
            >
              {pendingAction ? 'Sending…' : 'Send changes request'}
            </button>
            <button
              disabled={pendingAction}
              onClick={() => { setShowReject(false); setReason('') }}
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

export function AdminPartnersPage() {
  const [tab, setTab] = useState<Tab>('pending')
  const [partners, setPartners] = useState<PartnerSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [pendingId, setPendingId] = useState<number | null>(null)

  const reload = async (which: Tab) => {
    setLoading(true)
    setError(null)
    try {
      const res = which === 'pending'
        ? await adminApi.getPendingPartners()
        : await adminApi.getAllPartners()
      setPartners(res.data ?? [])
    } catch (e: any) {
      setError(e?.error?.message ?? 'Could not load partners')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { reload(tab) }, [tab])

  const handleApprove = async (id: number) => {
    setPendingId(id)
    try {
      await adminApi.approvePartner(id)
      await reload(tab)
    } finally {
      setPendingId(null)
    }
  }

  const handleReject = async (id: number, reason: string) => {
    setPendingId(id)
    try {
      await adminApi.rejectPartner(id, reason)
      await reload(tab)
    } finally {
      setPendingId(null)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Partner approvals</h1>
          <p className="mt-1 text-sm text-gray-500">
            Approve new partners before their venues and coaches go live.
          </p>
        </div>
        <div className="inline-flex rounded-lg border border-gray-200 bg-white p-1">
          {(['pending', 'all'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`rounded-md px-4 py-1.5 text-sm font-medium transition ${
                tab === t
                  ? 'bg-gray-900 text-yellow-300'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              {t === 'pending' ? 'Pending' : 'All'}
            </button>
          ))}
        </div>
      </div>

      {loading && <LoadingSpinner />}
      {error && <div className="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{error}</div>}

      {!loading && !error && partners.length === 0 && (
        <div className="rounded-xl border border-dashed border-gray-300 bg-white p-12 text-center">
          <XCircle className="mx-auto h-10 w-10 text-gray-300" />
          <div className="mt-3 text-sm font-medium text-gray-700">
            {tab === 'pending' ? 'No partners awaiting approval.' : 'No partners yet.'}
          </div>
        </div>
      )}

      <div className="space-y-3">
        {partners.map((p) => (
          <PartnerCard
            key={p.id}
            partner={p}
            onApprove={handleApprove}
            onReject={handleReject}
            pendingAction={pendingId === p.id}
          />
        ))}
      </div>
    </div>
  )
}

export default AdminPartnersPage

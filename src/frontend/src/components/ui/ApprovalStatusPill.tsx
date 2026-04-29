// Small status pill for a venue/coach's listing-level approval state.
// Used on partner-facing venue + coach lists so partners can see at a glance
// which of their listings are live vs awaiting admin review vs needing rework.
import { Clock, CheckCircle2, XCircle } from 'lucide-react'
import type { ListingApprovalStatus } from '@/types'

interface ApprovalStatusPillProps {
  status: ListingApprovalStatus
  rejectionReason?: string | null
}

export function ApprovalStatusPill({ status, rejectionReason }: ApprovalStatusPillProps) {
  if (status === 'approved') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">
        <CheckCircle2 className="h-3 w-3" />
        Approved
      </span>
    )
  }
  if (status === 'rejected') {
    return (
      <span
        className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-2 py-0.5 text-xs font-medium text-rose-700"
        title={rejectionReason ?? undefined}
      >
        <XCircle className="h-3 w-3" />
        Needs revision
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-yellow-100 px-2 py-0.5 text-xs font-medium text-yellow-800">
      <Clock className="h-3 w-3" />
      Pending review
    </span>
  )
}

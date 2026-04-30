import { useEffect, useState, useCallback } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { ArrowLeft, AlertTriangle } from 'lucide-react'
import { CoachForm } from '@/components/coaches/CoachForm'
import { ApprovalStatusPill } from '@/components/ui/ApprovalStatusPill'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { Toast } from '@/components/ui/Toast'
import { coachApi } from '@/api/coaches'
import type { Coach, UpdateCoachRequest, CreateCoachRequest } from '@/types'

export function CoachEditPage() {
  const { id } = useParams<{ id: string }>()
  const [coach, setCoach] = useState<Coach | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [fetchError, setFetchError] = useState<string | null>(null)
  const [toast, setToast] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchCoach = async () => {
      try {
        const response = await coachApi.getById(Number(id))
        setCoach(response.data)
      } catch (err: unknown) {
        setFetchError(
          (err as { error?: { message?: string } })?.error?.message || 'Failed to load coach',
        )
      } finally {
        setLoading(false)
      }
    }
    fetchCoach()
  }, [id])

  const handleSubmit = useCallback(
    async (data: UpdateCoachRequest | CreateCoachRequest) => {
      setSaving(true)
      try {
        await coachApi.update(Number(id), data as UpdateCoachRequest)
        setToast({ message: 'Coach profile updated successfully.', variant: 'success' })
        setTimeout(() => navigate('/coaches'), 1500)
      } catch (err: unknown) {
        const msg =
          (err as { error?: { message?: string } })?.error?.message || 'Failed to save coach'
        setToast({ message: msg, variant: 'error' })
        throw err
      } finally {
        setSaving(false)
      }
    },
    [id, navigate],
  )

  if (loading) return <LoadingSpinner />

  if (fetchError) {
    return (
      <div>
        <Link
          to="/coaches"
          className="mb-4 inline-flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Coaches
        </Link>
        <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{fetchError}</div>
      </div>
    )
  }

  if (!coach) {
    return (
      <div>
        <Link
          to="/coaches"
          className="mb-4 inline-flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Coaches
        </Link>
        <p className="text-gray-500">Coach not found.</p>
      </div>
    )
  }

  const isNonApproved = coach.approvalStatus !== 'approved'

  return (
    <>
      {isNonApproved && (
        <div className="mb-4 flex items-center justify-between rounded-xl border border-gray-200 bg-white px-4 py-3 shadow-sm">
          <div className="flex items-center gap-3">
            <AlertTriangle className="h-5 w-5 flex-shrink-0 text-yellow-600" />
            <div>
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-900">Listing status:</span>
                <ApprovalStatusPill
                  status={coach.approvalStatus}
                  rejectionReason={coach.approvalRejectionReason}
                />
              </div>
              {coach.approvalStatus === 'pending' && (
                <p className="mt-0.5 text-xs text-gray-500">
                  This coach profile is awaiting admin review and is not yet visible to players.
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      {coach.approvalStatus === 'rejected' && coach.approvalRejectionReason && (
        <div className="mb-4 rounded-xl border border-rose-200 bg-rose-50 p-4">
          <div className="flex items-start gap-3">
            <span className="inline-flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-rose-500 text-xs font-bold text-white">
              ×
            </span>
            <div className="flex-1 text-sm">
              <div className="font-semibold text-rose-900">
                Asked to revise — {coach.name}
              </div>
              <div className="mt-1 rounded-md bg-rose-100 p-2 text-rose-900">
                <span className="font-medium">Reason:</span> {coach.approvalRejectionReason}
              </div>
              <p className="mt-2 text-xs text-rose-700">
                Update the details below and save to resubmit for review.
              </p>
            </div>
          </div>
        </div>
      )}

      <CoachForm initialData={coach} onSubmit={handleSubmit} loading={saving} />
      {toast && (
        <Toast
          message={toast.message}
          variant={toast.variant}
          onDismiss={() => setToast(null)}
        />
      )}
    </>
  )
}

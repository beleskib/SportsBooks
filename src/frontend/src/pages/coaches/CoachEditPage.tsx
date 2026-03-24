import { useEffect, useState, useCallback } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { CoachForm } from '@/components/coaches/CoachForm'
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

  return (
    <>
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

import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CoachForm } from '@/components/coaches/CoachForm'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { coachApi } from '@/api/coaches'
import type { Coach, UpdateCoachRequest } from '@/types'

export function CoachEditPage() {
  const { id } = useParams<{ id: string }>()
  const [coach, setCoach] = useState<Coach | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchCoach = async () => {
      try {
        const response = await coachApi.getById(Number(id))
        setCoach(response.data)
      } catch (err: unknown) {
        setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load coach')
      } finally {
        setLoading(false)
      }
    }
    fetchCoach()
  }, [id])

  const handleSubmit = async (data: UpdateCoachRequest | import('@/types').CreateCoachRequest) => {
    setSaving(true)
    try {
      await coachApi.update(Number(id), data as UpdateCoachRequest)
      navigate('/coaches')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner />
  if (error) return <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{error}</div>
  if (!coach) return <div className="text-gray-500">Coach not found</div>

  return <CoachForm initialData={coach} onSubmit={handleSubmit} loading={saving} />
}

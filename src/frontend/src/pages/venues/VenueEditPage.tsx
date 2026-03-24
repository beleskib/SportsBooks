import { useEffect, useState, useCallback } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { VenueForm } from '@/components/venues/VenueForm'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { Toast } from '@/components/ui/Toast'
import { venueApi } from '@/api/venues'
import type { Venue, UpdateVenueRequest, CreateVenueRequest } from '@/types'

export function VenueEditPage() {
  const { id } = useParams<{ id: string }>()
  const [venue, setVenue] = useState<Venue | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [fetchError, setFetchError] = useState<string | null>(null)
  const [toast, setToast] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchVenue = async () => {
      try {
        const response = await venueApi.getById(Number(id))
        setVenue(response.data)
      } catch (err: unknown) {
        setFetchError(
          (err as { error?: { message?: string } })?.error?.message || 'Failed to load venue',
        )
      } finally {
        setLoading(false)
      }
    }
    fetchVenue()
  }, [id])

  const handleSubmit = useCallback(
    async (data: UpdateVenueRequest | CreateVenueRequest) => {
      setSaving(true)
      try {
        await venueApi.update(Number(id), data as UpdateVenueRequest)
        setToast({ message: 'Venue updated successfully.', variant: 'success' })
        setTimeout(() => navigate('/venues'), 1500)
      } catch (err: unknown) {
        const msg =
          (err as { error?: { message?: string } })?.error?.message || 'Failed to save venue'
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
          to="/venues"
          className="mb-4 inline-flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Venues
        </Link>
        <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{fetchError}</div>
      </div>
    )
  }

  if (!venue) {
    return (
      <div>
        <Link
          to="/venues"
          className="mb-4 inline-flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Venues
        </Link>
        <p className="text-gray-500">Venue not found.</p>
      </div>
    )
  }

  return (
    <>
      <VenueForm initialData={venue} onSubmit={handleSubmit} loading={saving} />
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

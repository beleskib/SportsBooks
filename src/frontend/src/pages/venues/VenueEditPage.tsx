import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { VenueForm } from '@/components/venues/VenueForm'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { venueApi } from '@/api/venues'
import type { Venue, UpdateVenueRequest } from '@/types'

export function VenueEditPage() {
  const { id } = useParams<{ id: string }>()
  const [venue, setVenue] = useState<Venue | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchVenue = async () => {
      try {
        const response = await venueApi.getById(Number(id))
        setVenue(response.data)
      } catch (err: unknown) {
        setError((err as { error?: { message?: string } })?.error?.message || 'Failed to load venue')
      } finally {
        setLoading(false)
      }
    }
    fetchVenue()
  }, [id])

  const handleSubmit = async (data: UpdateVenueRequest | import('@/types').CreateVenueRequest) => {
    setSaving(true)
    try {
      await venueApi.update(Number(id), data as UpdateVenueRequest)
      navigate('/venues')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner />
  if (error) return <div className="rounded-md bg-red-50 p-4 text-sm text-red-700">{error}</div>
  if (!venue) return <div className="text-gray-500">Venue not found</div>

  return <VenueForm initialData={venue} onSubmit={handleSubmit} loading={saving} />
}

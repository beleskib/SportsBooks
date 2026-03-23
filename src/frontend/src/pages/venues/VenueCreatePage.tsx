import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { VenueForm } from '@/components/venues/VenueForm'
import { venueApi } from '@/api/venues'
import { timeSlotApi } from '@/api/timeSlots'
import type { CreateVenueRequest, UpdateVenueRequest } from '@/types'
import type { AvailabilityConfig } from '@/components/ui/AvailabilitySetup'

export function VenueCreatePage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (data: CreateVenueRequest | UpdateVenueRequest, availability?: AvailabilityConfig) => {
    setLoading(true)
    try {
      const result = await venueApi.create(data as CreateVenueRequest)
      const venueId = result.data.id

      // If availability was configured, generate time slots immediately
      if (availability && availability.enabled && venueId) {
        try {
          await timeSlotApi.generate({
            venueId,
            dateFrom: availability.dateFrom,
            dateTo: availability.dateTo,
            startHour: availability.startHour,
            endHour: availability.endHour,
            daysOfWeek: availability.daysOfWeek,
          })
        } catch {
          // Venue was created successfully; slot generation is best-effort
          console.warn('Venue created but time slot generation failed')
        }
      }

      navigate('/venues')
    } finally {
      setLoading(false)
    }
  }

  return <VenueForm onSubmit={handleSubmit} loading={loading} />
}

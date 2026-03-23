import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CoachForm } from '@/components/coaches/CoachForm'
import { coachApi } from '@/api/coaches'
import { timeSlotApi } from '@/api/timeSlots'
import type { CreateCoachRequest, UpdateCoachRequest } from '@/types'
import type { AvailabilityConfig } from '@/components/ui/AvailabilitySetup'

export function CoachCreatePage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (data: CreateCoachRequest | UpdateCoachRequest, availability?: AvailabilityConfig) => {
    setLoading(true)
    try {
      const result = await coachApi.create(data as CreateCoachRequest)
      const coachId = result.data.id

      // If availability was configured, generate time slots immediately
      if (availability && availability.enabled && coachId) {
        try {
          await timeSlotApi.generate({
            coachId,
            dateFrom: availability.dateFrom,
            dateTo: availability.dateTo,
            startHour: availability.startHour,
            endHour: availability.endHour,
            daysOfWeek: availability.daysOfWeek,
          })
        } catch {
          // Coach was created successfully; slot generation is best-effort
          console.warn('Coach created but time slot generation failed')
        }
      }

      navigate('/coaches')
    } finally {
      setLoading(false)
    }
  }

  return <CoachForm onSubmit={handleSubmit} loading={loading} />
}

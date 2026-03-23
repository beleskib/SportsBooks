import { apiClient } from './client'
import type { ApiResponse, TimeSlot } from '@/types'

export const timeSlotApi = {
  getVenueSlots: (venueId: number, dateFrom: string, dateTo: string) =>
    apiClient.get(`/venues/${venueId}/time-slots?dateFrom=${dateFrom}&dateTo=${dateTo}`) as Promise<ApiResponse<TimeSlot[]>>,

  getCoachSlots: (coachId: number, dateFrom: string, dateTo: string) =>
    apiClient.get(`/coaches/${coachId}/time-slots?dateFrom=${dateFrom}&dateTo=${dateTo}`) as Promise<ApiResponse<TimeSlot[]>>,

  generate: (data: {
    venueId?: number;
    coachId?: number;
    dateFrom: string;
    dateTo: string;
    startHour?: number;
    endHour?: number;
    daysOfWeek?: number[];
  }) =>
    apiClient.post('/time-slots/generate', data) as Promise<ApiResponse<TimeSlot[]>>,

  deleteSlot: (id: number) =>
    apiClient.delete(`/time-slots/${id}`) as Promise<ApiResponse<{ id: number }>>,
}

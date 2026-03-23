import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface BookingCount {
  entityId: number
  entityType: 'venue' | 'coach'
  totalBookings: number
  confirmedBookings: number
  completedBookings: number
}

export interface BookingCountsResponse {
  venues: BookingCount[]
  coaches: BookingCount[]
}

export const bookingApi = {
  getCounts: () =>
    apiClient.get('/bookings/counts') as Promise<ApiResponse<BookingCountsResponse>>,
}

import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface PartnerStats {
  totalBookings: number
  confirmedBookings: number
  totalRevenue: number
  avgRating: number
  totalReviews: number
  upcomingBookings: number
  revenueByMonth: { month: string; revenue: number }[]
  bookingsByStatus: { status: string; count: number }[]
}

export const dashboardApi = {
  getPartnerStats: () =>
    apiClient.get('/dashboard/partner/stats') as Promise<ApiResponse<PartnerStats>>,
}

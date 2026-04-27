// Admin API surface — all endpoints require role=admin on the backend.
// Used by the owner dashboard pages under /admin.
import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface PlatformOverview {
  totalUsers: number
  totalPlayers: number
  totalPartners: number
  pendingPartners: number
  totalVenues: number
  totalCoaches: number
  totalBookings: number
  bookingsLast30d: number
  pendingBookings: number
}

export interface PartnerSummary {
  id: number
  email: string
  displayName: string | null
  photoUrl: string | null
  partnerType: string | null
  bio: string | null
  phoneNumber: string | null
  approvedAt: string | null
  approvedByUserId: number | null
  rejectionReason: string | null
  createdAt: string
  venueCount: number
  coachCount: number
}

export const adminApi = {
  getOverview: () =>
    apiClient.get('/admin/overview') as Promise<ApiResponse<PlatformOverview>>,

  getPendingPartners: () =>
    apiClient.get('/admin/partners/pending') as Promise<ApiResponse<PartnerSummary[]>>,

  getAllPartners: () =>
    apiClient.get('/admin/partners') as Promise<ApiResponse<PartnerSummary[]>>,

  getPartner: (id: number) =>
    apiClient.get(`/admin/partners/${id}`) as Promise<ApiResponse<PartnerSummary>>,

  approvePartner: (id: number) =>
    apiClient.post(`/admin/partners/${id}/approve`) as Promise<ApiResponse<PartnerSummary>>,

  rejectPartner: (id: number, reason: string) =>
    apiClient.post(`/admin/partners/${id}/reject`, { reason }) as Promise<ApiResponse<PartnerSummary>>,
}

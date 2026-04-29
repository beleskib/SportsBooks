// Admin API surface — all endpoints require role=admin on the backend.
// Used by the owner dashboard pages under /admin.
import { apiClient } from './client'
import type { ApiResponse, ListingApprovalStatus } from '@/types'

export interface PlatformOverview {
  totalUsers: number
  totalPlayers: number
  totalPartners: number
  totalVenues: number
  totalCoaches: number
  pendingListings: number
  pendingVenues: number
  pendingCoaches: number
  totalBookings: number
  bookingsLast30d: number
  pendingBookings: number
}

export type ListingType = 'venue' | 'coach'

/** Compact entry shown in the admin queue. */
export interface PendingListing {
  type: ListingType
  id: number
  name: string
  sportType: string
  pricePerHour: number
  city: string | null
  address: string | null
  primaryImageUrl: string | null
  ownerId: number
  ownerEmail: string
  ownerDisplayName: string | null
  createdAt: string
}

/** Full review detail for a single listing. */
export interface ListingDetail {
  type: ListingType
  id: number
  name: string
  description: string | null
  sportType: string
  pricePerHour: number
  address: string | null
  city: string | null
  country: string | null
  latitude: number | null
  longitude: number | null
  phoneNumber: string | null
  email: string | null
  isActive: boolean
  approvalStatus: ListingApprovalStatus
  approvalDecidedAt: string | null
  approvalRejectionReason: string | null
  imageUrls: string[]
  // Venue-only:
  equipment?: Array<{ name: string; description: string | null; isIncluded: boolean }>
  // Coach-only:
  specialization?: string | null
  experienceYears?: number | null
  certifications?: Array<{ name: string; issuingBody: string | null; yearObtained: number | null }>
  owner: {
    id: number
    email: string
    displayName: string | null
    photoUrl: string | null
    phoneNumber: string | null
    bio: string | null
    createdAt: string
  }
  createdAt: string
}

export const adminApi = {
  getOverview: () =>
    apiClient.get('/admin/overview') as Promise<ApiResponse<PlatformOverview>>,

  getPendingListings: () =>
    apiClient.get('/admin/listings/pending') as Promise<ApiResponse<PendingListing[]>>,

  getListingDetail: (type: ListingType, id: number) =>
    apiClient.get(`/admin/listings/${type}/${id}`) as Promise<ApiResponse<ListingDetail>>,

  approveListing: (type: ListingType, id: number) =>
    apiClient.post(`/admin/listings/${type}/${id}/approve`) as Promise<ApiResponse<unknown>>,

  rejectListing: (type: ListingType, id: number, reason: string) =>
    apiClient.post(`/admin/listings/${type}/${id}/reject`, { reason }) as Promise<ApiResponse<unknown>>,
}

import { apiClient } from './client'
import type { ApiResponse } from '@/types'

// ============================================================
// v2-practical-ux: API client for new endpoints
// Mirrors src/backend/src/routes/v2.routes.ts
// ============================================================

export interface RebookSuggestion {
  bookingId: number
  venueId: number | null
  venueName: string | null
  coachId: number | null
  coachName: string | null
  sportType: string
  lastPlayedAt: string
  lastSlotStart: string
  price: number
  timesBooked: number
}

export interface PlaySuggestion {
  type: 'lobby' | 'match' | 'open_slot'
  id: number
  title: string
  sportType: string
  startAt: string
  venueName: string | null
  distanceKm: number | null
  currentPlayers: number
  maxPlayers: number
  skillLevelMin: number | null
  skillLevelMax: number | null
  price: number | null
}

export interface FriendAvailability {
  userId: number
  displayName: string | null
  photoUrl: string | null
  sportType: string
  skillLevel: number | null
  availableUntil: string | null
  distanceKm: number | null
}

export interface HomeFeedResponse {
  greeting: { displayName: string | null; reliabilityScore: number; totalAttended: number }
  recentBookings: RebookSuggestion[]
  suggestedPlay: PlaySuggestion[]
  friendsAvailable: FriendAvailability[]
  upcoming: Array<{
    id: number
    venueName: string | null
    coachName: string | null
    status: string
    totalPrice: number
    slotDate: string
    startTime: string
    endTime: string
  }>
}

export interface PlaySearchResponse {
  results: PlaySuggestion[]
  counts: { lobbies: number; openSlots: number; availablePlayers: number }
}

export interface SplitPaymentSummary {
  bookingId: number
  totalAmount: number
  paidAmount: number
  pendingAmount: number
  shares: Array<{
    id: number
    bookingId: number
    payerUserId: number
    amount: number
    currency: string
    status: 'pending' | 'awaiting' | 'paid' | 'refunded' | 'expired'
    payerName?: string | null
    payerPhotoUrl?: string | null
    expiresAt: string | null
    paidAt: string | null
  }>
}

export const v2Api = {
  // Home feed
  getHomeFeed: () =>
    apiClient.get('/home/feed') as Promise<ApiResponse<HomeFeedResponse>>,

  // Unified Play search
  searchPlay: (params: {
    from: string
    to: string
    sportType?: string
    latitude?: number
    longitude?: number
    radiusKm?: number
    skillLevelMin?: number
    skillLevelMax?: number
    onlyEligible?: boolean
  }) => {
    const qs = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') qs.append(k, String(v))
    })
    return apiClient.get(`/play/search?${qs.toString()}`) as Promise<ApiResponse<PlaySearchResponse>>
  },

  // One-tap rebook
  rebook: (bookingId: number, slotDate: string, startTime: string) =>
    apiClient.post(`/bookings/${bookingId}/rebook`, { slotDate, startTime }) as Promise<
      ApiResponse<{ id: number; rebookedFrom: number }>
    >,

  // Split payments
  createSplit: (bookingId: number, payerUserIds: number[], expiresInMinutes?: number) =>
    apiClient.post(`/bookings/${bookingId}/split`, {
      payerUserIds,
      expiresInMinutes,
    }) as Promise<ApiResponse<SplitPaymentSummary>>,

  getSplitSummary: (bookingId: number) =>
    apiClient.get(`/bookings/${bookingId}/split`) as Promise<ApiResponse<SplitPaymentSummary>>,

  paySplitShare: (shareId: number, stripePaymentIntentId: string) =>
    apiClient.post(`/split-payments/${shareId}/pay`, { stripePaymentIntentId }),

  // Booking participants
  inviteParticipants: (bookingId: number, userIds: number[]) =>
    apiClient.post(`/bookings/${bookingId}/invite`, { userIds }),

  respondToInvite: (bookingId: number, accept: boolean) =>
    apiClient.post(`/bookings/${bookingId}/respond`, { accept }),

  markAttendance: (bookingId: number, attendance: Array<{ userId: number; attended: boolean }>) =>
    apiClient.post(`/bookings/${bookingId}/attendance`, { attendance }),

  listParticipants: (bookingId: number) =>
    apiClient.get(`/bookings/${bookingId}/participants`),
}

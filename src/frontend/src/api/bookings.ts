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

export interface BookingMessage {
  id: number
  bookingId: number
  senderId: number
  senderName: string
  content: string
  createdAt: string
}

// Reservation detail (partner approval flow — landing page from email link).
// Mirrors the BookingRow shape on the backend.
export interface ReservationDetail {
  id: number
  playerId: number
  status: string
  totalPrice: number
  notes: string | null
  playerName?: string
  playerEmail?: string
  timeSlot?: { slotDate: string; startTime: string; endTime: string } | null
  venue?: { id: number; name: string; address?: string } | null
  coach?: { id: number; name: string } | null
  createdAt: string
}

export const bookingApi = {
  getCounts: () =>
    apiClient.get('/bookings/counts') as Promise<ApiResponse<BookingCountsResponse>>,

  getById: (id: number) =>
    apiClient.get(`/bookings/${id}`) as Promise<ApiResponse<ReservationDetail>>,

  approve: (id: number) =>
    apiClient.put(`/bookings/${id}/approve`) as Promise<ApiResponse<ReservationDetail>>,

  decline: (id: number) =>
    apiClient.put(`/bookings/${id}/decline`) as Promise<ApiResponse<ReservationDetail>>,

  getPartnerBookings: (status?: string) =>
    apiClient.get(status ? `/bookings/partner?status=${status}` : '/bookings/partner') as Promise<ApiResponse<ReservationDetail[]>>,

  getMessages: (bookingId: number) =>
    apiClient.get(`/bookings/${bookingId}/messages`) as Promise<ApiResponse<BookingMessage[]>>,

  sendMessage: (bookingId: number, content: string) =>
    apiClient.post(`/bookings/${bookingId}/messages`, { content }) as Promise<ApiResponse<BookingMessage>>,
}

import { apiClient } from './client'
import type { ApiResponse } from '@/types'

export interface PaymentWithBooking {
  id: number
  bookingId: number
  payerId: number
  amount: number
  currency: string
  status: 'pending' | 'completed' | 'failed' | 'refunded'
  paymentMethod: string | null
  externalPaymentId: string | null
  platformFeeAmount: number | null
  paidAt: string | null
  venueName: string | null
  coachName: string | null
  venueId: number | null
  coachId: number | null
  slotDate: string | null
  startTime: string | null
  endTime: string | null
  createdAt: string
  updatedAt: string
}

export const paymentApi = {
  /** Get all payments for the authenticated user */
  getMyPayments: () =>
    apiClient.get('/payments/mine') as Promise<ApiResponse<PaymentWithBooking[]>>,

  /** Get a specific payment by ID */
  getById: (id: number) =>
    apiClient.get(`/payments/${id}`) as Promise<ApiResponse<PaymentWithBooking>>,

  /** Get payment for a specific booking */
  getByBookingId: (bookingId: number) =>
    apiClient.get(`/payments/booking/${bookingId}`) as Promise<ApiResponse<PaymentWithBooking>>,
}

import { PaymentStatus, StripeOnboardingStatus } from '../enums';

// ============================================================
// Payment types
// ============================================================

export interface Payment {
  id: number;
  bookingId: number;
  payerId: number;
  amount: number;
  currency: string;
  status: PaymentStatus;
  paymentMethod: string | null;
  externalPaymentId: string | null;
  platformFeeAmount: number | null;
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  bookingId: number;
  paymentMethod: string;
  externalPaymentId?: string;
}

// ============================================================
// Stripe Payment Intent types
// ============================================================

export interface CreatePaymentIntentRequest {
  bookingId: number;
}

export interface PaymentIntentResponse {
  clientSecret: string;
  bookingId: number;
  paymentId: number;
  amount: number;
  currency: string;
}

export interface UpdatePaymentStatusRequest {
  stripePaymentIntentId?: string;
}

export interface PaymentWithBooking extends Payment {
  venueName: string | null;
  coachName: string | null;
  venueId: number | null;
  coachId: number | null;
  slotDate: string | null;
  startTime: string | null;
  endTime: string | null;
}

// ============================================================
// Stripe Connect types
// ============================================================

export interface StripeConnectOnboardingResponse {
  onboardingUrl: string;
  stripeAccountId: string;
}

export interface StripeAccountStatusResponse {
  stripeAccountId: string | null;
  onboardingStatus: StripeOnboardingStatus;
  payoutsEnabled: boolean;
  dashboardUrl: string | null;
}

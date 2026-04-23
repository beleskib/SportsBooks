// ============================================================
// Split Payment types — v2-practical-ux
// Allows a booking to be paid by multiple players (each their share)
// ============================================================

export type SplitPaymentStatus =
  | 'pending'
  | 'awaiting'
  | 'paid'
  | 'refunded'
  | 'expired';

export interface SplitPayment {
  id: number;
  bookingId: number;
  payerUserId: number;
  amount: number;
  currency: string;
  status: SplitPaymentStatus;
  stripePaymentIntentId: string | null;
  paidAt: string | null;
  expiresAt: string | null;
  createdAt: string;
  updatedAt: string;
  // Optional joined fields
  payerName?: string | null;
  payerPhotoUrl?: string | null;
}

export interface CreateSplitPaymentRequest {
  // Caller supplies tagged friends; backend computes equal share
  payerUserIds: number[];
  // Optional custom amounts — must sum to booking total if provided
  customAmounts?: Array<{ userId: number; amount: number }>;
  // How long each payer has to settle before share reverts to booker
  expiresInMinutes?: number;
}

export interface SplitPaymentSummary {
  bookingId: number;
  totalAmount: number;
  paidAmount: number;
  pendingAmount: number;
  shares: SplitPayment[];
}

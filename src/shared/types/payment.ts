import { PaymentStatus } from '../enums';

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
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  bookingId: number;
  paymentMethod: string;
  externalPaymentId?: string;
}

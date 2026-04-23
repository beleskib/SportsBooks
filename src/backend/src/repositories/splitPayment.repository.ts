import { query } from '../config/database';

// ============================================================
// v2-practical-ux: Split Payment repository
// One booking -> N shares, one row per payer.
// Types mirror src/shared/types/splitPayment.ts
// ============================================================

export type SplitPaymentStatus = 'pending' | 'awaiting' | 'paid' | 'refunded' | 'expired';

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
  payerName?: string | null;
  payerPhotoUrl?: string | null;
}

function mapRow(row: any): SplitPayment {
  return {
    id: Number(row.id),
    bookingId: Number(row.booking_id),
    payerUserId: Number(row.payer_user_id),
    amount: Number(row.amount),
    currency: row.currency,
    status: row.status as SplitPaymentStatus,
    stripePaymentIntentId: row.stripe_payment_intent_id ?? null,
    paidAt: row.paid_at?.toISOString?.() ?? row.paid_at,
    expiresAt: row.expires_at?.toISOString?.() ?? row.expires_at,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
    payerName: row.payer_name ?? undefined,
    payerPhotoUrl: row.payer_photo_url ?? undefined,
  };
}

export async function createShare(
  bookingId: number,
  payerUserId: number,
  amount: number,
  currency = 'EUR',
  expiresAt: Date | null = null,
): Promise<SplitPayment> {
  const result = await query(
    `INSERT INTO split_payments (booking_id, payer_user_id, amount, currency, expires_at, status)
     VALUES ($1, $2, $3, $4, $5, 'awaiting')
     ON CONFLICT (booking_id, payer_user_id)
     DO UPDATE SET amount = EXCLUDED.amount, updated_at = NOW()
     RETURNING *`,
    [bookingId, payerUserId, amount, currency, expiresAt],
  );
  return mapRow(result.rows[0]);
}

export async function findByBookingId(bookingId: number): Promise<SplitPayment[]> {
  const result = await query(
    `SELECT sp.*, u.display_name AS payer_name, u.photo_url AS payer_photo_url
     FROM split_payments sp
     JOIN users u ON u.id = sp.payer_user_id
     WHERE sp.booking_id = $1
     ORDER BY sp.id`,
    [bookingId],
  );
  return result.rows.map(mapRow);
}

export async function findById(id: number): Promise<SplitPayment | null> {
  const result = await query(
    `SELECT sp.*, u.display_name AS payer_name, u.photo_url AS payer_photo_url
     FROM split_payments sp
     JOIN users u ON u.id = sp.payer_user_id
     WHERE sp.id = $1`,
    [id],
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function markPaid(id: number, stripePaymentIntentId: string): Promise<SplitPayment> {
  const result = await query(
    `UPDATE split_payments
     SET status = 'paid',
         stripe_payment_intent_id = $2,
         paid_at = NOW(),
         updated_at = NOW()
     WHERE id = $1
     RETURNING *`,
    [id, stripePaymentIntentId],
  );
  return mapRow(result.rows[0]);
}

export async function markRefunded(id: number): Promise<SplitPayment> {
  const result = await query(
    `UPDATE split_payments
     SET status = 'refunded', updated_at = NOW()
     WHERE id = $1
     RETURNING *`,
    [id],
  );
  return mapRow(result.rows[0]);
}

export async function refundAllForBooking(bookingId: number): Promise<void> {
  await query(
    `UPDATE split_payments
     SET status = 'refunded', updated_at = NOW()
     WHERE booking_id = $1 AND status IN ('paid', 'awaiting', 'pending')`,
    [bookingId],
  );
}

export async function expireOverdue(): Promise<number> {
  const result = await query(
    `UPDATE split_payments
     SET status = 'expired', updated_at = NOW()
     WHERE status = 'awaiting' AND expires_at IS NOT NULL AND expires_at < NOW()
     RETURNING id`,
  );
  return result.rows.length;
}

export async function getSummary(bookingId: number): Promise<{
  totalAmount: number;
  paidAmount: number;
  pendingAmount: number;
}> {
  const result = await query(
    `SELECT
       COALESCE(SUM(amount), 0)                                         AS total_amount,
       COALESCE(SUM(amount) FILTER (WHERE status = 'paid'), 0)           AS paid_amount,
       COALESCE(SUM(amount) FILTER (WHERE status IN ('pending','awaiting')), 0) AS pending_amount
     FROM split_payments
     WHERE booking_id = $1`,
    [bookingId],
  );
  const row = result.rows[0];
  return {
    totalAmount: Number(row.total_amount),
    paidAmount: Number(row.paid_amount),
    pendingAmount: Number(row.pending_amount),
  };
}

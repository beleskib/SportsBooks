import { query } from '../config/database';

export interface PaymentRow {
  id: number;
  bookingId: number;
  payerId: number;
  amount: number;
  currency: string;
  status: string;
  paymentMethod: string | null;
  externalPaymentId: string | null;
  platformFeeAmount: number | null;
  stripeTransferId: string | null;
  paidAt: string | null;
  venueName?: string | null;
  coachName?: string | null;
  slotDate?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  venueId?: number | null;
  coachId?: number | null;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): PaymentRow {
  return {
    id: Number(row.id),
    bookingId: Number(row.booking_id),
    payerId: Number(row.payer_id),
    amount: Number(row.amount),
    currency: row.currency,
    status: row.status,
    paymentMethod: row.payment_method,
    externalPaymentId: row.external_payment_id,
    platformFeeAmount: row.platform_fee_amount != null ? Number(row.platform_fee_amount) : null,
    stripeTransferId: row.stripe_transfer_id ?? null,
    paidAt: row.paid_at?.toISOString?.() ?? row.paid_at,
    venueName: row.venue_name ?? null,
    coachName: row.coach_name ?? null,
    slotDate: row.slot_date instanceof Date ? row.slot_date.toISOString().split('T')[0] : (row.slot_date ?? null),
    startTime: row.start_time ?? null,
    endTime: row.end_time ?? null,
    venueId: row.venue_id ? Number(row.venue_id) : null,
    coachId: row.coach_id ? Number(row.coach_id) : null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export async function create(
  bookingId: number,
  payerId: number,
  amount: number,
  currency: string,
  externalPaymentId: string,
  platformFeeAmount?: number
): Promise<PaymentRow> {
  const result = await query(
    `INSERT INTO payments (booking_id, payer_id, amount, currency, external_payment_id, platform_fee_amount)
     VALUES ($1, $2, $3, $4, $5, $6)
     RETURNING *`,
    [bookingId, payerId, amount, currency, externalPaymentId, platformFeeAmount ?? null]
  );
  return mapRow(result.rows[0]);
}

export async function findById(id: number): Promise<PaymentRow | null> {
  const result = await query(
    `SELECT p.*, b.venue_id, b.coach_id,
            v.name AS venue_name, c.name AS coach_name,
            ts.slot_date, ts.start_time, ts.end_time
     FROM payments p
     JOIN bookings b ON b.id = p.booking_id
     LEFT JOIN venues v ON v.id = b.venue_id
     LEFT JOIN coaches c ON c.id = b.coach_id
     LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
     WHERE p.id = $1`,
    [id]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function findByBookingId(bookingId: number): Promise<PaymentRow | null> {
  const result = await query(
    `SELECT p.*, b.venue_id, b.coach_id,
            v.name AS venue_name, c.name AS coach_name,
            ts.slot_date, ts.start_time, ts.end_time
     FROM payments p
     JOIN bookings b ON b.id = p.booking_id
     LEFT JOIN venues v ON v.id = b.venue_id
     LEFT JOIN coaches c ON c.id = b.coach_id
     LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
     WHERE p.booking_id = $1`,
    [bookingId]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function findByExternalId(externalPaymentId: string): Promise<PaymentRow | null> {
  const result = await query(
    `SELECT p.*, b.venue_id, b.coach_id,
            v.name AS venue_name, c.name AS coach_name,
            ts.slot_date, ts.start_time, ts.end_time
     FROM payments p
     JOIN bookings b ON b.id = p.booking_id
     LEFT JOIN venues v ON v.id = b.venue_id
     LEFT JOIN coaches c ON c.id = b.coach_id
     LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
     WHERE p.external_payment_id = $1`,
    [externalPaymentId]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function findByPayerId(payerId: number): Promise<PaymentRow[]> {
  const result = await query(
    `SELECT p.*, b.venue_id, b.coach_id,
            v.name AS venue_name, c.name AS coach_name,
            ts.slot_date, ts.start_time, ts.end_time
     FROM payments p
     JOIN bookings b ON b.id = p.booking_id
     LEFT JOIN venues v ON v.id = b.venue_id
     LEFT JOIN coaches c ON c.id = b.coach_id
     LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
     WHERE p.payer_id = $1
     ORDER BY p.created_at DESC`,
    [payerId]
  );
  return result.rows.map(mapRow);
}

export async function updateStatus(
  id: number,
  status: string,
  paidAt?: string
): Promise<PaymentRow> {
  const result = await query(
    `UPDATE payments
     SET status = $2, paid_at = $3, updated_at = NOW()
     WHERE id = $1
     RETURNING *`,
    [id, status, paidAt || null]
  );
  return mapRow(result.rows[0]);
}

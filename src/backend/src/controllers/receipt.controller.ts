import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import { query } from '../config/database';
import { NotFoundError, ForbiddenError } from '../utils/errors';

export async function getBookingReceipt(req: Request, res: Response, next: NextFunction) {
  try {
    const bookingId = Number(req.params.id);
    const userId = req.user!.id;
    const userRole = req.user!.role;

    const result = await query(
      `SELECT
        b.id AS booking_id, b.total_price, b.notes, b.status AS booking_status,
        b.player_id, b.venue_id AS b_venue_id, b.coach_id AS b_coach_id,
        p.id AS payment_id, p.amount, p.currency, p.status AS payment_status,
        p.payment_method, p.external_payment_id, p.platform_fee_amount, p.paid_at, p.created_at AS payment_created_at,
        ts.slot_date, ts.start_time, ts.end_time,
        v.id AS venue_id, v.name AS venue_name, v.address AS venue_address, v.sport_type AS venue_sport, v.owner_id AS venue_owner_id,
        c.id AS coach_id, c.name AS coach_name, c.address AS coach_address, c.sport_type AS coach_sport, c.user_id AS coach_user_id,
        u.display_name AS player_name, u.email AS player_email
      FROM bookings b
      JOIN payments p ON p.booking_id = b.id
      LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
      LEFT JOIN venues v ON v.id = b.venue_id
      LEFT JOIN coaches c ON c.id = b.coach_id
      JOIN users u ON u.id = b.player_id
      WHERE b.id = $1 AND p.status = 'completed'`,
      [bookingId]
    );

    if (result.rows.length === 0) {
      throw new NotFoundError('Receipt not available — payment not completed');
    }

    const row = result.rows[0];

    // Verify access: player, venue owner, coach, or admin
    const isPlayer = Number(row.player_id) === userId;
    const isVenueOwner = row.venue_owner_id != null && Number(row.venue_owner_id) === userId;
    const isCoach = row.coach_user_id != null && Number(row.coach_user_id) === userId;
    const isAdmin = userRole === 'admin';

    if (!isPlayer && !isVenueOwner && !isCoach && !isAdmin) {
      throw new ForbiddenError();
    }

    const slotDate = row.slot_date instanceof Date
      ? row.slot_date.toISOString().split('T')[0]
      : row.slot_date;

    const receipt = {
      receiptNumber: `SB-${row.booking_id}-${row.payment_id}`,
      bookingId: Number(row.booking_id),
      paymentId: Number(row.payment_id),
      status: row.payment_status,
      issuedAt: row.paid_at
        ? (row.paid_at instanceof Date ? row.paid_at.toISOString() : row.paid_at)
        : new Date().toISOString(),

      // Player
      playerName: row.player_name,
      playerEmail: row.player_email,

      // Provider
      providerType: row.venue_name ? 'venue' as const : 'coach' as const,
      providerName: row.venue_name || row.coach_name,
      providerAddress: row.venue_address || row.coach_address || null,

      // Booking details
      sportType: row.venue_sport || row.coach_sport,
      slotDate,
      startTime: row.start_time,
      endTime: row.end_time,

      // Financials
      subtotal: Number(row.amount),
      platformFee: row.platform_fee_amount ? Number(row.platform_fee_amount) : 0,
      total: Number(row.total_price),
      currency: row.currency,
      paymentMethod: row.payment_method || null,
      externalPaymentId: row.external_payment_id || null,
    };

    success(res, receipt);
  } catch (e) {
    next(e);
  }
}

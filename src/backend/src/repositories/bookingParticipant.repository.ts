import { query } from '../config/database';

// ============================================================
// v2-practical-ux: Booking Participant repository
// Tagged friends on a booking, tracked through invite -> attendance
// Types mirror src/shared/types/bookingParticipant.ts
// ============================================================

export type BookingParticipantStatus =
  | 'invited'
  | 'accepted'
  | 'declined'
  | 'attended'
  | 'no_show';

export interface BookingParticipant {
  id: number;
  bookingId: number;
  userId: number;
  status: BookingParticipantStatus;
  splitPaymentId: number | null;
  respondedAt: string | null;
  attendedAt: string | null;
  createdAt: string;
  updatedAt: string;
  displayName?: string | null;
  photoUrl?: string | null;
}

function mapRow(row: any): BookingParticipant {
  return {
    id: Number(row.id),
    bookingId: Number(row.booking_id),
    userId: Number(row.user_id),
    status: row.status as BookingParticipantStatus,
    splitPaymentId: row.split_payment_id ? Number(row.split_payment_id) : null,
    respondedAt: row.responded_at?.toISOString?.() ?? row.responded_at,
    attendedAt: row.attended_at?.toISOString?.() ?? row.attended_at,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
    displayName: row.display_name ?? undefined,
    photoUrl: row.photo_url ?? undefined,
  };
}

export async function inviteMany(
  bookingId: number,
  userIds: number[],
): Promise<BookingParticipant[]> {
  if (userIds.length === 0) return [];

  const valuesSql = userIds.map((_, i) => `($1, $${i + 2})`).join(', ');
  const result = await query(
    `INSERT INTO booking_participants (booking_id, user_id)
     VALUES ${valuesSql}
     ON CONFLICT (booking_id, user_id) DO NOTHING
     RETURNING *`,
    [bookingId, ...userIds],
  );
  return result.rows.map(mapRow);
}

export async function findByBookingId(bookingId: number): Promise<BookingParticipant[]> {
  const result = await query(
    `SELECT bp.*, u.display_name, u.photo_url
     FROM booking_participants bp
     JOIN users u ON u.id = bp.user_id
     WHERE bp.booking_id = $1
     ORDER BY bp.created_at`,
    [bookingId],
  );
  return result.rows.map(mapRow);
}

export async function findByUserId(userId: number, limit = 20): Promise<BookingParticipant[]> {
  const result = await query(
    `SELECT bp.*, u.display_name, u.photo_url
     FROM booking_participants bp
     JOIN users u ON u.id = bp.user_id
     WHERE bp.user_id = $1
     ORDER BY bp.created_at DESC
     LIMIT $2`,
    [userId, limit],
  );
  return result.rows.map(mapRow);
}

export async function respond(
  bookingId: number,
  userId: number,
  accept: boolean,
): Promise<BookingParticipant | null> {
  const result = await query(
    `UPDATE booking_participants
     SET status = $3, responded_at = NOW(), updated_at = NOW()
     WHERE booking_id = $1 AND user_id = $2
     RETURNING *`,
    [bookingId, userId, accept ? 'accepted' : 'declined'],
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function markAttendance(
  bookingId: number,
  attendance: Array<{ userId: number; attended: boolean }>,
): Promise<BookingParticipant[]> {
  if (attendance.length === 0) return [];

  const results: BookingParticipant[] = [];
  for (const { userId, attended } of attendance) {
    const result = await query(
      `UPDATE booking_participants
       SET status = $3,
           attended_at = CASE WHEN $3 = 'attended' THEN NOW() ELSE attended_at END,
           updated_at = NOW()
       WHERE booking_id = $1 AND user_id = $2
       RETURNING *`,
      [bookingId, userId, attended ? 'attended' : 'no_show'],
    );
    if (result.rows.length > 0) results.push(mapRow(result.rows[0]));
  }

  // Update user-level aggregates for reputation
  for (const { userId, attended } of attendance) {
    if (attended) {
      await query(`UPDATE users SET total_attended = total_attended + 1, updated_at = NOW() WHERE id = $1`, [userId]);
    } else {
      await query(`UPDATE users SET no_show_count = no_show_count + 1, updated_at = NOW() WHERE id = $1`, [userId]);
    }
  }

  return results;
}

export async function linkSplitPayment(
  bookingId: number,
  userId: number,
  splitPaymentId: number,
): Promise<void> {
  await query(
    `UPDATE booking_participants
     SET split_payment_id = $3, updated_at = NOW()
     WHERE booking_id = $1 AND user_id = $2`,
    [bookingId, userId, splitPaymentId],
  );
}

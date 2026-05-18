import { query, pool } from '../config/database';
import { NotFoundError } from '../utils/errors';

export interface BookingRow {
  id: number;
  playerId: number;
  timeSlotId: number | null;
  venueId: number | null;
  coachId: number | null;
  status: string;
  totalPrice: number;
  notes: string | null;
  expiresAt: string | null;
  playerName?: string;
  playerEmail?: string;
  timeSlot?: {
    id: number | null;
    slotDate: string;
    startTime: string;
    endTime: string;
    venueId?: number | null;
    coachId?: number | null;
    isAvailable?: boolean;
    priceOverride?: number | null;
  } | null;
  venue?: { id: number; name: string; sportType?: string; address?: string; pricePerHour?: number } | null;
  coach?: { id: number; name: string; sportType?: string; pricePerHour?: number } | null;
  isParticipant?: boolean;
  matchId?: number | null;
  matchTitle?: string | null;
  matchStatus?: string | null;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): BookingRow {
  const slotDate = row.slot_date instanceof Date
    ? row.slot_date.toISOString().split('T')[0]
    : row.slot_date;

  return {
    id: Number(row.id ?? row.booking_id),
    playerId: Number(row.player_id),
    timeSlotId: row.time_slot_id ? Number(row.time_slot_id) : null,
    venueId: row.venue_id ? Number(row.venue_id) : null,
    coachId: row.coach_id ? Number(row.coach_id) : null,
    status: row.status,
    totalPrice: Number(row.total_price),
    notes: row.notes,
    expiresAt: row.expires_at?.toISOString?.() ?? row.expires_at ?? null,
    playerName: row.player_name ?? row.display_name,
    playerEmail: row.player_email ?? row.email,
    timeSlot: (slotDate || row.start_time || row.end_time) ? {
      id: row.time_slot_id ? Number(row.time_slot_id) : null,
      slotDate: slotDate ?? '',
      startTime: row.start_time ?? '',
      endTime: row.end_time ?? '',
      venueId: row.venue_id ? Number(row.venue_id) : null,
      coachId: row.coach_id ? Number(row.coach_id) : null,
      isAvailable: false,
      priceOverride: null,
    } : null,
    venue: (row.venue_id && row.venue_name) ? {
      id: Number(row.venue_id),
      name: row.venue_name,
      sportType: row.venue_sport_type,
      address: row.venue_address,
      pricePerHour: row.venue_price_per_hour ? Number(row.venue_price_per_hour) : undefined,
    } : null,
    coach: (row.coach_id && row.coach_name) ? {
      id: Number(row.coach_id),
      name: row.coach_name,
      sportType: row.coach_sport_type,
      pricePerHour: row.coach_price_per_hour ? Number(row.coach_price_per_hour) : undefined,
    } : null,
    ...(row.is_participant !== undefined ? { isParticipant: row.is_participant } : {}),
    ...(row.match_id !== undefined ? {
      matchId: row.match_id ? Number(row.match_id) : null,
      matchTitle: row.match_title ?? null,
      matchStatus: row.match_status ?? null,
    } : {}),
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export interface BookingCountRow {
  entityId: number;
  entityType: 'venue' | 'coach';
  totalBookings: number;
  confirmedBookings: number;
  completedBookings: number;
}

export async function getVenueBookingCounts(): Promise<BookingCountRow[]> {
  const result = await query(
    `SELECT venue_id AS entity_id,
            COUNT(*) AS total_bookings,
            COUNT(*) FILTER (WHERE status = 'confirmed') AS confirmed_bookings,
            COUNT(*) FILTER (WHERE status = 'completed') AS completed_bookings
     FROM bookings
     WHERE venue_id IS NOT NULL
     GROUP BY venue_id`
  );
  return result.rows.map((r: any) => ({
    entityId: r.entity_id,
    entityType: 'venue' as const,
    totalBookings: Number(r.total_bookings),
    confirmedBookings: Number(r.confirmed_bookings),
    completedBookings: Number(r.completed_bookings),
  }));
}

export async function getCoachBookingCounts(): Promise<BookingCountRow[]> {
  const result = await query(
    `SELECT coach_id AS entity_id,
            COUNT(*) AS total_bookings,
            COUNT(*) FILTER (WHERE status = 'confirmed') AS confirmed_bookings,
            COUNT(*) FILTER (WHERE status = 'completed') AS completed_bookings
     FROM bookings
     WHERE coach_id IS NOT NULL
     GROUP BY coach_id`
  );
  return result.rows.map((r: any) => ({
    entityId: r.entity_id,
    entityType: 'coach' as const,
    totalBookings: Number(r.total_bookings),
    confirmedBookings: Number(r.confirmed_bookings),
    completedBookings: Number(r.completed_bookings),
  }));
}

export async function create(playerId: number, timeSlotId: number, notes?: string): Promise<BookingRow> {
  const result = await query(
    `SELECT * FROM create_booking($1, $2, $3)`,
    [playerId, timeSlotId, notes || null]
  );
  const createdRow = result.rows[0];
  // Re-fetch with full joins to get venue/coach/timeSlot nested data
  const booking = await findById(createdRow.booking_id ?? createdRow.id);
  if (!booking) throw new NotFoundError('Booking');
  return booking;
}

export async function findByPlayerId(
  playerId: number,
  status?: string,
  options: { page?: number; limit?: number } = {}
): Promise<{ data: BookingRow[]; total: number; page: number; limit: number }> {
  const page = options.page || 1;
  const limit = Math.min(options.limit || 20, 100);
  const offset = (page - 1) * limit;

  let sql = `
    SELECT b.id, b.player_id, b.time_slot_id, b.venue_id, b.coach_id,
           b.status, b.total_price, b.notes, b.expires_at, b.created_at, b.updated_at,
           v.name AS venue_name, v.sport_type AS venue_sport_type,
           v.address AS venue_address, v.price_per_hour AS venue_price_per_hour,
           c.name AS coach_name, c.sport_type AS coach_sport_type,
           c.price_per_hour AS coach_price_per_hour,
           ts.slot_date, ts.start_time, ts.end_time,
           CASE WHEN b.player_id = $1 THEN false ELSE true END AS is_participant,
           m.id AS match_id, m.title AS match_title, m.status AS match_status,
           COUNT(*) OVER() AS total_count
    FROM bookings b
    LEFT JOIN venues v ON v.id = b.venue_id
    LEFT JOIN coaches c ON c.id = b.coach_id
    LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
    LEFT JOIN matches m ON m.booking_id = b.id
    WHERE (b.player_id = $1 OR EXISTS (
      SELECT 1 FROM booking_participants bp
      WHERE bp.booking_id = b.id AND bp.user_id = $1 AND bp.status IN ('invited', 'accepted', 'attended')
    ))`;
  const params: any[] = [playerId];
  let paramIndex = 2;

  if (status) {
    sql += ` AND b.status = $${paramIndex}`;
    params.push(status);
    paramIndex++;
  }
  sql += ` ORDER BY b.created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
  params.push(limit, offset);

  const result = await query(sql, params);
  const total = result.rows.length > 0 ? Number(result.rows[0].total_count) : 0;
  return { data: result.rows.map(mapRow), total, page, limit };
}

export async function findById(id: number): Promise<BookingRow | null> {
  const result = await query(
    `SELECT b.id, b.player_id, b.time_slot_id, b.venue_id, b.coach_id,
            b.status, b.total_price, b.notes, b.expires_at, b.created_at, b.updated_at,
            u.display_name AS player_name, u.email AS player_email,
            v.name AS venue_name, v.sport_type AS venue_sport_type,
            v.address AS venue_address, v.price_per_hour AS venue_price_per_hour,
            c.name AS coach_name, c.sport_type AS coach_sport_type,
            c.price_per_hour AS coach_price_per_hour,
            ts.slot_date, ts.start_time, ts.end_time
     FROM bookings b
     LEFT JOIN users u ON u.id = b.player_id
     LEFT JOIN venues v ON v.id = b.venue_id
     LEFT JOIN coaches c ON c.id = b.coach_id
     LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
     WHERE b.id = $1`,
    [id]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function updateStatus(id: number, status: string): Promise<BookingRow> {
  await query(
    `UPDATE bookings SET status = $2, updated_at = NOW() WHERE id = $1`,
    [id, status]
  );
  const booking = await findById(id);
  if (!booking) throw new NotFoundError('Booking');
  return booking;
}

export async function findByPartner(
  userId: number,
  status?: string,
  options: { page?: number; limit?: number } = {}
): Promise<{ data: BookingRow[]; total: number; page: number; limit: number }> {
  const page = options.page || 1;
  const limit = Math.min(options.limit || 20, 100);
  const offset = (page - 1) * limit;

  let sql = `
    SELECT b.id, b.player_id, b.time_slot_id, b.venue_id, b.coach_id,
           b.status, b.total_price, b.notes, b.expires_at, b.created_at, b.updated_at,
           u.display_name AS player_name, u.email AS player_email,
           v.name AS venue_name, v.sport_type AS venue_sport_type,
           v.address AS venue_address, v.price_per_hour AS venue_price_per_hour,
           c.name AS coach_name, c.sport_type AS coach_sport_type,
           c.price_per_hour AS coach_price_per_hour,
           ts.slot_date, ts.start_time, ts.end_time,
           COUNT(*) OVER() AS total_count
    FROM bookings b
    LEFT JOIN users u ON u.id = b.player_id
    LEFT JOIN venues v ON v.id = b.venue_id
    LEFT JOIN coaches c ON c.id = b.coach_id
    LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
    WHERE (v.owner_id = $1 OR c.user_id = $1)`;
  const params: any[] = [userId];
  let paramIndex = 2;

  if (status) {
    sql += ` AND b.status = $${paramIndex}`;
    params.push(status);
    paramIndex++;
  }
  sql += ` ORDER BY b.created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
  params.push(limit, offset);

  const result = await query(sql, params);
  const total = result.rows.length > 0 ? Number(result.rows[0].total_count) : 0;
  return { data: result.rows.map(mapRow), total, page, limit };
}

export async function approveBooking(id: number): Promise<BookingRow> {
  const result = await query(
    `UPDATE bookings SET status = 'approved', expires_at = NULL, updated_at = NOW()
     WHERE id = $1 AND status = 'pending'
     RETURNING *`,
    [id]
  );
  if (result.rows.length === 0) throw new NotFoundError('Booking not found or not in pending status');
  // Re-fetch with joins to get full data
  const booking = await findById(id);
  if (!booking) throw new NotFoundError('Booking');
  return booking;
}

export async function declineBooking(id: number): Promise<BookingRow> {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const result = await client.query(
      `UPDATE bookings SET status = 'cancelled', expires_at = NULL, updated_at = NOW()
       WHERE id = $1 AND status = 'pending'
       RETURNING time_slot_id`,
      [id]
    );
    if (result.rows.length === 0) {
      await client.query('ROLLBACK');
      throw new NotFoundError('Booking not found or not in pending status');
    }

    // Reopen the time slot
    await client.query(
      'UPDATE time_slots SET is_available = true, updated_at = NOW() WHERE id = $1',
      [result.rows[0].time_slot_id]
    );

    await client.query('COMMIT');
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }

  const booking = await findById(id);
  if (!booking) throw new NotFoundError('Booking');
  return booking;
}

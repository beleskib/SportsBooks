import { query } from '../config/database';

export interface BookingRow {
  id: number;
  playerId: number;
  timeSlotId: number;
  venueId: number | null;
  coachId: number | null;
  status: string;
  totalPrice: number;
  notes: string | null;
  playerName?: string;
  playerEmail?: string;
  venueName?: string;
  coachName?: string;
  slotDate?: string;
  startTime?: string;
  endTime?: string;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): BookingRow {
  return {
    id: row.id ?? row.booking_id,
    playerId: row.player_id,
    timeSlotId: row.time_slot_id,
    venueId: row.venue_id,
    coachId: row.coach_id,
    status: row.status,
    totalPrice: Number(row.total_price),
    notes: row.notes,
    playerName: row.player_name ?? row.display_name,
    playerEmail: row.player_email ?? row.email,
    venueName: row.venue_name,
    coachName: row.coach_name,
    slotDate: row.slot_date instanceof Date ? row.slot_date.toISOString().split('T')[0] : row.slot_date,
    startTime: row.start_time,
    endTime: row.end_time,
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
  return mapRow(result.rows[0]);
}

export async function findByPlayerId(playerId: number, status?: string): Promise<BookingRow[]> {
  let sql = `
    SELECT b.id, b.player_id, b.time_slot_id, b.venue_id, b.coach_id,
           b.status, b.total_price, b.notes, b.created_at, b.updated_at,
           v.name AS venue_name, c.name AS coach_name,
           ts.slot_date, ts.start_time, ts.end_time
    FROM bookings b
    LEFT JOIN venues v ON v.id = b.venue_id
    LEFT JOIN coaches c ON c.id = b.coach_id
    LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
    WHERE b.player_id = $1`;
  const params: any[] = [playerId];

  if (status) {
    sql += ` AND b.status = $2`;
    params.push(status);
  }
  sql += ` ORDER BY b.created_at DESC`;

  const result = await query(sql, params);
  return result.rows.map(mapRow);
}

export async function findById(id: number): Promise<BookingRow | null> {
  const result = await query(
    `SELECT b.id, b.player_id, b.time_slot_id, b.venue_id, b.coach_id,
            b.status, b.total_price, b.notes, b.created_at, b.updated_at,
            u.display_name AS player_name, u.email AS player_email,
            v.name AS venue_name, c.name AS coach_name,
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
  const result = await query(
    `UPDATE bookings SET status = $2 WHERE id = $1
     RETURNING id, player_id, time_slot_id, venue_id, coach_id,
               status, total_price, notes, created_at, updated_at`,
    [id, status]
  );
  return mapRow(result.rows[0]);
}

export async function findByPartner(userId: number, status?: string): Promise<BookingRow[]> {
  let sql = `
    SELECT b.id, b.player_id, b.time_slot_id, b.venue_id, b.coach_id,
           b.status, b.total_price, b.notes, b.created_at, b.updated_at,
           u.display_name AS player_name, u.email AS player_email,
           v.name AS venue_name, c.name AS coach_name,
           ts.slot_date, ts.start_time, ts.end_time
    FROM bookings b
    LEFT JOIN users u ON u.id = b.player_id
    LEFT JOIN venues v ON v.id = b.venue_id
    LEFT JOIN coaches c ON c.id = b.coach_id
    LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
    WHERE (v.owner_id = $1 OR c.user_id = $1)`;
  const params: any[] = [userId];

  if (status) {
    sql += ` AND b.status = $2`;
    params.push(status);
  }
  sql += ` ORDER BY b.created_at DESC`;

  const result = await query(sql, params);
  return result.rows.map(mapRow);
}

import { query } from '../config/database';

export interface TimeSlotRow {
  id: number;
  venueId: number | null;
  coachId: number | null;
  slotDate: string;
  startTime: string;
  endTime: string;
  isAvailable: boolean;
  priceOverride: number | null;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): TimeSlotRow {
  return {
    id: row.id,
    venueId: row.venue_id,
    coachId: row.coach_id,
    slotDate: row.slot_date instanceof Date ? row.slot_date.toISOString().split('T')[0] : row.slot_date,
    startTime: row.start_time,
    endTime: row.end_time,
    isAvailable: row.is_available,
    priceOverride: row.price_override != null ? Number(row.price_override) : null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export async function findByVenue(venueId: number, dateFrom: string, dateTo: string): Promise<TimeSlotRow[]> {
  const result = await query(
    `SELECT id, venue_id, coach_id, slot_date, start_time, end_time,
            is_available, price_override, created_at, updated_at
     FROM time_slots
     WHERE venue_id = $1 AND slot_date >= $2 AND slot_date <= $3
     ORDER BY slot_date, start_time`,
    [venueId, dateFrom, dateTo]
  );
  return result.rows.map(mapRow);
}

export async function findByCoach(coachId: number, dateFrom: string, dateTo: string): Promise<TimeSlotRow[]> {
  const result = await query(
    `SELECT id, venue_id, coach_id, slot_date, start_time, end_time,
            is_available, price_override, created_at, updated_at
     FROM time_slots
     WHERE coach_id = $1 AND slot_date >= $2 AND slot_date <= $3
     ORDER BY slot_date, start_time`,
    [coachId, dateFrom, dateTo]
  );
  return result.rows.map(mapRow);
}

export async function findById(id: number): Promise<TimeSlotRow | null> {
  const result = await query(
    `SELECT id, venue_id, coach_id, slot_date, start_time, end_time,
            is_available, price_override, created_at, updated_at
     FROM time_slots WHERE id = $1`,
    [id]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function createBatch(slots: {
  venueId?: number;
  coachId?: number;
  slotDate: string;
  startTime: string;
  endTime: string;
}[]): Promise<TimeSlotRow[]> {
  if (slots.length === 0) return [];

  // Build a multi-row INSERT
  const values: any[] = [];
  const placeholders: string[] = [];
  let idx = 1;

  for (const slot of slots) {
    placeholders.push(`($${idx}, $${idx + 1}, $${idx + 2}, $${idx + 3}, $${idx + 4})`);
    values.push(
      slot.venueId || null,
      slot.coachId || null,
      slot.slotDate,
      slot.startTime,
      slot.endTime
    );
    idx += 5;
  }

  const result = await query(
    `INSERT INTO time_slots (venue_id, coach_id, slot_date, start_time, end_time)
     VALUES ${placeholders.join(', ')}
     ON CONFLICT DO NOTHING
     RETURNING id, venue_id, coach_id, slot_date, start_time, end_time,
               is_available, price_override, created_at, updated_at`,
    values
  );
  return result.rows.map(mapRow);
}

export async function deleteById(id: number): Promise<boolean> {
  const result = await query('DELETE FROM time_slots WHERE id = $1', [id]);
  return (result.rowCount ?? 0) > 0;
}

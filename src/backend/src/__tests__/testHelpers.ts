import request from 'supertest';
import app from '../index';
import { pool, query } from '../config/database';

export const agent = () => request(app);

export function authHeader(firebaseUid: string) {
  return { Authorization: `Bearer ${firebaseUid}` };
}

let testUserCounter = 0;

export async function createTestUser(overrides: {
  firebaseUid?: string;
  email?: string;
  displayName?: string;
  role?: string;
  partnerType?: string | null;
} = {}) {
  testUserCounter++;
  const uid = overrides.firebaseUid || `test-uid-${testUserCounter}-${Date.now()}`;
  const email = overrides.email || `test${testUserCounter}-${Date.now()}@test.com`;
  const displayName = overrides.displayName || `Test User ${testUserCounter}`;
  const role = overrides.role || 'player';
  const partnerType = overrides.partnerType ?? null;

  const result = await query(
    `INSERT INTO users (firebase_uid, email, display_name, role, partner_type)
     VALUES ($1, $2, $3, $4, $5)
     RETURNING id, firebase_uid, email, display_name, role, partner_type`,
    [uid, email, displayName, role, partnerType]
  );

  const row = result.rows[0];
  return {
    id: Number(row.id),
    firebaseUid: row.firebase_uid as string,
    email: row.email as string,
    displayName: row.display_name as string,
    role: row.role as string,
    partnerType: row.partner_type as string | null,
  };
}

export async function createTestVenue(ownerId: number, overrides: Record<string, any> = {}) {
  const result = await query(
    `INSERT INTO venues (owner_id, name, sport_type, price_per_hour, address, description, city, country)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
     RETURNING *`,
    [
      ownerId,
      overrides.name || `Test Venue ${Date.now()}`,
      overrides.sportType || 'basketball',
      overrides.pricePerHour || 500,
      overrides.address || '123 Test St',
      overrides.description || 'A test venue',
      overrides.city || 'Skopje',
      overrides.country || 'MK',
    ]
  );
  return result.rows[0];
}

export async function createTestTimeSlot(
  venueId: number | null,
  coachId: number | null,
  overrides: Record<string, any> = {}
) {
  const result = await query(
    `INSERT INTO time_slots (venue_id, coach_id, slot_date, start_time, end_time, is_available, price_override)
     VALUES ($1, $2, $3, $4, $5, $6, $7)
     RETURNING *`,
    [
      venueId,
      coachId,
      overrides.slotDate || '2026-11-01',
      overrides.startTime || '10:00',
      overrides.endTime || '11:00',
      overrides.isAvailable ?? true,
      overrides.priceOverride ?? null,
    ]
  );
  return result.rows[0];
}

export async function removeTestUser(userId: number) {
  await query('DELETE FROM users WHERE id = $1', [userId]);
}

export async function removeTestVenue(venueId: number) {
  await query('DELETE FROM time_slots WHERE venue_id = $1', [venueId]);
  await query('DELETE FROM venues WHERE id = $1', [venueId]);
}

export async function removeTestBooking(bookingId: number) {
  await query('DELETE FROM payments WHERE booking_id = $1', [bookingId]);
  await query('DELETE FROM bookings WHERE id = $1', [bookingId]);
}

export async function closePool() {
  await pool.end();
}

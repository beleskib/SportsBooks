import { query } from '../config/database';

// ── Row interfaces ──────────────────────────────────────────────────

export interface BookingMessageRow {
  id: number;
  bookingId: number;
  senderId: number;
  senderName: string;
  message: string;
  createdAt: string;
}

// ── Row mapper ──────────────────────────────────────────────────────

function mapRow(row: any): BookingMessageRow {
  return {
    id: row.id,
    bookingId: row.booking_id,
    senderId: row.sender_id,
    senderName: row.sender_name ?? row.display_name ?? '',
    message: row.message,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

// ── Queries ─────────────────────────────────────────────────────────

export async function getMessages(bookingId: number): Promise<BookingMessageRow[]> {
  const result = await query(
    `SELECT bm.id, bm.booking_id, bm.sender_id, bm.message, bm.created_at,
            u.display_name AS sender_name
     FROM booking_messages bm
     JOIN users u ON u.id = bm.sender_id
     WHERE bm.booking_id = $1
     ORDER BY bm.created_at ASC`,
    [bookingId]
  );
  return result.rows.map(mapRow);
}

export async function sendMessage(
  bookingId: number,
  senderId: number,
  message: string
): Promise<BookingMessageRow> {
  const result = await query(
    `INSERT INTO booking_messages (booking_id, sender_id, message)
     VALUES ($1, $2, $3)
     RETURNING id, booking_id, sender_id, message, created_at`,
    [bookingId, senderId, message]
  );

  // Fetch with sender name
  const full = await query(
    `SELECT bm.id, bm.booking_id, bm.sender_id, bm.message, bm.created_at,
            u.display_name AS sender_name
     FROM booking_messages bm
     JOIN users u ON u.id = bm.sender_id
     WHERE bm.id = $1`,
    [result.rows[0].id]
  );
  return mapRow(full.rows[0]);
}

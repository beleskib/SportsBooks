import { query, pool } from '../config/database';

export interface ReviewRow {
  id: number;
  playerId: number;
  venueId: number | null;
  coachId: number | null;
  bookingId: number | null;
  rating: number;
  comment: string | null;
  playerName: string | null;
  playerPhotoUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): ReviewRow {
  return {
    id: Number(row.id),
    playerId: Number(row.player_id),
    venueId: row.venue_id ? Number(row.venue_id) : null,
    coachId: row.coach_id ? Number(row.coach_id) : null,
    bookingId: row.booking_id ? Number(row.booking_id) : null,
    rating: row.rating,
    comment: row.comment,
    playerName: row.player_name ?? row.display_name,
    playerPhotoUrl: row.player_photo_url ?? row.photo_url,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export async function findByVenueId(venueId: number): Promise<ReviewRow[]> {
  const result = await query(
    `SELECT r.id, r.player_id, r.venue_id, r.coach_id, r.booking_id,
            r.rating, r.comment, r.created_at, r.updated_at,
            u.display_name AS player_name, u.photo_url AS player_photo_url
     FROM reviews r
     LEFT JOIN users u ON u.id = r.player_id
     WHERE r.venue_id = $1
     ORDER BY r.created_at DESC`,
    [venueId]
  );
  return result.rows.map(mapRow);
}

export async function findByCoachId(coachId: number): Promise<ReviewRow[]> {
  const result = await query(
    `SELECT r.id, r.player_id, r.venue_id, r.coach_id, r.booking_id,
            r.rating, r.comment, r.created_at, r.updated_at,
            u.display_name AS player_name, u.photo_url AS player_photo_url
     FROM reviews r
     LEFT JOIN users u ON u.id = r.player_id
     WHERE r.coach_id = $1
     ORDER BY r.created_at DESC`,
    [coachId]
  );
  return result.rows.map(mapRow);
}

export async function create(data: {
  playerId: number;
  venueId?: number;
  coachId?: number;
  bookingId?: number;
  rating: number;
  comment?: string;
}): Promise<ReviewRow> {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const result = await client.query(
      `INSERT INTO reviews (player_id, venue_id, coach_id, booking_id, rating, comment)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING id, player_id, venue_id, coach_id, booking_id, rating, comment, created_at, updated_at`,
      [data.playerId, data.venueId || null, data.coachId || null, data.bookingId || null, data.rating, data.comment || null]
    );

    // Update avg_rating and total_reviews
    if (data.venueId) {
      await client.query(
        `UPDATE venues SET
           avg_rating = (SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE venue_id = $1),
           total_reviews = (SELECT COUNT(*) FROM reviews WHERE venue_id = $1)
         WHERE id = $1`,
        [data.venueId]
      );
    }
    if (data.coachId) {
      await client.query(
        `UPDATE coaches SET
           avg_rating = (SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE coach_id = $1),
           total_reviews = (SELECT COUNT(*) FROM reviews WHERE coach_id = $1)
         WHERE id = $1`,
        [data.coachId]
      );
    }

    await client.query('COMMIT');
    return mapRow(result.rows[0]);
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }
}

export async function findByPlayerId(playerId: number): Promise<ReviewRow[]> {
  const result = await query(
    `SELECT r.id, r.player_id, r.venue_id, r.coach_id, r.booking_id,
            r.rating, r.comment, r.created_at, r.updated_at,
            u.display_name AS player_name, u.photo_url AS player_photo_url
     FROM reviews r
     LEFT JOIN users u ON u.id = r.player_id
     WHERE r.player_id = $1
     ORDER BY r.created_at DESC`,
    [playerId]
  );
  return result.rows.map(mapRow);
}

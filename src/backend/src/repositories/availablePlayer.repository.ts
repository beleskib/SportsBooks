import { query } from '../config/database';

// ============================================================
// Row interface
// ============================================================

export interface AvailablePlayerRow {
  id: string;
  userId: string;
  sportType: string;
  skillLevel: number | null;
  note: string | null;
  latitude: number | null;
  longitude: number | null;
  availableUntil: string | null;
  createdAt: string;
  updatedAt: string;
  // Joined fields
  displayName?: string;
  photoUrl?: string;
  email?: string;
}

// ============================================================
// Row mapping
// ============================================================

function mapRow(row: any): AvailablePlayerRow {
  return {
    id: row.id,
    userId: row.user_id,
    sportType: row.sport_type,
    skillLevel: row.skill_level ?? null,
    note: row.note ?? null,
    latitude: row.latitude ?? null,
    longitude: row.longitude ?? null,
    availableUntil: row.available_until ?? null,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    displayName: row.display_name ?? undefined,
    photoUrl: row.photo_url ?? undefined,
    email: row.email ?? undefined,
  };
}

// ============================================================
// Repository functions
// ============================================================

export async function register(
  userId: number,
  sportType: string,
  skillLevel?: number,
  note?: string,
  latitude?: number,
  longitude?: number,
  availableUntil?: string
): Promise<AvailablePlayerRow> {
  const result = await query(
    `INSERT INTO available_players (user_id, sport_type, skill_level, note, latitude, longitude, available_until)
     VALUES ($1, $2, $3, $4, $5, $6, $7)
     ON CONFLICT (user_id, sport_type)
     DO UPDATE SET skill_level = EXCLUDED.skill_level, note = EXCLUDED.note,
                   latitude = EXCLUDED.latitude, longitude = EXCLUDED.longitude,
                   available_until = EXCLUDED.available_until, updated_at = NOW()
     RETURNING *`,
    [userId, sportType, skillLevel ?? null, note ?? null, latitude ?? null, longitude ?? null, availableUntil ?? null]
  );
  return mapRow(result.rows[0]);
}

export async function unregister(userId: number, sportType: string): Promise<boolean> {
  const result = await query(
    'DELETE FROM available_players WHERE user_id = $1 AND sport_type = $2',
    [userId, sportType]
  );
  return (result.rowCount ?? 0) > 0;
}

export async function unregisterAll(userId: number): Promise<boolean> {
  const result = await query(
    'DELETE FROM available_players WHERE user_id = $1',
    [userId]
  );
  return (result.rowCount ?? 0) > 0;
}

export async function getMyAvailability(userId: number): Promise<AvailablePlayerRow[]> {
  const result = await query(
    'SELECT * FROM available_players WHERE user_id = $1 ORDER BY created_at DESC',
    [userId]
  );
  return result.rows.map(mapRow);
}

export async function listBySport(
  sportType: string,
  skillMin?: number,
  skillMax?: number,
  excludeUserId?: number
): Promise<AvailablePlayerRow[]> {
  let sql = `
    SELECT ap.*, u.display_name, u.photo_url, u.email
    FROM available_players ap
    JOIN users u ON u.id = ap.user_id
    WHERE ap.sport_type = $1
      AND (ap.available_until IS NULL OR ap.available_until > NOW())
  `;
  const params: any[] = [sportType];
  let idx = 2;

  if (excludeUserId != null) {
    sql += ` AND ap.user_id != $${idx}`;
    params.push(excludeUserId);
    idx++;
  }
  if (skillMin != null) {
    sql += ` AND ap.skill_level >= $${idx}`;
    params.push(skillMin);
    idx++;
  }
  if (skillMax != null) {
    sql += ` AND ap.skill_level <= $${idx}`;
    params.push(skillMax);
    idx++;
  }

  sql += ' ORDER BY ap.created_at DESC';

  const result = await query(sql, params);
  return result.rows.map(mapRow);
}

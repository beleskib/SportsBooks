import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface FavoriteRow {
  id: number;
  userId: number;
  entityType: string;
  entityId: number;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapFavoriteRow(row: any): FavoriteRow {
  return {
    id: row.id,
    userId: row.user_id,
    entityType: row.entity_type,
    entityId: row.entity_id,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

// ============================================================
// Favorite CRUD
// ============================================================

export async function toggleFavorite(
  userId: number,
  entityType: string,
  entityId: number
): Promise<{ added: boolean }> {
  const existing = await query(
    `SELECT id FROM user_favorites WHERE user_id = $1 AND entity_type = $2 AND entity_id = $3`,
    [userId, entityType, entityId]
  );

  if (existing.rows.length > 0) {
    await query(
      `DELETE FROM user_favorites WHERE user_id = $1 AND entity_type = $2 AND entity_id = $3`,
      [userId, entityType, entityId]
    );
    return { added: false };
  }

  await query(
    `INSERT INTO user_favorites (user_id, entity_type, entity_id)
     VALUES ($1, $2, $3)`,
    [userId, entityType, entityId]
  );
  return { added: true };
}

export async function findByUserId(
  userId: number,
  entityType?: string
): Promise<FavoriteRow[]> {
  let sql = `SELECT id, user_id, entity_type, entity_id, created_at, updated_at
     FROM user_favorites
     WHERE user_id = $1`;
  const params: any[] = [userId];

  if (entityType) {
    sql += ` AND entity_type = $2`;
    params.push(entityType);
  }

  sql += ` ORDER BY created_at DESC`;

  const result = await query(sql, params);
  return result.rows.map(mapFavoriteRow);
}

export async function isFavorited(
  userId: number,
  entityType: string,
  entityId: number
): Promise<boolean> {
  const result = await query(
    `SELECT EXISTS(
       SELECT 1 FROM user_favorites
       WHERE user_id = $1 AND entity_type = $2 AND entity_id = $3
     ) AS favorited`,
    [userId, entityType, entityId]
  );
  return result.rows[0].favorited;
}

export async function checkBulk(
  userId: number,
  entityType: string,
  entityIds: number[]
): Promise<Record<number, boolean>> {
  const result = await query(
    `SELECT entity_id FROM user_favorites
     WHERE user_id = $1 AND entity_type = $2 AND entity_id = ANY($3)`,
    [userId, entityType, entityIds]
  );

  const favoritedSet = new Set(result.rows.map((r: any) => r.entity_id));
  const map: Record<number, boolean> = {};
  for (const id of entityIds) {
    map[id] = favoritedSet.has(id);
  }
  return map;
}

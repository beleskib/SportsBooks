import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface FriendshipRow {
  id: number;
  requesterId: number;
  addresseeId: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface FriendRow {
  friendshipId: number;
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  createdAt: string;
}

export interface PendingRequestRow {
  friendshipId: number;
  requesterId: number;
  displayName: string | null;
  photoUrl: string | null;
  createdAt: string;
}

export interface UserSearchRow {
  id: number;
  displayName: string | null;
  photoUrl: string | null;
}

// ============================================================
// Map functions
// ============================================================

function mapFriendshipRow(row: any): FriendshipRow {
  return {
    id: Number(row.id),
    requesterId: Number(row.requester_id),
    addresseeId: Number(row.addressee_id),
    status: row.status,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapFriendRow(row: any): FriendRow {
  return {
    friendshipId: Number(row.friendship_id),
    userId: Number(row.user_id),
    displayName: row.display_name,
    photoUrl: row.photo_url,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapPendingRequestRow(row: any): PendingRequestRow {
  return {
    friendshipId: Number(row.friendship_id),
    requesterId: Number(row.requester_id),
    displayName: row.display_name,
    photoUrl: row.photo_url,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapUserSearchRow(row: any): UserSearchRow {
  return {
    id: Number(row.id),
    displayName: row.display_name,
    photoUrl: row.photo_url,
  };
}

// ============================================================
// Friendship CRUD
// ============================================================

export async function sendRequest(
  requesterId: number,
  addresseeId: number
): Promise<FriendshipRow> {
  const result = await query(
    `INSERT INTO friendships (requester_id, addressee_id, status)
     VALUES ($1, $2, 'pending')
     RETURNING id, requester_id, addressee_id, status, created_at, updated_at`,
    [requesterId, addresseeId]
  );
  return mapFriendshipRow(result.rows[0]);
}

export async function findPendingRequests(
  userId: number
): Promise<PendingRequestRow[]> {
  const result = await query(
    `SELECT f.id AS friendship_id, f.requester_id, u.display_name, u.photo_url, f.created_at
     FROM friendships f
     INNER JOIN users u ON u.id = f.requester_id
     WHERE f.addressee_id = $1 AND f.status = 'pending'
     ORDER BY f.created_at DESC`,
    [userId]
  );
  return result.rows.map(mapPendingRequestRow);
}

export async function respondToRequest(
  friendshipId: number,
  userId: number,
  accept: boolean
): Promise<FriendshipRow | null> {
  const newStatus = accept ? 'accepted' : 'declined';
  const result = await query(
    `UPDATE friendships
     SET status = $1, updated_at = NOW()
     WHERE id = $2 AND addressee_id = $3 AND status = 'pending'
     RETURNING id, requester_id, addressee_id, status, created_at, updated_at`,
    [newStatus, friendshipId, userId]
  );
  return result.rows.length > 0 ? mapFriendshipRow(result.rows[0]) : null;
}

export async function findFriends(userId: number): Promise<FriendRow[]> {
  const result = await query(
    `SELECT
       f.id AS friendship_id,
       CASE WHEN f.requester_id = $1 THEN f.addressee_id ELSE f.requester_id END AS user_id,
       u.display_name,
       u.photo_url,
       f.created_at
     FROM friendships f
     INNER JOIN users u ON u.id = CASE WHEN f.requester_id = $1 THEN f.addressee_id ELSE f.requester_id END
     WHERE (f.requester_id = $1 OR f.addressee_id = $1) AND f.status = 'accepted'
     ORDER BY f.created_at DESC`,
    [userId]
  );
  return result.rows.map(mapFriendRow);
}

export async function removeFriend(
  userId: number,
  friendId: number
): Promise<boolean> {
  const result = await query(
    `DELETE FROM friendships
     WHERE (
       (requester_id = $1 AND addressee_id = $2)
       OR (requester_id = $2 AND addressee_id = $1)
     ) AND status = 'accepted'`,
    [userId, friendId]
  );
  return (result.rowCount ?? 0) > 0;
}

export async function findFriendship(
  userId1: number,
  userId2: number
): Promise<FriendshipRow | null> {
  const result = await query(
    `SELECT id, requester_id, addressee_id, status, created_at, updated_at
     FROM friendships
     WHERE (requester_id = $1 AND addressee_id = $2)
        OR (requester_id = $2 AND addressee_id = $1)
     LIMIT 1`,
    [userId1, userId2]
  );
  return result.rows.length > 0 ? mapFriendshipRow(result.rows[0]) : null;
}

export async function searchUsers(
  q: string,
  excludeUserId: number,
  limit: number = 20
): Promise<UserSearchRow[]> {
  const result = await query(
    `SELECT id, display_name, photo_url
     FROM users
     WHERE display_name ILIKE $1
       AND id != $2
       AND role = 'player'
       AND is_active = true
     ORDER BY display_name
     LIMIT $3`,
    [`%${q}%`, excludeUserId, limit]
  );
  return result.rows.map(mapUserSearchRow);
}

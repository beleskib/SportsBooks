import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface CommunityRow {
  id: number;
  ownerId: number;
  ownerName: string | null;
  ownerPhotoUrl: string | null;
  name: string;
  description: string | null;
  sportType: string | null;
  imageUrl: string | null;
  maxMembers: number;
  isPublic: boolean;
  invitePolicy: string;
  isActive: boolean;
  memberCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CommunityMemberRow {
  id: number;
  communityId: number;
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  role: string;
  status: string;
  invitedBy: number | null;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapCommunityRow(row: any): CommunityRow {
  return {
    id: row.id,
    ownerId: row.owner_id,
    ownerName: row.owner_name ?? null,
    ownerPhotoUrl: row.owner_photo_url ?? null,
    name: row.name,
    description: row.description ?? null,
    sportType: row.sport_type ?? null,
    imageUrl: row.image_url ?? null,
    maxMembers: row.max_members,
    isPublic: row.is_public,
    invitePolicy: row.invite_policy,
    isActive: row.is_active,
    memberCount: Number(row.member_count ?? 0),
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapMemberRow(row: any): CommunityMemberRow {
  return {
    id: row.id,
    communityId: row.community_id,
    userId: row.user_id,
    displayName: row.display_name ?? null,
    photoUrl: row.photo_url ?? null,
    role: row.role,
    status: row.status,
    invitedBy: row.invited_by ?? null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

// ============================================================
// Community CRUD
// ============================================================

const COMMUNITY_SELECT = `
  c.id, c.owner_id, c.name, c.description, c.sport_type,
  c.image_url, c.max_members, c.is_public, c.invite_policy,
  c.is_active, c.created_at, c.updated_at,
  u.display_name AS owner_name, u.photo_url AS owner_photo_url`;

const MEMBER_COUNT_SUBQUERY = `
  (SELECT COUNT(*) FROM community_members cm2
   WHERE cm2.community_id = c.id AND cm2.status = 'approved') AS member_count`;

export async function create(ownerId: number, data: {
  name: string;
  description?: string;
  sportType?: string;
  imageUrl?: string;
  maxMembers?: number;
  isPublic?: boolean;
  invitePolicy?: string;
}): Promise<CommunityRow> {
  const result = await query(
    `INSERT INTO communities (owner_id, name, description, sport_type, image_url,
       max_members, is_public, invite_policy)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
     RETURNING *`,
    [
      ownerId,
      data.name,
      data.description || null,
      data.sportType || null,
      data.imageUrl || null,
      data.maxMembers || 50,
      data.isPublic !== undefined ? data.isPublic : false,
      data.invitePolicy || 'friends_only',
    ]
  );

  const communityId = result.rows[0].id;

  // Auto-add owner as approved member with role='owner'
  await query(
    `INSERT INTO community_members (community_id, user_id, role, status)
     VALUES ($1, $2, 'owner', 'approved')`,
    [communityId, ownerId]
  );

  return findById(communityId) as Promise<CommunityRow>;
}

export async function findById(id: number): Promise<CommunityRow | null> {
  const result = await query(
    `SELECT ${COMMUNITY_SELECT}, ${MEMBER_COUNT_SUBQUERY}
     FROM communities c
     LEFT JOIN users u ON u.id = c.owner_id
     WHERE c.id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;
  return mapCommunityRow(result.rows[0]);
}

export async function findByUserId(userId: number): Promise<CommunityRow[]> {
  const result = await query(
    `SELECT ${COMMUNITY_SELECT}, ${MEMBER_COUNT_SUBQUERY}
     FROM communities c
     LEFT JOIN users u ON u.id = c.owner_id
     INNER JOIN community_members cm ON cm.community_id = c.id
     WHERE cm.user_id = $1 AND cm.status = 'approved' AND c.is_active = true
     ORDER BY c.created_at DESC`,
    [userId]
  );
  return result.rows.map(mapCommunityRow);
}

export async function findPublic(sportType?: string): Promise<CommunityRow[]> {
  let sql = `SELECT ${COMMUNITY_SELECT}, ${MEMBER_COUNT_SUBQUERY}
     FROM communities c
     LEFT JOIN users u ON u.id = c.owner_id
     WHERE c.is_public = true AND c.is_active = true`;
  const params: any[] = [];

  if (sportType) {
    sql += ` AND c.sport_type = $1`;
    params.push(sportType);
  }

  sql += ` ORDER BY c.created_at DESC`;

  const result = await query(sql, params);
  return result.rows.map(mapCommunityRow);
}

export async function update(id: number, data: Record<string, any>): Promise<CommunityRow | null> {
  const fieldMap: Record<string, string> = {
    name: 'name',
    description: 'description',
    sportType: 'sport_type',
    imageUrl: 'image_url',
    maxMembers: 'max_members',
    isPublic: 'is_public',
    invitePolicy: 'invite_policy',
    isActive: 'is_active',
  };

  const setClauses: string[] = [];
  const params: any[] = [id];
  let idx = 2;

  for (const [key, col] of Object.entries(fieldMap)) {
    if (data[key] !== undefined) {
      setClauses.push(`${col} = $${idx++}`);
      params.push(data[key]);
    }
  }
  if (setClauses.length === 0) return findById(id);

  setClauses.push('updated_at = NOW()');
  const result = await query(
    `UPDATE communities SET ${setClauses.join(', ')} WHERE id = $1 RETURNING *`,
    params
  );
  if (result.rows.length === 0) return null;
  return findById(id);
}

// ============================================================
// Members
// ============================================================

export async function addMember(
  communityId: number,
  userId: number,
  invitedBy: number | null,
  status: string = 'pending'
): Promise<CommunityMemberRow> {
  const result = await query(
    `INSERT INTO community_members (community_id, user_id, invited_by, status)
     VALUES ($1, $2, $3, $4)
     RETURNING *`,
    [communityId, userId, invitedBy, status]
  );
  // Fetch with user info
  return getMemberRow(communityId, userId) as Promise<CommunityMemberRow>;
}

export async function updateMemberStatus(
  communityId: number,
  userId: number,
  status: string
): Promise<CommunityMemberRow | null> {
  const result = await query(
    `UPDATE community_members SET status = $3, updated_at = NOW()
     WHERE community_id = $1 AND user_id = $2
     RETURNING *`,
    [communityId, userId, status]
  );
  if (result.rows.length === 0) return null;
  return getMemberRow(communityId, userId);
}

export async function updateMemberRole(
  communityId: number,
  userId: number,
  role: string
): Promise<CommunityMemberRow | null> {
  const result = await query(
    `UPDATE community_members SET role = $3, updated_at = NOW()
     WHERE community_id = $1 AND user_id = $2
     RETURNING *`,
    [communityId, userId, role]
  );
  if (result.rows.length === 0) return null;
  return getMemberRow(communityId, userId);
}

export async function removeMember(
  communityId: number,
  userId: number
): Promise<boolean> {
  const result = await query(
    `DELETE FROM community_members
     WHERE community_id = $1 AND user_id = $2`,
    [communityId, userId]
  );
  return (result.rowCount ?? 0) > 0;
}

export async function getMembers(
  communityId: number,
  status?: string
): Promise<CommunityMemberRow[]> {
  let sql = `SELECT cm.id, cm.community_id, cm.user_id, cm.role, cm.status,
                    cm.invited_by, cm.created_at, cm.updated_at,
                    u.display_name, u.photo_url
             FROM community_members cm
             LEFT JOIN users u ON u.id = cm.user_id
             WHERE cm.community_id = $1`;
  const params: any[] = [communityId];

  if (status) {
    sql += ` AND cm.status = $2`;
    params.push(status);
  }

  sql += ` ORDER BY cm.created_at ASC`;

  const result = await query(sql, params);
  return result.rows.map(mapMemberRow);
}

export async function isMember(
  communityId: number,
  userId: number
): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM community_members
     WHERE community_id = $1 AND user_id = $2 AND status = 'approved'
     LIMIT 1`,
    [communityId, userId]
  );
  return result.rows.length > 0;
}

export async function isAdminOrOwner(
  communityId: number,
  userId: number
): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM community_members
     WHERE community_id = $1 AND user_id = $2
       AND status = 'approved' AND role IN ('admin', 'owner')
     LIMIT 1`,
    [communityId, userId]
  );
  return result.rows.length > 0;
}

export async function isOwner(
  communityId: number,
  userId: number
): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM community_members
     WHERE community_id = $1 AND user_id = $2
       AND status = 'approved' AND role = 'owner'
     LIMIT 1`,
    [communityId, userId]
  );
  return result.rows.length > 0;
}

// ============================================================
// Friendship helpers (used by invite logic)
// ============================================================

export async function areFriends(
  userId1: number,
  userId2: number
): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM friendships
     WHERE ((requester_id = $1 AND addressee_id = $2)
        OR (requester_id = $2 AND addressee_id = $1))
       AND status = 'accepted'
     LIMIT 1`,
    [userId1, userId2]
  );
  return result.rows.length > 0;
}

export async function areFriendsOfFriends(
  userId1: number,
  userId2: number
): Promise<boolean> {
  // Check if userId1 and userId2 share at least one mutual friend
  const result = await query(
    `SELECT 1
     FROM friendships f1
     INNER JOIN friendships f2
       ON (
         CASE WHEN f1.requester_id = $1 THEN f1.addressee_id ELSE f1.requester_id END
         = CASE WHEN f2.requester_id = $2 THEN f2.addressee_id ELSE f2.requester_id END
       )
     WHERE f1.status = 'accepted'
       AND f2.status = 'accepted'
       AND (f1.requester_id = $1 OR f1.addressee_id = $1)
       AND (f2.requester_id = $2 OR f2.addressee_id = $2)
     LIMIT 1`,
    [userId1, userId2]
  );
  return result.rows.length > 0;
}

// ============================================================
// Private helpers
// ============================================================

async function getMemberRow(
  communityId: number,
  userId: number
): Promise<CommunityMemberRow | null> {
  const result = await query(
    `SELECT cm.id, cm.community_id, cm.user_id, cm.role, cm.status,
            cm.invited_by, cm.created_at, cm.updated_at,
            u.display_name, u.photo_url
     FROM community_members cm
     LEFT JOIN users u ON u.id = cm.user_id
     WHERE cm.community_id = $1 AND cm.user_id = $2`,
    [communityId, userId]
  );
  if (result.rows.length === 0) return null;
  return mapMemberRow(result.rows[0]);
}

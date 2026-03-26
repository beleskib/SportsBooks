import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface LobbyRow {
  id: number;
  communityId: number;
  communityName: string | null;
  createdBy: number;
  creatorName: string | null;
  creatorPhotoUrl: string | null;
  title: string;
  sportType: string;
  scheduledDate: string;
  scheduledTime: string;
  durationMinutes: number;
  maxPlayers: number;
  currentPlayers: number;
  venueId: number | null;
  skillLevelMin: number;
  skillLevelMax: number;
  status: string;
  isPublic: boolean;
  madePublicAt: string | null;
  description: string | null;
  participants: LobbyParticipantRow[];
  createdAt: string;
  updatedAt: string;
}

export interface LobbyParticipantRow {
  id: number;
  lobbyId: number;
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  status: string;
  joinedAt: string;
  createdAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapLobbyRow(row: any): Omit<LobbyRow, 'participants'> {
  return {
    id: row.id,
    communityId: row.community_id,
    communityName: row.community_name ?? null,
    createdBy: row.created_by,
    creatorName: row.creator_name ?? null,
    creatorPhotoUrl: row.creator_photo_url ?? null,
    title: row.title,
    sportType: row.sport_type,
    scheduledDate: row.scheduled_date instanceof Date
      ? row.scheduled_date.toISOString().split('T')[0]
      : row.scheduled_date,
    scheduledTime: row.scheduled_time,
    durationMinutes: row.duration_minutes,
    maxPlayers: row.max_players,
    currentPlayers: row.current_players,
    venueId: row.venue_id ?? null,
    skillLevelMin: row.skill_level_min,
    skillLevelMax: row.skill_level_max,
    status: row.status,
    isPublic: row.is_public,
    madePublicAt: row.made_public_at?.toISOString?.() ?? row.made_public_at ?? null,
    description: row.description ?? null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapParticipantRow(row: any): LobbyParticipantRow {
  return {
    id: row.id,
    lobbyId: row.lobby_id,
    userId: row.user_id,
    displayName: row.display_name ?? null,
    photoUrl: row.photo_url ?? null,
    status: row.status,
    joinedAt: row.joined_at?.toISOString?.() ?? row.joined_at,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

// ============================================================
// Lobby CRUD
// ============================================================

const LOBBY_SELECT = `
  l.id, l.community_id, l.created_by, l.title, l.sport_type,
  l.scheduled_date, l.scheduled_time, l.duration_minutes,
  l.max_players, l.current_players, l.venue_id,
  l.skill_level_min, l.skill_level_max, l.status,
  l.is_public, l.made_public_at, l.description,
  l.created_at, l.updated_at,
  co.name AS community_name,
  u.display_name AS creator_name, u.photo_url AS creator_photo_url`;

export async function create(communityId: number, createdBy: number, data: {
  title: string;
  sportType: string;
  scheduledDate: string;
  scheduledTime: string;
  durationMinutes?: number;
  maxPlayers: number;
  venueId?: number;
  skillLevelMin?: number;
  skillLevelMax?: number;
  description?: string;
  isPublic?: boolean;
}): Promise<LobbyRow> {
  const result = await query(
    `INSERT INTO community_lobbies (community_id, created_by, title, sport_type,
       scheduled_date, scheduled_time, duration_minutes, max_players,
       current_players, venue_id, skill_level_min, skill_level_max,
       description, is_public)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 1, $9, $10, $11, $12, $13)
     RETURNING *`,
    [
      communityId,
      createdBy,
      data.title,
      data.sportType,
      data.scheduledDate,
      data.scheduledTime,
      data.durationMinutes || 60,
      data.maxPlayers,
      data.venueId || null,
      data.skillLevelMin || 1,
      data.skillLevelMax || 5,
      data.description || null,
      data.isPublic !== undefined ? data.isPublic : false,
    ]
  );

  const lobbyId = result.rows[0].id;

  // Auto-add creator as participant
  await query(
    `INSERT INTO lobby_participants (lobby_id, user_id, status)
     VALUES ($1, $2, 'joined')`,
    [lobbyId, createdBy]
  );

  return findById(lobbyId) as Promise<LobbyRow>;
}

export async function findById(id: number): Promise<LobbyRow | null> {
  const result = await query(
    `SELECT ${LOBBY_SELECT}
     FROM community_lobbies l
     LEFT JOIN communities co ON co.id = l.community_id
     LEFT JOIN users u ON u.id = l.created_by
     WHERE l.id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;

  const lobby = mapLobbyRow(result.rows[0]);
  const participants = await getParticipants(id);
  return { ...lobby, participants };
}

export async function findByCommunity(
  communityId: number,
  status?: string
): Promise<LobbyRow[]> {
  let sql = `SELECT ${LOBBY_SELECT}
     FROM community_lobbies l
     LEFT JOIN communities co ON co.id = l.community_id
     LEFT JOIN users u ON u.id = l.created_by
     WHERE l.community_id = $1`;
  const params: any[] = [communityId];

  if (status) {
    sql += ` AND l.status = $2`;
    params.push(status);
  }

  sql += ` ORDER BY l.scheduled_date ASC, l.scheduled_time ASC`;

  const result = await query(sql, params);
  return result.rows.map((r: any) => ({ ...mapLobbyRow(r), participants: [] }));
}

export async function findPublicLobbies(
  sportType?: string,
  date?: string
): Promise<LobbyRow[]> {
  let sql = `SELECT ${LOBBY_SELECT}
     FROM community_lobbies l
     LEFT JOIN communities co ON co.id = l.community_id
     LEFT JOIN users u ON u.id = l.created_by
     WHERE l.is_public = true AND l.status = 'open'`;
  const params: any[] = [];
  let idx = 1;

  if (sportType) {
    sql += ` AND l.sport_type = $${idx++}`;
    params.push(sportType);
  }
  if (date) {
    sql += ` AND l.scheduled_date = $${idx++}`;
    params.push(date);
  }

  sql += ` ORDER BY l.scheduled_date ASC, l.scheduled_time ASC`;

  const result = await query(sql, params);
  return result.rows.map((r: any) => ({ ...mapLobbyRow(r), participants: [] }));
}

export async function join(lobbyId: number, userId: number): Promise<LobbyParticipantRow> {
  // UPSERT — handles rejoin after leave (unique constraint on lobby_id + user_id)
  const result = await query(
    `INSERT INTO lobby_participants (lobby_id, user_id, status, joined_at)
     VALUES ($1, $2, 'joined', NOW())
     ON CONFLICT (lobby_id, user_id)
     DO UPDATE SET status = 'joined', joined_at = NOW(), updated_at = NOW()
     RETURNING *`,
    [lobbyId, userId]
  );

  // Increment current_players
  await query(
    `UPDATE community_lobbies SET current_players = current_players + 1, updated_at = NOW()
     WHERE id = $1`,
    [lobbyId]
  );

  // Auto-set to full if needed
  await query(
    `UPDATE community_lobbies SET status = 'full', updated_at = NOW()
     WHERE id = $1 AND current_players >= max_players AND status = 'open'`,
    [lobbyId]
  );

  // Return with user info
  const participant = await query(
    `SELECT lp.id, lp.lobby_id, lp.user_id, lp.status, lp.joined_at, lp.created_at,
            u.display_name, u.photo_url
     FROM lobby_participants lp
     LEFT JOIN users u ON u.id = lp.user_id
     WHERE lp.id = $1`,
    [result.rows[0].id]
  );
  return mapParticipantRow(participant.rows[0]);
}

export async function leave(lobbyId: number, userId: number): Promise<boolean> {
  const check = await query(
    `SELECT status FROM lobby_participants
     WHERE lobby_id = $1 AND user_id = $2`,
    [lobbyId, userId]
  );
  if (check.rows.length === 0) return false;

  const wasJoined = check.rows[0].status === 'joined';

  await query(
    `UPDATE lobby_participants SET status = 'left', updated_at = NOW()
     WHERE lobby_id = $1 AND user_id = $2`,
    [lobbyId, userId]
  );

  if (wasJoined) {
    await query(
      `UPDATE community_lobbies SET current_players = GREATEST(current_players - 1, 0), updated_at = NOW()
       WHERE id = $1`,
      [lobbyId]
    );
    // Re-open if was full
    await query(
      `UPDATE community_lobbies SET status = 'open', updated_at = NOW()
       WHERE id = $1 AND status = 'full' AND current_players < max_players`,
      [lobbyId]
    );
  }

  return true;
}

export async function makePublic(lobbyId: number): Promise<LobbyRow | null> {
  const result = await query(
    `UPDATE community_lobbies
     SET is_public = true, made_public_at = NOW(), updated_at = NOW()
     WHERE id = $1
     RETURNING *`,
    [lobbyId]
  );
  if (result.rows.length === 0) return null;
  return findById(lobbyId);
}

export async function updateStatus(lobbyId: number, status: string): Promise<LobbyRow | null> {
  const result = await query(
    `UPDATE community_lobbies SET status = $2, updated_at = NOW()
     WHERE id = $1
     RETURNING *`,
    [lobbyId, status]
  );
  if (result.rows.length === 0) return null;
  return findById(lobbyId);
}

export async function getParticipants(lobbyId: number): Promise<LobbyParticipantRow[]> {
  const result = await query(
    `SELECT lp.id, lp.lobby_id, lp.user_id, lp.status, lp.joined_at, lp.created_at,
            u.display_name, u.photo_url
     FROM lobby_participants lp
     LEFT JOIN users u ON u.id = lp.user_id
     WHERE lp.lobby_id = $1 AND lp.status = 'joined'
     ORDER BY lp.joined_at ASC`,
    [lobbyId]
  );
  return result.rows.map(mapParticipantRow);
}

export async function isParticipant(lobbyId: number, userId: number): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM lobby_participants
     WHERE lobby_id = $1 AND user_id = $2 AND status = 'joined'
     LIMIT 1`,
    [lobbyId, userId]
  );
  return result.rows.length > 0;
}

// ============================================================
// Lobby Chat
// ============================================================

export interface LobbyMessageRow {
  id: number;
  lobbyId: number;
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  message: string;
  createdAt: string;
}

export async function getMessages(lobbyId: number, limit = 50, offset = 0): Promise<LobbyMessageRow[]> {
  const result = await query(
    `SELECT lm.id, lm.lobby_id, lm.user_id, lm.message, lm.created_at,
            u.display_name, u.photo_url
     FROM lobby_messages lm
     LEFT JOIN users u ON u.id = lm.user_id
     WHERE lm.lobby_id = $1
     ORDER BY lm.created_at ASC
     LIMIT $2 OFFSET $3`,
    [lobbyId, limit, offset]
  );
  return result.rows.map((r: any) => ({
    id: r.id,
    lobbyId: r.lobby_id,
    userId: r.user_id,
    displayName: r.display_name ?? null,
    photoUrl: r.photo_url ?? null,
    message: r.message,
    createdAt: r.created_at?.toISOString?.() ?? r.created_at,
  }));
}

export async function sendMessage(lobbyId: number, userId: number, message: string): Promise<LobbyMessageRow> {
  const result = await query(
    `INSERT INTO lobby_messages (lobby_id, user_id, message)
     VALUES ($1, $2, $3)
     RETURNING *`,
    [lobbyId, userId, message]
  );
  const row = result.rows[0];
  // Get user info
  const userResult = await query(
    `SELECT display_name, photo_url FROM users WHERE id = $1`,
    [userId]
  );
  return {
    id: row.id,
    lobbyId: row.lobby_id,
    userId: row.user_id,
    displayName: userResult.rows[0]?.display_name ?? null,
    photoUrl: userResult.rows[0]?.photo_url ?? null,
    message: row.message,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface VenueBookingLobbyRow {
  id: number;
  creatorId: number;
  creatorName: string | null;
  creatorPhotoUrl: string | null;
  timeSlotId: number;
  venueId: number;
  venueName: string | null;
  title: string;
  paymentType: string; // 'split' | 'creator_pays' | 'split_to_teams'
  maxPlayers: number;
  currentPlayers: number;
  totalPrice: number;
  pricePerPlayer: number;
  currency: string;
  status: string;
  bookingId: number | null;
  description: string | null;
  slotDate: string;
  startTime: string;
  endTime: string;
  sportType: string | null;
  expiresAt: string | null;
  members: VenueBookingLobbyMemberRow[];
  teams: VenueBookingLobbyTeamRow[];
  createdAt: string;
  updatedAt: string;
}

export interface VenueBookingLobbyMemberRow {
  id: number;
  lobbyId: number;
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  status: string;
  shareAmount: number | null;
  paymentId: number | null;
  teamId: number | null;
  joinedAt: string;
  paidAt: string | null;
}

export interface VenueBookingLobbyTeamRow {
  id: number;
  lobbyId: number;
  teamName: string;
  teamNumber: number;
  leaderId: number | null;
  leaderName: string | null;
  shareAmount: number | null;
  members: VenueBookingLobbyMemberRow[];
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapLobbyRow(row: any): Omit<VenueBookingLobbyRow, 'members' | 'teams'> {
  return {
    id: row.id,
    creatorId: row.creator_id,
    creatorName: row.creator_name ?? null,
    creatorPhotoUrl: row.creator_photo_url ?? null,
    timeSlotId: row.time_slot_id,
    venueId: row.venue_id,
    venueName: row.venue_name ?? null,
    title: row.title,
    paymentType: row.payment_type,
    maxPlayers: row.max_players,
    currentPlayers: row.current_players,
    totalPrice: Number(row.total_price),
    pricePerPlayer: Number(row.price_per_player),
    currency: row.currency,
    status: row.status,
    bookingId: row.booking_id ?? null,
    description: row.description ?? null,
    slotDate: row.slot_date instanceof Date
      ? row.slot_date.toISOString().split('T')[0]
      : row.slot_date,
    startTime: row.start_time,
    endTime: row.end_time,
    sportType: row.sport_type ?? null,
    expiresAt: row.expires_at?.toISOString?.() ?? row.expires_at ?? null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapMemberRow(row: any): VenueBookingLobbyMemberRow {
  return {
    id: row.id,
    lobbyId: row.lobby_id,
    userId: row.user_id,
    displayName: row.display_name ?? null,
    photoUrl: row.photo_url ?? null,
    status: row.status,
    shareAmount: row.share_amount != null ? Number(row.share_amount) : null,
    paymentId: row.payment_id ?? null,
    teamId: row.team_id ?? null,
    joinedAt: row.joined_at?.toISOString?.() ?? row.joined_at,
    paidAt: row.paid_at?.toISOString?.() ?? row.paid_at ?? null,
  };
}

function mapTeamRow(row: any): Omit<VenueBookingLobbyTeamRow, 'members'> {
  return {
    id: row.id,
    lobbyId: row.lobby_id,
    teamName: row.team_name,
    teamNumber: row.team_number,
    leaderId: row.leader_id ?? null,
    leaderName: row.leader_name ?? null,
    shareAmount: row.share_amount != null ? Number(row.share_amount) : null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

// ============================================================
// Lobby SELECT
// ============================================================

const LOBBY_SELECT = `
  vbl.id, vbl.creator_id, vbl.time_slot_id, vbl.venue_id, vbl.title,
  vbl.payment_type, vbl.max_players, vbl.current_players,
  vbl.total_price, vbl.price_per_player, vbl.currency, vbl.status,
  vbl.booking_id, vbl.description, vbl.expires_at,
  vbl.created_at, vbl.updated_at,
  u.display_name AS creator_name, u.photo_url AS creator_photo_url,
  v.name AS venue_name, v.sport_type,
  ts.slot_date, ts.start_time, ts.end_time`;

// ============================================================
// Lobby CRUD
// ============================================================

export async function create(creatorId: number, data: {
  timeSlotId: number;
  venueId: number;
  title: string;
  paymentType: string;
  maxPlayers: number;
  totalPrice: number;
  description?: string;
}): Promise<VenueBookingLobbyRow> {
  const pricePerPlayer = Math.ceil((data.totalPrice / data.maxPlayers) * 100) / 100;

  const result = await query(
    `INSERT INTO venue_booking_lobbies (creator_id, time_slot_id, venue_id, title,
       payment_type, max_players, current_players, total_price, price_per_player,
       description)
     VALUES ($1, $2, $3, $4, $5, $6, 1, $7, $8, $9)
     RETURNING *`,
    [
      creatorId,
      data.timeSlotId,
      data.venueId,
      data.title,
      data.paymentType,
      data.maxPlayers,
      data.totalPrice,
      pricePerPlayer,
      data.description || null,
    ]
  );

  const lobbyId = result.rows[0].id;

  // Auto-add creator as first member with share_amount set
  await query(
    `INSERT INTO venue_booking_lobby_members (lobby_id, user_id, status, share_amount)
     VALUES ($1, $2, 'joined', $3)`,
    [lobbyId, creatorId, pricePerPlayer]
  );

  return findById(lobbyId) as Promise<VenueBookingLobbyRow>;
}

export async function findById(id: number): Promise<VenueBookingLobbyRow | null> {
  const result = await query(
    `SELECT ${LOBBY_SELECT}
     FROM venue_booking_lobbies vbl
     LEFT JOIN users u ON u.id = vbl.creator_id
     LEFT JOIN venues v ON v.id = vbl.venue_id
     LEFT JOIN time_slots ts ON ts.id = vbl.time_slot_id
     WHERE vbl.id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;

  const lobby = mapLobbyRow(result.rows[0]);
  const members = await getMembers(id);
  const teams = await getTeams(id);
  return { ...lobby, members, teams };
}

export async function findOpen(venueId?: number, sportType?: string): Promise<VenueBookingLobbyRow[]> {
  let sql = `SELECT ${LOBBY_SELECT}
     FROM venue_booking_lobbies vbl
     LEFT JOIN users u ON u.id = vbl.creator_id
     LEFT JOIN venues v ON v.id = vbl.venue_id
     LEFT JOIN time_slots ts ON ts.id = vbl.time_slot_id
     WHERE vbl.status = 'open'`;
  const params: any[] = [];
  let idx = 1;

  if (venueId) {
    sql += ` AND vbl.venue_id = $${idx++}`;
    params.push(venueId);
  }
  if (sportType) {
    sql += ` AND v.sport_type = $${idx++}`;
    params.push(sportType);
  }

  sql += ` ORDER BY vbl.created_at DESC`;

  const result = await query(sql, params);
  return result.rows.map((r: any) => ({ ...mapLobbyRow(r), members: [], teams: [] }));
}

export async function findByMember(userId: number): Promise<VenueBookingLobbyRow[]> {
  const result = await query(
    `SELECT ${LOBBY_SELECT}
     FROM venue_booking_lobbies vbl
     LEFT JOIN users u ON u.id = vbl.creator_id
     LEFT JOIN venues v ON v.id = vbl.venue_id
     LEFT JOIN time_slots ts ON ts.id = vbl.time_slot_id
     INNER JOIN venue_booking_lobby_members vblm ON vblm.lobby_id = vbl.id
     WHERE vblm.user_id = $1 AND vblm.status = 'joined'
     ORDER BY vbl.created_at DESC`,
    [userId]
  );

  const lobbies: VenueBookingLobbyRow[] = [];
  for (const row of result.rows) {
    const lobby = mapLobbyRow(row);
    const members = await getMembers(lobby.id);
    const teams = await getTeams(lobby.id);
    lobbies.push({ ...lobby, members, teams });
  }
  return lobbies;
}

export async function getMembers(lobbyId: number): Promise<VenueBookingLobbyMemberRow[]> {
  const result = await query(
    `SELECT vblm.id, vblm.lobby_id, vblm.user_id, vblm.status,
            vblm.share_amount, vblm.payment_id, vblm.joined_at, vblm.paid_at,
            vblm.team_id, u.display_name, u.photo_url
     FROM venue_booking_lobby_members vblm
     LEFT JOIN users u ON u.id = vblm.user_id
     WHERE vblm.lobby_id = $1 AND vblm.status != 'left'
     ORDER BY vblm.joined_at ASC`,
    [lobbyId]
  );
  return result.rows.map(mapMemberRow);
}

export async function join(lobbyId: number, userId: number): Promise<VenueBookingLobbyMemberRow> {
  // Get price_per_player from the lobby
  const lobbyResult = await query(
    `SELECT price_per_player FROM venue_booking_lobbies WHERE id = $1`,
    [lobbyId]
  );
  const shareAmount = Number(lobbyResult.rows[0].price_per_player);

  // UPSERT - handles rejoin after leave (unique constraint on lobby_id + user_id)
  const result = await query(
    `INSERT INTO venue_booking_lobby_members (lobby_id, user_id, status, share_amount, joined_at)
     VALUES ($1, $2, 'joined', $3, NOW())
     ON CONFLICT (lobby_id, user_id)
     DO UPDATE SET status = 'joined', share_amount = $3, joined_at = NOW(), updated_at = NOW()
     RETURNING *`,
    [lobbyId, userId, shareAmount]
  );

  // Increment current_players
  await query(
    `UPDATE venue_booking_lobbies SET current_players = current_players + 1, updated_at = NOW()
     WHERE id = $1`,
    [lobbyId]
  );

  // Auto-set to full if needed
  await query(
    `UPDATE venue_booking_lobbies SET status = 'full', updated_at = NOW()
     WHERE id = $1 AND current_players >= max_players AND status = 'open'`,
    [lobbyId]
  );

  // Return with user info
  const member = await query(
    `SELECT vblm.id, vblm.lobby_id, vblm.user_id, vblm.status,
            vblm.share_amount, vblm.payment_id, vblm.joined_at, vblm.paid_at,
            vblm.team_id, u.display_name, u.photo_url
     FROM venue_booking_lobby_members vblm
     LEFT JOIN users u ON u.id = vblm.user_id
     WHERE vblm.id = $1`,
    [result.rows[0].id]
  );
  return mapMemberRow(member.rows[0]);
}

export async function leave(lobbyId: number, userId: number): Promise<boolean> {
  const check = await query(
    `SELECT status FROM venue_booking_lobby_members
     WHERE lobby_id = $1 AND user_id = $2`,
    [lobbyId, userId]
  );
  if (check.rows.length === 0) return false;

  const wasJoined = check.rows[0].status === 'joined';

  await query(
    `UPDATE venue_booking_lobby_members SET status = 'left', updated_at = NOW()
     WHERE lobby_id = $1 AND user_id = $2`,
    [lobbyId, userId]
  );

  if (wasJoined) {
    await query(
      `UPDATE venue_booking_lobbies SET current_players = GREATEST(current_players - 1, 0), updated_at = NOW()
       WHERE id = $1`,
      [lobbyId]
    );
    // Re-open if was full
    await query(
      `UPDATE venue_booking_lobbies SET status = 'open', updated_at = NOW()
       WHERE id = $1 AND status = 'full' AND current_players < max_players`,
      [lobbyId]
    );
  }

  return true;
}

export async function update(lobbyId: number, data: {
  title?: string;
  description?: string;
  maxPlayers?: number;
  paymentType?: string;
}): Promise<VenueBookingLobbyRow | null> {
  const sets: string[] = [];
  const params: any[] = [];
  let idx = 1;

  if (data.title !== undefined) {
    sets.push(`title = $${idx++}`);
    params.push(data.title);
  }
  if (data.description !== undefined) {
    sets.push(`description = $${idx++}`);
    params.push(data.description || null);
  }
  if (data.paymentType !== undefined) {
    sets.push(`payment_type = $${idx++}`);
    params.push(data.paymentType);
  }
  if (data.maxPlayers !== undefined) {
    sets.push(`max_players = $${idx++}`);
    params.push(data.maxPlayers);
    // Recalculate price_per_player
    const lobbyResult = await query(
      `SELECT total_price FROM venue_booking_lobbies WHERE id = $1`,
      [lobbyId]
    );
    if (lobbyResult.rows.length > 0) {
      const totalPrice = Number(lobbyResult.rows[0].total_price);
      const pricePerPlayer = Math.ceil((totalPrice / data.maxPlayers) * 100) / 100;
      sets.push(`price_per_player = $${idx++}`);
      params.push(pricePerPlayer);
    }
  }

  if (sets.length === 0) return findById(lobbyId);

  sets.push('updated_at = NOW()');
  params.push(lobbyId);

  await query(
    `UPDATE venue_booking_lobbies SET ${sets.join(', ')} WHERE id = $${idx}`,
    params
  );

  // If maxPlayers changed, check if lobby should transition open <-> full
  if (data.maxPlayers !== undefined) {
    await query(
      `UPDATE venue_booking_lobbies SET status = 'full', updated_at = NOW()
       WHERE id = $1 AND current_players >= max_players AND status = 'open'`,
      [lobbyId]
    );
    await query(
      `UPDATE venue_booking_lobbies SET status = 'open', updated_at = NOW()
       WHERE id = $1 AND current_players < max_players AND status = 'full'`,
      [lobbyId]
    );
  }

  return findById(lobbyId);
}

export async function deleteAllTeams(lobbyId: number): Promise<void> {
  // Clear team_id from all members first
  await query(
    `UPDATE venue_booking_lobby_members SET team_id = NULL, updated_at = NOW()
     WHERE lobby_id = $1`,
    [lobbyId]
  );
  // Delete all teams
  await query(
    `DELETE FROM venue_booking_lobby_teams WHERE lobby_id = $1`,
    [lobbyId]
  );
}

export async function updateStatus(lobbyId: number, status: string): Promise<VenueBookingLobbyRow | null> {
  const result = await query(
    `UPDATE venue_booking_lobbies SET status = $2, updated_at = NOW()
     WHERE id = $1
     RETURNING *`,
    [lobbyId, status]
  );
  if (result.rows.length === 0) return null;
  return findById(lobbyId);
}

export async function setBookingId(lobbyId: number, bookingId: number): Promise<void> {
  await query(
    `UPDATE venue_booking_lobbies SET booking_id = $2, updated_at = NOW()
     WHERE id = $1`,
    [lobbyId, bookingId]
  );
}

export async function updateMemberPaymentStatus(
  lobbyId: number,
  userId: number,
  paymentId: number | null,
  status: string,
  paidAt?: string
): Promise<void> {
  await query(
    `UPDATE venue_booking_lobby_members
     SET status = $3, payment_id = $4, paid_at = $5, updated_at = NOW()
     WHERE lobby_id = $1 AND user_id = $2`,
    [lobbyId, userId, status, paymentId, paidAt || null]
  );
}

export async function areAllMembersPaid(lobbyId: number): Promise<boolean> {
  const result = await query(
    `SELECT COUNT(*) AS total,
            COUNT(*) FILTER (WHERE status = 'paid') AS paid
     FROM venue_booking_lobby_members
     WHERE lobby_id = $1 AND status != 'left'`,
    [lobbyId]
  );
  const { total, paid } = result.rows[0];
  return Number(total) > 0 && Number(total) === Number(paid);
}

export async function findByBookingId(bookingId: number): Promise<VenueBookingLobbyRow | null> {
  const result = await query(
    `SELECT ${LOBBY_SELECT}
     FROM venue_booking_lobbies vbl
     LEFT JOIN users u ON u.id = vbl.creator_id
     LEFT JOIN venues v ON v.id = vbl.venue_id
     LEFT JOIN time_slots ts ON ts.id = vbl.time_slot_id
     WHERE vbl.booking_id = $1`,
    [bookingId]
  );
  if (result.rows.length === 0) return null;

  const lobby = mapLobbyRow(result.rows[0]);
  const members = await getMembers(lobby.id);
  const teams = await getTeams(lobby.id);
  return { ...lobby, members, teams };
}

// ============================================================
// Team CRUD
// ============================================================

export async function getTeams(lobbyId: number): Promise<VenueBookingLobbyTeamRow[]> {
  const teamResult = await query(
    `SELECT t.id, t.lobby_id, t.team_name, t.team_number, t.leader_id,
            t.share_amount, t.created_at, t.updated_at,
            u.display_name AS leader_name
     FROM venue_booking_lobby_teams t
     LEFT JOIN users u ON u.id = t.leader_id
     WHERE t.lobby_id = $1
     ORDER BY t.team_number ASC`,
    [lobbyId]
  );

  const members = await getMembers(lobbyId);

  return teamResult.rows.map((row: any) => {
    const team = mapTeamRow(row);
    const teamMembers = members.filter(m => m.teamId === team.id);
    return { ...team, members: teamMembers };
  });
}

export async function createTeam(
  lobbyId: number,
  teamName: string,
  teamNumber: number,
  shareAmount?: number
): Promise<VenueBookingLobbyTeamRow> {
  const result = await query(
    `INSERT INTO venue_booking_lobby_teams (lobby_id, team_name, team_number, share_amount)
     VALUES ($1, $2, $3, $4)
     RETURNING *`,
    [lobbyId, teamName, teamNumber, shareAmount ?? null]
  );
  const row = result.rows[0];
  return { ...mapTeamRow(row), members: [] };
}

export async function createDefaultTeams(
  lobbyId: number,
  totalPrice: number,
  numTeams: number = 2
): Promise<VenueBookingLobbyTeamRow[]> {
  const sharePerTeam = Math.ceil((totalPrice / numTeams) * 100) / 100;
  const teams: VenueBookingLobbyTeamRow[] = [];
  for (let i = 1; i <= numTeams; i++) {
    const team = await createTeam(lobbyId, `Team ${i}`, i, sharePerTeam);
    teams.push(team);
  }
  return teams;
}

export async function addTeam(lobbyId: number, teamName: string): Promise<VenueBookingLobbyTeamRow> {
  // Get current max team_number
  const maxResult = await query(
    `SELECT COALESCE(MAX(team_number), 0) AS max_num FROM venue_booking_lobby_teams WHERE lobby_id = $1`,
    [lobbyId]
  );
  const nextNumber = Number(maxResult.rows[0].max_num) + 1;

  // Recalculate share amount for all teams
  const lobbyResult = await query(
    `SELECT total_price FROM venue_booking_lobbies WHERE id = $1`,
    [lobbyId]
  );
  const totalPrice = Number(lobbyResult.rows[0].total_price);
  const sharePerTeam = Math.ceil((totalPrice / nextNumber) * 100) / 100;

  // Create the new team
  const team = await createTeam(lobbyId, teamName, nextNumber, sharePerTeam);

  // Update all teams' share amounts
  await query(
    `UPDATE venue_booking_lobby_teams SET share_amount = $2, updated_at = NOW()
     WHERE lobby_id = $1`,
    [lobbyId, sharePerTeam]
  );

  return team;
}

export async function joinTeam(lobbyId: number, userId: number, teamId: number): Promise<void> {
  await query(
    `UPDATE venue_booking_lobby_members SET team_id = $3, updated_at = NOW()
     WHERE lobby_id = $1 AND user_id = $2 AND status != 'left'`,
    [lobbyId, userId, teamId]
  );

  // Set first member as team leader if none exists
  const leaderCheck = await query(
    `SELECT leader_id FROM venue_booking_lobby_teams WHERE id = $1`,
    [teamId]
  );
  if (!leaderCheck.rows[0]?.leader_id) {
    await query(
      `UPDATE venue_booking_lobby_teams SET leader_id = $2, updated_at = NOW() WHERE id = $1`,
      [teamId, userId]
    );
  }
}

export async function leaveTeam(lobbyId: number, userId: number): Promise<void> {
  // Get current team_id before clearing
  const memberResult = await query(
    `SELECT team_id FROM venue_booking_lobby_members
     WHERE lobby_id = $1 AND user_id = $2 AND status != 'left'`,
    [lobbyId, userId]
  );
  const teamId = memberResult.rows[0]?.team_id;

  await query(
    `UPDATE venue_booking_lobby_members SET team_id = NULL, updated_at = NOW()
     WHERE lobby_id = $1 AND user_id = $2 AND status != 'left'`,
    [lobbyId, userId]
  );

  // If this user was team leader, reassign to next member or clear
  if (teamId) {
    const nextLeader = await query(
      `SELECT user_id FROM venue_booking_lobby_members
       WHERE lobby_id = $1 AND team_id = $2 AND status != 'left' AND user_id != $3
       ORDER BY joined_at ASC LIMIT 1`,
      [lobbyId, teamId, userId]
    );
    const newLeaderId = nextLeader.rows[0]?.user_id ?? null;
    await query(
      `UPDATE venue_booking_lobby_teams SET leader_id = $2, updated_at = NOW() WHERE id = $1`,
      [teamId, newLeaderId]
    );
  }
}

export async function getTeamCount(lobbyId: number): Promise<number> {
  const result = await query(
    `SELECT COUNT(*) AS cnt FROM venue_booking_lobby_teams WHERE lobby_id = $1`,
    [lobbyId]
  );
  return Number(result.rows[0].cnt);
}

export async function areAllTeamLeadersPaid(lobbyId: number): Promise<boolean> {
  const result = await query(
    `SELECT COUNT(*) AS total,
            COUNT(*) FILTER (WHERE vblm.status = 'paid') AS paid
     FROM venue_booking_lobby_teams t
     INNER JOIN venue_booking_lobby_members vblm
       ON vblm.lobby_id = t.lobby_id AND vblm.user_id = t.leader_id AND vblm.status != 'left'
     WHERE t.lobby_id = $1 AND t.leader_id IS NOT NULL`,
    [lobbyId]
  );
  const { total, paid } = result.rows[0];
  return Number(total) > 0 && Number(total) === Number(paid);
}

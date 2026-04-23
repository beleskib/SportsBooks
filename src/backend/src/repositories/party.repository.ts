import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface PartyRow {
  id: number;
  leaderId: number;
  leaderName: string | null;
  leaderPhotoUrl: string | null;
  name: string | null;
  sportType: string | null;
  status: string;
  matchId: number | null;
  members: PartyMemberRow[];
  createdAt: string;
  updatedAt: string;
}

export interface PartyMemberRow {
  id: number;
  partyId: number;
  userId: number;
  userName: string | null;
  userPhotoUrl: string | null;
  status: string;
  respondedAt: string | null;
  createdAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapPartyRow(row: any): Omit<PartyRow, 'members'> {
  return {
    id: Number(row.id),
    leaderId: Number(row.leader_id),
    leaderName: row.leader_name ?? row.display_name ?? null,
    leaderPhotoUrl: row.leader_photo_url ?? row.photo_url ?? null,
    name: row.name,
    sportType: row.sport_type,
    status: row.status,
    matchId: row.match_id ? Number(row.match_id) : null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapPartyMemberRow(row: any): PartyMemberRow {
  return {
    id: Number(row.id),
    partyId: Number(row.party_id),
    userId: Number(row.user_id),
    userName: row.user_name ?? row.display_name ?? null,
    userPhotoUrl: row.user_photo_url ?? row.photo_url ?? null,
    status: row.status,
    respondedAt: row.responded_at?.toISOString?.() ?? row.responded_at ?? null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

// ============================================================
// Party CRUD
// ============================================================

export async function create(
  leaderId: number,
  name?: string | null,
  sportType?: string | null
): Promise<PartyRow> {
  const result = await query(
    `INSERT INTO parties (leader_id, name, sport_type, status)
     VALUES ($1, $2, $3, 'forming')
     RETURNING *`,
    [leaderId, name || null, sportType || null]
  );
  const party = mapPartyRow(result.rows[0]);

  // Auto-add leader as accepted member
  await query(
    `INSERT INTO party_members (party_id, user_id, status, responded_at)
     VALUES ($1, $2, 'accepted', NOW())`,
    [party.id, leaderId]
  );

  const members = await findMembers(party.id);
  return { ...party, members };
}

export async function findById(id: number): Promise<PartyRow | null> {
  const result = await query(
    `SELECT p.id, p.leader_id, p.name, p.sport_type, p.status, p.match_id,
            p.created_at, p.updated_at,
            u.display_name AS leader_name, u.photo_url AS leader_photo_url
     FROM parties p
     LEFT JOIN users u ON u.id = p.leader_id
     WHERE p.id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;

  const party = mapPartyRow(result.rows[0]);
  const members = await findMembers(id);
  return { ...party, members };
}

export async function findActiveByUserId(userId: number): Promise<PartyRow | null> {
  // Find a party where the user is either the leader or an accepted member,
  // and the party status is 'forming' or 'ready'
  const result = await query(
    `SELECT p.id, p.leader_id, p.name, p.sport_type, p.status, p.match_id,
            p.created_at, p.updated_at,
            u.display_name AS leader_name, u.photo_url AS leader_photo_url
     FROM parties p
     LEFT JOIN users u ON u.id = p.leader_id
     WHERE p.status IN ('forming', 'ready')
       AND (
         p.leader_id = $1
         OR EXISTS (
           SELECT 1 FROM party_members pm
           WHERE pm.party_id = p.id AND pm.user_id = $1 AND pm.status = 'accepted'
         )
       )
     ORDER BY p.created_at DESC
     LIMIT 1`,
    [userId]
  );
  if (result.rows.length === 0) return null;

  const party = mapPartyRow(result.rows[0]);
  const members = await findMembers(party.id);
  return { ...party, members };
}

export async function inviteMembers(
  partyId: number,
  userIds: number[]
): Promise<PartyMemberRow[]> {
  const inserted: PartyMemberRow[] = [];
  for (const userId of userIds) {
    const result = await query(
      `INSERT INTO party_members (party_id, user_id, status)
       VALUES ($1, $2, 'invited')
       ON CONFLICT (party_id, user_id) DO NOTHING
       RETURNING *`,
      [partyId, userId]
    );
    if (result.rows.length > 0) {
      inserted.push(mapPartyMemberRow(result.rows[0]));
    }
  }
  return inserted;
}

export async function respondToInvite(
  partyId: number,
  userId: number,
  accept: boolean
): Promise<PartyMemberRow | null> {
  const newStatus = accept ? 'accepted' : 'declined';
  const result = await query(
    `UPDATE party_members
     SET status = $1, responded_at = NOW(), updated_at = NOW()
     WHERE party_id = $2 AND user_id = $3 AND status = 'invited'
     RETURNING *`,
    [newStatus, partyId, userId]
  );
  return result.rows.length > 0 ? mapPartyMemberRow(result.rows[0]) : null;
}

export async function checkAutoReady(partyId: number): Promise<boolean> {
  // Check if all members have responded
  const pendingResult = await query(
    `SELECT COUNT(*) AS pending_count
     FROM party_members
     WHERE party_id = $1 AND status = 'invited'`,
    [partyId]
  );
  const pendingCount = Number(pendingResult.rows[0].pending_count);
  if (pendingCount > 0) return false;

  // Check if at least 2 members accepted (including leader)
  const acceptedResult = await query(
    `SELECT COUNT(*) AS accepted_count
     FROM party_members
     WHERE party_id = $1 AND status = 'accepted'`,
    [partyId]
  );
  const acceptedCount = Number(acceptedResult.rows[0].accepted_count);
  if (acceptedCount < 2) return false;

  // All responded and >=2 accepted — set to ready
  await query(
    `UPDATE parties SET status = 'ready', updated_at = NOW()
     WHERE id = $1 AND status = 'forming'`,
    [partyId]
  );
  return true;
}

export async function updateStatus(
  partyId: number,
  status: string
): Promise<void> {
  await query(
    `UPDATE parties SET status = $2, updated_at = NOW()
     WHERE id = $1`,
    [partyId, status]
  );
}

export async function setMatchId(
  partyId: number,
  matchId: number
): Promise<void> {
  await query(
    `UPDATE parties SET match_id = $2, status = 'in_match', updated_at = NOW()
     WHERE id = $1`,
    [partyId, matchId]
  );
}

export async function findMembers(partyId: number): Promise<PartyMemberRow[]> {
  const result = await query(
    `SELECT pm.id, pm.party_id, pm.user_id, pm.status, pm.responded_at, pm.created_at,
            u.display_name AS user_name, u.photo_url AS user_photo_url
     FROM party_members pm
     LEFT JOIN users u ON u.id = pm.user_id
     WHERE pm.party_id = $1
     ORDER BY pm.created_at ASC`,
    [partyId]
  );
  return result.rows.map(mapPartyMemberRow);
}

export async function disbandParty(partyId: number): Promise<void> {
  await query(
    `UPDATE parties SET status = 'disbanded', updated_at = NOW()
     WHERE id = $1`,
    [partyId]
  );
}

import { query } from '../config/database';
import { ValidationError } from '../utils/errors';

// ============================================================
// Row interfaces
// ============================================================

export interface MatchRow {
  id: number;
  hostId: number;
  hostName: string | null;
  hostPhotoUrl: string | null;
  bookingId: number | null;
  venueId: number | null;
  venueName: string | null;
  sportType: string;
  matchType: string;
  status: string;
  visibility: string;
  title: string;
  description: string | null;
  matchDate: string;
  startTime: string;
  endTime: string;
  minPlayers: number;
  maxPlayers: number;
  currentPlayers: number;
  minSkillLevel: number | null;
  maxSkillLevel: number | null;
  locationName: string | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  isFree: boolean;
  costPerPlayer: number;
  paymentType: string;
  timeSlotId: number | null;
  totalPrice: number;
  pricePerPlayer: number;
  currency: string;
  recurrenceRuleId: number | null;
  parentMatchId: number | null;
  participants: ParticipantRow[];
  createdAt: string;
  updatedAt: string;
}

export interface ParticipantRow {
  id: number;
  matchId: number;
  userId: number;
  userName: string | null;
  userPhotoUrl: string | null;
  status: string;
  role: string;
  joinedAt: string;
  createdAt: string;
}

export interface ChatMessageRow {
  id: number;
  matchId: number;
  senderId: number;
  senderName: string | null;
  senderPhotoUrl: string | null;
  content: string;
  createdAt: string;
}

export interface PlayerRatingRow {
  id: number;
  matchId: number;
  raterId: number;
  raterName: string | null;
  ratedId: number;
  ratedName: string | null;
  skillRating: number;
  sportsmanshipRating: number;
  punctualityRating: number;
  comment: string | null;
  createdAt: string;
}

export interface RecurrenceRuleRow {
  id: number;
  hostId: number;
  frequency: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  sportType: string;
  title: string;
  venueId: number | null;
  locationName: string | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  minPlayers: number;
  maxPlayers: number;
  minSkillLevel: number | null;
  maxSkillLevel: number | null;
  isActive: boolean;
  nextOccurrenceDate: string | null;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapMatchRow(row: any): Omit<MatchRow, 'participants'> {
  return {
    id: Number(row.id),
    hostId: Number(row.host_id),
    hostName: row.host_name ?? null,
    hostPhotoUrl: row.host_photo_url ?? null,
    bookingId: row.booking_id ? Number(row.booking_id) : null,
    venueId: row.venue_id ? Number(row.venue_id) : null,
    venueName: row.venue_name ?? null,
    sportType: row.sport_type,
    matchType: row.match_type,
    status: row.status,
    visibility: row.visibility,
    title: row.title,
    description: row.description,
    matchDate: row.match_date instanceof Date ? row.match_date.toISOString().split('T')[0] : row.match_date,
    startTime: row.start_time,
    endTime: row.end_time,
    minPlayers: row.min_players,
    maxPlayers: row.max_players,
    currentPlayers: row.current_players,
    minSkillLevel: row.min_skill_level,
    maxSkillLevel: row.max_skill_level,
    locationName: row.location_name,
    address: row.address,
    latitude: row.latitude ? Number(row.latitude) : null,
    longitude: row.longitude ? Number(row.longitude) : null,
    isFree: row.is_free,
    costPerPlayer: Number(row.cost_per_player ?? 0),
    paymentType: row.payment_type ?? 'host_pays',
    timeSlotId: row.time_slot_id ? Number(row.time_slot_id) : null,
    totalPrice: Number(row.total_price ?? 0),
    pricePerPlayer: Number(row.price_per_player ?? 0),
    currency: row.currency ?? 'MKD',
    recurrenceRuleId: row.recurrence_rule_id ? Number(row.recurrence_rule_id) : null,
    parentMatchId: row.parent_match_id ? Number(row.parent_match_id) : null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapParticipantRow(row: any): ParticipantRow {
  return {
    id: Number(row.id),
    matchId: Number(row.match_id),
    userId: Number(row.user_id),
    userName: row.user_name ?? row.display_name ?? null,
    userPhotoUrl: row.user_photo_url ?? row.photo_url ?? null,
    status: row.status,
    role: row.role,
    joinedAt: row.joined_at?.toISOString?.() ?? row.joined_at,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapChatMessageRow(row: any): ChatMessageRow {
  return {
    id: Number(row.id),
    matchId: Number(row.match_id),
    senderId: Number(row.sender_id),
    senderName: row.sender_name ?? row.display_name ?? null,
    senderPhotoUrl: row.sender_photo_url ?? row.photo_url ?? null,
    content: row.content,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapRatingRow(row: any): PlayerRatingRow {
  return {
    id: Number(row.id),
    matchId: Number(row.match_id),
    raterId: Number(row.rater_id),
    raterName: row.rater_name ?? null,
    ratedId: Number(row.rated_id),
    ratedName: row.rated_name ?? null,
    skillRating: row.skill_rating,
    sportsmanshipRating: row.sportsmanship_rating,
    punctualityRating: row.punctuality_rating,
    comment: row.comment,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapRecurrenceRow(row: any): RecurrenceRuleRow {
  return {
    id: Number(row.id),
    hostId: Number(row.host_id),
    frequency: row.frequency,
    dayOfWeek: row.day_of_week,
    startTime: row.start_time,
    endTime: row.end_time,
    sportType: row.sport_type,
    title: row.title,
    venueId: row.venue_id ? Number(row.venue_id) : null,
    locationName: row.location_name,
    address: row.address,
    latitude: row.latitude ? Number(row.latitude) : null,
    longitude: row.longitude ? Number(row.longitude) : null,
    minPlayers: row.min_players,
    maxPlayers: row.max_players,
    minSkillLevel: row.min_skill_level,
    maxSkillLevel: row.max_skill_level,
    isActive: row.is_active,
    nextOccurrenceDate: row.next_occurrence_date instanceof Date
      ? row.next_occurrence_date.toISOString().split('T')[0]
      : row.next_occurrence_date,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

// ============================================================
// Match CRUD
// ============================================================

const MATCH_SELECT = `
  m.id, m.host_id, m.booking_id, m.venue_id, m.sport_type, m.match_type,
  m.status, m.visibility, m.title, m.description, m.match_date,
  m.start_time, m.end_time, m.min_players, m.max_players, m.current_players,
  m.min_skill_level, m.max_skill_level, m.location_name, m.address,
  m.latitude, m.longitude, m.is_free, m.cost_per_player,
  m.payment_type, m.time_slot_id, m.total_price, m.price_per_player, m.currency,
  m.recurrence_rule_id, m.parent_match_id, m.created_at, m.updated_at,
  u.display_name AS host_name, u.photo_url AS host_photo_url,
  v.name AS venue_name`;

export async function create(hostId: number, data: {
  bookingId?: number; venueId?: number; sportType: string; matchType: string;
  visibility?: string; title: string; description?: string;
  matchDate: string; startTime: string; endTime: string;
  minPlayers: number; maxPlayers: number;
  minSkillLevel?: number; maxSkillLevel?: number;
  locationName?: string; address?: string;
  latitude?: number; longitude?: number;
  isFree?: boolean; costPerPlayer?: number;
  paymentType?: string; timeSlotId?: number;
  totalPrice?: number; pricePerPlayer?: number; currency?: string;
}): Promise<MatchRow> {
  const result = await query(
    `INSERT INTO matches (host_id, booking_id, venue_id, sport_type, match_type,
       visibility, title, description, match_date, start_time, end_time,
       min_players, max_players, current_players, min_skill_level, max_skill_level,
       location_name, address, latitude, longitude, is_free, cost_per_player,
       payment_type, time_slot_id, total_price, price_per_player, currency, status)
     VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,1,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23,$24,$25,$26,'open')
     RETURNING *`,
    [
      hostId, data.bookingId || null, data.venueId || null,
      data.sportType, data.matchType, data.visibility || 'public',
      data.title, data.description || null, data.matchDate,
      data.startTime, data.endTime, data.minPlayers, data.maxPlayers,
      data.minSkillLevel || 1, data.maxSkillLevel || 5,
      data.locationName || null, data.address || null,
      data.latitude || null, data.longitude || null,
      data.isFree !== false, data.costPerPlayer || 0,
      data.paymentType || 'host_pays', data.timeSlotId || null,
      data.totalPrice || 0, data.pricePerPlayer || 0, data.currency || 'MKD',
    ]
  );
  const match = mapMatchRow(result.rows[0]);

  // Auto-add host as participant
  await query(
    `INSERT INTO match_participants (match_id, user_id, status, role)
     VALUES ($1, $2, 'approved', 'host')`,
    [match.id, hostId]
  );

  return { ...match, participants: [] };
}

export async function findById(id: number): Promise<MatchRow | null> {
  const result = await query(
    `SELECT ${MATCH_SELECT}
     FROM matches m
     LEFT JOIN users u ON u.id = m.host_id
     LEFT JOIN venues v ON v.id = m.venue_id
     WHERE m.id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;

  const match = mapMatchRow(result.rows[0]);
  const participants = await findParticipants(id);
  return { ...match, participants };
}

export async function findAll(filters: {
  sportType?: string; status?: string; minSkillLevel?: number;
  maxSkillLevel?: number; matchDate?: string; matchType?: string; hostId?: number;
}): Promise<MatchRow[]> {
  let sql = `SELECT ${MATCH_SELECT}
     FROM matches m
     LEFT JOIN users u ON u.id = m.host_id
     LEFT JOIN venues v ON v.id = m.venue_id
     WHERE 1=1`;
  const params: any[] = [];
  let idx = 1;

  if (filters.sportType) { sql += ` AND m.sport_type = $${idx++}`; params.push(filters.sportType); }
  if (filters.status) { sql += ` AND m.status = $${idx++}`; params.push(filters.status); }
  if (filters.matchDate) { sql += ` AND m.match_date = $${idx++}`; params.push(filters.matchDate); }
  if (filters.matchType) { sql += ` AND m.match_type = $${idx++}`; params.push(filters.matchType); }
  if (filters.hostId) { sql += ` AND m.host_id = $${idx++}`; params.push(filters.hostId); }
  if (filters.minSkillLevel) { sql += ` AND m.max_skill_level >= $${idx++}`; params.push(filters.minSkillLevel); }
  if (filters.maxSkillLevel) { sql += ` AND m.min_skill_level <= $${idx++}`; params.push(filters.maxSkillLevel); }

  sql += ` ORDER BY m.match_date ASC, m.start_time ASC`;

  const result = await query(sql, params);
  return result.rows.map((r: any) => ({ ...mapMatchRow(r), participants: [] }));
}

export async function findNearby(lat: number, lng: number, radiusKm: number): Promise<MatchRow[]> {
  const result = await query(
    `SELECT sub.* FROM (
       SELECT ${MATCH_SELECT},
         (6371 * acos(
           LEAST(1.0, cos(radians($1)) * cos(radians(m.latitude))
           * cos(radians(m.longitude) - radians($2))
           + sin(radians($1)) * sin(radians(m.latitude)))
         )) AS distance_km
       FROM matches m
       LEFT JOIN users u ON u.id = m.host_id
       LEFT JOIN venues v ON v.id = m.venue_id
       WHERE m.status = 'open' AND m.visibility = 'public'
         AND m.latitude IS NOT NULL AND m.longitude IS NOT NULL
     ) sub
     WHERE sub.distance_km <= $3
     ORDER BY sub.distance_km ASC`,
    [lat, lng, radiusKm]
  );
  return result.rows.map((r: any) => ({ ...mapMatchRow(r), participants: [] }));
}

export async function findByHostId(hostId: number): Promise<MatchRow[]> {
  const result = await query(
    `SELECT ${MATCH_SELECT}
     FROM matches m
     LEFT JOIN users u ON u.id = m.host_id
     LEFT JOIN venues v ON v.id = m.venue_id
     WHERE m.host_id = $1
     ORDER BY m.match_date DESC`,
    [hostId]
  );
  return result.rows.map((r: any) => ({ ...mapMatchRow(r), participants: [] }));
}

export async function findByParticipantId(userId: number): Promise<MatchRow[]> {
  const result = await query(
    `SELECT ${MATCH_SELECT}
     FROM matches m
     LEFT JOIN users u ON u.id = m.host_id
     LEFT JOIN venues v ON v.id = m.venue_id
     INNER JOIN match_participants mp ON mp.match_id = m.id
     WHERE mp.user_id = $1 AND mp.status = 'approved'
     ORDER BY m.match_date DESC`,
    [userId]
  );
  return result.rows.map((r: any) => ({ ...mapMatchRow(r), participants: [] }));
}

export async function update(id: number, data: Record<string, any>): Promise<MatchRow | null> {
  const fieldMap: Record<string, string> = {
    title: 'title', description: 'description', matchDate: 'match_date',
    startTime: 'start_time', endTime: 'end_time', minPlayers: 'min_players',
    maxPlayers: 'max_players', minSkillLevel: 'min_skill_level',
    maxSkillLevel: 'max_skill_level', locationName: 'location_name',
    address: 'address', latitude: 'latitude', longitude: 'longitude',
    isFree: 'is_free', costPerPlayer: 'cost_per_player', visibility: 'visibility',
    status: 'status',
    paymentType: 'payment_type', timeSlotId: 'time_slot_id',
    totalPrice: 'total_price', pricePerPlayer: 'price_per_player', currency: 'currency',
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
    `UPDATE matches SET ${setClauses.join(', ')} WHERE id = $1 RETURNING *`,
    params
  );
  if (result.rows.length === 0) return null;
  const participants = await findParticipants(id);
  return { ...mapMatchRow(result.rows[0]), participants };
}

export async function updateStatus(id: number, status: string): Promise<MatchRow | null> {
  return update(id, { status });
}

// ============================================================
// Participants
// ============================================================

export async function addParticipant(matchId: number, userId: number, autoApprove: boolean = false): Promise<ParticipantRow> {
  const existing = await query(
    `SELECT id FROM match_participants WHERE match_id = $1 AND user_id = $2`,
    [matchId, userId]
  );
  if (existing.rows.length > 0) {
    throw new ValidationError('User is already a participant in this match');
  }

  const status = autoApprove ? 'approved' : 'pending';
  const result = await query(
    `INSERT INTO match_participants (match_id, user_id, status, role)
     VALUES ($1, $2, $3, 'player')
     RETURNING *`,
    [matchId, userId, status]
  );

  if (autoApprove) {
    await query(
      `UPDATE matches SET current_players = current_players + 1, updated_at = NOW()
       WHERE id = $1`,
      [matchId]
    );
    // Auto-set to full if needed
    await query(
      `UPDATE matches SET status = 'full', updated_at = NOW()
       WHERE id = $1 AND current_players >= max_players AND status = 'open'`,
      [matchId]
    );
  }

  return mapParticipantRow(result.rows[0]);
}

export async function updateParticipantStatus(matchId: number, participantId: number, status: string): Promise<ParticipantRow | null> {
  const prev = await query(
    `SELECT status FROM match_participants WHERE id = $2 AND match_id = $1`,
    [matchId, participantId]
  );
  if (prev.rows.length === 0) return null;
  const oldStatus = prev.rows[0].status;

  const result = await query(
    `UPDATE match_participants SET status = $3, updated_at = NOW()
     WHERE id = $2 AND match_id = $1
     RETURNING *`,
    [matchId, participantId, status]
  );
  if (result.rows.length === 0) return null;

  if (status === 'approved' && oldStatus !== 'approved') {
    await query(
      `UPDATE matches SET current_players = current_players + 1, updated_at = NOW()
       WHERE id = $1`,
      [matchId]
    );
    await query(
      `UPDATE matches SET status = 'full', updated_at = NOW()
       WHERE id = $1 AND current_players >= max_players AND status = 'open'`,
      [matchId]
    );
  } else if ((status === 'declined' || status === 'left') && oldStatus === 'approved') {
    await query(
      `UPDATE matches SET current_players = GREATEST(current_players - 1, 0), updated_at = NOW()
       WHERE id = $1`,
      [matchId]
    );
    await query(
      `UPDATE matches SET status = 'open', updated_at = NOW()
       WHERE id = $1 AND status = 'full' AND current_players < max_players`,
      [matchId]
    );
  }

  return mapParticipantRow(result.rows[0]);
}

export async function removeParticipant(matchId: number, userId: number): Promise<boolean> {
  const check = await query(
    `SELECT status FROM match_participants WHERE match_id = $1 AND user_id = $2`,
    [matchId, userId]
  );
  if (check.rows.length === 0) return false;

  const wasApproved = check.rows[0].status === 'approved';

  await query(
    `DELETE FROM match_participants WHERE match_id = $1 AND user_id = $2`,
    [matchId, userId]
  );

  if (wasApproved) {
    await query(
      `UPDATE matches SET current_players = GREATEST(current_players - 1, 0), updated_at = NOW()
       WHERE id = $1`,
      [matchId]
    );
    await query(
      `UPDATE matches SET status = 'open', updated_at = NOW()
       WHERE id = $1 AND status = 'full' AND current_players < max_players`,
      [matchId]
    );
  }

  return true;
}

export async function findParticipants(matchId: number): Promise<ParticipantRow[]> {
  const result = await query(
    `SELECT mp.id, mp.match_id, mp.user_id, mp.status, mp.role, mp.joined_at, mp.created_at,
            u.display_name AS user_name, u.photo_url AS user_photo_url
     FROM match_participants mp
     LEFT JOIN users u ON u.id = mp.user_id
     WHERE mp.match_id = $1
     ORDER BY mp.joined_at ASC`,
    [matchId]
  );
  return result.rows.map(mapParticipantRow);
}

// ============================================================
// Chat
// ============================================================

export async function addChatMessage(matchId: number, senderId: number, content: string): Promise<ChatMessageRow> {
  const result = await query(
    `INSERT INTO match_chat_messages (match_id, sender_id, content)
     VALUES ($1, $2, $3)
     RETURNING *`,
    [matchId, senderId, content]
  );
  return mapChatMessageRow(result.rows[0]);
}

export async function findChatMessages(matchId: number, since?: string, limit: number = 50): Promise<ChatMessageRow[]> {
  let sql = `
    SELECT cm.id, cm.match_id, cm.sender_id, cm.content, cm.created_at,
           u.display_name AS sender_name, u.photo_url AS sender_photo_url
    FROM match_chat_messages cm
    LEFT JOIN users u ON u.id = cm.sender_id
    WHERE cm.match_id = $1`;
  const params: any[] = [matchId];

  if (since) {
    sql += ` AND cm.created_at > $2`;
    params.push(since);
  }

  sql += ` ORDER BY cm.created_at ASC LIMIT $${params.length + 1}`;
  params.push(limit);

  const result = await query(sql, params);
  return result.rows.map(mapChatMessageRow);
}

// ============================================================
// Player Ratings
// ============================================================

export async function createPlayerRating(data: {
  matchId: number; raterId: number; ratedId: number;
  skillRating: number; sportsmanshipRating: number;
  punctualityRating: number; comment?: string;
}): Promise<PlayerRatingRow> {
  const result = await query(
    `INSERT INTO player_ratings (match_id, rater_id, rated_id, skill_rating,
       sportsmanship_rating, punctuality_rating, comment)
     VALUES ($1, $2, $3, $4, $5, $6, $7)
     RETURNING *`,
    [data.matchId, data.raterId, data.ratedId, data.skillRating,
     data.sportsmanshipRating, data.punctualityRating, data.comment || null]
  );

  // Update user aggregate ratings
  await query(
    `UPDATE users SET
       avg_player_skill_rating = sub.avg_skill,
       avg_player_sportsmanship_rating = sub.avg_sportsmanship,
       avg_player_punctuality_rating = sub.avg_punctuality,
       total_player_ratings = sub.total_ratings,
       updated_at = NOW()
     FROM (
       SELECT rated_id,
              AVG(skill_rating) AS avg_skill,
              AVG(sportsmanship_rating) AS avg_sportsmanship,
              AVG(punctuality_rating) AS avg_punctuality,
              COUNT(*) AS total_ratings
       FROM player_ratings
       WHERE rated_id = $1
       GROUP BY rated_id
     ) sub
     WHERE users.id = sub.rated_id`,
    [data.ratedId]
  );

  return mapRatingRow(result.rows[0]);
}

export async function findRatingsByMatch(matchId: number): Promise<PlayerRatingRow[]> {
  const result = await query(
    `SELECT pr.id, pr.match_id, pr.rater_id, pr.rated_id,
            pr.skill_rating, pr.sportsmanship_rating, pr.punctuality_rating,
            pr.comment, pr.created_at,
            rater.display_name AS rater_name,
            rated.display_name AS rated_name
     FROM player_ratings pr
     LEFT JOIN users rater ON rater.id = pr.rater_id
     LEFT JOIN users rated ON rated.id = pr.rated_id
     WHERE pr.match_id = $1
     ORDER BY pr.created_at DESC`,
    [matchId]
  );
  return result.rows.map(mapRatingRow);
}

export async function findRatingsByRatedUser(userId: number): Promise<PlayerRatingRow[]> {
  const result = await query(
    `SELECT pr.id, pr.match_id, pr.rater_id, pr.rated_id,
            pr.skill_rating, pr.sportsmanship_rating, pr.punctuality_rating,
            pr.comment, pr.created_at,
            rater.display_name AS rater_name,
            rated.display_name AS rated_name
     FROM player_ratings pr
     LEFT JOIN users rater ON rater.id = pr.rater_id
     LEFT JOIN users rated ON rated.id = pr.rated_id
     WHERE pr.rated_id = $1
     ORDER BY pr.created_at DESC`,
    [userId]
  );
  return result.rows.map(mapRatingRow);
}

// ============================================================
// Recurrence Rules
// ============================================================

export async function createRecurrenceRule(hostId: number, data: {
  frequency: string; dayOfWeek: string; startTime: string; endTime: string;
  sportType: string; title: string; venueId?: number;
  locationName?: string; address?: string;
  latitude?: number; longitude?: number;
  minPlayers: number; maxPlayers: number;
  minSkillLevel?: number; maxSkillLevel?: number;
}): Promise<RecurrenceRuleRow> {
  const result = await query(
    `INSERT INTO match_recurrence_rules (host_id, frequency, day_of_week, start_time, end_time,
       sport_type, title, venue_id, location_name, address, latitude, longitude,
       min_players, max_players, min_skill_level, max_skill_level)
     VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16)
     RETURNING *`,
    [
      hostId, data.frequency, data.dayOfWeek, data.startTime, data.endTime,
      data.sportType, data.title, data.venueId || null,
      data.locationName || null, data.address || null,
      data.latitude || null, data.longitude || null,
      data.minPlayers, data.maxPlayers,
      data.minSkillLevel || 1, data.maxSkillLevel || 5,
    ]
  );
  return mapRecurrenceRow(result.rows[0]);
}

export async function findRecurrenceRuleById(id: number): Promise<RecurrenceRuleRow | null> {
  const result = await query(`SELECT * FROM match_recurrence_rules WHERE id = $1`, [id]);
  return result.rows.length > 0 ? mapRecurrenceRow(result.rows[0]) : null;
}

export async function findActiveRecurrenceRules(): Promise<RecurrenceRuleRow[]> {
  const result = await query(`SELECT * FROM match_recurrence_rules WHERE is_active = TRUE ORDER BY id`);
  return result.rows.map(mapRecurrenceRow);
}

export async function updateRecurrenceRule(id: number, data: Record<string, any>): Promise<RecurrenceRuleRow | null> {
  const fieldMap: Record<string, string> = {
    frequency: 'frequency', dayOfWeek: 'day_of_week', startTime: 'start_time',
    endTime: 'end_time', sportType: 'sport_type', title: 'title',
    venueId: 'venue_id', locationName: 'location_name', address: 'address',
    latitude: 'latitude', longitude: 'longitude', minPlayers: 'min_players',
    maxPlayers: 'max_players', minSkillLevel: 'min_skill_level',
    maxSkillLevel: 'max_skill_level', isActive: 'is_active',
    nextOccurrenceDate: 'next_occurrence_date',
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
  if (setClauses.length === 0) return findRecurrenceRuleById(id);

  setClauses.push('updated_at = NOW()');
  const result = await query(
    `UPDATE match_recurrence_rules SET ${setClauses.join(', ')} WHERE id = $1 RETURNING *`,
    params
  );
  return result.rows.length > 0 ? mapRecurrenceRow(result.rows[0]) : null;
}

export async function deactivateRecurrenceRule(id: number): Promise<RecurrenceRuleRow | null> {
  return updateRecurrenceRule(id, { isActive: false });
}

// ============================================================
// Search
// ============================================================

export async function search(q: string): Promise<MatchRow[]> {
  const result = await query(
    `SELECT ${MATCH_SELECT}
     FROM matches m
     LEFT JOIN users u ON u.id = m.host_id
     LEFT JOIN venues v ON v.id = m.venue_id
     WHERE m.status = 'open' AND (m.title ILIKE $1 OR m.location_name ILIKE $1 OR m.address ILIKE $1)
     ORDER BY m.match_date ASC
     LIMIT 50`,
    [`%${q}%`]
  );
  return result.rows.map((r: any) => ({ ...mapMatchRow(r), participants: [] }));
}

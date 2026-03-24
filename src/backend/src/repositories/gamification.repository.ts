import { query } from '../config/database';

// ── Row interfaces ──────────────────────────────────────────────────

export interface XpTransactionRow {
  id: number;
  userId: number;
  amount: number;
  sourceType: string;
  sourceId: number | null;
  description: string | null;
  createdAt: string;
}

export interface PlayerLevelRow {
  id: number;
  userId: number;
  totalXp: number;
  currentLevel: number;
  xpToNextLevel: number;
  createdAt: string;
  updatedAt: string;
}

export interface AchievementRow {
  id: number;
  name: string;
  description: string;
  icon: string;
  category: string;
  xpReward: number;
  criteriaType: string;
  criteriaValue: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PlayerAchievementRow {
  id: number;
  userId: number;
  achievementId: number;
  earnedAt: string;
  // Achievement details (from join)
  name: string;
  description: string;
  icon: string;
  category: string;
  xpReward: number;
}

export interface PlayerStats {
  totalBookings: number;
  completedBookings: number;
  totalMatches: number;
  completedMatches: number;
  totalReviews: number;
  totalFriends: number;
  uniqueSportsBooked: number;
  hasEarlyBirdBooking: boolean;
  hasNightOwlBooking: boolean;
  totalHoursPlayed: number;
  memberSinceDays: number;
  favoritesSport: string | null;
}

// ── Row mappers ─────────────────────────────────────────────────────

function mapXpTransactionRow(row: any): XpTransactionRow {
  return {
    id: row.id,
    userId: row.user_id,
    amount: Number(row.amount),
    sourceType: row.source_type,
    sourceId: row.source_id ? Number(row.source_id) : null,
    description: row.description,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapPlayerLevelRow(row: any): PlayerLevelRow {
  return {
    id: row.id,
    userId: row.user_id,
    totalXp: Number(row.total_xp),
    currentLevel: Number(row.current_level),
    xpToNextLevel: Number(row.xp_to_next_level),
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapAchievementRow(row: any): AchievementRow {
  return {
    id: row.id,
    name: row.name,
    description: row.description,
    icon: row.icon,
    category: row.category,
    xpReward: Number(row.xp_reward),
    criteriaType: row.criteria_type,
    criteriaValue: Number(row.criteria_value),
    isActive: row.is_active,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapPlayerAchievementRow(row: any): PlayerAchievementRow {
  return {
    id: row.id,
    userId: row.user_id,
    achievementId: row.achievement_id,
    earnedAt: row.earned_at?.toISOString?.() ?? row.earned_at,
    name: row.name,
    description: row.description,
    icon: row.icon,
    category: row.category,
    xpReward: Number(row.xp_reward),
  };
}

// ── XP Level ────────────────────────────────────────────────────────

export async function getPlayerLevel(userId: number): Promise<PlayerLevelRow | null> {
  const result = await query(
    `SELECT id, user_id, total_xp, current_level, xp_to_next_level,
            created_at, updated_at
     FROM player_levels
     WHERE user_id = $1`,
    [userId]
  );
  return result.rows.length > 0 ? mapPlayerLevelRow(result.rows[0]) : null;
}

export async function getOrCreatePlayerLevel(userId: number): Promise<PlayerLevelRow> {
  const existing = await getPlayerLevel(userId);
  if (existing) return existing;

  const result = await query(
    `INSERT INTO player_levels (user_id, total_xp, current_level, xp_to_next_level)
     VALUES ($1, 0, 1, 100)
     ON CONFLICT (user_id) DO NOTHING
     RETURNING id, user_id, total_xp, current_level, xp_to_next_level,
               created_at, updated_at`,
    [userId]
  );

  if (result.rows.length > 0) return mapPlayerLevelRow(result.rows[0]);

  // Race condition: another transaction inserted between SELECT and INSERT
  return (await getPlayerLevel(userId))!;
}

// ── XP Transactions ─────────────────────────────────────────────────

export async function addXpTransaction(
  userId: number,
  amount: number,
  sourceType: string,
  sourceId: number | null,
  description: string
): Promise<XpTransactionRow> {
  // Ensure the player level row exists before recording a transaction
  await getOrCreatePlayerLevel(userId);

  const result = await query(
    `INSERT INTO xp_transactions (user_id, amount, source_type, source_id, description)
     VALUES ($1, $2, $3, $4, $5)
     RETURNING id, user_id, amount, source_type, source_id, description, created_at`,
    [userId, amount, sourceType, sourceId, description]
  );
  return mapXpTransactionRow(result.rows[0]);
}

export async function getXpHistory(userId: number, limit = 50): Promise<XpTransactionRow[]> {
  const result = await query(
    `SELECT id, user_id, amount, source_type, source_id, description, created_at
     FROM xp_transactions
     WHERE user_id = $1
     ORDER BY created_at DESC
     LIMIT $2`,
    [userId, limit]
  );
  return result.rows.map(mapXpTransactionRow);
}

// ── Level calculation ───────────────────────────────────────────────
// Formula:
//   level = floor(sqrt(totalXp / 100)) + 1
//   xpToNextLevel = (level^2 * 100) - totalXp

export async function updatePlayerLevel(userId: number): Promise<PlayerLevelRow> {
  await getOrCreatePlayerLevel(userId);

  // Sum all XP from xp_transactions
  const sumResult = await query(
    `SELECT COALESCE(SUM(amount), 0) AS total_xp
     FROM xp_transactions
     WHERE user_id = $1`,
    [userId]
  );
  const totalXp = Number(sumResult.rows[0].total_xp);

  const currentLevel = Math.floor(Math.sqrt(totalXp / 100)) + 1;
  const xpToNextLevel = (currentLevel * currentLevel * 100) - totalXp;

  const result = await query(
    `UPDATE player_levels
     SET total_xp = $2, current_level = $3, xp_to_next_level = $4
     WHERE user_id = $1
     RETURNING id, user_id, total_xp, current_level, xp_to_next_level,
               created_at, updated_at`,
    [userId, totalXp, currentLevel, xpToNextLevel]
  );
  return mapPlayerLevelRow(result.rows[0]);
}

// ── XP Redemption ───────────────────────────────────────────────────

/**
 * Returns the total XP-based discount (in denar) applied to a booking.
 * Sums all xp_redemption transactions for the given booking (source_id = bookingId).
 * The amounts are negative, so we negate the sum to get a positive discount.
 */
export async function getXpRedemptionDiscount(bookingId: number): Promise<number> {
  const result = await query(
    `SELECT COALESCE(SUM(ABS(amount)), 0) AS total_xp_redeemed
     FROM xp_transactions
     WHERE source_type = 'xp_redemption' AND source_id = $1`,
    [bookingId]
  );
  const totalXpRedeemed = Number(result.rows[0].total_xp_redeemed);
  // 100 XP = 1 MKD discount
  return totalXpRedeemed / 100;
}

// ── Achievements ────────────────────────────────────────────────────

export async function getAllAchievements(): Promise<AchievementRow[]> {
  const result = await query(
    `SELECT id, name, description, icon, category, xp_reward,
            criteria_type, criteria_value, is_active,
            created_at, updated_at
     FROM achievements
     WHERE is_active = true
     ORDER BY category, criteria_value`
  );
  return result.rows.map(mapAchievementRow);
}

export async function getPlayerAchievements(userId: number): Promise<PlayerAchievementRow[]> {
  const result = await query(
    `SELECT pa.id, pa.user_id, pa.achievement_id, pa.earned_at,
            a.name, a.description, a.icon, a.category, a.xp_reward
     FROM player_achievements pa
     JOIN achievements a ON a.id = pa.achievement_id
     WHERE pa.user_id = $1
     ORDER BY pa.earned_at DESC`,
    [userId]
  );
  return result.rows.map(mapPlayerAchievementRow);
}

export async function hasAchievement(userId: number, achievementId: number): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM player_achievements
     WHERE user_id = $1 AND achievement_id = $2
     LIMIT 1`,
    [userId, achievementId]
  );
  return result.rows.length > 0;
}

export async function awardAchievement(userId: number, achievementId: number): Promise<PlayerAchievementRow> {
  const result = await query(
    `INSERT INTO player_achievements (user_id, achievement_id)
     VALUES ($1, $2)
     RETURNING id, user_id, achievement_id, earned_at`,
    [userId, achievementId]
  );

  // Fetch the full row with achievement details
  const full = await query(
    `SELECT pa.id, pa.user_id, pa.achievement_id, pa.earned_at,
            a.name, a.description, a.icon, a.category, a.xp_reward
     FROM player_achievements pa
     JOIN achievements a ON a.id = pa.achievement_id
     WHERE pa.id = $1`,
    [result.rows[0].id]
  );
  return mapPlayerAchievementRow(full.rows[0]);
}

// ── Stats for achievement checking ──────────────────────────────────

export async function getPlayerStats(userId: number): Promise<PlayerStats> {
  const result = await query(
    `SELECT
       -- Booking counts
       (SELECT COUNT(*) FROM bookings WHERE player_id = $1)::int
         AS total_bookings,
       (SELECT COUNT(*) FROM bookings WHERE player_id = $1 AND status = 'completed')::int
         AS completed_bookings,

       -- Match counts (user participated as host OR was in match_participants)
       (SELECT COUNT(DISTINCT m.id)
        FROM matches m
        LEFT JOIN match_participants mp ON mp.match_id = m.id AND mp.user_id = $1
        WHERE m.host_id = $1 OR mp.user_id = $1)::int
         AS total_matches,
       (SELECT COUNT(DISTINCT m.id)
        FROM matches m
        LEFT JOIN match_participants mp ON mp.match_id = m.id AND mp.user_id = $1
        WHERE (m.host_id = $1 OR mp.user_id = $1) AND m.status = 'completed')::int
         AS completed_matches,

       -- Review count
       (SELECT COUNT(*) FROM reviews WHERE player_id = $1)::int
         AS total_reviews,

       -- Friend count (accepted friendships where user is either party)
       (SELECT COUNT(*) FROM friendships
        WHERE status = 'accepted' AND (requester_id = $1 OR addressee_id = $1))::int
         AS total_friends,

       -- Unique sports booked (via venue sport_type or coach sport_type)
       (SELECT COUNT(DISTINCT sport)
        FROM (
          SELECT v.sport_type AS sport FROM bookings b
            JOIN venues v ON v.id = b.venue_id
            WHERE b.player_id = $1 AND b.status = 'completed'
          UNION
          SELECT c.sport_type AS sport FROM bookings b
            JOIN coaches c ON c.id = b.coach_id
            WHERE b.player_id = $1 AND b.status = 'completed'
        ) sports)::int
         AS unique_sports_booked,

       -- Early bird: completed booking with 09:00 start
       EXISTS(
         SELECT 1 FROM bookings b
           JOIN time_slots ts ON ts.id = b.time_slot_id
           WHERE b.player_id = $1 AND b.status = 'completed'
             AND ts.start_time = '09:00:00'
       ) AS has_early_bird_booking,

       -- Night owl: completed booking with 21:00 start
       EXISTS(
         SELECT 1 FROM bookings b
           JOIN time_slots ts ON ts.id = b.time_slot_id
           WHERE b.player_id = $1 AND b.status = 'completed'
             AND ts.start_time = '21:00:00'
       ) AS has_night_owl_booking,

       -- Total hours played (each completed booking = 1 hour slot)
       (SELECT COUNT(*) FROM bookings WHERE player_id = $1 AND status = 'completed')::int
         AS total_hours_played,

       -- Member since days
       (SELECT EXTRACT(DAY FROM NOW() - created_at)::int
        FROM users WHERE id = $1)
         AS member_since_days,

       -- Favourite sport (most booked)
       (SELECT sport FROM (
          SELECT v.sport_type AS sport, COUNT(*) AS cnt FROM bookings b
            JOIN venues v ON v.id = b.venue_id
            WHERE b.player_id = $1 AND b.status = 'completed'
            GROUP BY v.sport_type
          UNION ALL
          SELECT c.sport_type AS sport, COUNT(*) AS cnt FROM bookings b
            JOIN coaches c ON c.id = b.coach_id
            WHERE b.player_id = $1 AND b.status = 'completed'
            GROUP BY c.sport_type
        ) combined
        GROUP BY sport
        ORDER BY SUM(cnt) DESC
        LIMIT 1)
         AS favorites_sport`,
    [userId]
  );

  const row = result.rows[0];

  return {
    totalBookings: Number(row.total_bookings),
    completedBookings: Number(row.completed_bookings),
    totalMatches: Number(row.total_matches),
    completedMatches: Number(row.completed_matches),
    totalReviews: Number(row.total_reviews),
    totalFriends: Number(row.total_friends),
    uniqueSportsBooked: Number(row.unique_sports_booked),
    hasEarlyBirdBooking: Boolean(row.has_early_bird_booking),
    hasNightOwlBooking: Boolean(row.has_night_owl_booking),
    totalHoursPlayed: Number(row.total_hours_played),
    memberSinceDays: Number(row.member_since_days) || 0,
    favoritesSport: row.favorites_sport ?? null,
  };
}

import { query } from '../config/database';

export interface UserRow {
  id: number;
  firebaseUid: string;
  email: string;
  displayName: string | null;
  photoUrl: string | null;
  phoneNumber: string | null;
  bio: string | null;
  dateOfBirth: string | null;
  onboardingCompleted: boolean;
  role: string;
  partnerType: string | null;
  isActive: boolean;
  stripeAccountId: string | null;
  stripeOnboardingStatus: string;
  stripePayoutsEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SportExpertiseRow {
  id: number;
  userId: number;
  sportType: string;
  skillLevel: string;
  experienceDuration: string;
  createdAt: string;
  updatedAt: string;
}

const USER_COLUMNS = `id, firebase_uid, email, display_name, photo_url, phone_number,
            bio, date_of_birth, onboarding_completed, role, partner_type, is_active,
            stripe_account_id, stripe_onboarding_status, stripe_payouts_enabled,
            avg_player_skill_rating, avg_player_sportsmanship_rating, avg_player_punctuality_rating,
            total_player_ratings, total_matches_played, created_at, updated_at`;

function mapRow(row: any): UserRow {
  return {
    id: Number(row.id),
    firebaseUid: row.firebase_uid,
    email: row.email,
    displayName: row.display_name,
    photoUrl: row.photo_url,
    phoneNumber: row.phone_number,
    bio: row.bio,
    dateOfBirth: row.date_of_birth ? (row.date_of_birth instanceof Date ? row.date_of_birth.toISOString().split('T')[0] : row.date_of_birth) : null,
    onboardingCompleted: row.onboarding_completed,
    role: row.role,
    partnerType: row.partner_type,
    isActive: row.is_active,
    stripeAccountId: row.stripe_account_id ?? null,
    stripeOnboardingStatus: row.stripe_onboarding_status ?? 'not_started',
    stripePayoutsEnabled: row.stripe_payouts_enabled ?? false,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapExpertiseRow(row: any): SportExpertiseRow {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    sportType: row.sport_type,
    skillLevel: row.skill_level,
    experienceDuration: row.experience_duration,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export async function findByFirebaseUid(uid: string): Promise<UserRow | null> {
  const result = await query(
    `SELECT ${USER_COLUMNS} FROM users WHERE firebase_uid = $1`,
    [uid]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function findById(id: number): Promise<UserRow | null> {
  const result = await query(
    `SELECT ${USER_COLUMNS} FROM users WHERE id = $1`,
    [id]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function create(data: {
  firebaseUid: string;
  email: string;
  displayName?: string;
  photoUrl?: string;
}): Promise<UserRow> {
  const result = await query(
    `INSERT INTO users (firebase_uid, email, display_name, photo_url)
     VALUES ($1, $2, $3, $4)
     RETURNING ${USER_COLUMNS}`,
    [data.firebaseUid, data.email, data.displayName || null, data.photoUrl || null]
  );
  return mapRow(result.rows[0]);
}

export async function update(
  id: number,
  data: { displayName?: string; photoUrl?: string; phoneNumber?: string; bio?: string; dateOfBirth?: string }
): Promise<UserRow> {
  const result = await query(
    `UPDATE users SET
       display_name = COALESCE($2, display_name),
       photo_url = COALESCE($3, photo_url),
       phone_number = COALESCE($4, phone_number),
       bio = COALESCE($5, bio),
       date_of_birth = COALESCE($6::date, date_of_birth)
     WHERE id = $1
     RETURNING ${USER_COLUMNS}`,
    [id, data.displayName, data.photoUrl, data.phoneNumber, data.bio, data.dateOfBirth || null]
  );
  return mapRow(result.rows[0]);
}

export async function setRole(
  id: number,
  role: string,
  partnerType?: string
): Promise<UserRow> {
  const result = await query(
    `UPDATE users SET role = $2, partner_type = $3
     WHERE id = $1
     RETURNING ${USER_COLUMNS}`,
    [id, role, partnerType || null]
  );
  return mapRow(result.rows[0]);
}

export async function setRoleByFirebaseUid(
  firebaseUid: string,
  role: string,
  partnerType?: string
): Promise<UserRow | null> {
  const result = await query(
    `UPDATE users SET role = $2, partner_type = $3
     WHERE firebase_uid = $1
     RETURNING ${USER_COLUMNS}`,
    [firebaseUid, role, partnerType || null]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

// ---- Stripe Connect ----

export async function updateStripeAccount(
  userId: number,
  data: { stripeAccountId?: string; stripeOnboardingStatus?: string; stripePayoutsEnabled?: boolean }
): Promise<UserRow> {
  const result = await query(
    `UPDATE users SET
       stripe_account_id = COALESCE($2, stripe_account_id),
       stripe_onboarding_status = COALESCE($3, stripe_onboarding_status),
       stripe_payouts_enabled = COALESCE($4, stripe_payouts_enabled),
       updated_at = NOW()
     WHERE id = $1
     RETURNING ${USER_COLUMNS}`,
    [userId, data.stripeAccountId ?? null, data.stripeOnboardingStatus ?? null, data.stripePayoutsEnabled ?? null]
  );
  return mapRow(result.rows[0]);
}

export async function findByStripeAccountId(stripeAccountId: string): Promise<UserRow | null> {
  const result = await query(
    `SELECT ${USER_COLUMNS} FROM users WHERE stripe_account_id = $1`,
    [stripeAccountId]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

// ---- Interested Sports ----

export async function findInterestedSports(userId: number): Promise<string[]> {
  const result = await query(
    `SELECT sport_type FROM user_interested_sports WHERE user_id = $1 ORDER BY created_at`,
    [userId]
  );
  return result.rows.map((r: any) => r.sport_type);
}

export async function setInterestedSports(userId: number, sportTypes: string[]): Promise<string[]> {
  await query('DELETE FROM user_interested_sports WHERE user_id = $1', [userId]);
  if (sportTypes.length > 0) {
    const values = sportTypes.map((_, i) => `($1, $${i + 2})`).join(', ');
    await query(
      `INSERT INTO user_interested_sports (user_id, sport_type) VALUES ${values}
       ON CONFLICT (user_id, sport_type) DO NOTHING`,
      [userId, ...sportTypes]
    );
  }
  return findInterestedSports(userId);
}

// ---- Sport Expertise ----

export async function findSportExpertise(userId: number): Promise<SportExpertiseRow[]> {
  const result = await query(
    `SELECT id, user_id, sport_type, skill_level, experience_duration, created_at, updated_at
     FROM user_sport_expertise WHERE user_id = $1 ORDER BY created_at`,
    [userId]
  );
  return result.rows.map(mapExpertiseRow);
}

export async function setSportExpertise(
  userId: number,
  expertise: Array<{ sportType: string; skillLevel: string; experienceDuration: string }>
): Promise<SportExpertiseRow[]> {
  await query('DELETE FROM user_sport_expertise WHERE user_id = $1', [userId]);
  if (expertise.length > 0) {
    const values = expertise.map((_, i) => {
      const base = i * 3 + 2;
      return `($1, $${base}, $${base + 1}, $${base + 2})`;
    }).join(', ');
    const params: any[] = [userId, ...expertise.flatMap(e => [e.sportType, e.skillLevel, e.experienceDuration])];
    await query(
      `INSERT INTO user_sport_expertise (user_id, sport_type, skill_level, experience_duration)
       VALUES ${values}
       ON CONFLICT (user_id, sport_type) DO UPDATE SET
         skill_level = EXCLUDED.skill_level,
         experience_duration = EXCLUDED.experience_duration,
         updated_at = NOW()`,
      params
    );
  }
  return findSportExpertise(userId);
}

// ---- Onboarding ----

export async function setOnboardingCompleted(userId: number): Promise<UserRow> {
  const result = await query(
    `UPDATE users SET onboarding_completed = true, updated_at = NOW()
     WHERE id = $1
     RETURNING ${USER_COLUMNS}`,
    [userId]
  );
  return mapRow(result.rows[0]);
}

// ---- Public Profile ----

export interface PublicProfileRow {
  id: number;
  displayName: string | null;
  photoUrl: string | null;
  bio: string | null;
  role: string;
  avgPlayerSkillRating: number | null;
  avgPlayerSportsmanshipRating: number | null;
  avgPlayerPunctualityRating: number | null;
  totalPlayerRatings: number;
  totalMatchesPlayed: number;
  createdAt: string;
  interestedSports: string[];
  sportExpertise: SportExpertiseRow[];
  recentMatches: RecentMatchRow[];
}

export interface RecentMatchRow {
  id: number;
  title: string;
  sportType: string;
  matchDate: string;
  status: string;
}

function mapRecentMatchRow(row: any): RecentMatchRow {
  return {
    id: Number(row.id),
    title: row.title,
    sportType: row.sport_type,
    matchDate: row.match_date instanceof Date ? row.match_date.toISOString().split('T')[0] : row.match_date,
    status: row.status,
  };
}

export async function findPublicProfile(userId: number): Promise<PublicProfileRow | null> {
  const userResult = await query(
    `SELECT id, display_name, photo_url, bio, role,
            avg_player_skill_rating, avg_player_sportsmanship_rating,
            avg_player_punctuality_rating, total_player_ratings,
            total_matches_played, created_at
     FROM users WHERE id = $1`,
    [userId]
  );
  if (userResult.rows.length === 0) return null;

  const row = userResult.rows[0];

  const [sportsResult, expertiseResult, matchesResult] = await Promise.all([
    query(
      `SELECT sport_type FROM user_interested_sports WHERE user_id = $1 ORDER BY created_at`,
      [userId]
    ),
    query(
      `SELECT id, user_id, sport_type, skill_level, experience_duration, created_at, updated_at
       FROM user_sport_expertise WHERE user_id = $1 ORDER BY created_at`,
      [userId]
    ),
    query(
      `SELECT m.id, m.title, m.sport_type, m.match_date, m.status
       FROM matches m
       INNER JOIN match_participants mp ON mp.match_id = m.id
       WHERE mp.user_id = $1 AND mp.status = 'approved'
       ORDER BY m.match_date DESC
       LIMIT 10`,
      [userId]
    ),
  ]);

  return {
    id: Number(row.id),
    displayName: row.display_name,
    photoUrl: row.photo_url,
    bio: row.bio,
    role: row.role,
    avgPlayerSkillRating: row.avg_player_skill_rating != null ? Number(row.avg_player_skill_rating) : null,
    avgPlayerSportsmanshipRating: row.avg_player_sportsmanship_rating != null ? Number(row.avg_player_sportsmanship_rating) : null,
    avgPlayerPunctualityRating: row.avg_player_punctuality_rating != null ? Number(row.avg_player_punctuality_rating) : null,
    totalPlayerRatings: row.total_player_ratings ?? 0,
    totalMatchesPlayed: row.total_matches_played ?? 0,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    interestedSports: sportsResult.rows.map((r: any) => r.sport_type),
    sportExpertise: expertiseResult.rows.map(mapExpertiseRow),
    recentMatches: matchesResult.rows.map(mapRecentMatchRow),
  };
}

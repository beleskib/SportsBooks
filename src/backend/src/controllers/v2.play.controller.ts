import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import { query } from '../config/database';
import { ValidationError } from '../utils/errors';

// ============================================================
// v2-practical-ux: Unified "Play" search
// One endpoint returns lobbies + matches + open bookable slots
// ranked by relevance so the user sees every option in one list.
// ============================================================

export async function searchPlay(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;

    const from = req.query.from as string;
    const to = req.query.to as string;
    if (!from || !to) throw new ValidationError('`from` and `to` ISO timestamps required');

    const sportType = req.query.sportType ? String(req.query.sportType) : null;
    const lat = req.query.latitude ? Number(req.query.latitude) : null;
    const lng = req.query.longitude ? Number(req.query.longitude) : null;
    const radiusKm = req.query.radiusKm ? Number(req.query.radiusKm) : 25;
    const skillMin = req.query.skillLevelMin ? Number(req.query.skillLevelMin) : null;
    const skillMax = req.query.skillLevelMax ? Number(req.query.skillLevelMax) : null;
    const onlyEligible = req.query.onlyEligible === 'true';

    // If the caller didn't pick an explicit sport, fall back to their followed
    // sports (Settings → Sports I follow). When they have explicit filters or
    // no follow list, behavior is unchanged.
    let interestedSports: string[] = [];
    if (!sportType) {
      const interestedRes = await query(
        `SELECT sport_type FROM user_interested_sports WHERE user_id = $1`,
        [userId],
      );
      interestedSports = interestedRes.rows.map((r: any) => r.sport_type);
    }
    // Build parameterized sport filter instead of string interpolation
    const params: any[] = [userId];
    let latIdx = -1, lngIdx = -1;
    if (lat !== null && lng !== null) {
      params.push(lat, lng);
      latIdx = params.length - 1;
      lngIdx = params.length;
    }
    const distanceCol = (latIdx > 0)
      ? `(6371 * acos(cos(radians($${latIdx})) * cos(radians(v.latitude)) * cos(radians(v.longitude) - radians($${lngIdx})) + sin(radians($${latIdx})) * sin(radians(v.latitude))))`
      : `NULL::DOUBLE PRECISION`;

    params.push(from, to);
    const fromIdx = params.length - 1;
    const toIdx = params.length;

    let sportFilterSql: (col: string) => string;
    if (sportType) {
      params.push(sportType);
      const sportIdx = params.length;
      sportFilterSql = (col: string) => `AND ${col} = $${sportIdx}`;
    } else if (interestedSports.length > 0) {
      const sportIdxStart = params.length + 1;
      interestedSports.forEach(s => params.push(s));
      const sportIdxEnd = params.length;
      const placeholders = Array.from(
        { length: sportIdxEnd - sportIdxStart + 1 },
        (_, i) => `$${sportIdxStart + i}`
      ).join(',');
      sportFilterSql = (col: string) => `AND ${col} IN (${placeholders})`;
    } else {
      sportFilterSql = () => '';
    }

    // --- Lobbies (community_lobbies) ---
    // NOTE: community_lobbies stores scheduled_date (DATE) + scheduled_time (TIME)
    // as two columns — there is no scheduled_start column. We synthesize it.
    const lobbiesRes = await query(
      `SELECT
         cl.id,
         cl.title,
         cl.sport_type,
         (cl.scheduled_date::TIMESTAMP + cl.scheduled_time::TIME) AS start_at,
         v.name                 AS venue_name,
         ${distanceCol}         AS distance_km,
         cl.current_players,
         cl.max_players,
         cl.skill_level_min,
         cl.skill_level_max,
         NULL::NUMERIC          AS price
       FROM community_lobbies cl
       LEFT JOIN venues v ON v.id = cl.venue_id
       LEFT JOIN users me ON me.id = $1
       WHERE cl.status = 'open'
         AND (cl.scheduled_date::TIMESTAMP + cl.scheduled_time::TIME)
             BETWEEN $${fromIdx}::TIMESTAMP AND $${toIdx}::TIMESTAMP
         ${sportFilterSql('cl.sport_type')}
         ${skillMin !== null ? `AND (cl.skill_level_max IS NULL OR cl.skill_level_max >= ${skillMin})` : ''}
         ${skillMax !== null ? `AND (cl.skill_level_min IS NULL OR cl.skill_level_min <= ${skillMax})` : ''}
         ${onlyEligible ? `AND (me.skill_level IS NULL OR cl.skill_level_min IS NULL OR cl.skill_level_max IS NULL
                                OR me.skill_level BETWEEN cl.skill_level_min AND cl.skill_level_max)` : ''}
       ORDER BY start_at ASC
       LIMIT 50`,
      params,
    );

    // --- Open slots (time_slots not yet booked) ---
    const slotsRes = await query(
      `SELECT
         ts.id,
         CONCAT(v.name, ' · ', ts.start_time) AS title,
         v.sport_type,
         (ts.slot_date::TIMESTAMP + ts.start_time::TIME) AT TIME ZONE 'UTC' AS start_at,
         v.name                 AS venue_name,
         ts.venue_id,
         ${distanceCol}         AS distance_km,
         0                      AS current_players,
         1                      AS max_players,
         NULL::INTEGER          AS skill_level_min,
         NULL::INTEGER          AS skill_level_max,
         COALESCE(ts.price_override, v.price_per_hour) AS price
       FROM time_slots ts
       JOIN venues v ON v.id = ts.venue_id
       WHERE ts.is_available = true
         AND NOT EXISTS (
           SELECT 1 FROM bookings b
           WHERE b.time_slot_id = ts.id AND b.status IN ('pending','approved','confirmed','completed')
         )
         AND $1::BIGINT IS NOT NULL
         AND (ts.slot_date::TIMESTAMP + ts.start_time::TIME) BETWEEN $${fromIdx}::TIMESTAMP AND $${toIdx}::TIMESTAMP
         ${sportFilterSql('v.sport_type')}
       ORDER BY start_at ASC
       LIMIT 50`,
      params,
    );

    // --- Available players count (shown as a chip, not individual cards here) ---
    let availPlayersSql = `SELECT COUNT(*)::INT AS cnt FROM available_players ap
       WHERE (ap.available_until IS NULL OR ap.available_until > NOW())`;
    let availParams: any[] = [];
    if (sportType) {
      availParams.push(sportType);
      availPlayersSql += ` AND ap.sport_type = $1`;
    } else if (interestedSports.length > 0) {
      interestedSports.forEach((s, i) => availParams.push(s));
      const placeholders = interestedSports.map((_, i) => `$${i + 1}`).join(',');
      availPlayersSql += ` AND ap.sport_type IN (${placeholders})`;
    }
    const availPlayersRes = await query(availPlayersSql, availParams);

    const mapLobby = (r: any) => ({
      type: 'lobby' as const,
      id: Number(r.id),
      title: r.title,
      sportType: r.sport_type,
      startAt: r.start_at?.toISOString?.() ?? r.start_at,
      venueName: r.venue_name ?? null,
      distanceKm: r.distance_km != null ? Number(r.distance_km) : null,
      currentPlayers: Number(r.current_players ?? 0),
      maxPlayers: Number(r.max_players ?? 0),
      skillLevelMin: r.skill_level_min ? Number(r.skill_level_min) : null,
      skillLevelMax: r.skill_level_max ? Number(r.skill_level_max) : null,
      price: r.price != null ? Number(r.price) : null,
    });

    const mapSlot = (r: any) => ({
      type: 'open_slot' as const,
      id: Number(r.id),
      title: r.title,
      sportType: r.sport_type,
      startAt: r.start_at?.toISOString?.() ?? r.start_at,
      venueName: r.venue_name ?? null,
      venueId: r.venue_id ? Number(r.venue_id) : null,
      distanceKm: r.distance_km != null ? Number(r.distance_km) : null,
      currentPlayers: Number(r.current_players ?? 0),
      maxPlayers: Number(r.max_players ?? 0),
      skillLevelMin: null,
      skillLevelMax: null,
      price: r.price != null ? Number(r.price) : null,
    });

    const results = [
      ...lobbiesRes.rows.map(mapLobby),
      ...slotsRes.rows.map(mapSlot),
    ].sort((a, b) => {
      // Lobbies that still have open seats rank above open slots, then by time
      const aPriority = a.type === 'lobby' ? 0 : 1;
      const bPriority = b.type === 'lobby' ? 0 : 1;
      if (aPriority !== bPriority) return aPriority - bPriority;
      return a.startAt.localeCompare(b.startAt);
    });

    // Optional radius filter applied post-query so SQL stays simple
    const filtered = (lat !== null && lng !== null)
      ? results.filter(r => r.distanceKm === null || r.distanceKm <= radiusKm)
      : results;

    success(res, {
      results: filtered.slice(0, 60),
      counts: {
        lobbies: lobbiesRes.rows.length,
        openSlots: slotsRes.rows.length,
        availablePlayers: Number(availPlayersRes.rows[0]?.cnt ?? 0),
      },
    });
  } catch (e) {
    next(e);
  }
}

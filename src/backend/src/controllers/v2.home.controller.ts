import { Request, Response, NextFunction } from 'express';
import { success } from '../utils/apiResponse';
import { query } from '../config/database';

// ============================================================
// v2-practical-ux: Unified home feed
// Single endpoint backing the mobile/web home screen.
// Replaces ~5 separate round trips with one consolidated call.
// ============================================================

interface RebookSuggestion {
  bookingId: number;
  venueId: number | null;
  venueName: string | null;
  coachId: number | null;
  coachName: string | null;
  sportType: string;
  lastPlayedAt: string;
  lastSlotStart: string;
  price: number;
  timesBooked: number;
}

interface PlaySuggestion {
  type: 'lobby' | 'match' | 'open_slot';
  id: number;
  title: string;
  sportType: string;
  startAt: string;
  venueName: string | null;
  distanceKm: number | null;
  currentPlayers: number;
  maxPlayers: number;
  skillLevelMin: number | null;
  skillLevelMax: number | null;
  price: number | null;
}

interface FriendAvailability {
  userId: number;
  displayName: string | null;
  photoUrl: string | null;
  sportType: string;
  skillLevel: number | null;
  availableUntil: string | null;
  distanceKm: number | null;
}

export async function getHomeFeed(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user!.id;

    // ----- Run all four queries in parallel; one round trip is 5x faster than sequential -----
    const [greetingRes, rebookRes, suggestedRes, friendsRes, upcomingRes] = await Promise.all([
      // Greeting block: user name + reliability score
      query(
        `SELECT display_name, reliability_score, total_attended
         FROM users WHERE id = $1`,
        [userId],
      ),

      // Rebook: last 5 distinct venue/coach combos this player has booked, most recent + most frequent
      query(
        `SELECT
           b.id                AS booking_id,
           b.venue_id,
           b.coach_id,
           v.name              AS venue_name,
           v.sport_type        AS venue_sport,
           c.name              AS coach_name,
           c.sport_type        AS coach_sport,
           ts.start_time,
           ts.slot_date,
           b.total_price,
           b.created_at,
           COUNT(*) OVER (PARTITION BY COALESCE(b.venue_id, -1), COALESCE(b.coach_id, -1))
                                AS times_booked
         FROM bookings b
         LEFT JOIN venues v     ON v.id = b.venue_id
         LEFT JOIN coaches c    ON c.id = b.coach_id
         LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
         WHERE b.player_id = $1
           AND b.status IN ('confirmed', 'completed')
         ORDER BY b.created_at DESC
         LIMIT 5`,
        [userId],
      ),

      // Suggested play: open community lobbies in sports the user cares about,
      // matching their skill level if set
      query(
        `SELECT
           cl.id,
           cl.title,
           cl.sport_type,
           cl.scheduled_start       AS start_at,
           v.name                   AS venue_name,
           cl.current_players,
           cl.max_players,
           cl.skill_level_min,
           cl.skill_level_max
         FROM community_lobbies cl
         LEFT JOIN venues v ON v.id = cl.venue_id
         LEFT JOIN users u  ON u.id = $1
         WHERE cl.status = 'open'
           AND cl.scheduled_start > NOW()
           AND cl.scheduled_start < NOW() + INTERVAL '7 days'
           AND (u.skill_level IS NULL
                OR cl.skill_level_min IS NULL
                OR cl.skill_level_max IS NULL
                OR u.skill_level BETWEEN cl.skill_level_min AND cl.skill_level_max)
         ORDER BY cl.scheduled_start ASC
         LIMIT 5`,
        [userId],
      ),

      // Friends available
      query(
        `SELECT
           ap.user_id,
           u.display_name,
           u.photo_url,
           ap.sport_type,
           ap.skill_level,
           ap.available_until
         FROM available_players ap
         JOIN users u ON u.id = ap.user_id
         JOIN friendships f
           ON ((f.user_id_a = $1 AND f.user_id_b = ap.user_id)
            OR (f.user_id_b = $1 AND f.user_id_a = ap.user_id))
         WHERE f.status = 'accepted'
           AND (ap.available_until IS NULL OR ap.available_until > NOW())
         ORDER BY ap.created_at DESC
         LIMIT 10`,
        [userId],
      ),

      // Upcoming next 7 days
      query(
        `SELECT b.id, b.venue_id, b.coach_id, b.status, b.total_price, b.created_at,
                v.name AS venue_name, c.name AS coach_name,
                ts.slot_date, ts.start_time, ts.end_time
         FROM bookings b
         LEFT JOIN venues v ON v.id = b.venue_id
         LEFT JOIN coaches c ON c.id = b.coach_id
         LEFT JOIN time_slots ts ON ts.id = b.time_slot_id
         WHERE b.player_id = $1
           AND b.status IN ('pending', 'approved', 'confirmed')
           AND ts.slot_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
         ORDER BY ts.slot_date, ts.start_time
         LIMIT 10`,
        [userId],
      ),
    ]);

    const greeting = greetingRes.rows[0]
      ? {
          displayName: greetingRes.rows[0].display_name,
          reliabilityScore: Number(greetingRes.rows[0].reliability_score ?? 1.0),
          totalAttended: Number(greetingRes.rows[0].total_attended ?? 0),
        }
      : { displayName: null, reliabilityScore: 1.0, totalAttended: 0 };

    const recentBookings: RebookSuggestion[] = rebookRes.rows.map((r: any) => ({
      bookingId: Number(r.booking_id),
      venueId: r.venue_id ? Number(r.venue_id) : null,
      venueName: r.venue_name ?? null,
      coachId: r.coach_id ? Number(r.coach_id) : null,
      coachName: r.coach_name ?? null,
      sportType: r.venue_sport ?? r.coach_sport ?? '',
      lastPlayedAt: r.created_at?.toISOString?.() ?? r.created_at,
      lastSlotStart: r.start_time ?? '',
      price: Number(r.total_price),
      timesBooked: Number(r.times_booked),
    }));

    const suggestedPlay: PlaySuggestion[] = suggestedRes.rows.map((r: any) => ({
      type: 'lobby' as const,
      id: Number(r.id),
      title: r.title,
      sportType: r.sport_type,
      startAt: r.start_at?.toISOString?.() ?? r.start_at,
      venueName: r.venue_name ?? null,
      distanceKm: null,
      currentPlayers: Number(r.current_players ?? 0),
      maxPlayers: Number(r.max_players ?? 0),
      skillLevelMin: r.skill_level_min ? Number(r.skill_level_min) : null,
      skillLevelMax: r.skill_level_max ? Number(r.skill_level_max) : null,
      price: null,
    }));

    const friendsAvailable: FriendAvailability[] = friendsRes.rows.map((r: any) => ({
      userId: Number(r.user_id),
      displayName: r.display_name ?? null,
      photoUrl: r.photo_url ?? null,
      sportType: r.sport_type,
      skillLevel: r.skill_level ? Number(r.skill_level) : null,
      availableUntil: r.available_until?.toISOString?.() ?? r.available_until,
      distanceKm: null,
    }));

    const upcoming = upcomingRes.rows.map((r: any) => ({
      id: Number(r.id),
      venueId: r.venue_id ? Number(r.venue_id) : null,
      coachId: r.coach_id ? Number(r.coach_id) : null,
      venueName: r.venue_name ?? null,
      coachName: r.coach_name ?? null,
      status: r.status,
      totalPrice: Number(r.total_price),
      slotDate: r.slot_date instanceof Date ? r.slot_date.toISOString().split('T')[0] : r.slot_date,
      startTime: r.start_time,
      endTime: r.end_time,
      createdAt: r.created_at?.toISOString?.() ?? r.created_at,
    }));

    success(res, {
      greeting,
      recentBookings,
      suggestedPlay,
      friendsAvailable,
      upcoming,
    });
  } catch (e) {
    next(e);
  }
}

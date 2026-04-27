// Admin queries: partner approval queue + platform overview metrics.
// Read by the admin dashboard at /admin/* (frontend) and /api/admin/* (backend).
import { query } from '../config/database';

export interface PartnerSummary {
  id: number;
  email: string;
  displayName: string | null;
  photoUrl: string | null;
  partnerType: string | null;
  bio: string | null;
  phoneNumber: string | null;
  approvedAt: string | null;
  approvedByUserId: number | null;
  rejectionReason: string | null;
  createdAt: string;
  // Quick context for the reviewer:
  venueCount: number;
  coachCount: number;
}

export interface PlatformOverview {
  totalUsers: number;
  totalPlayers: number;
  totalPartners: number;
  pendingPartners: number;
  totalVenues: number;
  totalCoaches: number;
  totalBookings: number;
  bookingsLast30d: number;
  pendingBookings: number;
}

const PARTNER_BASE_QUERY = `
  SELECT
    u.id,
    u.email,
    u.display_name,
    u.photo_url,
    u.partner_type,
    u.bio,
    u.phone_number,
    u.partner_approved_at,
    u.partner_approved_by_user_id,
    u.partner_rejection_reason,
    u.created_at,
    COALESCE(vc.cnt, 0)::int AS venue_count,
    COALESCE(cc.cnt, 0)::int AS coach_count
  FROM users u
  LEFT JOIN (SELECT owner_id, COUNT(*) AS cnt FROM venues GROUP BY owner_id) vc
    ON vc.owner_id = u.id
  LEFT JOIN (SELECT user_id, COUNT(*) AS cnt FROM coaches GROUP BY user_id) cc
    ON cc.user_id = u.id
`;

function mapPartner(row: any): PartnerSummary {
  return {
    id: Number(row.id),
    email: row.email,
    displayName: row.display_name,
    photoUrl: row.photo_url,
    partnerType: row.partner_type,
    bio: row.bio,
    phoneNumber: row.phone_number,
    approvedAt: row.partner_approved_at?.toISOString?.() ?? row.partner_approved_at ?? null,
    approvedByUserId: row.partner_approved_by_user_id != null ? Number(row.partner_approved_by_user_id) : null,
    rejectionReason: row.partner_rejection_reason,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    venueCount: row.venue_count ?? 0,
    coachCount: row.coach_count ?? 0,
  };
}

/** Pending = role partner AND no approval timestamp yet. Oldest first (FIFO queue). */
export async function findPendingPartners(): Promise<PartnerSummary[]> {
  const r = await query(
    `${PARTNER_BASE_QUERY}
     WHERE u.role = 'partner' AND u.partner_approved_at IS NULL
     ORDER BY u.created_at ASC`
  );
  return r.rows.map(mapPartner);
}

/** All partners (approved + pending) — newest first. */
export async function findAllPartners(): Promise<PartnerSummary[]> {
  const r = await query(
    `${PARTNER_BASE_QUERY}
     WHERE u.role = 'partner'
     ORDER BY (u.partner_approved_at IS NULL) DESC, u.created_at DESC`
  );
  return r.rows.map(mapPartner);
}

export async function findPartnerById(id: number): Promise<PartnerSummary | null> {
  const r = await query(
    `${PARTNER_BASE_QUERY} WHERE u.id = $1 AND u.role = 'partner'`,
    [id]
  );
  return r.rows[0] ? mapPartner(r.rows[0]) : null;
}

export async function approvePartner(id: number, adminUserId: number): Promise<PartnerSummary | null> {
  const r = await query(
    `UPDATE users
     SET partner_approved_at = NOW(),
         partner_approved_by_user_id = $2,
         partner_rejection_reason = NULL,
         updated_at = NOW()
     WHERE id = $1 AND role = 'partner'
     RETURNING id`,
    [id, adminUserId]
  );
  if (!r.rows[0]) return null;
  return findPartnerById(id);
}

export async function rejectPartner(
  id: number,
  adminUserId: number,
  reason: string
): Promise<PartnerSummary | null> {
  // "Reject" = clear the approval timestamp + record reason. Partner stays in
  // queue with the rejection note visible to the next reviewer.
  const r = await query(
    `UPDATE users
     SET partner_approved_at = NULL,
         partner_approved_by_user_id = $2,
         partner_rejection_reason = $3,
         updated_at = NOW()
     WHERE id = $1 AND role = 'partner'
     RETURNING id`,
    [id, adminUserId, reason]
  );
  if (!r.rows[0]) return null;
  return findPartnerById(id);
}

export async function getPlatformOverview(): Promise<PlatformOverview> {
  // One round-trip aggregate — admin dashboard hits this on every refresh.
  const r = await query(
    `SELECT
       (SELECT COUNT(*)::int FROM users)                                          AS total_users,
       (SELECT COUNT(*)::int FROM users WHERE role = 'player')                    AS total_players,
       (SELECT COUNT(*)::int FROM users WHERE role = 'partner')                   AS total_partners,
       (SELECT COUNT(*)::int FROM users
          WHERE role = 'partner' AND partner_approved_at IS NULL)                 AS pending_partners,
       (SELECT COUNT(*)::int FROM venues)                                         AS total_venues,
       (SELECT COUNT(*)::int FROM coaches)                                        AS total_coaches,
       (SELECT COUNT(*)::int FROM bookings)                                       AS total_bookings,
       (SELECT COUNT(*)::int FROM bookings
          WHERE created_at >= NOW() - INTERVAL '30 days')                         AS bookings_last_30d,
       (SELECT COUNT(*)::int FROM bookings WHERE status = 'pending')              AS pending_bookings`
  );
  const row = r.rows[0];
  return {
    totalUsers: row.total_users ?? 0,
    totalPlayers: row.total_players ?? 0,
    totalPartners: row.total_partners ?? 0,
    pendingPartners: row.pending_partners ?? 0,
    totalVenues: row.total_venues ?? 0,
    totalCoaches: row.total_coaches ?? 0,
    totalBookings: row.total_bookings ?? 0,
    bookingsLast30d: row.bookings_last_30d ?? 0,
    pendingBookings: row.pending_bookings ?? 0,
  };
}

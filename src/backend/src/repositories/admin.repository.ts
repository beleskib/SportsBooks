// Admin queries: pending-listing review queue + platform overview metrics.
// Read by the admin dashboard at /admin/* (frontend) and /api/admin/* (backend).
//
// Listings = venues + coaches. The approval gate lives on the listing itself
// (see migration 0054), not on the partner account: the admin verifies that
// a venue/coach is real before it goes live, but partner accounts do not need
// pre-approval.
import { query } from '../config/database';

export type ListingType = 'venue' | 'coach';

/** Compact entry shown in the admin queue (one row per pending listing). */
export interface PendingListingRow {
  type: ListingType;
  id: number;
  name: string;
  sportType: string;
  pricePerHour: number;
  city: string | null;
  address: string | null;
  primaryImageUrl: string | null;
  ownerId: number;
  ownerEmail: string;
  ownerDisplayName: string | null;
  createdAt: string;
}

/** Full detail used by the admin review page — admin needs enough info to
 *  decide whether the listing is real (address, photos, equipment, contact). */
export interface ListingDetail {
  type: ListingType;
  id: number;
  name: string;
  description: string | null;
  sportType: string;
  pricePerHour: number;
  address: string | null;
  city: string | null;
  country: string | null;
  latitude: number | null;
  longitude: number | null;
  phoneNumber: string | null;
  email: string | null;
  isActive: boolean;
  approvalStatus: 'pending' | 'approved' | 'rejected';
  approvalDecidedAt: string | null;
  approvalRejectionReason: string | null;
  imageUrls: string[];
  // Venue-only:
  equipment?: Array<{ name: string; description: string | null; isIncluded: boolean }>;
  // Coach-only:
  specialization?: string | null;
  experienceYears?: number | null;
  certifications?: Array<{ name: string; issuingBody: string | null; yearObtained: number | null }>;
  owner: {
    id: number;
    email: string;
    displayName: string | null;
    photoUrl: string | null;
    phoneNumber: string | null;
    bio: string | null;
    createdAt: string;
  };
  createdAt: string;
}

export interface PlatformOverview {
  totalUsers: number;
  totalPlayers: number;
  totalPartners: number;
  totalVenues: number;
  totalCoaches: number;
  pendingListings: number;
  pendingVenues: number;
  pendingCoaches: number;
  totalBookings: number;
  bookingsLast30d: number;
  pendingBookings: number;
}

export async function findPendingListings(): Promise<PendingListingRow[]> {
  // Union venue + coach pending queues, oldest first (FIFO).
  const r = await query(
    `WITH venue_q AS (
       SELECT
         'venue'::text AS type,
         v.id, v.name, v.sport_type, v.price_per_hour, v.city, v.address,
         v.owner_id, v.created_at,
         (SELECT image_url FROM venue_images
            WHERE venue_id = v.id
            ORDER BY is_primary DESC, display_order ASC LIMIT 1) AS primary_image_url
       FROM venues v
       WHERE v.approval_status = 'pending'
     ),
     coach_q AS (
       SELECT
         'coach'::text AS type,
         c.id, c.name, c.sport_type, c.price_per_hour, c.city, c.address,
         c.user_id AS owner_id, c.created_at,
         (SELECT image_url FROM coach_images
            WHERE coach_id = c.id
            ORDER BY is_primary DESC, display_order ASC LIMIT 1) AS primary_image_url
       FROM coaches c
       WHERE c.approval_status = 'pending'
     ),
     all_q AS (SELECT * FROM venue_q UNION ALL SELECT * FROM coach_q)
     SELECT a.type, a.id, a.name, a.sport_type, a.price_per_hour, a.city, a.address,
            a.primary_image_url, a.owner_id, a.created_at,
            u.email AS owner_email, u.display_name AS owner_display_name
     FROM all_q a
     INNER JOIN users u ON u.id = a.owner_id
     ORDER BY a.created_at ASC`
  );

  return r.rows.map((row: any) => ({
    type: row.type as ListingType,
    id: Number(row.id),
    name: row.name,
    sportType: row.sport_type,
    pricePerHour: Number(row.price_per_hour),
    city: row.city,
    address: row.address,
    primaryImageUrl: row.primary_image_url,
    ownerId: Number(row.owner_id),
    ownerEmail: row.owner_email,
    ownerDisplayName: row.owner_display_name,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  }));
}

export async function findVenueDetail(id: number): Promise<ListingDetail | null> {
  const r = await query(
    `SELECT v.id, v.name, v.description, v.sport_type, v.price_per_hour,
            v.address, v.city, v.country, v.latitude, v.longitude,
            v.phone_number, v.email, v.is_active,
            v.approval_status, v.approval_decided_at, v.approval_rejection_reason,
            v.created_at,
            u.id AS owner_id_, u.email AS owner_email,
            u.display_name AS owner_display_name, u.photo_url AS owner_photo_url,
            u.phone_number AS owner_phone, u.bio AS owner_bio,
            u.created_at AS owner_created_at
     FROM venues v
     INNER JOIN users u ON u.id = v.owner_id
     WHERE v.id = $1`,
    [id]
  );
  if (r.rows.length === 0) return null;
  const row = r.rows[0];

  const [imgsR, eqR] = await Promise.all([
    query(
      `SELECT image_url FROM venue_images WHERE venue_id = $1
       ORDER BY is_primary DESC, display_order ASC`,
      [id]
    ),
    query(
      `SELECT name, description, is_included FROM venue_equipment WHERE venue_id = $1`,
      [id]
    ),
  ]);

  return {
    type: 'venue',
    id: Number(row.id),
    name: row.name,
    description: row.description,
    sportType: row.sport_type,
    pricePerHour: Number(row.price_per_hour),
    address: row.address,
    city: row.city,
    country: row.country,
    latitude: row.latitude ? Number(row.latitude) : null,
    longitude: row.longitude ? Number(row.longitude) : null,
    phoneNumber: row.phone_number,
    email: row.email,
    isActive: row.is_active,
    approvalStatus: row.approval_status,
    approvalDecidedAt: row.approval_decided_at?.toISOString?.() ?? row.approval_decided_at ?? null,
    approvalRejectionReason: row.approval_rejection_reason ?? null,
    imageUrls: imgsR.rows.map((x: any) => x.image_url),
    equipment: eqR.rows.map((x: any) => ({
      name: x.name, description: x.description, isIncluded: x.is_included,
    })),
    owner: {
      id: Number(row.owner_id_),
      email: row.owner_email,
      displayName: row.owner_display_name,
      photoUrl: row.owner_photo_url,
      phoneNumber: row.owner_phone,
      bio: row.owner_bio,
      createdAt: row.owner_created_at?.toISOString?.() ?? row.owner_created_at,
    },
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

export async function findCoachDetail(id: number): Promise<ListingDetail | null> {
  const r = await query(
    `SELECT c.id, c.name, c.bio AS description, c.sport_type, c.price_per_hour,
            c.address, c.city, c.country, c.latitude, c.longitude,
            c.phone_number, c.email, c.is_active,
            c.approval_status, c.approval_decided_at, c.approval_rejection_reason,
            c.specialization, c.experience_years, c.created_at,
            u.id AS owner_id_, u.email AS owner_email,
            u.display_name AS owner_display_name, u.photo_url AS owner_photo_url,
            u.phone_number AS owner_phone, u.bio AS owner_bio,
            u.created_at AS owner_created_at
     FROM coaches c
     INNER JOIN users u ON u.id = c.user_id
     WHERE c.id = $1`,
    [id]
  );
  if (r.rows.length === 0) return null;
  const row = r.rows[0];

  const [imgsR, certR] = await Promise.all([
    query(
      `SELECT image_url FROM coach_images WHERE coach_id = $1
       ORDER BY is_primary DESC, display_order ASC`,
      [id]
    ),
    query(
      `SELECT name, issuing_body, year_obtained FROM coach_certifications WHERE coach_id = $1`,
      [id]
    ),
  ]);

  return {
    type: 'coach',
    id: Number(row.id),
    name: row.name,
    description: row.description,
    sportType: row.sport_type,
    pricePerHour: Number(row.price_per_hour),
    address: row.address,
    city: row.city,
    country: row.country,
    latitude: row.latitude ? Number(row.latitude) : null,
    longitude: row.longitude ? Number(row.longitude) : null,
    phoneNumber: row.phone_number,
    email: row.email,
    isActive: row.is_active,
    approvalStatus: row.approval_status,
    approvalDecidedAt: row.approval_decided_at?.toISOString?.() ?? row.approval_decided_at ?? null,
    approvalRejectionReason: row.approval_rejection_reason ?? null,
    imageUrls: imgsR.rows.map((x: any) => x.image_url),
    specialization: row.specialization,
    experienceYears: row.experience_years != null ? Number(row.experience_years) : null,
    certifications: certR.rows.map((x: any) => ({
      name: x.name, issuingBody: x.issuing_body, yearObtained: x.year_obtained,
    })),
    owner: {
      id: Number(row.owner_id_),
      email: row.owner_email,
      displayName: row.owner_display_name,
      photoUrl: row.owner_photo_url,
      phoneNumber: row.owner_phone,
      bio: row.owner_bio,
      createdAt: row.owner_created_at?.toISOString?.() ?? row.owner_created_at,
    },
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

export async function getPlatformOverview(): Promise<PlatformOverview> {
  // One round-trip aggregate — admin dashboard hits this on every refresh.
  const r = await query(
    `SELECT
       (SELECT COUNT(*)::int FROM users)                                     AS total_users,
       (SELECT COUNT(*)::int FROM users WHERE role = 'player')               AS total_players,
       (SELECT COUNT(*)::int FROM users WHERE role = 'partner')              AS total_partners,
       (SELECT COUNT(*)::int FROM venues)                                    AS total_venues,
       (SELECT COUNT(*)::int FROM coaches)                                   AS total_coaches,
       (SELECT COUNT(*)::int FROM venues  WHERE approval_status = 'pending') AS pending_venues,
       (SELECT COUNT(*)::int FROM coaches WHERE approval_status = 'pending') AS pending_coaches,
       (SELECT COUNT(*)::int FROM bookings)                                  AS total_bookings,
       (SELECT COUNT(*)::int FROM bookings
          WHERE created_at >= NOW() - INTERVAL '30 days')                    AS bookings_last_30d,
       (SELECT COUNT(*)::int FROM bookings WHERE status = 'pending')         AS pending_bookings`
  );
  const row = r.rows[0];
  const pendingVenues = row.pending_venues ?? 0;
  const pendingCoaches = row.pending_coaches ?? 0;
  return {
    totalUsers: row.total_users ?? 0,
    totalPlayers: row.total_players ?? 0,
    totalPartners: row.total_partners ?? 0,
    totalVenues: row.total_venues ?? 0,
    totalCoaches: row.total_coaches ?? 0,
    pendingVenues,
    pendingCoaches,
    pendingListings: pendingVenues + pendingCoaches,
    totalBookings: row.total_bookings ?? 0,
    bookingsLast30d: row.bookings_last_30d ?? 0,
    pendingBookings: row.pending_bookings ?? 0,
  };
}

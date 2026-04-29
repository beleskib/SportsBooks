import { query } from '../config/database';
import { DiscountRow, ListingApprovalStatus } from './venue.repository';

export interface CoachRow {
  id: number;
  userId: number;
  name: string;
  bio: string | null;
  sportType: string;
  specialization: string | null;
  experienceYears: number;
  pricePerHour: number;
  address: string | null;
  city: string | null;
  country: string | null;
  latitude: number | null;
  longitude: number | null;
  phoneNumber: string | null;
  email: string | null;
  avgRating: number;
  totalReviews: number;
  isActive: boolean;
  // Listing-level approval gate (see migration 0054). New rows default to
  // 'pending'; only 'approved' rows are visible on public list/search endpoints.
  approvalStatus: ListingApprovalStatus;
  approvalDecidedAt: string | null;
  approvalDecidedByUserId: number | null;
  approvalRejectionReason: string | null;
  images: CoachImageRow[];
  certifications: CoachCertificationRow[];
  activeDiscount: DiscountRow | null;
  createdAt: string;
  updatedAt: string;
}

export interface CoachImageRow {
  id: number;
  coachId: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface CoachCertificationRow {
  id: number;
  coachId: number;
  name: string;
  issuingBody: string | null;
  yearObtained: number | null;
  certificateUrl: string | null;
}

const COACH_COLS = `id, user_id, name, bio, sport_type, specialization, experience_years,
  price_per_hour, address, city, country, latitude, longitude,
  phone_number, email, avg_rating, total_reviews, is_active,
  approval_status, approval_decided_at, approval_decided_by_user_id,
  approval_rejection_reason,
  created_at, updated_at`;

function mapCoachRow(row: any): Omit<CoachRow, 'images' | 'certifications' | 'activeDiscount'> {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    name: row.name,
    bio: row.bio,
    sportType: row.sport_type,
    specialization: row.specialization,
    experienceYears: row.experience_years,
    pricePerHour: Number(row.price_per_hour),
    address: row.address,
    city: row.city,
    country: row.country,
    latitude: row.latitude ? Number(row.latitude) : null,
    longitude: row.longitude ? Number(row.longitude) : null,
    phoneNumber: row.phone_number,
    email: row.email,
    avgRating: Number(row.avg_rating),
    totalReviews: row.total_reviews,
    isActive: row.is_active,
    approvalStatus: (row.approval_status ?? 'pending') as ListingApprovalStatus,
    approvalDecidedAt: row.approval_decided_at?.toISOString?.() ?? row.approval_decided_at ?? null,
    approvalDecidedByUserId: row.approval_decided_by_user_id != null ? Number(row.approval_decided_by_user_id) : null,
    approvalRejectionReason: row.approval_rejection_reason ?? null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

async function loadCoachRelations(coachIds: number[]) {
  const images = new Map<number, CoachImageRow[]>();
  const certifications = new Map<number, CoachCertificationRow[]>();
  const discounts = new Map<number, DiscountRow>();

  if (coachIds.length === 0) return { images, certifications, discounts };

  const [imgResult, certResult, discResult] = await Promise.all([
    query(
      `SELECT id, coach_id, image_url, is_primary, display_order
       FROM coach_images WHERE coach_id = ANY($1) ORDER BY display_order`,
      [coachIds]
    ),
    query(
      `SELECT id, coach_id, name, issuing_body, year_obtained, certificate_url
       FROM coach_certifications WHERE coach_id = ANY($1)`,
      [coachIds]
    ),
    query(
      `SELECT id, venue_id, coach_id, title, description, discount_percent,
              discount_amount, valid_from, valid_until, is_active, created_at, updated_at
       FROM discounts
       WHERE coach_id = ANY($1) AND is_active = true
         AND valid_from <= NOW() AND valid_until >= NOW()`,
      [coachIds]
    ),
  ]);

  for (const row of imgResult.rows) {
    const list = images.get(row.coach_id) || [];
    list.push({
      id: Number(row.id), coachId: Number(row.coach_id), imageUrl: row.image_url,
      isPrimary: row.is_primary, displayOrder: row.display_order,
    });
    images.set(Number(row.coach_id), list);
  }

  for (const row of certResult.rows) {
    const list = certifications.get(Number(row.coach_id)) || [];
    list.push({
      id: Number(row.id), coachId: Number(row.coach_id), name: row.name,
      issuingBody: row.issuing_body, yearObtained: row.year_obtained,
      certificateUrl: row.certificate_url,
    });
    certifications.set(Number(row.coach_id), list);
  }

  for (const row of discResult.rows) {
    if (!discounts.has(Number(row.coach_id))) {
      discounts.set(Number(row.coach_id), {
        id: Number(row.id), venueId: row.venue_id ? Number(row.venue_id) : null, coachId: Number(row.coach_id),
        title: row.title, description: row.description,
        discountPercent: row.discount_percent != null ? Number(row.discount_percent) : null,
        discountAmount: row.discount_amount != null ? Number(row.discount_amount) : null,
        validFrom: row.valid_from?.toISOString?.() ?? null,
        validUntil: row.valid_until?.toISOString?.() ?? null,
        isActive: row.is_active, createdAt: '', updatedAt: '',
      });
    }
  }

  return { images, certifications, discounts };
}

function assembleCoaches(rows: any[], relations: Awaited<ReturnType<typeof loadCoachRelations>>): CoachRow[] {
  return rows.map((row) => ({
    ...mapCoachRow(row),
    images: relations.images.get(row.id) || [],
    certifications: relations.certifications.get(row.id) || [],
    activeDiscount: relations.discounts.get(row.id) || null,
  }));
}

// ============================================================
// Public reads — filter to approval_status = 'approved'.
// ============================================================

export async function findAll(): Promise<CoachRow[]> {
  const result = await query(
    `SELECT ${COACH_COLS}
     FROM coaches
     WHERE approval_status = 'approved'
     ORDER BY created_at DESC`
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

export async function findBySport(sportType: string): Promise<CoachRow[]> {
  const result = await query(
    `SELECT ${COACH_COLS}
     FROM coaches
     WHERE sport_type = $1 AND is_active = true AND approval_status = 'approved'
     ORDER BY avg_rating DESC`,
    [sportType]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

export async function findById(id: number): Promise<CoachRow | null> {
  // Returns the row regardless of approval status. The caller (controller)
  // is responsible for hiding non-approved coaches from non-owners.
  const result = await query(
    `SELECT ${COACH_COLS} FROM coaches WHERE id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadCoachRelations([id]);
  return assembleCoaches(result.rows, relations)[0];
}

export async function findTopDeals(): Promise<CoachRow[]> {
  const result = await query(
    `SELECT ${COACH_COLS.split(',').map((c) => `c.${c.trim()}`).join(', ')}
     FROM coaches c
     INNER JOIN discounts d ON d.coach_id = c.id
       AND d.is_active = true AND d.valid_from <= NOW() AND d.valid_until >= NOW()
     WHERE c.is_active = true AND c.approval_status = 'approved'
     ORDER BY d.discount_percent DESC NULLS LAST
     LIMIT 20`
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

export async function search(q: string): Promise<CoachRow[]> {
  const result = await query(
    `SELECT ${COACH_COLS}
     FROM coaches
     WHERE is_active = true AND approval_status = 'approved'
       AND (name ILIKE $1 OR specialization ILIKE $1 OR city ILIKE $1)
     ORDER BY avg_rating DESC LIMIT 50`,
    [`%${q}%`]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

// ============================================================
// Owner reads — return all approval states.
// ============================================================

export async function findByUserId(userId: number): Promise<CoachRow | null> {
  const result = await query(
    `SELECT ${COACH_COLS} FROM coaches WHERE user_id = $1`,
    [userId]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadCoachRelations([result.rows[0].id]);
  return assembleCoaches(result.rows, relations)[0];
}

// ============================================================
// Mutations
// ============================================================

export async function update(
  id: number,
  data: {
    name?: string;
    bio?: string;
    specialization?: string;
    experienceYears?: number;
    pricePerHour?: number;
    address?: string;
    city?: string;
    country?: string;
    latitude?: number;
    longitude?: number;
    phoneNumber?: string;
    email?: string;
    isActive?: boolean;
  }
): Promise<CoachRow | null> {
  const result = await query(
    `UPDATE coaches SET
       name = COALESCE($2, name),
       bio = COALESCE($3, bio),
       specialization = COALESCE($4, specialization),
       experience_years = COALESCE($5, experience_years),
       price_per_hour = COALESCE($6, price_per_hour),
       address = COALESCE($7, address),
       city = COALESCE($8, city),
       country = COALESCE($9, country),
       latitude = COALESCE($10, latitude),
       longitude = COALESCE($11, longitude),
       phone_number = COALESCE($12, phone_number),
       email = COALESCE($13, email),
       is_active = COALESCE($14, is_active),
       updated_at = NOW()
     WHERE id = $1
     RETURNING ${COACH_COLS}`,
    [
      id, data.name, data.bio, data.specialization, data.experienceYears,
      data.pricePerHour, data.address, data.city, data.country,
      data.latitude, data.longitude, data.phoneNumber, data.email, data.isActive,
    ]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadCoachRelations([id]);
  return assembleCoaches(result.rows, relations)[0];
}

export async function softDelete(id: number): Promise<void> {
  await query(
    `UPDATE coaches SET is_active = false, updated_at = NOW() WHERE id = $1`,
    [id]
  );
}

export async function create(data: {
  userId: number;
  name: string;
  bio?: string;
  sportType: string;
  specialization?: string;
  experienceYears?: number;
  pricePerHour: number;
  address?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
  email?: string;
}): Promise<CoachRow> {
  // approval_status defaults to 'pending' (see migration 0054).
  const result = await query(
    `INSERT INTO coaches (user_id, name, bio, sport_type, specialization, experience_years,
       price_per_hour, address, city, country, phone_number, email)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
     RETURNING ${COACH_COLS}`,
    [
      data.userId, data.name, data.bio || null, data.sportType,
      data.specialization || null, data.experienceYears || 0, data.pricePerHour,
      data.address || null, data.city || null, data.country || null,
      data.phoneNumber || null, data.email || null,
    ]
  );
  return { ...mapCoachRow(result.rows[0]), images: [], certifications: [], activeDiscount: null };
}

// ============================================================
// Approval mutations — called by the admin controller only.
// ============================================================

export async function approve(id: number, adminUserId: number): Promise<CoachRow | null> {
  const r = await query(
    `UPDATE coaches SET
       approval_status = 'approved',
       approval_decided_at = NOW(),
       approval_decided_by_user_id = $2,
       approval_rejection_reason = NULL,
       updated_at = NOW()
     WHERE id = $1
     RETURNING ${COACH_COLS}`,
    [id, adminUserId]
  );
  if (r.rows.length === 0) return null;
  const relations = await loadCoachRelations([id]);
  return assembleCoaches(r.rows, relations)[0];
}

export async function reject(
  id: number,
  adminUserId: number,
  reason: string
): Promise<CoachRow | null> {
  const r = await query(
    `UPDATE coaches SET
       approval_status = 'rejected',
       approval_decided_at = NOW(),
       approval_decided_by_user_id = $2,
       approval_rejection_reason = $3,
       updated_at = NOW()
     WHERE id = $1
     RETURNING ${COACH_COLS}`,
    [id, adminUserId, reason]
  );
  if (r.rows.length === 0) return null;
  const relations = await loadCoachRelations([id]);
  return assembleCoaches(r.rows, relations)[0];
}

// ---- Coach Image Management ----

export async function createCoachImage(data: {
  coachId: number;
  imageUrl: string;
  isPrimary?: boolean;
  displayOrder?: number;
}): Promise<CoachImageRow> {
  if (data.isPrimary) {
    await query(`UPDATE coach_images SET is_primary = false WHERE coach_id = $1`, [data.coachId]);
  }
  const result = await query(
    `INSERT INTO coach_images (coach_id, image_url, is_primary, display_order)
     VALUES ($1, $2, $3, $4)
     RETURNING id, coach_id, image_url, is_primary, display_order`,
    [data.coachId, data.imageUrl, data.isPrimary ?? false, data.displayOrder ?? 0]
  );
  const row = result.rows[0];
  return {
    id: Number(row.id),
    coachId: Number(row.coach_id),
    imageUrl: row.image_url,
    isPrimary: row.is_primary,
    displayOrder: row.display_order,
  };
}

export async function deleteCoachImage(imageId: number): Promise<void> {
  await query(`DELETE FROM coach_images WHERE id = $1`, [imageId]);
}

export async function setCoachPrimaryImage(coachId: number, imageId: number): Promise<void> {
  await query(`UPDATE coach_images SET is_primary = false WHERE coach_id = $1`, [coachId]);
  await query(`UPDATE coach_images SET is_primary = true WHERE id = $1 AND coach_id = $2`, [imageId, coachId]);
}

import { query } from '../config/database';
import { DiscountRow } from './venue.repository';

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

function mapCoachRow(row: any): Omit<CoachRow, 'images' | 'certifications' | 'activeDiscount'> {
  return {
    id: row.id,
    userId: row.user_id,
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
      id: row.id, coachId: row.coach_id, imageUrl: row.image_url,
      isPrimary: row.is_primary, displayOrder: row.display_order,
    });
    images.set(row.coach_id, list);
  }

  for (const row of certResult.rows) {
    const list = certifications.get(row.coach_id) || [];
    list.push({
      id: row.id, coachId: row.coach_id, name: row.name,
      issuingBody: row.issuing_body, yearObtained: row.year_obtained,
      certificateUrl: row.certificate_url,
    });
    certifications.set(row.coach_id, list);
  }

  for (const row of discResult.rows) {
    if (!discounts.has(row.coach_id)) {
      discounts.set(row.coach_id, {
        id: row.id, venueId: row.venue_id, coachId: row.coach_id,
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

export async function findAll(): Promise<CoachRow[]> {
  const result = await query(
    `SELECT id, user_id, name, bio, sport_type, specialization, experience_years,
            price_per_hour, address, city, country, latitude, longitude,
            phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at
     FROM coaches ORDER BY created_at DESC`
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

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
       is_active = COALESCE($14, is_active)
     WHERE id = $1
     RETURNING id, user_id, name, bio, sport_type, specialization, experience_years,
               price_per_hour, address, city, country, latitude, longitude,
               phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at`,
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

export async function findBySport(sportType: string): Promise<CoachRow[]> {
  const result = await query(
    `SELECT id, user_id, name, bio, sport_type, specialization, experience_years,
            price_per_hour, address, city, country, latitude, longitude,
            phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at
     FROM coaches WHERE sport_type = $1 AND is_active = true ORDER BY avg_rating DESC`,
    [sportType]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

export async function findById(id: number): Promise<CoachRow | null> {
  const result = await query(
    `SELECT id, user_id, name, bio, sport_type, specialization, experience_years,
            price_per_hour, address, city, country, latitude, longitude,
            phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at
     FROM coaches WHERE id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadCoachRelations([id]);
  return assembleCoaches(result.rows, relations)[0];
}

export async function findTopDeals(): Promise<CoachRow[]> {
  const result = await query(
    `SELECT c.id, c.user_id, c.name, c.bio, c.sport_type, c.specialization,
            c.experience_years, c.price_per_hour, c.address, c.city, c.country,
            c.latitude, c.longitude, c.phone_number, c.email, c.avg_rating,
            c.total_reviews, c.is_active, c.created_at, c.updated_at
     FROM coaches c
     INNER JOIN discounts d ON d.coach_id = c.id
       AND d.is_active = true AND d.valid_from <= NOW() AND d.valid_until >= NOW()
     WHERE c.is_active = true
     ORDER BY d.discount_percent DESC NULLS LAST
     LIMIT 20`
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

export async function search(q: string): Promise<CoachRow[]> {
  const result = await query(
    `SELECT id, user_id, name, bio, sport_type, specialization, experience_years,
            price_per_hour, address, city, country, latitude, longitude,
            phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at
     FROM coaches
     WHERE is_active = true AND (name ILIKE $1 OR specialization ILIKE $1 OR city ILIKE $1)
     ORDER BY avg_rating DESC LIMIT 50`,
    [`%${q}%`]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadCoachRelations(ids);
  return assembleCoaches(result.rows, relations);
}

export async function findByUserId(userId: number): Promise<CoachRow | null> {
  const result = await query(
    `SELECT id, user_id, name, bio, sport_type, specialization, experience_years,
            price_per_hour, address, city, country, latitude, longitude,
            phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at
     FROM coaches WHERE user_id = $1`,
    [userId]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadCoachRelations([result.rows[0].id]);
  return assembleCoaches(result.rows, relations)[0];
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
  const result = await query(
    `INSERT INTO coaches (user_id, name, bio, sport_type, specialization, experience_years,
       price_per_hour, address, city, country, phone_number, email)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
     RETURNING id, user_id, name, bio, sport_type, specialization, experience_years,
               price_per_hour, address, city, country, latitude, longitude,
               phone_number, email, avg_rating, total_reviews, is_active, created_at, updated_at`,
    [
      data.userId, data.name, data.bio || null, data.sportType,
      data.specialization || null, data.experienceYears || 0, data.pricePerHour,
      data.address || null, data.city || null, data.country || null,
      data.phoneNumber || null, data.email || null,
    ]
  );
  return { ...mapCoachRow(result.rows[0]), images: [], certifications: [], activeDiscount: null };
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
    id: row.id,
    coachId: row.coach_id,
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

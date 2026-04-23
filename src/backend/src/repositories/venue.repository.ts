import { query, pool } from '../config/database';

export interface VenueRow {
  id: number;
  ownerId: number;
  name: string;
  description: string | null;
  sportType: string;
  pricePerHour: number;
  address: string;
  city: string | null;
  country: string | null;
  latitude: number | null;
  longitude: number | null;
  phoneNumber: string | null;
  email: string | null;
  avgRating: number;
  totalReviews: number;
  isActive: boolean;
  images: VenueImageRow[];
  equipment: VenueEquipmentRow[];
  activeDiscount: DiscountRow | null;
  createdAt: string;
  updatedAt: string;
}

export interface VenueImageRow {
  id: number;
  venueId: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface VenueEquipmentRow {
  id: number;
  venueId: number;
  name: string;
  description: string | null;
  isIncluded: boolean;
}

export interface DiscountRow {
  id: number;
  venueId: number | null;
  coachId: number | null;
  title: string;
  description: string | null;
  discountPercent: number | null;
  discountAmount: number | null;
  validFrom: string | null;
  validUntil: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

function mapVenueRow(row: any): Omit<VenueRow, 'images' | 'equipment' | 'activeDiscount'> {
  return {
    id: Number(row.id),
    ownerId: Number(row.owner_id),
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
    avgRating: Number(row.avg_rating),
    totalReviews: row.total_reviews,
    isActive: row.is_active,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapDiscountRow(row: any): DiscountRow {
  return {
    id: Number(row.id ?? row.discount_id),
    venueId: (row.venue_id ?? row.discount_venue_id) ? Number(row.venue_id ?? row.discount_venue_id) : null,
    coachId: (row.coach_id ?? row.discount_coach_id) ? Number(row.coach_id ?? row.discount_coach_id) : null,
    title: row.title ?? row.discount_title,
    description: row.discount_description ?? row.description ?? null,
    discountPercent: row.discount_percent != null ? Number(row.discount_percent) : null,
    discountAmount: row.discount_amount != null ? Number(row.discount_amount) : null,
    validFrom: row.valid_from?.toISOString?.() ?? row.valid_from ?? null,
    validUntil: row.valid_until?.toISOString?.() ?? row.valid_until ?? null,
    isActive: row.discount_is_active ?? row.is_active ?? true,
    createdAt: row.discount_created_at?.toISOString?.() ?? row.created_at?.toISOString?.() ?? '',
    updatedAt: row.discount_updated_at?.toISOString?.() ?? row.updated_at?.toISOString?.() ?? '',
  };
}

async function loadVenueRelations(venueIds: number[]): Promise<{
  images: Map<number, VenueImageRow[]>;
  equipment: Map<number, VenueEquipmentRow[]>;
  discounts: Map<number, DiscountRow>;
}> {
  const images = new Map<number, VenueImageRow[]>();
  const equipment = new Map<number, VenueEquipmentRow[]>();
  const discounts = new Map<number, DiscountRow>();

  if (venueIds.length === 0) return { images, equipment, discounts };

  const [imgResult, eqResult, discResult] = await Promise.all([
    query(
      `SELECT id, venue_id, image_url, is_primary, display_order
       FROM venue_images WHERE venue_id = ANY($1) ORDER BY display_order`,
      [venueIds]
    ),
    query(
      `SELECT id, venue_id, name, description, is_included
       FROM venue_equipment WHERE venue_id = ANY($1)`,
      [venueIds]
    ),
    query(
      `SELECT id, venue_id, coach_id, title, description, discount_percent,
              discount_amount, valid_from, valid_until, is_active, created_at, updated_at
       FROM discounts
       WHERE venue_id = ANY($1) AND is_active = true
         AND valid_from <= NOW() AND valid_until >= NOW()`,
      [venueIds]
    ),
  ]);

  for (const row of imgResult.rows) {
    const vid = Number(row.venue_id);
    const list = images.get(vid) || [];
    list.push({
      id: Number(row.id),
      venueId: vid,
      imageUrl: row.image_url,
      isPrimary: row.is_primary,
      displayOrder: row.display_order,
    });
    images.set(vid, list);
  }

  for (const row of eqResult.rows) {
    const vid = Number(row.venue_id);
    const list = equipment.get(vid) || [];
    list.push({
      id: Number(row.id),
      venueId: vid,
      name: row.name,
      description: row.description,
      isIncluded: row.is_included,
    });
    equipment.set(vid, list);
  }

  for (const row of discResult.rows) {
    const vid = Number(row.venue_id);
    if (!discounts.has(vid)) {
      discounts.set(vid, mapDiscountRow(row));
    }
  }

  return { images, equipment, discounts };
}

function assembleVenues(
  rows: any[],
  relations: { images: Map<number, VenueImageRow[]>; equipment: Map<number, VenueEquipmentRow[]>; discounts: Map<number, DiscountRow> }
): VenueRow[] {
  return rows.map((row) => ({
    ...mapVenueRow(row),
    images: relations.images.get(row.id) || [],
    equipment: relations.equipment.get(row.id) || [],
    activeDiscount: relations.discounts.get(row.id) || null,
  }));
}

export async function findAll(): Promise<VenueRow[]> {
  const result = await query(
    `SELECT id, owner_id, name, description, sport_type, price_per_hour, address,
            city, country, latitude, longitude, phone_number, email,
            avg_rating, total_reviews, is_active, created_at, updated_at
     FROM venues ORDER BY created_at DESC`
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadVenueRelations(ids);
  return assembleVenues(result.rows, relations);
}

export async function update(
  id: number,
  data: {
    name?: string;
    description?: string;
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
): Promise<VenueRow | null> {
  const result = await query(
    `UPDATE venues SET
       name = COALESCE($2, name),
       description = COALESCE($3, description),
       price_per_hour = COALESCE($4, price_per_hour),
       address = COALESCE($5, address),
       city = COALESCE($6, city),
       country = COALESCE($7, country),
       latitude = COALESCE($8, latitude),
       longitude = COALESCE($9, longitude),
       phone_number = COALESCE($10, phone_number),
       email = COALESCE($11, email),
       is_active = COALESCE($12, is_active)
     WHERE id = $1
     RETURNING id, owner_id, name, description, sport_type, price_per_hour, address,
               city, country, latitude, longitude, phone_number, email,
               avg_rating, total_reviews, is_active, created_at, updated_at`,
    [
      id, data.name, data.description, data.pricePerHour, data.address,
      data.city, data.country, data.latitude, data.longitude,
      data.phoneNumber, data.email, data.isActive,
    ]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadVenueRelations([id]);
  return assembleVenues(result.rows, relations)[0];
}

export async function softDelete(id: number): Promise<void> {
  await query(
    `UPDATE venues SET is_active = false, updated_at = NOW() WHERE id = $1`,
    [id]
  );
}

export async function findBySport(sportType: string): Promise<VenueRow[]> {
  const result = await query(
    `SELECT id, owner_id, name, description, sport_type, price_per_hour, address,
            city, country, latitude, longitude, phone_number, email,
            avg_rating, total_reviews, is_active, created_at, updated_at
     FROM venues
     WHERE sport_type = $1 AND is_active = true
     ORDER BY avg_rating DESC`,
    [sportType]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadVenueRelations(ids);
  return assembleVenues(result.rows, relations);
}

export async function findById(id: number): Promise<VenueRow | null> {
  const result = await query(
    `SELECT id, owner_id, name, description, sport_type, price_per_hour, address,
            city, country, latitude, longitude, phone_number, email,
            avg_rating, total_reviews, is_active, created_at, updated_at
     FROM venues WHERE id = $1`,
    [id]
  );
  if (result.rows.length === 0) return null;
  const relations = await loadVenueRelations([id]);
  return assembleVenues(result.rows, relations)[0];
}

export async function findTopDeals(): Promise<VenueRow[]> {
  const result = await query(
    `SELECT v.id, v.owner_id, v.name, v.description, v.sport_type, v.price_per_hour,
            v.address, v.city, v.country, v.latitude, v.longitude, v.phone_number,
            v.email, v.avg_rating, v.total_reviews, v.is_active, v.created_at, v.updated_at
     FROM venues v
     INNER JOIN discounts d ON d.venue_id = v.id
       AND d.is_active = true AND d.valid_from <= NOW() AND d.valid_until >= NOW()
     WHERE v.is_active = true
     ORDER BY d.discount_percent DESC NULLS LAST
     LIMIT 20`
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadVenueRelations(ids);
  return assembleVenues(result.rows, relations);
}

export async function search(q: string): Promise<VenueRow[]> {
  const result = await query(
    `SELECT id, owner_id, name, description, sport_type, price_per_hour, address,
            city, country, latitude, longitude, phone_number, email,
            avg_rating, total_reviews, is_active, created_at, updated_at
     FROM venues
     WHERE is_active = true AND (name ILIKE $1 OR address ILIKE $1 OR city ILIKE $1)
     ORDER BY avg_rating DESC
     LIMIT 50`,
    [`%${q}%`]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadVenueRelations(ids);
  return assembleVenues(result.rows, relations);
}

export async function findByOwnerId(ownerId: number): Promise<VenueRow[]> {
  const result = await query(
    `SELECT id, owner_id, name, description, sport_type, price_per_hour, address,
            city, country, latitude, longitude, phone_number, email,
            avg_rating, total_reviews, is_active, created_at, updated_at
     FROM venues WHERE owner_id = $1 ORDER BY created_at DESC`,
    [ownerId]
  );
  const ids = result.rows.map((r: any) => r.id);
  const relations = await loadVenueRelations(ids);
  return assembleVenues(result.rows, relations);
}

export async function create(data: {
  ownerId: number;
  name: string;
  description?: string;
  sportType: string;
  pricePerHour: number;
  address: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  phoneNumber?: string;
  email?: string;
}): Promise<VenueRow> {
  const result = await query(
    `INSERT INTO venues (owner_id, name, description, sport_type, price_per_hour,
       address, city, country, latitude, longitude, phone_number, email)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
     RETURNING id, owner_id, name, description, sport_type, price_per_hour, address,
               city, country, latitude, longitude, phone_number, email,
               avg_rating, total_reviews, is_active, created_at, updated_at`,
    [
      data.ownerId, data.name, data.description || null, data.sportType,
      data.pricePerHour, data.address, data.city || null, data.country || null,
      data.latitude || null, data.longitude || null, data.phoneNumber || null, data.email || null,
    ]
  );
  return { ...mapVenueRow(result.rows[0]), images: [], equipment: [], activeDiscount: null };
}

// ---- Venue Image Management ----

export async function createVenueImage(data: {
  venueId: number;
  imageUrl: string;
  isPrimary?: boolean;
  displayOrder?: number;
}): Promise<VenueImageRow> {
  if (data.isPrimary) {
    await query(`UPDATE venue_images SET is_primary = false WHERE venue_id = $1`, [data.venueId]);
  }
  const result = await query(
    `INSERT INTO venue_images (venue_id, image_url, is_primary, display_order)
     VALUES ($1, $2, $3, $4)
     RETURNING id, venue_id, image_url, is_primary, display_order`,
    [data.venueId, data.imageUrl, data.isPrimary ?? false, data.displayOrder ?? 0]
  );
  const row = result.rows[0];
  return {
    id: row.id,
    venueId: row.venue_id,
    imageUrl: row.image_url,
    isPrimary: row.is_primary,
    displayOrder: row.display_order,
  };
}

export async function deleteVenueImage(imageId: number): Promise<void> {
  await query(`DELETE FROM venue_images WHERE id = $1`, [imageId]);
}

export async function setVenuePrimaryImage(venueId: number, imageId: number): Promise<void> {
  await query(`UPDATE venue_images SET is_primary = false WHERE venue_id = $1`, [venueId]);
  await query(`UPDATE venue_images SET is_primary = true WHERE id = $1 AND venue_id = $2`, [imageId, venueId]);
}

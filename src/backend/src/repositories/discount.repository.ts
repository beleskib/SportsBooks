import { query } from '../config/database';

export interface DiscountRow {
  id: number;
  venueId: number | null;
  coachId: number | null;
  title: string;
  description: string | null;
  discountPercent: number | null;
  discountAmount: number | null;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): DiscountRow {
  return {
    id: Number(row.id),
    venueId: row.venue_id ? Number(row.venue_id) : null,
    coachId: row.coach_id ? Number(row.coach_id) : null,
    title: row.title,
    description: row.description,
    discountPercent: row.discount_percent != null ? Number(row.discount_percent) : null,
    discountAmount: row.discount_amount != null ? Number(row.discount_amount) : null,
    validFrom: row.valid_from?.toISOString?.() ?? row.valid_from,
    validUntil: row.valid_until?.toISOString?.() ?? row.valid_until,
    isActive: row.is_active,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export async function findByVenueId(venueId: number): Promise<DiscountRow[]> {
  const result = await query(
    `SELECT id, venue_id, coach_id, title, description, discount_percent,
            discount_amount, valid_from, valid_until, is_active, created_at, updated_at
     FROM discounts WHERE venue_id = $1 ORDER BY created_at DESC`,
    [venueId]
  );
  return result.rows.map(mapRow);
}

export async function findByCoachId(coachId: number): Promise<DiscountRow[]> {
  const result = await query(
    `SELECT id, venue_id, coach_id, title, description, discount_percent,
            discount_amount, valid_from, valid_until, is_active, created_at, updated_at
     FROM discounts WHERE coach_id = $1 ORDER BY created_at DESC`,
    [coachId]
  );
  return result.rows.map(mapRow);
}

export async function findById(id: number): Promise<DiscountRow | null> {
  const result = await query(
    `SELECT id, venue_id, coach_id, title, description, discount_percent,
            discount_amount, valid_from, valid_until, is_active, created_at, updated_at
     FROM discounts WHERE id = $1`,
    [id]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function create(data: {
  venueId?: number;
  coachId?: number;
  title: string;
  description?: string;
  discountPercent?: number;
  discountAmount?: number;
  validFrom: string;
  validUntil: string;
}): Promise<DiscountRow> {
  const result = await query(
    `INSERT INTO discounts (venue_id, coach_id, title, description,
       discount_percent, discount_amount, valid_from, valid_until)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
     RETURNING id, venue_id, coach_id, title, description, discount_percent,
               discount_amount, valid_from, valid_until, is_active, created_at, updated_at`,
    [
      data.venueId || null, data.coachId || null, data.title,
      data.description || null, data.discountPercent || null,
      data.discountAmount || null, data.validFrom, data.validUntil,
    ]
  );
  return mapRow(result.rows[0]);
}

export async function update(id: number, data: {
  title?: string;
  description?: string;
  discountPercent?: number;
  discountAmount?: number;
  validFrom?: string;
  validUntil?: string;
  isActive?: boolean;
}): Promise<DiscountRow> {
  const result = await query(
    `UPDATE discounts SET
       title = COALESCE($2, title),
       description = COALESCE($3, description),
       discount_percent = COALESCE($4, discount_percent),
       discount_amount = COALESCE($5, discount_amount),
       valid_from = COALESCE($6, valid_from),
       valid_until = COALESCE($7, valid_until),
       is_active = COALESCE($8, is_active)
     WHERE id = $1
     RETURNING id, venue_id, coach_id, title, description, discount_percent,
               discount_amount, valid_from, valid_until, is_active, created_at, updated_at`,
    [id, data.title, data.description, data.discountPercent, data.discountAmount, data.validFrom, data.validUntil, data.isActive],
  );
  return mapRow(result.rows[0]);
}

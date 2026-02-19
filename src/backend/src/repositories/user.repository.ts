import { query } from '../config/database';

export interface UserRow {
  id: number;
  firebaseUid: string;
  email: string;
  displayName: string | null;
  photoUrl: string | null;
  phoneNumber: string | null;
  role: string;
  partnerType: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): UserRow {
  return {
    id: row.id,
    firebaseUid: row.firebase_uid,
    email: row.email,
    displayName: row.display_name,
    photoUrl: row.photo_url,
    phoneNumber: row.phone_number,
    role: row.role,
    partnerType: row.partner_type,
    isActive: row.is_active,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

export async function findByFirebaseUid(uid: string): Promise<UserRow | null> {
  const result = await query(
    `SELECT id, firebase_uid, email, display_name, photo_url, phone_number,
            role, partner_type, is_active, created_at, updated_at
     FROM users WHERE firebase_uid = $1`,
    [uid]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function findById(id: number): Promise<UserRow | null> {
  const result = await query(
    `SELECT id, firebase_uid, email, display_name, photo_url, phone_number,
            role, partner_type, is_active, created_at, updated_at
     FROM users WHERE id = $1`,
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
     RETURNING id, firebase_uid, email, display_name, photo_url, phone_number,
               role, partner_type, is_active, created_at, updated_at`,
    [data.firebaseUid, data.email, data.displayName || null, data.photoUrl || null]
  );
  return mapRow(result.rows[0]);
}

export async function update(
  id: number,
  data: { displayName?: string; photoUrl?: string; phoneNumber?: string }
): Promise<UserRow> {
  const result = await query(
    `UPDATE users SET
       display_name = COALESCE($2, display_name),
       photo_url = COALESCE($3, photo_url),
       phone_number = COALESCE($4, phone_number)
     WHERE id = $1
     RETURNING id, firebase_uid, email, display_name, photo_url, phone_number,
               role, partner_type, is_active, created_at, updated_at`,
    [id, data.displayName, data.photoUrl, data.phoneNumber]
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
     RETURNING id, firebase_uid, email, display_name, photo_url, phone_number,
               role, partner_type, is_active, created_at, updated_at`,
    [id, role, partnerType || null]
  );
  return mapRow(result.rows[0]);
}

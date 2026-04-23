import { query } from '../config/database';

// ============================================================
// Row interfaces
// ============================================================

export interface NotificationRow {
  id: number;
  userId: number;
  type: string;
  title: string;
  body: string;
  data: Record<string, any> | null;
  isRead: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DeviceTokenRow {
  id: number;
  userId: number;
  fcmToken: string;
  deviceType: string;
  isActive: boolean;
  lastUsedAt: string;
}

// ============================================================
// Map functions
// ============================================================

function mapNotificationRow(row: any): NotificationRow {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    type: row.type,
    title: row.title,
    body: row.body,
    data: row.data,
    isRead: row.is_read,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

function mapDeviceTokenRow(row: any): DeviceTokenRow {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    fcmToken: row.fcm_token,
    deviceType: row.device_type,
    isActive: row.is_active,
    lastUsedAt: row.last_used_at?.toISOString?.() ?? row.last_used_at,
  };
}

// ============================================================
// Notification CRUD
// ============================================================

export async function createNotification(
  userId: number,
  type: string,
  title: string,
  body: string,
  data?: Record<string, any>
): Promise<NotificationRow> {
  const result = await query(
    `INSERT INTO notifications (user_id, type, title, body, data)
     VALUES ($1, $2, $3, $4, $5)
     RETURNING id, user_id, type, title, body, data, is_read, created_at, updated_at`,
    [userId, type, title, body, data ? JSON.stringify(data) : null]
  );
  return mapNotificationRow(result.rows[0]);
}

export async function findByUserId(
  userId: number,
  limit: number = 20,
  offset: number = 0
): Promise<NotificationRow[]> {
  const result = await query(
    `SELECT id, user_id, type, title, body, data, is_read, created_at, updated_at
     FROM notifications
     WHERE user_id = $1
     ORDER BY created_at DESC
     LIMIT $2 OFFSET $3`,
    [userId, limit, offset]
  );
  return result.rows.map(mapNotificationRow);
}

export async function getUnreadCount(userId: number): Promise<number> {
  const result = await query(
    `SELECT COUNT(*) AS count FROM notifications WHERE user_id = $1 AND is_read = false`,
    [userId]
  );
  return Number(result.rows[0].count);
}

export async function markAsRead(notificationIds: number[], userId: number): Promise<void> {
  await query(
    `UPDATE notifications SET is_read = true, updated_at = NOW()
     WHERE id = ANY($1) AND user_id = $2`,
    [notificationIds, userId]
  );
}

export async function markAllAsRead(userId: number): Promise<void> {
  await query(
    `UPDATE notifications SET is_read = true, updated_at = NOW()
     WHERE user_id = $1 AND is_read = false`,
    [userId]
  );
}

// ============================================================
// Device Tokens
// ============================================================

export async function registerDeviceToken(
  userId: number,
  fcmToken: string,
  deviceType: string = 'android'
): Promise<DeviceTokenRow> {
  const result = await query(
    `INSERT INTO device_tokens (user_id, fcm_token, device_type, is_active, last_used_at)
     VALUES ($1, $2, $3, true, NOW())
     ON CONFLICT (fcm_token) DO UPDATE SET
       user_id = $1,
       is_active = true,
       last_used_at = NOW(),
       updated_at = NOW()
     RETURNING id, user_id, fcm_token, device_type, is_active, last_used_at`,
    [userId, fcmToken, deviceType]
  );
  return mapDeviceTokenRow(result.rows[0]);
}

export async function removeDeviceToken(fcmToken: string): Promise<void> {
  await query(
    `UPDATE device_tokens SET is_active = false, updated_at = NOW() WHERE fcm_token = $1`,
    [fcmToken]
  );
}

export async function findActiveTokensByUserId(userId: number): Promise<string[]> {
  const result = await query(
    `SELECT fcm_token FROM device_tokens WHERE user_id = $1 AND is_active = true`,
    [userId]
  );
  return result.rows.map((r: any) => r.fcm_token);
}

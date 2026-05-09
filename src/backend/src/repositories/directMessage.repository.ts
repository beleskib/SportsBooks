import { query } from '../config/database';

// ── Row interfaces ──────────────────────────────────────────────────

export interface DirectMessageRow {
  id: number;
  senderId: number;
  receiverId: number;
  senderName: string | null;
  senderPhotoUrl: string | null;
  message: string;
  readAt: string | null;
  createdAt: string;
}

export interface ConversationPreviewRow {
  friendUserId: number;
  friendDisplayName: string | null;
  friendPhotoUrl: string | null;
  lastMessage: string;
  lastMessageAt: string;
  lastMessageSenderId: number;
  unreadCount: number;
}

// ── Row mappers ─────────────────────────────────────────────────────

function mapRow(row: any): DirectMessageRow {
  return {
    id: Number(row.id),
    senderId: Number(row.sender_id),
    receiverId: Number(row.receiver_id),
    senderName: row.sender_name ?? row.display_name ?? null,
    senderPhotoUrl: row.sender_photo_url ?? row.photo_url ?? null,
    message: row.message,
    readAt: row.read_at?.toISOString?.() ?? row.read_at ?? null,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapConversationPreview(row: any): ConversationPreviewRow {
  return {
    friendUserId: Number(row.friend_user_id),
    friendDisplayName: row.friend_display_name ?? null,
    friendPhotoUrl: row.friend_photo_url ?? null,
    lastMessage: row.last_message,
    lastMessageAt: row.last_message_at?.toISOString?.() ?? row.last_message_at,
    lastMessageSenderId: Number(row.last_message_sender_id),
    unreadCount: Number(row.unread_count ?? 0),
  };
}

// ── Queries ─────────────────────────────────────────────────────────

/**
 * Get messages between two users, ordered oldest-first (chat-style).
 */
export async function getConversation(
  userId1: number,
  userId2: number,
  limit: number = 50,
  offset: number = 0
): Promise<DirectMessageRow[]> {
  const result = await query(
    `SELECT dm.id, dm.sender_id, dm.receiver_id, dm.message, dm.read_at, dm.created_at,
            u.display_name AS sender_name, u.photo_url AS sender_photo_url
     FROM direct_messages dm
     JOIN users u ON u.id = dm.sender_id
     WHERE (dm.sender_id = $1 AND dm.receiver_id = $2)
        OR (dm.sender_id = $2 AND dm.receiver_id = $1)
     ORDER BY dm.created_at ASC
     LIMIT $3 OFFSET $4`,
    [userId1, userId2, limit, offset]
  );
  return result.rows.map(mapRow);
}

/**
 * Send a direct message. Returns the new row with sender info.
 */
export async function sendMessage(
  senderId: number,
  receiverId: number,
  message: string
): Promise<DirectMessageRow> {
  const result = await query(
    `INSERT INTO direct_messages (sender_id, receiver_id, message)
     VALUES ($1, $2, $3)
     RETURNING id, sender_id, receiver_id, message, read_at, created_at`,
    [senderId, receiverId, message]
  );

  // Fetch with sender info joined
  const full = await query(
    `SELECT dm.id, dm.sender_id, dm.receiver_id, dm.message, dm.read_at, dm.created_at,
            u.display_name AS sender_name, u.photo_url AS sender_photo_url
     FROM direct_messages dm
     JOIN users u ON u.id = dm.sender_id
     WHERE dm.id = $1`,
    [result.rows[0].id]
  );
  return mapRow(full.rows[0]);
}

/**
 * List all conversations for a user (the "inbox"), showing the latest
 * message and unread count per conversation partner.
 */
export async function getConversationList(
  userId: number
): Promise<ConversationPreviewRow[]> {
  const result = await query(
    `WITH conversation_partners AS (
       -- Every user we've ever messaged or been messaged by
       SELECT DISTINCT
         CASE WHEN sender_id = $1 THEN receiver_id ELSE sender_id END AS friend_user_id
       FROM direct_messages
       WHERE sender_id = $1 OR receiver_id = $1
     ),
     latest_msg AS (
       -- Latest message per conversation partner
       SELECT DISTINCT ON (cp.friend_user_id)
         cp.friend_user_id,
         dm.message AS last_message,
         dm.created_at AS last_message_at,
         dm.sender_id AS last_message_sender_id
       FROM conversation_partners cp
       JOIN direct_messages dm
         ON ((dm.sender_id = $1 AND dm.receiver_id = cp.friend_user_id)
          OR (dm.sender_id = cp.friend_user_id AND dm.receiver_id = $1))
       ORDER BY cp.friend_user_id, dm.created_at DESC
     ),
     unread_counts AS (
       -- Count unread messages FROM each friend TO me
       SELECT sender_id AS friend_user_id,
              COUNT(*) AS unread_count
       FROM direct_messages
       WHERE receiver_id = $1 AND read_at IS NULL
       GROUP BY sender_id
     )
     SELECT
       lm.friend_user_id,
       u.display_name AS friend_display_name,
       u.photo_url AS friend_photo_url,
       lm.last_message,
       lm.last_message_at,
       lm.last_message_sender_id,
       COALESCE(uc.unread_count, 0) AS unread_count
     FROM latest_msg lm
     JOIN users u ON u.id = lm.friend_user_id
     LEFT JOIN unread_counts uc ON uc.friend_user_id = lm.friend_user_id
     ORDER BY lm.last_message_at DESC`,
    [userId]
  );
  return result.rows.map(mapConversationPreview);
}

/**
 * Mark all messages FROM otherUserId TO userId as read.
 */
export async function markRead(
  userId: number,
  otherUserId: number
): Promise<number> {
  const result = await query(
    `UPDATE direct_messages
     SET read_at = NOW()
     WHERE receiver_id = $1 AND sender_id = $2 AND read_at IS NULL`,
    [userId, otherUserId]
  );
  return result.rowCount ?? 0;
}

/**
 * Total unread DM count across all conversations.
 */
export async function getUnreadCount(userId: number): Promise<number> {
  const result = await query(
    `SELECT COUNT(*)::INT AS cnt
     FROM direct_messages
     WHERE receiver_id = $1 AND read_at IS NULL`,
    [userId]
  );
  return Number(result.rows[0]?.cnt ?? 0);
}

import { query } from '../config/database';

// ── Row interfaces ──────────────────────────────────────────────────

export interface FeedPostRow {
  id: number;
  userId: number;
  authorName: string | null;
  authorPhotoUrl: string | null;
  postType: string;
  content: string | null;
  imageUrl: string | null;
  metadata: Record<string, any>;
  likesCount: number;
  commentsCount: number;
  isLikedByMe: boolean;
  createdAt: string;
}

export interface FeedCommentRow {
  id: number;
  postId: number;
  userId: number;
  userName: string | null;
  userPhotoUrl: string | null;
  content: string;
  createdAt: string;
}

// ── Row mappers ─────────────────────────────────────────────────────

function mapFeedPostRow(row: any): FeedPostRow {
  // Ensure all metadata values are strings for Android compatibility
  const rawMeta = typeof row.metadata === 'string' ? JSON.parse(row.metadata) : (row.metadata ?? {});
  const safeMeta: Record<string, any> = {};
  for (const [key, value] of Object.entries(rawMeta)) {
    safeMeta[key] = value != null ? String(value) : '';
  }

  return {
    id: row.id,
    userId: row.user_id,
    authorName: row.display_name ?? '',
    authorPhotoUrl: row.photo_url ?? null,
    postType: row.post_type,
    content: row.content ?? null,
    imageUrl: row.image_url ?? null,
    metadata: safeMeta,
    likesCount: Number(row.likes_count),
    commentsCount: Number(row.comments_count),
    isLikedByMe: Boolean(row.is_liked_by_me),
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

function mapFeedCommentRow(row: any): FeedCommentRow {
  return {
    id: row.id,
    postId: row.post_id,
    userId: row.user_id,
    userName: row.display_name ?? null,
    userPhotoUrl: row.photo_url ?? null,
    content: row.content,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
  };
}

// ── Feed queries ────────────────────────────────────────────────────

/**
 * Get the social feed for a user: posts from friends (accepted friendships)
 * plus the user's own posts, ordered newest first.
 */
export async function getFeedForUser(
  userId: number,
  limit = 20,
  offset = 0
): Promise<FeedPostRow[]> {
  const result = await query(
    `SELECT fp.id, fp.user_id, u.display_name, u.photo_url,
            fp.post_type, fp.content, fp.image_url, fp.metadata,
            fp.likes_count, fp.comments_count,
            EXISTS(
              SELECT 1 FROM feed_post_likes fpl
              WHERE fpl.post_id = fp.id AND fpl.user_id = $1
            ) AS is_liked_by_me,
            fp.created_at
     FROM feed_posts fp
     JOIN users u ON u.id = fp.user_id
     WHERE fp.is_active = true
       AND (
         fp.user_id = $1
         OR fp.user_id IN (
           SELECT CASE
             WHEN f.requester_id = $1 THEN f.addressee_id
             ELSE f.requester_id
           END
           FROM friendships f
           WHERE f.status = 'accepted'
             AND (f.requester_id = $1 OR f.addressee_id = $1)
         )
       )
     ORDER BY fp.created_at DESC
     LIMIT $2 OFFSET $3`,
    [userId, limit, offset]
  );
  return result.rows.map(mapFeedPostRow);
}

// ── Post CRUD ───────────────────────────────────────────────────────

export async function createPost(
  userId: number,
  postType: string,
  content: string | null,
  imageUrl: string | null,
  metadata: Record<string, any> | null
): Promise<FeedPostRow> {
  const result = await query(
    `WITH inserted AS (
       INSERT INTO feed_posts (user_id, post_type, content, image_url, metadata)
       VALUES ($1, $2, $3, $4, $5::jsonb)
       RETURNING *
     )
     SELECT i.id, i.user_id, u.display_name, u.photo_url,
            i.post_type, i.content, i.image_url, i.metadata,
            i.likes_count, i.comments_count,
            false AS is_liked_by_me,
            i.created_at
     FROM inserted i
     JOIN users u ON u.id = i.user_id`,
    [userId, postType, content, imageUrl, JSON.stringify(metadata ?? {})]
  );
  return mapFeedPostRow(result.rows[0]);
}

export async function getPostById(postId: number, viewerId?: number): Promise<FeedPostRow | null> {
  const result = await query(
    `SELECT fp.id, fp.user_id, u.display_name, u.photo_url,
            fp.post_type, fp.content, fp.image_url, fp.metadata,
            fp.likes_count, fp.comments_count,
            EXISTS(
              SELECT 1 FROM feed_post_likes fpl
              WHERE fpl.post_id = fp.id AND fpl.user_id = $2
            ) AS is_liked_by_me,
            fp.created_at
     FROM feed_posts fp
     JOIN users u ON u.id = fp.user_id
     WHERE fp.id = $1 AND fp.is_active = true`,
    [postId, viewerId ?? 0]
  );
  return result.rows.length > 0 ? mapFeedPostRow(result.rows[0]) : null;
}

/**
 * Soft delete: set is_active = false. Only the author can delete.
 * Returns true if the row was updated, false otherwise.
 */
export async function deletePost(postId: number, userId: number): Promise<boolean> {
  const result = await query(
    `UPDATE feed_posts
     SET is_active = false
     WHERE id = $1 AND user_id = $2 AND is_active = true`,
    [postId, userId]
  );
  return (result.rowCount ?? 0) > 0;
}

// ── Likes ───────────────────────────────────────────────────────────

export async function hasUserLiked(postId: number, userId: number): Promise<boolean> {
  const result = await query(
    `SELECT 1 FROM feed_post_likes
     WHERE post_id = $1 AND user_id = $2
     LIMIT 1`,
    [postId, userId]
  );
  return result.rows.length > 0;
}

export async function likePost(postId: number, userId: number): Promise<void> {
  await query(
    `INSERT INTO feed_post_likes (post_id, user_id)
     VALUES ($1, $2)
     ON CONFLICT (post_id, user_id) DO NOTHING`,
    [postId, userId]
  );
  await query(
    `UPDATE feed_posts
     SET likes_count = (
       SELECT COUNT(*) FROM feed_post_likes WHERE post_id = $1
     )
     WHERE id = $1`,
    [postId]
  );
}

export async function unlikePost(postId: number, userId: number): Promise<void> {
  await query(
    `DELETE FROM feed_post_likes
     WHERE post_id = $1 AND user_id = $2`,
    [postId, userId]
  );
  await query(
    `UPDATE feed_posts
     SET likes_count = (
       SELECT COUNT(*) FROM feed_post_likes WHERE post_id = $1
     )
     WHERE id = $1`,
    [postId]
  );
}

// ── Comments ────────────────────────────────────────────────────────

export async function getComments(
  postId: number,
  limit = 20,
  offset = 0
): Promise<FeedCommentRow[]> {
  const result = await query(
    `SELECT c.id, c.post_id, c.user_id, u.display_name, u.photo_url,
            c.content, c.created_at
     FROM feed_post_comments c
     JOIN users u ON u.id = c.user_id
     WHERE c.post_id = $1
     ORDER BY c.created_at ASC
     LIMIT $2 OFFSET $3`,
    [postId, limit, offset]
  );
  return result.rows.map(mapFeedCommentRow);
}

export async function addComment(
  postId: number,
  userId: number,
  content: string
): Promise<FeedCommentRow> {
  const result = await query(
    `WITH inserted AS (
       INSERT INTO feed_post_comments (post_id, user_id, content)
       VALUES ($1, $2, $3)
       RETURNING *
     )
     SELECT i.id, i.post_id, i.user_id, u.display_name, u.photo_url,
            i.content, i.created_at
     FROM inserted i
     JOIN users u ON u.id = i.user_id`,
    [postId, userId, content]
  );

  // Update comments_count on the post
  await query(
    `UPDATE feed_posts
     SET comments_count = (
       SELECT COUNT(*) FROM feed_post_comments WHERE post_id = $1
     )
     WHERE id = $1`,
    [postId]
  );

  return mapFeedCommentRow(result.rows[0]);
}

// ── User posts ──────────────────────────────────────────────────────

export async function getUserPosts(
  userId: number,
  limit = 20,
  offset = 0,
  viewerId?: number
): Promise<FeedPostRow[]> {
  const result = await query(
    `SELECT fp.id, fp.user_id, u.display_name, u.photo_url,
            fp.post_type, fp.content, fp.image_url, fp.metadata,
            fp.likes_count, fp.comments_count,
            EXISTS(
              SELECT 1 FROM feed_post_likes fpl
              WHERE fpl.post_id = fp.id AND fpl.user_id = $3
            ) AS is_liked_by_me,
            fp.created_at
     FROM feed_posts fp
     JOIN users u ON u.id = fp.user_id
     WHERE fp.user_id = $1 AND fp.is_active = true
     ORDER BY fp.created_at DESC
     LIMIT $2 OFFSET $4`,
    [userId, limit, viewerId ?? 0, offset]
  );
  return result.rows.map(mapFeedPostRow);
}

// ── Follower / following counts ─────────────────────────────────────
// Friendships are mutual: once accepted, both parties follow each other.
// Therefore followers === following === total accepted friendships.

export async function getFollowerCount(userId: number): Promise<number> {
  const result = await query(
    `SELECT COUNT(*)::int AS count
     FROM friendships
     WHERE (requester_id = $1 OR addressee_id = $1)
       AND status = 'accepted'`,
    [userId]
  );
  return Number(result.rows[0].count);
}

export async function getFollowingCount(userId: number): Promise<number> {
  const result = await query(
    `SELECT COUNT(*)::int AS count
     FROM friendships
     WHERE (requester_id = $1 OR addressee_id = $1)
       AND status = 'accepted'`,
    [userId]
  );
  return Number(result.rows[0].count);
}

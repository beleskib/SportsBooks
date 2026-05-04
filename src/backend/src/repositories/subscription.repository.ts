import { query } from '../config/database';

// ============================================================
// SportsBooks+ subscription repository
// ============================================================

export interface SubscriptionRow {
  id: number;
  userId: number;
  stripeSubscriptionId: string;
  stripeCustomerId: string;
  stripePriceId: string;
  status: string;
  currentPeriodStart: string;
  currentPeriodEnd: string;
  cancelAtPeriodEnd: boolean;
  canceledAt: string | null;
  trialStart: string | null;
  trialEnd: string | null;
  createdAt: string;
  updatedAt: string;
}

function mapRow(row: any): SubscriptionRow {
  return {
    id: Number(row.id),
    userId: Number(row.user_id),
    stripeSubscriptionId: row.stripe_subscription_id,
    stripeCustomerId: row.stripe_customer_id,
    stripePriceId: row.stripe_price_id,
    status: row.status,
    currentPeriodStart: row.current_period_start?.toISOString?.() ?? row.current_period_start,
    currentPeriodEnd: row.current_period_end?.toISOString?.() ?? row.current_period_end,
    cancelAtPeriodEnd: row.cancel_at_period_end,
    canceledAt: row.canceled_at?.toISOString?.() ?? row.canceled_at,
    trialStart: row.trial_start?.toISOString?.() ?? row.trial_start,
    trialEnd: row.trial_end?.toISOString?.() ?? row.trial_end,
    createdAt: row.created_at?.toISOString?.() ?? row.created_at,
    updatedAt: row.updated_at?.toISOString?.() ?? row.updated_at,
  };
}

// ---- Queries ----

export async function findByUserId(userId: number): Promise<SubscriptionRow | null> {
  const result = await query(
    `SELECT * FROM subscriptions
     WHERE user_id = $1 AND status IN ('active', 'trialing')
     ORDER BY created_at DESC LIMIT 1`,
    [userId],
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function findByStripeSubId(stripeSubId: string): Promise<SubscriptionRow | null> {
  const result = await query(
    `SELECT * FROM subscriptions WHERE stripe_subscription_id = $1`,
    [stripeSubId],
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}

export async function upsert(data: {
  userId: number;
  stripeSubscriptionId: string;
  stripeCustomerId: string;
  stripePriceId: string;
  status: string;
  currentPeriodStart: string;
  currentPeriodEnd: string;
  cancelAtPeriodEnd: boolean;
  canceledAt?: string | null;
  trialStart?: string | null;
  trialEnd?: string | null;
}): Promise<SubscriptionRow> {
  const result = await query(
    `INSERT INTO subscriptions (
       user_id, stripe_subscription_id, stripe_customer_id, stripe_price_id,
       status, current_period_start, current_period_end,
       cancel_at_period_end, canceled_at, trial_start, trial_end
     ) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)
     ON CONFLICT (stripe_subscription_id) DO UPDATE SET
       status = EXCLUDED.status,
       stripe_price_id = EXCLUDED.stripe_price_id,
       current_period_start = EXCLUDED.current_period_start,
       current_period_end = EXCLUDED.current_period_end,
       cancel_at_period_end = EXCLUDED.cancel_at_period_end,
       canceled_at = EXCLUDED.canceled_at,
       trial_start = EXCLUDED.trial_start,
       trial_end = EXCLUDED.trial_end,
       updated_at = NOW()
     RETURNING *`,
    [
      data.userId,
      data.stripeSubscriptionId,
      data.stripeCustomerId,
      data.stripePriceId,
      data.status,
      data.currentPeriodStart,
      data.currentPeriodEnd,
      data.cancelAtPeriodEnd,
      data.canceledAt ?? null,
      data.trialStart ?? null,
      data.trialEnd ?? null,
    ],
  );
  return mapRow(result.rows[0]);
}

// ---- Sync is_plus flag on users table ----
// Called after every subscription state change from webhooks.
export async function syncUserPlusFlag(userId: number): Promise<boolean> {
  const result = await query(
    `UPDATE users
     SET is_plus = EXISTS (
       SELECT 1 FROM subscriptions
       WHERE user_id = $1 AND status IN ('active', 'trialing')
     ),
     updated_at = NOW()
     WHERE id = $1
     RETURNING is_plus`,
    [userId],
  );
  return result.rows[0]?.is_plus ?? false;
}

// ---- Set Stripe customer ID on user (created lazily at checkout) ----
export async function setStripeCustomerId(userId: number, stripeCustomerId: string): Promise<void> {
  await query(
    `UPDATE users SET stripe_customer_id = $2, updated_at = NOW() WHERE id = $1`,
    [userId, stripeCustomerId],
  );
}

// ---- Get Stripe customer ID ----
export async function getStripeCustomerId(userId: number): Promise<string | null> {
  const result = await query(
    `SELECT stripe_customer_id FROM users WHERE id = $1`,
    [userId],
  );
  return result.rows[0]?.stripe_customer_id ?? null;
}

// ---- Profile visibility ----
export async function setProfileVisibility(
  userId: number,
  visibility: 'public' | 'friends_only' | 'private',
): Promise<void> {
  await query(
    `UPDATE users SET profile_visibility = $2, updated_at = NOW() WHERE id = $1`,
    [userId, visibility],
  );
}

export async function getProfileVisibility(userId: number): Promise<string> {
  const result = await query(
    `SELECT profile_visibility FROM users WHERE id = $1`,
    [userId],
  );
  return result.rows[0]?.profile_visibility ?? 'public';
}

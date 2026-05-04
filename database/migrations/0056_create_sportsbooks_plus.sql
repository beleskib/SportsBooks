-- Migration: 0056_create_sportsbooks_plus.sql
-- Description: SportsBooks+ premium subscription tier.
--   - subscriptions table tracks Stripe subscription state
--   - users.is_plus computed flag for fast gating reads
--   - users.profile_visibility controls who can see your profile
--   - users.plus_trial_ends_at one-time trial tracking
-- Created: 2026-05-04

-- ============================================================
-- UP
-- ============================================================

-- 1. Subscription status enum
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subscription_status') THEN
        CREATE TYPE subscription_status AS ENUM (
            'trialing', 'active', 'past_due', 'canceled', 'unpaid'
        );
    END IF;
END
$$;

-- 2. Profile visibility enum
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'profile_visibility') THEN
        CREATE TYPE profile_visibility AS ENUM (
            'public', 'friends_only', 'private'
        );
    END IF;
END
$$;

-- 3. Subscriptions table — mirrors Stripe subscription lifecycle
CREATE TABLE IF NOT EXISTS subscriptions (
    id                       BIGSERIAL PRIMARY KEY,
    user_id                  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    stripe_subscription_id   TEXT NOT NULL UNIQUE,
    stripe_customer_id       TEXT NOT NULL,
    stripe_price_id          TEXT NOT NULL,
    status                   subscription_status NOT NULL DEFAULT 'trialing',
    current_period_start     TIMESTAMPTZ NOT NULL,
    current_period_end       TIMESTAMPTZ NOT NULL,
    cancel_at_period_end     BOOLEAN NOT NULL DEFAULT false,
    canceled_at              TIMESTAMPTZ,
    trial_start              TIMESTAMPTZ,
    trial_end                TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id
    ON subscriptions (user_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_stripe_sub_id
    ON subscriptions (stripe_subscription_id);

-- Partial index: active/trialing subscriptions (the fast "is this user Plus?" check)
CREATE INDEX IF NOT EXISTS idx_subscriptions_active
    ON subscriptions (user_id)
    WHERE status IN ('active', 'trialing');

-- 4. Add columns to users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_plus             BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS profile_visibility   profile_visibility NOT NULL DEFAULT 'public',
    ADD COLUMN IF NOT EXISTS stripe_customer_id   TEXT,
    ADD COLUMN IF NOT EXISTS plus_trial_ends_at   TIMESTAMPTZ;

-- Index for matchmaking priority sort (Plus users first)
CREATE INDEX IF NOT EXISTS idx_users_is_plus
    ON users (is_plus)
    WHERE is_plus = true;

-- ============================================================
-- DOWN
-- ============================================================
-- ALTER TABLE users DROP COLUMN IF EXISTS plus_trial_ends_at;
-- ALTER TABLE users DROP COLUMN IF EXISTS stripe_customer_id;
-- ALTER TABLE users DROP COLUMN IF EXISTS profile_visibility;
-- ALTER TABLE users DROP COLUMN IF EXISTS is_plus;
-- DROP TABLE IF EXISTS subscriptions;
-- DROP TYPE IF EXISTS subscription_status;
-- DROP TYPE IF EXISTS profile_visibility;

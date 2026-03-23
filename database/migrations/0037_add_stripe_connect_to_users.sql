-- UP
-- Add Stripe Connect fields to users table for partner payouts
ALTER TABLE users
  ADD COLUMN stripe_account_id VARCHAR(255),
  ADD COLUMN stripe_onboarding_status VARCHAR(20) NOT NULL DEFAULT 'not_started',
  ADD COLUMN stripe_payouts_enabled BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE users
  ADD CONSTRAINT chk_stripe_onboarding_status
    CHECK (stripe_onboarding_status IN ('not_started', 'pending', 'complete'));

CREATE INDEX CONCURRENTLY idx_users_stripe_account_id
  ON users (stripe_account_id) WHERE stripe_account_id IS NOT NULL;

-- Add platform fee and transfer tracking to payments table
ALTER TABLE payments
  ADD COLUMN platform_fee_amount NUMERIC(10,2),
  ADD COLUMN stripe_transfer_id VARCHAR(255);

-- DOWN
ALTER TABLE payments
  DROP COLUMN IF EXISTS stripe_transfer_id,
  DROP COLUMN IF EXISTS platform_fee_amount;

DROP INDEX IF EXISTS idx_users_stripe_account_id;

ALTER TABLE users
  DROP CONSTRAINT IF EXISTS chk_stripe_onboarding_status,
  DROP COLUMN IF EXISTS stripe_payouts_enabled,
  DROP COLUMN IF EXISTS stripe_onboarding_status,
  DROP COLUMN IF EXISTS stripe_account_id;

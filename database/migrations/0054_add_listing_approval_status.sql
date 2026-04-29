-- Migration: 0054_add_listing_approval_status.sql
-- Description: Add an approval gate at the listing level (venues + coaches).
--              New listings default to 'pending' and are hidden from the
--              public until an admin approves. Existing rows are grandfathered
--              in as 'approved' so we don't yank live listings out from under
--              their owners. Replaces the partner-account-level gate that lived
--              on users.partner_approved_at (see 0055 for that revert).
-- Created: 2026-04-28

-- ============================================================
-- UP
-- ============================================================

-- 1. Enum used by both venues and coaches.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'listing_approval_status') THEN
        CREATE TYPE listing_approval_status AS ENUM ('pending', 'approved', 'rejected');
    END IF;
END
$$;

-- 2. venues: approval columns.
ALTER TABLE venues
    ADD COLUMN IF NOT EXISTS approval_status            listing_approval_status NOT NULL DEFAULT 'pending',
    ADD COLUMN IF NOT EXISTS approval_decided_at        TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approval_decided_by_user_id BIGINT
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD COLUMN IF NOT EXISTS approval_rejection_reason  TEXT;

-- 3. coaches: approval columns.
ALTER TABLE coaches
    ADD COLUMN IF NOT EXISTS approval_status            listing_approval_status NOT NULL DEFAULT 'pending',
    ADD COLUMN IF NOT EXISTS approval_decided_at        TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS approval_decided_by_user_id BIGINT
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD COLUMN IF NOT EXISTS approval_rejection_reason  TEXT;

-- 4. Backfill existing rows as approved (grandfather what's already live).
--    Anything that was inactive stays inactive but is still "approved" for
--    visibility-policy purposes; the public filter is approval_status = 'approved'
--    AND is_active = true, so this is safe.
UPDATE venues
SET    approval_status     = 'approved',
       approval_decided_at = COALESCE(approval_decided_at, NOW())
WHERE  approval_status = 'pending'
  AND  created_at < NOW();

UPDATE coaches
SET    approval_status     = 'approved',
       approval_decided_at = COALESCE(approval_decided_at, NOW())
WHERE  approval_status = 'pending'
  AND  created_at < NOW();

-- 5. Partial indexes for the admin "pending listings" queue.
CREATE INDEX IF NOT EXISTS idx_venues_pending_approval
    ON venues (created_at DESC)
    WHERE approval_status = 'pending';

CREATE INDEX IF NOT EXISTS idx_coaches_pending_approval
    ON coaches (created_at DESC)
    WHERE approval_status = 'pending';

-- 6. Partial indexes for public-visible listings (the hot read path).
CREATE INDEX IF NOT EXISTS idx_venues_publicly_visible
    ON venues (sport_type, city)
    WHERE approval_status = 'approved' AND is_active = true;

CREATE INDEX IF NOT EXISTS idx_coaches_publicly_visible
    ON coaches (sport_type)
    WHERE approval_status = 'approved' AND is_active = true;

-- ============================================================
-- DOWN
-- ============================================================
-- DROP INDEX IF EXISTS idx_coaches_publicly_visible;
-- DROP INDEX IF EXISTS idx_venues_publicly_visible;
-- DROP INDEX IF EXISTS idx_coaches_pending_approval;
-- DROP INDEX IF EXISTS idx_venues_pending_approval;
-- ALTER TABLE coaches DROP COLUMN IF EXISTS approval_rejection_reason;
-- ALTER TABLE coaches DROP COLUMN IF EXISTS approval_decided_by_user_id;
-- ALTER TABLE coaches DROP COLUMN IF EXISTS approval_decided_at;
-- ALTER TABLE coaches DROP COLUMN IF EXISTS approval_status;
-- ALTER TABLE venues  DROP COLUMN IF EXISTS approval_rejection_reason;
-- ALTER TABLE venues  DROP COLUMN IF EXISTS approval_decided_by_user_id;
-- ALTER TABLE venues  DROP COLUMN IF EXISTS approval_decided_at;
-- ALTER TABLE venues  DROP COLUMN IF EXISTS approval_status;
-- DROP TYPE IF EXISTS listing_approval_status;

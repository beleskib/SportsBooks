-- Migration: 0053_add_partner_approval_gate.sql
-- Description: Add partner approval gate so partners can't list venues/coaches
--              until an admin reviews their account. Also auto-promotes the
--              founding owner account (bojanbeleski@gmail.com) to admin and
--              pre-approves it as a partner.
-- Created: 2026-04-27

-- ============================================================
-- UP
-- ============================================================

-- 1. Approval audit columns on users.
--    NULL  = not approved (cannot create/update venues or coaches)
--    SET   = approved at this timestamp by this admin
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS partner_approved_at         TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS partner_approved_by_user_id BIGINT
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD COLUMN IF NOT EXISTS partner_rejection_reason    TEXT;

-- 2. Index for the admin "pending partners" queue.
--    Matches: partners (or pending applicants) with no approval yet.
CREATE INDEX IF NOT EXISTS idx_users_pending_partner_approval
    ON users (created_at DESC)
    WHERE role = 'partner' AND partner_approved_at IS NULL;

-- 3. Backfill: existing partner accounts already in the wild are grandfathered
--    in as approved (so we don't break anything currently live).
UPDATE users
SET    partner_approved_at = COALESCE(partner_approved_at, NOW())
WHERE  role = 'partner'
  AND  partner_approved_at IS NULL;

-- 4. Founding owner account: promote to admin if it exists, and pre-approve.
--    Safe to re-run: WHERE clause is idempotent.
UPDATE users
SET    role = 'admin',
       partner_approved_at = COALESCE(partner_approved_at, NOW()),
       updated_at = NOW()
WHERE  email = 'bojanbeleski@gmail.com'
  AND  role <> 'admin';

-- ============================================================
-- DOWN
-- ============================================================
-- DROP INDEX IF EXISTS idx_users_pending_partner_approval;
-- ALTER TABLE users DROP COLUMN IF EXISTS partner_rejection_reason;
-- ALTER TABLE users DROP COLUMN IF EXISTS partner_approved_by_user_id;
-- ALTER TABLE users DROP COLUMN IF EXISTS partner_approved_at;
-- Note: role/admin promotion is not reverted automatically — manual fix if needed.

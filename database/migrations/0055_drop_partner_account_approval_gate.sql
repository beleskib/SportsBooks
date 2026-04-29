-- Migration: 0055_drop_partner_account_approval_gate.sql
-- Description: Reverts the partner-account-level approval gate added in 0053.
--              That approach blocked an empty partner account from doing
--              anything, which is wrong: an empty partner account is harmless,
--              and the public only ever sees listings. The real verification
--              point is the venue/coach itself (see 0054), so the user-level
--              approval columns are no longer used.
--
--              The founder admin promotion from 0053 is preserved (still
--              correct).
-- Created: 2026-04-28

-- ============================================================
-- UP
-- ============================================================

DROP INDEX IF EXISTS idx_users_pending_partner_approval;

ALTER TABLE users
    DROP COLUMN IF EXISTS partner_rejection_reason,
    DROP COLUMN IF EXISTS partner_approved_by_user_id,
    DROP COLUMN IF EXISTS partner_approved_at;

-- ============================================================
-- DOWN
-- ============================================================
-- ALTER TABLE users
--     ADD COLUMN partner_approved_at         TIMESTAMPTZ,
--     ADD COLUMN partner_approved_by_user_id BIGINT
--         REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
--     ADD COLUMN partner_rejection_reason    TEXT;
-- CREATE INDEX idx_users_pending_partner_approval
--     ON users (created_at DESC)
--     WHERE role = 'partner' AND partner_approved_at IS NULL;

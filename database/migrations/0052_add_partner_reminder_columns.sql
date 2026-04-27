-- Migration: 0052_add_partner_reminder_columns.sql
-- Description: Track which partner-approval reminder emails have been sent for a booking.
-- Part of v2-practical-ux #9: partners get an email when a player requests a booking,
-- followed by reminders at +2h and +6h if the request is still pending. Each reminder
-- column is set atomically when the email is dispatched, giving us idempotency without
-- needing a separate "sent emails" log table. Cron job filters with `WHERE col IS NULL`.

-- UP
ALTER TABLE bookings
    ADD COLUMN partner_reminded_2h_at  TIMESTAMPTZ,
    ADD COLUMN partner_reminded_6h_at  TIMESTAMPTZ;

-- Partial indexes — only the rows that still need reminders. Cheap to maintain
-- (most bookings will have both columns set or be in a non-pending state).
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_bookings_pending_reminder_2h
    ON bookings (created_at)
    WHERE status = 'pending' AND partner_reminded_2h_at IS NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_bookings_pending_reminder_6h
    ON bookings (created_at)
    WHERE status = 'pending' AND partner_reminded_6h_at IS NULL;

-- DOWN
DROP INDEX IF EXISTS idx_bookings_pending_reminder_6h;
DROP INDEX IF EXISTS idx_bookings_pending_reminder_2h;

ALTER TABLE bookings
    DROP COLUMN IF EXISTS partner_reminded_6h_at,
    DROP COLUMN IF EXISTS partner_reminded_2h_at;

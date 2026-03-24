-- ============================================================
-- 0041_partner_approval_booking.sql
-- Adds partner approval step to the booking flow
-- ============================================================

-- UP

-- 1. Add 'approved' to booking_status enum
ALTER TYPE booking_status ADD VALUE IF NOT EXISTS 'approved' AFTER 'pending';

-- 2. Add expires_at column to bookings for 24h auto-decline
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ;

-- 3. Add new notification types for booking approval flow
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'booking_request';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'booking_approved';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'booking_declined';

-- 4. Index for expiry queries (find pending bookings past their deadline)
CREATE INDEX idx_bookings_pending_expires
    ON bookings (expires_at)
    WHERE status = 'pending' AND expires_at IS NOT NULL;

-- 5. Function to bulk-expire pending bookings past 24h
CREATE OR REPLACE FUNCTION expire_pending_bookings()
RETURNS TABLE (
    expired_booking_id BIGINT,
    expired_player_id BIGINT,
    expired_time_slot_id BIGINT,
    expired_venue_name TEXT,
    expired_coach_name TEXT
) AS $$
BEGIN
    -- Cancel expired pending bookings and return their details
    RETURN QUERY
    WITH expired AS (
        UPDATE bookings
        SET status = 'cancelled', updated_at = NOW()
        WHERE status = 'pending'
          AND expires_at IS NOT NULL
          AND expires_at < NOW()
        RETURNING id, player_id, time_slot_id, venue_id, coach_id
    )
    SELECT
        e.id,
        e.player_id,
        e.time_slot_id,
        v.name::TEXT,
        c.name::TEXT
    FROM expired e
    LEFT JOIN venues v ON v.id = e.venue_id
    LEFT JOIN coaches c ON c.id = e.coach_id;

    -- Reopen the time slots for expired bookings
    UPDATE time_slots
    SET is_available = true, updated_at = NOW()
    WHERE id IN (
        SELECT b.time_slot_id
        FROM bookings b
        WHERE b.status = 'cancelled'
          AND b.expires_at IS NOT NULL
          AND b.expires_at < NOW()
          AND b.updated_at >= NOW() - INTERVAL '1 minute'
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- DOWN
-- ============================================================
-- DROP FUNCTION IF EXISTS expire_pending_bookings();
-- DROP INDEX IF EXISTS idx_bookings_pending_expires;
-- ALTER TABLE bookings DROP COLUMN IF EXISTS expires_at;
-- Note: PostgreSQL does not support removing enum values directly.
-- To fully reverse, recreate the enum types without the new values.

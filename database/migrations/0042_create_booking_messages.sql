-- Migration: 0042_create_booking_messages.sql
-- Description: Create booking_messages table for player-partner chat on bookings
-- Created: 2026-03-24

-- ============================================================
-- UP
-- ============================================================

-- Stores chat messages exchanged between a player and the venue owner
-- or coach on a specific booking. Messages are immutable once written.
CREATE TABLE booking_messages (
    id          BIGSERIAL       PRIMARY KEY,
    booking_id  BIGINT          NOT NULL
                    REFERENCES bookings (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    sender_id   BIGINT          NOT NULL
                    REFERENCES users (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    message     TEXT            NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_booking_messages_booking_id
    ON booking_messages (booking_id);

CREATE INDEX CONCURRENTLY idx_booking_messages_sender_id
    ON booking_messages (sender_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_booking_messages_sender_id;
DROP INDEX IF EXISTS idx_booking_messages_booking_id;
DROP TABLE IF EXISTS booking_messages;

-- Migration: 0011_create_bookings.sql
-- Description: Create bookings table with double-booking prevention
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE bookings (
    id              BIGSERIAL       PRIMARY KEY,
    player_id       BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    time_slot_id    BIGINT          NOT NULL
                        REFERENCES time_slots (id)
                        ON DELETE RESTRICT
                        ON UPDATE CASCADE,
    venue_id        BIGINT
                        REFERENCES venues (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    coach_id        BIGINT
                        REFERENCES coaches (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    status          booking_status  NOT NULL DEFAULT 'pending',
    total_price     NUMERIC(10,2)   NOT NULL
                        CHECK (total_price >= 0),
    notes           TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- CRITICAL: Partial unique index prevents double-booking
-- Only one non-cancelled booking per time slot
CREATE UNIQUE INDEX CONCURRENTLY idx_bookings_active_slot
    ON bookings (time_slot_id)
    WHERE status NOT IN ('cancelled');

CREATE INDEX CONCURRENTLY idx_bookings_player_id ON bookings (player_id);
CREATE INDEX CONCURRENTLY idx_bookings_venue_id ON bookings (venue_id);
CREATE INDEX CONCURRENTLY idx_bookings_coach_id ON bookings (coach_id);
CREATE INDEX CONCURRENTLY idx_bookings_status ON bookings (status);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_bookings_status;
DROP INDEX IF EXISTS idx_bookings_coach_id;
DROP INDEX IF EXISTS idx_bookings_venue_id;
DROP INDEX IF EXISTS idx_bookings_player_id;
DROP INDEX IF EXISTS idx_bookings_active_slot;
DROP TABLE IF EXISTS bookings;

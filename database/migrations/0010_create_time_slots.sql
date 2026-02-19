-- Migration: 0010_create_time_slots.sql
-- Description: Create time_slots table for hourly bookable slots (09:00-22:00)
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE time_slots (
    id              BIGSERIAL       PRIMARY KEY,
    venue_id        BIGINT
                        REFERENCES venues (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    coach_id        BIGINT
                        REFERENCES coaches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    slot_date       DATE            NOT NULL,
    start_time      TIME            NOT NULL,
    end_time        TIME            NOT NULL,
    is_available    BOOLEAN         NOT NULL DEFAULT true,
    price_override  NUMERIC(10,2),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Exactly one of venue_id or coach_id must be set
    CONSTRAINT chk_time_slots_owner CHECK (
        (venue_id IS NOT NULL AND coach_id IS NULL) OR
        (venue_id IS NULL AND coach_id IS NOT NULL)
    ),

    -- End time must be after start time
    CONSTRAINT chk_time_slots_time_order CHECK (start_time < end_time)
);

-- Prevent duplicate slots for the same venue/date/time
CREATE UNIQUE INDEX CONCURRENTLY idx_time_slots_venue_unique
    ON time_slots (venue_id, slot_date, start_time)
    WHERE venue_id IS NOT NULL;

-- Prevent duplicate slots for the same coach/date/time
CREATE UNIQUE INDEX CONCURRENTLY idx_time_slots_coach_unique
    ON time_slots (coach_id, slot_date, start_time)
    WHERE coach_id IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_time_slots_venue_id ON time_slots (venue_id);
CREATE INDEX CONCURRENTLY idx_time_slots_coach_id ON time_slots (coach_id);
CREATE INDEX CONCURRENTLY idx_time_slots_slot_date ON time_slots (slot_date);
CREATE INDEX CONCURRENTLY idx_time_slots_available ON time_slots (is_available) WHERE is_available = true;

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_time_slots_available;
DROP INDEX IF EXISTS idx_time_slots_slot_date;
DROP INDEX IF EXISTS idx_time_slots_coach_id;
DROP INDEX IF EXISTS idx_time_slots_venue_id;
DROP INDEX IF EXISTS idx_time_slots_coach_unique;
DROP INDEX IF EXISTS idx_time_slots_venue_unique;
DROP TABLE IF EXISTS time_slots;

-- Migration: 0015_create_availability_templates.sql
-- Description: Create availability_templates for weekly recurring schedules
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE availability_templates (
    id              BIGSERIAL       PRIMARY KEY,
    venue_id        BIGINT
                        REFERENCES venues (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    coach_id        BIGINT
                        REFERENCES coaches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    day_of_week     day_of_week     NOT NULL,
    start_time      TIME            NOT NULL DEFAULT '09:00',
    end_time        TIME            NOT NULL DEFAULT '22:00',
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Exactly one of venue_id or coach_id must be set
    CONSTRAINT chk_availability_owner CHECK (
        (venue_id IS NOT NULL AND coach_id IS NULL) OR
        (venue_id IS NULL AND coach_id IS NOT NULL)
    ),

    -- End time must be after start time
    CONSTRAINT chk_availability_time_order CHECK (start_time < end_time)
);

-- One template per venue per day of week
CREATE UNIQUE INDEX CONCURRENTLY idx_availability_venue_day
    ON availability_templates (venue_id, day_of_week)
    WHERE venue_id IS NOT NULL;

-- One template per coach per day of week
CREATE UNIQUE INDEX CONCURRENTLY idx_availability_coach_day
    ON availability_templates (coach_id, day_of_week)
    WHERE coach_id IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_availability_venue_id ON availability_templates (venue_id);
CREATE INDEX CONCURRENTLY idx_availability_coach_id ON availability_templates (coach_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_availability_coach_id;
DROP INDEX IF EXISTS idx_availability_venue_id;
DROP INDEX IF EXISTS idx_availability_coach_day;
DROP INDEX IF EXISTS idx_availability_venue_day;
DROP TABLE IF EXISTS availability_templates;

-- Migration: 0006_create_venue_equipment.sql
-- Description: Create venue_equipment table for venue amenities
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE venue_equipment (
    id              BIGSERIAL       PRIMARY KEY,
    venue_id        BIGINT          NOT NULL
                        REFERENCES venues (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    is_included     BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_venue_equipment_venue_id ON venue_equipment (venue_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_venue_equipment_venue_id;
DROP TABLE IF EXISTS venue_equipment;

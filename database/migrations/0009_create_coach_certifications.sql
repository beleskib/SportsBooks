-- Migration: 0009_create_coach_certifications.sql
-- Description: Create coach_certifications table for qualifications
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE coach_certifications (
    id              BIGSERIAL       PRIMARY KEY,
    coach_id        BIGINT          NOT NULL
                        REFERENCES coaches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    name            VARCHAR(255)    NOT NULL,
    issuing_body    VARCHAR(255),
    year_obtained   INTEGER,
    certificate_url TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_coach_certifications_coach_id ON coach_certifications (coach_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_coach_certifications_coach_id;
DROP TABLE IF EXISTS coach_certifications;

-- Migration: 0008_create_coach_images.sql
-- Description: Create coach_images table for coach photo gallery
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE coach_images (
    id              BIGSERIAL       PRIMARY KEY,
    coach_id        BIGINT          NOT NULL
                        REFERENCES coaches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    image_url       TEXT            NOT NULL,
    is_primary      BOOLEAN         NOT NULL DEFAULT false,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_coach_images_coach_id ON coach_images (coach_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_coach_images_coach_id;
DROP TABLE IF EXISTS coach_images;

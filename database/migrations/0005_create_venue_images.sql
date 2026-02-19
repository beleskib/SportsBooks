-- Migration: 0005_create_venue_images.sql
-- Description: Create venue_images table for venue photo gallery
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE venue_images (
    id              BIGSERIAL       PRIMARY KEY,
    venue_id        BIGINT          NOT NULL
                        REFERENCES venues (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    image_url       TEXT            NOT NULL,
    is_primary      BOOLEAN         NOT NULL DEFAULT false,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_venue_images_venue_id ON venue_images (venue_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_venue_images_venue_id;
DROP TABLE IF EXISTS venue_images;

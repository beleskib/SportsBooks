-- Migration: 0003_create_sports_categories.sql
-- Description: Create sports_categories table for browsable sport types
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE sports_categories (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    sport_type      sport_type      NOT NULL UNIQUE,
    icon_url        TEXT,
    description     TEXT,
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_sports_categories_is_active ON sports_categories (is_active);
CREATE INDEX CONCURRENTLY idx_sports_categories_display_order ON sports_categories (display_order);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_sports_categories_display_order;
DROP INDEX IF EXISTS idx_sports_categories_is_active;
DROP TABLE IF EXISTS sports_categories;

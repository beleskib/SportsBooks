-- Migration: 0013_create_discounts.sql
-- Description: Create discounts table powering "Top Deals" feature
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE discounts (
    id                  BIGSERIAL       PRIMARY KEY,
    venue_id            BIGINT
                            REFERENCES venues (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    coach_id            BIGINT
                            REFERENCES coaches (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    title               VARCHAR(255)    NOT NULL,
    description         TEXT,
    discount_percent    NUMERIC(5,2)
                            CHECK (discount_percent > 0 AND discount_percent <= 100),
    discount_amount     NUMERIC(10,2)
                            CHECK (discount_amount > 0),
    valid_from          TIMESTAMPTZ     NOT NULL,
    valid_until         TIMESTAMPTZ     NOT NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Exactly one of venue_id or coach_id must be set
    CONSTRAINT chk_discounts_owner CHECK (
        (venue_id IS NOT NULL AND coach_id IS NULL) OR
        (venue_id IS NULL AND coach_id IS NOT NULL)
    ),

    -- Exactly one of discount_percent or discount_amount must be set
    CONSTRAINT chk_discounts_type CHECK (
        (discount_percent IS NOT NULL AND discount_amount IS NULL) OR
        (discount_percent IS NULL AND discount_amount IS NOT NULL)
    ),

    -- Valid period must be positive
    CONSTRAINT chk_discounts_period CHECK (valid_from < valid_until)
);

CREATE INDEX CONCURRENTLY idx_discounts_venue_id ON discounts (venue_id);
CREATE INDEX CONCURRENTLY idx_discounts_coach_id ON discounts (coach_id);
CREATE INDEX CONCURRENTLY idx_discounts_valid_until ON discounts (valid_until);
CREATE INDEX CONCURRENTLY idx_discounts_active ON discounts (is_active) WHERE is_active = true;

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_discounts_active;
DROP INDEX IF EXISTS idx_discounts_valid_until;
DROP INDEX IF EXISTS idx_discounts_coach_id;
DROP INDEX IF EXISTS idx_discounts_venue_id;
DROP TABLE IF EXISTS discounts;

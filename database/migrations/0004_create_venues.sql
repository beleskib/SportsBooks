-- Migration: 0004_create_venues.sql
-- Description: Create venues table for venue owner listings
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE venues (
    id              BIGSERIAL       PRIMARY KEY,
    owner_id        BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    sport_type      sport_type      NOT NULL,
    price_per_hour  NUMERIC(10,2)   NOT NULL
                        CHECK (price_per_hour > 0),
    address         TEXT            NOT NULL,
    city            VARCHAR(100),
    country         VARCHAR(100),
    latitude        NUMERIC(10,7),
    longitude       NUMERIC(10,7),
    phone_number    VARCHAR(20),
    email           VARCHAR(255),
    avg_rating      NUMERIC(3,2)    NOT NULL DEFAULT 0
                        CHECK (avg_rating >= 0 AND avg_rating <= 5),
    total_reviews   INTEGER         NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_venues_owner_id ON venues (owner_id);
CREATE INDEX CONCURRENTLY idx_venues_sport_type ON venues (sport_type);
CREATE INDEX CONCURRENTLY idx_venues_city ON venues (city);
CREATE INDEX CONCURRENTLY idx_venues_is_active ON venues (is_active);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_venues_is_active;
DROP INDEX IF EXISTS idx_venues_city;
DROP INDEX IF EXISTS idx_venues_sport_type;
DROP INDEX IF EXISTS idx_venues_owner_id;
DROP TABLE IF EXISTS venues;

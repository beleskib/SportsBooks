-- Migration: 0007_create_coaches.sql
-- Description: Create coaches table for coach listings
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE coaches (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL
                            REFERENCES users (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    name                VARCHAR(255)    NOT NULL,
    bio                 TEXT,
    sport_type          sport_type      NOT NULL,
    specialization      VARCHAR(255),
    experience_years    INTEGER         NOT NULL DEFAULT 0
                            CHECK (experience_years >= 0),
    price_per_hour      NUMERIC(10,2)   NOT NULL
                            CHECK (price_per_hour > 0),
    address             TEXT,
    city                VARCHAR(100),
    country             VARCHAR(100),
    latitude            NUMERIC(10,7),
    longitude           NUMERIC(10,7),
    phone_number        VARCHAR(20),
    email               VARCHAR(255),
    avg_rating          NUMERIC(3,2)    NOT NULL DEFAULT 0
                            CHECK (avg_rating >= 0 AND avg_rating <= 5),
    total_reviews       INTEGER         NOT NULL DEFAULT 0,
    is_active           BOOLEAN         NOT NULL DEFAULT true,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_coaches_user_id ON coaches (user_id);
CREATE INDEX CONCURRENTLY idx_coaches_sport_type ON coaches (sport_type);
CREATE INDEX CONCURRENTLY idx_coaches_city ON coaches (city);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_coaches_city;
DROP INDEX IF EXISTS idx_coaches_sport_type;
DROP INDEX IF EXISTS idx_coaches_user_id;
DROP TABLE IF EXISTS coaches;

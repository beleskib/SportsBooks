-- Migration: 0014_create_reviews.sql
-- Description: Create reviews table for player reviews of venues/coaches
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE reviews (
    id              BIGSERIAL       PRIMARY KEY,
    player_id       BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    venue_id        BIGINT
                        REFERENCES venues (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    coach_id        BIGINT
                        REFERENCES coaches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    booking_id      BIGINT
                        REFERENCES bookings (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    rating          INTEGER         NOT NULL
                        CHECK (rating >= 1 AND rating <= 5),
    comment         TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Exactly one of venue_id or coach_id must be set
    CONSTRAINT chk_reviews_target CHECK (
        (venue_id IS NOT NULL AND coach_id IS NULL) OR
        (venue_id IS NULL AND coach_id IS NOT NULL)
    )
);

-- One review per player per booking
CREATE UNIQUE INDEX CONCURRENTLY idx_reviews_player_booking
    ON reviews (player_id, booking_id)
    WHERE booking_id IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_reviews_venue_id ON reviews (venue_id);
CREATE INDEX CONCURRENTLY idx_reviews_coach_id ON reviews (coach_id);
CREATE INDEX CONCURRENTLY idx_reviews_player_id ON reviews (player_id);
CREATE INDEX CONCURRENTLY idx_reviews_rating ON reviews (rating);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_reviews_rating;
DROP INDEX IF EXISTS idx_reviews_player_id;
DROP INDEX IF EXISTS idx_reviews_coach_id;
DROP INDEX IF EXISTS idx_reviews_venue_id;
DROP INDEX IF EXISTS idx_reviews_player_booking;
DROP TABLE IF EXISTS reviews;

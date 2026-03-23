-- Migration: 0027_create_player_ratings.sql
-- Description: Create player ratings table for post-match player reviews
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE player_ratings (
    id                      BIGSERIAL       PRIMARY KEY,
    match_id                BIGINT          NOT NULL
                                REFERENCES matches (id)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE,
    rater_id                BIGINT          NOT NULL
                                REFERENCES users (id)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE,
    rated_id                BIGINT          NOT NULL
                                REFERENCES users (id)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE,
    skill_rating            INT             NOT NULL
                                CHECK (skill_rating >= 1 AND skill_rating <= 5),
    sportsmanship_rating    INT             NOT NULL
                                CHECK (sportsmanship_rating >= 1 AND sportsmanship_rating <= 5),
    punctuality_rating      INT             NOT NULL
                                CHECK (punctuality_rating >= 1 AND punctuality_rating <= 5),
    comment                 TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_player_rating UNIQUE (match_id, rater_id, rated_id),
    CONSTRAINT chk_no_self_rating CHECK (rater_id <> rated_id)
);

CREATE INDEX CONCURRENTLY idx_ratings_match_id ON player_ratings (match_id);
CREATE INDEX CONCURRENTLY idx_ratings_rater_id ON player_ratings (rater_id);
CREATE INDEX CONCURRENTLY idx_ratings_rated_id ON player_ratings (rated_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_ratings_rated_id;
DROP INDEX IF EXISTS idx_ratings_rater_id;
DROP INDEX IF EXISTS idx_ratings_match_id;
DROP TABLE IF EXISTS player_ratings;

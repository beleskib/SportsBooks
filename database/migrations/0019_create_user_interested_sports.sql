-- Migration: 0019_create_user_interested_sports.sql
-- Description: Junction table linking users to their interested sports
-- Created: 2026-03-04

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE user_interested_sports (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    sport_type      sport_type      NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX CONCURRENTLY idx_user_interested_sports_unique
    ON user_interested_sports (user_id, sport_type);
CREATE INDEX CONCURRENTLY idx_user_interested_sports_user_id
    ON user_interested_sports (user_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_user_interested_sports_user_id;
DROP INDEX IF EXISTS idx_user_interested_sports_unique;
DROP TABLE IF EXISTS user_interested_sports;

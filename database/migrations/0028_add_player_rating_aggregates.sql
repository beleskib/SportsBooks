-- Migration: 0028_add_player_rating_aggregates.sql
-- Description: Add denormalized player rating aggregates to users table
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

ALTER TABLE users
    ADD COLUMN avg_player_skill_rating          NUMERIC(3,2) DEFAULT 0,
    ADD COLUMN avg_player_sportsmanship_rating  NUMERIC(3,2) DEFAULT 0,
    ADD COLUMN avg_player_punctuality_rating    NUMERIC(3,2) DEFAULT 0,
    ADD COLUMN total_player_ratings             INT          DEFAULT 0,
    ADD COLUMN total_matches_played             INT          DEFAULT 0;

-- ============================================================
-- DOWN
-- ============================================================

ALTER TABLE users
    DROP COLUMN IF EXISTS total_matches_played,
    DROP COLUMN IF EXISTS total_player_ratings,
    DROP COLUMN IF EXISTS avg_player_punctuality_rating,
    DROP COLUMN IF EXISTS avg_player_sportsmanship_rating,
    DROP COLUMN IF EXISTS avg_player_skill_rating;

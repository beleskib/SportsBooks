-- Migration: 0021_create_user_sport_expertise.sql
-- Description: Create skill_level and experience_duration enums, plus user_sport_expertise table
-- Created: 2026-03-04

-- ============================================================
-- UP
-- ============================================================

CREATE TYPE skill_level AS ENUM ('newbie', 'beginner', 'intermediate', 'semi_pro', 'pro');

CREATE TYPE experience_duration AS ENUM (
    'less_than_1_year', '1_to_3_years', '3_to_5_years',
    '5_to_10_years', '10_plus_years'
);

CREATE TABLE user_sport_expertise (
    id                  BIGSERIAL           PRIMARY KEY,
    user_id             BIGINT              NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    sport_type          sport_type          NOT NULL,
    skill_level         skill_level         NOT NULL DEFAULT 'newbie',
    experience_duration experience_duration NOT NULL DEFAULT 'less_than_1_year',
    created_at          TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_user_sport_expertise_unique
    ON user_sport_expertise (user_id, sport_type);
CREATE INDEX idx_user_sport_expertise_user_id
    ON user_sport_expertise (user_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_user_sport_expertise_user_id;
DROP INDEX IF EXISTS idx_user_sport_expertise_unique;
DROP TABLE IF EXISTS user_sport_expertise;
DROP TYPE IF EXISTS experience_duration;
DROP TYPE IF EXISTS skill_level;

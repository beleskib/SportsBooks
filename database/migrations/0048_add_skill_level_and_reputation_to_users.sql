-- Migration: 0048_add_skill_level_and_reputation_to_users.sql
-- Description: Add self-declared skill level and no-show reputation tracking to users
-- Part of v2-practical-ux: enables skill-filtered matchmaking and no-show gating.
-- The reliability_score column is GENERATED ALWAYS: attended / (attended + no_shows),
-- falling back to 1.00 for brand-new users so they aren't blocked before their first game.

-- UP
ALTER TABLE users
    ADD COLUMN skill_level       INTEGER CHECK (skill_level BETWEEN 1 AND 5),
    ADD COLUMN no_show_count     INTEGER NOT NULL DEFAULT 0 CHECK (no_show_count >= 0),
    ADD COLUMN total_attended    INTEGER NOT NULL DEFAULT 0 CHECK (total_attended >= 0);

ALTER TABLE users
    ADD COLUMN reliability_score NUMERIC(3,2) GENERATED ALWAYS AS (
        CASE
            WHEN total_attended + no_show_count = 0 THEN 1.00
            ELSE ROUND(total_attended::NUMERIC / (total_attended + no_show_count), 2)
        END
    ) STORED;

CREATE INDEX CONCURRENTLY idx_users_skill_level       ON users (skill_level) WHERE skill_level IS NOT NULL;
CREATE INDEX CONCURRENTLY idx_users_reliability_score ON users (reliability_score);

-- DOWN
DROP INDEX IF EXISTS idx_users_reliability_score;
DROP INDEX IF EXISTS idx_users_skill_level;

ALTER TABLE users
    DROP COLUMN IF EXISTS reliability_score,
    DROP COLUMN IF EXISTS total_attended,
    DROP COLUMN IF EXISTS no_show_count,
    DROP COLUMN IF EXISTS skill_level;

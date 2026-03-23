-- Migration: 0039_create_gamification_tables.sql
-- Description: Create gamification tables — xp_transactions, player_levels, achievements, player_achievements
-- Created: 2026-03-18

-- ============================================================
-- UP
-- ============================================================

-- Tracks every XP earn or spend event for a user.
-- Positive amount = XP earned; negative amount = XP spent/redeemed.
CREATE TABLE xp_transactions (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    amount          INTEGER         NOT NULL,
    source_type     VARCHAR(50)     NOT NULL,
    source_id       BIGINT,
    description     TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_xp_transactions_source_type CHECK (
        source_type IN (
            'booking_completed',
            'match_completed',
            'review_written',
            'first_booking',
            'achievement_earned',
            'xp_redemption'
        )
    )
);

CREATE INDEX CONCURRENTLY idx_xp_transactions_user_id
    ON xp_transactions (user_id);

CREATE INDEX CONCURRENTLY idx_xp_transactions_source_type
    ON xp_transactions (source_type);

CREATE INDEX CONCURRENTLY idx_xp_transactions_created_at
    ON xp_transactions (user_id, created_at DESC);

-- ----------------------------------------------------------------
-- Cached current level and XP totals for each player.
-- Updated whenever an xp_transaction is inserted.
-- ----------------------------------------------------------------
CREATE TABLE player_levels (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL UNIQUE
                            REFERENCES users (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    total_xp            INTEGER         NOT NULL DEFAULT 0,
    current_level       INTEGER         NOT NULL DEFAULT 1,
    xp_to_next_level    INTEGER         NOT NULL DEFAULT 100,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_player_levels_total_xp       CHECK (total_xp >= 0),
    CONSTRAINT chk_player_levels_current_level  CHECK (current_level >= 1)
);

-- ----------------------------------------------------------------
-- Catalogue of all achievements that can be unlocked.
-- ----------------------------------------------------------------
CREATE TABLE achievements (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    description     TEXT            NOT NULL,
    icon            VARCHAR(50)     NOT NULL,
    category        VARCHAR(50)     NOT NULL DEFAULT 'general',
    xp_reward       INTEGER         NOT NULL DEFAULT 0,
    criteria_type   VARCHAR(50)     NOT NULL,
    criteria_value  INTEGER         NOT NULL DEFAULT 1,
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_achievements_xp_reward       CHECK (xp_reward >= 0),
    CONSTRAINT chk_achievements_criteria_value  CHECK (criteria_value >= 1),
    CONSTRAINT chk_achievements_category CHECK (
        category IN ('booking', 'match', 'social', 'general')
    )
);

CREATE INDEX CONCURRENTLY idx_achievements_category
    ON achievements (category);

CREATE INDEX CONCURRENTLY idx_achievements_is_active
    ON achievements (is_active)
    WHERE is_active = true;

-- ----------------------------------------------------------------
-- Records each achievement that a user has earned.
-- ----------------------------------------------------------------
CREATE TABLE player_achievements (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    achievement_id  BIGINT          NOT NULL
                        REFERENCES achievements (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    earned_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_player_achievement UNIQUE (user_id, achievement_id)
);

CREATE INDEX CONCURRENTLY idx_player_achievements_user_id
    ON player_achievements (user_id);

CREATE INDEX CONCURRENTLY idx_player_achievements_achievement_id
    ON player_achievements (achievement_id);

-- ----------------------------------------------------------------
-- updated_at auto-maintenance triggers for tables that carry the column
-- ----------------------------------------------------------------
CREATE TRIGGER trg_player_levels_updated_at
    BEFORE UPDATE ON player_levels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_achievements_updated_at
    BEFORE UPDATE ON achievements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_achievements_updated_at   ON achievements;
DROP TRIGGER IF EXISTS trg_player_levels_updated_at  ON player_levels;

DROP INDEX IF EXISTS idx_player_achievements_achievement_id;
DROP INDEX IF EXISTS idx_player_achievements_user_id;
DROP TABLE IF EXISTS player_achievements;

DROP INDEX IF EXISTS idx_achievements_is_active;
DROP INDEX IF EXISTS idx_achievements_category;
DROP TABLE IF EXISTS achievements;

DROP TABLE IF EXISTS player_levels;

DROP INDEX IF EXISTS idx_xp_transactions_created_at;
DROP INDEX IF EXISTS idx_xp_transactions_source_type;
DROP INDEX IF EXISTS idx_xp_transactions_user_id;
DROP TABLE IF EXISTS xp_transactions;

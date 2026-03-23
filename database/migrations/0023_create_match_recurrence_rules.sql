-- Migration: 0023_create_match_recurrence_rules.sql
-- Description: Create match recurrence rules table for recurring matches
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE match_recurrence_rules (
    id                      BIGSERIAL               PRIMARY KEY,
    host_id                 BIGINT                  NOT NULL
                                REFERENCES users (id)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE,
    frequency               recurrence_frequency    NOT NULL,
    day_of_week             day_of_week             NOT NULL,
    start_time              TIME                    NOT NULL,
    end_time                TIME                    NOT NULL
                                CHECK (end_time > start_time),
    sport_type              sport_type              NOT NULL,
    title                   VARCHAR(200)            NOT NULL,
    venue_id                BIGINT
                                REFERENCES venues (id)
                                ON DELETE SET NULL
                                ON UPDATE CASCADE,
    location_name           VARCHAR(200),
    address                 TEXT,
    latitude                NUMERIC(10,7),
    longitude               NUMERIC(10,7),
    min_players             INT                     NOT NULL DEFAULT 2
                                CHECK (min_players >= 2),
    max_players             INT                     NOT NULL
                                CHECK (max_players >= min_players),
    min_skill_level         INT                     DEFAULT 1
                                CHECK (min_skill_level >= 1 AND min_skill_level <= 5),
    max_skill_level         INT                     DEFAULT 5
                                CHECK (max_skill_level >= 1 AND max_skill_level <= 5),
    is_active               BOOLEAN                 NOT NULL DEFAULT TRUE,
    next_occurrence_date    DATE,
    created_at              TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_recurrence_skill_range CHECK (max_skill_level >= min_skill_level)
);

CREATE INDEX CONCURRENTLY idx_recurrence_host_id ON match_recurrence_rules (host_id);
CREATE INDEX CONCURRENTLY idx_recurrence_active ON match_recurrence_rules (is_active) WHERE is_active = TRUE;

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_recurrence_active;
DROP INDEX IF EXISTS idx_recurrence_host_id;
DROP TABLE IF EXISTS match_recurrence_rules;

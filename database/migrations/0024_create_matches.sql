-- Migration: 0024_create_matches.sql
-- Description: Create matches table — core of the matchmaking system
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE matches (
    id                      BIGSERIAL           PRIMARY KEY,
    host_id                 BIGINT              NOT NULL
                                REFERENCES users (id)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE,
    booking_id              BIGINT
                                REFERENCES bookings (id)
                                ON DELETE SET NULL
                                ON UPDATE CASCADE,
    venue_id                BIGINT
                                REFERENCES venues (id)
                                ON DELETE SET NULL
                                ON UPDATE CASCADE,
    sport_type              sport_type          NOT NULL,
    match_type              match_type          NOT NULL,
    status                  match_status        NOT NULL DEFAULT 'draft',
    visibility              match_visibility    NOT NULL DEFAULT 'public',
    title                   VARCHAR(200)        NOT NULL,
    description             TEXT,
    match_date              DATE                NOT NULL,
    start_time              TIME                NOT NULL,
    end_time                TIME                NOT NULL
                                CHECK (end_time > start_time),
    min_players             INT                 NOT NULL DEFAULT 2
                                CHECK (min_players >= 2),
    max_players             INT                 NOT NULL
                                CHECK (max_players >= min_players),
    current_players         INT                 NOT NULL DEFAULT 1
                                CHECK (current_players >= 0),
    min_skill_level         INT                 DEFAULT 1
                                CHECK (min_skill_level >= 1 AND min_skill_level <= 5),
    max_skill_level         INT                 DEFAULT 5
                                CHECK (max_skill_level >= 1 AND max_skill_level <= 5),
    location_name           VARCHAR(200),
    address                 TEXT,
    latitude                NUMERIC(10,7),
    longitude               NUMERIC(10,7),
    is_free                 BOOLEAN             NOT NULL DEFAULT TRUE,
    cost_per_player         NUMERIC(10,2)       DEFAULT 0
                                CHECK (cost_per_player >= 0),
    recurrence_rule_id      BIGINT
                                REFERENCES match_recurrence_rules (id)
                                ON DELETE SET NULL
                                ON UPDATE CASCADE,
    parent_match_id         BIGINT
                                REFERENCES matches (id)
                                ON DELETE SET NULL
                                ON UPDATE CASCADE,
    created_at              TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_match_skill_range CHECK (max_skill_level >= min_skill_level),
    CONSTRAINT chk_match_players CHECK (current_players <= max_players)
);

CREATE INDEX CONCURRENTLY idx_matches_host_id ON matches (host_id);
CREATE INDEX CONCURRENTLY idx_matches_venue_id ON matches (venue_id);
CREATE INDEX CONCURRENTLY idx_matches_sport_type ON matches (sport_type);
CREATE INDEX CONCURRENTLY idx_matches_status ON matches (status);
CREATE INDEX CONCURRENTLY idx_matches_date ON matches (match_date);
CREATE INDEX CONCURRENTLY idx_matches_open_public
    ON matches (visibility, status)
    WHERE status = 'open' AND visibility = 'public';

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_matches_open_public;
DROP INDEX IF EXISTS idx_matches_date;
DROP INDEX IF EXISTS idx_matches_status;
DROP INDEX IF EXISTS idx_matches_sport_type;
DROP INDEX IF EXISTS idx_matches_venue_id;
DROP INDEX IF EXISTS idx_matches_host_id;
DROP TABLE IF EXISTS matches;

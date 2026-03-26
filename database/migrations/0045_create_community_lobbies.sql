-- Migration: 0045_create_community_lobbies.sql
-- Description: Create community_lobbies and lobby_participants tables for in-community game organisation
-- Created: 2026-03-25

-- ============================================================
-- UP
-- ============================================================

-- Stores game lobbies created inside a community.
-- Lobbies start private (is_public = false) and can be promoted to
-- public by the creator; made_public_at records the exact moment
-- that transition happened so the feed can surface the event.
-- current_players is a denormalised counter kept in sync by the
-- application layer (incremented/decremented as participants join
-- or leave) to avoid expensive COUNT(*) joins on hot read paths.
CREATE TABLE community_lobbies (
    id                  BIGSERIAL       PRIMARY KEY,
    community_id        BIGINT          NOT NULL
                            REFERENCES communities (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    created_by          BIGINT          NOT NULL
                            REFERENCES users (id)
                            ON DELETE RESTRICT
                            ON UPDATE CASCADE,
    title               VARCHAR(150)    NOT NULL,
    sport_type          VARCHAR(50)     NOT NULL,
    scheduled_date      DATE            NOT NULL,
    scheduled_time      TIME            NOT NULL,
    duration_minutes    INT             NOT NULL DEFAULT 60,
    max_players         INT             NOT NULL,
    current_players     INT             NOT NULL DEFAULT 0,
    venue_id            BIGINT
                            REFERENCES venues (id)
                            ON DELETE SET NULL
                            ON UPDATE CASCADE,
    skill_level_min     INT             NOT NULL DEFAULT 1,
    skill_level_max     INT             NOT NULL DEFAULT 5,
    status              VARCHAR(20)     NOT NULL DEFAULT 'open',
    is_public           BOOLEAN         NOT NULL DEFAULT false,
    made_public_at      TIMESTAMPTZ,
    description         TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_community_lobbies_status CHECK (
        status IN ('open', 'full', 'in_progress', 'completed', 'cancelled')
    ),
    CONSTRAINT chk_community_lobbies_duration       CHECK (duration_minutes > 0),
    CONSTRAINT chk_community_lobbies_max_players    CHECK (max_players > 0),
    CONSTRAINT chk_community_lobbies_current_players CHECK (
        current_players >= 0 AND current_players <= max_players
    ),
    CONSTRAINT chk_community_lobbies_skill_range    CHECK (
        skill_level_min >= 1
        AND skill_level_max <= 5
        AND skill_level_min <= skill_level_max
    ),
    -- made_public_at must only be set when the lobby is actually public.
    CONSTRAINT chk_community_lobbies_public_at CHECK (
        made_public_at IS NULL OR is_public = true
    )
);

CREATE INDEX CONCURRENTLY idx_community_lobbies_community_id
    ON community_lobbies (community_id);

CREATE INDEX CONCURRENTLY idx_community_lobbies_status
    ON community_lobbies (status);

CREATE INDEX CONCURRENTLY idx_community_lobbies_scheduled_date
    ON community_lobbies (scheduled_date);

-- Composite index for the common query: upcoming open lobbies in a community.
CREATE INDEX CONCURRENTLY idx_community_lobbies_community_open
    ON community_lobbies (community_id, scheduled_date, scheduled_time)
    WHERE status = 'open';

-- ----------------------------------------------------------------
-- Tracks every participant in a lobby.
-- joined_at is a dedicated column (not created_at) because it
-- semantically represents when the player entered the lobby, and
-- it defaults independently.  A player who leaves and re-joins
-- would have a new row; the unique constraint prevents duplicates
-- in the active state — history is preserved by the status column.
-- ----------------------------------------------------------------
CREATE TABLE lobby_participants (
    id          BIGSERIAL       PRIMARY KEY,
    lobby_id    BIGINT          NOT NULL
                    REFERENCES community_lobbies (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    user_id     BIGINT          NOT NULL
                    REFERENCES users (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    status      VARCHAR(20)     NOT NULL DEFAULT 'joined',
    joined_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_lobby_participants_lobby_user UNIQUE (lobby_id, user_id),
    CONSTRAINT chk_lobby_participants_status CHECK (
        status IN ('joined', 'left', 'kicked')
    )
);

CREATE INDEX CONCURRENTLY idx_lobby_participants_lobby_id
    ON lobby_participants (lobby_id);

CREATE INDEX CONCURRENTLY idx_lobby_participants_user_id
    ON lobby_participants (user_id);

-- Partial index for fast count of active participants per lobby.
CREATE INDEX CONCURRENTLY idx_lobby_participants_active
    ON lobby_participants (lobby_id)
    WHERE status = 'joined';

-- ----------------------------------------------------------------
-- updated_at auto-maintenance triggers (function defined in 0016)
-- ----------------------------------------------------------------
CREATE TRIGGER trg_community_lobbies_updated_at
    BEFORE UPDATE ON community_lobbies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_lobby_participants_updated_at
    BEFORE UPDATE ON lobby_participants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_lobby_participants_updated_at ON lobby_participants;
DROP TRIGGER IF EXISTS trg_community_lobbies_updated_at  ON community_lobbies;

DROP INDEX IF EXISTS idx_lobby_participants_active;
DROP INDEX IF EXISTS idx_lobby_participants_user_id;
DROP INDEX IF EXISTS idx_lobby_participants_lobby_id;
DROP TABLE IF EXISTS lobby_participants;

DROP INDEX IF EXISTS idx_community_lobbies_community_open;
DROP INDEX IF EXISTS idx_community_lobbies_scheduled_date;
DROP INDEX IF EXISTS idx_community_lobbies_status;
DROP INDEX IF EXISTS idx_community_lobbies_community_id;
DROP TABLE IF EXISTS community_lobbies;

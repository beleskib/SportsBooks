-- Migration: 0025_create_match_participants.sql
-- Description: Create match participants table for tracking who joined each match
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE match_participants (
    id              BIGSERIAL           PRIMARY KEY,
    match_id        BIGINT              NOT NULL
                        REFERENCES matches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    user_id         BIGINT              NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    status          participant_status  NOT NULL DEFAULT 'pending',
    role            participant_role    NOT NULL DEFAULT 'player',
    joined_at       TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_match_participant UNIQUE (match_id, user_id)
);

CREATE INDEX CONCURRENTLY idx_participants_match_id ON match_participants (match_id);
CREATE INDEX CONCURRENTLY idx_participants_user_id ON match_participants (user_id);
CREATE INDEX CONCURRENTLY idx_participants_status ON match_participants (status);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_participants_status;
DROP INDEX IF EXISTS idx_participants_user_id;
DROP INDEX IF EXISTS idx_participants_match_id;
DROP TABLE IF EXISTS match_participants;

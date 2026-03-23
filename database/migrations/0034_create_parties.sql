-- Migration: 0034_create_parties.sql
-- Description: Create parties table for squad/group match joining
-- Created: 2026-03-13

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE parties (
    id              BIGSERIAL           PRIMARY KEY,
    leader_id       BIGINT              NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    name            VARCHAR(100)        NULL,
    sport_type      sport_type          NULL,
    status          party_status        NOT NULL DEFAULT 'forming',
    match_id        BIGINT              NULL
                        REFERENCES matches (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_parties_leader ON parties (leader_id);
CREATE INDEX CONCURRENTLY idx_parties_status ON parties (status);
CREATE INDEX CONCURRENTLY idx_parties_active
    ON parties (leader_id)
    WHERE status IN ('forming', 'ready');

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_parties_active;
DROP INDEX IF EXISTS idx_parties_status;
DROP INDEX IF EXISTS idx_parties_leader;
DROP TABLE IF EXISTS parties;

-- Migration: 0035_create_party_members.sql
-- Description: Create party_members table for tracking party invites and membership
-- Created: 2026-03-13

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE party_members (
    id              BIGSERIAL               PRIMARY KEY,
    party_id        BIGINT                  NOT NULL
                        REFERENCES parties (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    user_id         BIGINT                  NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    status          party_member_status     NOT NULL DEFAULT 'invited',
    responded_at    TIMESTAMPTZ             NULL,
    created_at      TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_party_member UNIQUE (party_id, user_id)
);

CREATE INDEX CONCURRENTLY idx_party_members_party ON party_members (party_id);
CREATE INDEX CONCURRENTLY idx_party_members_user ON party_members (user_id);
CREATE INDEX CONCURRENTLY idx_party_members_invited
    ON party_members (user_id)
    WHERE status = 'invited';

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_party_members_invited;
DROP INDEX IF EXISTS idx_party_members_user;
DROP INDEX IF EXISTS idx_party_members_party;
DROP TABLE IF EXISTS party_members;

-- Migration: 0036_add_party_id_to_match_participants.sql
-- Description: Add party_id column to match_participants to link group joins
-- Created: 2026-03-13

-- ============================================================
-- UP
-- ============================================================

ALTER TABLE match_participants
    ADD COLUMN party_id BIGINT NULL
        REFERENCES parties (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;

CREATE INDEX CONCURRENTLY idx_participants_party_id ON match_participants (party_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_participants_party_id;
ALTER TABLE match_participants DROP COLUMN IF EXISTS party_id;

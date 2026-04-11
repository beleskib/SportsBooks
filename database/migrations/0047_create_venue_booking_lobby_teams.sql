-- Migration: 0047_create_venue_booking_lobby_teams.sql
-- Description: Add teams support to venue booking lobbies.
--              Teams allow lobby members to organise into groups.
--              In "split_to_teams" payment mode the total price is divided
--              equally among teams and each team leader pays the team share.
-- Created: 2026-03-28

-- ============================================================
-- UP
-- ============================================================

ALTER TABLE venue_booking_lobbies
    DROP CONSTRAINT IF EXISTS chk_vbl_payment_type;

ALTER TABLE venue_booking_lobbies
    ADD CONSTRAINT chk_vbl_payment_type CHECK (
        payment_type IN ('split', 'creator_pays', 'split_to_teams')
    );

CREATE TABLE IF NOT EXISTS venue_booking_lobby_teams (
    id              BIGSERIAL       PRIMARY KEY,
    lobby_id        BIGINT          NOT NULL
                        REFERENCES venue_booking_lobbies (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    team_name       VARCHAR(100)    NOT NULL,
    team_number     INT             NOT NULL,
    leader_id       BIGINT
                        REFERENCES users (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    share_amount    NUMERIC(10,2),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_vblt_lobby_number  UNIQUE (lobby_id, team_number),
    CONSTRAINT chk_vblt_team_number  CHECK (team_number >= 1)
);

CREATE INDEX IF NOT EXISTS idx_vblt_lobby_id
    ON venue_booking_lobby_teams (lobby_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'venue_booking_lobby_members' AND column_name = 'team_id'
    ) THEN
        ALTER TABLE venue_booking_lobby_members
            ADD COLUMN team_id BIGINT
                REFERENCES venue_booking_lobby_teams (id)
                ON DELETE SET NULL
                ON UPDATE CASCADE;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_vblm_team_id
    ON venue_booking_lobby_members (team_id);

DROP TRIGGER IF EXISTS trg_vblt_updated_at ON venue_booking_lobby_teams;
CREATE TRIGGER trg_vblt_updated_at
    BEFORE UPDATE ON venue_booking_lobby_teams
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_vblt_updated_at ON venue_booking_lobby_teams;

DROP INDEX IF EXISTS idx_vblm_team_id;
ALTER TABLE venue_booking_lobby_members DROP COLUMN IF EXISTS team_id;

DROP INDEX IF EXISTS idx_vblt_lobby_id;
DROP TABLE IF EXISTS venue_booking_lobby_teams;

ALTER TABLE venue_booking_lobbies
    DROP CONSTRAINT IF EXISTS chk_vbl_payment_type;
ALTER TABLE venue_booking_lobbies
    ADD CONSTRAINT chk_vbl_payment_type CHECK (
        payment_type IN ('split', 'creator_pays')
    );

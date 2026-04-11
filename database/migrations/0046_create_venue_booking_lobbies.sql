-- Migration: 0046_create_venue_booking_lobbies.sql
-- Description: Create venue_booking_lobbies and venue_booking_lobby_members tables
--              for player-organised venue bookings with split payments
-- Created: 2026-03-27

-- ============================================================
-- UP
-- ============================================================

-- Stores lobbies that players create to organise a shared venue booking.
-- A lobby represents a group of players who intend to split the cost of a
-- single time slot at a venue.  The status lifecycle is:
--
--   open → full → booking_pending → booking_approved
--        → payment_in_progress → confirmed
--   (any state) → cancelled | expired
--
-- total_price and price_per_player are stored at creation time so that
-- subsequent price changes on the venue or time slot do not alter what
-- members agreed to pay.
-- current_players is a denormalised counter kept in sync by the application
-- layer to avoid COUNT(*) joins on hot read paths; it starts at 1 because
-- the creator is automatically the first member.
-- venue_id is denormalised from the time_slot row for efficient filtering
-- without a join when listing open lobbies for a venue.
CREATE TABLE venue_booking_lobbies (
    id                  BIGSERIAL           PRIMARY KEY,
    creator_id          BIGINT              NOT NULL
                            REFERENCES users (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    time_slot_id        BIGINT              NOT NULL
                            REFERENCES time_slots (id)
                            ON DELETE RESTRICT
                            ON UPDATE CASCADE,
    venue_id            BIGINT              NOT NULL
                            REFERENCES venues (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE,
    title               VARCHAR(150)        NOT NULL,
    payment_type        VARCHAR(20)         NOT NULL DEFAULT 'split',
    max_players         INT                 NOT NULL,
    current_players     INT                 NOT NULL DEFAULT 1,
    total_price         NUMERIC(10,2)       NOT NULL,
    price_per_player    NUMERIC(10,2)       NOT NULL,
    currency            VARCHAR(3)          NOT NULL DEFAULT 'MKD',
    status              VARCHAR(20)         NOT NULL DEFAULT 'open',
    booking_id          BIGINT
                            REFERENCES bookings (id)
                            ON DELETE SET NULL
                            ON UPDATE CASCADE,
    description         TEXT,
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_vbl_max_players          CHECK (max_players >= 2),
    CONSTRAINT chk_vbl_current_players      CHECK (current_players >= 0),
    CONSTRAINT chk_vbl_total_price          CHECK (total_price > 0),
    CONSTRAINT chk_vbl_price_per_player     CHECK (price_per_player > 0),
    CONSTRAINT chk_vbl_payment_type        CHECK (
        payment_type IN ('split', 'creator_pays')
    ),
    CONSTRAINT chk_vbl_status              CHECK (
        status IN (
            'open',
            'full',
            'booking_pending',
            'booking_approved',
            'payment_in_progress',
            'confirmed',
            'cancelled',
            'expired'
        )
    )
);

CREATE INDEX CONCURRENTLY idx_vbl_creator_id
    ON venue_booking_lobbies (creator_id);

CREATE INDEX CONCURRENTLY idx_vbl_venue_id
    ON venue_booking_lobbies (venue_id);

CREATE INDEX CONCURRENTLY idx_vbl_time_slot_id
    ON venue_booking_lobbies (time_slot_id);

CREATE INDEX CONCURRENTLY idx_vbl_status
    ON venue_booking_lobbies (status);

-- Partial index for the hot path: listing open lobbies for a specific venue,
-- newest first.  Only rows where status = 'open' are indexed, so the scan
-- is narrow even on large tables.
CREATE INDEX CONCURRENTLY idx_vbl_open_by_venue
    ON venue_booking_lobbies (venue_id, created_at DESC)
    WHERE status = 'open';

-- ----------------------------------------------------------------
-- Tracks every member of a venue booking lobby.
-- joined_at is a dedicated column (semantically distinct from
-- created_at) recording when the player entered the lobby.
-- payment_id and paid_at are set when the member's share payment
-- is processed.  share_amount captures the exact amount due at the
-- time of joining so that late price recalculations do not affect
-- already-committed members.
-- The unique constraint on (lobby_id, user_id) prevents duplicate
-- membership rows at the database level.
-- ----------------------------------------------------------------
CREATE TABLE venue_booking_lobby_members (
    id              BIGSERIAL       PRIMARY KEY,
    lobby_id        BIGINT          NOT NULL
                        REFERENCES venue_booking_lobbies (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    user_id         BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'joined',
    payment_id      BIGINT
                        REFERENCES payments (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    share_amount    NUMERIC(10,2),
    joined_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_vblm_lobby_user   UNIQUE (lobby_id, user_id),
    CONSTRAINT chk_vblm_status      CHECK (
        status IN ('joined', 'left', 'payment_pending', 'paid')
    )
);

CREATE INDEX CONCURRENTLY idx_vblm_lobby_id
    ON venue_booking_lobby_members (lobby_id);

CREATE INDEX CONCURRENTLY idx_vblm_user_id
    ON venue_booking_lobby_members (user_id);

-- Partial index for fast look-up of active members per lobby, used when
-- computing current_players counts and for lobby roster queries.
CREATE INDEX CONCURRENTLY idx_vblm_active
    ON venue_booking_lobby_members (lobby_id)
    WHERE status = 'joined';

-- ----------------------------------------------------------------
-- updated_at auto-maintenance triggers (function defined in 0016)
-- ----------------------------------------------------------------
CREATE TRIGGER trg_vbl_updated_at
    BEFORE UPDATE ON venue_booking_lobbies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_vblm_updated_at
    BEFORE UPDATE ON venue_booking_lobby_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_vblm_updated_at ON venue_booking_lobby_members;
DROP TRIGGER IF EXISTS trg_vbl_updated_at   ON venue_booking_lobbies;

DROP INDEX IF EXISTS idx_vblm_active;
DROP INDEX IF EXISTS idx_vblm_user_id;
DROP INDEX IF EXISTS idx_vblm_lobby_id;
DROP TABLE IF EXISTS venue_booking_lobby_members;

DROP INDEX IF EXISTS idx_vbl_open_by_venue;
DROP INDEX IF EXISTS idx_vbl_status;
DROP INDEX IF EXISTS idx_vbl_time_slot_id;
DROP INDEX IF EXISTS idx_vbl_venue_id;
DROP INDEX IF EXISTS idx_vbl_creator_id;
DROP TABLE IF EXISTS venue_booking_lobbies;

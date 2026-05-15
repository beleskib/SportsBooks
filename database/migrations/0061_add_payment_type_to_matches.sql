-- Migration: 0061_add_payment_type_to_matches.sql
-- Description: Add payment-related columns to the matches table to absorb the
--   Venue Booking Lobby functionality.  Introduces the match_payment_type enum
--   and five new columns: payment_type, time_slot_id, total_price,
--   price_per_player, and currency.
-- Created: 2026-05-15
--
-- NOTE ON CONCURRENTLY:
--   CREATE INDEX CONCURRENTLY cannot run inside an explicit transaction block.
--   When applying this migration with a transaction-wrapping runner, replace
--   each CONCURRENTLY keyword with a plain CREATE INDEX (or run this file
--   outside a transaction).  The ALTER TABLE statements are transactional and
--   safe to wrap.

-- ============================================================
-- UP
-- ============================================================

-- 1. Create the enum type for match payment modes
CREATE TYPE match_payment_type AS ENUM (
    'host_pays',
    'split',
    'split_to_teams',
    'cash_at_venue'
);

-- 2. Add payment_type column
--    Defaults to 'host_pays' so existing rows are not invalidated.
ALTER TABLE matches
    ADD COLUMN payment_type match_payment_type NOT NULL DEFAULT 'host_pays';

-- 3. Add time_slot_id column
--    Persists the time slot that was used when creating the match booking.
--    SET NULL on delete prevents cascade-deleting the match if the slot is
--    removed; the match data remains intact.
ALTER TABLE matches
    ADD COLUMN time_slot_id BIGINT
        REFERENCES time_slots (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;

-- 4. Add total_price column
--    Denormalized from the linked venue / time slot for fast display without
--    additional joins.  Stored as NUMERIC to avoid floating-point drift.
ALTER TABLE matches
    ADD COLUMN total_price NUMERIC(10,2) NOT NULL DEFAULT 0
        CONSTRAINT chk_matches_total_price CHECK (total_price >= 0);

-- 5. Add price_per_player column
--    Auto-computed as total_price / max_players when payment_type is 'split'
--    or 'split_to_teams'; kept as 0 for 'host_pays' / 'cash_at_venue'.
ALTER TABLE matches
    ADD COLUMN price_per_player NUMERIC(10,2) NOT NULL DEFAULT 0
        CONSTRAINT chk_matches_price_per_player CHECK (price_per_player >= 0);

-- 6. Add currency column
--    ISO 4217 three-letter code.  Defaults to 'MKD' (Macedonian denar) which
--    is the primary market currency for this deployment.
ALTER TABLE matches
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'MKD';

-- 7. Index on payment_type — used when filtering lobbies/matches by payment mode
CREATE INDEX CONCURRENTLY idx_matches_payment_type
    ON matches (payment_type);

-- 8. Index on time_slot_id — supports FK lookups and JOIN to time_slots
CREATE INDEX CONCURRENTLY idx_matches_time_slot_id
    ON matches (time_slot_id);

-- ============================================================
-- DOWN
-- ============================================================

-- 8. Drop time_slot_id index
DROP INDEX IF EXISTS idx_matches_time_slot_id;

-- 7. Drop payment_type index
DROP INDEX IF EXISTS idx_matches_payment_type;

-- 6. Drop currency column
ALTER TABLE matches DROP COLUMN IF EXISTS currency;

-- 5. Drop price_per_player column (constraint is dropped automatically)
ALTER TABLE matches DROP COLUMN IF EXISTS price_per_player;

-- 4. Drop total_price column (constraint is dropped automatically)
ALTER TABLE matches DROP COLUMN IF EXISTS total_price;

-- 3. Drop time_slot_id column (FK constraint is dropped automatically)
ALTER TABLE matches DROP COLUMN IF EXISTS time_slot_id;

-- 2. Drop payment_type column
ALTER TABLE matches DROP COLUMN IF EXISTS payment_type;

-- 1. Drop the enum type
--    Must come after the column that depends on it is dropped.
DROP TYPE IF EXISTS match_payment_type;

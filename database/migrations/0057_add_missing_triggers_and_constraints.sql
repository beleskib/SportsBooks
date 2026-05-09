-- Migration: 0057_add_missing_triggers_and_constraints.sql
-- Description: Backfill missing updated_at triggers, add unique active-subscription
--   constraint, add coaches is_active index, enforce uniqueness on users.email,
--   and migrate communities/community_lobbies sport_type to the sport_type ENUM.
-- Created: 2026-05-08

-- ============================================================
-- UP
-- ============================================================

-- ------------------------------------------------------------
-- 1. Missing updated_at triggers
--    The update_updated_at_column() function was created in
--    0016_create_updated_at_trigger.sql.  The tables below all
--    have an updated_at column but were created without the
--    corresponding BEFORE UPDATE trigger.
-- ------------------------------------------------------------

CREATE TRIGGER trg_match_recurrence_rules_updated_at
    BEFORE UPDATE ON match_recurrence_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_match_participants_updated_at
    BEFORE UPDATE ON match_participants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_user_favorites_updated_at
    BEFORE UPDATE ON user_favorites
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_friendships_updated_at
    BEFORE UPDATE ON friendships
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_parties_updated_at
    BEFORE UPDATE ON parties
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_party_members_updated_at
    BEFORE UPDATE ON party_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_device_tokens_updated_at
    BEFORE UPDATE ON device_tokens
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_available_players_updated_at
    BEFORE UPDATE ON available_players
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ------------------------------------------------------------
-- 2. Unique active-subscription constraint
--    Prevents a user from accumulating more than one row with
--    status 'active' or 'trialing' in the subscriptions table.
--    A partial unique index is the correct mechanism: canceled
--    and past_due rows are not covered, so historical records
--    are preserved without violating the constraint.
-- ------------------------------------------------------------

CREATE UNIQUE INDEX CONCURRENTLY idx_subscriptions_one_active_per_user
    ON subscriptions (user_id)
    WHERE status IN ('active', 'trialing');

-- ------------------------------------------------------------
-- 3. Missing coaches is_active index
--    The venues table has idx_venues_is_active; coaches lacks
--    an equivalent, causing full-table scans when filtering for
--    active coaches in browse/search queries.
-- ------------------------------------------------------------

CREATE INDEX CONCURRENTLY idx_coaches_is_active ON coaches (is_active);

-- ------------------------------------------------------------
-- 4. Unique constraint on users.email
--    The original idx_users_email (0002) is a plain index that
--    allows duplicate e-mail addresses — a correctness gap.
--    We drop it and replace it with a unique index so the
--    database enforces the invariant independently of the
--    application layer.
-- ------------------------------------------------------------

DROP INDEX IF EXISTS idx_users_email;

CREATE UNIQUE INDEX CONCURRENTLY idx_users_email_unique ON users (email);

-- ------------------------------------------------------------
-- 5. Migrate communities.sport_type and
--    community_lobbies.sport_type to the sport_type ENUM
--    Both columns were created as VARCHAR(50); casting them to
--    the existing sport_type ENUM enforces domain validity at
--    the database level and aligns them with every other table
--    that carries this column.
-- ------------------------------------------------------------

ALTER TABLE communities
    ALTER COLUMN sport_type TYPE sport_type
    USING sport_type::sport_type;

ALTER TABLE community_lobbies
    ALTER COLUMN sport_type TYPE sport_type
    USING sport_type::sport_type;

-- ============================================================
-- DOWN
-- ============================================================

-- 5. Revert sport_type columns to VARCHAR(50)
ALTER TABLE community_lobbies
    ALTER COLUMN sport_type TYPE VARCHAR(50)
    USING sport_type::TEXT;

ALTER TABLE communities
    ALTER COLUMN sport_type TYPE VARCHAR(50)
    USING sport_type::TEXT;

-- 4. Restore the original non-unique email index
DROP INDEX IF EXISTS idx_users_email_unique;

CREATE INDEX CONCURRENTLY idx_users_email ON users (email);

-- 3. Drop coaches is_active index
DROP INDEX IF EXISTS idx_coaches_is_active;

-- 2. Drop unique active-subscription constraint
DROP INDEX IF EXISTS idx_subscriptions_one_active_per_user;

-- 1. Drop updated_at triggers (reverse order of creation)
DROP TRIGGER IF EXISTS trg_available_players_updated_at   ON available_players;
DROP TRIGGER IF EXISTS trg_device_tokens_updated_at       ON device_tokens;
DROP TRIGGER IF EXISTS trg_party_members_updated_at       ON party_members;
DROP TRIGGER IF EXISTS trg_parties_updated_at             ON parties;
DROP TRIGGER IF EXISTS trg_friendships_updated_at         ON friendships;
DROP TRIGGER IF EXISTS trg_user_favorites_updated_at      ON user_favorites;
DROP TRIGGER IF EXISTS trg_notifications_updated_at       ON notifications;
DROP TRIGGER IF EXISTS trg_match_participants_updated_at  ON match_participants;
DROP TRIGGER IF EXISTS trg_match_recurrence_rules_updated_at ON match_recurrence_rules;

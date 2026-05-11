-- Migration: 0059_add_missing_indexes_triggers_constraints.sql
-- Description: Add 16 missing FK indexes, 7 missing updated_at triggers, and
--   a CHECK constraint on bookings ensuring exactly one of venue_id/coach_id
--   is set.  Nine of the 16 originally-identified updated_at triggers were
--   already backfilled in 0057; only the remaining seven are created here.
-- Created: 2026-05-11
--
-- NOTE ON CONCURRENTLY:
--   CREATE INDEX CONCURRENTLY cannot run inside an explicit transaction block.
--   When applying this migration with a transaction-wrapping runner, replace
--   each CONCURRENTLY keyword with a plain CREATE INDEX (or run this file
--   outside a transaction).  All other statements (triggers, ALTER TABLE) are
--   transactional and safe to wrap.

-- ============================================================
-- UP
-- ============================================================

-- ------------------------------------------------------------
-- 1. Missing FK indexes (16 total)
--    Each foreign key column below has no supporting index,
--    causing sequential scans on every JOIN that references it.
-- ------------------------------------------------------------

-- booking_participants.split_payment_id
CREATE INDEX CONCURRENTLY idx_booking_participants_split_payment_id
    ON booking_participants (split_payment_id);

-- coaches.approval_decided_by_user_id
CREATE INDEX CONCURRENTLY idx_coaches_approval_decided_by_user_id
    ON coaches (approval_decided_by_user_id);

-- community_lobbies.created_by
CREATE INDEX CONCURRENTLY idx_community_lobbies_created_by
    ON community_lobbies (created_by);

-- community_lobbies.venue_id
CREATE INDEX CONCURRENTLY idx_community_lobbies_venue_id
    ON community_lobbies (venue_id);

-- community_members.invited_by
CREATE INDEX CONCURRENTLY idx_community_members_invited_by
    ON community_members (invited_by);

-- feed_post_comments.user_id
CREATE INDEX CONCURRENTLY idx_feed_post_comments_user_id
    ON feed_post_comments (user_id);

-- lobby_messages.user_id
CREATE INDEX CONCURRENTLY idx_lobby_messages_user_id
    ON lobby_messages (user_id);

-- match_recurrence_rules.venue_id
CREATE INDEX CONCURRENTLY idx_match_recurrence_rules_venue_id
    ON match_recurrence_rules (venue_id);

-- matches.booking_id
CREATE INDEX CONCURRENTLY idx_matches_booking_id
    ON matches (booking_id);

-- matches.parent_match_id
CREATE INDEX CONCURRENTLY idx_matches_parent_match_id
    ON matches (parent_match_id);

-- matches.recurrence_rule_id
CREATE INDEX CONCURRENTLY idx_matches_recurrence_rule_id
    ON matches (recurrence_rule_id);

-- parties.match_id
CREATE INDEX CONCURRENTLY idx_parties_match_id
    ON parties (match_id);

-- venue_booking_lobbies.booking_id
CREATE INDEX CONCURRENTLY idx_venue_booking_lobbies_booking_id
    ON venue_booking_lobbies (booking_id);

-- venue_booking_lobby_members.payment_id
CREATE INDEX CONCURRENTLY idx_venue_booking_lobby_members_payment_id
    ON venue_booking_lobby_members (payment_id);

-- venue_booking_lobby_teams.leader_id
CREATE INDEX CONCURRENTLY idx_venue_booking_lobby_teams_leader_id
    ON venue_booking_lobby_teams (leader_id);

-- venues.approval_decided_by_user_id
CREATE INDEX CONCURRENTLY idx_venues_approval_decided_by_user_id
    ON venues (approval_decided_by_user_id);

-- ------------------------------------------------------------
-- 2. Missing updated_at triggers (7 remaining)
--    Nine of the 16 originally-identified tables were covered
--    in migration 0057.  The seven below are the remainder.
--    All reference the existing update_updated_at_column()
--    function created in 0016_create_updated_at_trigger.sql.
-- ------------------------------------------------------------

CREATE TRIGGER trg_booking_participants_updated_at
    BEFORE UPDATE ON booking_participants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_match_chat_messages_updated_at
    BEFORE UPDATE ON match_chat_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_matches_updated_at
    BEFORE UPDATE ON matches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_player_ratings_updated_at
    BEFORE UPDATE ON player_ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_split_payments_updated_at
    BEFORE UPDATE ON split_payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_subscriptions_updated_at
    BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_user_sport_expertise_updated_at
    BEFORE UPDATE ON user_sport_expertise
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ------------------------------------------------------------
-- 3. CHECK constraint on bookings
--    Enforces that exactly one of venue_id / coach_id is set,
--    preventing orphaned bookings that reference neither or both.
-- ------------------------------------------------------------

ALTER TABLE bookings ADD CONSTRAINT chk_bookings_owner
    CHECK (
        (venue_id IS NOT NULL AND coach_id IS NULL) OR
        (venue_id IS NULL  AND coach_id IS NOT NULL)
    );

-- ============================================================
-- DOWN
-- ============================================================

-- 3. Drop bookings owner constraint
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS chk_bookings_owner;

-- 2. Drop updated_at triggers (reverse order of creation)
DROP TRIGGER IF EXISTS trg_user_sport_expertise_updated_at ON user_sport_expertise;
DROP TRIGGER IF EXISTS trg_subscriptions_updated_at        ON subscriptions;
DROP TRIGGER IF EXISTS trg_split_payments_updated_at       ON split_payments;
DROP TRIGGER IF EXISTS trg_player_ratings_updated_at       ON player_ratings;
DROP TRIGGER IF EXISTS trg_matches_updated_at              ON matches;
DROP TRIGGER IF EXISTS trg_match_chat_messages_updated_at  ON match_chat_messages;
DROP TRIGGER IF EXISTS trg_booking_participants_updated_at ON booking_participants;

-- 1. Drop FK indexes (reverse order of creation)
DROP INDEX IF EXISTS idx_venues_approval_decided_by_user_id;
DROP INDEX IF EXISTS idx_venue_booking_lobby_teams_leader_id;
DROP INDEX IF EXISTS idx_venue_booking_lobby_members_payment_id;
DROP INDEX IF EXISTS idx_venue_booking_lobbies_booking_id;
DROP INDEX IF EXISTS idx_parties_match_id;
DROP INDEX IF EXISTS idx_matches_recurrence_rule_id;
DROP INDEX IF EXISTS idx_matches_parent_match_id;
DROP INDEX IF EXISTS idx_matches_booking_id;
DROP INDEX IF EXISTS idx_match_recurrence_rules_venue_id;
DROP INDEX IF EXISTS idx_lobby_messages_user_id;
DROP INDEX IF EXISTS idx_feed_post_comments_user_id;
DROP INDEX IF EXISTS idx_community_members_invited_by;
DROP INDEX IF EXISTS idx_community_lobbies_venue_id;
DROP INDEX IF EXISTS idx_community_lobbies_created_by;
DROP INDEX IF EXISTS idx_coaches_approval_decided_by_user_id;
DROP INDEX IF EXISTS idx_booking_participants_split_payment_id;

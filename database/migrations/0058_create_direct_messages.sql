-- Migration: 0058_create_direct_messages.sql
-- Description: Create direct_messages table for 1:1 chat between friends
-- Created: 2026-05-08

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE direct_messages (
    id          BIGSERIAL       PRIMARY KEY,
    sender_id   BIGINT          NOT NULL
                    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    receiver_id BIGINT          NOT NULL
                    REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    message     TEXT            NOT NULL,
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_dm_no_self CHECK (sender_id != receiver_id)
);

-- Composite index for conversation lookups (canonicalize pair with LEAST/GREATEST)
CREATE INDEX CONCURRENTLY idx_dm_conversation
    ON direct_messages (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), created_at DESC);

-- Index for unread-message queries
CREATE INDEX CONCURRENTLY idx_dm_receiver_unread
    ON direct_messages (receiver_id, read_at)
    WHERE read_at IS NULL;

-- Index for "inbox" — latest message per conversation
CREATE INDEX CONCURRENTLY idx_dm_sender_created
    ON direct_messages (sender_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_dm_receiver_created
    ON direct_messages (receiver_id, created_at DESC);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_dm_receiver_created;
DROP INDEX IF EXISTS idx_dm_sender_created;
DROP INDEX IF EXISTS idx_dm_receiver_unread;
DROP INDEX IF EXISTS idx_dm_conversation;
DROP TABLE IF EXISTS direct_messages;

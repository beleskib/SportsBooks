-- Migration: 0026_create_match_chat_messages.sql
-- Description: Create match chat messages table for in-match group chat
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE match_chat_messages (
    id              BIGSERIAL       PRIMARY KEY,
    match_id        BIGINT          NOT NULL
                        REFERENCES matches (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    sender_id       BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    content         TEXT            NOT NULL
                        CHECK (char_length(content) >= 1 AND char_length(content) <= 2000),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_chat_match_created
    ON match_chat_messages (match_id, created_at DESC);
CREATE INDEX CONCURRENTLY idx_chat_sender_id
    ON match_chat_messages (sender_id);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_chat_sender_id;
DROP INDEX IF EXISTS idx_chat_match_created;
DROP TABLE IF EXISTS match_chat_messages;

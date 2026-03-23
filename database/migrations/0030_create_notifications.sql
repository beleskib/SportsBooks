-- Migration: 0030_create_notifications.sql
-- Description: Create notification_type enum and notifications table
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TYPE notification_type AS ENUM (
    'match_join_request',
    'match_join_approved',
    'match_join_declined',
    'match_chat_message',
    'match_starting_soon',
    'match_cancelled',
    'booking_confirmed',
    'booking_cancelled',
    'booking_reminder',
    'rating_received',
    'general'
);

CREATE TABLE notifications (
    id              BIGSERIAL           PRIMARY KEY,
    user_id         BIGINT              NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    type            notification_type   NOT NULL,
    title           VARCHAR(255)        NOT NULL,
    body            TEXT                NOT NULL,
    data            JSONB               NOT NULL DEFAULT '{}',
    is_read         BOOLEAN             NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX CONCURRENTLY idx_notifications_user_id ON notifications (user_id);
CREATE INDEX CONCURRENTLY idx_notifications_user_unread ON notifications (user_id, is_read) WHERE is_read = false;
CREATE INDEX CONCURRENTLY idx_notifications_created_at ON notifications (user_id, created_at DESC);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_notifications_created_at;
DROP INDEX IF EXISTS idx_notifications_user_unread;
DROP INDEX IF EXISTS idx_notifications_user_id;
DROP TABLE IF EXISTS notifications;
DROP TYPE IF EXISTS notification_type;

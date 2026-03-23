-- Migration: 0029_create_device_tokens.sql
-- Description: Create device_tokens table for FCM push notification tokens
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE device_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    fcm_token       TEXT            NOT NULL,
    device_type     VARCHAR(20)     NOT NULL DEFAULT 'android',
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    last_used_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX CONCURRENTLY idx_device_tokens_fcm_token ON device_tokens (fcm_token);
CREATE INDEX CONCURRENTLY idx_device_tokens_user_id ON device_tokens (user_id);
CREATE INDEX CONCURRENTLY idx_device_tokens_active ON device_tokens (user_id, is_active) WHERE is_active = true;

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_device_tokens_active;
DROP INDEX IF EXISTS idx_device_tokens_user_id;
DROP INDEX IF EXISTS idx_device_tokens_fcm_token;
DROP TABLE IF EXISTS device_tokens;

-- Migration: 0002_create_users.sql
-- Description: Create users table synced with Firebase Auth
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    firebase_uid    VARCHAR(128)    NOT NULL UNIQUE,
    email           VARCHAR(255)    NOT NULL,
    display_name    VARCHAR(255),
    photo_url       TEXT,
    phone_number    VARCHAR(20),
    role            user_role       NOT NULL DEFAULT 'player',
    partner_type    partner_type,
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX CONCURRENTLY idx_users_firebase_uid ON users (firebase_uid);
CREATE INDEX CONCURRENTLY idx_users_email ON users (email);
CREATE INDEX CONCURRENTLY idx_users_role ON users (role);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_users_role;
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_firebase_uid;
DROP TABLE IF EXISTS users;

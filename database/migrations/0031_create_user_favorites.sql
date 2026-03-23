-- Migration: 0031_create_user_favorites.sql
-- Description: Create user_favorites table and favorite_entity_type enum
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TYPE favorite_entity_type AS ENUM ('venue', 'coach', 'match');

CREATE TABLE user_favorites (
    id              BIGSERIAL               PRIMARY KEY,
    user_id         BIGINT                  NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    entity_type     favorite_entity_type    NOT NULL,
    entity_id       BIGINT                  NOT NULL,
    created_at      TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_favorite UNIQUE (user_id, entity_type, entity_id)
);

CREATE INDEX CONCURRENTLY idx_user_favorites_user_id ON user_favorites (user_id);
CREATE INDEX CONCURRENTLY idx_user_favorites_entity ON user_favorites (entity_type, entity_id);
CREATE INDEX CONCURRENTLY idx_user_favorites_user_entity ON user_favorites (user_id, entity_type);

-- ============================================================
-- DOWN
-- ============================================================

DROP INDEX IF EXISTS idx_user_favorites_user_entity;
DROP INDEX IF EXISTS idx_user_favorites_entity;
DROP INDEX IF EXISTS idx_user_favorites_user_id;
DROP TABLE IF EXISTS user_favorites;
DROP TYPE IF EXISTS favorite_entity_type;

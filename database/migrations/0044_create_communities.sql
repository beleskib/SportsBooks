-- Migration: 0044_create_communities.sql
-- Description: Create communities and community_members tables for the community system
-- Created: 2026-03-25

-- ============================================================
-- UP
-- ============================================================

-- Stores user-created communities grouped by sport type.
-- invite_policy controls who can discover and join the community:
--   'friends_only'        — only direct friends of existing members
--   'friends_of_friends'  — second-degree connections
--   'open'                — any authenticated user
CREATE TABLE communities (
    id              BIGSERIAL       PRIMARY KEY,
    owner_id        BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    sport_type      VARCHAR(50),
    image_url       TEXT,
    max_members     INT             NOT NULL DEFAULT 50,
    is_public       BOOLEAN         NOT NULL DEFAULT false,
    invite_policy   VARCHAR(20)     NOT NULL DEFAULT 'friends_only',
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_communities_invite_policy CHECK (
        invite_policy IN ('friends_only', 'friends_of_friends', 'open')
    ),
    CONSTRAINT chk_communities_max_members CHECK (max_members > 0)
);

CREATE INDEX CONCURRENTLY idx_communities_owner_id
    ON communities (owner_id);

CREATE INDEX CONCURRENTLY idx_communities_sport_type
    ON communities (sport_type);

-- Partial index for fast retrieval of active, publicly listed communities.
CREATE INDEX CONCURRENTLY idx_communities_active_public
    ON communities (sport_type, created_at DESC)
    WHERE is_active = true AND is_public = true;

-- ----------------------------------------------------------------
-- Tracks every member of a community, their role, and their
-- membership status.  invited_by is nullable because the owner's
-- own row is inserted without an invitation reference.
-- ----------------------------------------------------------------
CREATE TABLE community_members (
    id              BIGSERIAL       PRIMARY KEY,
    community_id    BIGINT          NOT NULL
                        REFERENCES communities (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    user_id         BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    role            VARCHAR(20)     NOT NULL DEFAULT 'member',
    status          VARCHAR(20)     NOT NULL DEFAULT 'pending',
    invited_by      BIGINT
                        REFERENCES users (id)
                        ON DELETE SET NULL
                        ON UPDATE CASCADE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_community_members_community_user UNIQUE (community_id, user_id),
    CONSTRAINT chk_community_members_role CHECK (
        role IN ('owner', 'admin', 'member')
    ),
    CONSTRAINT chk_community_members_status CHECK (
        status IN ('pending', 'approved', 'declined')
    )
);

CREATE INDEX CONCURRENTLY idx_community_members_community_id
    ON community_members (community_id);

CREATE INDEX CONCURRENTLY idx_community_members_user_id
    ON community_members (user_id);

-- Partial index for fast pending-invitation lookups per user.
CREATE INDEX CONCURRENTLY idx_community_members_status_pending
    ON community_members (user_id)
    WHERE status = 'pending';

-- ----------------------------------------------------------------
-- updated_at auto-maintenance triggers (function defined in 0016)
-- ----------------------------------------------------------------
CREATE TRIGGER trg_communities_updated_at
    BEFORE UPDATE ON communities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_community_members_updated_at
    BEFORE UPDATE ON community_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_community_members_updated_at ON community_members;
DROP TRIGGER IF EXISTS trg_communities_updated_at        ON communities;

DROP INDEX IF EXISTS idx_community_members_status_pending;
DROP INDEX IF EXISTS idx_community_members_user_id;
DROP INDEX IF EXISTS idx_community_members_community_id;
DROP TABLE IF EXISTS community_members;

DROP INDEX IF EXISTS idx_communities_active_public;
DROP INDEX IF EXISTS idx_communities_sport_type;
DROP INDEX IF EXISTS idx_communities_owner_id;
DROP TABLE IF EXISTS communities;

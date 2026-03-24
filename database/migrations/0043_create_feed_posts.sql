-- Migration: 0043_create_feed_posts.sql
-- Description: Create social feed tables — feed_posts, feed_post_likes, feed_post_comments
-- Created: 2026-03-24

-- ============================================================
-- UP
-- ============================================================

-- Stores every post that appears in the social feed.
-- post_type is constrained to the known activity kinds; metadata
-- holds flexible payload (achievement name, match score, venue name, etc.).
CREATE TABLE feed_posts (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE,
    post_type       VARCHAR(30)     NOT NULL,
    content         TEXT,
    image_url       TEXT,
    metadata        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    likes_count     INT             NOT NULL DEFAULT 0,
    comments_count  INT             NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_feed_posts_post_type CHECK (
        post_type IN (
            'text',
            'milestone',
            'achievement',
            'booking_completed',
            'match_result',
            'photo'
        )
    ),
    CONSTRAINT chk_feed_posts_likes_count    CHECK (likes_count >= 0),
    CONSTRAINT chk_feed_posts_comments_count CHECK (comments_count >= 0)
);

CREATE INDEX idx_feed_posts_user_id
    ON feed_posts (user_id);

-- DESC ordering matches the default "newest first" feed query pattern.
CREATE INDEX idx_feed_posts_created_at
    ON feed_posts (created_at DESC);

-- ----------------------------------------------------------------
-- Records which users have liked a post.
-- The unique constraint prevents duplicate likes from the same user.
-- ----------------------------------------------------------------
CREATE TABLE feed_post_likes (
    id          BIGSERIAL   PRIMARY KEY,
    post_id     BIGINT      NOT NULL
                    REFERENCES feed_posts (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    user_id     BIGINT      NOT NULL
                    REFERENCES users (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_feed_post_likes_post_user UNIQUE (post_id, user_id)
);

CREATE INDEX idx_feed_post_likes_post_id
    ON feed_post_likes (post_id);

CREATE INDEX idx_feed_post_likes_user_id
    ON feed_post_likes (user_id);

-- ----------------------------------------------------------------
-- Stores comments left on a post.
-- ----------------------------------------------------------------
CREATE TABLE feed_post_comments (
    id          BIGSERIAL   PRIMARY KEY,
    post_id     BIGINT      NOT NULL
                    REFERENCES feed_posts (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    user_id     BIGINT      NOT NULL
                    REFERENCES users (id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE,
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_feed_post_comments_post_id
    ON feed_post_comments (post_id);

-- ----------------------------------------------------------------
-- updated_at auto-maintenance triggers (function defined in 0016)
-- ----------------------------------------------------------------
CREATE TRIGGER trg_feed_posts_updated_at
    BEFORE UPDATE ON feed_posts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_feed_post_comments_updated_at
    BEFORE UPDATE ON feed_post_comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_feed_post_comments_updated_at ON feed_post_comments;
DROP TRIGGER IF EXISTS trg_feed_posts_updated_at         ON feed_posts;

DROP INDEX IF EXISTS idx_feed_post_comments_post_id;
DROP TABLE IF EXISTS feed_post_comments;

DROP INDEX IF EXISTS idx_feed_post_likes_user_id;
DROP INDEX IF EXISTS idx_feed_post_likes_post_id;
DROP TABLE IF EXISTS feed_post_likes;

DROP INDEX IF EXISTS idx_feed_posts_created_at;
DROP INDEX IF EXISTS idx_feed_posts_user_id;
DROP TABLE IF EXISTS feed_posts;

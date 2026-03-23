-- UP

CREATE TYPE friendship_status AS ENUM ('pending', 'accepted', 'declined', 'blocked');

CREATE TABLE friendships (
    id              BIGSERIAL           PRIMARY KEY,
    requester_id    BIGINT              NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
    addressee_id    BIGINT              NOT NULL
                        REFERENCES users (id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
    status          friendship_status   NOT NULL DEFAULT 'pending',
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_friendship UNIQUE (requester_id, addressee_id),
    CONSTRAINT chk_no_self_friend CHECK (requester_id != addressee_id)
);

CREATE INDEX CONCURRENTLY idx_friendships_requester ON friendships (requester_id);
CREATE INDEX CONCURRENTLY idx_friendships_addressee ON friendships (addressee_id);
CREATE INDEX CONCURRENTLY idx_friendships_accepted
    ON friendships (requester_id, addressee_id)
    WHERE status = 'accepted';
CREATE INDEX CONCURRENTLY idx_friendships_pending
    ON friendships (addressee_id)
    WHERE status = 'pending';

-- DOWN

DROP INDEX IF EXISTS idx_friendships_pending;
DROP INDEX IF EXISTS idx_friendships_accepted;
DROP INDEX IF EXISTS idx_friendships_addressee;
DROP INDEX IF EXISTS idx_friendships_requester;
DROP TABLE IF EXISTS friendships;
DROP TYPE IF EXISTS friendship_status;

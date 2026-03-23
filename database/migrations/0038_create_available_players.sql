-- UP
CREATE TABLE available_players (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    sport_type sport_type NOT NULL,
    skill_level INTEGER CHECK (skill_level BETWEEN 1 AND 5),
    note TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    available_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_available_players_user_sport ON available_players(user_id, sport_type);
CREATE INDEX CONCURRENTLY idx_available_players_sport ON available_players(sport_type);

-- DOWN
DROP TABLE IF EXISTS available_players;

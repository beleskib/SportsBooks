-- Migration: 0049_add_skill_filters_to_lobbies.sql
-- Description: Add skill-level range filters and strictness flag to matchmaking lobbies
-- Part of v2-practical-ux: lets hosts restrict lobbies by skill range + reliability.

-- UP
ALTER TABLE community_lobbies
    ADD COLUMN skill_level_min INTEGER CHECK (skill_level_min BETWEEN 1 AND 5),
    ADD COLUMN skill_level_max INTEGER CHECK (skill_level_max BETWEEN 1 AND 5),
    ADD COLUMN skill_strict    BOOLEAN NOT NULL DEFAULT false,
    ADD CONSTRAINT chk_community_lobbies_skill_range
        CHECK (skill_level_min IS NULL OR skill_level_max IS NULL OR skill_level_min <= skill_level_max);

ALTER TABLE venue_booking_lobbies
    ADD COLUMN skill_level_min INTEGER CHECK (skill_level_min BETWEEN 1 AND 5),
    ADD COLUMN skill_level_max INTEGER CHECK (skill_level_max BETWEEN 1 AND 5),
    ADD COLUMN skill_strict    BOOLEAN NOT NULL DEFAULT false,
    ADD CONSTRAINT chk_venue_booking_lobbies_skill_range
        CHECK (skill_level_min IS NULL OR skill_level_max IS NULL OR skill_level_min <= skill_level_max);

ALTER TABLE matches
    ADD COLUMN skill_strict    BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN min_reliability NUMERIC(3,2) DEFAULT 0.90 CHECK (min_reliability BETWEEN 0 AND 1);

CREATE INDEX CONCURRENTLY idx_community_lobbies_skill_range     ON community_lobbies (skill_level_min, skill_level_max);
CREATE INDEX CONCURRENTLY idx_venue_booking_lobbies_skill_range ON venue_booking_lobbies (skill_level_min, skill_level_max);

-- DOWN
DROP INDEX IF EXISTS idx_venue_booking_lobbies_skill_range;
DROP INDEX IF EXISTS idx_community_lobbies_skill_range;

ALTER TABLE matches
    DROP COLUMN IF EXISTS min_reliability,
    DROP COLUMN IF EXISTS skill_strict;

ALTER TABLE venue_booking_lobbies
    DROP CONSTRAINT IF EXISTS chk_venue_booking_lobbies_skill_range,
    DROP COLUMN IF EXISTS skill_strict,
    DROP COLUMN IF EXISTS skill_level_max,
    DROP COLUMN IF EXISTS skill_level_min;

ALTER TABLE community_lobbies
    DROP CONSTRAINT IF EXISTS chk_community_lobbies_skill_range,
    DROP COLUMN IF EXISTS skill_strict,
    DROP COLUMN IF EXISTS skill_level_max,
    DROP COLUMN IF EXISTS skill_level_min;

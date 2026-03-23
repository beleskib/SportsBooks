-- Migration: 0022_create_match_enums.sql
-- Description: Create ENUM types for the matchmaking system
-- Created: 2026-03-12

-- ============================================================
-- UP
-- ============================================================

CREATE TYPE match_status AS ENUM (
    'draft', 'open', 'full', 'in_progress', 'completed', 'cancelled'
);

CREATE TYPE match_type AS ENUM (
    'venue_linked', 'standalone'
);

CREATE TYPE match_visibility AS ENUM (
    'public', 'private'
);

CREATE TYPE participant_status AS ENUM (
    'pending', 'approved', 'declined', 'left'
);

CREATE TYPE participant_role AS ENUM (
    'host', 'player'
);

CREATE TYPE recurrence_frequency AS ENUM (
    'weekly', 'biweekly', 'monthly'
);

-- ============================================================
-- DOWN
-- ============================================================

DROP TYPE IF EXISTS recurrence_frequency;
DROP TYPE IF EXISTS participant_role;
DROP TYPE IF EXISTS participant_status;
DROP TYPE IF EXISTS match_visibility;
DROP TYPE IF EXISTS match_type;
DROP TYPE IF EXISTS match_status;

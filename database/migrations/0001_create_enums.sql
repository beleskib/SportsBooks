-- Migration: 0001_create_enums.sql
-- Description: Create all ENUM types used across the database
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

CREATE TYPE user_role AS ENUM ('player', 'partner', 'admin');

CREATE TYPE partner_type AS ENUM ('coach', 'venue_owner');

CREATE TYPE booking_status AS ENUM ('pending', 'confirmed', 'cancelled', 'completed', 'no_show');

CREATE TYPE payment_status AS ENUM ('pending', 'completed', 'failed', 'refunded');

CREATE TYPE sport_type AS ENUM (
    'basketball', 'football', 'tennis', 'paddle', 'volleyball',
    'swimming', 'boxing', 'mma', 'yoga', 'pilates',
    'crossfit', 'running', 'cycling', 'golf', 'badminton',
    'table_tennis', 'handball', 'baseball', 'cricket'
);

CREATE TYPE day_of_week AS ENUM (
    'monday', 'tuesday', 'wednesday', 'thursday',
    'friday', 'saturday', 'sunday'
);

-- ============================================================
-- DOWN
-- ============================================================

DROP TYPE IF EXISTS day_of_week;
DROP TYPE IF EXISTS sport_type;
DROP TYPE IF EXISTS payment_status;
DROP TYPE IF EXISTS booking_status;
DROP TYPE IF EXISTS partner_type;
DROP TYPE IF EXISTS user_role;

-- Migration: 0033_create_party_enums.sql
-- Description: Create enum types for party/squad system
-- Created: 2026-03-13

-- ============================================================
-- UP
-- ============================================================

CREATE TYPE party_status AS ENUM ('forming', 'ready', 'in_match', 'disbanded');
CREATE TYPE party_member_status AS ENUM ('invited', 'accepted', 'declined');

-- ============================================================
-- DOWN
-- ============================================================

DROP TYPE IF EXISTS party_member_status;
DROP TYPE IF EXISTS party_status;

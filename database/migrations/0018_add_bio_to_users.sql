-- Migration: 0018_add_bio_to_users.sql
-- Description: Add bio text field to users table for player profiles
-- Created: 2026-03-04

-- ============================================================
-- UP
-- ============================================================

ALTER TABLE users ADD COLUMN bio TEXT;

-- ============================================================
-- DOWN
-- ============================================================

ALTER TABLE users DROP COLUMN IF EXISTS bio;

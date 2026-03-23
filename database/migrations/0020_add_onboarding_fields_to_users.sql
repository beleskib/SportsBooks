-- Migration: 0020_add_onboarding_fields_to_users.sql
-- Description: Add date_of_birth and onboarding_completed fields to users table
-- Created: 2026-03-04

-- ============================================================
-- UP
-- ============================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT false;

-- Mark existing players as having completed onboarding
UPDATE users SET onboarding_completed = true WHERE role = 'player';

-- ============================================================
-- DOWN
-- ============================================================

ALTER TABLE users DROP COLUMN IF EXISTS onboarding_completed;
ALTER TABLE users DROP COLUMN IF EXISTS date_of_birth;

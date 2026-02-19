-- Migration: 0016_create_updated_at_trigger.sql
-- Description: Auto-update updated_at timestamp trigger for all tables
-- Created: 2026-02-18

-- ============================================================
-- UP
-- ============================================================

-- Create the trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to all tables that have an updated_at column

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_sports_categories_updated_at
    BEFORE UPDATE ON sports_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_venues_updated_at
    BEFORE UPDATE ON venues
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_coaches_updated_at
    BEFORE UPDATE ON coaches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_time_slots_updated_at
    BEFORE UPDATE ON time_slots
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_discounts_updated_at
    BEFORE UPDATE ON discounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_availability_templates_updated_at
    BEFORE UPDATE ON availability_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- DOWN
-- ============================================================

DROP TRIGGER IF EXISTS trg_availability_templates_updated_at ON availability_templates;
DROP TRIGGER IF EXISTS trg_reviews_updated_at ON reviews;
DROP TRIGGER IF EXISTS trg_discounts_updated_at ON discounts;
DROP TRIGGER IF EXISTS trg_payments_updated_at ON payments;
DROP TRIGGER IF EXISTS trg_bookings_updated_at ON bookings;
DROP TRIGGER IF EXISTS trg_time_slots_updated_at ON time_slots;
DROP TRIGGER IF EXISTS trg_coaches_updated_at ON coaches;
DROP TRIGGER IF EXISTS trg_venues_updated_at ON venues;
DROP TRIGGER IF EXISTS trg_sports_categories_updated_at ON sports_categories;
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;

DROP FUNCTION IF EXISTS update_updated_at_column();

-- ============================================================
-- get_available_slots.sql
-- Returns available time slots for a venue or coach within a date range
-- Usage: Pass venue_id OR coach_id (not both), plus date range
-- ============================================================

-- EXPLAIN ANALYZE (remove for production)
-- Expected: Index scan on time_slots(venue_id, slot_date) or (coach_id, slot_date)

CREATE OR REPLACE FUNCTION get_available_slots(
    p_venue_id BIGINT DEFAULT NULL,
    p_coach_id BIGINT DEFAULT NULL,
    p_date_from DATE DEFAULT CURRENT_DATE,
    p_date_to DATE DEFAULT CURRENT_DATE + INTERVAL '7 days'
)
RETURNS TABLE (
    slot_id BIGINT,
    slot_date DATE,
    start_time TIME,
    end_time TIME,
    is_available BOOLEAN,
    price_override NUMERIC(10,2),
    base_price NUMERIC(10,2)
) AS $$
BEGIN
    IF p_venue_id IS NOT NULL THEN
        RETURN QUERY
        SELECT
            ts.id AS slot_id,
            ts.slot_date,
            ts.start_time,
            ts.end_time,
            ts.is_available,
            ts.price_override,
            v.price_per_hour AS base_price
        FROM time_slots ts
        JOIN venues v ON v.id = ts.venue_id
        WHERE ts.venue_id = p_venue_id
          AND ts.slot_date BETWEEN p_date_from AND p_date_to
          AND ts.is_available = true
          AND NOT EXISTS (
              SELECT 1 FROM bookings b
              WHERE b.time_slot_id = ts.id
                AND b.status NOT IN ('cancelled')
          )
        ORDER BY ts.slot_date, ts.start_time;

    ELSIF p_coach_id IS NOT NULL THEN
        RETURN QUERY
        SELECT
            ts.id AS slot_id,
            ts.slot_date,
            ts.start_time,
            ts.end_time,
            ts.is_available,
            ts.price_override,
            c.price_per_hour AS base_price
        FROM time_slots ts
        JOIN coaches c ON c.id = ts.coach_id
        WHERE ts.coach_id = p_coach_id
          AND ts.slot_date BETWEEN p_date_from AND p_date_to
          AND ts.is_available = true
          AND NOT EXISTS (
              SELECT 1 FROM bookings b
              WHERE b.time_slot_id = ts.id
                AND b.status NOT IN ('cancelled')
          )
        ORDER BY ts.slot_date, ts.start_time;

    ELSE
        RAISE EXCEPTION 'Either p_venue_id or p_coach_id must be provided';
    END IF;
END;
$$ LANGUAGE plpgsql STABLE;

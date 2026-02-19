-- ============================================================
-- get_partner_dashboard_stats.sql
-- Returns aggregated stats for a partner's dashboard
-- Works for both venue owners and coaches
-- ============================================================

-- EXPLAIN ANALYZE (remove for production)
-- Expected: Index scans on bookings(venue_id), bookings(coach_id), payments(status)

CREATE OR REPLACE FUNCTION get_partner_dashboard_stats(
    p_user_id BIGINT
)
RETURNS TABLE (
    total_bookings BIGINT,
    confirmed_bookings BIGINT,
    pending_bookings BIGINT,
    completed_bookings BIGINT,
    cancelled_bookings BIGINT,
    upcoming_bookings BIGINT,
    total_revenue NUMERIC(10,2),
    avg_rating NUMERIC(3,2),
    total_reviews BIGINT
) AS $$
BEGIN
    RETURN QUERY
    WITH partner_bookings AS (
        -- Get all bookings for venues owned by this user
        SELECT b.id, b.status, b.total_price, b.created_at, ts.slot_date
        FROM bookings b
        JOIN time_slots ts ON ts.id = b.time_slot_id
        JOIN venues v ON v.id = b.venue_id
        WHERE v.owner_id = p_user_id

        UNION ALL

        -- Get all bookings for this user's coach profile
        SELECT b.id, b.status, b.total_price, b.created_at, ts.slot_date
        FROM bookings b
        JOIN time_slots ts ON ts.id = b.time_slot_id
        JOIN coaches c ON c.id = b.coach_id
        WHERE c.user_id = p_user_id
    ),
    partner_reviews AS (
        SELECT r.rating
        FROM reviews r
        JOIN venues v ON v.id = r.venue_id
        WHERE v.owner_id = p_user_id

        UNION ALL

        SELECT r.rating
        FROM reviews r
        JOIN coaches c ON c.id = r.coach_id
        WHERE c.user_id = p_user_id
    )
    SELECT
        COUNT(pb.id)::BIGINT AS total_bookings,
        COUNT(pb.id) FILTER (WHERE pb.status = 'confirmed')::BIGINT AS confirmed_bookings,
        COUNT(pb.id) FILTER (WHERE pb.status = 'pending')::BIGINT AS pending_bookings,
        COUNT(pb.id) FILTER (WHERE pb.status = 'completed')::BIGINT AS completed_bookings,
        COUNT(pb.id) FILTER (WHERE pb.status = 'cancelled')::BIGINT AS cancelled_bookings,
        COUNT(pb.id) FILTER (WHERE pb.slot_date >= CURRENT_DATE AND pb.status IN ('pending', 'confirmed'))::BIGINT AS upcoming_bookings,
        COALESCE(SUM(pb.total_price) FILTER (WHERE pb.status IN ('confirmed', 'completed')), 0)::NUMERIC(10,2) AS total_revenue,
        COALESCE(AVG(pr.rating), 0)::NUMERIC(3,2) AS avg_rating,
        (SELECT COUNT(*) FROM partner_reviews)::BIGINT AS total_reviews
    FROM partner_bookings pb
    CROSS JOIN (SELECT 1) AS dummy
    LEFT JOIN partner_reviews pr ON true;
END;
$$ LANGUAGE plpgsql STABLE;

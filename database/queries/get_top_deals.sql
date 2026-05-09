-- ============================================================
-- get_top_deals.sql
-- Returns venues and coaches that have active discounts
-- Used to populate the "Top Deals" section on the home screen
-- ============================================================

-- EXPLAIN ANALYZE (remove for production)
-- Expected: Index scan on discounts(is_active, valid_until)

-- Top deal venues
CREATE OR REPLACE FUNCTION get_top_deal_venues(
    p_limit INTEGER DEFAULT 10,
    p_sport_type TEXT DEFAULT NULL
)
RETURNS TABLE (
    venue_id BIGINT,
    venue_name VARCHAR(255),
    sport_type sport_type,
    price_per_hour NUMERIC(10,2),
    avg_rating NUMERIC(3,2),
    total_reviews INTEGER,
    city VARCHAR(100),
    primary_image_url TEXT,
    discount_title VARCHAR(255),
    discount_percent NUMERIC(5,2),
    discount_amount NUMERIC(10,2),
    discounted_price NUMERIC(10,2),
    valid_until TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.id AS venue_id,
        v.name AS venue_name,
        v.sport_type,
        v.price_per_hour,
        v.avg_rating,
        v.total_reviews,
        v.city,
        (SELECT vi.image_url FROM venue_images vi WHERE vi.venue_id = v.id AND vi.is_primary = true LIMIT 1) AS primary_image_url,
        d.title AS discount_title,
        d.discount_percent,
        d.discount_amount,
        CASE
            WHEN d.discount_percent IS NOT NULL THEN ROUND(v.price_per_hour * (1 - d.discount_percent / 100), 2)
            WHEN d.discount_amount IS NOT NULL THEN GREATEST(v.price_per_hour - d.discount_amount, 0)
        END AS discounted_price,
        d.valid_until
    FROM venues v
    INNER JOIN discounts d ON d.venue_id = v.id
    WHERE v.is_active = true
      AND d.is_active = true
      AND NOW() BETWEEN d.valid_from AND d.valid_until
      AND (p_sport_type IS NULL OR v.sport_type::TEXT = p_sport_type)
    ORDER BY
        CASE
            WHEN d.discount_percent IS NOT NULL THEN d.discount_percent
            WHEN d.discount_amount IS NOT NULL THEN (d.discount_amount / v.price_per_hour * 100)
            ELSE 0
        END DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql STABLE;

-- Top deal coaches
CREATE OR REPLACE FUNCTION get_top_deal_coaches(
    p_limit INTEGER DEFAULT 10,
    p_sport_type TEXT DEFAULT NULL
)
RETURNS TABLE (
    coach_id BIGINT,
    coach_name VARCHAR(255),
    sport_type sport_type,
    specialization VARCHAR(255),
    price_per_hour NUMERIC(10,2),
    avg_rating NUMERIC(3,2),
    total_reviews INTEGER,
    city VARCHAR(100),
    primary_image_url TEXT,
    discount_title VARCHAR(255),
    discount_percent NUMERIC(5,2),
    discount_amount NUMERIC(10,2),
    discounted_price NUMERIC(10,2),
    valid_until TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.id AS coach_id,
        c.name AS coach_name,
        c.sport_type,
        c.specialization,
        c.price_per_hour,
        c.avg_rating,
        c.total_reviews,
        c.city,
        (SELECT ci.image_url FROM coach_images ci WHERE ci.coach_id = c.id AND ci.is_primary = true LIMIT 1) AS primary_image_url,
        d.title AS discount_title,
        d.discount_percent,
        d.discount_amount,
        CASE
            WHEN d.discount_percent IS NOT NULL THEN ROUND(c.price_per_hour * (1 - d.discount_percent / 100), 2)
            WHEN d.discount_amount IS NOT NULL THEN GREATEST(c.price_per_hour - d.discount_amount, 0)
        END AS discounted_price,
        d.valid_until
    FROM coaches c
    INNER JOIN discounts d ON d.coach_id = c.id
    WHERE c.is_active = true
      AND d.is_active = true
      AND NOW() BETWEEN d.valid_from AND d.valid_until
      AND (p_sport_type IS NULL OR c.sport_type::TEXT = p_sport_type)
    ORDER BY
        CASE
            WHEN d.discount_percent IS NOT NULL THEN d.discount_percent
            WHEN d.discount_amount IS NOT NULL THEN (d.discount_amount / c.price_per_hour * 100)
            ELSE 0
        END DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql STABLE;

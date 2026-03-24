-- ============================================================
-- create_booking.sql
-- Atomically creates a booking with double-booking prevention
-- Uses SELECT FOR UPDATE to lock the time slot row
-- ============================================================

-- EXPLAIN ANALYZE (remove for production)
-- Expected: Index scan on time_slots(id), partial unique index on bookings prevents races

CREATE OR REPLACE FUNCTION create_booking(
    p_player_id BIGINT,
    p_time_slot_id BIGINT,
    p_notes TEXT DEFAULT NULL
)
RETURNS TABLE (
    booking_id BIGINT,
    time_slot_id BIGINT,
    venue_id BIGINT,
    coach_id BIGINT,
    status TEXT,
    total_price NUMERIC(10,2),
    expires_at TIMESTAMPTZ
) AS $$
DECLARE
    v_slot RECORD;
    v_booking_id BIGINT;
    v_price NUMERIC(10,2);
BEGIN
    -- Lock the time slot row to prevent concurrent bookings
    SELECT
        ts.id,
        ts.venue_id,
        ts.coach_id,
        ts.is_available,
        ts.price_override,
        COALESCE(ts.price_override,
            CASE
                WHEN ts.venue_id IS NOT NULL THEN (SELECT v.price_per_hour FROM venues v WHERE v.id = ts.venue_id)
                WHEN ts.coach_id IS NOT NULL THEN (SELECT c.price_per_hour FROM coaches c WHERE c.id = ts.coach_id)
            END
        ) AS effective_price
    INTO v_slot
    FROM time_slots ts
    WHERE ts.id = p_time_slot_id
    FOR UPDATE;

    -- Validate slot exists
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Time slot not found' USING ERRCODE = 'P0002';
    END IF;

    -- Validate slot is available
    IF NOT v_slot.is_available THEN
        RAISE EXCEPTION 'Time slot is not available' USING ERRCODE = 'P0003';
    END IF;

    -- Check no active booking exists (defense in depth — partial unique index also prevents this)
    IF EXISTS (
        SELECT 1 FROM bookings b
        WHERE b.time_slot_id = p_time_slot_id
          AND b.status NOT IN ('cancelled')
    ) THEN
        RAISE EXCEPTION 'Time slot is already booked' USING ERRCODE = 'P0004';
    END IF;

    v_price := v_slot.effective_price;

    -- Apply active discount if any
    IF v_slot.venue_id IS NOT NULL THEN
        SELECT
            CASE
                WHEN d.discount_percent IS NOT NULL THEN v_price * (1 - d.discount_percent / 100)
                WHEN d.discount_amount IS NOT NULL THEN GREATEST(v_price - d.discount_amount, 0)
                ELSE v_price
            END
        INTO v_price
        FROM discounts d
        WHERE d.venue_id = v_slot.venue_id
          AND d.is_active = true
          AND NOW() BETWEEN d.valid_from AND d.valid_until
        ORDER BY d.created_at DESC
        LIMIT 1;
    ELSIF v_slot.coach_id IS NOT NULL THEN
        SELECT
            CASE
                WHEN d.discount_percent IS NOT NULL THEN v_price * (1 - d.discount_percent / 100)
                WHEN d.discount_amount IS NOT NULL THEN GREATEST(v_price - d.discount_amount, 0)
                ELSE v_price
            END
        INTO v_price
        FROM discounts d
        WHERE d.coach_id = v_slot.coach_id
          AND d.is_active = true
          AND NOW() BETWEEN d.valid_from AND d.valid_until
        ORDER BY d.created_at DESC
        LIMIT 1;
    END IF;

    -- Use COALESCE in case no discount was found (v_price would be NULL from failed subquery)
    v_price := COALESCE(v_price, v_slot.effective_price);

    -- Create the booking with 24h expiry for partner approval
    INSERT INTO bookings (player_id, time_slot_id, venue_id, coach_id, status, total_price, notes, expires_at)
    VALUES (p_player_id, p_time_slot_id, v_slot.venue_id, v_slot.coach_id, 'pending', v_price, p_notes, NOW() + INTERVAL '24 hours')
    RETURNING bookings.id INTO v_booking_id;

    -- Mark slot as unavailable
    UPDATE time_slots SET is_available = false WHERE id = p_time_slot_id;

    -- Return the created booking
    RETURN QUERY
    SELECT
        v_booking_id,
        p_time_slot_id,
        v_slot.venue_id,
        v_slot.coach_id,
        'pending'::TEXT,
        v_price,
        (NOW() + INTERVAL '24 hours')::TIMESTAMPTZ;
END;
$$ LANGUAGE plpgsql;

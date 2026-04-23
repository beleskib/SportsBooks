-- Migration: 0051_create_booking_participants.sql
-- Description: Tag friends on a booking so they're invited, tracked for attendance,
-- and optionally pay a split-payment share.
-- Part of v2-practical-ux: first-class multi-player bookings.

-- UP
CREATE TYPE booking_participant_status AS ENUM (
    'invited',
    'accepted',
    'declined',
    'attended',
    'no_show'
);

CREATE TABLE booking_participants (
    id               BIGSERIAL                     PRIMARY KEY,
    booking_id       BIGINT                        NOT NULL REFERENCES bookings(id) ON DELETE CASCADE ON UPDATE CASCADE,
    user_id          BIGINT                        NOT NULL REFERENCES users(id)    ON DELETE CASCADE ON UPDATE CASCADE,
    status           booking_participant_status    NOT NULL DEFAULT 'invited',
    split_payment_id BIGINT                        REFERENCES split_payments(id)    ON DELETE SET NULL ON UPDATE CASCADE,
    responded_at     TIMESTAMPTZ,
    attended_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ                   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ                   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_booking_participant UNIQUE (booking_id, user_id)
);

CREATE INDEX CONCURRENTLY idx_booking_participants_booking   ON booking_participants (booking_id);
CREATE INDEX CONCURRENTLY idx_booking_participants_user      ON booking_participants (user_id);
CREATE INDEX CONCURRENTLY idx_booking_participants_status    ON booking_participants (status);

-- DOWN
DROP INDEX IF EXISTS idx_booking_participants_status;
DROP INDEX IF EXISTS idx_booking_participants_user;
DROP INDEX IF EXISTS idx_booking_participants_booking;
DROP TABLE IF EXISTS booking_participants;
DROP TYPE IF EXISTS booking_participant_status;
